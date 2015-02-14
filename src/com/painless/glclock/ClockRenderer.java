package com.painless.glclock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.painless.glclock.spirit.SpiritManager;

public class ClockRenderer implements GLWallpaperService.Renderer {

	private static final long targetFrameInterval = 1000L / 15L; // target 15 FPS

	// An array of things to draw every frame.
	private final SpiritManager manager;

	private float x, y;

	public ClockRenderer(SpiritManager manager) {
		this.manager = manager;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		final long frameStartTime = System.currentTimeMillis();

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef(x, y, 0);
		gl.glPushMatrix();
		manager.draw(gl);

		gl.glPopMatrix();

		// Sleep the extra time
		final long frameEndTime = System.currentTimeMillis();
		final long delta = frameEndTime - frameStartTime;
		if (targetFrameInterval - delta > 10L) {
			try {
				Thread.sleep(targetFrameInterval - delta);
			} catch (final InterruptedException e) {}
		}

	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);

		x = width / 2;
		y = height / 2;

		/*
		 * Set our projection matrix. This doesn't have to be done each time we
		 * draw, but usually a new projection needs to be set when the viewport
		 * is resized.
		 */
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrthof(0.0f, width, 0.0f, height, 0.0f, 1.0f);

		//gl.glShadeModel(GL10.GL_FLAT);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL10.GL_TEXTURE_2D);

		// for grid
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

		manager.setDim(width, height);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig eglconfig) {
		Debug.log("Renderer Surface created");

		/*
		 * Some one-time OpenGL initialization can be made here probably based
		 * on features of this particular context
		 */
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

		gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
		//gl.glShadeModel(GL10.GL_FLAT);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		/*
		 * By default, OpenGL enables features that improve quality but reduce
		 * performance. One might want to tweak that especially on software
		 * renderer.
		 */
		gl.glDisable(GL10.GL_DITHER);
		gl.glDisable(GL10.GL_LIGHTING);

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
	}

	public void shutdown() {
		manager.shutdown();
	}
}
