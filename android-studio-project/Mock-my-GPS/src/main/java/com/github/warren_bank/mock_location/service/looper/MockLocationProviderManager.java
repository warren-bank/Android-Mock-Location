package com.github.warren_bank.mock_location.service.looper;

// copied from:
//   https://github.com/mcastillof/FakeTraveler/blob/v1.6/app/src/main/java/cl/coders/faketraveler/MainActivity.java

import android.content.Context;
import android.location.LocationManager;

public class MockLocationProviderManager {

    private static MockLocationProvider mockNetwork = null;
    private static MockLocationProvider mockGps     = null;

    /**
     * Initialize instances of 'MockLocationProvider'.
     */
    protected static void startMockingLocation(Context context) {
        startMockingLocationNetwork(context);
        startMockingLocationGps(context);
    }

    private static void startMockingLocationNetwork(Context context) {
        stopMockingLocationNetwork();

        try {
            mockNetwork = new MockLocationProvider(LocationManager.NETWORK_PROVIDER, context);
        }
        catch (SecurityException e) {
            stopMockingLocationNetwork();
        }
    }

    protected static void startMockingLocationGps(Context context) {
        stopMockingLocationGps();

        try {
            mockGps = new MockLocationProvider(LocationManager.GPS_PROVIDER, context);
        }
        catch (SecurityException e) {
            stopMockingLocationGps();
        }
    }

    /**
     * Set a mocked location.
     *
     * @param lat latitude
     * @param lon longitude
     */
    static void exec(double lat, double lon) {
        if (mockNetwork != null) {
            try {
                mockNetwork.pushLocation(lat, lon);
            }
            catch (Exception e) {
             // stopMockingLocationNetwork();
            }
        }

        if (mockGps != null) {
            try {
                mockGps.pushLocation(lat, lon);
            }
            catch (Exception e) {
             // stopMockingLocationGps();
            }
        }
    }

    /**
     * Destroy instances of 'MockLocationProvider'.
     */
    protected static void stopMockingLocation() {
        stopMockingLocationNetwork();
        stopMockingLocationGps();
    }

    private static void stopMockingLocationNetwork() {
        if (mockNetwork != null) {
            try {
                mockNetwork.shutdown();
            }
            catch(Exception e) {}
            mockNetwork = null;
        }
    }

    private static void stopMockingLocationGps() {
        if (mockGps != null) {
            try {
                mockGps.shutdown();
            }
            catch(Exception e) {}
            mockGps = null;
        }
    }
}
