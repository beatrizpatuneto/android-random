/* Copyright (c) 2008 AndroidNerds
 *
 * Written and Maintained by the random guys.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package com.androidnerds.tools.Messages;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.SQLiteDatabase;
import android.util.Log;

import java.io.FileNotFoundException;

public class MessagesDbAdapter
{
	public static final String KEY_SENDER = "sender";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_ROWID = "_id";

	private static final String DATABASE_NAME = "an_sms";
	private static final String DATABASE_TABLE = "an_messages";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE =
		"create table an_messages( _id integer primary key autoincrement, " +
		"sender text not null, message text not null, message_date datetime not null );";

	private SQLiteDatabase gDb;
	private final Context gCtx;

	public MessagesDbAdapter( Context ctx )
	{
		this.gCtx = ctx;
	}

	public MessagesDbAdapter open()
	{
		try {
			gDb = gCtx.openDatabase( DATABASE_NAME, null );
		} catch( Exception e ) {
			try {
				gDb = gCtx.createDatabase( DATABASE_NAME, DATABASE_VERSION, 0, null );
				gDb.execSQL( DATABASE_CREATE );
			} catch( FileNotFoundException e1 ) {
				throw new SQLException( "Could not create database" );
			}
		}

		//extra code to make sure the database table exists.
		try {
			Cursor c = this.fetchAllMessages();
		} catch( Exception e ) {
			gDb.execSQL( DATABASE_CREATE );
		}

		return this;
	}

	public void close()
	{
		gDb.close();
	}

	public long createMessage( String sender, String message )
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put( KEY_SENDER, sender );
		initialValues.put( KEY_MESSAGE, message );
		
		return gDb.insert( DATABASE_TABLE, null, initialValues );
	}

	public boolean deleteMessage( long rowId )
	{
		return gDb.delete( DATABASE_TABLE, KEY_ROWID + "=" + rowId, null ) > 0;
	}

	public Cursor fetchAllMessages()
	{
		return gDb.query( true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_SENDER, KEY_MESSAGE }, null, null, null, null, null );
	}

}