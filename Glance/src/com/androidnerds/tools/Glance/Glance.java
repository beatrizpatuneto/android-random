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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.util.Log;

import java.io.*;

/** This is a really simple elementary application that displays directories and files on the Android system. **/
public class Glance extends Activity
{
	public GridView gIconView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		setContentView( R.layout.main );
		
		//grab the GridView from the layout file.
		gIconView = ( GridView )findViewById( R.id.gIconView );

		//get the directory listing and set it to an adapter.
		String[] gListing = getDirectoryListing( "/" );
		View[] gViews = prepareViewArray( gListing );
		
	}

	public String[] getDirectoryListing( String gPath )
	{
		//create the file object to work with and check to see if its a directory.
		File gCurrentFile = new File( gPath );

		return gCurrentFile.list();
	}

	public View[] prepareViewArray( String[] gDirListing )
	{
		View[] gTmpArr = new View[50];
		int i;
		for( i = 0; i < gDirListing.length(); i++ ) {
			View gView = new View();
			gView.setClickable( true );
			
		}
	}
}
