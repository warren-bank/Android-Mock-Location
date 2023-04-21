package com.github.warren_bank.mock_location.service.looper;

import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationServices;

import android.content.Context;
import android.location.Location;

public class HmsMockLocationProviderManager {

    private static FusedLocationProviderClient client = null;
    private static int advanceTimeMillis              = 45000;

    protected static void startMockingLocation(Context context) {
        if (HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(context) != ConnectionResult.SUCCESS)
            return;

        stopMockingLocation();

        try {
            client = LocationServices.getFusedLocationProviderClient(context);
            client.setMockMode(true);
        }
        catch (Exception e) {
            stopMockingLocation();
        }
    }

    protected static void exec(double lat, double lon) {
        if (client != null) {
            try {
                Location mockLocation = MockLocationProvider.getLocation(lat, lon, advanceTimeMillis);
                client.setMockLocation(mockLocation);
            }
            catch (Exception e) {}
        }
    }

    protected static void stopMockingLocation() {
        if (client != null) {
            try {
                client.setMockMode(false);
            }
            catch(Exception e) {}
            client = null;
        }
    }
}
