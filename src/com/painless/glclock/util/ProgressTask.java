package com.painless.glclock.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;

/**
 * A simple cancelable async task that shows a progress dialog.
 */
public abstract class ProgressTask <T> extends AsyncTask<String, Void, T> implements OnCancelListener {

  private final ProgressDialog mProgress;

  public ProgressTask(Context context, int msgId) {
    mProgress = ProgressDialog.show(context, "",context.getText(msgId), true, true, this);
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    cancel(true);
  }

  @Override
  protected void onPostExecute(T result) {
    mProgress.dismiss();
  }
}
