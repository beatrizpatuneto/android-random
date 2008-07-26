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

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.Menu.Item;
import android.view.View;
import android.view.View.OnPopulateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.ContextMenuInfo;
import android.widget.ImageButton;
import android.widget.ListView;

/** This is a really simple elementary application that displays directories and files on the Android system. **/
/** The application menu allows for creating new files and folders. **/
public class Glance extends ListActivity
{
	DirectoryAdapter gAdapter;
	FileManager gFileManager;
	public ListView gListView;

	private static final int NEW_FOLDER_ID = Menu.FIRST;
	private static final int NEW_FILE_ID = Menu.FIRST + 1;
	private static final int COPY_ACTION = 0;
	private static final int CUT_ACTION = 1;
	private static final int PASTE_ACTION = 2;
	private static final int DELETE_ACTION = 3;

	/** Called when the activity is first created. */
	@Override
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		setContentView( R.layout.main );
		
		gAdapter = new DirectoryAdapter( this );
		setListAdapter( gAdapter );

		gFileManager = new FileManager( this );

		gAdapter.fillDirectoryListing( "/" );
		setTitle( "Glance - / " );
		determineContextMenu();
	}

	@Override
	protected void onListItemClick( ListView l, View v, int position, long id ) 
	{
		super.onListItemClick( l, v, position, id );
		if( position == 0 ) gAdapter.loadParentDirectory();
		else gAdapter.loadSubDirectory( position );
		
		//change the title of the activity to display the current directory
		setTitle( "Glance - " + gAdapter.getDirectoryName( ) );
	}

	@Override
    	public boolean onContextItemSelected(Item item) 
	{ 
		AdapterView.ContextMenuInfo gMenuInfo = ( AdapterView.ContextMenuInfo )item.getMenuInfo();
		Log.d( "ContextMenu", "Testing Context menu..." + gMenuInfo.position );
		Log.d( "ContextMenu", "Option that was selected is...." + item.getTitle() );
		Log.d( "ContextMenu", "Menu Item selected is: " + gAdapter.getDirectoryName( ) + "/" + gAdapter.getObject( gMenuInfo.position ) ); 
		String object = gAdapter.getDirectoryName() + "/" + gAdapter.getObject( gMenuInfo.position );
		if( gFileManager.isApk( object ) && item.getTitle().equals( "Install" ) ) gFileManager.installApk( gAdapter.getDirectoryName(), gAdapter.getObject( gMenuInfo.position ) );

		String directoryName;
		if( gAdapter.getDirectoryName( ).equals( "/" ) ) directoryName = "";
		else directoryName = gAdapter.getDirectoryName();

		//use a switch statement to see what option was selected.
		if( item.getTitle().equals( "Copy" ) )
			gFileManager.addToClipboard( directoryName + "/" + gAdapter.getObject( gMenuInfo.position ), "copy" );
		else if( item.getTitle().equals( "Cut" )  )
			gFileManager.addToClipboard( directoryName + "/" + gAdapter.getObject( gMenuInfo.position ), "cut" );
		else if( item.getTitle().equals( "Paste" ) )
			gFileManager.pasteFromClipboard( directoryName + "/" + gAdapter.getObject( gMenuInfo.position ) );
		else if( item.getTitle().equals( "Delete" ) )
			gFileManager.removeFile( directoryName + "/" + gAdapter.getObject( gMenuInfo.position ) );
		else
			Log.d( "ContextMenu", "Hm, you should have chosen an option here." );

		gAdapter.alertDataChanged();
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		super.onCreateOptionsMenu( menu );
		//TODO: make the icons have the asterisk that means 'new'
		menu.add( 0, NEW_FOLDER_ID, "New Folder", R.drawable.folder );
		menu.add( 0, NEW_FILE_ID, "New File", R.drawable.file );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected( Menu.Item item )
	{
		switch( item.getId() ) {
			case NEW_FOLDER_ID:
				gFileManager.createNewDirectory( gAdapter.getDirectoryName() );
				break;
			case NEW_FILE_ID:
				gFileManager.createNewFile( gAdapter.getDirectoryName() );
				break;
		}
		return super.onOptionsItemSelected( item );
	}

	//The following method is setup to determine the type of context menu to display to the user.
	public void determineContextMenu()
	{
		View gListView = getListView();
		gListView.setOnPopulateContextMenuListener(
            			new View.OnPopulateContextMenuListener() {
					@Override
          				public void onPopulateContextMenu(ContextMenu menu, View view, Object menuInfo) 
					{
            					AdapterView.ContextMenuInfo mi = (AdapterView.ContextMenuInfo) menuInfo;
						String gItem = gAdapter.getDirectoryName() + "/" + gAdapter.getObject( mi.position );
						if( gItem.endsWith( ".apk" ) ) menu.add( 0, 0, "Install" );
						menu.add( 0, 0, "Copy" );
						menu.add( 0, 0, "Cut" );
						menu.add( 0, 0, "Paste" );
						menu.add( 0, 0, "Delete" );
          				}
		});
	}
}
