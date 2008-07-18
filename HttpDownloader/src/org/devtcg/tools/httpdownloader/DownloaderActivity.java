package org.devtcg.tools.httpdownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DownloaderActivity extends Activity 
{
	private EditText mSrcView;
	private EditText mDstView;
	private Button mGetBtn;

	private Download mDownload;
	private DownloadProcessor mThread;
	private ProgressDialog mProgress;
	
	private static final int MSG_FINISHED = 0;
	private static final int MSG_STATUS = 1;
	private static final int MSG_LENGTH = 2;
	private static final int MSG_PROGRESS = 3;
	private static final int MSG_ERROR = 4;
	
	private static final int MSG_STATUS_SUCCESS = 0;

	private final Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case MSG_FINISHED:
				mProgress.dismiss();
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
				mProgress.setIndeterminate(false);
				mProgress.setProgress(0);
				mDownload.length = (Long)msg.obj;
				break;
			case MSG_PROGRESS:
				if (mDownload.length >= 0)
				{
					float prog =
					  ((float)((Long)msg.obj) / (float)mDownload.length) * 10000f;

					mProgress.setProgress((int)prog);
				}
				break;
			case MSG_ERROR:
				mDownload.dst.delete();
				mProgress.dismiss();
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
        
        mSrcView = (EditText)findViewById(R.id.src);
        mDstView = (EditText)findViewById(R.id.dst);
        
        mGetBtn = (Button)findViewById(R.id.get);
        mGetBtn.setOnClickListener(mGetClick);
    }
    
    @Override
    public void onDestroy()
    {
    	mThread.stopDownload();
    	super.onDestroy();
    }
    
    private final OnClickListener mGetClick = new OnClickListener()
    {
    	public void onClick(View v)
    	{
    		mProgress = ProgressDialog.show(DownloaderActivity.this, "Downloading...",
    		  "Connecting to server...", true, true, mGetCancelClick);
    		
    		String src = mSrcView.getText().toString();
    		String dst = mDstView.getText().toString();

    		mDownload = new Download(new URL(src), new File(dst));
    	}
    };
    
    private final OnCancelListener mGetCancelClick = new OnCancelListener()
    {
		public void onCancel(DialogInterface di)
		{
			mThread.stopDownload();
		}
    };
    
    static final class Download
    {
    	URL url;
    	File dst;
    	long length;
    	
    	public Download(URL url, File dst)
    	{
    		this.url = url;
    		this.dst = dst;
    	}    	
    }
    
    private static final class DownloadProcessor extends Thread
    {
    	private Download mDownload;
    	private Handler mHandler;

    	private volatile HttpClient mClient = null;
    	private GetMethod mMethod = null;

    	public void DownloadProcessor(Download d, Handler handler)
    	{
    		mDownload = d;
    		mHandler = handler;
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
    			out = new FileOutputStream(mDownload.dst);

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
    				sendProgress(n);
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
    		mDownload.dst.delete();
    	}
    }
}