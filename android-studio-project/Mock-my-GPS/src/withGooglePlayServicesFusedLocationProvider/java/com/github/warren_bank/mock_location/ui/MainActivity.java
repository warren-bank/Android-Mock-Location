package com.github.warren_bank.mock_location.ui;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import android.content.Intent;

public class MainActivity extends AospMainActivity {
    private static int GOOGLE_API_AVAILABILITY_REQUEST_CODE = 1;

    protected void requestPermissions() {
        super.requestPermissions();

        isGooglePlayServicesAvailable();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MainActivity.GOOGLE_API_AVAILABILITY_REQUEST_CODE)
          isGooglePlayServicesAvailable();
    }

    private void isGooglePlayServicesAvailable() {
        int connectionResult = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (connectionResult != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, connectionResult, MainActivity.GOOGLE_API_AVAILABILITY_REQUEST_CODE);
        }
    }
}
