/**
 * Written by Mike Novak - michael.novakjr@gmail.com
 *
 * Submit issues to http://code.google.com/p/android-random
 */
package com.androidnerds.tools.RemoteLogger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RemoteLogger extends Activity
{
	private String serverAddress;
	private String serverPort;

	private Intent mService;
	
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
		//if( isBinded ) unbindService( gConnection );
	}

	public void stopLogService() {
		stopService(mService);
	}
	
	public void workThatMagic()
	{
		EditText gAddressField = ( EditText )findViewById( R.id.serverAddr );
		Editable gServer = gAddressField.getText();
		serverAddress = gServer.toString();

		EditText gPortField = ( EditText )findViewById( R.id.port );
		Editable gPort = gPortField.getText();
		serverPort = gPort.toString();

		mService = new Intent(this, LogProcessor.class);
		mService.putExtra("address", serverAddress);
		mService.putExtra("port", serverPort);
		
		startService(mService);
		
		
		//if( !serverAddress.equals("") ) this.bindService( new Intent( LogProcessorInterface.class.getName() ), gConnection, Context.BIND_AUTO_CREATE );
		//oh yea that's all for this method :)
	}
}
