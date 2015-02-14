package com.painless.glclock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * A 2D rectangular mesh. Can be drawn textured or untextured.
 * This version is modified from the original Grid.java (found in
 * the SpriteText package in the APIDemos Android sample) to support hardware
 * vertex buffers.
 */
public final class Grid {
	private final FloatBuffer mFloatVertexBuffer;
	private final FloatBuffer mFloatTexCoordBuffer;

	private final CharBuffer mIndexBuffer;

	private final int mW;
	private final int mH;
	private final int mIndexCount;

	public Grid(int vertsAcross, int vertsDown) {
		mW = vertsAcross;
		mH = vertsDown;
		final int size = vertsAcross * vertsDown;
		final int FLOAT_SIZE = 4;
		final int CHAR_SIZE = 2;

		mFloatVertexBuffer = ByteBuffer.allocateDirect(FLOAT_SIZE * size * 3)
		.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mFloatTexCoordBuffer = ByteBuffer.allocateDirect(FLOAT_SIZE * size * 2)
		.order(ByteOrder.nativeOrder()).asFloatBuffer();

		final int quadW = mW - 1;
		final int quadH = mH - 1;
		final int quadCount = quadW * quadH;
		final int indexCount = quadCount * 6;
		mIndexCount = indexCount;
		mIndexBuffer = ByteBuffer.allocateDirect(CHAR_SIZE * indexCount)
		.order(ByteOrder.nativeOrder()).asCharBuffer();

		/*
		 * Initialize triangle list mesh.
		 *
		 *     [0]-----[  1] ...
		 *      |    /   |
		 *      |   /    |
		 *      |  /     |
		 *     [w]-----[w+1] ...
		 *      |       |
		 *
		 */

		{
			int i = 0;
			for (int y = 0; y < quadH; y++) {
				for (int x = 0; x < quadW; x++) {
					final char a = (char) (y * mW + x);
					final char b = (char) (y * mW + x + 1);
					final char c = (char) ((y + 1) * mW + x);
					final char d = (char) ((y + 1) * mW + x + 1);

					mIndexBuffer.put(i++, a);
					mIndexBuffer.put(i++, b);
					mIndexBuffer.put(i++, c);

					mIndexBuffer.put(i++, b);
					mIndexBuffer.put(i++, c);
					mIndexBuffer.put(i++, d);
				}
			}
		}
	}

	public void set(int i, int j, float x, float y, float z, float u, float v) {
		if (i < 0 || i >= mW) {
			throw new IllegalArgumentException("i");
		}
		if (j < 0 || j >= mH) {
			throw new IllegalArgumentException("j");
		}

		final int index = mW * j + i;

		final int posIndex = index * 3;
		final int texIndex = index * 2;

		mFloatVertexBuffer.put(posIndex, x);
		mFloatVertexBuffer.put(posIndex + 1, y);
		mFloatVertexBuffer.put(posIndex + 2, z);

		mFloatTexCoordBuffer.put(texIndex, u);
		mFloatTexCoordBuffer.put(texIndex + 1, v);
	}

	public void draw(GL10 gl) {
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFloatVertexBuffer);

		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mFloatTexCoordBuffer);

		gl.glDrawElements(GL10.GL_TRIANGLES, mIndexCount,
				GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
	}

	public static Grid getSimpleGrid(int width, int height) {
		final Grid grid = new Grid(2, 2);
		grid.set(0, 0, -width / 2, -height / 2, 0.0f, 0.0f, 1.0f);
		grid.set(1, 0,  width / 2, -height / 2, 0.0f, 1.0f, 1.0f);
		grid.set(0, 1, -width / 2,  height / 2, 0.0f, 0.0f, 0.0f);
		grid.set(1, 1,  width / 2,  height / 2, 0.0f, 1.0f, 0.0f);
		return grid;
	}
}
