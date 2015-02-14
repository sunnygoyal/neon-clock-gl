package com.painless.glclock.setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.painless.glclock.Constants.WidgetCode;
import com.painless.glclock.R;
import com.painless.glclock.util.LayoutSlideAnim;

public class WidgetSettingPopup implements OnClickListener, OnCheckedChangeListener, OnSeekBarChangeListener {

	private final OffsetPicker mMain;
	private final SharedPreferences mPrefs;

	private DragImg mImg;
	private WidgetCode mCode;


	private View mContent;
	private TextView mTitleText;
	private SeekBar mZoomSeek;
	private TextView mZoomText;
	private CompoundButton mEnabled;
	
	private boolean mIsShowing = false;

	public WidgetSettingPopup(OffsetPicker main) {
		mMain = main;
		mPrefs = main.prefs;
	}

	public void show(DragImg img) {
		mImg = img;
		mCode = img.widgetCode;

		if (mContent == null) {
			initContent();
		}

		mEnabled.setOnCheckedChangeListener(null);
		mEnabled.setChecked(mPrefs.getBoolean(mCode.code, true));
		mEnabled.setOnCheckedChangeListener(this);

		mZoomSeek.setProgress((int) (mPrefs.getFloat(mCode.zoom, 1) * 10) - 5);

		mTitleText.setText(mImg.titleRes);
		mTitleText.setCompoundDrawablesWithIntrinsicBounds(mImg.iconRes, 0, 0, 0);
		
		mIsShowing = true;
		mContent.startAnimation(new LayoutSlideAnim(mContent, true, true));
	}

	public boolean isShowing() {
		return mIsShowing;
	}

	public void hide() {
		if (mIsShowing) {
			mIsShowing = false;
			mContent.startAnimation(new LayoutSlideAnim(mContent, false, true));	
		}
	}

	private void initContent() {
		mContent = mMain.findViewById(R.id.setting_content);

		mEnabled = (CompoundButton) mContent.findViewById(R.id.chk_enabled);

		mTitleText = (TextView) mContent.findViewById(R.id.txt_title_setting);
		mZoomText = (TextView) mContent.findViewById(R.id.txt_zoom);
		mZoomSeek = (SeekBar) mContent.findViewById(R.id.seek_zoom);
		mZoomSeek.setOnSeekBarChangeListener(this);

		mContent.findViewById(R.id.btn_color).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		final Intent shadeIntent = new Intent(mMain, ColorPicker.class);
		shadeIntent.putExtra("title", mMain.getString(mImg.titleRes) + " " + mMain.getString(R.string.wp_color_change));
		shadeIntent.putExtra("key", mCode.color);
		mMain.startActivity(shadeIntent);
	}

	@Override
	public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
		mPrefs.edit().putBoolean(mCode.code, isChecked).commit();
		mImg.updateAlpha();
		mMain.refreshView();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			mImg.setZoom((progress + 5f) / 10f);
			mMain.refreshView();
		}
		mZoomText.setText((progress*10 + 50) + "%");
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mPrefs.edit().putFloat(mCode.zoom, (seekBar.getProgress() + 5f) / 10f).commit();		
	}
	
}
