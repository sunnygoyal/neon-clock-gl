package com.painless.glclock.setting;

import android.os.Bundle;

/**
 * A simple class to hold a city information.
 */
public class CityInfo {
	public static final String USER_LOCATION = "user_location";
	public static final String LOCATION_DEFAULT = "New York, US";

	public static final String USER_LOCATION_TYPE = "user_location_type";
	public static final String USER_LOCATION_ADDR = "user_location_addr";
	public static final String USER_LOCATION_LAT = "user_location_lat";
	public static final String USER_LOCATION_LNG = "user_location_lng";

	public static final String USER_LOCATION_ID = "woeid";
	public static final String LOCATION_DEFAULT_ID = "2459115";

	public String woeid;

	public String name;
	public String type;
	public String address;

	public double lat, lng;

	public void readFromExtra(Bundle extras) {
		name = extras.getString(USER_LOCATION);
		type = extras.getString(USER_LOCATION_TYPE);
		address = extras.getString(USER_LOCATION_ADDR);
		woeid = extras.getString(USER_LOCATION_ID);

		lat = extras.getDouble(USER_LOCATION_LAT, 0);
		lng = extras.getDouble(USER_LOCATION_LNG, 0);
	}
}
