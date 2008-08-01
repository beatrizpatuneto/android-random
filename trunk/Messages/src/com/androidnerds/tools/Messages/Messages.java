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
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.Menu.Item;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;

public class Messages extends ListActivity
{
	private MessageViewAdapter gViewAdapter;
	private NotificationManager gNotification;
	private static int ACTIVITY_CREATE = 0;
	private static final int NEW_MESSAGE_ID = Menu.FIRST;
	private static final int ABOUT_ID = Menu.FIRST + 1;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle)
	{
		gNotification = ( NotificationManager )getSystemService( NOTIFICATION_SERVICE );
		gNotification.cancel( R.string.new_message );

        	super.onCreate(icicle);
        	setContentView(R.layout.main);

		gViewAdapter = new MessageViewAdapter( this );
		setListAdapter( gViewAdapter );

	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		super.onCreateOptionsMenu( menu );
		//TODO: make the icons have the asterisk that means 'new'
		menu.add( 0, NEW_MESSAGE_ID, "New Message", R.drawable.newmessage );
		menu.add( 0, ABOUT_ID, "About", R.drawable.info );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected( Menu.Item item )
	{
		switch( item.getId() ) {
			case NEW_MESSAGE_ID:
				sendNewMessage();
				break;
			case ABOUT_ID:
				Toast.makeText( this, "Version 0.0.014\nWritten By Mike Novak\nReport bugs: mike@novaklabs.com", Toast.LENGTH_LONG ).show();
				break;
		}

		return super.onOptionsItemSelected( item );
	}

	@Override
	protected void onListItemClick( ListView l, View v, int position, long id ) 
	{
		super.onListItemClick( l, v, position, id );
		
		TextView gSender = ( TextView )v.findViewById( R.id.gSender );
		Intent subAct = new Intent( this, Conversations.class );
		subAct.putExtra( "sender", gSender.getText().toString() );
		startSubActivity( subAct, ACTIVITY_CREATE );
	}

	public void sendNewMessage()
	{
		Intent subAct = new Intent( this, CreateMessage.class );
		startSubActivity( subAct, ACTIVITY_CREATE );
	}
}
