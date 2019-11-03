package com.github.warren_bank.mock_location.service.looper;

// copied from:
//   https://github.com/xiangtailiang/FakeGPS/blob/V1.1/app/src/main/java/com/github/fakegps/JoyStickManager.java

import com.github.warren_bank.mock_location.data_model.LocPoint;
import com.github.warren_bank.mock_location.data_model.SharedPrefsState;
import com.github.warren_bank.mock_location.event_hooks.IJoyStickPresenter;
import com.github.warren_bank.mock_location.event_hooks.ISharedPrefsListener;
import com.github.warren_bank.mock_location.service.LocationService;
import com.github.warren_bank.mock_location.ui.components.JoyStickView;

import android.content.Context;

public class LocationThreadManager implements IJoyStickPresenter, ISharedPrefsListener {
    private static LocationThreadManager INSTANCE = new LocationThreadManager();

    private Context mContext;
    private JoyStickView mJoyStickView;
    private LocationThread mLocationThread;
    private LocPoint mCurrentLocPoint;
    private LocPoint mTargetLocPoint;
    private int mFlyTime;
    private int mFlyTimeIndex;

    private int mTimeInterval;
    private int mFixedCount;
    private int mFixedCountRemaining;
    private boolean mFixedJoystickEnabled;
    private double mFixedJoystickIncrement;

    private boolean mIsStarted = false;
    private boolean mIsFlyMode = false;

    private LocationThreadManager() {
        mContext = null;
    }

    public void init(Context context) {
        mContext = context;
        importSharedPrefs();
    }

    public static LocationThreadManager get() {
        return INSTANCE;
    }

    public void start(LocPoint locPoint) {
        if (mContext == null) return;
        if (locPoint == null) return;

        mCurrentLocPoint = locPoint;
        if ((mLocationThread == null) || !mLocationThread.isAlive()) {
            mLocationThread = new LocationThread(mContext, this, mTimeInterval);
            mLocationThread.startThread();
        }

        if (mFixedJoystickEnabled && !mIsFlyMode) {
            showJoyStick();
        }

        mFixedCountRemaining = mFixedCount;
        mIsStarted = true;
    }

    public void stop() {
        if (mLocationThread != null) {
            mLocationThread.stopThread();
            mLocationThread = null;
        }

        hideJoyStick();
        mIsStarted = false;

        stopService();
    }

    private void stopService() {
        LocationService.doStop(mContext, true);
    }

    public boolean isStarted() {
        return mIsStarted;
    }

    public void showJoyStick() {
        if (mJoyStickView == null) {
            mJoyStickView = new JoyStickView(mContext);
            mJoyStickView.setJoyStickPresenter(this);
        }

        if (!mJoyStickView.isShowing()) {
            mJoyStickView.addToWindow();
        }
    }

    public void hideJoyStick() {
        if ((mJoyStickView != null) && mJoyStickView.isShowing()) {
            mJoyStickView.removeFromWindow();
        }
    }

    public LocPoint getCurrentLocPoint() {
        return mCurrentLocPoint;
    }

    public LocPoint getUpdateLocPoint() {
        if (!mIsFlyMode && (mFixedCountRemaining != 0)) {
            if (mFixedCountRemaining < 0) {
                return null;
            }
            if (mFixedCountRemaining == 1) {
                mFixedCountRemaining = -1;
            }
            else {
                mFixedCountRemaining--;
            }
        }

        if (!mIsFlyMode) {
            return mCurrentLocPoint;
        }

        if (mFlyTimeIndex >= mFlyTime) {
            mFlyTimeIndex++;
            jumpToLocation(mTargetLocPoint);
            mFixedCountRemaining = -1;
            return mCurrentLocPoint;
        }
        else {
            float factor = (float) mFlyTimeIndex / (float) mFlyTime;
            double lat = mCurrentLocPoint.getLatitude()  + (factor * (mTargetLocPoint.getLatitude()  - mCurrentLocPoint.getLatitude()));
            double lon = mCurrentLocPoint.getLongitude() + (factor * (mTargetLocPoint.getLongitude() - mCurrentLocPoint.getLongitude()));
            mFlyTimeIndex++;
            mCurrentLocPoint.setLatitude(lat);
            mCurrentLocPoint.setLongitude(lon);
            return mCurrentLocPoint;
        }
    }

    public boolean shouldContinue() {
        boolean is_done = (
                (!mIsFlyMode && (mFixedCountRemaining < 0))
            ||  ( mIsFlyMode && (mFlyTimeIndex > mFlyTime))
        );

        if (is_done) {
            stop();
        }

        return !is_done;
    }

    public void jumpToLocation(LocPoint location) {
        mIsFlyMode = false;
        mCurrentLocPoint = location;
    }

    public void flyToLocation(LocPoint location, int flyTime) {
        if (mIsStarted && mFixedJoystickEnabled) {
            hideJoyStick();
        }

        mTargetLocPoint = location;
        mFlyTime = flyTime;
        mFlyTimeIndex = 0;
        mIsFlyMode = true;
    }

    public boolean isFlyMode() {
        return mIsFlyMode;
    }

    public void stopFlyMode() {
        mIsFlyMode = false;
    }

    public void setMoveStep(double moveStep) {
        mFixedJoystickIncrement = moveStep;
    }

    public double getMoveStep() {
        return mFixedJoystickIncrement;
    }

    @Override
    public void onArrowUpClick() {
        mCurrentLocPoint.setLatitude(mCurrentLocPoint.getLatitude() + mFixedJoystickIncrement);
    }

    @Override
    public void onArrowDownClick() {
        mCurrentLocPoint.setLatitude(mCurrentLocPoint.getLatitude() - mFixedJoystickIncrement);
    }

    @Override
    public void onArrowLeftClick() {
        mCurrentLocPoint.setLongitude(mCurrentLocPoint.getLongitude() - mFixedJoystickIncrement);
    }

    @Override
    public void onArrowRightClick() {
        mCurrentLocPoint.setLongitude(mCurrentLocPoint.getLongitude() + mFixedJoystickIncrement);
    }

    // =================================
    // integrate with Shared Preferences
    // =================================

    @Override
    public void onSharedPrefsChange(short diff_fields) {
        importSharedPrefs();
    }

    private void importSharedPrefs() {
        if (mContext == null) return;

        SharedPrefsState prefsState = new SharedPrefsState(mContext, true);

        mTimeInterval           = prefsState.time_interval;
        mFixedCount             = prefsState.fixed_count;
        mFixedJoystickEnabled   = prefsState.fixed_joystick_enabled;
        mFixedJoystickIncrement = prefsState.fixed_joystick_increment;

        /*
        mCurrentLocPoint = new LocPoint(prefsState.trip_origin_lat,      prefsState.trip_origin_lon);
        mTargetLocPoint  = new LocPoint(prefsState.trip_destination_lat, prefsState.trip_destination_lon);
        mFlyTime         = prefsState.trip_duration;
        */

        if (mIsStarted && !mIsFlyMode) {
            if (mFixedJoystickEnabled)
                showJoyStick();
            else
                hideJoyStick();
        }

        if ((mLocationThread != null) && mLocationThread.isAlive()) {
            mLocationThread.updateTimeInterval(mTimeInterval);
        }
    }

}
