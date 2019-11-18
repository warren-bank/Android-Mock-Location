package com.github.warren_bank.mock_location.security_model;

import com.github.warren_bank.mock_location.R;
import com.github.warren_bank.mock_location.data_model.SharedPrefs;
import com.github.warren_bank.mock_location.ui.RuntimePermissionsActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import java.util.ArrayList;
import java.util.Arrays;

public final class RuntimePermissions {
    private static final int REQUEST_CODE_PERMISSIONS  = 0;
    private static final int REQUEST_CODE_DRAWOVERLAYS = 1;

    private static final ArrayList<String> MANDATORY_PERMISSIONS = new ArrayList<String>(
        Arrays.asList("android.permission.ACCESS_MOCK_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION")
    );

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

        return missingPermissions.toArray(new String[missingPermissions.size()]);
    }

    public static boolean isEnabled(Activity activity) {
        if (Build.VERSION.SDK_INT < 23)
            return true;

        final String[] missingPermissions = getMissingPermissions(activity);

        if (missingPermissions.length > 0) {
            activity.requestPermissions(missingPermissions, REQUEST_CODE_PERMISSIONS);
            return false;
        }
        else {
            if (!SharedPrefs.getFixedJoystickEnabled(activity) || canDrawOverlays(activity))
                return true;

            requestPermissionDrawOverlays(activity);
            return false;
        }
    }

    public static void onRequestPermissionsResult (RuntimePermissionsActivity activity, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != REQUEST_CODE_PERMISSIONS)
            return;

        if (grantResults.length == 0) {
            if (permissions.length == 0) {
                // no "dangerous" permissions are needed
                onPermissionsGranted(activity);
            }
            else {
                // request was cancelled. show the prompts again.
                if (isEnabled(activity))
                    activity.onPermissionsGranted();
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
                onPermissionsGranted(activity);
            }
            else {
                activity.onPermissionsDenied(
                    deniedPermissions.toArray(new String[deniedPermissions.size()])
                );
            }
        }
    }

    // =============================================================================================

    public static void onPermissionsGranted(RuntimePermissionsActivity activity) {
        if (!SharedPrefs.getFixedJoystickEnabled(activity) || canDrawOverlays(activity))
            activity.onPermissionsGranted();
        else
            requestPermissionDrawOverlays(activity);
    }

    // =============================================================================================

    public static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT < 23)
            return true;

        return Settings.canDrawOverlays(context);
    }

    public static void requestPermissionDrawOverlays(Activity activity) {
        Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
        activity.startActivityForResult(permissionIntent, REQUEST_CODE_DRAWOVERLAYS);
    }

    public static void onActivityResult(RuntimePermissionsActivity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_DRAWOVERLAYS)
            return;

        if (canDrawOverlays(activity))
            activity.onPermissionsGranted();
        else
            activity.onPermissionsDenied(new String[]{"android.permission.SYSTEM_ALERT_WINDOW"});
    }

    // =============================================================================================

    public static boolean hasMandatoryPermissions(Context context) {
        for (String permission : MANDATORY_PERMISSIONS) {
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

}
