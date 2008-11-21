package org.devtcg.demo.sqliteinjection;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObservable;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class Provider extends ContentProvider
{
	public static final String DATABASE_NAME = "foo.db";
	public static final int DATABASE_VERSION = 1;
	
	private SQLiteOpenHelper mHelper;

	private int mOperations = 0;
	private CountDownLatch mBusyLatch;

	/* Tracks cursors so that if we swap out the underlying database we
	 * can properly support requery. */
	private final Set<MyCursorWrapper> mCursors =
	  Collections.synchronizedSet(new HashSet<MyCursorWrapper>());

	private static final UriMatcher URI_MATCHER;

	private static final int FOO = 0;
	private static final int FOOS = 1;

	public static final String EXTERNAL_DATABASE_PATH = "externalPath";

	private class DatabaseHelper extends SQLiteOpenHelper
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
		String sql = qb.buildQuery(projection, selection, selectionArgs,
		  groupBy, null, sortOrder, null);
		Cursor c = db.rawQuery(sql, selectionArgs);
		c.setNotificationUri(getContext().getContentResolver(), uri);

		synchronized(this) {
			if (mBusyLatch != null)
				mBusyLatch.countDown();

			mOperations--;
		}
		
		return new MyCursorWrapper(c, sql, selectionArgs);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		/* Normally you would discriminate by creating a custom URI, but since
		 * our provider is mostly empty we can just abuse the general insert
		 * method. */
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
	
	private class MyCursorWrapper extends CursorWrapper
	{
		private final String mSql;
		private final String[] mSelArgs;
		
		/* Necessary because CursorAdapter expects observer notifications to
		 * fire in the thread that created the cursor. */
		private Handler mTarget;

		private MyDataSetObservable mDataSetObservable =
		  new MyDataSetObservable();

		private MyContentObservable mContentObservable =
		  new MyContentObservable();

		public MyCursorWrapper(Cursor cursor, String sql, String[] selArgs)
        {
	        super(cursor);

			Log.i(SqliteInjection.TAG, "SQL=" + sql);

	        mSql = sql;
	        mSelArgs = selArgs;

	        mTarget = new Handler();

	        mCursors.add(this);
        }

		public void close()
		{
			Log.i(SqliteInjection.TAG, "close");

			mContentObservable.unregisterAll();
			super.close();

			mCursors.remove(this);
		}

		/* The database was closed, we must close the underlying cursor. */ 
		public void notifyDatabaseInvalidated()
		{
			Log.i(SqliteInjection.TAG, "notifyDatabaseInvalidated");
			
			mTarget.post(new Runnable() {
				public void run() {
					mCursor.close();					
				}
			});
		}

		public void notifyDatabaseChanged(SQLiteDatabase db)
		{
			Log.i(SqliteInjection.TAG, "notifyDatabaseChanged");

			/* Unregister all observers from the old cursor. */
			for (DataSetObserver o: mDataSetObservable.getObservers())
				mCursor.unregisterDataSetObserver(o);

			for (ContentObserver o: mContentObservable.getObservers())
				mCursor.unregisterContentObserver(o);

			mCursor = db.rawQuery(mSql, mSelArgs);

			/* ...and register them to the new one. */
			for (DataSetObserver o: mDataSetObservable.getObservers())
				mCursor.registerDataSetObserver(o);

			for (ContentObserver o: mContentObservable.getObservers())
				mCursor.registerContentObserver(o);

			/* Simulate requery(). */
			mTarget.post(new Runnable() {
				public void run() {
					mDataSetObservable.notifyChanged();					
				}
			});
		}

		@Override
        public void registerContentObserver(ContentObserver observer)
        {
	        mContentObservable.registerObserver(observer);
	        super.registerContentObserver(observer);
        }

		@Override
        public void unregisterContentObserver(ContentObserver observer)
        {
	        mContentObservable.unregisterObserver(observer);
	        super.unregisterContentObserver(observer);
        }

		@Override
        public void registerDataSetObserver(DataSetObserver observer)
        {
			mDataSetObservable.registerObserver(observer);
	        super.registerDataSetObserver(observer);
        }
		
		@Override
        public void unregisterDataSetObserver(DataSetObserver observer)
        {
			mDataSetObservable.unregisterObserver(observer);
	        super.unregisterDataSetObserver(observer);
        }

		private class MyDataSetObservable extends DataSetObservable
		{
			public ArrayList<DataSetObserver> getObservers()
			{
				return mObservers;
			}
		}
		
		private class MyContentObservable extends ContentObservable
		{
			public ArrayList<ContentObserver> getObservers()
			{
				return mObservers;
			}
		}
	}

	private class ReplaceDbThread extends Thread
	{
		private File mSource;

		public ReplaceDbThread(File path)
		{
			mSource = path;
		}

		private void copyData(SQLiteDatabase dst, SQLiteDatabase src,
		  String tableName)
		{
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

		private void copyTable(SQLiteDatabase dst, SQLiteDatabase src,
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

			copyData(dst, src, tableName);
		}

		/* Transfers all tables (except excluded) from src to dst. */
		private void transferDatabase(SQLiteDatabase dst, SQLiteDatabase src,
		  String[] excluded)
		{
			HashSet<String> excludedSet =
			  new HashSet<String>(Arrays.asList(excluded));

			Cursor srcTables =
			  src.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'", null);

			dst.beginTransaction();

			try {
				while (srcTables.moveToNext() == true)
				{
					String tableName = srcTables.getString(0);

					if (tableName.startsWith("sqlite") == true)
						continue;

					if (excludedSet.contains(tableName) == true)
						continue;

					copyTable(dst, src, tableName);
				}

				dst.setTransactionSuccessful();
			} finally {
				dst.endTransaction();
				srcTables.close();
			}
		}

		public void run()
		{
			SQLiteDatabase newDb = null;

			try {
				File oldPath;
				
				try {
					Log.i(SqliteInjection.TAG, "Waiting for busy latch...");
					mBusyLatch.await();

					Log.i(SqliteInjection.TAG, "Moving database into place.");

					newDb = SQLiteDatabase.openDatabase(mSource.getAbsolutePath(), null,
					  SQLiteDatabase.OPEN_READWRITE);

					SQLiteDatabase oldDb = mHelper.getReadableDatabase();

					/* Transfer any meta tables into the new database. */
					transferDatabase(newDb, oldDb,
					  new String[] { Schema.Foo.SQL.TABLE });

					newDb.setVersion(oldDb.getVersion());

					oldPath = new File(oldDb.getPath());
				} catch (Exception e) {
					mSource.delete();
					throw e;
				} finally {
					newDb.close();
				}

				mHelper.close();
				
				for (MyCursorWrapper c: mCursors)
					c.notifyDatabaseInvalidated();

				/* Perform the swap. */
				oldPath.delete();
				mSource.renameTo(oldPath);

				/* Confirm it all worked and reinitialize. */
				SQLiteDatabase currentDb = mHelper.getWritableDatabase();

				for (MyCursorWrapper c: mCursors)
					c.notifyDatabaseChanged(currentDb);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Log.i(SqliteInjection.TAG, "Yay!");

				synchronized(this) {
					mBusyLatch = null;
				}
			}
		}
	}
}
