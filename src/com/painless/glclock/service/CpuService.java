package com.painless.glclock.service;

import android.os.Handler;

import com.painless.glclock.Globals;

import java.io.RandomAccessFile;

public final class CpuService implements Runnable, RService {

	private static final long REFRESH_RATE = 5000;	// 5 seconds.

	private final Handler updateHandler;

	// keep these static to prevent restart
	private static long lastTotal = -1, lastIdle = -1;

	private boolean running = false;

	public CpuService() {
		updateHandler = new Handler();
	}

	@Override
	public void run() {
		try {
			final RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
			String line = reader.readLine();
			reader.close();

			line = line.split(" ", 2) [1];
			final String values[] = line.trim().split(" ");

			final long idle = Long.parseLong(values[3]);
			long total = 0;
			for (int i=0; i<values.length; i++) {
				total += Long.parseLong(values[i]);
			}

			if (lastTotal > 0) {
				final long idleD = idle - lastIdle;
				final long totalD = total - lastTotal;
				Globals.cpuUsage = (int) (1000 * (totalD - idleD) / totalD + 5) / 10;
				Globals.cpuUsageAngle = (int) (2880 * (totalD - idleD) / totalD + 5) / 10 - 144;	// angle in 288
			}
			lastIdle = idle;
			lastTotal = total;
		} catch (final Exception e) { }

		updateHandler.removeCallbacks(this);
		if (running) {
			updateHandler.postDelayed(this, REFRESH_RATE);
		}
	}

	@Override
	public void stop() {
		updateHandler.removeCallbacks(this);
		running = false;
//		Debug.log("CpuService stopped");
	}

	@Override
	public void start() {
		running = true;
//		Debug.log("CpuService started");
		run();
	}
}
