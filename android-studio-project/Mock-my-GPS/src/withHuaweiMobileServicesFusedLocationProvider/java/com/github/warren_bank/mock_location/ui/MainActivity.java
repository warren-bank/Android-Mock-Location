package com.github.warren_bank.mock_location.ui;

import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiAvailability;

import android.content.Intent;

public class MainActivity extends AospMainActivity {
    private static int HUAWEI_API_AVAILABILITY_REQUEST_CODE = 1;

    protected void requestPermissions() {
        super.requestPermissions();

        isHuaweiMobileServicesAvailable();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MainActivity.HUAWEI_API_AVAILABILITY_REQUEST_CODE)
          isHuaweiMobileServicesAvailable();
    }

    private void isHuaweiMobileServicesAvailable() {
        int connectionResult = HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(MainActivity.this);

        if (connectionResult != ConnectionResult.SUCCESS) {
            HuaweiApiAvailability.getInstance().getErrorDialog(MainActivity.this, connectionResult, MainActivity.HUAWEI_API_AVAILABILITY_REQUEST_CODE);
        }
    }
}
