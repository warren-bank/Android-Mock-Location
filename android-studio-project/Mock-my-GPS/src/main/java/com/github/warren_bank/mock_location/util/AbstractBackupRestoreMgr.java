package com.github.warren_bank.mock_location.util;

import com.github.warren_bank.mock_location.R;
import com.github.warren_bank.mock_location.data_model.BookmarkItem;
import com.github.warren_bank.mock_location.data_model.SharedPrefs;

import android.app.Activity;
import android.content.Intent;

import java.util.ArrayList;

public abstract class AbstractBackupRestoreMgr {

  public interface ResultListener {
    public void onResult(boolean success);
  }

  protected interface FilePathListener {
    public void onFilePath(Activity activity, ResultListener rListener, String filepath);
  }

  protected class BackupListener implements FilePathListener {
    public void onFilePath(Activity activity, ResultListener rListener, String filepath) {}
  }

  protected class RestoreListener implements FilePathListener {
    public void onFilePath(Activity activity, ResultListener rListener, String filepath) {}
  }

  protected class CallbackData {
    public Activity         activity;
    public FilePathListener fListener;
    public ResultListener   rListener;

    public CallbackData(Activity activity, FilePathListener fListener, ResultListener rListener) {
      this.activity  = activity;
      this.fListener = fListener;
      this.rListener = rListener;
    }
  }

  private static int nonce = 0;

  private static ArrayList<CallbackData> allCallbackData = new ArrayList<CallbackData>();

  private void setCallbackData(int requestCode, CallbackData cData) {
    allCallbackData.add(requestCode, cData);
  }

  private CallbackData getCallbackData(int requestCode, boolean remove) {
    CallbackData cData = allCallbackData.get(requestCode);

    if (remove)
      setCallbackData(requestCode, null);

    return cData;
  }

  protected abstract BackupListener  getBackupListener();
  protected abstract RestoreListener getRestoreListener();

  protected abstract void performBackup( Activity activity, int requestCode);
  protected abstract void performRestore(Activity activity, int requestCode);

  protected abstract void onActivityResult(Intent intent, CallbackData cData);

  public void doBackup(Activity activity, ResultListener rListener) {
    try {
      int requestCode = nonce++;
      FilePathListener fListener = (FilePathListener) getBackupListener();
      CallbackData cData = new CallbackData(activity, fListener, rListener);
      setCallbackData(requestCode, cData);

      performBackup(activity, requestCode);
    }
    catch(Exception e) {}
  }

  public void doRestore(Activity activity, ResultListener rListener) {
    try {
      int requestCode = nonce++;
      FilePathListener fListener = (FilePathListener) getRestoreListener();
      CallbackData cData = new CallbackData(activity, fListener, rListener);
      setCallbackData(requestCode, cData);

      performRestore(activity, requestCode);
    }
    catch(Exception e) {}
  }

  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    try {
      CallbackData cData = getCallbackData(requestCode, /* remove */ true);

      if (cData != null) {
        if ((resultCode == Activity.RESULT_OK) && (intent != null)) {
          onActivityResult(intent, cData);
        }
        else {
          cData.rListener.onResult(false);
        }
      }
    }
    catch(Exception e) {}
  }

  // ---------------------------------------------------------------------------
  // app-specific custom logic:
  // ---------------------------------------------------------------------------

  protected byte[] getBackupData(Activity activity) {
    try {
      ArrayList<BookmarkItem> arrayList = SharedPrefs.getBookmarkItems(activity);
      String json = BookmarkItem.toJson(arrayList);

      return json.getBytes("UTF-8");
    }
    catch(Exception e) {
      return null;
    }
  }

  protected boolean setRestoreData(Activity activity, byte[] data) {
    try {
      String json = new String(data, "UTF-8");
      ArrayList<BookmarkItem> arrayList = BookmarkItem.fromJson(json);

      return SharedPrefs.putBookmarkItems(activity, arrayList);
    }
    catch(Exception e) {
      return false;
    }
  }

  protected String getDefaultFilename(Activity activity) {
    return (activity.getString(R.string.app_name).replace(" ", "-") + ".json");
  }
}
