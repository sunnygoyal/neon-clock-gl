package com.painless.glclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.SurfaceHolder;

import com.painless.glclock.spirit.SpiritManager;

public class ClockWallpaperService extends GLWallpaperService {

	@Override
	public void onCreate() {
		super.onCreate();

		// register battery
		final Intent intent = this.registerReceiver(new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				processBatteryIntent(intent);
			}
		}, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		if (intent != null) {
		  processBatteryIntent(intent);
		}
	}

	@Override
	public Engine onCreateEngine() {
		return new ClockEngine(this);
	}

	private void processBatteryIntent(Intent intent) {
		final int level = intent.getIntExtra("level", 0);
		final int scale = intent.getIntExtra("scale", 100);
		Globals.battery = level * 100 / scale;
		Globals.batteryCount = Math.round(Globals.battery * 24L / 100);
	}

	private final class ClockEngine extends GLEngine {
		private final ClockRenderer renderer;
		private final SpiritManager spiritManager;

		public ClockEngine(Context context) {
			spiritManager = new SpiritManager(context);
			this.renderer = new ClockRenderer(spiritManager);

			setRenderer(renderer);
			setRenderMode(RENDERMODE_CONTINUOUSLY);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			super.onVisibilityChanged(visible);
			if (visible) {
				spiritManager.start();
			} else {
				spiritManager.stop();
			}
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			renderer.shutdown();
			super.onSurfaceDestroyed(holder);
		}
	}
}
