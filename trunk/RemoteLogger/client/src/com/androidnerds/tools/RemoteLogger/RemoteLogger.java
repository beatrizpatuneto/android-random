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

	private boolean isBinded = false;

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

	@Override
	public void onPause()
	{
		super.onPause();
		if( isBinded ) unbindService( gConnection );
	}

	@Override
	public void onResume()
	{
		super.onResume();
		workThatMagic();
	}

	public void workThatMagic()
	{
		EditText gAddressField = ( EditText )findViewById( R.id.serverAddr );
		Editable gServer = gAddressField.getText();
		serverAddress = gServer.toString();

		EditText gPortField = ( EditText )findViewById( R.id.port );
		Editable gPort = gPortField.getText();
		serverPort = gPort.toString();

		
		if( !serverAddress.equals("") ) this.bindService( new Intent( LogProcessorInterface.class.getName() ), gConnection, Context.BIND_AUTO_CREATE );
		//oh yea that's all for this method :)
	}

	private void stopLogService()
	{
		if( isBinded ) unbindService( gConnection );
	}

	private ServiceConnection gConnection = new ServiceConnection() 
	{
		public void onServiceConnected( ComponentName className, IBinder service ) 
		{
			logInterface = LogProcessorInterface.Stub.asInterface( service );
			try {
				isBinded = true;
				logInterface.startTheCat( serverAddress, serverPort );
			} catch( DeadObjectException e ) {

			}
		}

		public void onServiceDisconnected( ComponentName className )
		{
			isBinded = false;
		}
	};
}
