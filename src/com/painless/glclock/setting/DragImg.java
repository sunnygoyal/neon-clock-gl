package com.painless.glclock.setting;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;

import com.painless.glclock.Constants.WidgetCode;
import com.painless.glclock.Debug;

public class DragImg {
	public final WidgetCode widgetCode;
	public final Bitmap previewBitmap;
	public final Rect srcRect;
	public final Rect dstRect;
	public final Paint drawPaint = new Paint();

	private final OffsetPicker mMain;

	public final int titleRes;
	public final int iconRes;


	public int offsetX = 0, offsetY = 0;
	public int zoomOffsetX = 0, zoomOffsetY = 0;

	public DragImg(OffsetPicker main,
			int myWidth, int myHeight, int x, int y,
			WidgetCode widgetCode, int titleRes, int iconRes) {
		mMain = main;
		this.titleRes = titleRes;
		this.iconRes = iconRes;
		this.widgetCode = widgetCode;

		// Set preview
		Bitmap temp = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.ARGB_8888);
		new Canvas(temp).drawBitmap(mMain.widgetBitmap, -x, -y, null);

		previewBitmap = Bitmap.createBitmap(temp);

		// initialize offset
		final int[] values = widgetCode.getOffset(mMain.prefs);
		offsetX = values[0];
		offsetY = values[1];

		offsetX -= myWidth/2;	// move the offset to refer top left corner
		offsetY -= myHeight/2;

		dstRect = new Rect(0, 0, myWidth, myHeight);

		finishMove(0, 0);

	
		srcRect = new Rect(0, 0, myWidth, myHeight);

		updateColor();
		updateAlpha();
		setZoom(mMain.prefs.getFloat(widgetCode.zoom, 1));
	}

	public void showDrag(int sx, int sy) {
		sx += offsetX;
		sy += offsetY;

		move(sx, sy);
	}

	public void finishMove(int endX, int endY) {
		offsetX += endX;
		offsetY += endY;
		dstRect.offsetTo(mMain.halfW + offsetX, mMain.halfH + offsetY);

		move(offsetX, offsetY);
	}

	private void move(int ox, int oy) {
		dstRect.offsetTo(mMain.halfW + ox + zoomOffsetX, mMain.halfH + oy + zoomOffsetY);
	}

	public boolean isIn(int pointX, int pointY) {
		return dstRect.contains(pointX, pointY);
	}

	public void saveSettings() {
		final int fx = offsetX + previewBitmap.getWidth() / 2;
		final int fy = offsetY + previewBitmap.getHeight() / 2;
		mMain.prefs.edit().putString(widgetCode.offset, fx + "," + fy).commit();
		Debug.log("Save " + widgetCode.offset + " " + fx + "," + fy);
	}

	public void resetPref(SharedPreferences.Editor editor) {
		editor.remove(widgetCode.offset).remove(widgetCode.code).remove(widgetCode.zoom);
	}


	public void updateColor() {
		final int color = mMain.prefs.getInt(widgetCode.color, mMain.themeColor);
		drawPaint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
	}

	public void updateAlpha() {
		drawPaint.setAlpha(mMain.prefs.getBoolean(widgetCode.code, true) ? 255 : 120);
	}

	public void setZoom(float zoom) {
		dstRect.right = dstRect.left + (int) (previewBitmap.getWidth() * zoom);
		dstRect.bottom = dstRect.top + (int) (previewBitmap.getHeight() * zoom);
		zoomOffsetX = (previewBitmap.getWidth() - dstRect.width()) / 2;
		zoomOffsetY = (previewBitmap.getWidth() - dstRect.height()) / 2;
		move(offsetX, offsetY);
	}
}