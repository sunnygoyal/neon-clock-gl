package com.painless.glclock.spirit;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.painless.glclock.Globals;
import com.painless.glclock.Grid;
import com.painless.glclock.R;

final class BatterySpirit extends MovableSpirit {

	private final Bitmap batteryBitmap;
	private final DigitSpirit digit;
	private final Grid grid;

	private int batteryCount = 0;

	double callerCount = 0;

	public BatterySpirit(Context context, SpiritManager spiritManager) {
		super(context, 1, SpiritManager.NO_SERVICE);

		batteryBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.battery);
		this.digit = spiritManager.digits;
		grid = Grid.getSimpleGrid(128, 128);
	}

	@Override
	int[] getResources() {
		return new int[]{ };
	}

	@Override
	public void drawOnPos(GL10 gl) {
		if (batteryCount != Globals.batteryCount) {
			createBatteryTexture(gl);
		}

		bind(gl, 0);
		grid.draw(gl);

		gl.glTranslatef(-15, 0, 0);
		digit.drawNumber(gl, Globals.battery, 30, 0);
		gl.glPopMatrix();
	}

	@Override
	public void loadTextures(GL10 gl) {
		super.loadTextures(gl);

		batteryCount = 0;
	}

	private void createBatteryTexture(GL10 gl) {
		batteryCount = Globals.batteryCount;

		final Bitmap cache = Bitmap.createBitmap(128, 128, Bitmap.Config.RGB_565);
		final Canvas c = new Canvas(cache);
		final Rect sourceRect1 = new Rect(0, 0, 26, 40);
		final Rect sourceRect2 = new Rect(26, 0, 52, 40);
		final Rect destRect = new Rect(-8, -64, 18, -24);

		c.translate(64, 64);
		for (int i=0; i < batteryCount; i++) {
			c.save();
			c.rotate(-i*15);
			c.drawBitmap(batteryBitmap, sourceRect1, destRect, null);
			c.restore();
		}
		for (int i=batteryCount; i < 24; i++) {
			c.save();
			c.rotate(-i*15);
			c.drawBitmap(batteryBitmap, sourceRect2, destRect, null);
			c.restore();
		}
		bindBitmap(gl, cache, textureNames[0], true);
	}
}
