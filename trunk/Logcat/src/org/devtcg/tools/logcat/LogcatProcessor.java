package org.devtcg.tools.logcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class LogcatProcessor extends Thread
{
	/* TODO: Support logcat filtering. */
	public static final String[] LOGCAT_CMD = new String[] { "/system/bin/logcat" };
	private static final int BUFFER_SIZE = 1024;

	private int mLines = 0;
	protected Process mLogcatProc;

	public void run()
	{
		try
		{
			mLogcatProc = Runtime.getRuntime().exec(LOGCAT_CMD);
		}
		catch (IOException e)
		{
			onError("Can't start " + LOGCAT_CMD[0], e);
		}

		try
		{
			BufferedReader reader =
			  new BufferedReader(new InputStreamReader(mLogcatProc.getInputStream()),
			    BUFFER_SIZE);
			
			String line;
			
			while ((line = reader.readLine()) != null)
			{
				onNewline(line);
				mLines++;
			}
		}
		catch (IOException e)
		{
			onError("Error reading from process " + LOGCAT_CMD[0], e);
		}
	}

	public void stopCatter()
	{
		mLogcatProc.destroy();
	}
	
	public int getLineCount()
	{
		return mLines;
	}

	public abstract void onError(String msg, Throwable e);
	public abstract void onNewline(String line);
}
