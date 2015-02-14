package com.painless.glclock;

import android.util.Log;

//A wrapper over android logging. This is removed in release builds.
public class Debug {

	private static final String TAG = "ClockGL";

	public static void log(Object msg) {
		Log.e(getTag(), msg + "");
	}

	public static void log(Throwable err) {
		Log.e(getTag(), err.getMessage() + " ");
		err.printStackTrace();
	}

	private static String getTag() {
		boolean startFound = false;
		String className = Debug.class.getName();

		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		for(StackTraceElement item : trace){
			if (item.getClassName().equals(className)) {
				startFound = true;
			} else if (startFound) {
				return String.format(
						"[%s/%s/%s]", item.getFileName(), item.getMethodName(), item.getLineNumber());
			}
		}

		return TAG;
	}
}
