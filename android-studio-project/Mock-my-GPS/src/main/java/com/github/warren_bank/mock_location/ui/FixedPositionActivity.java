package com.github.warren_bank.mock_location.ui;

import com.github.warren_bank.mock_location.R;
import com.github.warren_bank.mock_location.data_model.LocPoint;
import com.github.warren_bank.mock_location.data_model.SharedPrefs;
import com.github.warren_bank.mock_location.security_model.RuntimePermissions;
import com.github.warren_bank.mock_location.service.LocationService;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FixedPositionActivity extends RuntimePermissionsActivity {
    private LocPoint originalLoc;

    private TextView input_fixed_position;
    private Button   button_toggle_state;
    private Button   button_update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixed_position);

        originalLoc = SharedPrefs.getTripOrigin(FixedPositionActivity.this);

        input_fixed_position = (TextView) findViewById(R.id.input_fixed_position);
        button_toggle_state  = (Button)   findViewById(R.id.button_toggle_state);
        button_update        = (Button)   findViewById(R.id.button_update);

        reset();

        input_fixed_position.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (!LocationService.isStarted()) return;

                try {
                    String fixed_position = s.toString();
                    LocPoint modifiedLoc  = new LocPoint(fixed_position);

                    if (originalLoc.equals(modifiedLoc)) {
                        button_update.setVisibility(View.GONE);
                    }
                    else {
                        button_update.setVisibility(View.VISIBLE);
                    }
                }
                catch(Exception e) {}
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        button_toggle_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (LocationService.isStarted()) {
                        LocationService.doStop(FixedPositionActivity.this, true);
                        button_toggle_state.setText(R.string.label_button_start);
                        button_update.setVisibility(View.GONE);
                    }
                    else {
                        doStart(true);
                    }
                }
                catch(Exception e) {}
            }
        });

        button_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (LocationService.isStarted()) {
                        doStart(true);
                    }
                    else {
                        button_update.setVisibility(View.GONE);
                    }
                }
                catch(Exception e) {}
            }
        });
    }

    private void reset() {
        input_fixed_position.setText(originalLoc.toString());

        if (LocationService.isStarted())
            button_toggle_state.setText(R.string.label_button_stop);

        button_update.setVisibility(View.GONE);
    }

    /*
     * -----------------------------------------------------------------------------------------
     * notes:
     *  - the following methodology works, but with a caveat:
     *     * this app doesn't require any "dangerous" permissions
     *     * for this reason, the callback `onRequestPermissionsResult()` is not invoked
     *       after clicking the "Start" button for the 1st time
     *     * however, the callback `onRequestPermissionsResult()` is invoked
     *       after clicking the "Start" button for the 2nd time and thereafter
     *     * so, even though it does work.. it's bad UX
     * -----------------------------------------------------------------------------------------
     */
    /*
    private void doStart(boolean check_permissions) {
        boolean has_permissions = (!check_permissions || RuntimePermissions.isEnabled(FixedPositionActivity.this));

        if (has_permissions) {
            String fixed_position = input_fixed_position.getText().toString();
            LocPoint modifiedLoc  = new LocPoint(fixed_position);

            LocationService.doStart(FixedPositionActivity.this, true, modifiedLoc, null, 0);

            SharedPrefs.putTripOrigin(FixedPositionActivity.this, modifiedLoc);
            originalLoc = modifiedLoc;

            button_toggle_state.setText(R.string.label_button_stop);
            button_update.setVisibility(View.GONE);
        }
    }
    */

    /*
     * -----------------------------------------------------------------------------------------
     * notes:
     *  - the following methodology does not check the grant status of "dangerous" permissions
     *     * it hooks into the callback mechanism that would normally get called
     *       after having determined that all required "dangerous" permissions are granted
     *  - it only checks the permission: "android.permission.SYSTEM_ALERT_WINDOW"
     *     * `this.onPermissionsGranted()` is called:
     *       - immediately: if the permission is either granted or prefs indicate it's not needed
     *       - later: after prompting the user to grant the permission, and the user does so
     * -----------------------------------------------------------------------------------------
     */
    private void doStart(boolean check_permissions) {
        if (check_permissions) {
            RuntimePermissions.onPermissionsGranted(FixedPositionActivity.this);
        }
        else {
            String fixed_position = input_fixed_position.getText().toString();
            LocPoint modifiedLoc  = new LocPoint(fixed_position);

            LocationService.doStart(FixedPositionActivity.this, true, modifiedLoc, null, 0);

            SharedPrefs.putTripOrigin(FixedPositionActivity.this, modifiedLoc);
            originalLoc = modifiedLoc;

            button_toggle_state.setText(R.string.label_button_stop);
            button_update.setVisibility(View.GONE);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Runtime Permissions:
    // ---------------------------------------------------------------------------------------------

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        RuntimePermissions.onRequestPermissionsResult(FixedPositionActivity.this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        RuntimePermissions.onActivityResult(FixedPositionActivity.this, requestCode, resultCode, data);
    }

    @Override
    public void onPermissionsGranted() {
        doStart(false);
    }

    @Override
    public void onPermissionsDenied(String[] permissions) {
        String text = "The following list contains required permissions that are not yet granted:\n  " + TextUtils.join("\n  ", permissions);
        Toast.makeText(FixedPositionActivity.this, text, Toast.LENGTH_LONG).show();
    }
}
