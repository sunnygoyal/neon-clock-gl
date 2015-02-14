package com.painless.glclock.util;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout.LayoutParams;

import com.painless.glclock.R;

public class LayoutSlideAnim extends Animation {

	private final View mtargetView;
	private final LayoutParams mParams;
	private final boolean mSlideIn;

	private final int mDelta;
	private final boolean mIsVert;

	public LayoutSlideAnim(View target, boolean slideIn, boolean isVert) {
		mSlideIn = slideIn;
		mtargetView = target;
		mParams = (LayoutParams) target.getLayoutParams();
		mDelta = (int) target.getResources().getDimension(R.dimen.info_margin);
		mIsVert = isVert;

		setFillAfter(true);
		setDuration(200);
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		if (mSlideIn) {
			interpolatedTime = 1 - interpolatedTime;
		}

		if (mIsVert) {
			mParams.topMargin = (int) (-interpolatedTime * mtargetView.getHeight());
		} else {
			mParams.leftMargin = (int) (-interpolatedTime * (mtargetView.getWidth() - mDelta)) - mDelta;			
		}

		mtargetView.setLayoutParams(mParams);
	}

}
