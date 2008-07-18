package com.androidnerds.tools.RemoteLogger;

import android.app.Activity;
import android.content.Intent;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.Context;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RemoteLogger extends Activity
{
	private LogProcessorInterface logInterface;
	private String serverAddress;
	private String serverPort;

	/** Called when the activity is first created. */
	@Override
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		setContentView( R.layout.main );

		final Button gMagicButton = ( Button )findViewById( R.id.gButton );
		gMagicButton.setOnClickListener( new Button.OnClickListener()  
		{
			public void onClick( View v )
			{
				workThatMagic();
			}
		} );
		final Button gStopButton = ( Button )findViewById( R.id.gStopButton );
		gStopButton.setOnClickListener( new Button.OnClickListener()  
		{
			public void onClick( View v )
			{
				stopLogService();
			}
		} );
	}

	public void workThatMagic()
	{
		EditText gAddressField = ( EditText )findViewById( R.id.serverAddr );
		Editable gServer = gAddressField.getText();
		serverAddress = gServer.toString();

		EditText gPortField = ( EditText )findViewById( R.id.port );
		Editable gPort = gPortField.getText();
		serverPort = gPort.toString();

		this.bindService( new Intent( LogProcessorInterface.class.getName() ), gConnection, Context.BIND_AUTO_CREATE );
		//oh yea that's all for this method :)
	}

	private void stopLogService()
	{
		unbindService( gConnection );
	}

	private ServiceConnection gConnection = new ServiceConnection() 
	{
		public void onServiceConnected( ComponentName className, IBinder service ) 
		{
			logInterface = LogProcessorInterface.Stub.asInterface( service );
			try {
				boolean result = logInterface.startTheCat( serverAddress, serverPort );
				if( !result ) Log.d( "LogWriter", "Hm, writing to the log that we are supposed to be streaming..." );
			} catch( DeadObjectException e ) {

			}
		}

		public void onServiceDisconnected( ComponentName className )
		{
			try {
				boolean result = logInterface.closeServer();
				if( !result ) Log.d("LogWriter", "Hm, not good when the logger needs to write to the log :) " );
			} catch( DeadObjectException e ) {
				//don't really have anything for here yet.
			}
		}
	};
}
