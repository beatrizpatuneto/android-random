package org.devtcg.asyncservice;

import android.app.Service;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.util.Log;

public class AsyncService extends Service
{
    private static final String TAG = "AsyncService";
    
    @Override
    protected void onCreate()
    {
        Log.d(TAG, "onCreate");
    }
    
    @Override
    protected void onDestroy()
    {
        /* TODO: Of course we would need to clean up. */
        Log.d(TAG, "onDestroy");
    }
    
    @Override
    public IBinder getBinder()
    {
        return mBinder;
    }

    private final IAsyncService.Stub mBinder = new IAsyncService.Stub()
    {
        public void startCount(final int to, final IAsyncServiceCounter callback)
        {
            Thread t = new Thread()
            {
                /* Survives interruption, but not otherwise more precise. */
                public void preciseSleep(long millis)
                {
                    long endTime = System.currentTimeMillis() + millis;

                    do {
                        try
                        {
                            Thread.sleep(endTime - System.currentTimeMillis());
                        }
                        catch (InterruptedException e)
                        {}
                    } while (System.currentTimeMillis() < endTime);
                }

                public void run()
                {
                    for (int i = 1; i <= to; i++)
                    {
                        preciseSleep(1000);

                        try
                        {
                            callback.handleCount(i);
                        }
                        catch (DeadObjectException e)
                        {
                            Log.d(TAG, "Dead peer, aborting...", e);
                            break;
                        }
                    }
                }
            };

            t.start();
        }
    };
}
