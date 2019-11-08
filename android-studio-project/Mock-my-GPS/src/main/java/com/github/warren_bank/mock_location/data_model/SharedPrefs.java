package com.github.warren_bank.mock_location.data_model;

import com.github.warren_bank.mock_location.R;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;

public final class SharedPrefs {

    // ---------------------------------------------------------------------------------------------

    public static SharedPreferences getSharedPreferences(Context context) {
        String PREFS_FILENAME = context.getString(R.string.prefs_filename);
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    public static SharedPreferences.Editor getSharedPreferencesEditor(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        return editor;
    }

    // --------------------------------------------------------------------------------------------- putString()

    protected static boolean putString(Context context, int pref_key_id, String value) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putString(editor, context, pref_key_id, value, true);
    }

    protected static boolean putString(SharedPreferences.Editor editor, Context context, int pref_key_id, String value, boolean flush) {
        String key = context.getString(pref_key_id);
        return putString(editor, key, value, flush);
    }

    protected static boolean putString(SharedPreferences.Editor editor, String key, String value, boolean flush) {
        editor.putString(key, value);
        return flush && editor.commit();
    }

    // --------------------------------------------------------------------------------------------- getString()

    protected static String getString(Context context, int pref_key_id, String defValue) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getString(sharedPreferences, context, pref_key_id, defValue);
    }

    protected static String getString(SharedPreferences sharedPreferences, Context context, int pref_key_id, String defValue) {
        String key = context.getString(pref_key_id);
        return getString(sharedPreferences, key, defValue);
    }

    protected static String getString(SharedPreferences sharedPreferences, String key, String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    // --------------------------------------------------------------------------------------------- putBoolean()

    protected static boolean putBoolean(Context context, int pref_key_id, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putBoolean(editor, context, pref_key_id, value, true);
    }

    protected static boolean putBoolean(SharedPreferences.Editor editor, Context context, int pref_key_id, boolean value, boolean flush) {
        String key = context.getString(pref_key_id);
        return putBoolean(editor, key, value, flush);
    }

    protected static boolean putBoolean(SharedPreferences.Editor editor, String key, boolean value, boolean flush) {
        editor.putBoolean(key, value);
        return flush && editor.commit();
    }

    // --------------------------------------------------------------------------------------------- getBoolean()

    protected static boolean getBoolean(Context context, int pref_key_id, boolean defValue) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getBoolean(sharedPreferences, context, pref_key_id, defValue);
    }

    protected static boolean getBoolean(SharedPreferences sharedPreferences, Context context, int pref_key_id, boolean defValue) {
        String key = context.getString(pref_key_id);
        return getBoolean(sharedPreferences, key, defValue);
    }

    protected static boolean getBoolean(SharedPreferences sharedPreferences, String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    // --------------------------------------------------------------------------------------------- putInt()

    protected static boolean putInt(Context context, int pref_key_id, int value) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putInt(editor, context, pref_key_id, value, true);
    }

    protected static boolean putInt(SharedPreferences.Editor editor, Context context, int pref_key_id, int value, boolean flush) {
        String key = context.getString(pref_key_id);
        return putInt(editor, key, value, flush);
    }

    protected static boolean putInt(SharedPreferences.Editor editor, String key, int value, boolean flush) {
        editor.putInt(key, value);
        return flush && editor.commit();
    }

    // --------------------------------------------------------------------------------------------- getInt()

    protected static int getInt(Context context, int pref_key_id, int defValue) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getInt(sharedPreferences, context, pref_key_id, defValue);
    }

    protected static int getInt(SharedPreferences sharedPreferences, Context context, int pref_key_id, int defValue) {
        String key = context.getString(pref_key_id);
        return getInt(sharedPreferences, key, defValue);
    }

    protected static int getInt(SharedPreferences sharedPreferences, String key, int defValue) {
        return sharedPreferences.getInt(key, defValue);
    }

    // --------------------------------------------------------------------------------------------- putLong()

    protected static boolean putLong(Context context, int pref_key_id, long value) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putLong(editor, context, pref_key_id, value, true);
    }

    protected static boolean putLong(SharedPreferences.Editor editor, Context context, int pref_key_id, long value, boolean flush) {
        String key = context.getString(pref_key_id);
        return putLong(editor, key, value, flush);
    }

    protected static boolean putLong(SharedPreferences.Editor editor, String key, long value, boolean flush) {
        editor.putLong(key, value);
        return flush && editor.commit();
    }

    // --------------------------------------------------------------------------------------------- getLong()

    protected static long getLong(Context context, int pref_key_id, long defValue) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getLong(sharedPreferences, context, pref_key_id, defValue);
    }

    protected static long getLong(SharedPreferences sharedPreferences, Context context, int pref_key_id, long defValue) {
        String key = context.getString(pref_key_id);
        return getLong(sharedPreferences, key, defValue);
    }

    protected static long getLong(SharedPreferences sharedPreferences, String key, long defValue) {
        return sharedPreferences.getLong(key, defValue);
    }

    // --------------------------------------------------------------------------------------------- putFloat()

    protected static boolean putFloat(Context context, int pref_key_id, float value) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putFloat(editor, context, pref_key_id, value, true);
    }

    protected static boolean putFloat(SharedPreferences.Editor editor, Context context, int pref_key_id, float value, boolean flush) {
        String key = context.getString(pref_key_id);
        return putFloat(editor, key, value, flush);
    }

    protected static boolean putFloat(SharedPreferences.Editor editor, String key, float value, boolean flush) {
        editor.putFloat(key, value);
        return flush && editor.commit();
    }

    // --------------------------------------------------------------------------------------------- getFloat()

    protected static float getFloat(Context context, int pref_key_id, float defValue) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getFloat(sharedPreferences, context, pref_key_id, defValue);
    }

    protected static float getFloat(SharedPreferences sharedPreferences, Context context, int pref_key_id, float defValue) {
        String key = context.getString(pref_key_id);
        return getFloat(sharedPreferences, key, defValue);
    }

    protected static float getFloat(SharedPreferences sharedPreferences, String key, float defValue) {
        return sharedPreferences.getFloat(key, defValue);
    }

    // --------------------------------------------------------------------------------------------- putDouble()

    protected static boolean putDouble(Context context, int pref_key_id, double value) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putDouble(editor, context, pref_key_id, value, true);
    }

    protected static boolean putDouble(SharedPreferences.Editor editor, Context context, int pref_key_id, double value, boolean flush) {
        String key = context.getString(pref_key_id);
        return putDouble(editor, key, value, flush);
    }

    protected static boolean putDouble(SharedPreferences.Editor editor, String key, double value, boolean flush) {
        long longValue = Double.doubleToLongBits(value);
        editor.putLong(key, longValue);
        return flush && editor.commit();
    }

    // --------------------------------------------------------------------------------------------- getDouble()

    protected static double getDouble(Context context, int pref_key_id, double defValue) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getDouble(sharedPreferences, context, pref_key_id, defValue);
    }

    protected static double getDouble(SharedPreferences sharedPreferences, Context context, int pref_key_id, double defValue) {
        String key = context.getString(pref_key_id);
        return getDouble(sharedPreferences, key, defValue);
    }

    protected static double getDouble(SharedPreferences sharedPreferences, String key, double defValue) {
        long longDefValue = Double.doubleToLongBits(defValue);
        long longValue    = sharedPreferences.getLong(key, longDefValue);
        double value      = Double.longBitsToDouble(longValue);
        return value;
    }

    // --------------------------------------------------------------------------------------------- putBookmarkItems()

    public static boolean putBookmarkItems(Context context, ArrayList<BookmarkItem> arrayList) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putBookmarkItems(editor, context, arrayList, true);
    }

    public static boolean putBookmarkItems(SharedPreferences.Editor editor, Context context, ArrayList<BookmarkItem> arrayList, boolean flush) {
        int pref_key_id = R.string.pref_bookmarks;
        String json     = BookmarkItem.toJson(arrayList);
        return putString(editor, context, pref_key_id, json, flush);
    }

    // --------------------------------------------------------------------------------------------- getBookmarkItems()

    public static ArrayList<BookmarkItem> getBookmarkItems(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getBookmarkItems(sharedPreferences, context);
    }

    public static ArrayList<BookmarkItem> getBookmarkItems(SharedPreferences sharedPreferences, Context context) {
        int pref_key_id = R.string.pref_bookmarks;
        String defValue = null;
        String json     = getString(sharedPreferences, context, pref_key_id, defValue);

        return (json == null)
          ? new ArrayList<BookmarkItem>()
          : BookmarkItem.fromJson(json)
        ;
    }

    // --------------------------------------------------------------------------------------------- putTimeInterval()

    public static boolean putTimeInterval(Context context, int value) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putTimeInterval(editor, context, value, true);
    }

    public static boolean putTimeInterval(SharedPreferences.Editor editor, Context context, int value, boolean flush) {
        int pref_key_id = R.string.pref_time_interval;
        return putInt(editor, context, pref_key_id, value, flush);
    }

    // --------------------------------------------------------------------------------------------- getTimeInterval()

    public static int getTimeInterval(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getTimeInterval(sharedPreferences, context);
    }

    public static int getTimeInterval(SharedPreferences sharedPreferences, Context context) {
        int pref_key_id = R.string.pref_time_interval;
        int defValue = 1000;
        return getInt(sharedPreferences, context, pref_key_id, defValue);
    }

    // --------------------------------------------------------------------------------------------- putFixedCount()

    public static boolean putFixedCount(Context context, int value) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putFixedCount(editor, context, value, true);
    }

    public static boolean putFixedCount(SharedPreferences.Editor editor, Context context, int value, boolean flush) {
        int pref_key_id = R.string.pref_fixed_count;
        return putInt(editor, context, pref_key_id, value, flush);
    }

    // --------------------------------------------------------------------------------------------- getFixedCount()

    public static int getFixedCount(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getFixedCount(sharedPreferences, context);
    }

    public static int getFixedCount(SharedPreferences sharedPreferences, Context context) {
        int pref_key_id = R.string.pref_fixed_count;
        int defValue = 0;
        return getInt(sharedPreferences, context, pref_key_id, defValue);
    }

    // --------------------------------------------------------------------------------------------- putFixedJoystickEnabled()

    public static boolean putFixedJoystickEnabled(Context context, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putFixedJoystickEnabled(editor, context, value, true);
    }

    public static boolean putFixedJoystickEnabled(SharedPreferences.Editor editor, Context context, boolean value, boolean flush) {
        int pref_key_id = R.string.pref_fixed_joystick_enabled;
        return putBoolean(editor, context, pref_key_id, value, flush);
    }

    // --------------------------------------------------------------------------------------------- getFixedJoystickEnabled()

    public static boolean getFixedJoystickEnabled(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getFixedJoystickEnabled(sharedPreferences, context);
    }

    public static boolean getFixedJoystickEnabled(SharedPreferences sharedPreferences, Context context) {
        int pref_key_id = R.string.pref_fixed_joystick_enabled;
        boolean defValue = true;
        return getBoolean(sharedPreferences, context, pref_key_id, defValue);
    }

    // --------------------------------------------------------------------------------------------- putFixedJoystickIncrement()

    public static boolean putFixedJoystickIncrement(Context context, double value) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putFixedJoystickIncrement(editor, context, value, true);
    }

    public static boolean putFixedJoystickIncrement(SharedPreferences.Editor editor, Context context, double value, boolean flush) {
        int pref_key_id = R.string.pref_fixed_joystick_increment;
        return putDouble(editor, context, pref_key_id, value, flush);
    }

    // --------------------------------------------------------------------------------------------- getFixedJoystickIncrement()

    public static double getFixedJoystickIncrement(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getFixedJoystickIncrement(sharedPreferences, context);
    }

    public static double getFixedJoystickIncrement(SharedPreferences sharedPreferences, Context context) {
        int pref_key_id = R.string.pref_fixed_joystick_increment;
        double defValue = 0.00002;
        return getDouble(sharedPreferences, context, pref_key_id, defValue);
    }

    // --------------------------------------------------------------------------------------------- putTripHoldDestination()

    public static boolean putTripHoldDestination(Context context, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putTripHoldDestination(editor, context, value, true);
    }

    public static boolean putTripHoldDestination(SharedPreferences.Editor editor, Context context, boolean value, boolean flush) {
        int pref_key_id = R.string.pref_trip_hold_destination;
        return putBoolean(editor, context, pref_key_id, value, flush);
    }

    // --------------------------------------------------------------------------------------------- getTripHoldDestination()

    public static boolean getTripHoldDestination(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getTripHoldDestination(sharedPreferences, context);
    }

    public static boolean getTripHoldDestination(SharedPreferences sharedPreferences, Context context) {
        int pref_key_id = R.string.pref_trip_hold_destination;
        boolean defValue = true;
        return getBoolean(sharedPreferences, context, pref_key_id, defValue);
    }

    // --------------------------------------------------------------------------------------------- putTripOriginLat()

    public static boolean putTripOriginLat(Context context, double value) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putTripOriginLat(editor, context, value, true);
    }

    public static boolean putTripOriginLat(SharedPreferences.Editor editor, Context context, double value, boolean flush) {
        int pref_key_id = R.string.pref_trip_origin_lat;
        return putDouble(editor, context, pref_key_id, value, flush);
    }

    // --------------------------------------------------------------------------------------------- getTripOriginLat()

    public static double getTripOriginLat(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getTripOriginLat(sharedPreferences, context);
    }

    public static double getTripOriginLat(SharedPreferences sharedPreferences, Context context) {
        int pref_key_id = R.string.pref_trip_origin_lat;
        double defValue = 38.897957;  // The White House, Washington, DC, USA
        return getDouble(sharedPreferences, context, pref_key_id, defValue);
    }

    // --------------------------------------------------------------------------------------------- putTripOriginLon()

    public static boolean putTripOriginLon(Context context, double value) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putTripOriginLon(editor, context, value, true);
    }

    public static boolean putTripOriginLon(SharedPreferences.Editor editor, Context context, double value, boolean flush) {
        int pref_key_id = R.string.pref_trip_origin_lon;
        return putDouble(editor, context, pref_key_id, value, flush);
    }

    // --------------------------------------------------------------------------------------------- getTripOriginLon()

    public static double getTripOriginLon(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getTripOriginLon(sharedPreferences, context);
    }

    public static double getTripOriginLon(SharedPreferences sharedPreferences, Context context) {
        int pref_key_id = R.string.pref_trip_origin_lon;
        double defValue = -77.036560;  // The White House, Washington, DC, USA
        return getDouble(sharedPreferences, context, pref_key_id, defValue);
    }

    // --------------------------------------------------------------------------------------------- putTripOrigin()

    public static boolean putTripOrigin(Context context, LocPoint point) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putTripOrigin(editor, context, point, true);
    }

    public static boolean putTripOrigin(SharedPreferences.Editor editor, Context context, LocPoint point, boolean flush) {
        double lat = point.getLatitude();
        double lon = point.getLongitude();

        putTripOriginLat(editor, context, lat, false);
        putTripOriginLon(editor, context, lon, false);

        return flush && editor.commit();
    }

    // --------------------------------------------------------------------------------------------- getTripOrigin()

    public static LocPoint getTripOrigin(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getTripOrigin(sharedPreferences, context);
    }

    public static LocPoint getTripOrigin(SharedPreferences sharedPreferences, Context context) {
        double lat = getTripOriginLat(sharedPreferences, context);
        double lon = getTripOriginLon(sharedPreferences, context);

        return new LocPoint(lat, lon);
    }

    // --------------------------------------------------------------------------------------------- putTripDestinationLat()

    public static boolean putTripDestinationLat(Context context, double value) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putTripDestinationLat(editor, context, value, true);
    }

    public static boolean putTripDestinationLat(SharedPreferences.Editor editor, Context context, double value, boolean flush) {
        int pref_key_id = R.string.pref_trip_destination_lat;
        return putDouble(editor, context, pref_key_id, value, flush);
    }

    // --------------------------------------------------------------------------------------------- getTripDestinationLat()

    public static double getTripDestinationLat(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getTripDestinationLat(sharedPreferences, context);
    }

    public static double getTripDestinationLat(SharedPreferences sharedPreferences, Context context) {
        int pref_key_id = R.string.pref_trip_destination_lat;
        double defValue = 38.921939;  // Dischord Records, 3819 Beecher St., Washington, DC, USA (https://www.dischord.com/)
        return getDouble(sharedPreferences, context, pref_key_id, defValue);
    }

    // --------------------------------------------------------------------------------------------- putTripDestinationLon()

    public static boolean putTripDestinationLon(Context context, double value) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putTripDestinationLon(editor, context, value, true);
    }

    public static boolean putTripDestinationLon(SharedPreferences.Editor editor, Context context, double value, boolean flush) {
        int pref_key_id = R.string.pref_trip_destination_lon;
        return putDouble(editor, context, pref_key_id, value, flush);
    }

    // --------------------------------------------------------------------------------------------- getTripDestinationLon()

    public static double getTripDestinationLon(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getTripDestinationLon(sharedPreferences, context);
    }

    public static double getTripDestinationLon(SharedPreferences sharedPreferences, Context context) {
        int pref_key_id = R.string.pref_trip_destination_lon;
        double defValue = -77.075191;  // Dischord Records, 3819 Beecher St., Washington, DC, USA (https://www.dischord.com/)
        return getDouble(sharedPreferences, context, pref_key_id, defValue);
    }

    // --------------------------------------------------------------------------------------------- putTripDestination()

    public static boolean putTripDestination(Context context, LocPoint point) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putTripDestination(editor, context, point, true);
    }

    public static boolean putTripDestination(SharedPreferences.Editor editor, Context context, LocPoint point, boolean flush) {
        double lat = point.getLatitude();
        double lon = point.getLongitude();

        putTripDestinationLat(editor, context, lat, false);
        putTripDestinationLon(editor, context, lon, false);

        return flush && editor.commit();
    }

    // --------------------------------------------------------------------------------------------- getTripDestination()

    public static LocPoint getTripDestination(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getTripDestination(sharedPreferences, context);
    }

    public static LocPoint getTripDestination(SharedPreferences sharedPreferences, Context context) {
        double lat = getTripDestinationLat(sharedPreferences, context);
        double lon = getTripDestinationLon(sharedPreferences, context);

        return new LocPoint(lat, lon);
    }

    // --------------------------------------------------------------------------------------------- putTripDuration()

    public static boolean putTripDuration(Context context, int value) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        return putTripDuration(editor, context, value, true);
    }

    public static boolean putTripDuration(SharedPreferences.Editor editor, Context context, int value, boolean flush) {
        int pref_key_id = R.string.pref_trip_duration;
        return putInt(editor, context, pref_key_id, value, flush);
    }

    // --------------------------------------------------------------------------------------------- getTripDuration()

    public static int getTripDuration(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return getTripDuration(sharedPreferences, context);
    }

    public static int getTripDuration(SharedPreferences sharedPreferences, Context context) {
        int pref_key_id = R.string.pref_trip_duration;
        int defValue = 60;
        return getInt(sharedPreferences, context, pref_key_id, defValue);
    }

    // ---------------------------------------------------------------------------------------------

}
