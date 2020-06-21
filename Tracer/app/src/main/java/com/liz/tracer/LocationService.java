package com.liz.tracer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.liz.androidutils.LocationUtils;
import com.liz.androidutils.LogEx;
import com.liz.androidutils.LogUtils;
import com.liz.androidutils.TimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint({"MissingPermission"})
@SuppressWarnings("unused, WeakerAccess")
public class LocationService {

    private static final int LOCATION_UPDATE_MIN_TIME = 1000;
    private static final float LOCATION_UPDATE_MIN_DISTANCE = 0.5f;

    private static final int LOCATION_CHECK_TIMER_DELAY = 100;
    private static final int LOCATION_CHECK_TIMER_PERIOD = 1000;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private static LocationService inst_ = new LocationService();
    public static LocationService inst() {
        return inst_;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private LocationManager mLocationManager = null;
    private String mLocationProvider = "";
    private LocationCallback mCallback;

    // running parameters
    private ArrayList<LocationEx> mLocationList = new ArrayList<>();  // list of locations on change
    private double mDistanceTotal = 0;
    private long mTimeStart = 0;
    private long mTimeStop = 0;
    private double mMaxSpeed = 0;
    private boolean mIsRunning = false;

    public interface LocationCallback {
        void onLocationUpdate();
    }

    public void init(Context context, LocationCallback callback) {
        if (!checkPermissions(context)) {
            LogEx.e("No location permissions");
            return;
        }

        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager == null) {
            LogEx.e("get location manager failed");
            return;
        }

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        mLocationProvider = mLocationManager.getBestProvider(criteria, true);

//        List<String> providerList = mLocationManager.getProviders(true);
//        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
//            mLocationProvider = LocationManager.GPS_PROVIDER;
//        } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
//            mLocationProvider = LocationManager.NETWORK_PROVIDER;
//        } else {
//            Toast.makeText(context, "No location mLocationProvider", Toast.LENGTH_SHORT).show();
//            LogEx.e("No location mLocationProvider available");
//            return;
//        }

        mCallback = callback;
    }

    public void release() {
        onStop();
    }

    public void switchTracing() {
        if (mIsRunning) {
            onStop();
        } else {
            onStart();
        }
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public String getDistanceText() {
        return LocationUtils.formatDistance(mDistanceTotal);
    }

    public int getBearing() {
        LocationEx location = getLastLocation();
        if (location == null) {
            return 0;
        }
        else {
            return (int)location.getBearing();
        }
    }

    public String getBearingText() {
        String text = "NA";
        LocationEx location = getLastLocation();
        if (location != null) {
            text = "" + (int)location.getBearing();
        }
        return text;
    }

    public String getStartTimeText() {
        if (mTimeStart == 0) {
            return comdef.TIME_RESET_STRING;
        }
        else {
            return TimeUtils.formatTime(mTimeStart);
        }
    }

    public double getAverageSpeed() {
        long duration = getDuration();
        if (duration == 0) {
            return 0;  //just start or param reset
        }
        else {
            return mDistanceTotal * 1000 / duration;
        }
    }

    public String getAverageSpeedText() {
        return LocationUtils.getDualSpeedText(getAverageSpeed());
    }

    public String getMaxSpeedText() {
        return LocationUtils.getDualSpeedText(mMaxSpeed);
    }

    public double getCurrentSpeedRatio() {
        return getCurrentSpeed() / getSpeedMax();
    }

    public double getAverageSpeedRatio() {
        return getAverageSpeed() / getSpeedMax();
    }

    public double getSpeedMax() {
        if (mMaxSpeed < comdef.MIN_SPEED_LIMIT) {
            return comdef.MIN_SPEED_LIMIT;
        }
        else {
            return mMaxSpeed;
        }
    }

    public double getCurrentSpeed() {
        if (!mIsRunning) {
            return 0;
        }
        else {
            LocationEx location = getLastLocation();
            if (location == null) {
                return 0;
            } else {
                return location.getSpeed();
            }
        }
    }

    public String getCurrentSpeedText() {
        return LocationUtils.getDualSpeedText(getCurrentSpeed());
    }

    public String getStatisInfo() {
        return LocationService.inst().getDistanceText()
                + "\n"
                + (LocationService.inst().getDuration() / 1000)
                + "\n"
                + LocationService.inst().getAverageSpeedText()
                + "\n"
                + LocationService.inst().getMaxSpeedText()
                + "\n"
                + LocationService.inst().getCurrentSpeedText()
                + "\n"
                + LocationService.inst().getBearingText()
                ;
    }

    public long getDuration() {
        if (mIsRunning) {
            return System.currentTimeMillis() - mTimeStart;
        }
        else {
            return mTimeStop - mTimeStart;
        }
    }

    public String getDurationText() {
        return TimeUtils.formatDuration(getDuration());
    }

    public String getLastLocationInfo() {
        LocationEx location = getLastLocation();
        if (location == null) {
            return "location null";
        } else {
            return location.toString();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Check Timer
    private Timer mCheckTimer;
    private void setCheckTimer(long timerDelay, long timerPeriod) {
        this.mCheckTimer = new Timer();
        this.mCheckTimer.schedule(new TimerTask() {
            public void run() {
                onCheckTimer();
            }
        }, timerDelay, timerPeriod);
    }
    private void removeCheckTimer() {
        if (this.mCheckTimer != null) {
            this.mCheckTimer.cancel();
            this.mCheckTimer = null;
        }
    }
    // Check Timer
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void resetRunningParameters() {
        mLocationList.clear();
        mDistanceTotal = 0;
        mTimeStart = 0;
        mTimeStop = 0;
        mMaxSpeed = 0;
    }

    private void onStart() {
        if (mIsRunning) {
            LogUtils.td("already started");
        }
        else {
            mIsRunning = true;
            resetRunningParameters();
            mTimeStart = System.currentTimeMillis();
            mLocationManager.requestLocationUpdates(mLocationProvider, LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
            setCheckTimer(LOCATION_CHECK_TIMER_DELAY, LOCATION_CHECK_TIMER_PERIOD);
        }
    }

    private void onStop() {
        if (!mIsRunning) {
            LogUtils.td("already stopped");
        }
        else {
            //removeCheckTimer();
            mLocationManager.removeUpdates(mLocationListener);
            mTimeStop = System.currentTimeMillis();
            mIsRunning = false;
        }
    }

    public void onReset() {
        if (!mIsRunning) {
            resetRunningParameters();
        }
    }

    private void onCheckTimer() {
        Location location = mLocationManager.getLastKnownLocation(mLocationProvider);
        if (location == null) {
            LogUtils.td("last known location null");
        }
        else {
            LogUtils.td(new LocationEx(location).toString() + ", current time = " + System.currentTimeMillis());
        }
    }

    private boolean isLocationChanged(Location l) {
        if (l == null) {
            return false;
        }
        LocationEx lastLoc = getLastLocation();
        if (lastLoc == null) {
            return true;
        } else {
            return lastLoc.isDifferentLocation(l);
        }
    }

    private boolean checkPermissions(Context context) {
        return (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void onLocationChanged(Location location) {
        LogUtils.trace();
        LocationEx lastLocation = getLastLocation();
        LocationEx newLocation = new LocationEx(location);
        mLocationList.add(newLocation);

        if (lastLocation != null) {
            double distance = LocationUtils.getDistance(newLocation, lastLocation);
            mDistanceTotal += distance;
        }

        if (newLocation.getSpeed() > mMaxSpeed) {
            mMaxSpeed = newLocation.getSpeed();
        }

        // save location info to log file
        LogEx.i(getLastLocationInfo());

        if (mCallback != null) {
            mCallback.onLocationUpdate();
        }
    }

    private LocationEx getLastLocation() {
        if (mLocationList.size() > 0) {
            return mLocationList.get(mLocationList.size() - 1);
        } else {
            return null;
        }
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            LocationService.this.onLocationChanged(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            LogEx.trace();
        }

        @Override
        public void onProviderEnabled(String provider) {
            LogEx.trace();
        }

        @Override
        public void onProviderDisabled(String provider) {
            LogEx.trace();
        }
    };
}
