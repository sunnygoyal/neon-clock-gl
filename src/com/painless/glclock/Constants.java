package com.painless.glclock;

import android.content.SharedPreferences;

public final class Constants {

	public static final String SHARED_PREFS_NAME = "wallpaperSettings";

	public static final String BACK1_URL = "back1.jpg";
	public static final String BACK2_URL = "back2.jpg";
	public static final String BACK_NONE = "none";
	public static final String BACK_IMG_CODE = "back";
	public static final String BACK_CCODE = "backcolor";

	public static final String THEME_CODE = "theme";
	public static final int DEFAULT_THEME = 0xFF4CAF50;

	public static final String TRAILS_COUNT = "trails";
	public static final String TRAILS_CCODE = "tcolor";

	public static final String WEATHER_DEFAULT = "2,35,94,68";
	public static final String WEATHER_VALUE = "weather_val";
	public static final String LAST_WEATHER_CHECK = "last_weather_check";
    public static final String WEATHER_NIGHT_MODE = "weather_night";

	public static final String USE_FARENHIET = "weather_fahren";
	
	public static final WidgetCode CLOCK = new WidgetCode("clock", "-92,17");
	public static final WidgetCode COMPASS = new WidgetCode("compass", "96,-176");
	public static final WidgetCode BATTERY = new WidgetCode("battery", "155,-48");
	public static final WidgetCode CPU = new WidgetCode("cpu", "113,84");
	public static final WidgetCode RAM = new WidgetCode("ram", "-41,192");
	public static final WidgetCode CALENDAR = new WidgetCode("calendar", "-110,-174");
	public static final WidgetCode WEATHER = new WidgetCode("weather", "125,265");

	public static final class WidgetCode {
		public final String code;
		public final String prefix;

		public final String offset;
		public final String offsetDefault;

		public final String color;
		public final String zoom;

		private WidgetCode(String code, String defaultOffset) {
			this.code = code + "Enabled";
			this.offset = code + "Offset";
			this.color  = code + "color";
			this.zoom  = code + "zoom";
			this.prefix = code + "_";

			this.offsetDefault = defaultOffset;
		}

		public int[] getOffset(SharedPreferences pref) {
			final String[] values = pref.getString(offset, offsetDefault).split(",");

			final int[] offsets = new int[2];
			try {
				offsets[0] = Integer.valueOf(values[0]);
				offsets[1] = Integer.valueOf(values[1]);
			} catch (final Exception e) {
				offsets[0] = 0;
				offsets[1] = 0;
			}
			return offsets;
		}

		public float getZoom(SharedPreferences pref) {
			return pref.getFloat(zoom, 1);
		}
	}
}
