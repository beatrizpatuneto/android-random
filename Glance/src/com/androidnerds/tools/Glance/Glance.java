package com.androidnerds.tools.Glance;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.io.*;

/** This is a really simple elementary application that displays directories and files on the Android system. **/
public class Glance extends Activity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		setContentView( R.layout.main );
		
		//grab the GridView from the layout file.
		GridView gIconView = ( GridView )findViewById( R.id.gIconView );

		//setup the default user directory if needed.
		setUpUserDefault();
	}

	public void setUpUserDefault()
	{
		//check to see if it exists. if not create it.
		File gHomeDir = new File( "/data/home/" );
		if( gHomeDir.exists() ) {
			return;
		} else {
			gHomeDir.mkdir();
			Process gMkProc = Runtime.getRuntime().exec( "chmod 777 /data/home" );
		}

		return;
	}

	public String[] getDirectoryListing( String gPath )
	{
		//create the file object to work with and check to see if its a directory.
		File gCurrentFile = new File( gPath );
		if( !gCurrentFile.isDirectory() ) return { "" };

		return gCurrentFile.list();
	}
}
