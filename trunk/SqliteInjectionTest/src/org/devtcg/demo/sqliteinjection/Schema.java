package org.devtcg.demo.sqliteinjection;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Schema
{
	public static final String AUTHORITY = "org.devtcg.demo.sqliteinjection";
	
	public interface Foo extends BaseColumns
	{
		public static final String CONTENT_TYPE = "org.devtcg.demo.sqliteinjection/whatever";
		
		public static final Uri CONTENT_URI =
		  Uri.parse("content://" + AUTHORITY + "/foo");
		
		public static final String NAME = "name";
		public static final String FOO = "foo";
		public static final String BAR = "bar";
		public static final String BAZ = "baz";
		
		public static final class SQL
		{
			public static final String TABLE = "foo";
			
			public static final String CREATE =
			  "CREATE TABLE " + TABLE + " (" +
			  "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			  NAME + " TEXT UNIQUE NOT NULL, " +
			  FOO + " TEXT, " +
			  BAR + " INTEGER, " +
			  BAZ + " DATETIME " +
			  ");";
			
			public static final String DROP =
			  "DROP TABLE IF EXISTS " + TABLE + ";";
		}
	}
}
