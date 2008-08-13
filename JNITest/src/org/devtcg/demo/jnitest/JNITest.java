package org.devtcg.demo.jnitest;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.text.DecimalFormat;

import java.io.*;

public class JNITest extends Activity
{
	public static final String TAG = "JNITest";

	public static final DecimalFormat secFmt = new DecimalFormat("#0.0000");

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.main);

		byte[] digest;
		long now;
		
		now = System.nanoTime();
		digest = NativeMD5.digestFile("/sdcard/foo");
		printResult("digestFile(/sdcard/foo)", digest, now);

		now = System.nanoTime();
		digest = digestWithStream("/sdcard/bar");
		printResult("digestStream(/sdcard/bar)", digest, now);

		now = System.nanoTime();
		digest = NativeMD5.digestFile("/sdcard/foo");
		printResult("digestFile(/sdcard/foo)", digest, now);

		now = System.nanoTime();
		digest = digestWithStream("/sdcard/bar");
		printResult("digestStream(/sdcard/bar)", digest, now);
    }

	public static void printResult(String call, byte[] digest, long start)
	{
		long diff = System.nanoTime() - start;

		Log.i(TAG, call + " = " + HexFormatter.format(digest) + ": " +
		  secFmt.format(diff / 1000000000.0) + " seconds elapsed.");
	}

	public static class HexFormatter
	{
		protected static final char[] map = "0123456789ABCDEF".toCharArray();

		public static String format(byte[] b)
		{
			StringBuffer buf = new StringBuffer();

			for (int i = 0; i < b.length; i++)
			{
				buf.append(map[(b[i] >> 4) & 0x0f]);
				buf.append(map[b[i] & 0xf]);
			}

			return buf.toString();
		}
	}

	public static byte[] digestWithStream(String file)
	{
		InputStream in = null;

		try {
			in = new FileInputStream(file);
			return NativeMD5.digestStream(in);
		} catch (IOException e) {
			Log.e(TAG, "Damn", e);
			return null;
		} finally {
			if (in != null)
				try { in.close(); } catch (IOException e) {}
		}
	}
}
