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

import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.util.AttributeSet;
import android.util.Log;
import android.util.XmlPullAttributes;
import android.view.Menu;
import android.view.Menu.Item;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import java.io.*;

public class Conversations extends ListActivity
{
	private ConversationViewAdapter gViewAdapter;
	private NotificationManager gNotification;
	private static int ACTIVITY_CREATE = 0;
	private static final int REPLY_ID = Menu.FIRST;
	String sender = "";
	String senderName = "";
	/** Called when the activity is first created. */
	@Override
	public void onCreate( Bundle icicle )
	{
        	super.onCreate( icicle );
        	setContentView( R.layout.main );

		Bundle extras = getIntent().getExtras();
		
		if( extras != null ) {
			sender = extras.getString( "sender" );

			Cursor c = managedQuery( android.provider.Contacts.People.CONTENT_URI, null, android.provider.Contacts.PhonesColumns.NUMBER + "='" + sender + "'", null, Contacts.People.DEFAULT_SORT_ORDER );

			while( c.next() ) {
				//check to find the person in the cursor and set their phone number as such.
				senderName = c.getString( c.getColumnIndex( android.provider.Contacts.PeopleColumns.NAME ) );
			}
			c.close();
			if( !senderName.equals( "" ) ) setTitle( "Conversation with " + senderName );
			else setTitle( "Conversation with " + sender );

			MessagesDbAdapter dbAdapter = new MessagesDbAdapter( this );
			dbAdapter.open();
			dbAdapter.markAsRead( sender );
			dbAdapter.close();
		}
	}

	protected void onResume()
	{
		super.onResume();
		gViewAdapter = new ConversationViewAdapter( this, sender );
		setListAdapter( gViewAdapter );
		getListView().setSelection( gViewAdapter.getCount() - 1 );
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		super.onCreateOptionsMenu( menu );
		menu.add( 0, REPLY_ID, "Reply", R.drawable.replymessage );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected( Menu.Item item )
	{
		switch( item.getId() ) {
			case REPLY_ID:
				replyToMessage();
				break;
		}

		return super.onOptionsItemSelected( item );
	}

	public void replyToMessage()
	{
		Intent i = new Intent( this, CreateMessage.class );
		i.putExtra( "contact", sender );
    		startSubActivity( i, ACTIVITY_CREATE );

	}

	public void markMessageAsRead( String sender )
	{

	}
}
