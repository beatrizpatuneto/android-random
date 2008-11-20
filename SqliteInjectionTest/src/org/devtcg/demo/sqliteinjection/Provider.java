package org.devtcg.demo.sqliteinjection;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.util.Log;

public class Provider extends ContentProvider
{
	public static final String DATABASE_NAME = "foo.db";
	public static final int DATABASE_VERSION = 1;
	
	private SQLiteOpenHelper mHelper;

	private int mOperations = 0;
	private CountDownLatch mBusyLatch;	

	private static final UriMatcher URI_MATCHER;
	
	private static final int FOO = 0;
	private static final int FOOS = 1;
	
	public static final String EXTERNAL_DATABASE_PATH = "externalPath";

	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		public DatabaseHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL(Schema.Foo.SQL.CREATE);
		}
		
		private void onDrop(SQLiteDatabase db)
		{
			db.execSQL(Schema.Foo.SQL.DROP);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			onDrop(db);
			onCreate(db);
		}
	}
	
	@Override
	public boolean onCreate()
	{
		mHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri)
	{
		switch (URI_MATCHER.match(uri))
		{
		case FOO:
			return Schema.Foo.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException(uri.toString());
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
	  String[] selectionArgs, String sortOrder)
	{
		synchronized(this) {
			if (mBusyLatch != null)
				throw new IllegalStateException("Back off...");
			
			mOperations++;			
		}

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String groupBy = null;

		int type = URI_MATCHER.match(uri);

		switch (type)
		{
		case FOOS:
			qb.setTables(Schema.Foo.SQL.TABLE);
			break;
		case FOO:
			qb.setTables(Schema.Foo.SQL.TABLE);
			qb.appendWhere("_id=" + uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException(uri.toString());
		}

		SQLiteDatabase db = mHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs,
		  groupBy, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);

		synchronized(this) {
			if (mBusyLatch != null)
				mBusyLatch.countDown();

			mOperations--;
		}

		return c;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		if (values.containsKey(EXTERNAL_DATABASE_PATH) == false)
			throw new IllegalArgumentException();

		File dbPath = new File(values.getAsString(EXTERNAL_DATABASE_PATH));
		if (dbPath.exists() == false)
			throw new IllegalArgumentException();

		synchronized(this) {
			mBusyLatch = new CountDownLatch(mOperations);
		}

		(new ReplaceDbThread(dbPath)).start();

		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
	  String[] selectionArgs)
	{
		return 0;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		return 0;
	}

	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(Schema.AUTHORITY, "foo", FOOS);
		URI_MATCHER.addURI(Schema.AUTHORITY, "foo/#", FOO);
	}

	private class ReplaceDbThread extends Thread
	{
		private File mSource;

		public ReplaceDbThread(File path)
		{
			mSource = path;
		}

		private void copyTable(SQLiteDatabase src, SQLiteDatabase dst,
		  String tableName)
		{
			dst.execSQL("DROP TABLE IF EXISTS " + tableName);

			Cursor masterCursor =
			  src.rawQuery("SELECT sql FROM sqlite_master WHERE type = 'table' and name = ?",
			    new String[] { tableName });

			try {
				if (masterCursor.moveToFirst() == false)
					return;

				String createSql = masterCursor.getString(0);
				dst.execSQL(createSql);
			} finally {
				masterCursor.close();
			}

			SQLiteStatement stmt = null;

			Cursor rows = src.rawQuery("SELECT * FROM " + tableName, null);

			try {
				dst.beginTransaction();

				int columnCount = rows.getColumnCount();

				while (rows.moveToNext() == true)
				{
					if (stmt == null)
					{
						StringBuffer valuePlaces = new StringBuffer("?");
						for (int i = 1; i < columnCount; i++)
							valuePlaces.append(",?");
						
						String insertSql = "INSERT INTO " + tableName +
						  " VALUES (" + valuePlaces + ")";
						stmt = dst.compileStatement(insertSql);
					}

					for (int i = 0; i < columnCount; i++)
					{
						try {
							byte[] v = rows.getBlob(i);
							stmt.bindBlob(i + 1, v);
						} catch (Exception e) {
							try {
								String v = rows.getString(i);
								stmt.bindString(i + 1, v);
							} catch (Exception e2) {
								try {
									double v = rows.getDouble(i);
									stmt.bindDouble(i + 1, v);
								} catch (Exception e3) {
									long v = rows.getLong(i);
									stmt.bindLong(i + 1, v);
								}
							}
						}
					}

					stmt.executeInsert();
				}

				dst.setTransactionSuccessful();
			} finally {
				if (stmt != null)
					stmt.close();

				dst.endTransaction();
				rows.close();
			}
		}

		/* Transfers all tables (except excluded) from src to dst. */
		private void transferDatabase(File src, File dst, String[] excluded)
		{
			SQLiteDatabase srcDb =
			  SQLiteDatabase.openDatabase(src.getAbsolutePath(), null,
			    SQLiteDatabase.OPEN_READONLY);

			SQLiteDatabase dstDb =
			  SQLiteDatabase.openDatabase(dst.getAbsolutePath(), null,
			    SQLiteDatabase.OPEN_READWRITE);

			HashSet<String> excludedSet =
			  new HashSet<String>(Arrays.asList(excluded));

			try {
				Cursor srcTables =
				  srcDb.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'", null);

				try {
					while (srcTables.moveToNext() == true)
					{
						String tableName = srcTables.getString(0);
						
						if (tableName.startsWith("sqlite") == true)
							continue;

						if (excludedSet.contains(tableName) == true)
							continue;
						
						copyTable(srcDb, dstDb, tableName);
					}
				} finally {
					srcTables.close();
				}

				dstDb.setVersion(srcDb.getVersion());
			} finally {
				srcDb.close();
				dstDb.close();
			}
		}

		public void run()
		{
			try {
				Log.i(SqliteInjection.TAG, "Waiting for busy latch...");
				mBusyLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}

			Log.i(SqliteInjection.TAG, "Moving database into place.");

			try {
				mHelper.close();
				File old = getContext().getDatabasePath(DATABASE_NAME);

				if (old.exists() == true)
				{
					transferDatabase(old, mSource,
					  new String[] { Schema.Foo.SQL.TABLE });

					old.delete();
				}

				if (mSource.renameTo(old) == false)
					throw new IllegalStateException("Failed to install new database.");

				/* Ensure that the new database opens. */
				mHelper.getWritableDatabase();
			} catch (Exception e) {
				e.printStackTrace();
			}

			synchronized(this) {
				mBusyLatch = null;
			}

//			getContext().getContentResolver()
//			  .notifyChange(Schema.Foo.CONTENT_URI, null);
		}
	}
}
