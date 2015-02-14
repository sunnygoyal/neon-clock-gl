package com.painless.glclock.setting;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.painless.glclock.Constants;
import com.painless.glclock.R;

public class ClockSettings extends PreferenceActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getPreferenceManager().setSharedPreferencesName(Constants.SHARED_PREFS_NAME);
		addPreferencesFromResource(R.xml.settings);
	}

	@Override
	protected void onResume() {
	  super.onResume();

	  findPreference(CityInfo.USER_LOCATION).setSummary(
	      getPreferenceManager().getSharedPreferences().getString(CityInfo.USER_LOCATION, CityInfo.LOCATION_DEFAULT));
	}
}
