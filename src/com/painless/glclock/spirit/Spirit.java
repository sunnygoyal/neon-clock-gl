package com.painless.glclock.spirit;

import java.nio.ByteBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.painless.glclock.Debug;
import com.painless.glclock.Grid;

abstract class Spirit {

	private static final int[] pixels = new int[1024*1024];
	private static final byte[] alphaPixels = new byte[1024*1024*3];

	protected final int[] textureNames;
	protected int width, height;

	// A reference to the application context.
	protected final Context mContext;
	protected GL10 mGl;

	Spirit(Context context, int textureCount) {
		this.mContext = context;
		textureNames = new int[textureCount];
	}

	public void setDim(int width, int height) {
		this.width = width;
		this.height = height;
	}

	abstract int[] getResources();

	public void loadTextures(GL10 gl) {
		mGl = gl;

		final int[] mTextureNameWorkspace = new int[1];
		if (isLoaded()) {
			shutdown();
		}

		int i=0;
		// generate texture names
		for (; i<textureNames.length; i++) {
			gl.glGenTextures(1, mTextureNameWorkspace, 0);
			final int textureName = mTextureNameWorkspace[0];
			textureNames[i] = textureName;
		}

		i = 0;
		for (final int resource : getResources()) {
			// load resource
			loadBitmap(gl, resource, textureNames[i]);
			i++;
		}
	}

	public boolean isLoaded() {
		return (textureNames.length > 0) && (textureNames[0] != 0);
	}

	private void loadBitmap(GL10 gl, int resourceId, int textureName) {
		if (mContext != null && gl != null) {
			final Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resourceId);
			bindBitmap(gl, bitmap, textureName, true);
		}
	}

	protected void bindBitmap(GL10 gl, Bitmap bitmap, int textureName, boolean onlyAlpha) {
		final int[] mCropWorkspace = new int[4];
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureName);

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

		gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);

		bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

		if (onlyAlpha) {
			for (int i=0;i<pixels.length;i+=1) {
				final int argb = pixels[i];
				alphaPixels[i] = (byte) Color.red(argb);
			}

			gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_ALPHA, bitmap.getWidth(), bitmap.getHeight(),
					0, GL10.GL_ALPHA, GL10.GL_UNSIGNED_BYTE, ByteBuffer.wrap(alphaPixels));

		} else {
			for (int i=0;i<pixels.length;i+=1) {
				final int argb = pixels[i];
				alphaPixels[i*3    ] = (byte) ((argb>>16)&0xff);	// red
				alphaPixels[i*3 + 1] = (byte) ((argb>>8)&0xff);		// green
				alphaPixels[i*3 + 2] = (byte) (argb&0xff);			// blue
			}

			gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGB, bitmap.getWidth(), bitmap.getHeight(),
					0, GL10.GL_RGB, GL10.GL_UNSIGNED_BYTE, ByteBuffer.wrap(alphaPixels));
		}

		mCropWorkspace[0] = 0;
		mCropWorkspace[1] = bitmap.getHeight();
		mCropWorkspace[2] = bitmap.getWidth();
		mCropWorkspace[3] = -bitmap.getHeight();

		bitmap.recycle();

		((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D,
				GL11Ext.GL_TEXTURE_CROP_RECT_OES, mCropWorkspace, 0);

		final int error = gl.glGetError();
		if (error != GL10.GL_NO_ERROR) {
			Debug.log("Spirit Texture Load GLError: " + error);
		}
	}

	public void shutdown() {
		if (mGl == null) {
			return;
		}
		final int[] textureToDelete = new int[1];
		for (int i=0; i<textureNames.length; i++) {
			textureToDelete[0] = textureNames[i];
			mGl.glDeleteTextures(1, textureToDelete, 0);
			textureNames[i] = 0;
		}
	}

	abstract public void draw(GL10 gl);

	private int red,green,blue;
	public void setColor(int color) {
		this.red = 0x10000 * Color.red(color) / 255;
		this.green = 0x10000 * Color.green(color) / 255;
		this.blue = 0x10000 * Color.blue(color) / 255;
	}

	public void color(GL10 gl) {
		gl.glColor4x(red, green, blue, 0x10000);
	}

	static final Grid getSimpleGrid(int width, int height, float startU, float endU, float startV, float endV) {
		final Grid grid = new Grid(2, 2);
		grid.set(0, 0, -width / 2, -height / 2, 0.0f, startU,   endV);
		grid.set(1, 0,  width / 2, -height / 2, 0.0f,   endU,   endV);
		grid.set(0, 1, -width / 2,  height / 2, 0.0f, startU, startV);
		grid.set(1, 1,  width / 2,  height / 2, 0.0f,   endU, startV);
		return grid;
	}
}
