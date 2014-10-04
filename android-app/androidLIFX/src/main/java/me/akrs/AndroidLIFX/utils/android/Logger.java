package me.akrs.AndroidLIFX.utils.android;

import android.util.Log;

public class Logger {
	public static final String TAG = "AndroidLIFX";
	public static final int DEBUG = 1;
	public static final int ERROR = 2;
	public static final int INFO = 3;
	public static final int VERBOSE = 4;

	public static void log (String s, int level) {
		switch (level) {
			case DEBUG:
				Log.d(TAG, s);
				break;
			case ERROR:
				Log.e(TAG, s);
				break;
			case INFO:
				Log.i(TAG, s);
				break;
			case VERBOSE:
				Log.v(TAG, s);
				break;
		}
	}

	public static void log (String s, Throwable e) {
		Log.e(TAG, s, e);
	}

}