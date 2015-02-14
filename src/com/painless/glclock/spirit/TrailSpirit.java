package com.painless.glclock.spirit;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.content.Context;

import com.painless.glclock.Debug;

final class TrailSpirit extends Spirit {

	private static final int MIN_TRAIL_LENGTH = 4;
	private static final int BUFFER_SIZE = 64 * 128;

	private static final int TRAIL_LENGTH = 32;	// 2 ^ x
	private static final int TRAIL_AND = TRAIL_LENGTH - 1;

	// must have TRAIL_LENGTH entries
	private static final int[] ALPHAS = new int[TRAIL_LENGTH];
	static {
		for (int i=0; i<TRAIL_LENGTH; i++) {
			ALPHAS[i] = (200 * (TRAIL_LENGTH - i) / TRAIL_LENGTH);
		}
	}

	private final ArrayList<Trail> trails;
	private final Random r;
	private final int[] textureCrop = new int[4];

	private final byte[] pixelBytes;
	private final ByteBuffer pixelBuffer;

	private int textureW, textureH;
	private int textureDW, textureDH;
	private int trailLimitX, trailLimitY;

	public TrailSpirit(Context context) {
		super(context, 1);

		r = new Random();
		trails = new ArrayList<Trail>();

		pixelBytes = new byte[BUFFER_SIZE];
		pixelBuffer = ByteBuffer.wrap(pixelBytes);
	}

	@Override
	int[] getResources() {
		return new int[] { };
	}

	public void setCount(int count) {
		while(count < trails.size()) {
			trails.remove(0).clear();
		}
		while(count > trails.size()) {
			final Trail t = new Trail();
			if ((trailLimitX > 0) && (trailLimitY > 0)) {
				t.init();
			}
			trails.add(t);
		}
	}

	@Override
	public void loadTextures(GL10 gl) {
		super.loadTextures(gl);

		if (textureW == 0 || textureH == 0) {
			return;
		}

		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureNames[0]);

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);

		// and init the GL texture with the pixels
		gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_ALPHA, textureW, textureH,
				0, GL10.GL_ALPHA, GL10.GL_UNSIGNED_BYTE, pixelBuffer);

		final int error = gl.glGetError();
		if (error != GL10.GL_NO_ERROR) {
			Debug.log("Trails Texture Load GLError: " + error);
		}
	}

	@Override
	public void draw(GL10 gl) {
		for (final Trail t : trails) {
			t.clear();
		}
		for (final Trail t : trails) {
			t.tick();
		}

		// Choose the texture
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureNames[0]);

		// Update the texture
		gl.glTexSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, textureW, textureH,
				GL10.GL_ALPHA, GL10.GL_UNSIGNED_BYTE, pixelBuffer);

		// Draw the texture on the surface
		((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES, textureCrop, 0);
		((GL11Ext) gl).glDrawTexiOES(0, 0, 0, textureDW, textureDH);
	}

	private int nextPowerOf2(int n) {
		n |= (n >> 16);
		n |= (n >> 8);
		n |= (n >> 4);
		n |= (n >> 2);
		n |= (n >> 1);
		++n;
		return n;
	}

	@Override
	public void setDim(int width, int height) {
		final boolean reset = (width != this.width || height != this.height);
		super.setDim(width, height);
		if (reset) {
			int factor = 1;
			boolean factorFound = false;

			while(!factorFound) {
				factor ++;
				factorFound = nextPowerOf2(width / factor) * nextPowerOf2(height / factor) <= BUFFER_SIZE;
			}

			trailLimitX = width / factor;
			trailLimitY = height / factor;

			textureW = nextPowerOf2(trailLimitX);
			textureH = nextPowerOf2(trailLimitY);

			textureCrop[0] = 0;
			textureCrop[1] = textureH;
			textureCrop[2] = textureW;
			textureCrop[3] = textureH;

			textureDW = width * textureW / trailLimitX;
			textureDH = height * textureH / trailLimitY;

			for (final Trail t : trails) {
				t.init();
			}

			if (mGl != null) {
				loadTextures(mGl);
			}
		}
	}

	private final class Trail {

		public int x;
		public int y;
		public int finalX;
		public int finalY;
		public int dX;
		public int dY;
		public boolean isVert;

		private final int[] positionList = new int[TRAIL_LENGTH];
		private int positionStart = 0;

		public void init() {
			this.x = this.finalX = r.nextInt(trailLimitX);
			this.y = this.finalY = r.nextInt(trailLimitY);
			isVert = r.nextInt(10) < 5;

			reSchedule();
		}

		private void clear() {
			for (final int i : positionList) {
				pixelBytes[i] = 0;
			}
		}

		private void tick() {
			reSchedule();
			x += dX;
			y += dY;

			final int pos = x + textureW*y;
			positionList[positionStart] = pos;

			int j = positionStart;
			for (int i=0; i<TRAIL_LENGTH; i++) {
				// calculating the result alpha
				int alpha = pixelBytes[positionList[j]] & 0xff;
				alpha = alpha + ALPHAS[i] * (255 - alpha) / 255;
				alpha = alpha & 0xff;

				pixelBytes[positionList[j]] = (byte) alpha;
				j = (j + 1) & TRAIL_AND;
			}

			positionStart = (positionStart - 1) & TRAIL_AND;
		}

		private void reSchedule() {
			if (x==finalX && y==finalY) {
				isVert = !isVert;
				if (isVert) {
					dX = 0;

					final int yUp = Math.max(finalY - MIN_TRAIL_LENGTH, 0);
					final int yDown = Math.max(trailLimitY - finalY - MIN_TRAIL_LENGTH, 0);

					final int probable = r.nextInt(yUp + yDown);
					if (probable < yUp) {
						finalY = y - probable - MIN_TRAIL_LENGTH;
						dY = -1;
					} else {
						finalY = y + probable - yUp + MIN_TRAIL_LENGTH;
						dY = 1;
					}
				} else {
					dY = 0;

					final int xLeft = Math.max(finalX - MIN_TRAIL_LENGTH, 0);
					final int xRight = Math.max(trailLimitX - finalX - MIN_TRAIL_LENGTH, 0);

					final int probable = r.nextInt(xLeft + xRight);
					if (probable < xLeft) {
						finalX = x - probable - MIN_TRAIL_LENGTH;
						dX = -1;
					} else {
						finalX = x + probable - xLeft + MIN_TRAIL_LENGTH;
						dX = 1;
					}
				}
			}
		}
	}
}
