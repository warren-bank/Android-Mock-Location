package com.github.warren_bank.mock_location.service.looper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import android.content.Context;
import android.location.Location;

public class GmsMockLocationProviderManager {

    private static FusedLocationProviderClient client = null;
    private static int advanceTimeMillis              = 45000;

    protected static void startMockingLocation(Context context) {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS)
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
