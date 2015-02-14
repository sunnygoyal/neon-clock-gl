package com.painless.glclock.setting;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class DragPreview extends View {

	private final ArrayList<DragImg> mList = new ArrayList<DragImg>();

	private DragImg mSelected;

	private final Paint mSelectionPaint = new Paint();
	private final Rect mBackDest = new Rect(0, 0, 0, 0);
	private Bitmap mBack;
	private Rect mBackSrc;

	public DragPreview(Context context, AttributeSet attrs) {
		super(context, attrs);

		mSelectionPaint.setStyle(Paint.Style.STROKE);
		mSelectionPaint.setStrokeWidth(2);
		mSelectionPaint.setColor(0xFF13BEED);
	}

	public void addAll(ArrayList<DragImg> list) {
		mList.addAll(list);
	}

	public void setBack(Bitmap back) {
		mBack = back;
		mBackSrc = new Rect(0, 0, back.getWidth(), back.getHeight());
	}

	public void setSelected(DragImg img) {
		mSelected = img;
	}

	@Override
	protected void onDraw(Canvas c) {
		if (mBack != null) {
			mBackDest.right = getWidth();
			mBackDest.bottom = getHeight();
			c.drawBitmap(mBack, mBackSrc, mBackDest, null);
		}

		for (DragImg img : mList) {
			c.drawBitmap(img.previewBitmap, img.srcRect, img.dstRect, img.drawPaint);
		}
		if (mSelected != null) {
			c.drawRect(mSelected.dstRect, mSelectionPaint);
		}
	}
}
