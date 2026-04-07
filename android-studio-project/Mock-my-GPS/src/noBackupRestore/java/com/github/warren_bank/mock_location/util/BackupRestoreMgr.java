package com.github.warren_bank.mock_location.util;

import android.app.Activity;
import android.content.Intent;

public class BackupRestoreMgr extends AbstractBackupRestoreMgr {

  protected BackupListener  getBackupListener() {return null;}
  protected RestoreListener getRestoreListener(){return null;}

  protected void performBackup( Activity activity, int requestCode){}
  protected void performRestore(Activity activity, int requestCode){}

  protected void onActivityResult(Intent intent, CallbackData cData){}
}
