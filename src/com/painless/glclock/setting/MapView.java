package com.painless.glclock.setting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Scroller;

import com.painless.glclock.Debug;
import com.painless.glclock.R;
import com.painless.glclock.util.PathParser;

public class MapView extends FrameLayout {

  private static final float MAP_WIDTH = 900;
  private static final float MAP_HEIGHT = 440.70631074413296f;

  private static final double X1 = -20004297.151525836;
  private static final double Y1 = -12671671.123330014;

  private static final double X2 = 20026572.39474939;
  private static final double Y2 = 6930392.02513512;

  private static final double CENTRAL_MERIDIAN = 11.5;
  private static final double RADIUS = 6381372;
  private static final double RAD_DEG = Math.PI / 180;

  private final float mBaseZoom;

  private final Paint mPaint;
  private Path mPath;

  private final int mLeftGap, mTopGap;
  private final LayoutInflater mInflator;
  private final Scroller mScroller;

  private float mCurrentZoom = 1;
  private int mMapWidth, mMapHeight;

  private VelocityTracker mVelocityTracker;
  private int mMinimumVelocity;
  private int mMaximumVelocity;

  public MapView(Context context, AttributeSet attrs) {
    super(context, attrs);

    mPaint = new Paint();
    mPaint.setStyle(Paint.Style.FILL);
    mPaint.setColor(0xFF01579B);
    new MapLoader().execute();

    setWillNotDraw(false);
    mBaseZoom = getResources().getDimension(R.dimen.map_width) / MAP_WIDTH;

    mLeftGap = getResources().getDimensionPixelSize(R.dimen.map_left_space);
    mTopGap = getResources().getDimensionPixelSize(R.dimen.map_top_space);

    mScroller = new Scroller(context);
    mInflator = LayoutInflater.from(context);

    setZoom(1);

    final ViewConfiguration configuration = ViewConfiguration.get(context);
    mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
    mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
  }

  public void setZoom(float zoom) {
    // On scrolling the center point should remail the same.
    int scrollX = getScrollX();
    int scrollY = getScrollY();

    float cX = 0, cY = 0;
    if (mCurrentZoom != 0) {
      cX = ((float) (mMapWidth - scrollX - getWidth() / 2)) / mMapWidth;
      cY = ((float) (mMapHeight - scrollY - getHeight() / 2)) / mMapHeight;
    }

    mCurrentZoom = zoom;
    mMapWidth = (int) (MAP_WIDTH * mBaseZoom * mCurrentZoom) + 2 * mLeftGap;
    mMapHeight = (int) (MAP_HEIGHT * mBaseZoom * mCurrentZoom) + 2 * mTopGap;

    if (cX != 0 && cY != 0) {
      scrollX = (int) (mMapWidth - mMapWidth * cX - getWidth() / 2);
      scrollY = (int) (mMapHeight - mMapHeight * cY - getHeight() / 2);
    }

    scrollX = Math.max(0, Math.min(scrollX, getScrollXRange()));
    scrollY = Math.max(0, Math.min(scrollY, getScrollYRange()));
    scrollTo(scrollX, scrollY);

    for (int i = 0; i < getChildCount(); i++) {
      View v = getChildAt(i);
      CityInfo info = (CityInfo) v.getTag();
      Point margin = latlongToPoint(info.lat, info.lng);
      if (margin != null) {
        applyMargins(v, margin);
      }
    }
    requestLayout();
    invalidate();
  }

  public View addMarker(CityInfo info) {
    Point margin = latlongToPoint(info.lat, info.lng);
    if (margin != null) {
      // Add shadow to the start
      ImageView shadow = (ImageView) mInflator.inflate(R.layout.map_marker, this, false);
      shadow.setTag(info);
      shadow.setImageResource(R.drawable.ic_marker_shadow);
      applyMargins(shadow, margin);
      addView(shadow, 0);

      // Add pointer to the end
      View v = mInflator.inflate(R.layout.map_marker, this, false);
      v.setTag(info);
      applyMargins(v, margin);
      addView(v);
      return v;
    }
    return null;
  }

  private void applyMargins(View v, Point margin) {
    LayoutParams lp = (LayoutParams) v.getLayoutParams();
    lp.leftMargin = margin.x;
    lp.topMargin = margin.y;
  }

  private int getScrollXRange() {
    return mMapWidth - getMeasuredWidth();
  }

  private int getScrollYRange() {
    return mMapHeight - getHeight();
  }

  private int mLastMotionX, mLastMotionY;

  private void initVelocityTrackerIfNotExists() {
    if (mVelocityTracker == null) {
      mVelocityTracker = VelocityTracker.obtain();
    }
  }

  private void recycleVelocityTracker() {
    if (mVelocityTracker != null) {
      mVelocityTracker.recycle();
      mVelocityTracker = null;
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    initVelocityTrackerIfNotExists();

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN: {
        mLastMotionX = (int) event.getX();
        mLastMotionY = (int) event.getY();

        if (!mScroller.isFinished()) {
          mScroller.abortAnimation();
        }
        mVelocityTracker.clear();
        mVelocityTracker.addMovement(event);
        break;
      }
      case MotionEvent.ACTION_MOVE: {
        final int x = (int) event.getX();
        final int y = (int) event.getY();

        int scrollX = getScrollX() + mLastMotionX - x;
        scrollX = Math.max(0, Math.min(scrollX, getScrollXRange()));

        int scrollY = getScrollY() + mLastMotionY - y;
        scrollY = Math.max(0, Math.min(scrollY, getScrollYRange()));
        scrollTo(scrollX, scrollY);

        mLastMotionX = x;
        mLastMotionY = y;

        mVelocityTracker.addMovement(event);
        invalidate();
        break;
      }
      
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP: {
        mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
        float vX = mVelocityTracker.getXVelocity();
        float vY = mVelocityTracker.getYVelocity();
        if (Math.abs(vX) < mMinimumVelocity) {
          vX = 0;
        }
        if (Math.abs(vY) < mMinimumVelocity) {
          vY = 0;
        }
        if (vX != 0 || vY != 0) {
          mScroller.forceFinished(true);
          mScroller.fling(getScrollX(), getScrollY(), (int) -vX, (int) -vY, 0, getScrollXRange(), 0, getScrollYRange());
          postInvalidate();
        }
        recycleVelocityTracker();
      }
    }
    return true;
  }

  @Override
  public void computeScroll() {
    if (mScroller.computeScrollOffset()) {
      scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (mPath != null) {
      canvas.save();
      canvas.translate(mLeftGap, mTopGap);
      canvas.scale(mCurrentZoom, mCurrentZoom);
      canvas.drawPath(mPath, mPaint);
      canvas.restore();
    }
  }

  private Point latlongToPoint(double lat, double lng) {
    if (lng < (-180 + CENTRAL_MERIDIAN)) {
      lng += 360;
    }
    double x = RADIUS * (lng - CENTRAL_MERIDIAN) * RAD_DEG;
    double y = RADIUS * Math.log(Math.tan((45 - 0.4 * lat) * RAD_DEG)) / 0.8;

    if (x < X1 || x >= X2 || y < Y1 || y >= Y2) {
      return null;
    }
    float scale = mBaseZoom * mCurrentZoom;
    x = ((x - X1) / (X2 - X1)) * MAP_WIDTH * scale;
    y = ((y - Y1) / (Y2 - Y1)) * MAP_HEIGHT * scale;

    return new Point((int) x,  (int) y);
  }

  private class MapLoader extends AsyncTask<Void, Void, Path> {

    @Override
    protected Path doInBackground(Void... params) {
      Path p = new Path();
      InputStream in = getResources().openRawResource(R.raw.map);
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        while((line = reader.readLine()) != null) {
          PathParser.parse(p, line);
        }

        RectF bounds = new RectF();
        p.computeBounds(bounds, true);

        Matrix m = new Matrix();
        m.setScale(mBaseZoom, mBaseZoom);
        p.transform(m);

      } catch (Exception e) {
        Debug.log(e);
        return null;
      } finally {
        try {
          if (in != null) in.close();
        } catch (IOException e) { }
      }
      return p;
    }

    @Override
    protected void onPostExecute(Path result) {
      mPath = result;
      invalidate();
    }
  }
}
