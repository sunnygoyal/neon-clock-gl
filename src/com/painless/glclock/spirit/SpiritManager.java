package com.painless.glclock.spirit;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.SharedPreferences;

import com.painless.glclock.Constants;
import com.painless.glclock.Debug;
import com.painless.glclock.Constants.WidgetCode;
import com.painless.glclock.service.RService;

public final class SpiritManager implements RService, SharedPreferences.OnSharedPreferenceChangeListener {

	static final RService NO_SERVICE = new RService() {

		@Override
		public void start() {}

		@Override
		public void stop() {}
	};

	@SuppressWarnings("unchecked")
	private static final Class<? extends MovableSpirit>[] MOVABLE_SPIRITS = new Class[] {
		ClockSpirit.class,
		CompassSpirit.class,
		BatterySpirit.class,
		CpuSpirit.class,
		RamSpirit.class,
		CalendarSpirit.class,
		WeatherSpirit.class
	};

	private static final WidgetCode[] MOVABLE_SPIRITS_Settings = new WidgetCode[] {
		Constants.CLOCK,
		Constants.COMPASS,
		Constants.BATTERY,
		Constants.CPU,
		Constants.RAM,
		Constants.CALENDAR,
		Constants.WEATHER
	};

	private final ArrayList<Spirit> drawables;

	private final Context context;
	private final SharedPreferences mPrefs;
	private final MovableSpirit[] allMovableSpirits;
	private final BackSpirit backSpirit;

	final DigitSpirit digits;

	private TrailSpirit trails;
	private int themeColor;

	public SpiritManager(Context context) {
		this.context = context;
		mPrefs = context.getSharedPreferences(Constants.SHARED_PREFS_NAME, 0);
		mPrefs.registerOnSharedPreferenceChangeListener(this);

		digits = new DigitSpirit(context);
		drawables = new ArrayList<Spirit>();

		backSpirit = new BackSpirit(context);
		allMovableSpirits = new MovableSpirit[MOVABLE_SPIRITS.length];
		updateColors();
		updateMovables();
		updateOffsetsAndZoom();
		updateTrails();
		updateDrawables();
	}

	/**
	 * Updates the {@link #allMovableSpirits} array with all the enabled movale spirits.
	 */
	private void updateMovables() {
		for (int i=0; i<MOVABLE_SPIRITS_Settings.length; i++) {
			final boolean enabled = mPrefs.getBoolean(MOVABLE_SPIRITS_Settings[i].code, true);
			if (enabled && allMovableSpirits[i] == null) {

				final Class<? extends MovableSpirit> spiritClass = MOVABLE_SPIRITS[i];
				try {
					final Constructor<? extends MovableSpirit> ct = spiritClass.getConstructor(
							new Class[] {Context.class, SpiritManager.class});
					allMovableSpirits[i] = ct.newInstance(context, this);
					allMovableSpirits[i].setColor(
							mPrefs.getInt(MOVABLE_SPIRITS_Settings[i].color, themeColor));
					allMovableSpirits[i].updatePref(mPrefs);
					allMovableSpirits[i].setOffset(MOVABLE_SPIRITS_Settings[i].getOffset(mPrefs));
					allMovableSpirits[i].setZoom(MOVABLE_SPIRITS_Settings[i].getZoom(mPrefs));
				} catch (final Exception e) {
					Debug.log(e);
				}
			} else if (!enabled && allMovableSpirits[i] != null) {
				allMovableSpirits[i].shutdown();
				allMovableSpirits[i] = null;
			}
		}
	}

	private void updateColors() {
		themeColor = mPrefs.getInt(Constants.THEME_CODE, Constants.DEFAULT_THEME);

		// Update background.
		final int backColor = mPrefs.getInt(Constants.BACK_CCODE, themeColor);
		final String backType = mPrefs.getString(Constants.BACK_IMG_CODE, "");
		backSpirit.setColor(backColor, backType);

		if (trails != null) {
			trails.setColor(mPrefs.getInt(Constants.TRAILS_CCODE, themeColor));
		}

		for (int i=0; i<MOVABLE_SPIRITS_Settings.length; i++) {
			if (allMovableSpirits[i] != null) {
				allMovableSpirits[i].setColor(
						mPrefs.getInt(MOVABLE_SPIRITS_Settings[i].color, themeColor));
			}
		}
	}

	public void updateOffsetsAndZoom() {
		for (int i=0; i<MOVABLE_SPIRITS_Settings.length; i++) {
			if (allMovableSpirits[i] != null) {
				allMovableSpirits[i].setOffset(MOVABLE_SPIRITS_Settings[i].getOffset(mPrefs));
				allMovableSpirits[i].setZoom(MOVABLE_SPIRITS_Settings[i].getZoom(mPrefs));
			}
		}
	}

	private void updateTrails() {
		final int tCount = Integer.parseInt(mPrefs.getString(Constants.TRAILS_COUNT, "16"));

		if (tCount > 0) {
			if (trails == null) {
				trails = new TrailSpirit(context);
				trails.setColor(mPrefs.getInt(Constants.TRAILS_CCODE, themeColor));
			}
			trails.setCount(tCount);
			isLoaded = false;
		} else if (trails != null) {
			trails.shutdown();
			trails = null;
		}
	}

	private void updateDrawables() {
		drawables.clear();
		drawables.add(backSpirit);

		if (trails != null) {
			drawables.add(trails);
		}

		drawables.add(digits);
		for (int i=0; i<MOVABLE_SPIRITS_Settings.length; i++) {
			if (allMovableSpirits[i] != null) {
				drawables.add(allMovableSpirits[i]);
			}
		}

		isLoaded = false;
	}

	@Override
	public void start() {
		for (final MovableSpirit s : allMovableSpirits) {
			if (s != null) {
				s.getService().start();
			}
		}
	}

	@Override
	public void stop() {
		for (final MovableSpirit s : allMovableSpirits) {
			if (s != null) {
				s.getService().stop();
			}
		}
	}

	@Override
	public synchronized void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		changedPreferences.add(key);
		isLoaded = false;
	}

	private final ArrayList<String> changedPreferences = new ArrayList<String>();

	// Loading related code.
	private boolean isLoaded = false;

	private int width, height;

	private synchronized void load(GL10 gl) {
		// process preferences
		for (String key : changedPreferences) {
			if (key.equals(Constants.BACK_IMG_CODE) ||
					key.equals(Constants.THEME_CODE) ||
					key.endsWith("color")) {
				updateColors();
			} else if (key.equals(Constants.TRAILS_COUNT)) {
				updateTrails();
				updateDrawables();
			} else if (key.endsWith("Enabled")) {
				updateMovables();
				updateDrawables();
			} else if (key.endsWith("Offset") || key.endsWith("zoom")) {
				updateOffsetsAndZoom();
			} else {
				for (int i=0; i<MOVABLE_SPIRITS_Settings.length; i++) {
					if (allMovableSpirits[i] != null &&
							key.startsWith(MOVABLE_SPIRITS_Settings[i].prefix)) {
						isLoaded &= !allMovableSpirits[i].updatePref(mPrefs);
					}
				}
			}
		}
		changedPreferences.clear();

		for (final Spirit s : drawables) {
			s.setDim(width, height);
			if (!s.isLoaded()) {
				s.loadTextures(gl);
			}
		}
		isLoaded = true;
	}

	public void setDim(int width, int height) {
		this.width = width;
		this.height = height;
		for (final Spirit s : drawables) {
			s.setDim(width, height);
		}
	}

	public void shutdown() {
		for (final Spirit spirit : drawables) {
			spirit.shutdown();
		}
	}

	public void draw(GL10 gl) {
		if (!isLoaded) {
			load(gl);
		}
		for (final Spirit spirit : drawables) {
			spirit.color(gl);
			spirit.draw(gl);
		}
	}
}
