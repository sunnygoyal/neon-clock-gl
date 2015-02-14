package com.painless.glclock.spirit;

import java.util.Date;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.painless.glclock.Grid;
import com.painless.glclock.R;

class CalendarSpirit extends MovableSpirit {

	private final Grid grid;

	private int month, day;

	public CalendarSpirit(Context context, SpiritManager spiritManager) {
		super(context, 1, SpiritManager.NO_SERVICE);

		grid = Grid.getSimpleGrid(256, 128);
	}

	@Override
	int[] getResources() {
		return new int[]{ };
	}

	@SuppressWarnings("deprecation")
	@Override
	public void drawOnPos(GL10 gl) {
		final Date d = new Date();
		if (d.getMonth() != month || d.getDate() != day) {
			createCalendarTexture(gl, d);
		}
		bind(gl, 0);
		grid.draw(gl);
		gl.glPopMatrix();
	}

	@Override
	public void loadTextures(GL10 gl) {
		super.loadTextures(gl);
		month = day = -1;
	}

	@SuppressWarnings("deprecation")
	private void createCalendarTexture(GL10 gl, Date date) {
		final Bitmap calendarBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.calendar);
		month = date.getMonth();
		day = date.getDate();

		final Bitmap cache = Bitmap.createBitmap(256, 128, Bitmap.Config.RGB_565);
		final Canvas c = new Canvas(cache);

		final Rect src = new Rect(0, 0, 250, 70);
		Rect dst = new Rect(src);

		src.offsetTo(month > 5 ? 250 : 0, 70 * (month % 6));
		c.drawBitmap(calendarBitmap, src, dst, null);

		dst = new Rect(150, 50, 200, 120);
		paintInt(c, calendarBitmap, dst, day / 10);

		dst.offset(30, 0);
		paintInt(c, calendarBitmap, dst, day % 10);

		calendarBitmap.recycle();
		bindBitmap(gl, cache, textureNames[0], true);
	}

	private void paintInt(Canvas c, Bitmap bitmap, Rect dst, int i) {
		final Rect src = new Rect(500, 0, 550, 70);
		src.offset(i > 4 ? 50 : 0, 70 * (i % 5));
		c.drawBitmap(bitmap, src, dst, null);
	}
}
