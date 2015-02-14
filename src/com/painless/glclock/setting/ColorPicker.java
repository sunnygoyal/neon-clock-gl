package com.painless.glclock.setting;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.painless.glclock.Constants;
import com.painless.glclock.Debug;
import com.painless.glclock.R;
import com.painless.glclock.setting.ColorPickerView.OnColorChangedListener;

public class ColorPicker extends Activity {
	private static final int UN_SELECTED = R.drawable.picker1;
	private static final int SELECTED = R.drawable.picker2;

	private final OnClickListener sameAsThemeClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (sameAsTheme.isChecked()) {
				customColorView.setImageResource(UN_SELECTED);
			} else {
				customColorView.setImageResource(SELECTED);
			}
		}
	};

	private final OnClickListener customColorClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			customColorView.setImageResource(SELECTED);
			sameAsTheme.setChecked(false);
		}
	};

	private final OnColorChangedListener colorChange = new OnColorChangedListener() {

		@Override
		public void onColorChanged(int color) {
			customColorClicked.onClick(null);
			customColorView.setBackgroundColor(color);
		}
	};

	private CheckBox sameAsTheme;
	private ImageView customColorView;
	private ColorPickerView colorPickerView;

	SharedPreferences prefs;
	private String key;

	private int themeColor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.color_picker);
		final Bundle bundle = getIntent().getExtras();
		setTitle(bundle.getString("title"));

		sameAsTheme = (CheckBox) findViewById(R.id.sameAsTheme);
		sameAsTheme.setOnClickListener(sameAsThemeClicked);

		customColorView = (ImageView) findViewById(R.id.customColorView);
		customColorView.setOnClickListener(customColorClicked);

		// Setting the type
		key = bundle.getString("key");
		Debug.log("Color picker key : " + key);
		prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, 0);

		themeColor = prefs.getInt(Constants.THEME_CODE, Constants.DEFAULT_THEME);
		int defaultColor = 0;
		boolean checked;
		if (Constants.THEME_CODE.equals(key)) {
			sameAsTheme.setText(R.string.cp_defaultTheme);
			defaultColor = themeColor;
			checked = defaultColor == Constants.DEFAULT_THEME;

		} else if (key != null) {
			defaultColor = prefs.getInt(key, themeColor);
			checked = !prefs.contains(key);

		} else {
			defaultColor = bundle.getInt("color");
			checked = bundle.getBoolean("default");
		}

		if (checked) {
			sameAsTheme.setChecked(true);
		} else {
			customColorClicked.onClick(null);
		}


		customColorView.setBackgroundColor(defaultColor);

		((Button) findViewById(R.id.cancel)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		((Button) findViewById(R.id.done)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (saveSettings()) {
					finish();
				} else {
					Toast.makeText(getApplicationContext(), R.string.cp_error, Toast.LENGTH_LONG).show();
				}
			}
		});

		colorPickerView = (ColorPickerView) findViewById(R.id.color_picker);
		colorPickerView.setColor(defaultColor);
		colorPickerView.setOnColorChangedListener(colorChange);
	}

	private boolean saveSettings() {
		if (Constants.THEME_CODE.equals(key)) {
			final int value = sameAsTheme.isChecked() ? Constants.DEFAULT_THEME : getColor();
			return prefs.edit().putInt(key, value).commit();
		} else if (key != null) {
			if (sameAsTheme.isChecked()) {
				return prefs.edit().remove(key).commit();
			} else {
				return prefs.edit().putInt(key, getColor()).commit();
			}
		} else {
			final Intent data = new Intent();
			data.putExtra("color", sameAsTheme.isChecked() ? themeColor : getColor());
			data.putExtra("default", sameAsTheme.isChecked());
			setResult(RESULT_OK, data);
		}
		return true;
	}

	private int getColor() {
		return colorPickerView.getColor();
	}
}
