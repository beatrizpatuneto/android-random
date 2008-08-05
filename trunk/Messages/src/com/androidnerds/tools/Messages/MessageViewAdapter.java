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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

public class MessageViewAdapter extends BaseAdapter
{
	private Context gCtx;
	private View gView;
	private ArrayList<Long> messageIds = new ArrayList<Long>();
	private int gCount;

	public MessageViewAdapter( Context c )
	{
		gCtx = c;
		MessagesDbAdapter gDb = new MessagesDbAdapter( gCtx );
		gDb.open();

		parseCursor( gDb.fetchUniqueUsers() );

		gDb.close();
	}

	public int getCount()
	{
		return messageIds.size();
	}

	public Object getItem( int position )
	{
		return messageIds.get( position );
	}

	public long getItemId( int position )
	{
		return position;
	}

	public String getSender( int position )
	{
		MessagesDbAdapter gDb = new MessagesDbAdapter( gCtx );
		gDb.open();
		long id = messageIds.get( position ).longValue();
		Log.d( "MessagesView", "Trying for position..." + messageIds.get( position ).longValue() );
		Cursor item = gDb.fetchMessage( id );
		Log.d( "MessagesView", "Cursor output is.... "  + item.toString() );
		item.moveTo( -1 );

		String returnItem = new String( " " );
		if( item.next() ) {
			Log.d( "MessagesView", "Cursor sender: " + item.getString( 1 ) );
			returnItem = item.getString( 1 );
		}

		item.close();
		gDb.close();
		return returnItem;
	}

	public View getView( int position, View convertView, ViewGroup parent )
	{
		ViewInflate inflate = ViewInflate.from( gCtx );
		View view = inflate.inflate( R.layout.messagelist, parent, false, null );
		gView = view;

		MessagesDbAdapter gDb = new MessagesDbAdapter( gCtx );
		gDb.open();

		long id = messageIds.get( position ).longValue();
		
		//query using the id.
		Cursor item = gDb.fetchMessage( id );

		item.moveTo( -1 );
		if( item.next() ) {
			String sender = item.getString( 1 );
			String body = item.getString( 2 );
			long timeMillis = item.getLong( 5 );
			int status = item.getInt( 3 );
			int direction = item.getInt( 4 );

			ImageView statusIcon = ( ImageView )view.findViewById( R.id.gStatusIcon );
			statusIcon.setImageResource( R.drawable.conversation );
			
			//See if the sender is one of the contacts.
			ContentResolver resolver = gCtx.getContentResolver();
			Cursor c = resolver.query( android.provider.Contacts.People.CONTENT_URI, null, android.provider.Contacts.PhonesColumns.NUMBER + "='" + sender + "'", null, Contacts.People.DEFAULT_SORT_ORDER );

			while( c.next() ) {
				//check to find the person in the cursor and set their phone number as such.
				sender = c.getString( c.getColumnIndex( android.provider.Contacts.PeopleColumns.NAME ) );
			}
			c.close();

			TextView gSenderView = ( TextView )view.findViewById( R.id.gSender );
			if( status == 0 ) gSenderView.setTypeface( Typeface.DEFAULT_BOLD );
			gSenderView.setText( sender );

			TextView gBodyView = ( TextView )view.findViewById( R.id.gMessage );
			if( status == 0 ) gBodyView.setTypeface( Typeface.DEFAULT_BOLD );
			gBodyView.setText( body );

			//do some date parsing.
			boolean useMinutes = false;
			java.util.Date gDate = new java.util.Date( timeMillis );
			java.util.Date now = new java.util.Date( System.currentTimeMillis() );
			SimpleDateFormat date = new SimpleDateFormat( "MM/dd/yy" );
			if( date.format( gDate ).equals( date.format( now )  ) ) useMinutes = true;
			SimpleDateFormat timeFormat = new SimpleDateFormat( "hh:mm a" );

			TextView gDateView = ( TextView )view.findViewById( R.id.gTimestamp );
			if( status == 0 ) gDateView.setTypeface( Typeface.DEFAULT_BOLD );
			if( useMinutes ) gDateView.setText( timeFormat.format( gDate ) );
			else gDateView.setText( date.format( gDate ) );
		}
		item.close();
		gDb.close();

		return view;
	}	

	public void parseCursor( Cursor result )
	{
		messageIds.clear();
		Log.d( "CursorParser", "Cursor output = " + result.toString() );

		while( result.next() ) {
			Log.d( "CursorParser", "Adding message id: " + result.getLong( 0 ) );
			messageIds.add( new Long( result.getLong( 0 ) ) );
		}
	}

	public void alertDataChanged()
	{
		MessagesDbAdapter gDb = new MessagesDbAdapter( gCtx );
		gDb.open();

		parseCursor( gDb.fetchUniqueUsers() );

		gDb.close();

		Log.d( "MessagesView", "Message Ids state: " + messageIds.toString() );
		notifyDataSetChanged();
		gView.requestLayout();
	}

}