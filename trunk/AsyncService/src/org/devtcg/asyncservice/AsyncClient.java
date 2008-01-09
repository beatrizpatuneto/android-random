package org.devtcg.asyncservice;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AsyncClient extends Activity
{
    private static final String TAG = "AsyncClient";
    
    private Handler mHandler = new Handler();
    private IAsyncService mService;

    private TextView mCounterText;

    private boolean mCounting = false;
    
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        
        mCounterText = (TextView)findViewById(R.id.counter);
        
        Button b = (Button)findViewById(R.id.start);
        b.setOnClickListener(mFire);

        bindService(new Intent(this, AsyncService.class), null, mConnection,
          Context.BIND_AUTO_CREATE);
    }
    
    protected OnClickListener mFire = new OnClickListener()
    {
        public void onClick(View view)
        {
            if (mService == null)
            {
                Log.d(TAG, "Nothing to do!");
                return;
            }

            if (mCounting == true)
            {
                /* This would be simple to work around, if you're curious. */
                Log.d(TAG, "mCounter is implemented globally and cannot be reused while counting is in progress.");
                return;
            }

            try
            {
                mCounting = true;
                mService.startCount(10, mCounter);
                Log.d(TAG, "Counting has begun...");
            }
            catch (DeadObjectException e)
            {
                mCounting = false;
                Log.d(TAG, Log.getStackTraceString(e));
            }
        }
    };
    
    private ServiceConnection mConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            Log.d(TAG, "onServiceConnected");
            mService = IAsyncService.Stub.asInterface((IBinder)service);
        }
        
        public void onServiceDisconnected(ComponentName className)
        {
            Log.d(TAG, "onServiceDisconnected");
            mService = null;
        }
    };
    
    private IAsyncServiceCounter.Stub mCounter = new IAsyncServiceCounter.Stub()
    {
        public void handleCount(final int n)
        {
            Log.d(TAG, "handleCount(" + n + ")");

            mHandler.post(new Runnable()
            {
                public void run()
                {
                    if (n == 10)
                    {
                        mCounterText.setText("Done!");
                        mCounting = false;
                    }
                    else
                        mCounterText.setText(String.valueOf(n));
                }
            });
        }
    };
}
