package com.painless.glclock.spirit;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import com.painless.glclock.Globals;
import com.painless.glclock.Grid;
import com.painless.glclock.R;
import com.painless.glclock.service.RamService;

final class RamSpirit extends MovableSpirit {

	private static final RamService SERVICE = new RamService();

	private static final int SAVE_FLAGS = Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG |
	Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG |Canvas.CLIP_TO_LAYER_SAVE_FLAG;

	private final Bitmap disk;
	private final Bitmap diskIndicator;
	private final Grid grid;
	private final DigitSpirit digit;

	private int currentPercent = -1;

	public RamSpirit(Context context, SpiritManager spiritManager) {
		super(context, 1, SERVICE);
		this.digit = spiritManager.digits;

		disk = BitmapFactory.decodeResource(context.getResources(), R.drawable.disk_back);
		diskIndicator = BitmapFactory.decodeResource(context.getResources(), R.drawable.disk_indicator);
		grid = Grid.getSimpleGrid(128, 128);
	}

	@Override
	int[] getResources() {
		return new int[]{ };
	}

	@Override
	public void drawOnPos(GL10 gl) {
		if (currentPercent != Globals.ramUsage) {
			createRamTexture(gl);
		}

		bind(gl, 0);
		grid.draw(gl);

		gl.glTranslatef(-15, 0, 0);
		digit.drawNumber(gl, currentPercent, 30, 0);

		gl.glPopMatrix();
	}

	private void createRamTexture(GL10 gl) {
		currentPercent = Globals.ramUsage;

		final Bitmap cache = Bitmap.createBitmap(128, 128, Bitmap.Config.RGB_565);
		final Canvas cacheCanvas = new Canvas(cache);
		cacheCanvas.drawBitmap(disk, 0, 0, null);

		final float endAngle = currentPercent * 2.7f + 45;
		final float midAngle = endAngle / 2;

		final PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
		final Paint paint = new Paint();
		paint.setFilterBitmap(false);

		final Bitmap inBitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
		final Canvas inCanvas = new Canvas(inBitmap);
		Bitmap mask = getMaskBitmap(0, midAngle);

		int sc = inCanvas.save(SAVE_FLAGS);
		inCanvas.drawBitmap(diskIndicator, 0, 0, paint);
		paint.setXfermode(mode);
		inCanvas.drawBitmap(mask, 0, 0, paint);
		paint.setXfermode(null);
		inCanvas.restoreToCount(sc);
		mask.recycle();

		// draw this on cache
		cacheCanvas.drawBitmap(inBitmap, 0, 0, null);


		// Now the second half.
		mask = getMaskBitmap(midAngle, endAngle + 30);
		inCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

		sc = inCanvas.save(SAVE_FLAGS);
		inCanvas.rotate(endAngle - 315, 64, 64);	// rotate
		inCanvas.drawBitmap(diskIndicator, 0, 0, paint);
		inCanvas.rotate(315 - endAngle, 64, 64);	// rotate back
		paint.setXfermode(mode);
		inCanvas.drawBitmap(mask, 0, 0, paint);
		paint.setXfermode(null);
		inCanvas.restoreToCount(sc);
		mask.recycle();

		// draw this on cache
		cacheCanvas.drawBitmap(inBitmap, 0, 0, paint);
		inBitmap.recycle();

		bindBitmap(gl, cache, textureNames[0], true);
	}

	private Bitmap getMaskBitmap(float startAngle, float endAngle) {
		final Bitmap mask = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
		final Canvas maskCanvas = new Canvas(mask);
		final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		maskPaint.setColor(0xFF66AAFF);
		maskPaint.setStyle(Paint.Style.FILL);

		maskCanvas.rotate(90, 64, 64);
		maskCanvas.drawArc(new RectF(0, 0, 128, 128), startAngle, endAngle-startAngle, true, maskPaint);

		return mask;
	}
}
