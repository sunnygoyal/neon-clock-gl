package com.painless.glclock.service;

import android.os.Handler;

import com.painless.glclock.Globals;

import java.io.RandomAccessFile;

public final class RamService implements Runnable, RService {

	private static final long REFRESH_RATE = 15000;	// 15 seconds.

	private final Handler updateHandler;

	private boolean running = false;

	public RamService() {
		updateHandler = new Handler();
	}

	@Override
	public void run() {
		try {
			final RandomAccessFile reader = new RandomAccessFile("/proc/meminfo", "r");
			long memFree = 0, memTotal = 0;

			int readCount = 0, foundCount = 0;
			String line;
			// read maximum 10 lines and find 3 items.
			while (readCount < 10 && foundCount < 3) {
				line = reader.readLine().toLowerCase();
				if (line.startsWith("memtotal")) {
					memTotal = readMemInfo(line);
					foundCount++;
				} else if (line.startsWith("memfree") || line.startsWith("cached")) {
					memFree += readMemInfo(line);
					foundCount++;
				}
				readCount++;
			}
			reader.close();

			final long percent = 100 - (memFree * 100 / memTotal);
			Globals.ramUsage = (int) percent;
		} catch (final Exception e) { }

		updateHandler.removeCallbacks(this);
		if (running) {
			updateHandler.postDelayed(this, REFRESH_RATE);
		}
	}

	private long readMemInfo(String line) {
		return Long.parseLong(line.split("\\s+")[1]);
	}

	@Override
	public void stop() {
		updateHandler.removeCallbacks(this);
		running = false;
//		Debug.log("RamService stopped");
	}

	@Override
	public void start() {
		running = true;
//	s	Debug.log("RamService started");
		run();
	}
}
