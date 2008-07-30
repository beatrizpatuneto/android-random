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

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class MessageViewAdapter extends BaseAdapter
{
	private Context gCtx;
	private View gView;
	private long[] messageIds;
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
		return gCount;
	}

	public Object getItem( int position )
	{
		return messageIds[ position ];
	}

	public long getItemId( int position )
	{
		return position;
	}

	public View getView( int position, View convertView, ViewGroup parent )
	{
		ViewInflate inflate = ViewInflate.from( gCtx );
		View view = inflate.inflate( R.layout.messagelist, parent, false, null );
		
		MessagesDbAdapter gDb = new MessagesDbAdapter( gCtx );
		gDb.open();

		long id = messageIds[ position ];
		
		//query using the id.
		Cursor item = gDb.fetchMessage( id );
		Log.d( "CursorOutput", item.toString() );
		Log.d( "Cursor", "The current item id is " + id );

		item.moveTo( -1 );
		if( item.next() ) {
			String sender = item.getString( 1 );
			String body = item.getString( 2 );
			long timeMillis = item.getLong( 5 );
			int status = item.getInt( 3 );
			int direction = item.getInt( 4 );

			Log.d( "Messages", "Sender is .. " + sender );
			ImageView statusIcon = ( ImageView )view.findViewById( R.id.gStatusIcon );
			statusIcon.setImageResource( R.drawable.icon );

			TextView gSenderView = ( TextView )view.findViewById( R.id.gSender );
			Log.d( "Messages", "Setting sender as: " + sender );
			gSenderView.setText( sender );

			TextView gBodyView = ( TextView )view.findViewById( R.id.gMessage );
			Log.d( "Messages", "Setting body as: " + body );
			gBodyView.setText( body );

			//do some date parsing.
			java.util.Date gDate = new java.util.Date( timeMillis );
			SimpleDateFormat gFormatter = new SimpleDateFormat( "MM/dd/yy h:m a" );

			TextView gDateView = ( TextView )view.findViewById( R.id.gTimestamp );
			Log.d( "Messages", "Setting date as: " + gFormatter.format( gDate ) );
			gDateView.setText( gFormatter.format( gDate ) );
		}
		item.close();
		gDb.close();

		return view;
	}	

	public void parseCursor( Cursor result )
	{
		int i = 0;
		gCount = result.count();
		messageIds = new long[ gCount ];
		while( result.next() ) {
			Log.d( "CursorParser", "Adding message id: " + result.getLong( 0 ) );
			messageIds[ i ] = result.getLong( 0 );
			i++;
		}
	}

}