package com.painless.glclock.setting;

import java.util.Collection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.painless.glclock.Constants;
import com.painless.glclock.R;
import com.painless.glclock.util.WeatherUtil;

public class LocationViewActivity extends Activity implements View.OnClickListener, OnSeekBarChangeListener {

  private final ColorMatrixColorFilter mAlphaFilter = new ColorMatrixColorFilter(new float[] {
      0,0,0,0,255,  0,0,0,0,255,  0,0,0,0,255,  1,0,0,0,0
  });

  private boolean useFahrenheit = false;
  private String mCurrentWeather;
  private SharedPreferences pref;
  private CityInfo mCurrentCityInfo;

  private View mLocationView;

  private View mMapContent;
  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.location_view);

    pref = getSharedPreferences(Constants.SHARED_PREFS_NAME, 0);
    useFahrenheit = pref.getBoolean(Constants.USE_FARENHIET, false);

    // initialize from prefs.
    mCurrentCityInfo = new CityInfo();
    mCurrentCityInfo.name = pref.getString(CityInfo.USER_LOCATION, CityInfo.LOCATION_DEFAULT);
    mCurrentCityInfo.type = pref.getString(CityInfo.USER_LOCATION_TYPE, "");
    mCurrentCityInfo.address = pref.getString(CityInfo.USER_LOCATION_ADDR, "");
    mCurrentCityInfo.woeid = pref.getString(CityInfo.USER_LOCATION_ID, CityInfo.LOCATION_DEFAULT_ID);

    mCurrentCityInfo.lat = pref.getFloat(CityInfo.USER_LOCATION_LAT, 0);
    mCurrentCityInfo.lng = pref.getFloat(CityInfo.USER_LOCATION_LNG, 0);

    mCurrentWeather = pref.getString(Constants.WEATHER_VALUE, Constants.WEATHER_DEFAULT);

    mLocationView = findViewById(R.id.location_view);
    mMapContent = findViewById(R.id.map_content);
    mMapView = (MapView) findViewById(R.id.map_view);

    ((SeekBar) findViewById(R.id.zoom_seek)).setOnSeekBarChangeListener(this);
    showCityInfo(mCurrentCityInfo);
  }

  private void showCityInfo(CityInfo info) {
    mCurrentCityInfo = info;
    getTV(R.id.txtName).setText(mCurrentCityInfo.name);
    getTV(R.id.txtType).setText(mCurrentCityInfo.type);
    getTV(R.id.txtAddress).setText(mCurrentCityInfo.address);
    updateTemp();
    mLocationView.setVisibility(View.VISIBLE);
    mMapContent.setVisibility(View.GONE);
    setTitle(R.string.app_name);
  }

  @Override
  public void onBackPressed() {
    if (mLocationView.getVisibility() == View.GONE) {
      showCityInfo(mCurrentCityInfo);
    } else {
      super.onBackPressed();
    }
  }

  private void updateTemp() {
    getTV(R.id.butUnit).setText(useFahrenheit ? R.string.lp_degee_f : R.string.lp_degee_c);

    Bitmap preview = WeatherUtil.createWeatherIcon(this, mCurrentWeather, useFahrenheit, WeatherUtil.isNightMode(mCurrentWeather));
    BitmapDrawable drawable = new BitmapDrawable(getResources(), preview);
    drawable.setColorFilter(mAlphaFilter);
    ((ImageView) findViewById(R.id.imgPreview)).setImageDrawable(drawable);
  }

  public void toggleUnitClicked(View v) {
    useFahrenheit = !useFahrenheit;
    updateTemp();
  }

  @SuppressLint("NewApi")
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuItem item = menu.add(android.R.string.search_go)
        .setIcon(android.R.drawable.ic_menu_search);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);  
    }
    item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

      @Override
      public boolean onMenuItemClick(MenuItem item) {
        return onSearchRequested();
      }
    });
    return true;
  }

  @Override
  public void onNewIntent(Intent intent) { 
    setIntent(intent); 
    handleIntent(intent); 
  }

  /**
   * Process the search intent.
   */
  private void handleIntent(Intent intent) { 
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) { 
      String query = intent.getStringExtra(SearchManager.QUERY);
      setTitle(getString(R.string.lb_search_title, query));
      if (!TextUtils.isEmpty(query)) {
        new LocationSearchTask(this) {
          @Override
          protected void onPostExecute(Collection<CityInfo> result) {
            super.onPostExecute(result);
            if (result != null) {
              renderCities(result);
            } else {
              Toast.makeText(LocationViewActivity.this, R.string.lp_error_no_result, Toast.LENGTH_LONG).show();
            }
          }
        }.execute(query);
      }
    }
  }

  private void renderCities(Collection<CityInfo> cities) {
    mLocationView.setVisibility(View.GONE);
    mMapContent.setVisibility(View.VISIBLE);
    mMapView.removeAllViews();

    for (CityInfo info : cities) {
      View v = mMapView.addMarker(info);
      if (v != null) {
        v.setOnClickListener(this);
      }
    }
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) { }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) { };

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    float scale = 1 + progress / 10f;
    mMapView.setZoom(scale);
  }

  @Override
  public void onClick(View v) {
    final CityInfo info = (CityInfo) v.getTag();
    new AlertDialog.Builder(this)
    .setTitle(info.name)
    .setMessage(info.address)
    .setPositiveButton(R.string.ip_set_back, new OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        new WeatherDataLoader(LocationViewActivity.this) {

          @Override
          public void onWeatherLoaded(String weather) {
            mCurrentWeather = weather;
            showCityInfo(info);
            saveCityInfo();
          };
        }.execute(info.woeid);
      }
    }).show();
  }

  private void saveCityInfo() {
    pref.edit()
    .putString(CityInfo.USER_LOCATION, mCurrentCityInfo.name)
    .putString(CityInfo.USER_LOCATION_TYPE, mCurrentCityInfo.type)
    .putString(CityInfo.USER_LOCATION_ADDR, mCurrentCityInfo.address)
    .putString(CityInfo.USER_LOCATION_ID, mCurrentCityInfo.woeid)
    .putFloat(CityInfo.USER_LOCATION_LAT, (float) mCurrentCityInfo.lat)
    .putFloat(CityInfo.USER_LOCATION_LNG, (float) mCurrentCityInfo.lng)

    .putString(Constants.WEATHER_VALUE, mCurrentWeather)
    .putBoolean(Constants.USE_FARENHIET, useFahrenheit)
    .commit();
  }

  private TextView getTV(int id) {
    return (TextView) findViewById(id);
  }
}
