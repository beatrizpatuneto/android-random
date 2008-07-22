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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class DirectoryAdapter extends BaseAdapter
{
	private Context gContext;
	private String gDirectory;
	private ArrayList<String> gChildNames = new ArrayList<String>();
	private ArrayList<String> gChildTypes = new ArrayList<String>();
	private ArrayList<String> gFullPath = new ArrayList<String>();
	private LinkedList<String> gHistory = new LinkedList<String>();

	public DirectoryAdapter( Context c )
	{
		gContext = c;
	}

	public int getCount()
	{
		return gChildNames.size();
	}

	public Object getItem( int position )
	{
		return position;
	}

	public String get( int position ) {
		return gChildNames.get( position );
	}

	public long getItemId( int position )
	{
		return position;
	}

	public View getView( int position, View convertView, ViewGroup parent )
	{
		ViewInflate inflate = ViewInflate.from( gContext );
		View gView = inflate.inflate( R.layout.list_row, parent, false, null );

		TextView gNameView = ( TextView )gView.findViewById( R.id.gFileName );
		if( position == 0 ) gNameView.setText( " .. " );
		else gNameView.setText( gChildNames.get( position ) );

		ImageView gIconView = ( ImageView )gView.findViewById( R.id.gIconHolder );
		if( position == 0 ) {
			gIconView.setImageResource( R.drawable.parent );
		} else { 
			if( gChildTypes.get( position ).equals( "directory" ) ) gIconView.setImageResource( R.drawable.folder );
			else gIconView.setImageResource( R.drawable.file );
		}

		return gView;
	}

	public void loadParentDirectory( )
	{
		Log.d( "FileIO", "Checking the history... " + gHistory.toString() );
		gHistory.removeLast();
		Log.d( "FileIO", "New history written..." + gHistory.toString() );
		fillDirectoryListing( ( String )gHistory.getLast() );
	}

	public void loadSubDirectory( int position )
	{
		if( gDirectory.equals( "/") ) gDirectory = "";
		Log.d( "Glance", "loadSubDirectory has been called. Loading: " + gDirectory + "/" + gChildNames.get( position ) );
		File gTemp = new File( gDirectory + "/" + gChildNames.get( position ) );
		if( !gTemp.isFile() )
			fillDirectoryListing( gDirectory + "/" + gChildNames.get( position ) );
	}

	public void fillDirectoryListing( String directory )
	{
		if( gHistory.size() > 0 ) {
			if( !directory.equals( gHistory.getLast() ) )  gHistory.add( directory );
		} else {
			gHistory.add( directory );
		}
		gChildNames.clear();
		gChildTypes.clear();
		gDirectory = directory;

		//Let's see what the File permissions are for what we are looking at.
		FilePermission gFilePerms = new FilePermission( directory + "/-", "read,write,execute,delete" );
		File gCurrentLocation = new File( directory );
		//let's see what is actually in this directory....
		String[] gListing = gCurrentLocation.list();
		
		if( !gCurrentLocation.canRead() ) {
			//Toast.makeText( this, "Cannot read the selected directory.", Toast.LENGTH_LONG ).show();
			return;
		}
	
		Log.d( "FileIO", "Current listing size: " + gListing.length );
		int i;
		for( i = 0; i < gListing.length; i++ ) {
			File gTemp = new File( gListing[ i ] );

			//Simple debugging code that won't be here forever.
			Log.d( "FileIO", "Found file: " + gListing[ i ] );

			if( gTemp.isDirectory() ) {
				gChildNames.add( gListing[ i ] );
				gChildTypes.add( "directory" );
			} else if( gTemp.isFile() ) {
				gChildNames.add( gListing[ i ] );
				gChildTypes.add( "file" );
			} else {
				gChildNames.add( gListing[ i ] );
				gChildTypes.add( "unknown" );
			}
		}

		notifyDataSetChanged();
	}

}