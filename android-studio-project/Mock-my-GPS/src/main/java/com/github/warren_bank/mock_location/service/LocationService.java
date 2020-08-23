package com.github.warren_bank.mock_location.service;

import com.github.warren_bank.mock_location.R;
import com.github.warren_bank.mock_location.data_model.LocPoint;
import com.github.warren_bank.mock_location.service.looper.LocationThreadManager;
import com.github.warren_bank.mock_location.ui.MainActivity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;

public class LocationService extends Service {
    private final static int NOTIFICATION_ID          = 1;
    private final static String ACTION_START          = "START";
    private final static String ACTION_STOP           = "STOP";
    private final static String ACTION_PREFS          = "SHARED_PREFS_CHANGE";
    private final static String EXTRA_ORIGIN_LAT      = "ORIGIN_LAT";
    private final static String EXTRA_ORIGIN_LON      = "ORIGIN_LON";
    private final static String EXTRA_DESTINATION_LAT = "DESTINATION_LAT";
    private final static String EXTRA_DESTINATION_LON = "DESTINATION_LON";
    private final static String EXTRA_TRIP_DURATION   = "TRIP_DURATION";

    private static boolean running = false;

    private IBinder locationBinder;
    private LocationThreadManager LTM;

    public class LocationBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public void onCreate() {
        LTM = LocationThreadManager.get();
        LTM.init(LocationService.this);

        locationBinder = new LocationBinder();

        showNotification();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return locationBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart(intent, startId);
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        processIntent(intent);
    }

    @Override
    public void onDestroy() {
        hideNotification();
    }

    // -------------------------------------------------------------------------
    // foregrounding..

    private String getNotificationChannelId() {
        return getPackageName();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            String channelId       = getNotificationChannelId();
            NotificationManager NM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel NC = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH);

            NC.setDescription(channelId);
            NC.setSound(null, null);
            NM.createNotificationChannel(NC);
        }
    }

    private void showNotification() {
        Notification notification = getNotification();

        if (Build.VERSION.SDK_INT >= 5) {
            createNotificationChannel();
            startForeground(NOTIFICATION_ID, notification);
        }
        else {
            NotificationManager NM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NM.notify(NOTIFICATION_ID, notification);
        }
    }

    private void hideNotification() {
        if (Build.VERSION.SDK_INT >= 5) {
            stopForeground(true);
        }
        else {
            NotificationManager NM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NM.cancel(NOTIFICATION_ID);
        }
    }

    private Notification getNotification() {
        Notification notification  = (Build.VERSION.SDK_INT >= 26)
            ? (new Notification.Builder(/* context= */ LocationService.this, /* channelId= */ getNotificationChannelId())).build()
            :  new Notification()
        ;

        notification.when          = System.currentTimeMillis();
        notification.flags         = 0;
        notification.flags        |= Notification.FLAG_ONGOING_EVENT;
        notification.flags        |= Notification.FLAG_NO_CLEAR;
        notification.icon          = R.drawable.launcher;
        notification.tickerText    = getString(R.string.notification_service_ticker);
        notification.contentIntent = getPendingIntent_MainActivity();
     // notification.deleteIntent  = getPendingIntent_StopService();

        if (Build.VERSION.SDK_INT >= 16) {
            notification.priority  = Notification.PRIORITY_HIGH;
        }
        else {
            notification.flags    |= Notification.FLAG_HIGH_PRIORITY;
        }

        if (Build.VERSION.SDK_INT >= 21) {
            notification.visibility = Notification.VISIBILITY_PUBLIC;
        }

        RemoteViews contentView    = new RemoteViews(getPackageName(), R.layout.service_notification);
        contentView.setImageViewResource(R.id.notification_icon, R.drawable.launcher);
        contentView.setTextViewText(R.id.notification_text_line1, getString(R.string.notification_service_content_line1));
        contentView.setTextViewText(R.id.notification_text_line2, getString(R.string.notification_service_content_line2));
        notification.contentView   = contentView;

        return notification;
    }

    private PendingIntent getPendingIntent_MainActivity() {
        Intent intent = new Intent(LocationService.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        String current_tab_tag = (!LTM.isFlyMode())
            ? getString(R.string.MainActivity_tab_1_tag)
            : getString(R.string.MainActivity_tab_2_tag)
        ;
        intent.putExtra(getString(R.string.MainActivity_extra_current_tab_tag), current_tab_tag);

        return PendingIntent.getActivity(LocationService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getPendingIntent_StopService() {
        Intent intent = doStop(LocationService.this, false);

        return PendingIntent.getService(LocationService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // -------------------------------------------------------------------------
    // process inbound intents

    private void processIntent(Intent intent) {
        if (intent == null)
            return;

        String action = intent.getAction();
        if (action == null)
            return;

        switch (action) {
            case ACTION_START: {
                running = true;
                LTM.start(processIntentExtras(intent));
                break;
            }
            case ACTION_STOP: {
                running = false;
                LTM.stop();
                stopSelf();
                break;
            }
            case ACTION_PREFS: {
                LTM.onSharedPrefsChange((short) 0);
                break;
            }
        }
    }

    private LocPoint processIntentExtras(Intent intent) {
        double origin_lat      = intent.getDoubleExtra(EXTRA_ORIGIN_LAT,      2000.0);
        double origin_lon      = intent.getDoubleExtra(EXTRA_ORIGIN_LON,      2000.0);
        double destination_lat = intent.getDoubleExtra(EXTRA_DESTINATION_LAT, 2000.0);
        double destination_lon = intent.getDoubleExtra(EXTRA_DESTINATION_LON, 2000.0);
        int    trip_duration   = intent.getIntExtra(EXTRA_TRIP_DURATION,      0);

        if ((origin_lat > 1000) || (origin_lon > 1000))
            return null;

        LocPoint origin = new LocPoint(origin_lat, origin_lon);
        LTM.jumpToLocation(origin);

        if ((destination_lat > 1000) || (destination_lon > 1000) || (trip_duration <= 0))
            return origin;

        LocPoint destination = new LocPoint(destination_lat, destination_lon);
        LTM.flyToLocation(destination, trip_duration);
        return origin;
    }

    // -------------------------------------------------------------------------
    // static API for Activities that need to send intents to Service

    public static Intent doStart(Context context, boolean broadcast, LocPoint origin, LocPoint destination, int trip_duration) {
        if (origin == null)
            return null;

        Intent intent = new Intent(context, LocationService.class);
        addIntentExtras(intent, origin, destination, trip_duration);
        return doAction(context, intent, ACTION_START, broadcast);
    }

    private static void addIntentExtras(Intent intent, LocPoint origin, LocPoint destination, int trip_duration) {
        boolean is_trip = (destination != null) && (trip_duration > 0);

        intent.putExtra(EXTRA_ORIGIN_LAT, origin.getLatitude());
        intent.putExtra(EXTRA_ORIGIN_LON, origin.getLongitude());

        if (is_trip) {
            intent.putExtra(EXTRA_DESTINATION_LAT, destination.getLatitude());
            intent.putExtra(EXTRA_DESTINATION_LON, destination.getLongitude());
            intent.putExtra(EXTRA_TRIP_DURATION,   trip_duration);
        }
    }

    public static Intent doStop(Context context, boolean broadcast) {
        if (!running) return null;

        Intent intent = new Intent(context, LocationService.class);
        return doAction(context, intent, ACTION_STOP, broadcast);
    }

    public static Intent doSharedPrefsChange(Context context, boolean broadcast) {
        if (!running) return null;

        Intent intent = new Intent(context, LocationService.class);
        return doAction(context, intent, ACTION_PREFS, broadcast);
    }

    private static Intent doAction(Context context, Intent intent, String action, boolean broadcast) {
        intent.setAction(action);

        if (broadcast)
            context.startService(intent);

        return intent;
    }

    public static boolean isStarted() {
        return running;
    }

    // -------------------------------------------------------------------------
    // API for Activities that bind to Service
    //
    // STATUS: none of the Activities currently need to bind to this Service
    // TO DO?: remove `LocationBinder` class, `locationBinder` instance, `getLocationThreadManager()` method, and update `onBind()` to return `null`

    public LocationThreadManager getLocationThreadManager() {
        return LTM;
    }

}
