package com.painless.glclock.spirit;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.SharedPreferences;

import com.painless.glclock.service.RService;

abstract class MovableSpirit extends Spirit {

	private int x, y;
	private final RService service;
	private float zoom = 1;

	MovableSpirit(Context context, int textureCount, RService service) {
		super(context, textureCount);
		this.service = service;
	}

	void setOffset(int[] offsets) {
		this.x = offsets[0];
		this.y = -offsets[1];
	}

	void setZoom(float zoom) {
		this.zoom = zoom;
	}

	@Override
	public final void draw(GL10 gl) {
		gl.glPushMatrix();
		gl.glTranslatef(x, y, 0);
		gl.glScalef(zoom, zoom, 0);
		drawOnPos(gl);
		gl.glPopMatrix();
	}

	abstract void drawOnPos(GL10 gl);

	void bind(GL10 gl, int textureId) {
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureNames[textureId]);
		gl.glPushMatrix();
	}

	RService getService() {
		return service;
	}

	@Override
	public void shutdown() {
		super.shutdown();
		getService().stop();
	}

	/**
	 * Returns true is reload is required.
	 */
	boolean updatePref(SharedPreferences pref) {
		return false;
	}
}
