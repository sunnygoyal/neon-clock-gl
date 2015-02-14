package com.painless.glclock.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.painless.glclock.Debug;
import com.painless.glclock.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

/*
 * use moon from
http://i.space.com/images/i/000/005/980/i02/moon-watching-night-100916-02.jpg?1294154541
 *
 */

public class WeatherUtil {

	public static Bitmap createWeatherIcon(Context context, String weather, boolean useFahrenheit, boolean nightMode) {
		final String[] parts = weather.split(",");

		// Generate Icon
		final Bitmap icons = BitmapFactory.decodeResource(context.getResources(), R.drawable.weather);
		final Bitmap cache = Bitmap.createBitmap(256, 256, Bitmap.Config.RGB_565);
		final Canvas canvas = new Canvas(cache);

		int bottom = 10;

		Paint bitmapPaint = new Paint();
		bitmapPaint.setFilterBitmap(true);
		bitmapPaint.setAntiAlias(true);
		final ArrayList<ArrayList<Rect>> config = getConfig(Integer.parseInt(parts[0]), context, nightMode);
		for (final ArrayList<Rect> rA : config) {
			for (int i=1; i<rA.size(); i++) {
				canvas.save();
				Rect r = rA.get(i);
				if (r.bottom < r.top) {
					canvas.scale(1,-1,0,(r.top + r.bottom)/2);
					r = new Rect(r.left, r.bottom, r.right, r.top);
				}
				bottom = Math.max(bottom, r.bottom);
				canvas.drawBitmap(icons, rA.get(0), r, bitmapPaint);
				canvas.restore();
			}
		}

		int temp = Integer.parseInt(useFahrenheit ? parts[2] : parts[1]);
		int humid = Integer.parseInt(parts[3]);
		bottom += 5;
		Bitmap digits = BitmapFactory.decodeResource(context.getResources(), R.drawable.digits);

		Rect dest = new Rect(10, bottom, 40, bottom + 30);

		if (temp < 0) {
		  temp = -temp;
		  Paint linePaint = new Paint();
		  linePaint.setColor(Color.WHITE);
		  linePaint.setStrokeWidth(5);
		  linePaint.setStyle(Paint.Style.STROKE);

		  canvas.drawLine(10, bottom + 16, 20, bottom + 16, linePaint);
		  dest.offset(15, 0);
		}

		canvas.drawBitmap(digits, getDigit(temp/10), dest, null);
		dest.offset(30,0);

		canvas.drawBitmap(digits, getDigit(temp % 10), dest, null);
		dest.offset(30,0);

		Rect tSRect = new Rect(310, 30, 350, 60);
		if (useFahrenheit) {
			tSRect.offset(0, -30);
		}
		canvas.drawBitmap(icons, tSRect, dest, null);

		dest.offsetTo(156, bottom);
		canvas.drawBitmap(digits, getDigit(humid/10), dest, null);
		dest.offset(30,0);
		canvas.drawBitmap(digits, getDigit(humid % 10), dest, null);
		tSRect.offsetTo(310, 60);


		canvas.drawBitmap(icons, tSRect, new Rect(216,bottom, 256, bottom + 30), null);


		digits.recycle();

		icons.recycle();
		return cache;
	}

	private static Rect getDigit(int digit) {
		Rect d = new Rect(0, 1, 30, 31);
		if (digit < 8) {
			d.offset(30*digit, 0);
		} else {
			d.offset(30 * (digit-8), 32);
		}
		if (digit == 7) {
			d.right -= 1;
		}
		return d;
	}


	private static ArrayList<ArrayList<Rect>> getConfig(int id, Context context, boolean nightMode) {
		final BufferedReader reader = new BufferedReader(
				new InputStreamReader(context.getResources().openRawResource(R.raw.icon_def)));
		try {
			final String config = reader.readLine();
			final String[] icons = config.split(";");
			final ArrayList<Rect> iconDefs = new ArrayList<Rect>();
			for (final String def : icons) {
				iconDefs.add(getRect(def.split(",")));
			}
			if (nightMode) {
			  Rect sun = iconDefs.get(1);
			  // change it to moon;
			  sun.set(350, 0, 450, 100);
			}

			String line = "";
			while ((line = reader.readLine()) != null && id > 0) {
				id --;
			}
			reader.close();

			line = line.split("#")[0].trim();

			final String[] parts = line.split(";");

			final ArrayList<ArrayList<Rect>> ret = new ArrayList<ArrayList<Rect>>();
			for (final String part : parts) {
				final ArrayList<Rect> lst = new ArrayList<Rect>();

				String[] defs = part.split("-");
				final Rect mainIcon = iconDefs.get(Integer.parseInt(defs[0]));
				defs = defs[1].split(":");

				lst.add(mainIcon);
				for (final String def : defs) {
					final String[] points = def.split(",");
					if (points.length > 3) {
						lst.add(getRect(points));
					} else {
						final Rect pos = new Rect(mainIcon);
						pos.offsetTo(Integer.parseInt(points[0]), Integer.parseInt(points[1]));
						lst.add(pos);
					}
				}

				ret.add(lst);
			}
			return ret;

		} catch (final Exception e) {
			Debug.log(e);
			return new ArrayList<ArrayList<Rect>>();
		}
	}

	private static Rect getRect(String[] points) {
		return new Rect(Integer.parseInt(points[0]), Integer.parseInt(points[1]),
				Integer.parseInt(points[2]), Integer.parseInt(points[3]));
	}

	public static boolean isNightMode(String weather) {
	  String[] parts = weather.split(",");

      if (parts.length < 5) {
        return false;
      }
	  
	  parts = parts[4].split("-");
	  Calendar c = Calendar.getInstance();
	  int min = c.get(Calendar.MINUTE) + 60 * c.get(Calendar.HOUR_OF_DAY);

	  return (min < Integer.parseInt(parts[0])) ||
	      (min > Integer.parseInt(parts[1]));	  
	}
}
