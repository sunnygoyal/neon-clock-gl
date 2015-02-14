package com.painless.glclock.spirit;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.SharedPreferences;

import com.painless.glclock.Globals;
import com.painless.glclock.Grid;
import com.painless.glclock.R;
import com.painless.glclock.service.CompassService;

final class CompassSpirit extends MovableSpirit {

	private final Grid grid;
	private float rotation = 0;

	private int deltaSettings = 0;
	private int deltaApplied = 0;

	public CompassSpirit(Context context, SpiritManager spiritManager) {
		super(context, 1, getService(context));

		grid = Grid.getSimpleGrid(128, 128);
	}

	@Override
	public int[] getResources() {
		return new int[] { R.drawable.compass };
	}

	@Override
	public void drawOnPos(GL10 gl) {
		if ((rotation - Globals.compassRotation) > 180) {
			rotation -= 360;
		} else if ((Globals.compassRotation - rotation) > 180) {
			rotation += 360;
		}
		rotation = (rotation + Globals.compassRotation) / 2;

		bind(gl, 0);
		gl.glRotatef(Globals.compassRotation + deltaApplied, 0, 0, 1);
		grid.draw(gl);
		gl.glPopMatrix();
	}

	@Override
	public void setDim(int width, int height) {
		super.setDim(width, height);
		applyDelta();
	}

	private void applyDelta() {
		if (deltaSettings == 0) {
			deltaApplied = 0;
		} else {
			final float orientation = Math.signum(width - height);
			deltaApplied = orientation == Math.signum(deltaSettings) ?
					(int) orientation * deltaSettings : 0;
		}
	}

	@Override
	boolean updatePref(SharedPreferences pref) {
		deltaSettings = Integer.parseInt(pref.getString("compass_delta", "0"));
		applyDelta();
		return false;
	}

	private static CompassService SERVICE = null;
	private static CompassService getService(Context context) {
		if (SERVICE == null) {
			SERVICE = new CompassService(context);
		}
		return SERVICE;
	}
}
