package org.devtcg.demo.jnitest;

import android.util.Log;

import java.io.*;

public class NativeMD5
{
	static {
		try {
			Log.i("JNI", "Loading libNativeMD5.so...");
			System.loadLibrary("NativeMD5");
		} catch (UnsatisfiedLinkError e) {
			Log.d("JNI", "Failed to load libNativeMD5.so", e);
		}
	}

	public static native byte[] digestStream(InputStream in);
	public static native byte[] digestFile(String file);
}
