package org.devtcg.tools.httpdownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Resources;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DownloaderActivity extends Activity 
{
	public static final String TAG = "DownloaderActivity";
	
	private SharedPreferences mPrefs;
	
	private EditText mSrcView;
	private EditText mDstView;
	private Button mGetBtn;

	private DownloadProcessor mThread;
	private ProgressDialog mProgress;
	
	private static final int MSG_FINISHED = 0;
	private static final int MSG_STATUS = 1;
	private static final int MSG_LENGTH = 2;
	private static final int MSG_PROGRESS = 3;
	private static final int MSG_ERROR = 4;
	
	private static final int MSG_STATUS_SUCCESS = 0;
	
	private static final String PREF_LAST_SRC_URL = "lastSrcURL";
	private static final String PREF_LAST_DST_PATH = "lastDstPath";

	private final Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			/* We introduce a race condition on cancellation... */
			if (mThread == null || mProgress == null)
				return;
			
			Download dl = mThread.getDownload();
			
			switch (msg.what)
			{
			case MSG_FINISHED:
				Log.d(TAG, "MSG_FINISHED");
				
				Editor prefs = mPrefs.edit();
				prefs.putString(PREF_LAST_SRC_URL, dl.url.toString());
				prefs.putString(PREF_LAST_DST_PATH, dl.dst.toString());
				prefs.commit();
				
				mProgress.dismiss();
				mProgress = null;
				break;
			case MSG_STATUS:
				switch (msg.arg1)
				{
				case MSG_STATUS_SUCCESS:
					mProgress.setMessage("Receiving content...");
					break;
				}
				break;
			case MSG_LENGTH:
				Log.d(TAG, "MSG_LENGTH: " + (Long)msg.obj);
				//mProgress.setIndeterminate(false);
				mProgress.setProgress(0);
				dl.length = (Long)msg.obj;
				break;
			case MSG_PROGRESS:
				Log.d(TAG, "MSG_PROGRESS: " + (Long)msg.obj);
				if (dl.length >= 0)
				{
					float prog =
					  ((float)((Long)msg.obj) / (float)dl.length) * 100f;

					mProgress.setProgress((int)(prog * 100f));
					mProgress.setMessage("Received " + (int)prog + "%");
				}
				else
				{
					mProgress.setMessage("Received " + (Long)msg.obj + " bytes");
				}
				break;
			case MSG_ERROR:
				dl.abortCleanup();
				mProgress.dismiss();
				mProgress = null;
				Toast.makeText(DownloaderActivity.this, "Error: " + msg.obj,
				  Toast.LENGTH_LONG).show();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	};

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        
        mPrefs = getSharedPreferences("prefs", 0);
        
        mSrcView = (EditText)findViewById(R.id.src);
        mDstView = (EditText)findViewById(R.id.dst);
      
        mSrcView.setText(mPrefs.getString(PREF_LAST_SRC_URL,
          getResources().getString(R.string.default_src_url)));
        mDstView.setText(mPrefs.getString(PREF_LAST_DST_PATH,
          getResources().getString(R.string.default_dst_path)));
        
        mGetBtn = (Button)findViewById(R.id.get);
        mGetBtn.setOnClickListener(mGetClick);
    }
    
    @Override
    public void onDestroy()
    {
    	mThread.stopDownload();
    	mThread = null;

    	super.onDestroy();
    }
    
    private final OnClickListener mGetClick = new OnClickListener()
    {
    	public void onClick(View v)
    	{
    		String src = mSrcView.getText().toString();
    		String dst = mDstView.getText().toString();
    		
    		URL srcUrl;

    		try
    		{
    			srcUrl = new URL(src);
    		}
    		catch (MalformedURLException e)
    		{
    			Toast.makeText(DownloaderActivity.this,
    			  "Invalid source URL: " + e.toString(),
    			  Toast.LENGTH_SHORT).show();

    			return;
    		}

    		mProgress = ProgressDialog.show(DownloaderActivity.this, "Downloading...",
    	    		  "Connecting to server...", true, true, mGetCancelClick);

    		Download dl = new Download(srcUrl, new File(dst));
    		mThread = new DownloadProcessor(dl, mHandler);
    		mThread.start();
    	}
    };
    
    private final OnCancelListener mGetCancelClick = new OnCancelListener()
    {
		public void onCancel(DialogInterface di)
		{
			mProgress.dismiss();
			mProgress = null;
			
			mThread.stopDownload();
			mThread = null;
		}
    };

    static final class Download
    {
    	URL url;
    	boolean directory;
    	File dst;
    	String name;
    	long length;

    	public Download(URL url, File dst)
    	{
    		this.url = url;
    		this.dst = dst;

    		/* Figure out the filename to save to from the URL.  Note that
    		 * it would be better to override once the HTTP server responds,
    		 * since a better name will have been provided, possibly after
    		 * redirect.  But I don't care right now. */
    		if ((directory = dst.isDirectory()) == true)
    		{
    			String[] paths = url.getPath().split("/");

    			int n = paths.length;
    			
    			if (n > 0)
    				n--;

    			if (paths[n].length() > 0)
    				name = paths[n];
    			else
    				name = "index.html";
    		}
    	}
    	
    	public File getDestination()
    	{
    		File f;
    		
    		if (directory == true)
    			f = new File(dst.getAbsolutePath() + File.separator + name);
    		else
    			f = dst;
    		
    		return f;
    	}

    	/**
    	 * Delete the destination file, if it exists.
    	 */
    	public void abortCleanup()
    	{
    		getDestination().delete();
    	}
    }
    
    private static final class DownloadProcessor extends Thread
    {
    	private Download mDownload;
    	private Handler mHandler;

    	private volatile HttpClient mClient = null;
    	private GetMethod mMethod = null;

    	public DownloadProcessor(Download d, Handler handler)
    	{
    		mDownload = d;
    		mHandler = handler;
    	}
    	
    	public Download getDownload()
    	{
    		return mDownload;
    	}

    	public void run()
    	{
    		mClient = new HttpClient(new SimpleHttpConnectionManager());
    		
    		synchronized (this) {
    			mMethod = new GetMethod(mDownload.url.toString());
    		}

    		InputStream in = null;
    		FileOutputStream out = null;

    		try
    		{
    			int st = mClient.executeMethod(mMethod);
    			
    			if (interrupted() == true)
    				return;

    			if (st != HttpStatus.SC_OK)
    			{
    				sendError(mMethod.getStatusLine().toString());
    				return;
    			}

    			sendStatus(MSG_STATUS_SUCCESS);
    			
    			long len;

    			if ((len = mMethod.getResponseContentLength()) >= 0)
    				sendLength(len);

    			in = mMethod.getResponseBodyAsStream();
    			out = new FileOutputStream(mDownload.getDestination());

    			byte[] b = new byte[1024];
    			int n;
    			long bytes = 0;

    			if (interrupted() == true)
    				return;

    			while ((n = in.read(b)) >= 0)
    			{
    				if (interrupted() == true)
    					return;
    				
    				bytes += n;
    				sendProgress(bytes);
    				out.write(b, 0, n);
    			}
    			
    			sendFinished();
    		}
    		catch (HttpException e)
    		{
    			sendError("Fatal protocol violation: " + e.getMessage());
    		}
    		catch (IOException e)
    		{
    			sendError("Fatal I/O error: " + e.toString());
    		}
    		finally
    		{
    			/* A no-op, I think, but still looks nice for symmetry. */
    			if (in != null)
    				try { in.close(); } catch (IOException e) {}

    			/* The real operation. */
   				mMethod.releaseConnection();
   				mMethod = null;

    			if (out != null)
    			{
    				try { out.close(); }
    				catch (IOException e) { sendError("Fatal I/O error: " + e.toString()); }
    			}
    		}
    	}
    	
    	public void sendFinished()
    	{
    		Message msg = mHandler.obtainMessage(MSG_FINISHED);
    		mHandler.sendMessage(msg);
    	}

    	public void sendStatus(int statusOp)
    	{
    		Message msg = mHandler.obtainMessage(MSG_STATUS, statusOp, -1);
    		mHandler.sendMessage(msg);
    	}
    	
    	public void sendLength(long len)
    	{
    		Message msg = mHandler.obtainMessage(MSG_LENGTH, (Long)len);
    		mHandler.sendMessage(msg);
    	}

    	public void sendProgress(long n)
    	{
    		mHandler.removeMessages(MSG_PROGRESS);
    		Message msg = mHandler.obtainMessage(MSG_PROGRESS, (Long)n);
    		mHandler.sendMessage(msg);
    	}

    	public void sendError(String err)
    	{
    		Message msg = mHandler.obtainMessage(MSG_ERROR, err);
    		mHandler.sendMessage(msg);
    	}

    	public void stopDownload()
    	{
   			if (mMethod == null)
   				return;

    		interrupt();
    		mDownload.abortCleanup();
    	}
    }
}