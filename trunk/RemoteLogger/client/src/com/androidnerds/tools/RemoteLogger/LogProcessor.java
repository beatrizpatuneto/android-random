/**
 * Written by Mike Novak - michael.novakjr@gmail.com
 *
 * Submit issues to http://code.google.com/p/android-random
 */
package com.androidnerds.tools.RemoteLogger;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;

import java.io.*;
import java.net.*;

public class LogProcessor extends Service
{
	private String serverAddress = "";
	private String serverPort = "";
	private Process gLogProcess = null;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		if (intent.hasExtra("address")) serverAddress = intent.getExtras().getString("address");
		if (intent.hasExtra("port")) serverPort = intent.getExtras().getString("port");
		
		new Thread(thrRunner).start();
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
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
	
	public IBinder onBind(Intent intent) {
		return mBinder;

	}
	
	private final IBinder mBinder = new Binder() {
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
				int flags) {
			try {
			return super.onTransact(code, data, reply, flags);
			} catch (Exception e) {
				return false;
			}
		}
	};
}
