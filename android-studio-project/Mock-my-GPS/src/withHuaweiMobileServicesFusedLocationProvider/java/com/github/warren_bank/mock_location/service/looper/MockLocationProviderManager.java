package com.github.warren_bank.mock_location.service.looper;

import android.content.Context;

public class MockLocationProviderManager {

    protected static void startMockingLocation(Context context) {
        AospMockLocationProviderManager.startMockingLocation(context);
        HmsMockLocationProviderManager.startMockingLocation(context);
    }

    protected static void exec(double lat, double lon) {
        AospMockLocationProviderManager.exec(lat, lon);
        HmsMockLocationProviderManager.exec(lat, lon);
    }

    protected static void stopMockingLocation() {
        AospMockLocationProviderManager.stopMockingLocation();
        HmsMockLocationProviderManager.stopMockingLocation();
    }
}
