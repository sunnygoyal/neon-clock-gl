package com.painless.glclock.spirit;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

import com.painless.glclock.Grid;
import com.painless.glclock.R;

final class DigitSpirit extends Spirit {

	private final Grid[] digitGrids;

	public DigitSpirit(Context context) {
		super(context, 1);

		digitGrids = new Grid[20];
		float factor = 30f / 256;
		for (int i=0; i<7; ) {
			digitGrids[i] = getSimpleGrid(30, 32, i*factor, ++i*factor, 0, 0.5f);
		}
		digitGrids[7] = getSimpleGrid(30, 32, 7*factor, 8*factor -.01f, 0, 0.5f);

		final float factor2 = factor + factor;
		digitGrids[8] = getSimpleGrid(30, 32, 0, factor, 0.5f, 1);
		digitGrids[9] = getSimpleGrid(30, 32, factor, factor2, 0.5f, 1);
		factor = 22f / 256;
		for (int i=10, j=0; i<19; i++) {
			digitGrids[i] = getSimpleGrid(22, 32, factor2 + j*factor, factor2 + ++j*factor, 0.5f, 1);
		}
		digitGrids[19] = getSimpleGrid(22, 32, 1.01f - factor, 1, 0, 0.5f);
	}

	@Override
	int[] getResources() {
		return new int[] { R.drawable.digits };
	}

	@Override
	public void draw(GL10 gl) {
		// DO nothing.
	}

	public void drawNumber(GL10 gl, int number, int gap, int start) {
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureNames[0]);
		if (number < 0) {
			number = 0;
		}
		final int d1 = (number % 100) / 10 + start;
		final int d2 = (number % 10) + start;
		digitGrids[d1].draw(gl);
		gl.glTranslatef(gap, 0, 0);
		digitGrids[d2].draw(gl);
	}
}
