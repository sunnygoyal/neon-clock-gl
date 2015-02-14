package com.painless.glclock;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class ColorUtils {

	public static Paint getPaintFromColor(int color) {
		final float[] array = new float[20];
		array[18] = 1;
		array[1] = (float) Color.red(color) / 255;
		array[6] = (float) Color.green(color) / 255;
		array[11] = (float) Color.blue(color) / 255;
		final Paint paint = new Paint();
		paint.setColorFilter(new ColorMatrixColorFilter(array));
		return paint;
	}

	public static Bitmap getPaintedBitmap(Bitmap bitmap, Paint paint, boolean deleteOld) {
		final Bitmap colorBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
		final Canvas c = new Canvas(colorBitmap);
		c.drawBitmap(bitmap, 0, 0, paint);
		if (deleteOld) {
			bitmap.recycle();
		}
		return colorBitmap;
	}
}
