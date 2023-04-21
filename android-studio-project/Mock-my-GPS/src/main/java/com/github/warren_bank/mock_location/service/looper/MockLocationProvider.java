package com.github.warren_bank.mock_location.service.looper;

// copied from:
//   https://github.com/mcastillof/FakeTraveler/blob/v1.6/app/src/main/java/cl/coders/faketraveler/MockLocationProvider.java

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;

public class MockLocationProvider {
    String providerName;
    Context ctx;

    /**
     * Class constructor
     *
     * @param name provider
     * @param ctx  context
     * @return Void
     */
    public MockLocationProvider(String name, Context ctx) {
        this.providerName = name;
        this.ctx = ctx;

        int powerUsage = 0;
        int accuracy   = 5;

        if (Build.VERSION.SDK_INT >= 30) {
            powerUsage = 1;
            accuracy   = 2;
        }

        LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        startup(lm, powerUsage, accuracy, /* maxRetryCount= */ 3, /* currentRetryCount= */ 0);
    }

    private void startup(LocationManager lm, int powerUsage, int accuracy, int maxRetryCount, int currentRetryCount) {
        if (currentRetryCount < maxRetryCount) {
            try {
                shutdown();
                lm.addTestProvider(providerName, false, false, false, false, false, true, true, powerUsage, accuracy);
                lm.setTestProviderEnabled(providerName, true);
            }
            catch(Exception e) {
                startup(lm, powerUsage, accuracy, maxRetryCount, (currentRetryCount + 1));
            }
        }
        else {
            throw new SecurityException("Not allowed to perform MOCK_LOCATION");
        }
    }

    /**
     * Pushes the location in the system (mock). This is where the magic gets done.
     *
     * @param lat latitude
     * @param lon longitude
     * @return Void
     */
    public void pushLocation(double lat, double lon) {
        LocationManager lm    = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        Location mockLocation = MockLocationProvider.getLocation(providerName, lat, lon);

        lm.setTestProviderLocation(providerName, mockLocation);
    }

    protected static Location getLocation(double lat, double lon) {
        return MockLocationProvider.getLocation(/* providerName= */ null, lat, lon);
    }

    protected static Location getLocation(double lat, double lon, int advanceTimeMillis) {
        return MockLocationProvider.getLocation(/* providerName= */ null, lat, lon, advanceTimeMillis);
    }

    protected static Location getLocation(String providerName, double lat, double lon) {
        return MockLocationProvider.getLocation(providerName, lat, lon, /* advanceTimeMillis= */ 0);
    }

    protected static Location getLocation(String providerName, double lat, double lon, int advanceTimeMillis) {
        Location mockLocation = new Location(providerName);
        mockLocation.setLatitude(lat);
        mockLocation.setLongitude(lon);
        mockLocation.setAltitude(3F);
        mockLocation.setTime(System.currentTimeMillis() + advanceTimeMillis);
        mockLocation.setSpeed(0.01F);
        mockLocation.setBearing(1F);
        mockLocation.setAccuracy(3F);
        if (Build.VERSION.SDK_INT >= 26) {
            mockLocation.setBearingAccuracyDegrees(0.1F);
        }
        if (Build.VERSION.SDK_INT >= 26) {
            mockLocation.setVerticalAccuracyMeters(0.1F);
        }
        if (Build.VERSION.SDK_INT >= 26) {
            mockLocation.setSpeedAccuracyMetersPerSecond(0.01F);
        }
        if (Build.VERSION.SDK_INT >= 17) {
            mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        return mockLocation;
    }

    /**
     * Removes the provider
     *
     * @return Void
     */
    public void shutdown() {
        try {
            LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
            lm.removeTestProvider(providerName);
        }
        catch(Exception e) {}
    }
}
