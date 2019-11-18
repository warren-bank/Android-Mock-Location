package com.github.warren_bank.mock_location.ui;

import android.app.Activity;

public abstract class RuntimePermissionsActivity extends Activity {
    public abstract void onPermissionsGranted();
    public abstract void onPermissionsDenied(String[] permissions);
}
