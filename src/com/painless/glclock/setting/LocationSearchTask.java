package com.painless.glclock.setting;

import android.content.Context;
import android.text.TextUtils;

import com.painless.glclock.Debug;
import com.painless.glclock.R;
import com.painless.glclock.service.WeatherService;
import com.painless.glclock.util.ProgressTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * AsyncTask that searches for a locations
 */
public class LocationSearchTask extends ProgressTask<Collection<CityInfo>> {

  private static final String PARAM = "select*from geo.places where text='%s'";

  public LocationSearchTask(Context context) {
    super(context, R.string.lp_busy_searching);
  }

  @Override
  protected Collection<CityInfo> doInBackground(String... params) {
    String query = params[0];
    try {
      String result = WeatherService.getResponse("http://query.yahooapis.com/v1/public/yql?format=json&q=" +
               URLEncoder.encode(String.format(PARAM, query), "UTF-8"));
      
      if (TextUtils.isEmpty(result)) {
        return null;
      }

      try {
        JSONArray results = new JSONObject(result).getJSONObject("query").getJSONObject("results").getJSONArray("place");
        HashMap<String, CityInfo> cityMap = new HashMap<String, CityInfo>();
        
        for (int i=0; i<results.length(); i++) {
          try {
              JSONObject cityObj = results.getJSONObject(i);
              CityInfo city = new CityInfo();
              city.woeid = cityObj.getString("woeid");
              city.name = cityObj.getString("name");
              city.type = getContent(cityObj, "placeTypeName");

              // init address
              ArrayList<String> admins = new ArrayList<String>();
              for (int j = 3; j > 0; j--) {
                if (!cityObj.isNull("admin" + j)) {
                  admins.add(getContent(cityObj, "admin" + j));
                }
              }
              admins.add(getContent(cityObj, "country"));
              city.address = TextUtils.join(",  ", admins);

              String lat = getContent(cityObj, "centroid", "latitude");
              String lng = getContent(cityObj, "centroid", "longitude");
              city.lat = Double.parseDouble(lat);
              city.lng = Double.parseDouble(lng);

              cityMap.put(lat + lng, city);
          } catch (Exception e) {
            Debug.log(e);
          }
        }

        if (cityMap.size() > 0) {
          return cityMap.values();
        }

      } catch (JSONException e) {
        Debug.log(e);
      }
      
    } catch (Exception e) {
        Debug.log(e);
    }
    return null;
  }

  private static String getContent(JSONObject obj, String... path) throws JSONException {
    return obj.getJSONObject(path[0]).getString(path.length > 1 ? path[1] : "content");
  }
}