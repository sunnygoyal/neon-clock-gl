package com.painless.glclock.setting;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import com.painless.glclock.Constants;
import com.painless.glclock.R;
import com.painless.glclock.spirit.BackSpirit;
import com.painless.glclock.util.LayoutSlideAnim;

public final class OffsetPicker extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

  protected SharedPreferences prefs;

  private Context mContext;

  protected Bitmap widgetBitmap;
  protected int themeColor;
  protected int width, height;
  protected int halfW, halfH;

  private DragPreview preview;
  private DragImg selectedImg;

  private final ArrayList<DragImg> widgets = new ArrayList<DragImg>();
  private final HashMap<String, DragImg> widgetColorMap = new HashMap<String, DragImg>();

  private WidgetSettingPopup settingsPopup;
  private TextView txtInfoTitle;
  private View lytInfo;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    setContentView(R.layout.widget_editor);

    mContext = getApplicationContext();

    prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, 0);
    selectedImg = null;

    final Display display = getWindowManager().getDefaultDisplay();
    width = display.getWidth(); halfW = width/2;
    height = display.getHeight(); halfH = height/2;

    txtInfoTitle = (TextView) findViewById(R.id.txt_title);
    lytInfo = findViewById(R.id.lyt_selected_widget);
    settingsPopup = new WidgetSettingPopup(this);
    preview = (DragPreview) findViewById(R.id.preview);

    loadScreen();
    preview.addAll(widgets);
  }

  /************ Code for Initializing everything ****************/
  private void loadScreen() {
    themeColor = prefs.getInt(Constants.THEME_CODE, Constants.DEFAULT_THEME);
    widgetBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.widget_all);

    // set background
    final BackSpirit backSpirit = new BackSpirit(mContext);

    final int backColor = prefs.getInt(Constants.BACK_CCODE, themeColor);
    final String backType = prefs.getString(Constants.BACK_IMG_CODE, "");
    backSpirit.setColor(backColor, backType);

    final Bitmap background = backSpirit.getActualBitmap(
        BitmapFactory.decodeResource(getResources(), R.drawable.sample_back));
    preview.setBack(background);

    widgets.add(new DragImg(this, 280, 280,   0,   0,	Constants.CLOCK, R.string.wp_clock, R.drawable.ic_clock));
    widgets.add(new DragImg(this, 128, 128, 280,   0, Constants.COMPASS, R.string.wp_compass, R.drawable.ic_compass));
    widgets.add(new DragImg(this, 128, 128, 408,   0, Constants.CPU, R.string.wp_cpu, R.drawable.ic_cpu));
    widgets.add(new DragImg(this, 128, 128, 280, 280, Constants.BATTERY, R.string.wp_battery, R.drawable.ic_battery));
    widgets.add(new DragImg(this, 128, 128, 408, 280, Constants.RAM, R.string.wp_ram, R.drawable.ic_ram));
    widgets.add(new DragImg(this, 258, 128, 280, 128,	Constants.CALENDAR, R.string.wp_calendar, R.drawable.ic_calendar));
    widgets.add(new DragImg(this, 256, 256,   0, 280,	Constants.WEATHER, R.string.wp_weather, R.drawable.ic_weather));

    for (DragImg img : widgets) {
      widgetColorMap.put(img.widgetCode.color, img);
    }

    prefs.registerOnSharedPreferenceChangeListener(this);
    Toast.makeText(getApplicationContext(), R.string.wp_info, Toast.LENGTH_LONG).show();
  }

  public void onResetClicked(View v) {
    final SharedPreferences.Editor editor = prefs.edit();
    for (final DragImg img : widgets) {
      img.resetPref(editor);
    }
    editor.commit();
    final Intent intent = getIntent();
    finish();
    startActivity(intent);
  }

  public void onFinishClicked(View v) {
    finish();
  }

  public void onWidgetSettingsClicked(View v) {
    settingsPopup.show(selectedImg);
  }

  /************ Code for Moving Widget ****************/
  private float startX, startY;
  private DragImg movingWidget = null;

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      if (movingWidget == null) {
        for (final DragImg dImg : widgets) {
          if (dImg.isIn((int) event.getRawX(), (int) event.getRawY())) {
            movingWidget = dImg;
            startX = event.getRawX();
            startY = event.getRawY();
            if (selectedImg != dImg) {
              updateSelectedWidgetInfo(dImg);
              selectedImg = dImg;
            }
            break;
          }
        }
        if (movingWidget == null) {
          if (selectedImg != null) {
            updateSelectedWidgetInfo(null);
            selectedImg = null;						
          }
        }
      }
    }
    final int dX = (int) (event.getRawX() - startX);
    final int dY = (int) (event.getRawY() - startY);
    if (event.getAction() == MotionEvent.ACTION_UP) {
      if (movingWidget != null) {
        movingWidget.finishMove(dX, dY);
        movingWidget.saveSettings();
        movingWidget = null;
      }
    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
      if (movingWidget != null) {
        movingWidget.showDrag(dX, dY);
      }
    }
    settingsPopup.hide();
    preview.setSelected(selectedImg);
    refreshView();
    return false;
  }

  @Override
  public void onBackPressed() {
    if (settingsPopup.isShowing()) {
      settingsPopup.hide();
    } else {
      super.onBackPressed();
    }
  }

  private void updateSelectedWidgetInfo(DragImg selectedImg) {
    Animation anim = new LayoutSlideAnim(lytInfo, selectedImg != null, false);

    if (selectedImg != null) {
      txtInfoTitle.setText(selectedImg.titleRes);
    }
    lytInfo.startAnimation(anim);
  }

  /************ Code for Widget color change ****************/
  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
      String key) {
    if (widgetColorMap.containsKey(key)) {
      widgetColorMap.get(key).updateColor();
      refreshView();
    }
  }

  public void refreshView() {
    preview.invalidate();
  }
}
