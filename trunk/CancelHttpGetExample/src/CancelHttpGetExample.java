/*
 * $Id$
 *
 * Written by Josh Guilfoyle <jasta@devtcg.org>.
 *
 * This example demonstrates a high-level interruptible I/O request using
 * Apache's HttpClient library.  The approach shown here allows you to tidy
 * resources neatly at the user's request, rather than waiting for the read
 * operation to timeout which could possibly leave your application in an
 * inconsistent state.
 *
 * Do note that there is still a possibility that the download thread will
 * block on connect (prior to any reads).  This case is handled specially in
 * the download thread, and a sensible connect timeout is set to ensure
 * termination.
 */

package org.devtcg.demo;

import java.io.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;

public class CancelHttpGetExample
{
	public static void main(String[] args)
	{
		if (args.length < 1)
			usage();

		String url = args[0];
		File file = null;

		if (args.length > 1)
			file = new File(args[1]);

		StoppableDownloadThread dl = new StoppableDownloadThread(url, file);

		/* To illustrate that it does in fact terminate. */
		dl.setDaemon(false);

		if (file == null)
			System.out.println("Requesting " + url + "...");
		else
			System.out.println("Saving " + url + " to " + file + "...");

		dl.start();

		/* 
		 * In a more practical application, this sleep would represent a length
		 * of time before the user decides to cancel the download, which may be
		 * arbitrarily small.  Potentially even before an external connection
		 * has been made, which is a condition we handle.
		 */
		try { Thread.sleep(6000); }
		catch (InterruptedException e) {}

		/* Interrupt and stop the thread from processing. */
		dl.stopDownload();
	}

	public static void usage()
	{
		System.out.println("Usage: java CancelHttpGetExample <url> [ <output-file> ]");
		System.exit(64 /* EX_USAGE */);
	}

	public static class StoppableDownloadThread extends Thread
	{
		private String mURL;
		private File mDst;

		private GetMethod mMethod = null;

		/**
		 * Volatile stop flag used to coordinate state between the two threads
		 * involved in this example.
		 */
		protected volatile boolean mStopped = false;

		public StoppableDownloadThread(String url, File dst)
		{
			mURL = url;
			mDst = dst;
		}

		public void run()
		{
			System.out.println("Connecting...");

			HttpClient cli = new HttpClient();
			GetMethod method = new GetMethod(mURL);

			/* Connect timeout of 10 seconds. */
			cli.getHttpConnectionManager().getParams()
			  .setConnectionTimeout(10000);

			OutputStream out;

			if (mDst == null)
				out = new NullOutputStream();
			else
			{
				try {
					out = new FileOutputStream(mDst);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return;
				}
			}

			BufferedPipe pipe = new BufferedPipe(2048);
			pipe.setObserver(new BufferedPipe.BufferedPipeObserver() {
				public int mCount = 0;

				public void onRead(byte[] b, int offs, int n)
				{
					mCount += (n - offs);
					System.out.println("Read " + mCount + " bytes...");
				}
			});

			/* One last check if we've been prematurely stopped before we get
			 * moving. */
			if (cleanupOutputIfStopped() == true)
			{
				try { out.close(); } catch (IOException e) {}
				return;
			}

			mMethod = method;

			try {
				/* This method is the one place that may block!  stopDownload()
				 * works by closing the connected socket, so this operation
				 * can't happen when the socket is blocked on connect(). */
				int rc = cli.executeMethod(mMethod);

				if (mStopped == true)
					return;

				if (rc != 200)
					System.out.println("GET failed: " + mMethod.getStatusLine());

				InputStream in = mMethod.getResponseBodyAsStream();
				pipe.connectThenCloseOutput(in, out);
			} catch (IOException e) {
				/* This condition will happen when the download is stopped
				 * while in the input stream read call.  Specifically, we
				 * should see a "SocketException: Socket closed" message. */
				System.out.println("Exception!  Have we been successfully interrupted?  Let's find out:");
				e.printStackTrace();
			} finally {
				mMethod.releaseConnection();
				mMethod = null;

				cleanupOutputIfStopped();

				System.out.println("Exiting!");
			}
		}

		private boolean cleanupOutputIfStopped()
		{
			boolean stopped = mStopped;

			if (stopped == true)
			{
				if (mDst != null)
					mDst.delete();
			}

			return stopped;
		}

		/**
		 * This method is to be called from a separate thread.  That is, not
		 * the one executing run().  When it exits, the download thread should
		 * either be on its way out (failing a read call and cleaning up), or
		 * blocking on connect() until the timeout is reached.
		 */
		public void stopDownload()
		{
			/* As we've written this method, calling it from multiple threads
			 * would be problematic. */
			if (mStopped == true)
				return;

			/* Too late! */
			if (isAlive() == false)
				return;

			System.out.println("Stopping download...");

			/* This condition is used to avoid progress no matter the state the
			 * download was in.  It is also used to help us determine the
			 * outcome of the thread: success, failure, or cancelled? */
			mStopped = true;

			/* Interrupt the blocking thread.  It's always a good idea for
			 * implementations where you don't know if the thread is
			 * sleeping or waiting for any other reason, even though in
			 * this case we know that it won't be. */
			interrupt();

			/* This closes the socket handling our blocking InputStream,
			 * which will interrupt the blocking I/O immediately.  This is
			 * not the same as using mMethod.releaseConnection() or closing
			 * the InputStream yieled by mMethod.getResponseBodyAsStream(),
			 * as both of those methods will be operating on a synchronized
			 * InputStream and will block, starving our main thread until
			 * the blocking read() returns normally. */
			if (mMethod != null)
				mMethod.abort();

			System.out.println("Download stopped.");
		}
	}

	/**
	 * Simple utility class to avoid the ugly loop to exhaust data from an
	 * InputStream into a connected OutputStream.
	 *
	 * Not really a buffered class, instead it just copies data with a
	 * block-read strategy.
	 */
	public static class BufferedPipe
	{
		private int mBufSz;
		private BufferedPipeObserver mObserver;

		public BufferedPipe(int sz)
		{
			mBufSz = sz;
		}

		public void setObserver(BufferedPipeObserver o)
		{
			mObserver = o;
		}

		public void connectThenCloseOutput(InputStream in, OutputStream out)
		  throws IOException
		{
			byte[] b = new byte[mBufSz];
			int n;

			try
			{
				while ((n = in.read(b)) >= 0)
				{
					if (mObserver != null)
						mObserver.onRead(b, 0, n);

					out.write(b, 0, n);

					if (mObserver != null)
						mObserver.onWrite(b, n);
				}
			}
			finally
			{
				out.close();
			}
		}

		public static abstract class BufferedPipeObserver
		{
			public void onRead(byte[] b, int offs, int n) {}
			public void onWrite(byte[] b, int n) {}
		}
	}

	public static class NullOutputStream extends OutputStream
	{
		public NullOutputStream() {}

		public void close() {}
		public void flush() {}
		public void write(byte[] b) {}
		public void write(byte[] b, int off, int len) {}
		public void write(int c) {}
	}
}
