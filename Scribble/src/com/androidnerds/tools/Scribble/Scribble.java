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
package com.androidnerds.tools.Scribble;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.Menu.Item;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class Scribble extends Activity
{
	
	private static final int SAVE_ID = Menu.FIRST;
	private static final int SAVEAS_ID = Menu.FIRST + 1;
	private static final int ABOUT_ID = Menu.FIRST + 2;
	private File gFile;
	private String gFilename = new String();
	private String gFileContents;
	private Dialog gDialog;
	private SharedPreferences gPrefs;
	private String gDirectory;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
        	setContentView(R.layout.main);

		//we want to be able to specify a file to open off the bat if one's provided. ( i.e if Glance is trying to open a text file for us to read and/or modify. )
		Bundle extras = getIntent().getExtras();
	
		if( extras != null ) {
			gFilename = extras.getString( "filename" );
		}
		
		gPrefs = getPreferences( MODE_PRIVATE );
		gDirectory = gPrefs.getString( "directory", "/" );
		//first thing to check is see if the file is writable. If not alert the user this is read only.
		checkFilePermissions();
	}

	protected void onPause()
	{
		super.onPause();

		if( gFilename.equals( "" ) ) {
			EditText fileContents = ( EditText )findViewById( R.id.gDocumentView );
			String contents = fileContents.getText().toString();

			//save the contents of the file in the persistent state.
			SharedPreferences.Editor ed = gPrefs.edit();
			ed.putString( "filecontents", contents );
			ed.commit();
		}
	}

	protected void onResume()
	{
		super.onResume();
		
		if( gFilename.equals( "" ) ) {
			//pull the contents of the file from persistent state.
			gFileContents = gPrefs.getString( "filecontents", " " );
		
			//setup the EditText view with the persistent contents.
			EditText contents = ( EditText )findViewById( R.id.gDocumentView );
			contents.setText( gFileContents );
		}
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		super.onCreateOptionsMenu( menu );
		//TODO: make the icons have the asterisk that means 'new'
		menu.add( 0, SAVE_ID, "Save", R.drawable.save );
		menu.add( 0, SAVEAS_ID, "Save As...", R.drawable.saveas );
		menu.add( 0, ABOUT_ID, "About", R.drawable.info );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected( Menu.Item item )
	{
		switch( item.getId() ) {
			case SAVE_ID:
				writeFileToDisk();
				break;
			case SAVEAS_ID:
				promptSaveDialog();
				break;
			case ABOUT_ID:
				Toast.makeText( this, "Version 0.0.1\nWritten By Mike Novak\nReport bugs: mike@novaklabs.com", Toast.LENGTH_LONG ).show();
				break;
		}

		return super.onOptionsItemSelected( item );
	}

	public void checkFilePermissions()
	{
		if( gFilename.equals( "" ) ) return;

		gFile = new File( gFilename );
		if( !gFile.canWrite() ) {
			Toast.makeText( this, "Opening current file as read-only.", Toast.LENGTH_LONG ).show();
		}

		readFromFile();
	}

	public void readFromFile()
	{
		try {
			byte[] buffer = new byte[ ( int )gFile.length() ];
			FileInputStream reader = new FileInputStream( gFile );
			reader.read( buffer );

			gFileContents = new String( buffer );
			EditText gTextField = ( EditText )findViewById( R.id.gDocumentView );
			gTextField.setText( gFileContents );
			setTitle( "Scribble - " + gFilename );
			reader.close();
		} catch( IOException e ) {
			Log.d( "Scribble IO", "Problem parsing the file" );
		}
	}

	public void writeFileToDisk()
	{
		try {
			EditText gTextField = ( EditText )findViewById( R.id.gDocumentView );
			String text = gTextField.getText().toString();

			FileWriter writer = new FileWriter( gFile, false );
			writer.write( text );
			
			writer.close();
			Toast.makeText( this, "File has been saved.", Toast.LENGTH_LONG ).show();

			//for the sake of sanity let's store the previous directory as a starting point for the file.
			String directory = gFilename.substring( 0, gFilename.lastIndexOf( "/" ) );
			SharedPreferences.Editor ed = gPrefs.edit();
			ed.putString( "lastDirectory",  directory );
			ed.commit();
		} catch( IOException e ) {
			Log.d( "Scribble IO", "Problem writing the file to disk" );
		}
	}

	public void promptSaveDialog()
	{
		gDialog = new Dialog( this );
		gDialog.setContentView( R.layout.savedialog );
		gDialog.setTitle( "Save As... ( Enter full path )" );
		
		EditText filenameField = ( EditText )gDialog.findViewById( R.id.gNewFileName );
		filenameField.setText( gDirectory );

		final Button gCreateButton = ( Button )gDialog.findViewById( R.id.gCreateButton );
		gCreateButton.setOnClickListener( new Button.OnClickListener()  
		{
			public void onClick( View v )
			{
				saveFileAs();
			}
		} );

		gDialog.show();
	}

	public void saveFileAs()
	{
		EditText filenameField = ( EditText )gDialog.findViewById( R.id.gNewFileName );
		String filename = filenameField.getText().toString();
		File newfile = new File( filename );
		if( !newfile.canWrite() ) {
			Toast.makeText( this, "No write access to " + filename + ".", Toast.LENGTH_LONG ).show();
			gDialog.dismiss();
			return;
		}
		
		gFile = newfile;

		if( newfile.exists() ) {
			//alert the masses, make sure this file is cool to overwrite!
			new AlertDialog.Builder( this )
                        .setTitle(R.string.alertTitle)
                        .setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick( DialogInterface dialog, int whichButton ) {

				gDialog.dismiss();
                                writeFileToDisk();
                            }
                        })
                        .setNegativeButton( R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick( DialogInterface dialog, int whichButton ) {

                                cancelOperation();
                            }
                        })
                        .show();
            	}
	}

	public void cancelOperation()
	{
		gDialog.dismiss();
		return;
	}
}
