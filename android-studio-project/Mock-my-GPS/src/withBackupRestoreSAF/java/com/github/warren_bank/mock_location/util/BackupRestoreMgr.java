package com.github.warren_bank.mock_location.util;

import com.github.warren_bank.mock_location.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class BackupRestoreMgr extends AbstractBackupRestoreMgr {

  protected class MyBackupListener extends BackupListener {
    @Override
    public void onFilePath(Activity activity, ResultListener rListener, String filepath) {
      // save data to content provider
      try {
        byte[] data = getBackupData(activity);

        if ((data == null) || (data.length == 0))
          throw new Exception();

        Uri uri = Uri.parse(filepath);
        OutputStream out = activity.getContentResolver().openOutputStream(uri);

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
      // read data from content provider
      try {
        Uri uri = Uri.parse(filepath);
        InputStream is = activity.getContentResolver().openInputStream(uri);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] bytes = new byte[1024];

        while ((nRead = is.read(bytes)) != -1) {
          buffer.write(bytes, 0, nRead);
        }
        buffer.flush();

        byte[] data = buffer.toByteArray();

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
    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("*/*");
    intent.putExtra(Intent.EXTRA_TITLE, getDefaultFilename(activity));

    activity.startActivityForResult(intent, requestCode);
  }

  protected void performRestore(Activity activity, int requestCode) {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    String[] mimeTypes = {"application/json", "text/javascript", "text/plain", "application/octet-stream"};

    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("*/*");
    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

    activity.startActivityForResult(intent, requestCode);
  }

  protected void onActivityResult(Intent intent, CallbackData cData) {
    try {
      Uri uri = intent.getData();

      if (uri == null)
        throw new Exception();

      String filepath = uri.toString();

      if ((filepath == null) || filepath.isEmpty() || !filepath.startsWith("content:"))
        throw new Exception();

      cData.fListener.onFilePath(cData.activity, cData.rListener, filepath);
    }
    catch(Exception e) {
      cData.rListener.onResult(false);
    }
  }
}
