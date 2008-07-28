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

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentReceiver;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.gsm.SmsMessage;
import android.widget.Toast;

public class MessagesReceiver extends IntentReceiver
{
	public static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

	private MessagesDbAdapter gDbHelper;
	private NotificationManager gNotification;
	private Context gCtx;

	public void onReceiveIntent( Context ctx, Intent intent )
	{
		gCtx = ctx;
		gDbHelper = new MessagesDbAdapter( ctx );
		gDbHelper.open();

		//do message related stuff here.
		if( intent.getAction().equals( ACTION ) ) {
			Bundle bundle = intent.getExtras();
			
			if( bundle != null ) {
				SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent( intent );
				//take the messages and add them to the database.
				for( int i = 0; i < messages.length; i++ ) {
					SmsMessage message = messages[ i ];
					String sender = message.getDisplayOriginatingAddress();
					String body = message.getDisplayMessageBody();
					gDbHelper.createMessage( sender, body, 0, 0, message.getTimestampMillis() );

					//message has been inserted into the database. notify the user.
					this.setupNotification( sender, body );
				}
			}
		}

		gDbHelper.close();
	}

	public void setupNotification( String sender, String body )
	{
		gNotification = ( NotificationManager )getSystemService( NOTIFICATION_SERVICE );
		
		Intent contentIntent = new Intent( gCtx, Messages.class );
		Intent appIntent = new Intent( gCtx, Messages.class );
		String app_name = "Messages";

		Notification newMessage = new Notification( this, R.drawable.icon, body.subSequence( 0,  body.length() ), System.currentTimeMillis(), sender.subSequence( 0, sender.length() ), body.subSequence( 0, body.length() ), contentIntent, R.drawable.icon, app_name.subSequence( 0, app_name.length() ), appIntent );
		gNotification.notify( R.string.new_message, newMessage );
	}
}