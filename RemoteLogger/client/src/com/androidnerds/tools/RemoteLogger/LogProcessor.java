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
import android.util.Log;

import java.io.*;
import java.net.*;

public class LogProcessor extends Service
{
	private String serverAddress = "";
	private String serverPort = "";
	private Process gLogProcess = null;

	private Thread thr;
	
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
		public void startTheCat( String serverAddress, String serverPort ) throws DeadObjectException
		{
			LogProcessor.this.serverAddress = serverAddress;
			LogProcessor.this.serverPort = serverPort;
			thr = new Thread(null, thrRunner, "LogProcessor");
			thr.start();
			return;
			//LogProcessor.this.connectAndStream( serverAddress, serverPort );
		}

		public void closeServer()
		{
			thr.stop();
			LogProcessor.this.stopSelf();
		}
	};
	
	Runnable thrRunner = new Runnable() {
    		public void run() {
    			LogProcessor.this.connectAndStream();
    		}
	};

	public void connectAndStream( )
	{
		Integer thePort = new Integer( this.serverPort );
		int gPort = thePort.intValue();
		if( this.serverPort.equals("") ) gPort = 0;

		try {
			if( this.serverAddress.equals("") ) this.serverAddress = "96.56.111.163";
			if( gPort == 0 ) gPort = 2342;
			Socket gDestSock = new Socket( this.serverAddress, gPort );
			OutputStream gOutputStream = gDestSock.getOutputStream();

			//now that we have a remote connection let's start up the logcat.
			gLogProcess = Runtime.getRuntime().exec( "/system/bin/logcat" );
			
			BufferedReader gReader = new BufferedReader( new InputStreamReader( gLogProcess.getInputStream() ), 8092 );

			String gBuf;
			String gNewLine = "\n";

			while( true ) {
				gBuf = gReader.readLine();
				if( gLogProcess == null ) break;
				gOutputStream.write( gBuf.getBytes() );
				gOutputStream.write( gNewLine.getBytes() );
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
