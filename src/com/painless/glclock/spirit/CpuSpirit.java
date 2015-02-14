package com.painless.glclock.spirit;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

import com.painless.glclock.Globals;
import com.painless.glclock.Grid;
import com.painless.glclock.R;
import com.painless.glclock.service.CpuService;

final class CpuSpirit extends MovableSpirit {

	private static final CpuService SERVICE = new CpuService();

	private final Grid frameGrid;
	private final Grid indicatorGrid;
	private final DigitSpirit digit;

	private float currentAngle = 0;

	public CpuSpirit(Context context, SpiritManager spiritManager) {
		super(context, 2, SERVICE);

		frameGrid = Grid.getSimpleGrid(128, 128);
		indicatorGrid = Grid.getSimpleGrid(32, 64);
		this.digit = spiritManager.digits;
	}

	@Override
	int[] getResources() {
		return new int[] { R.drawable.cpu_back, R.drawable.cpu_indicator };
	}

	@Override
	public void drawOnPos(GL10 gl) {

		// draw cpu frame
		bind(gl, 0);
		frameGrid.draw(gl);

		currentAngle += Math.signum(Globals.cpuUsageAngle - currentAngle);
		currentAngle += Math.signum(Globals.cpuUsageAngle - currentAngle);
		currentAngle += Math.signum(Globals.cpuUsageAngle - currentAngle);

		// draw second
		bind(gl, 1);
		gl.glRotatef(-currentAngle, 0, 0, 1);
		gl.glTranslatef(0, 16, 0);		//Match the center point
		indicatorGrid.draw(gl);
		gl.glPopMatrix();

		gl.glTranslatef(-10, -36, 0);
		digit.drawNumber(gl, Globals.cpuUsage, 22, 10);
		gl.glPopMatrix();
	}

}
