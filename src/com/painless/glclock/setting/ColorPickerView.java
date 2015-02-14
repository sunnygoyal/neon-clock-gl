package com.painless.glclock.setting;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerView extends View {

	public interface OnColorChangedListener{
		public void onColorChanged(int color);
	}

	private final static int        PANEL_SAT_VAL = 0;
	private final static int        PANEL_HUE = 1;

	/**
	 * The width in pixels of the border
	 * surrounding all color panels.
	 */
	private final static float      BORDER_WIDTH_PX = 1;

	/**
	 * The width in dp of the hue panel.
	 */
	private float           HUE_PANEL_WIDTH = 30f;
	/**
	 * The distance in dp between the different
	 * color panels.
	 */
	private float           PANEL_SPACING = 10f;
	/**
	 * The radius in dp of the color palette tracker circle.
	 */
	private float           PALETTE_CIRCLE_TRACKER_RADIUS = 5f;
	/**
	 * The dp which the tracker of the hue or alpha panel
	 * will extend outside of its bounds.
	 */
	private float           RECTANGLE_TRACKER_OFFSET = 2f;


	private static float mDensity = 1f;

	private OnColorChangedListener  mListener;

	private Paint           mSatValBackPaint;
	private Paint           mSatValPaint;
	private Paint           mSatValTrackerPaint;

	private Paint           mHuePaint;
	private Paint           mHueTrackerPaint;

	private Paint           mBorderPaint;

	private Shader          mSatShader;
	private Shader          mHueShader;

	private final int       mAlpha = 0xff;
	private float           mHue = 360f;
	private float           mSat = 0f;
	private float           mVal = 0f;

	private int             mSliderTrackerColor = 0xff1c1c1c;
	private int             mBorderColor = 0xff6E6E6E;

	/*
	 * To remember which panel that has the "focus" when
	 * processing hardware button data.
	 */
	private int                     mLastTouchedPanel = PANEL_SAT_VAL;

	/**
	 * Offset from the edge we must have or else
	 * the finger tracker will get clipped when
	 * it is drawn outside of the view.
	 */
	private float           mDrawingOffset;


	/*
	 * Distance form the edges of the view
	 * of where we are allowed to draw.
	 */
	private RectF   mDrawingRect;

	private RectF   mSatValRect;
	private RectF   mHueRect;

	private Point   mStartTouchPoint = null;


	public ColorPickerView(Context context){
		this(context, null);
	}

	public ColorPickerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ColorPickerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init(){
		mDensity = getContext().getResources().getDisplayMetrics().density;
		PALETTE_CIRCLE_TRACKER_RADIUS *= mDensity;
		RECTANGLE_TRACKER_OFFSET *= mDensity;
		HUE_PANEL_WIDTH *= mDensity;
		PANEL_SPACING = PANEL_SPACING * mDensity;

		mDrawingOffset = calculateRequiredOffset();

		initPaintTools();

		//Needed for receiving trackball motion events.
		setFocusable(true);
		setFocusableInTouchMode(true);
	}

	private void initPaintTools(){
		mSatValBackPaint = new Paint();

		mSatValPaint = new Paint();
		mSatValPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

		mSatValTrackerPaint = new Paint();
		mHuePaint = new Paint();
		mHueTrackerPaint = new Paint();
		mBorderPaint = new Paint();

		mSatValTrackerPaint.setStyle(Style.STROKE);
		mSatValTrackerPaint.setStrokeWidth(2f * mDensity);
		mSatValTrackerPaint.setAntiAlias(true);

		mHueTrackerPaint.setColor(mSliderTrackerColor);
		mHueTrackerPaint.setStyle(Style.STROKE);
		mHueTrackerPaint.setStrokeWidth(2f * mDensity);
		mHueTrackerPaint.setAntiAlias(true);
	}

	private float calculateRequiredOffset(){
		float offset = Math.max(PALETTE_CIRCLE_TRACKER_RADIUS, RECTANGLE_TRACKER_OFFSET);
		offset = Math.max(offset, BORDER_WIDTH_PX * mDensity);

		return offset * 1.5f;
	}

	private int[] buildHueColorArray(){

		final int[] hue = new int[361];

		int count = 0;
		for(int i = hue.length -1; i >= 0; i--, count++){
			hue[count] = Color.HSVToColor(new float[]{i, 1f, 1f});
		}

		return hue;
	}


	@Override
	protected void onDraw(Canvas canvas) {

		if(mDrawingRect.width() <= 0 || mDrawingRect.height() <= 0) {
			return;
		}

		drawSatValPanel(canvas);
		drawHuePanel(canvas);
	}

	private void drawSatValPanel(Canvas canvas){

		final RectF     rect = mSatValRect;

		if(BORDER_WIDTH_PX > 0){
			mBorderPaint.setColor(mBorderColor);
			canvas.drawRect(mDrawingRect.left, mDrawingRect.top, rect.right + BORDER_WIDTH_PX, rect.bottom + BORDER_WIDTH_PX, mBorderPaint);
		}

		final int rgb = Color.HSVToColor(new float[]{mHue,1f,1f});

		mSatShader = new LinearGradient(rect.left, rect.top, rect.right, rect.top,
				0xffffffff, rgb, TileMode.CLAMP);


		mSatValPaint.setShader(mSatShader);

		canvas.drawRect(rect, mSatValBackPaint);
		canvas.drawRect(rect, mSatValPaint);

		final Point p = satValToPoint(mSat, mVal);

		mSatValTrackerPaint.setColor(0xff000000);
		canvas.drawCircle(p.x, p.y, PALETTE_CIRCLE_TRACKER_RADIUS - 1f * mDensity, mSatValTrackerPaint);

		mSatValTrackerPaint.setColor(0xffdddddd);
		canvas.drawCircle(p.x, p.y, PALETTE_CIRCLE_TRACKER_RADIUS, mSatValTrackerPaint);
	}

	private void drawHuePanel(Canvas canvas){

		final RectF rect = mHueRect;

		if(BORDER_WIDTH_PX > 0){
			mBorderPaint.setColor(mBorderColor);
			canvas.drawRect(rect.left - BORDER_WIDTH_PX,
					rect.top - BORDER_WIDTH_PX,
					rect.right + BORDER_WIDTH_PX,
					rect.bottom + BORDER_WIDTH_PX,
					mBorderPaint);
		}

		if (mHueShader == null) {
			mHueShader = new LinearGradient(rect.left, rect.top, rect.left, rect.bottom, buildHueColorArray(), null, TileMode.CLAMP);
			mHuePaint.setShader(mHueShader);
		}

		canvas.drawRect(rect, mHuePaint);

		final float rectHeight = 4 * mDensity / 2;

		final Point p = hueToPoint(mHue);

		final RectF r = new RectF();
		r.left = rect.left - RECTANGLE_TRACKER_OFFSET;
		r.right = rect.right + RECTANGLE_TRACKER_OFFSET;
		r.top = p.y - rectHeight;
		r.bottom = p.y + rectHeight;


		canvas.drawRoundRect(r, 2, 2, mHueTrackerPaint);

	}

	private Point hueToPoint(float hue){
		final RectF rect = mHueRect;
		final float height = rect.height();

		final Point p = new Point();

		p.y = (int) (height - (hue * height / 360f) + rect.top);
		p.x = (int) rect.left;

		return p;
	}

	private Point satValToPoint(float sat, float val){
		final RectF rect = mSatValRect;
		final float height = rect.height();
		final float width = rect.width();

		final Point p = new Point();

		p.x = (int) (sat * width + rect.left);
		p.y = (int) ((1f - val) * height + rect.top);

		return p;
	}

	private float inRange(float val, float min, float max) {
		return Math.max(Math.min(val, max), min);
	}

	private float[] pointToSatVal(float x, float y){

		final RectF rect = mSatValRect;
		final float[] result = new float[2];

		final float width = rect.width();
		final float height = rect.height();

		x = inRange(x - rect.left, 0, width);
		y = inRange(y - rect.top, 0, height);

		result[0] = 1.f / width * x;
		result[1] = 1.f - (1.f / height * y);

		return result;
	}

	private float pointToHue(float y){

		final RectF rect = mHueRect;
		final float height = rect.height();
		y = inRange(y - rect.top, 0, height);

		return 360f - (y * 360f / height);
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {

		final float x = event.getX();
		final float y = event.getY();

		boolean update = false;


		if(event.getAction() == MotionEvent.ACTION_MOVE){

			switch(mLastTouchedPanel){

				case PANEL_SAT_VAL:

					float sat, val;

					sat = mSat + x/50f;
					sat = inRange(sat, 0, 1);

					val = mVal - y/50f;
					val = inRange(val, 0, 1);

					mSat = sat;
					mVal = val;

					update = true;

					break;

				case PANEL_HUE:

					float hue = mHue - y * 10f;
					hue = inRange(hue, 0, 360);
					mHue = hue;

					update = true;
					break;
			}


		}


		if(update){

			if(mListener != null){
				mListener.onColorChanged(Color.HSVToColor(mAlpha, new float[]{mHue, mSat, mVal}));
			}

			invalidate();
			return true;
		}


		return super.onTrackballEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		boolean update = false;

		switch(event.getAction()){

			case MotionEvent.ACTION_DOWN:

				mStartTouchPoint = new Point((int)event.getX(), (int)event.getY());

				update = moveTrackersIfNeeded(event);

				break;

			case MotionEvent.ACTION_MOVE:

				update = moveTrackersIfNeeded(event);

				break;

			case MotionEvent.ACTION_UP:

				mStartTouchPoint = null;

				update = moveTrackersIfNeeded(event);

				break;

		}

		if(update){

			if(mListener != null){
				mListener.onColorChanged(Color.HSVToColor(mAlpha, new float[]{mHue, mSat, mVal}));
			}

			invalidate();
			return true;
		}


		return super.onTouchEvent(event);
	}

	private boolean moveTrackersIfNeeded(MotionEvent event){

		if(mStartTouchPoint == null) {
			return false;
		}

		boolean update = false;

		final int startX = mStartTouchPoint.x;
		final int startY = mStartTouchPoint.y;


		if(mHueRect.contains(startX, startY)){
			mLastTouchedPanel = PANEL_HUE;

			mHue = pointToHue(event.getY());

			update = true;
		}
		else if(mSatValRect.contains(startX, startY)){

			mLastTouchedPanel = PANEL_SAT_VAL;

			final float[] result = pointToSatVal(event.getX(), event.getY());

			mSat = result[0];
			mVal = result[1];

			update = true;
		}

		return update;
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int width = 0;
		int height = 0;

		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		int widthAllowed = MeasureSpec.getSize(widthMeasureSpec);
		int heightAllowed = MeasureSpec.getSize(heightMeasureSpec);


		widthAllowed = chooseWidth(widthMode, widthAllowed);
		heightAllowed = chooseHeight(heightMode, heightAllowed);

		height = (int) (widthAllowed - PANEL_SPACING - HUE_PANEL_WIDTH);

		//If calculated height (based on the width) is more than the allowed height.
		if(height > heightAllowed){
			height = heightAllowed;
			width = (int) (height + PANEL_SPACING + HUE_PANEL_WIDTH);
		}
		else{
			width = widthAllowed;
		}

		setMeasuredDimension(width, height);
	}

	private int chooseWidth(int mode, int size){
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
			return size;
		} else { // (mode == MeasureSpec.UNSPECIFIED)
			return getPrefferedWidth();
		}
	}

	private int chooseHeight(int mode, int size){
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
			return size;
		} else { // (mode == MeasureSpec.UNSPECIFIED)
			return getPrefferedHeight();
		}
	}

	private int getPrefferedWidth(){
		final int width = getPrefferedHeight();
		return (int) (width + HUE_PANEL_WIDTH + PANEL_SPACING);

	}

	private int getPrefferedHeight(){
		final int height = (int)(200 * mDensity);
		return height;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mDrawingRect = new RectF();
		mDrawingRect.left = mDrawingOffset + getPaddingLeft();
		mDrawingRect.right  = w - mDrawingOffset - getPaddingRight();
		mDrawingRect.top = mDrawingOffset + getPaddingTop();
		mDrawingRect.bottom = h - mDrawingOffset - getPaddingBottom();

		setUpSatValRect();
		setUpHueRect();
	}

	private void setUpSatValRect(){

		final RectF     dRect = mDrawingRect;
		final float panelSide = dRect.height() - BORDER_WIDTH_PX * 2;

		final float left = dRect.left + BORDER_WIDTH_PX;
		final float top = dRect.top + BORDER_WIDTH_PX;
		final float bottom = top + panelSide;
		final float right = left + panelSide;

		mSatValRect = new RectF(left,top, right, bottom);

		mSatValBackPaint.setShader(new LinearGradient(left, top, left, bottom, 0xffffffff, 0xff000000, TileMode.CLAMP));
	}

	private void setUpHueRect(){
		final RectF     dRect = mDrawingRect;

		final float left = dRect.right - HUE_PANEL_WIDTH + BORDER_WIDTH_PX;
		final float top = dRect.top + BORDER_WIDTH_PX;
		final float bottom = dRect.bottom - BORDER_WIDTH_PX;
		final float right = dRect.right - BORDER_WIDTH_PX;

		mHueRect = new RectF(left, top, right, bottom);
	}

	/**
	 * Set a OnColorChangedListener to get notified when the color
	 * selected by the user has changed.
	 * @param listener
	 */
	public void setOnColorChangedListener(OnColorChangedListener listener){
		mListener = listener;
	}

	/**
	 * Set the color of the border surrounding all panels.
	 * @param color
	 */
	public void setBorderColor(int color){
		mBorderColor = color;
		invalidate();
	}

	/**
	 * Get the color of the border surrounding all panels.
	 */
	public int getBorderColor(){
		return mBorderColor;
	}

	/**
	 * Get the current color this view is showing.
	 * @return the current color.
	 */
	public int getColor(){
		return Color.HSVToColor(mAlpha, new float[]{mHue,mSat,mVal});
	}

	/**
	 * Set the color the view should show.
	 * @param color The color that should be selected.
	 */
	public void setColor(int color){
		setColor(color, false);
	}

	/**
	 * Set the color this view should show.
	 * @param color The color that should be selected.
	 * @param callback If you want to get a callback to
	 * your OnColorChangedListener.
	 */
	public void setColor(int color, boolean callback){
		final int red = Color.red(color);
		final int blue = Color.blue(color);
		final int green = Color.green(color);

		final float[] hsv = new float[3];

		Color.RGBToHSV(red, green, blue, hsv);

		mHue = hsv[0];
		mSat = hsv[1];
		mVal = hsv[2];

		if(callback && mListener != null){
			mListener.onColorChanged(Color.HSVToColor(mAlpha, new float[]{mHue, mSat, mVal}));
		}

		invalidate();
	}

	/**
	 * Get the drawing offset of the color picker view.
	 * The drawing offset is the distance from the side of
	 * a panel to the side of the view minus the padding.
	 * Useful if you want to have your own panel below showing
	 * the currently selected color and want to align it perfectly.
	 * @return The offset in pixels.
	 */
	public float getDrawingOffset(){
		return mDrawingOffset;
	}

	public void setSliderTrackerColor(int color){
		mSliderTrackerColor = color;

		mHueTrackerPaint.setColor(mSliderTrackerColor);

		invalidate();
	}

	public int getSliderTrackerColor(){
		return mSliderTrackerColor;
	}
}
