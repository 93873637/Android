package com.liz.tracer;


import android.location.Location;

import androidx.annotation.NonNull;

public class LocationEx extends Location {

    public LocationEx(Location l) {
        super(l);
    }

    public boolean isSameLocation(Location l) {
        if (l == null) {
            return false;
        }
        else {
            return l.getTime() == this.getTime();
        }
    }

    public boolean isDifferentLocation(Location l) {
        return !isSameLocation(l);
    }

    @NonNull
    public String toString() {
        return this.getLongitude()
                + ", " + this.getLatitude()
                + ", " + String.format("%.1f", this.getAltitude())
                + ", " + this.getSpeed()
                + ", " + this.getBearing()
                + ", " + String.format("%.1f", this.getAccuracy())
                + ", " + this.getTime()
                ;
    }
}
