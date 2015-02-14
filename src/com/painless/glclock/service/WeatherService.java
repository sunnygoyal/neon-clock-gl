package com.painless.glclock.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.painless.glclock.Constants;
import com.painless.glclock.Debug;
import com.painless.glclock.R;
import com.painless.glclock.setting.CityInfo;
import com.painless.glclock.util.WeatherUtil;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherService implements RService {

	private static final long RPC_GAP = 30 * 60 * 1000;	// 30 mins.

	private final Context context;
	private final SharedPreferences mPrefs;

	private AsyncTask<Void, Void, Void> rpcTask;

	public WeatherService(Context context) {
		this.context = context;
		mPrefs = context.getSharedPreferences(Constants.SHARED_PREFS_NAME, 0);
	}

	@Override
	public void start() {
		if (rpcTask != null) {
			return;
		}
		final long lastCheck = mPrefs.getLong(Constants.LAST_WEATHER_CHECK, 0);
		if ((System.currentTimeMillis() - lastCheck) < RPC_GAP) {
		    boolean nightMode = WeatherUtil.isNightMode(mPrefs.getString(Constants.WEATHER_VALUE, Constants.WEATHER_DEFAULT));
		    mPrefs.edit().putBoolean(Constants.WEATHER_NIGHT_MODE, nightMode).commit();
			return;
		}

		final String location = mPrefs.getString(CityInfo.USER_LOCATION_ID, CityInfo.LOCATION_DEFAULT_ID);
		rpcTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				final String result = getInfo(location, context);
				if (result != null) {
					mPrefs.edit().putString(Constants.WEATHER_VALUE, result)
					    .putLong(Constants.LAST_WEATHER_CHECK, System.currentTimeMillis())
					    .putBoolean(Constants.WEATHER_NIGHT_MODE, WeatherUtil.isNightMode(result))
					    .commit();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				rpcTask = null;
			}
		};
		rpcTask.execute();
	}

	@Override
	public void stop() { }

	private static String getAttr(String attr, String xml) {
		return getAttr(Pattern.compile(attr+ "=\"([^\"]*)\""), xml);
	}

	private static String getAttr(Pattern p, String xml) {
		final Matcher m = p.matcher(xml);
		if (m.find()) {
			return m.group(1);
		}
		return "";
	}

	public static String getInfo(String woeid, Context context) {
		try {
			final String xml = getResponse("http://weather.yahooapis.com/forecastrss?w=" + woeid);

			final String tempF = getAttr("temp", xml);
			String humidity = getAttr("humidity", xml);
			
			float tempFf = Float.parseFloat(tempF);
			float tempCf = (tempFf-32) * 5 / 9;

			final int tempFi = (int) tempFf;
			final int tempCi = (int) tempCf;

			int sunrise = parseTimeToMins(getAttr("sunrise", xml));
			int sunset = parseTimeToMins(getAttr("sunset", xml));

			// map icon
			final String code = getAttr("code", xml);
			final BufferedReader reader =
				new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.icon_map)));

			int iconNo = -1;
			String line = "";
			boolean found = false;
			while ((line = reader.readLine()) != null && !found) {
				final String[] parts = line.split(",");
				for (final String part : parts) {
					if (part.equals(code)) {
						found = true;
						break;
					}
				}
				iconNo ++;
			}

			reader.close();

			return TextUtils.join(",", new Object[] {iconNo, tempCi, tempFi, humidity, sunrise + "-" + sunset });
		} catch (final Exception e) {
			Debug.log(e);
			return null;
		}
	}

	public static String getResponse(String reqUrl) throws Exception {
		final URL url = new URL(reqUrl);
		final URLConnection ucon = url.openConnection();

		/* Define InputStreams to read
		 * from the URLConnection. */
		final InputStream is = ucon.getInputStream();
		final BufferedInputStream bis = new BufferedInputStream(is);

		/* Read bytes to the Buffer until
		 * there is nothing more to read(-1). */
		final ByteArrayBuffer baf = new ByteArrayBuffer(50);
		int current = 0;
		while((current = bis.read()) != -1){
			baf.append((byte)current);
		}

		return new String(baf.toByteArray());
	}

	private static final int parseTimeToMins(String time) {
	  SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
	  try {
        Date d = format.parse(time);
        return d.getHours()*60 + d.getMinutes();
      } catch (ParseException e) {
        Debug.log(e);
      }
	  return 0;
	}
}
