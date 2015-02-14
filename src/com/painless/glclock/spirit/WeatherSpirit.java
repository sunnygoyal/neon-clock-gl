package com.painless.glclock.spirit;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.painless.glclock.Constants;
import com.painless.glclock.Grid;
import com.painless.glclock.service.WeatherService;
import com.painless.glclock.util.WeatherUtil;

final class WeatherSpirit extends MovableSpirit {

	private final Grid grid;

    private boolean useFahrenheit = false;
    private boolean nightMode = false;
	private String weather = "";
	private boolean loaded = false;

	public WeatherSpirit(Context context, SpiritManager spiritManager) {
		super(context, 1, getService(context));

		grid = Grid.getSimpleGrid(256, 256);
	}

	@Override
	int[] getResources() {
		return new int[] { };
	}

	@Override
	public void drawOnPos(GL10 gl) {
		if (!loaded) {
			Bitmap cache = WeatherUtil.createWeatherIcon(mContext, weather, useFahrenheit, nightMode);
			bindBitmap(gl, cache, textureNames[0], true);
			loaded = true;
		}

		bind(gl, 0);
		grid.draw(gl);
		gl.glPopMatrix();
	}

	@Override
	boolean updatePref(SharedPreferences pref) {
		final String weather = pref.getString(Constants.WEATHER_VALUE, Constants.WEATHER_DEFAULT);
        final boolean useFahrenheit = pref.getBoolean(Constants.USE_FARENHIET, false);
        final boolean nightMode = pref.getBoolean(Constants.WEATHER_NIGHT_MODE, false);

		if (weather != this.weather || useFahrenheit != this.useFahrenheit || nightMode != this.nightMode) {
			loaded = false;
			this.weather = weather;
			this.useFahrenheit = useFahrenheit;
			this.nightMode = nightMode;
		}
		return false;
	}

	private static WeatherService SERVICE = null;
	private static WeatherService getService(Context context) {
		if (SERVICE == null) {
			SERVICE = new WeatherService(context);
		}
		return SERVICE;
	}
}
