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
package com.androidnerds.tools.Glance;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.widget.Button;
import android.widget.EditText;

import java.io.*;
import java.util.Hashtable;

/** Class for managing the files on the system and creating new ones. **/
public class FileManager
{
	private Context gCtx;
	private Hashtable<String, String> gClipboard = new Hashtable<String, String>();
	public String gLastItem;
	String gPath;
	Dialog gDialog;

	public FileManager( Context ctx )
	{
		gCtx = ctx;
	}

	public boolean createNewDirectory( String path )
	{
		gPath  = path;
		Log.d( "FileManager", "Creating new directory in.... " + path );
		gDialog = new Dialog( gCtx );
		gDialog.setContentView( R.layout.textdialog );
		gDialog.setTitle( "Create new directory:" );
		final Button gCreateButton = ( Button )gDialog.findViewById( R.id.gCreateButton );
		gCreateButton.setOnClickListener( new Button.OnClickListener()  
		{
			public void onClick( View v )
			{
				makeDirectory( gPath );
			}
		} );
		gDialog.show();
		return true;
	}
	
	public boolean createNewFile( String path )
	{
		gPath  = path;
		Log.d( "FileManager", "Creating new file in.... " + path );
		gDialog = new Dialog( gCtx );
		gDialog.setContentView( R.layout.textdialog );
		gDialog.setTitle( "Create new file:" );
		final Button gCreateButton = ( Button )gDialog.findViewById( R.id.gCreateButton );
		gCreateButton.setOnClickListener( new Button.OnClickListener()  
		{
			public void onClick( View v )
			{
				createFile( gPath );
			}
		} );
		gDialog.show();
		return true;
	}

	//This method takes the selected apk and moves it to the /data/app directory.
	public boolean installApk( String path, String file )
	{
		try {
			FileInputStream src = new FileInputStream( path + "/" + file );
			BufferedReader reader = new BufferedReader( new InputStreamReader( src ), 8092 );
			File destFile = new File( "/data/app/" + file );
			destFile.createNewFile();
			FileOutputStream dest = new FileOutputStream( destFile );

			int piece;
			while( ( piece = reader.read() ) >= 0 ) {
				dest.write( piece );
			}
		} catch( IOException e ) {
			Log.d( "FileIO", "Problem installing the apk" );
			Log.d( "FileIO", e.toString() );
		}
		return true;
	}

	public boolean isApk( String file )
	{
		return file.endsWith( ".apk" );
	}

	public boolean makeDirectory( String path )
	{
		try {
			//gDialog.close();
			EditText newName = ( EditText )gDialog.findViewById( R.id.gNewItemName );
			Editable textItem = newName.getText();
			File newFile = new File( path + "/" + textItem.toString() );
			Log.d( "FileIO", "Creating... " + path + "/" + textItem.toString() );
			newFile.mkdirs();
			Log.d( "FileManager", "Making new directory.... " + path );
		} catch( Exception e ) {
			Log.d( "FileIO", "issue creating new directory" );
		}
		return true;
	}

	public boolean createFile( String path )
	{
		try {
			//gDialog.close();
			EditText newName = ( EditText )gDialog.findViewById( R.id.gNewItemName );
			Editable textItem = newName.getText();
			File newFile = new File( path + "/" + textItem.toString() );
			Log.d( "FileIO", "Creating... " + path + "/" + textItem.toString() );
			newFile.createNewFile();
			Log.d( "FileManager", "Making new file.... " + path );
		} catch( IOException e ) {
			Log.d( "FileIO", "issue creating new file" );
		}
		return true;
	}

	public boolean addToClipboard( String item, String action )
	{
		gClipboard.put( item, action );
		gLastItem  = item;
		checkClipboard();
		return true;
	}

	public boolean pasteFromClipboard( String dest )
	{
		//let's find out what the desired action is and then perform that.
		String action = gClipboard.get( gLastItem );
		if( action.equals( "copy" ) ) pasteFromCopy( gLastItem, dest );
		if( action.equals( "cut" ) ) {
			pasteFromCut( gLastItem, dest );
			gClipboard.remove( gLastItem );
		}
		return true;
	}

	private void pasteFromCopy( String item, String dest )
	{
		try {
			FileInputStream src = new FileInputStream( item );
			BufferedReader reader = new BufferedReader( new InputStreamReader( src ), 8092 );
			File destFile = new File( dest + "/" + item.substring( item.lastIndexOf( "/" ) ) );
			destFile.createNewFile();
			FileOutputStream destStream = new FileOutputStream( destFile );

			int piece;
			while( ( piece = reader.read() ) >= 0 ) {
				destStream.write( piece );
			}
		} catch( IOException e ) {
			Log.d( "FileIO", "Problem copying the item." );
			Log.d( "FileIO", e.toString() );
		}
	}

	private void pasteFromCut( String item, String dest )
	{
		try {
			File srcFile = new File( item );
			FileInputStream src = new FileInputStream( item );
			BufferedReader reader = new BufferedReader( new InputStreamReader( src ), 8092 );
			File destFile = new File( dest + "/" + item.substring( item.lastIndexOf( "/" ) ) );
			destFile.createNewFile();
			FileOutputStream destStream = new FileOutputStream( destFile );

			int piece;
			while( ( piece = reader.read() ) >= 0 ) {
				destStream.write( piece );
			}
		
			//now that the item is copied over to the destination remove it from the source.
			srcFile.delete();
		} catch( IOException e ) {
			Log.d( "FileIO", "Problem copying the item." );
			Log.d( "FileIO", e.toString() );
		}
	}

	public boolean removeFile( String item )
	{
		File itemFile = new File( item );
		itemFile.delete();
		return true;
	}

	public void checkClipboard()
	{
		Log.d( "FileIO", "Clipboard contents: " + gClipboard.toString() );
	}
}