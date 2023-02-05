package com.github.warren_bank.mock_location.service.looper;

// copied from:
//   https://github.com/mcastillof/FakeTraveler/blob/v1.6/app/src/main/java/cl/coders/faketraveler/MainActivity.java

import com.github.warren_bank.mock_location.service.microg_nlp_backend.UnifiedNlpManager;

import android.content.Context;
import android.location.LocationManager;
import android.os.Build;

public class MockLocationProviderManager {

    private static MockLocationProvider mockNetwork = null;
    private static MockLocationProvider mockGps     = null;
    private static MockLocationProvider mockFused   = null;
    private static UnifiedNlpManager    nlpManager  = null;

    /**
     * Initialize instances of 'MockLocationProvider'.
     */
    protected static void startMockingLocation(Context context) {
        startMockingLocationNetwork(context);
        startMockingLocationGps(context);

        if (Build.VERSION.SDK_INT >= 31) {
            startMockingLocationFused(context);
        }

        nlpManager = new UnifiedNlpManager(context);
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

    private static void startMockingLocationGps(Context context) {
        stopMockingLocationGps();

        try {
            mockGps = new MockLocationProvider(LocationManager.GPS_PROVIDER, context);
        }
        catch (SecurityException e) {
            stopMockingLocationGps();
        }
    }

    private static void startMockingLocationFused(Context context) {
        stopMockingLocationFused();

        try {
            mockFused = new MockLocationProvider(LocationManager.FUSED_PROVIDER, context);
        }
        catch (SecurityException e) {
            stopMockingLocationFused();
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

        if (mockFused != null) {
            try {
                mockFused.pushLocation(lat, lon);
            }
            catch (Exception e) {
             // stopMockingLocationFused();
            }
        }

        if (nlpManager != null) {
            nlpManager.update(lat, lon);
        }
    }

    /**
     * Destroy instances of 'MockLocationProvider'.
     */
    protected static void stopMockingLocation() {
        stopMockingLocationNetwork();
        stopMockingLocationGps();
        stopMockingLocationFused();

        nlpManager = null;
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

    private static void stopMockingLocationFused() {
        if (mockFused != null) {
            try {
                mockFused.shutdown();
            }
            catch(Exception e) {}
            mockFused = null;
        }
    }
}
