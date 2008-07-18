/**
 * Written by Mike Novak - mike@novaklabs.com
 *
 * Submit patches to remotelogger@novaklabs.com
 */
package com.androidnerds.tools.RemoteLogger;

import android.content.Context;
import android.content.Intent;
import android.content.IntentReceiver;

public class LogProcessorReceiver extends IntentReceiver
{
	@Override
	public void onReceiveIntent( Context context, Intent intent )
	{
		context.startService( new Intent( context, LogProcessor.class ), null );
	}
}