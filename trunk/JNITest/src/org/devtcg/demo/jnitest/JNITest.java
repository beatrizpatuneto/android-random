package org.devtcg.demo.jnitest;

import android.app.*;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;
import android.text.*;
import android.text.style.*;
import android.graphics.*;

import java.text.DecimalFormat;

import java.io.*;

public class JNITest extends Activity
{
	public static final String TAG = "JNITest";

	private static final DecimalFormat secFmt = new DecimalFormat("#0.0000");

	protected static final String FILE1 = "/sdcard/foo";
	protected static final String FILE2 = "/sdcard/bar";

	private Button mGo;
	private LinearLayout mResults;
	private Thread mRunning;
	private ProgressDialog mProgress;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.main);

		mGo = (Button)findViewById(R.id.go);
		mGo.setOnClickListener(mGoClick);

		mResults = (LinearLayout)findViewById(R.id.results);
    }

	private final OnClickListener mGoClick = new OnClickListener()
	{
		public void onClick(View v)
		{
			mResults.removeAllViews();

			mGo.setEnabled(false);
			mProgress = ProgressDialog.show(JNITest.this,
			  "Running test", "Please be patient while the test runs...", true);

			mRunning = new Thread()
			{
				public void run()
				{
					byte[] r;
					long now;

					now = System.nanoTime();
					r = NativeMD5.digestFile(FILE1);
					showResult("native", FILE1, r, now);

					now = System.nanoTime();
					r = digestWithStream(FILE2);
					showResult("hybrid", FILE2, r, now);

					now = System.nanoTime();
					r = NativeMD5.digestFile(FILE1);
					showResult("native", FILE1, r, now);

					now = System.nanoTime();
					r = digestWithStream(FILE2);
					showResult("hybrid", FILE2, r, now);

					UIThreadUtilities.runOnUIThread(JNITest.this, new Runnable() {
						public void run()
						{
							mGo.setEnabled(true);
							mProgress.dismiss();
						}
					});
				}

				public void showResult(String call, String file, byte[] r, 
				  long then)
				{
					double elapsed = (System.nanoTime() - then) / 1000000000.0;
					UIThreadUtilities.runOnUIThread(JNITest.this,
					  new ResultRunnable(call, file, r, elapsed));
				}
			};

			mRunning.start();
		}
	};

	private class ResultRunnable implements Runnable
	{
		private String call;
		private String file;
		private byte[] digest;
		private double elapsed;

		public ResultRunnable(String call, String file, byte[] digest,
		  double elapsed)
		{
			this.call = call;
			this.file = file;
			this.digest = digest;
			this.elapsed = elapsed;
		}

		public void run()
		{
			ResultFormattedString msg =
			  new ResultFormattedString(call, file, 
			    HexFormatter.format(digest), secFmt.format(elapsed));

			TextView res = new TextView(JNITest.this);
			res.setText(msg);

			res.setTextAppearance(JNITest.this,
			  android.R.style.TextAppearance_Medium);

			mResults.addView(res, new LayoutParams(LayoutParams.FILL_PARENT,
			  LayoutParams.WRAP_CONTENT));
		}
	}

	private static class HexFormatter
	{
		protected static final char[] map = "0123456789abcdef".toCharArray();

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

	private static class ResultFormattedString extends SpannableString
	{
		private int pos = 0;

		public ResultFormattedString(String call, String file, 
		  String digestStr, String elapsedStr)
		{
			super(call + "(" + file + ") = " + digestStr +
			  ": " + elapsedStr + " seconds elapsed.");

			setSpanPos(new ForegroundColorSpan(0xffffff99), call.length(), 0);
			walk(call.length() + 1);

			setSpanPos(new ForegroundColorSpan(0xff9999ff), file.length(), 0);
			setSpanPos(new StyleSpan(Typeface.ITALIC), file.length(), 0);
			walk(file.length() + 1);

			walk(3);
//			setSpanPos(new ForegroundColorSpan(0xffcccccc), digestStr.length(), 0);
			walk(digestStr.length() + 2);

			setSpanPos(new ForegroundColorSpan(0xffff9999), elapsedStr.length(), 0);
			setSpanPos(new StyleSpan(Typeface.ITALIC), elapsedStr.length(), 0);
			walk(elapsedStr.length() + 1);
		}
		
		public void setSpanPos(Object span, int len, int flags)
		{
			setSpan(span, pos, pos + len, flags);
		}

		public void walk(int len)
		{
			pos += len;
		}
	}

	private static byte[] digestWithStream(String file)
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
