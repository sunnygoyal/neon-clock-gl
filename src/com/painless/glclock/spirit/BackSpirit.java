package com.painless.glclock.spirit;

import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11Ext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;

import com.painless.glclock.ColorUtils;
import com.painless.glclock.Constants;
import com.painless.glclock.R;

public final class BackSpirit extends Spirit {

	private int color;
	private String type;

	private final Context context;

	public BackSpirit(Context context) {
		super(context, 1);
		this.context = context;
	}

	@Override
	public int[] getResources() {
		return new int[] { R.drawable.sample_back };
	}

	@Override
	public void draw(GL10 gl) {
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureNames[0]);
		((GL11Ext) gl).glDrawTexfOES(0, 0, 0, width, height);
	}

	public void setColor(int color, String type) {
		if ((this.color == color) && this.type.equals(type)) {
			return;
		}
		this.color = color;
		this.type = type;
		shutdown();
	}

	@Override
	protected void bindBitmap(GL10 gl, Bitmap bitmap, int textureName, boolean onlyAlpha) {
		super.bindBitmap(gl, getActualBitmap(bitmap), textureName, false);
	}

	/**
	 * Returns the actual appliable bitmap.
	 * @param bitmap sample back
	 */
	public Bitmap getActualBitmap(Bitmap bitmap) {
		final Paint paint = ColorUtils.getPaintFromColor(color);
		if (type.equals(Constants.BACK_NONE)) {
			bitmap.recycle();
			bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.RGB_565);
		} else if (type.equals(Constants.BACK1_URL) || type.equals(Constants.BACK2_URL)) {
			Bitmap back = null;
			try {
				final InputStream in = context.openFileInput(type);
				back = BitmapFactory.decodeStream(in);
				in.close();
			} catch (final Exception e) { }

			if (back != null) {
				bitmap.recycle();
				bitmap = back;
			} else {
				bitmap =  ColorUtils.getPaintedBitmap(bitmap, paint, true);
			}
		} else {
			bitmap =  ColorUtils.getPaintedBitmap(bitmap, paint, true);
		}
		return bitmap;
	}
}
