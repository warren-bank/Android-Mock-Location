package com.github.warren_bank.mock_location.util;

import com.github.warren_bank.mock_location.R;

import lib.folderpicker.FolderPicker;

import android.app.Activity;
import android.content.Intent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class BackupRestoreMgr extends AbstractBackupRestoreMgr {

  protected class MyBackupListener extends BackupListener {
    @Override
    public void onFilePath(Activity activity, ResultListener rListener, String filepath) {
      // save data to filepath
      try {
        byte[] data = getBackupData(activity);

        if ((data == null) || (data.length == 0))
          throw new Exception();

        FileOutputStream out = new FileOutputStream(filepath);

        out.write(data);
        out.flush();
        out.close();

        rListener.onResult(true);
      }
      catch(Exception e) {
        rListener.onResult(false);
      }
    }
  }

  protected class MyRestoreListener extends RestoreListener {
    @Override
    public void onFilePath(Activity activity, ResultListener rListener, String filepath) {
      // read data from filepath
      try {
        File   file = new File(filepath);
        byte[] data = new byte[(int) file.length()];
        FileInputStream in = new FileInputStream(file);

        in.read(data);
        in.close();

        rListener.onResult(
          setRestoreData(activity, data)
        );
      }
      catch(Exception e) {
        rListener.onResult(false);
      }
    }
  }

  protected BackupListener  getBackupListener() {return new MyBackupListener();}
  protected RestoreListener getRestoreListener(){return new MyRestoreListener();}

  protected void performBackup(Activity activity, int requestCode) {
    FolderPicker
      .withBuilder()
      .withActivity(activity)
      .withRequestCode(requestCode)
      .withTitle(activity.getString(R.string.app_folderpicker_json_export_filepath_title))
      .withDescription(activity.getString(R.string.app_folderpicker_json_export_filepath_description))
      .withNewFilePrompt(activity.getString(R.string.app_folderpicker_json_export_filepath_newfile_prompt))
      .withNewFileName(getDefaultFilename(activity))
      .withFileFilter("^.*\\.(?:json|txt)$")
      .start();
  }

  protected void performRestore(Activity activity, int requestCode) {
    FolderPicker
      .withBuilder()
      .withActivity(activity)
      .withRequestCode(requestCode)
      .withTitle(activity.getString(R.string.app_folderpicker_json_import_filepath_title))
      .withDescription(activity.getString(R.string.app_folderpicker_json_import_filepath_description))
      .withFilePicker(true)
      .withFileFilter("^.*\\.(?:json|txt)$")
      .withHomeButton(true)
      .start();
  }

  protected void onActivityResult(Intent intent, CallbackData cData) {
    try {
      if (!intent.hasExtra(FolderPicker.EXTRA_DATA))
        throw new Exception();

      String filepath = intent.getExtras().getString(FolderPicker.EXTRA_DATA);

      if ((filepath == null) || filepath.isEmpty())
        throw new Exception();

      cData.fListener.onFilePath(cData.activity, cData.rListener, filepath);
    }
    catch(Exception e) {
      cData.rListener.onResult(false);
    }
  }
}
