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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;

public class CreateMessage extends Activity
{

	private String[] contactNames;
	@Override
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );

		setContentView( R.layout.createmessage );

		final Button cancelButton = ( Button )findViewById( R.id.createCancelBtn );
		cancelButton.setOnClickListener( new Button.OnClickListener()  
		{
			public void onClick( View v )
			{
				finish();
			}
		} );

		final Button sendButton = ( Button )findViewById( R.id.createSendBtn );
		sendButton.setOnClickListener( new Button.OnClickListener()
		{
			public void onClick( View v )
			{
				//do something.
			}
		} );

		ContentResolver content = getContentResolver();
        	Cursor cursor = content.query( Contacts.People.CONTENT_URI, PEOPLE_PROJECTION, null, null, Contacts.People.DEFAULT_SORT_ORDER );
        	ContactListAdapter adapter = new ContactListAdapter( cursor, this );

        	AutoCompleteTextView textView = ( AutoCompleteTextView )findViewById( R.id.contactPerson );
		textView.setThreshold( 1 );
        	textView.setAdapter( adapter );
	}

	public static class ContactListAdapter extends CursorAdapter implements Filterable 
	{
        	public ContactListAdapter( Cursor c, Context context ) {
            		super( c, context );
            		mContent = context.getContentResolver();
        	}

        	@Override
        	public View newView( Context context, Cursor cursor, ViewGroup parent ) {
            		TextView view = new TextView( context );
            		view.setText( cursor, 5 );
            		return view;
        	}

        	@Override
        	public void bindView( View view, Context context, Cursor cursor ) {
            		( ( TextView ) view ).setText( cursor, 5 );
        	}

       		@Override
        	protected String convertToString( Cursor cursor ) {
            		return cursor.getString( 5 );
        	}

        	@Override
        	protected Cursor runQuery( CharSequence constraint ) {
            		StringBuilder buffer = null;
            		String[] args = null;
            		if( constraint != null ) {
                		buffer = new StringBuilder();
                		buffer.append("UPPER(");
                		buffer.append( Contacts.ContactMethods.NAME );
                		buffer.append( ") GLOB ?" );
                		args = new String[] { constraint.toString().toUpperCase() + "*" };
            		}

            		return mContent.query( Contacts.People.CONTENT_URI, PEOPLE_PROJECTION, buffer == null ? null : buffer.toString(), args, Contacts.People.DEFAULT_SORT_ORDER );
        	}

        	private ContentResolver mContent;
    	}

    	private static final String[] PEOPLE_PROJECTION = new String[] {
        	Contacts.People._ID,
        	Contacts.People.PREFERRED_PHONE_ID,
        	Contacts.People.TYPE,
        	Contacts.People.NUMBER,
        	Contacts.People.LABEL,
        	Contacts.People.NAME,
        	Contacts.People.COMPANY
   	};

	@Override
	public void onDestroy( )
	{
		super.onDestroy();
	}

	private void sendMessage()
	{
		//SmsManager manager = SmsManager.getDefault();
		//manager.sendTextMessage( "9177033050", null, "I love you!", null, null, null );

		//its important to set the new message in the database so we can track it in the conversation.
		//we put the sender as the person who receives it and set the direction column to denote its outgoing.
		
	}
}