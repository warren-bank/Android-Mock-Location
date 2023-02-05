package com.github.warren_bank.mock_location.service.microg_nlp_backend;

import org.microg.nlp.api.LocationBackendService;
import org.microg.nlp.api.LocationHelper;

import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

public class BackendService extends LocationBackendService {
    protected static BackendService instance;

    protected static void update(double lat, double lon) {
        if (instance != null) {
            instance.report(
                instance.getLocation(lat, lon)
            );
        }
    }

    public static void reloadInstanceSettings() {
        if (instance != null) {
            instance.reloadSettings();
        }
    }

    private int timeadvance;

    @Override
    protected void onOpen() {
        super.onOpen();
        reloadSettings();
        instance = this;
    }

    @Override
    protected void onClose() {
        super.onClose();
        if (instance == this) {
            instance = null;
        }
    }

    private Location getLocation(double lat, double lon) {
        Location location = LocationHelper.create(
            /* source= */ getPackageName(),
            lat,
            lon,
            /* accuracy= */ 0.0f
        );

        location.setTime(System.currentTimeMillis() + timeadvance);

        return location;
    }

    private void reloadSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        timeadvance = Integer.parseInt(
            preferences.getString(
                getString(R.string.pref_timeadvance_key),
                getString(R.string.pref_timeadvance_default)
            )
        );
    }

}
