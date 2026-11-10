package com.github.warren_bank.mock_location.ui;

// based on:
//   https://github.com/osmandapp/Osmand/blob/2.0.0/OsmAnd/src/net/osmand/plus/activities/search/GeoIntentActivity.java

import com.github.warren_bank.mock_location.R;
import com.github.warren_bank.mock_location.data_model.LocPoint;
import com.github.warren_bank.mock_location.data_model.SharedPrefs;
import com.github.warren_bank.mock_location.security_model.RuntimePermissions;
import com.github.warren_bank.mock_location.service.LocationService;
import com.github.warren_bank.mock_location.util.GeoPointParserUtil;
import com.github.warren_bank.mock_location.util.GeoPointParserUtil.GeoParsedPoint;
import com.github.warren_bank.mock_location.util.LocaleUtils;
import com.github.warren_bank.mock_location.util.ShortLinkLookup;
import com.github.warren_bank.mock_location.util.ThemeUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.net.Uri;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeoIntentActivity extends Activity implements RuntimePermissions.RuntimePermissionsListener {
    private final static String EXTRA_PURPOSE               = "purpose";
    private final static String EXTRA_SILENT_UPDATE         = "silent_update";
    private final static String EXTRA_FORCE_START           = "force_start";
    private final static String EXTRA_TRIP_DURATION_SECONDS = "trip_duration_seconds";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ThemeUtils.applyUiModeOverride(GeoIntentActivity.this, base);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // its dialogs (Purpose, link lookup, messages) follow the theme chosen in "PreferencesActivity"
        ThemeUtils.applyTheme(GeoIntentActivity.this);
        super.onCreate(savedInstanceState);
        LocaleUtils.apply(this);

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent == null) {
            handleNoData(null);
            return;
        }

        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            handleSharedText(intent.getStringExtra(Intent.EXTRA_TEXT));
            return;
        }

        final Uri uri = intent.getData();
        if (uri == null) {
            handleNoData(null);
            return;
        }

        final String uriString = uri.toString();
        final GeoParsedPoint geoPoint = GeoPointParserUtil.parse(uriString);
        if (geoPoint == null) {
            handleNoData(getString(R.string.error_geo_intent, uriString));
            return;
        }
        if (!geoPoint.isGeoPoint()) {
            handleNoData(getString(R.string.error_geo_intent, uriString));
            return;
        }

        final LocPoint point = new LocPoint(
            geoPoint.getLatitude(),
            geoPoint.getLongitude()
        );

        boolean handled = false;
        if (intent.hasExtra(EXTRA_PURPOSE)) {
            int purpose               = intent.getIntExtra(EXTRA_PURPOSE, 0);
            boolean silent_update     = intent.getBooleanExtra(EXTRA_SILENT_UPDATE, false);
            boolean force_start       = intent.getBooleanExtra(EXTRA_FORCE_START, false);
            int trip_duration_seconds = intent.getIntExtra(EXTRA_TRIP_DURATION_SECONDS, 60);
            handled = handleGeoDataPurpose(point, purpose, silent_update, force_start, trip_duration_seconds);
        }

        if (!handled)
            handleGeoData(point);
    }

    private void handleNoData(String error_message) {
        startMainActivity(error_message);
    }

    // =============================================================================================
    // Share: text sent from another app through the share sheet (enabled in "PreferencesActivity")
    // =============================================================================================

    private static final Pattern urlPattern        = Pattern.compile("https?://[^\\s<>\"']+", Pattern.CASE_INSENSITIVE);
    private static final Pattern coordinatePattern = Pattern.compile("([+-]?\\d{1,2}\\.\\d+)\\s*,\\s*([+-]?\\d{1,3}\\.\\d+)");

    private ProgressDialog lookupDialog;
    private boolean        lookupCancelled;

    private void handleSharedText(String text) {
        if ((text == null) || (text.trim().length() == 0)) {
            finishWithMessage(R.string.share_error_no_location);
            return;
        }

        // 1. a map URL that carries coordinates
        Matcher urlMatcher = urlPattern.matcher(text);
        while (urlMatcher.find()) {
            LocPoint point = parsePoint(urlMatcher.group());
            if (point != null) {
                handleGeoData(point);
                return;
            }
        }

        // 2. plain coordinates: "48.2082, 16.3738"
        Matcher coordinateMatcher = coordinatePattern.matcher(text);
        if (coordinateMatcher.find()) {
            try {
                double lat = Double.parseDouble(coordinateMatcher.group(1));
                double lon = Double.parseDouble(coordinateMatcher.group(2));

                if ((Math.abs(lat) <= 90) && (Math.abs(lon) <= 180)) {
                    handleGeoData(new LocPoint(lat, lon));
                    return;
                }
            }
            catch(NumberFormatException e) {}
        }

        // 3. a Google Maps short link: the only case that goes online
        String shortLink = ShortLinkLookup.findShortLink(text);
        if (shortLink != null) {
            lookUpShortLink(shortLink);
            return;
        }

        finishWithMessage(R.string.share_error_no_location);
    }

    private LocPoint parsePoint(String uriString) {
        GeoParsedPoint geoPoint = GeoPointParserUtil.parse(uriString);

        return ((geoPoint != null) && geoPoint.isGeoPoint())
            ? new LocPoint(geoPoint.getLatitude(), geoPoint.getLongitude())
            : null;
    }

    private void lookUpShortLink(final String shortLink) {
        lookupCancelled = false;

        lookupDialog = new ProgressDialog(GeoIntentActivity.this);
        lookupDialog.setMessage(getString(R.string.share_progress_lookup));
        lookupDialog.setIndeterminate(true);
        lookupDialog.setCancelable(true);
        lookupDialog.setCanceledOnTouchOutside(false);
        lookupDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                lookupCancelled = true;
                finish();
            }
        });
        lookupDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        lookupDialog.show();

        final Handler handler = new Handler(Looper.getMainLooper());

        new Thread(new Runnable() {
            @Override
            public void run() {
                String location = null;
                boolean reached = true;

                try {
                    location = ShortLinkLookup.lookUp(shortLink);
                }
                catch(IOException e) {
                    reached = false;
                }

                final String  result         = location;
                final boolean resultReached  = reached;

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onShortLinkLookedUp(result, resultReached);
                    }
                });
            }
        }).start();
    }

    private void onShortLinkLookedUp(String location, boolean reached) {
        if (lookupCancelled || isFinishing()) return;

        if (lookupDialog != null) {
            try {
                lookupDialog.dismiss();
            }
            catch(Exception e) {}
            lookupDialog = null;
        }

        if (!reached) {
            finishWithMessage(R.string.share_error_unreachable);
            return;
        }

        LocPoint point = (location == null) ? null : parsePoint(location);
        if (point != null) {
            handleGeoData(point);
            return;
        }

        // a place picked by search or tap resolves to an address and a feature ID, without coordinates
        finishWithMessage((location == null) ? R.string.share_error_no_location : R.string.share_error_no_coordinates);
    }

    // a dialog rather than a Toast: some devices (ex: Samsung One UI) cut a Toast off after two lines
    private void finishWithMessage(int message_id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(GeoIntentActivity.this);
        builder.setMessage(message_id);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        dialog.show();
    }

    private void handleGeoData(LocPoint point) {
        AlertDialog.Builder builder = new AlertDialog.Builder(GeoIntentActivity.this);
        builder.setTitle(R.string.geo_intent_menu_title);
        builder.setItems(R.array.geo_intent_menu_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int purpose = which + 1;
                boolean handled = handleGeoDataPurpose(point, purpose);

                if (handled)
                  dialog.dismiss();
            }
        });
        builder.show();
    }

    private boolean handleGeoDataPurpose(LocPoint point, int purpose) {
        return handleGeoDataPurpose(point, purpose, /* silent_update= */ false, /* force_start= */ false, /* trip_duration_seconds= */ 0);
    }

    private boolean handleGeoDataPurpose(LocPoint point, int purpose, boolean silent_update, boolean force_start, int trip_duration_seconds) {
        boolean handled = false;
        switch (purpose) {
            case 1:
                // fixed position
                if (silent_update) {
                    if (LocationService.isStarted()) {
                        SharedPrefs.putTripOrigin(GeoIntentActivity.this, point);
                        LocationService.getLocationThreadManager().jumpToLocation(point);
                        handled = true;
                        finish();
                    }
                    else if (force_start) {
                        startLocationService(point);
                        handled = true;
                    }
                }
                if (!handled) {
                    handleFixedPosition(point);
                    handled = true;
                }
                break;
            case 2:
                // trip origin
                handleTripOrigin(point);
                handled = true;
                break;
            case 3:
                // trip destination
                if (silent_update && LocationService.isStarted()) {
                    SharedPrefs.putTripDestination(GeoIntentActivity.this, point);
                    LocationService.getLocationThreadManager().flyToLocation(point, trip_duration_seconds);
                    handled = true;
                    finish();
                }
                else {
                    handleTripDestination(point);
                    handled = true;
                }
                break;
            case 4:
                // new bookmark
                handleNewBookmark(point);
                handled = true;
                break;
        }
        return handled;
    }

    private void handleFixedPosition(LocPoint point) {
        SharedPrefs.putTripOrigin(GeoIntentActivity.this, point);
        startMainActivity();
    }

    private void handleTripOrigin(LocPoint point) {
        SharedPrefs.putTripOrigin(GeoIntentActivity.this, point);
        startMainActivity(2);
    }

    private void handleTripDestination(LocPoint point) {
        SharedPrefs.putTripDestination(GeoIntentActivity.this, point);
        startMainActivity(2);
    }

    private void handleNewBookmark(LocPoint point) {
        startBookmarksActivity(point);
    }

    private void startMainActivity() {
        startMainActivity(0, null);
    }

    private void startMainActivity(int tab) {
        startMainActivity(tab, null);
    }

    private void startMainActivity(String toast) {
        startMainActivity(0, toast);
    }

    private void startMainActivity(int tab, String toast) {
        Intent intent = new Intent(GeoIntentActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (tab > 0) {
            String current_tab_tag = (tab == 1)
                ? getString(R.string.MainActivity_tab_1_tag)
                : getString(R.string.MainActivity_tab_2_tag)
            ;

            intent.putExtra(getString(R.string.MainActivity_extra_current_tab_tag), current_tab_tag);
        }
        if (toast != null) {
            intent.putExtra(getString(R.string.MainActivity_extra_toast), toast);
        }
        startActivity(intent);
    }

    private void startBookmarksActivity(LocPoint point) {
        double lat = point.getLatitude();
        double lon = point.getLongitude();

        Intent intent = new Intent(GeoIntentActivity.this, BookmarksActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(getString(R.string.BookmarksActivity_extra_add_lat), lat);
        intent.putExtra(getString(R.string.BookmarksActivity_extra_add_lon), lon);
        startActivity(intent);
    }

    // =============================================================================================
    // Runtime Permissions
    // =============================================================================================

    private LocPoint pointLocationService;

    private void startLocationService(LocPoint point) {
        if (point == null) return;

        pointLocationService = point;
        RuntimePermissions.requestPermissions(/* activity */ GeoIntentActivity.this, /* listener */ GeoIntentActivity.this);
    }

    private void startLocationService() {
        if (pointLocationService == null) return;

        SharedPrefs.putTripOrigin(GeoIntentActivity.this, pointLocationService);
        LocationService.doStart(getApplicationContext(), true, pointLocationService, null, 0);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        RuntimePermissions.onRequestPermissionsResult(/* activity */ GeoIntentActivity.this, /* listener */ GeoIntentActivity.this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        RuntimePermissions.onActivityResult(/* activity */ GeoIntentActivity.this, /* listener */ GeoIntentActivity.this, requestCode, resultCode, data);
    }

    // =============================================================================================
    // interface implementation: RuntimePermissions.RuntimePermissionsListener
    // =============================================================================================

    public void onPermissionsGranted() {
        if (RuntimePermissions.canAccessBackgroundLocation(GeoIntentActivity.this))
            startLocationService();
        else
            RuntimePermissions.requestPermissionAccessBackgroundLocation(/* activity */ GeoIntentActivity.this, /* listener */ GeoIntentActivity.this);
    }

    public void onPermissionsDenied(String[] permissions) {
        finish();
    }

}
