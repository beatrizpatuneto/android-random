package org.devtcg.demo.sqliteinjection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class SqliteInjection extends Activity
{
	public static final String TAG = "SqliteInjection";

	private ListView mList;
	private SimpleCursorAdapter mAdapter;
	private Button mGetDb;
	private Button mReQueryDb;

	private ProgressDialog mProgress;
	
	private final Handler mHandler = new Handler();

	private static final String[] QUERY_FIELDS =
	  { Schema.Foo._ID, Schema.Foo.NAME, Schema.Foo.FOO, Schema.Foo.BAR,
		Schema.Foo.BAZ };
	
	private Cursor getCursor()
	{
		Log.i(TAG, "Handing out a new cursor...");

        Cursor c = getContentResolver().query(Schema.Foo.CONTENT_URI,
          QUERY_FIELDS, null, null, null);

        c.registerContentObserver(new ContentObserver(new Handler()) {
        	public void onChange(boolean selfChange) {
        		Log.d(TAG, "onChange: selfChange=" + selfChange);
        	}
        });
        
        return c;
	}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mGetDb = (Button)findViewById(R.id.getDatabase);
        mGetDb.setOnClickListener(mClick);
        
        mReQueryDb = (Button)findViewById(R.id.queryDatabase);
        mReQueryDb.setOnClickListener(mClick);
        
        mList = (ListView)findViewById(R.id.list);

        Cursor c = getCursor();

        mAdapter = new SimpleCursorAdapter(this, R.layout.foo_item, c,
          new String[] { Schema.Foo.NAME, Schema.Foo.FOO, Schema.Foo.BAR,
            Schema.Foo.BAZ },
          new int[] { R.id.foo_name, R.id.foo_foo, R.id.foo_bar,
            R.id.foo_baz });

        mAdapter.registerDataSetObserver(new DataSetObserver() {
        	public void onChanged() {
        		Log.e(TAG, "onChanged");
        	}

        	public void onInvalidated() {
        		Log.e(TAG, "onInvalidated");
        	}
        });
        
        mList.setAdapter(mAdapter);
    }
    
    @Override
    protected void onDestroy()
    {
    	mAdapter.changeCursor(null);
    	super.onDestroy();
    }

    private final OnClickListener mClick = new OnClickListener()
    {
    	public void onClick(View v)
    	{
    		switch (v.getId())
    		{
    		case R.id.getDatabase:
    			mProgress = ProgressDialog.show(SqliteInjection.this,
    			  "Downloading", "Downloading database...");
    			(new ReplaceDbThread()).start();
    			break;
    		case R.id.queryDatabase:
    			mAdapter.getCursor().requery();
    			break;
    		}
    	}
    };

    private class ReplaceDbThread extends Thread
    {
    	public void run()
    	{
    		URL url;
    		
    		InputStream in = null;
    		OutputStream out = null;

    		try {
				url = new URL("http://jasta.dyndns.org/android/foo.db");
   				
				File dbPath = getDatabasePath("foo-remote.db");
   				
				in = url.openStream();
				out = new FileOutputStream(dbPath);
   				
				byte[] b = new byte[2048];
				int n;

				while ((n = in.read(b)) >= 0)
					out.write(b, 0, n);

   				Log.i(TAG, "Installing database...");
   				ContentValues cv = new ContentValues();
   				cv.put(Provider.EXTERNAL_DATABASE_PATH,
   				  dbPath.getAbsolutePath());
   				getContentResolver().insert(Schema.Foo.CONTENT_URI, cv);

   				mProgress.dismiss();
    		} catch (Exception e) {
    			final String msg = e.toString();
    			mHandler.post(new Runnable() {
    				public void run() {
    	    			Toast.makeText(SqliteInjection.this, "Fuck: " + msg,
    	    	    	  Toast.LENGTH_LONG).show();
    				}
    			});
    		} finally {
    			if (in != null)
    				try { in.close(); } catch (IOException e) {}
    			
    			if (out != null)
    				try { out.close(); } catch (IOException e) {}
    		}
    	}
    }
}