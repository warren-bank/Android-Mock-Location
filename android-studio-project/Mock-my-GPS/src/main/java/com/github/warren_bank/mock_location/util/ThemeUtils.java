package com.github.warren_bank.mock_location.util;

import com.github.warren_bank.mock_location.R;
import com.github.warren_bank.mock_location.data_model.SharedPrefs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;

/*
 * Light/dark theme selection without AndroidX:
 * - the user picks MODE_SYSTEM, MODE_LIGHT or MODE_DARK in "PreferencesActivity"
 * - each activity calls applyUiModeOverride() from attachBaseContext() (API 17+), so framework colors that
 *   depend on "-night" resources match the chosen theme even when it differs from the system setting
 * - each activity calls applyTheme() before super.onCreate()
 * - top-level activities call restartIfThemeChanged() from onResume() and onConfigurationChanged(),
 *   because they declare "uiMode" in "configChanges" and are not recreated by the system
 * - embedded activities (tabs of "MainActivity") only call applyTheme(); their parent restarts them
 */
public final class ThemeUtils {

    public static final int MODE_SYSTEM = 0;
    public static final int MODE_LIGHT  = 1;
    public static final int MODE_DARK   = 2;

    public static boolean isDark(Context context) {
        switch (SharedPrefs.getThemeMode(context)) {
            case MODE_LIGHT:
                return false;
            case MODE_DARK:
                return true;
            default:
                return isSystemDark(context);
        }
    }

    private static boolean isSystemDark(Context context) {
        // Configuration.uiMode was added in API 8
        if (Build.VERSION.SDK_INT < 8) return false;

        // the application context is never overridden by applyUiModeOverride(), so it reflects the system setting
        int nightMode = context.getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return (nightMode == Configuration.UI_MODE_NIGHT_YES);
    }

    /**
     * Call from Activity.attachBaseContext(), after super.attachBaseContext(base).
     * Activity.applyOverrideConfiguration() can only be called once per activity: any other override
     * (ex: locale) must be merged into the same Configuration.
     */
    public static void applyUiModeOverride(Activity activity, Context base) {
        if (Build.VERSION.SDK_INT < 17) return;

        boolean dark = isDark(base);  // must not touch the activity's own resources before the override

        Configuration config = new Configuration();
        config.fontScale = 0;  // new Configuration() defaults to 1.0, which would override the user's font size
        config.uiMode    = (base.getResources().getConfiguration().uiMode & ~Configuration.UI_MODE_NIGHT_MASK)
                         | (dark ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO);

        activity.applyOverrideConfiguration(config);
    }

    public static int getThemeResId(Context context) {
        return isDark(context) ? R.style.app_theme_dark : R.style.app_theme_light;
    }

    /**
     * @return the theme resource ID that was applied
     */
    public static int applyTheme(Activity activity) {
        int themeResId = getThemeResId(activity);
        activity.setTheme(themeResId);
        return themeResId;
    }

    /**
     * @return true when the activity is being restarted
     */
    public static boolean restartIfThemeChanged(Activity activity, int appliedThemeResId) {
        if (getThemeResId(activity) == appliedThemeResId) return false;

        restart(activity);
        return true;
    }

    /**
     * The action bar icons are black PNGs; in the dark theme they're tinted with the theme's primary text color.
     */
    public static void tintMenuIcons(Activity activity, Menu menu, int appliedThemeResId) {
        if (appliedThemeResId != R.style.app_theme_dark) return;

        TypedArray a = activity.getTheme().obtainStyledAttributes(new int[] {android.R.attr.textColorPrimary});
        int color = a.getColor(0, Color.WHITE);
        a.recycle();

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            Drawable icon = item.getIcon();
            if (icon == null) continue;

            icon = icon.mutate();
            icon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            item.setIcon(icon);
        }
    }

    public static void restart(Activity activity) {
        if (Build.VERSION.SDK_INT >= 11) {
            activity.recreate();
        }
        else {
            Intent intent = activity.getIntent();
            activity.finish();
            activity.startActivity(intent);
        }
    }

}
