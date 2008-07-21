/* Copyright (c) 2008 AndroidNerds
 *
 * Written and Maintained by Mike Novak <mike@novaklabs.com>
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

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/** This is a really simple elementary application that displays directories and files on the Android system. **/
public class Glance extends ListActivity
{
	public ListView gListView;
	public ArrayList<HashMap<String, String>> gDirChildren = new ArrayList<HashMap<String, String>>();
	SimpleAdapter files;

	/** Called when the activity is first created. */
	@Override
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		setContentView( R.layout.main );

		//simple little starter test.
		fillDirectoryListing( "/system" );	
		
		String[] from = new String[] { "name", "type" };
		int[] to = new int[] { R.id.gFileName, R.id.gFileType };
		files = new SimpleAdapter( this, gDirChildren, R.layout.list_row, from, to );
		setListAdapter( files );
	}
	
	public void fillDirectoryListing( String gPath )
	{
		//Let's see what the File permissions are for what we are looking at.
		FilePermission gFilePerms = new FilePermission( gPath + "/-", "read,write,execute,delete" );
		File gCurrentLocation = new File( gPath );
		//let's see what is actually in this directory....
		String[] gListing = gCurrentLocation.list();

		if( !gCurrentLocation.canRead() ) {
			Toast.makeText( this, "Cannot read the selected directory.", Toast.LENGTH_LONG ).show();
			return;
		}
	
		Log.d( "FileIO", "Current listing size: " + gListing.length );
		int i;
		for( i = 0; i < gListing.length; i++ ) {
			HashMap<String, String> item = new HashMap<String, String>();
			File gTemp = new File( gListing[ i ] );

			//Simple debugging code that won't be here forever.
			Log.d( "FileIO", "Found file: " + gListing[ i ] );

			if( gTemp.isDirectory() ) {
				item.put( "name", gListing[ i ] );
				item.put( "type", "directory" );
			} else if( gTemp.isFile() ) {
				item.put( "name", gListing[ i ] );
				item.put( "type", "file" );
			} else {
				item.put( "name", gListing[ i ] );
				item.put( "type", "unknown" );
			}
		
			gDirChildren.add( item );
		}
	}
}
