package com.painless.glclock.setting;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.painless.glclock.ColorUtils;
import com.painless.glclock.Constants;
import com.painless.glclock.Debug;
import com.painless.glclock.R;

public final class ImagePicker extends Activity implements AdapterView.OnItemSelectedListener {

  private static final int IMAGE_REQUEST = 22;
  private static final int COLOR_REQUEST = 25;

  private String[] backValues;
  private Spinner backSpinner;
  private ArrayAdapter<String> spinnerAdapter;

  private View background;

  private BitmapDrawable currentBackground;
  private BitmapDrawable customBackground;
  private BitmapDrawable abstractDrawable;

  private int currentShade;
  private boolean shadeEqualsTheme;
  private Bitmap abstractBitmap;

  private ImageButton shadePicker;

  private SharedPreferences pref;
  private String backCode = "";

  private String newBackUrl;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.image_picker);

    backValues = getResources().getStringArray(R.array.ip_backValues);
    background = findViewById(R.id.pickerLayout);

    final ArrayList<String> items = new ArrayList<String>();
    items.add(backValues[0]);
    items.add(backValues[1]);
    spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    backSpinner = (Spinner) findViewById(R.id.pickerList);
    backSpinner.setAdapter(spinnerAdapter);
    backSpinner.setOnItemSelectedListener(this);

    shadePicker = (ImageButton) findViewById(R.id.colorPick);
    shadePicker.setVisibility(View.GONE);

    // set up back code
    pref = getSharedPreferences(Constants.SHARED_PREFS_NAME, 0);
    backCode = pref.getString(Constants.BACK_IMG_CODE, "");
    abstractBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample_back);
    abstractDrawable = new BitmapDrawable(getResources(), abstractBitmap);

    final int color = pref.getInt(Constants.THEME_CODE, Constants.DEFAULT_THEME);
    shadeEqualsTheme = !pref.contains(Constants.BACK_CCODE);
    currentShade = pref.getInt(Constants.BACK_CCODE, color);

    currentBackground = getDrawable(backCode);
    resetAbstractBack();
    if (currentBackground != null) {
      spinnerAdapter.add(backValues[2]);
      backSpinner.setSelection(2);
      setBackground(currentBackground);
    } else if (backCode.equals(Constants.BACK_NONE)) {
      background.setBackgroundColor(Color.BLACK);
    } else {
      shadePicker.setVisibility(View.VISIBLE);
      backSpinner.setSelection(1);
    }

    newBackUrl = backCode.equals(Constants.BACK1_URL) ? Constants.BACK2_URL : Constants.BACK1_URL;

    ((Button) findViewById(R.id.browse)).setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        selectBack();
      }
    });

    ((Button) findViewById(R.id.setBack)).setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        savePref();
      }
    });

    shadePicker.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        selectShade();
      }
    });
  }

  private void resetAbstractBack() {
    final Bitmap shadedAbstractBitmap = ColorUtils.getPaintedBitmap(
        abstractBitmap,
        ColorUtils.getPaintFromColor(currentShade),
        false);

    abstractDrawable = new BitmapDrawable(getResources(), shadedAbstractBitmap);
    setBackground(abstractDrawable);
  }

  private void savePref() {
    final SharedPreferences.Editor editor = pref.edit();

    switch (backSpinner.getSelectedItemPosition()) {
      case 0:
        editor.putString(Constants.BACK_IMG_CODE, Constants.BACK_NONE);
        break;
      case 1:
        editor.remove(Constants.BACK_IMG_CODE);
        if (shadeEqualsTheme) {
          editor.remove(Constants.BACK_CCODE);
        } else {
          editor.putInt(Constants.BACK_CCODE, currentShade);
        }
        break;
      case 2:
        editor.putString(Constants.BACK_IMG_CODE,
            currentBackground == null ? newBackUrl : backCode);
        break;
      case 3:
        editor.putString(Constants.BACK_IMG_CODE, newBackUrl);
        break;
    }
    editor.commit();
    finish();
  }

  private BitmapDrawable getDrawable(String url) {
    if (!url.equals(Constants.BACK1_URL) && !url.equals(Constants.BACK2_URL)) {
      return null;
    }
    try {
      final InputStream in = openFileInput(url);
      final Bitmap back = BitmapFactory.decodeStream(in);
      in.close();
      return new BitmapDrawable(getResources(), back);
    } catch (final Exception e) {
      return null;
    }
  }

  @Override
  public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
    shadePicker.setVisibility(View.GONE);
    switch (position) {
      case 0:
        background.setBackgroundColor(Color.BLACK);
        break;
      case 1:
        setBackground(abstractDrawable);
        shadePicker.setVisibility(View.VISIBLE);
        break;
      case 2:
        setBackground(currentBackground == null ? customBackground : currentBackground);
        break;
      case 3:
        setBackground(customBackground);
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> arg0) {
    background.setBackgroundColor(Color.BLACK);
  }

  private void selectShade() {
    final Intent shadeIntent = new Intent(this, ColorPicker.class);
    shadeIntent.putExtra("title", getResources().getString(R.string.ip_backShade));
    shadeIntent.putExtra("color", currentShade);
    shadeIntent.putExtra("default", shadeEqualsTheme);
    startActivityForResult(shadeIntent, COLOR_REQUEST);
  }

  private void selectBack() {
    final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
    photoPickerIntent.setType("image/*");
    startActivityForResult(photoPickerIntent, IMAGE_REQUEST);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode != RESULT_OK) {
      return;
    }
    if (requestCode == IMAGE_REQUEST) {
      new LoadImageTask().execute(data.getData());
    } else if (requestCode == COLOR_REQUEST) {
      currentShade = data.getExtras().getInt("color");
      shadeEqualsTheme = data.getExtras().getBoolean("default");
      resetAbstractBack();
    }
  }

  private void setBackground(Drawable d) {
    background.setBackgroundDrawable(d);
  }

  /**
   * Asynchronous task which loads image, resizes it and saves it in the private url.
   */
  private class LoadImageTask extends AsyncTask<Uri, Void, Bitmap> {

    private final ProgressDialog dialog;

    private LoadImageTask() {
      dialog = ProgressDialog.show(ImagePicker.this, "Loading Image", "Working, Please wait...",
          true, false);
    }

    @Override
    protected Bitmap doInBackground(Uri... uris) {
      try {
        final Uri contentUri = uris[0];
        InputStream imageStream = getContentResolver().openInputStream(contentUri);
        final Bitmap original = BitmapFactory.decodeStream(imageStream);
        final Bitmap resized = Bitmap.createScaledBitmap(original, 512, 1024, false);

        // Save the bitmap;
        final OutputStream out = openFileOutput(newBackUrl, MODE_PRIVATE);
        resized.compress(CompressFormat.PNG, 90, out);
        out.close();

        return resized;
      } catch (final Exception e) {
        Debug.log(e);
        return null;
      }
    }

    @Override
    protected void onPostExecute(Bitmap back) {
      if (back == null) {
        Toast.makeText(ImagePicker.this, "Unable to load image", Toast.LENGTH_SHORT).show();
      } else {
        if (customBackground == null) {
          spinnerAdapter.add(backValues[3]);
        }
        customBackground = new BitmapDrawable(getResources(), back);
        backSpinner.setSelection(currentBackground == null ? 2 : 3);
        shadePicker.setVisibility(View.GONE);
        setBackground(customBackground);
      }
      dialog.dismiss();
    }
  }
}
