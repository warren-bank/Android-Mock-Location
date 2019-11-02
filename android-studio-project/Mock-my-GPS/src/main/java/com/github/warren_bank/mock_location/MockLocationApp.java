package com.github.warren_bank.mock_location;

import com.github.warren_bank.mock_location.looper.LocationThreadManager;

import android.app.Application;

public class MockLocationApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LocationThreadManager.get().init(getApplicationContext());
    }

}
