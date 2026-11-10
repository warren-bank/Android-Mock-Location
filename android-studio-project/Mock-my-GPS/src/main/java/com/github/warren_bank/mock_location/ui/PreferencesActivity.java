package com.github.warren_bank.mock_location.ui;

import com.github.warren_bank.mock_location.R;
import com.github.warren_bank.mock_location.data_model.SharedPrefs;
import com.github.warren_bank.mock_location.data_model.SharedPrefsState;
import com.github.warren_bank.mock_location.service.LocationService;
import com.github.warren_bank.mock_location.util.LocaleUtils;
import com.github.warren_bank.mock_location.util.ThemeUtils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class PreferencesActivity extends Activity {
    private SharedPrefsState originalState;
    private int              originalThemeMode;
    private int              appliedThemeResId;

    // index 0 = "" = follow the system language
    private ArrayList<String> languageTags;
    private int originalLanguageIndex;

    private TextView input_time_interval;
    private CheckBox input_share_target_enabled;
    private TextView input_fixed_count;
    private CheckBox input_fixed_joystick_enabled;
    private TextView input_fixed_joystick_increment;
    private CheckBox input_trip_hold_destination;
    private RadioGroup input_theme_mode;
    private Spinner  input_language;
    private Button   button_cancel;
    private Button   button_save;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ThemeUtils.applyUiModeOverride(PreferencesActivity.this, base);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appliedThemeResId = ThemeUtils.applyTheme(PreferencesActivity.this);
        super.onCreate(savedInstanceState);
        LocaleUtils.apply(this);
        setContentView(R.layout.activity_preferences);

        originalState     = new SharedPrefsState(PreferencesActivity.this, true);
        originalThemeMode = SharedPrefs.getThemeMode(PreferencesActivity.this);

        input_time_interval            = (TextView) findViewById(R.id.input_time_interval);
        input_share_target_enabled     = (CheckBox) findViewById(R.id.input_share_target_enabled);
        input_fixed_count              = (TextView) findViewById(R.id.input_fixed_count);
        input_fixed_joystick_enabled   = (CheckBox) findViewById(R.id.input_fixed_joystick_enabled);
        input_fixed_joystick_increment = (TextView) findViewById(R.id.input_fixed_joystick_increment);
        input_trip_hold_destination    = (CheckBox) findViewById(R.id.input_trip_hold_destination);
        input_theme_mode               = (RadioGroup) findViewById(R.id.input_theme_mode);
        input_language                 = (Spinner)  findViewById(R.id.input_language);
        button_cancel                  = (Button)   findViewById(R.id.button_cancel);
        button_save                    = (Button)   findViewById(R.id.button_save);

        initLanguage();
        reset();

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferencesActivity.this.finish();
            }
        });

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int time_interval;
                int fixed_count;
                boolean fixed_joystick_enabled;
                double fixed_joystick_increment;
                boolean trip_hold_destination;

                String text = null;

                try {
                    text = input_time_interval.getText().toString();
                    time_interval = Integer.parseInt(text, 10);
                }
                catch(Exception e) {
                    showError(getString(R.string.error_number_format, text));
                    return;
                }
                try {
                    text = input_fixed_count.getText().toString();
                    fixed_count = Integer.parseInt(text, 10);
                }
                catch(Exception e) {
                    showError(getString(R.string.error_number_format, text));
                    return;
                }
                fixed_joystick_enabled = input_fixed_joystick_enabled.isChecked();
                try {
                    text = input_fixed_joystick_increment.getText().toString();
                    fixed_joystick_increment = Double.parseDouble(text);
                }
                catch(Exception e) {
                    showError(getString(R.string.error_number_format, text));
                    return;
                }
                trip_hold_destination = input_trip_hold_destination.isChecked();

                // not part of "SharedPrefsState": stored as the enabled state of the share target itself
                boolean share_target_enabled = input_share_target_enabled.isChecked();
                if (share_target_enabled != isShareTargetEnabled()) {
                    setShareTargetEnabled(share_target_enabled);
                }

                // not part of "SharedPrefsState": the theme only matters to the UI, not to "LocationService"
                int theme_mode = getCheckedThemeMode();
                if (theme_mode != originalThemeMode) {
                    SharedPrefs.putThemeMode(PreferencesActivity.this, theme_mode);
                }

                SharedPrefsState modifiedState = new SharedPrefsState(
                    originalState.bookmarks,

                    // fields that could be modified
                    time_interval,
                    fixed_count,
                    fixed_joystick_enabled,
                    fixed_joystick_increment,
                    trip_hold_destination,

                    originalState.trip_origin_lat,
                    originalState.trip_origin_lon,
                    originalState.trip_destination_lat,
                    originalState.trip_destination_lon,
                    originalState.trip_duration
                );

                short diff_fields = originalState.diff(modifiedState);
                boolean is_equal  = (diff_fields == 0);

                if (!is_equal) {
                    SharedPreferences.Editor editor = SharedPrefs.getSharedPreferencesEditor(PreferencesActivity.this);
                    boolean flush = false;
                    short mask;

                    mask = (1 << 1);
                    if ((diff_fields & mask) == mask) {
                        SharedPrefs.putTimeInterval(editor, PreferencesActivity.this, time_interval, flush);
                    }

                    mask = (1 << 2);
                    if ((diff_fields & mask) == mask) {
                        SharedPrefs.putFixedCount(editor, PreferencesActivity.this, fixed_count, flush);
                    }

                    mask = (1 << 3);
                    if ((diff_fields & mask) == mask) {
                        SharedPrefs.putFixedJoystickEnabled(editor, PreferencesActivity.this, fixed_joystick_enabled, flush);
                    }

                    mask = (1 << 4);
                    if ((diff_fields & mask) == mask) {
                        SharedPrefs.putFixedJoystickIncrement(editor, PreferencesActivity.this, fixed_joystick_increment, flush);
                    }

                    mask = (1 << 5);
                    if ((diff_fields & mask) == mask) {
                        SharedPrefs.putTripHoldDestination(editor, PreferencesActivity.this, trip_hold_destination, flush);
                    }

                    editor.commit();

                    LocationService.doSharedPrefsChange(PreferencesActivity.this, true);
                }

                int languageIndex = input_language.getSelectedItemPosition();

                if ((languageIndex >= 0) && (languageIndex != originalLanguageIndex)) {
                    LocaleUtils.setSelectedLanguageTag(PreferencesActivity.this, languageTags.get(languageIndex));
                    restartMainActivity();
                    return;
                }

                PreferencesActivity.this.finish();
            }
        });
    }

    private void reset() {
        input_time_interval.setText(Integer.toString(originalState.time_interval, 10));
        input_share_target_enabled.setChecked(isShareTargetEnabled());
        input_fixed_count.setText(Integer.toString(originalState.fixed_count, 10));
        input_fixed_joystick_enabled.setChecked(originalState.fixed_joystick_enabled);
        input_fixed_joystick_increment.setText(Double.toString(originalState.fixed_joystick_increment));
        input_trip_hold_destination.setChecked(originalState.trip_hold_destination);
        setCheckedThemeMode(originalThemeMode);
        input_language.setSelection(originalLanguageIndex);
    }

    private int getCheckedThemeMode() {
        int id = input_theme_mode.getCheckedRadioButtonId();

        if (id == R.id.input_theme_mode_light) return ThemeUtils.MODE_LIGHT;
        if (id == R.id.input_theme_mode_dark)  return ThemeUtils.MODE_DARK;
        return ThemeUtils.MODE_SYSTEM;
    }

    private void setCheckedThemeMode(int theme_mode) {
        int id;
        switch (theme_mode) {
            case ThemeUtils.MODE_LIGHT:
                id = R.id.input_theme_mode_light;
                break;
            case ThemeUtils.MODE_DARK:
                id = R.id.input_theme_mode_dark;
                break;
            default:
                id = R.id.input_theme_mode_system;
                break;
        }
        input_theme_mode.check(id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ThemeUtils.restartIfThemeChanged(PreferencesActivity.this, appliedThemeResId);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // "locale" config changes reset the Resources to the system language below Android 13 (see LocaleUtils)
        LocaleUtils.apply(this);
        ThemeUtils.restartIfThemeChanged(PreferencesActivity.this, appliedThemeResId);
    }


    private void initLanguage() {
        ArrayList<String> supportedTags = LocaleUtils.getSupportedLanguageTags(PreferencesActivity.this);

        languageTags = new ArrayList<String>();
        languageTags.add("");
        languageTags.addAll(supportedTags);

        ArrayList<String> names = new ArrayList<String>();
        names.add(getString(R.string.PreferencesActivity_language_system_default));
        for (String tag : supportedTags) {
            names.add(LocaleUtils.getDisplayName(tag));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(PreferencesActivity.this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        input_language.setAdapter(adapter);

        // a language the list doesn't know shows as "System default", but is only replaced when the user picks another entry
        originalLanguageIndex = 0;
        String selectedTag = LocaleUtils.getSelectedLanguageTag(PreferencesActivity.this);
        for (int i = 1; i < languageTags.size(); i++) {
            if (languageTags.get(i).equalsIgnoreCase(selectedTag)) {
                originalLanguageIndex = i;
                break;
            }
        }

        // nothing to choose from: e.g. the "english" flavor
        if (supportedTags.size() <= 1) {
            findViewById(R.id.section_language).setVisibility(View.GONE);
        }
    }

    // every activity handles "locale" config changes itself, so redraw all tabs in the new language
    private void restartMainActivity() {
        Intent intent = new Intent(PreferencesActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        PreferencesActivity.this.finish();
    }

    // the share target is an <activity-alias> of "GeoIntentActivity", disabled in AndroidManifest.xml;
    // switching it on or off adds or removes the app from every text share sheet, and with it the only network use
    private ComponentName getShareTargetComponent() {
        return new ComponentName(PreferencesActivity.this, "com.github.warren_bank.mock_location.ui.ShareTargetActivity");
    }

    private boolean isShareTargetEnabled() {
        int state = getPackageManager().getComponentEnabledSetting(getShareTargetComponent());
        return (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
    }

    private void setShareTargetEnabled(boolean enabled) {
        int state = enabled
            ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        getPackageManager().setComponentEnabledSetting(getShareTargetComponent(), state, PackageManager.DONT_KILL_APP);
    }

    private void showError(String text) {
        Toast.makeText(PreferencesActivity.this, text, Toast.LENGTH_SHORT).show();
    }

}
