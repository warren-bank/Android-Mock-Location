package com.github.warren_bank.mock_location.data_model;

import com.github.warren_bank.mock_location.R;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsState {
    public String  bookmarks;
    public int     time_interval;
    public int     fixed_count;
    public boolean fixed_joystick_enabled;
    public double  fixed_joystick_increment;
    public boolean trip_hold_destination;
    public double  trip_origin_lat;
    public double  trip_origin_lon;
    public double  trip_destination_lat;
    public double  trip_destination_lon;
    public int     trip_duration;

    public SharedPrefsState(Context context, boolean skip_bookmarks) {
        SharedPreferences sharedPreferences = SharedPrefs.getSharedPreferences(context);

        bookmarks                = skip_bookmarks ? "" : SharedPrefs.getString(sharedPreferences, context, R.string.pref_bookmarks, "");
        time_interval            = SharedPrefs.getTimeInterval(sharedPreferences, context);
        fixed_count              = SharedPrefs.getFixedCount(sharedPreferences, context);
        fixed_joystick_enabled   = SharedPrefs.getFixedJoystickEnabled(sharedPreferences, context);
        fixed_joystick_increment = SharedPrefs.getFixedJoystickIncrement(sharedPreferences, context);
        trip_hold_destination    = SharedPrefs.getTripHoldDestination(sharedPreferences, context);
        trip_origin_lat          = SharedPrefs.getTripOriginLat(sharedPreferences, context);
        trip_origin_lon          = SharedPrefs.getTripOriginLon(sharedPreferences, context);
        trip_destination_lat     = SharedPrefs.getTripDestinationLat(sharedPreferences, context);
        trip_destination_lon     = SharedPrefs.getTripDestinationLon(sharedPreferences, context);
        trip_duration            = SharedPrefs.getTripDuration(sharedPreferences, context);
    }

    public SharedPrefsState(
        String  _bookmarks,
        int     _time_interval,
        int     _fixed_count,
        boolean _fixed_joystick_enabled,
        double  _fixed_joystick_increment,
        boolean _trip_hold_destination,
        double  _trip_origin_lat,
        double  _trip_origin_lon,
        double  _trip_destination_lat,
        double  _trip_destination_lon,
        int     _trip_duration
    ) {
        bookmarks                = _bookmarks;
        time_interval            = _time_interval;
        fixed_count              = _fixed_count;
        fixed_joystick_enabled   = _fixed_joystick_enabled;
        fixed_joystick_increment = _fixed_joystick_increment;
        trip_hold_destination    = _trip_hold_destination;
        trip_origin_lat          = _trip_origin_lat;
        trip_origin_lon          = _trip_origin_lon;
        trip_destination_lat     = _trip_destination_lat;
        trip_destination_lon     = _trip_destination_lon;
        trip_duration            = _trip_duration;
    }

    public boolean equals(SharedPrefsState that) {
        return (
                (that.bookmarks.equals(                 bookmarks))
            &&  (that.time_interval                  == time_interval)
            &&  (that.fixed_count                    == fixed_count)
            &&  (that.fixed_joystick_enabled         == fixed_joystick_enabled)
            &&  is_equal(that.fixed_joystick_increment, fixed_joystick_increment)
            &&  (that.trip_hold_destination          == trip_hold_destination)
            &&  is_equal(that.trip_origin_lat,          trip_origin_lat)
            &&  is_equal(that.trip_origin_lon,          trip_origin_lon)
            &&  is_equal(that.trip_destination_lat,     trip_destination_lat)
            &&  is_equal(that.trip_destination_lon,     trip_destination_lon)
            &&  (that.trip_duration                  == trip_duration)
        );
    }

    public short diff(SharedPrefsState that) {
        short diff_fields = 0;

        if (!that.bookmarks.equals(bookmarks))                                           diff_fields |= (1 <<  0);  //    1
        if (that.time_interval != time_interval)                                         diff_fields |= (1 <<  1);  //    2
        if (that.fixed_count   != fixed_count)                                           diff_fields |= (1 <<  2);  //    4
        if (that.fixed_joystick_enabled != fixed_joystick_enabled)                       diff_fields |= (1 <<  3);  //    8
        if (!is_equal(that.fixed_joystick_increment, fixed_joystick_increment, 1e-6))    diff_fields |= (1 <<  4);  //   16
        if (that.trip_hold_destination != trip_hold_destination)                         diff_fields |= (1 <<  5);  //   32
        if (!is_equal(that.trip_origin_lat, trip_origin_lat))                            diff_fields |= (1 <<  6);  //   64
        if (!is_equal(that.trip_origin_lon, trip_origin_lon))                            diff_fields |= (1 <<  7);  //  128
        if (!is_equal(that.trip_destination_lat, trip_destination_lat))                  diff_fields |= (1 <<  8);  //  256
        if (!is_equal(that.trip_destination_lon, trip_destination_lon))                  diff_fields |= (1 <<  9);  //  512
        if (that.trip_duration != trip_duration)                                         diff_fields |= (1 << 10);  // 1024

        return diff_fields;
    }

    private boolean is_equal(double a, double b) {
        double threshold = 1e-4;
        return is_equal(a, b, threshold);
    }

    private boolean is_equal(double a, double b, double threshold) {
        return (Math.abs(a - b) < threshold);
    }

}
