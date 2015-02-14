package com.painless.glclock.setting;

import android.content.Context;
import android.widget.Toast;

import com.painless.glclock.R;
import com.painless.glclock.service.WeatherService;
import com.painless.glclock.util.ProgressTask;

public class WeatherDataLoader extends ProgressTask<String> {

	 private final Context mContext;

	 public WeatherDataLoader(Context context) {
		 super(context, R.string.lp_busy_validating);
		 mContext = context;
	 }

	 @Override
	 protected String doInBackground(String... params) {
		 return WeatherService.getInfo(params[0], mContext);
	 }

	 @Override
	 protected final void onPostExecute(String result) {
		 super.onPostExecute(result);
		 if (result == null) {
			 Toast.makeText(mContext, R.string.lp_error_unknown, Toast.LENGTH_LONG).show();
			 return;
		 }
		 onWeatherLoaded(result);
	 }

	 public void onWeatherLoaded(String weather) { }
}