package com.github.warren_bank.mock_location.service.looper;

// copied from:
//   https://github.com/xiangtailiang/FakeGPS/blob/V1.1/app/src/main/java/com/github/fakegps/JoyStickManager.java

import com.github.warren_bank.mock_location.data_model.LocPoint;
import com.github.warren_bank.mock_location.data_model.SharedPrefsState;
import com.github.warren_bank.mock_location.event_hooks.IJoyStickPresenter;
import com.github.warren_bank.mock_location.event_hooks.ISharedPrefsListener;
import com.github.warren_bank.mock_location.security_model.RuntimePermissions;
import com.github.warren_bank.mock_location.service.LocationService;
import com.github.warren_bank.mock_location.ui.components.JoyStickView;

import android.content.Context;

public class LocationThreadManager implements IJoyStickPresenter, ISharedPrefsListener {
    private static LocationThreadManager INSTANCE = new LocationThreadManager();

    private Context mContext;
    private JoyStickView mJoyStickView;
    private LocationThread mLocationThread;
    private LocPoint mCurrentLocPoint;
    private LocPoint mOriginLocPoint;
    private LocPoint mTargetLocPoint;
    private int mFlyTime;
    private int mFlyTimeIndex;

    private int mTimeInterval;
    private int mFixedCount;
    private int mFixedCountRemaining;
    private boolean mFixedJoystickEnabled;
    private double mFixedJoystickIncrement;
    private boolean mTripHoldDestination;

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

        mCurrentLocPoint = new LocPoint(locPoint);
        if ((mLocationThread == null) || !mLocationThread.isAlive()) {
            mLocationThread = new LocationThread(mContext, this, mTimeInterval);
            mLocationThread.startThread();
        }

        if (mFixedJoystickEnabled && !mIsFlyMode && RuntimePermissions.canDrawOverlays(mContext)) {
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
        return new LocPoint(mCurrentLocPoint);
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
            return new LocPoint(mCurrentLocPoint);
        }

        if (mFlyTimeIndex >= mFlyTime) {
            jumpToLocation(mTargetLocPoint);
            mFixedCountRemaining = (mTripHoldDestination) ? mFixedCount : -1;
            return new LocPoint(mCurrentLocPoint);
        }
        else {
            float factor = (float) mFlyTimeIndex / (float) mFlyTime;
            double lat = mOriginLocPoint.getLatitude()  + (factor * (mTargetLocPoint.getLatitude()  - mOriginLocPoint.getLatitude()));
            double lon = mOriginLocPoint.getLongitude() + (factor * (mTargetLocPoint.getLongitude() - mOriginLocPoint.getLongitude()));
            mFlyTimeIndex++;
            mCurrentLocPoint.setLatitude(lat);
            mCurrentLocPoint.setLongitude(lon);
            return new LocPoint(mCurrentLocPoint);
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
        mCurrentLocPoint = new LocPoint(location);
    }

    public void flyToLocation(LocPoint location, int trip_duration_seconds) {
        if (mIsStarted && mFixedJoystickEnabled) {
            hideJoyStick();
        }

        mOriginLocPoint = new LocPoint(mCurrentLocPoint);
        mTargetLocPoint = new LocPoint(location);
        mIsFlyMode      = true;
        mFlyTimeIndex   = 0;
        mFlyTime        = convertFlyTime_secondsToLoopIterations(trip_duration_seconds, mTimeInterval);
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

        if (mFixedCount != prefsState.fixed_count) {
            mFixedCount = prefsState.fixed_count;

            if (mIsStarted && !mIsFlyMode) {
                mFixedCountRemaining = mFixedCount;
            }
        }

        if (mTimeInterval != prefsState.time_interval) {
            updateFlyTime(prefsState.time_interval);

            mTimeInterval = prefsState.time_interval;

            if ((mLocationThread != null) && mLocationThread.isAlive()) {
                mLocationThread.updateTimeInterval(mTimeInterval);
            }
        }

        if (mFixedJoystickEnabled != prefsState.fixed_joystick_enabled) {
            mFixedJoystickEnabled = prefsState.fixed_joystick_enabled;

            if (mIsStarted && !mIsFlyMode) {
                if (mFixedJoystickEnabled)
                    showJoyStick();
                else
                    hideJoyStick();
            }
        }

        mFixedJoystickIncrement = prefsState.fixed_joystick_increment;
        mTripHoldDestination    = prefsState.trip_hold_destination;

        /*
        mCurrentLocPoint = new LocPoint(prefsState.trip_origin_lat,      prefsState.trip_origin_lon);
        mOriginLocPoint  = new LocPoint(prefsState.trip_origin_lat,      prefsState.trip_origin_lon);
        mTargetLocPoint  = new LocPoint(prefsState.trip_destination_lat, prefsState.trip_destination_lon);
        mFlyTime         = prefsState.trip_duration;
        */
    }

    // =================================
    // mFlyTime counts the number of loop iterations that occur @ mTimeInterval
    // - when mTimeInterval changes, mFlyTime needs to be recalculated
    // - call this method BEFORE mTimeInterval is changed
    // =================================
    private void updateFlyTime(int new_time_interval) {
        if (!mIsStarted || !mIsFlyMode || (mFlyTimeIndex >= mFlyTime))
            return;

        int remaining_trip_duration_seconds    = convertFlyTime_loopIterationsToSeconds(mFlyTime - mFlyTimeIndex, mTimeInterval);
        int remaining_trip_duration_iterations = convertFlyTime_secondsToLoopIterations(remaining_trip_duration_seconds, new_time_interval);

        mOriginLocPoint = new LocPoint(mCurrentLocPoint);
        mFlyTimeIndex   = 0;
        mFlyTime        = remaining_trip_duration_iterations;
    }

    // =================================
    // static helpers
    // =================================

    private static int convertFlyTime_secondsToLoopIterations(int trip_duration_seconds, int time_interval) {
        // (1 loop iteration / time_interval ms)(1000 ms / 1 sec)(trip_duration_seconds secs)
        return (int) Math.ceil((1000f / time_interval) * trip_duration_seconds);
    }

    private static int convertFlyTime_loopIterationsToSeconds(int trip_duration_iterations, int time_interval) {
        // (time_interval ms / 1 loop iteration)(1 sec / 1000 ms)(trip_duration_iterations loop iterations)
        return (int) Math.ceil((time_interval / 1000f) * trip_duration_iterations);
    }

}
