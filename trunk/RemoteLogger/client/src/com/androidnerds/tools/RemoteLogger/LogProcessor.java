/**
 * Written by Mike Novak - mike@novaklabs.com
 *
 * Submit patches to remotelogger@novaklabs.com
 */
package com.androidnerds.tools.RemoteLogger;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Handler;
import android.os.Bundle;
import android.os.DeadObjectException;

import java.io.*;
import java.net.*;

public class LogProcessor extends Service
{
	private String serverAddress = "";
	private int serverPort = 0;
	private Process gLogProcess = null;

	@Override
	public void onCreate()
	{
		super.onCreate();
	}

	@Override
	public void onStart( int startId, Bundle args )
	{
		super.onStart( startId, args );
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public IBinder onBind( Intent intent )
	{
		return gBinder;
	}

	private final LogProcessorInterface.Stub gBinder = new LogProcessorInterface.Stub()
	{
		public boolean startTheCat( String serverAddress, String serverPort ) throws DeadObjectException
		{
			LogProcessor.this.connectAndStream( serverAddress, serverPort );
			return true;
		}

		public boolean closeServer()
		{
			LogProcessor.this.stopSelf();
			return true;
		}
	};

	public void connectAndStream( String serverAddress, String serverPort )
	{
		Integer thePort = new Integer( serverPort );
		int gPort = thePort.intValue();
		try {
			if( serverAddress.equals("") ) serverAddress = "96.56.111.163";
			if( gPort == 0 ) gPort = 2342;
			Socket gDestSock = new Socket( serverAddress, gPort );
			OutputStream gOutputStream = gDestSock.getOutputStream();
			
			//now that we have a remote connection let's start up the logcat.
			gLogProcess = Runtime.getRuntime().exec( "/system/bin/logcat" );
			
			BufferedReader gReader = new BufferedReader( new InputStreamReader( gLogProcess.getInputStream() ), 8092 );

			String gBuf;
	
			while( true ) {
				gBuf = gReader.readLine();
				if( gLogProcess == null ) break;
				gOutputStream.write( gBuf.getBytes() );
			}
			
			if( gReader != null ) gReader.close();
			gLogProcess.destroy();
			gLogProcess = null;
			gDestSock.close();
		} catch( Exception e ) {
			//error.
		}
	}
}
