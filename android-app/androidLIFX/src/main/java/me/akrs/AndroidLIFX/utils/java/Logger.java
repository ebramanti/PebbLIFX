package me.akrs.AndroidLIFX.utils.java;

public class Logger {
	public static final String TAG = "AndroidLIFX";
	public static final int DEBUG = 1;
	public static final int ERROR = 2;
	public static final int INFO = 3;
	public static final int VERBOSE = 4;
	
	public static void log (String s, int level) {
		switch (level) {
			case DEBUG:
				System.out.println("DEBUG: " + s);
				break;
			case ERROR:
				System.out.println("ERROR: " + s);
				break;
			case INFO:
				System.out.println("INFO: " + s);
				break;
			case VERBOSE:
				System.out.println("VERBOSE: " + s);
				break;
		}
	}
	
	public static void log (String s, Throwable e) {
		System.out.println("ERROR: " + s + " " + e.toString());
	}
	
}
