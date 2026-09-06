package com.github.warren_bank.mock_location.security_model;

import com.github.warren_bank.mock_location.R;
import com.github.warren_bank.mock_location.data_model.SharedPrefs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.Manifest;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import java.util.ArrayList;
import java.util.Arrays;

public final class RuntimePermissions {
    private static final int REQUEST_CODE_PERMISSIONS                = 999;
    private static final int REQUEST_CODE_DRAW_OVERLAYS              = 998;
    private static final int REQUEST_CODE_ACCESS_BACKGROUND_LOCATION = 997;

    private static final ArrayList<String> MANDATORY_PERMISSIONS = new ArrayList<String>(
        Arrays.asList(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    );

    // =============================================================================================
    // public API
    // =============================================================================================

    public interface RuntimePermissionsListener {
      public void onPermissionsGranted();
      public void onPermissionsDenied(String[] permissions);
    }

    public static void requestPermissions(Activity activity, RuntimePermissionsListener listener) {
        if (Build.VERSION.SDK_INT >= 23) {
          String[] missingPermissions = getMissingPermissions(activity);

          if (missingPermissions.length > 0) {
              activity.requestPermissions(missingPermissions, REQUEST_CODE_PERMISSIONS);
              return;
          }
        }

        // no permissions to request
        onPermissionsGranted(activity, listener);
    }

    public static boolean canAccessBackgroundLocation(Context context) {
        if (Build.VERSION.SDK_INT < 29)
            return true;

        String permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
        return (context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
    }

    public static void requestPermissionAccessBackgroundLocation(Activity activity, RuntimePermissionsListener listener) {
        if (Build.VERSION.SDK_INT >= 29) {
          String[] missingPermissions = new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION};

          activity.requestPermissions(missingPermissions, REQUEST_CODE_ACCESS_BACKGROUND_LOCATION);
          return;
        }

        // no permissions to request
        onPermissionsGranted(activity, listener);
    }

    public static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT < 23)
            return true;

        return Settings.canDrawOverlays(context);
    }

    public static void requestPermissionDrawOverlays(Activity activity) {
        Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
        activity.startActivityForResult(permissionIntent, REQUEST_CODE_DRAW_OVERLAYS);
    }

    // not used
    public static boolean hasMandatoryPermissions(Context context) {
        for (String permission : MANDATORY_PERMISSIONS) {
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    // =============================================================================================
    // public API: delegated handlers for lifecycle events, which call methods in interface
    // =============================================================================================

    public static void onRequestPermissionsResult (Activity activity, RuntimePermissionsListener listener, int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {

            case REQUEST_CODE_PERMISSIONS : {
                if (grantResults.length == 0) {
                    if (permissions.length == 0) {
                        // no "dangerous" permissions are needed
                        onPermissionsGranted(activity, listener);
                    }
                    else {
                        // request was cancelled. show the prompts again.
                        requestPermissions(activity, listener);
                    }
                }
                else {
                    ArrayList<String> deniedPermissions = new ArrayList<>();

                    for (int i=0; i < grantResults.length; i++) {
                        if (
                            (grantResults[i] != PackageManager.PERMISSION_GRANTED) &&
                            MANDATORY_PERMISSIONS.contains(permissions[i])
                        ) {
                            // a mandatory permission is not granted
                            deniedPermissions.add(permissions[i]);
                        }
                    }

                    if (deniedPermissions.isEmpty()) {
                        onPermissionsGranted(activity, listener);
                    }
                    else {
                        listener.onPermissionsDenied(
                            deniedPermissions.toArray(new String[deniedPermissions.size()])
                        );
                    }
                }
            }
            break;

            case REQUEST_CODE_ACCESS_BACKGROUND_LOCATION : {
                if (canAccessBackgroundLocation(activity))
                    listener.onPermissionsGranted();
                else
                    listener.onPermissionsDenied(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION});
            }
            break;
        }
    }

    public static void onActivityResult(Activity activity, RuntimePermissionsListener listener, int requestCode, int resultCode, Intent data) {
        switch(requestCode) {

            case REQUEST_CODE_DRAW_OVERLAYS : {
                if (canDrawOverlays(activity))
                    listener.onPermissionsGranted();
                else
                    listener.onPermissionsDenied(new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW});
            }
            break;
        }
    }

    // =============================================================================================
    // private implementation
    // =============================================================================================

    public static String[] getMissingPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT < 23)
            return new String[0];

        PackageInfo info;
        try {
            info = activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
        }
        catch (PackageManager.NameNotFoundException e) {
            return new String[0];
        }

        if (info.requestedPermissions == null) {
            return new String[0];
        }

        ArrayList<String> missingPermissions = new ArrayList<>();
        for (int i = 0; i < info.requestedPermissions.length; i++) {
            if ((info.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0) {
                missingPermissions.add(info.requestedPermissions[i]);
            }
        }

        // permissions that must always be requested individually:
        missingPermissions.remove(Manifest.permission.ACCESS_BACKGROUND_LOCATION);

        return missingPermissions.toArray(new String[missingPermissions.size()]);
    }

    // =============================================================================================
    // private implementation: call methods in interface
    // =============================================================================================

    public static void onPermissionsGranted(Activity activity, RuntimePermissionsListener listener) {
        if (!SharedPrefs.getFixedJoystickEnabled(activity) || canDrawOverlays(activity))
            listener.onPermissionsGranted();
        else
            requestPermissionDrawOverlays(activity);
    }
}
