package com.liz.whatsai.logic;

/**
 * RemindTime:
 * Created by liz on 2018/10/23.
 */

@SuppressWarnings("WeakerAccess")
public class RemindTime {
    public int year;
    public int month;
    public int day;
    public int hour;
    public int minute;
    public int second;

    RemindTime() {
        year = 0;
        month = 0;
        day = 0;
        hour = 0;
        minute = 0;
        second = 0;
    }

    public String timeFormatString() {
        String timeString = "";

        if (hour < 10) {
            timeString += "0" + hour;
        }
        else {
            timeString += hour;
        }

        if (minute < 10) {
            timeString += "0" + minute;
        }
        else {
            timeString += minute;
        }

        if (second < 10) {
            timeString += "0" + second;
        }
        else {
            timeString += second;
        }

        return timeString;
    }

    public int compareTo(RemindTime remindTime) {
        if (this.year < remindTime.year) {
            return ComDef.TIME_BEFORE;
        }
        if (this.year > remindTime.year) {
            return ComDef.TIME_AFTER;
        }

        if (this.month < remindTime.month) {
            return ComDef.TIME_BEFORE;
        }
        if (this.month > remindTime.month) {
            return ComDef.TIME_AFTER;
        }

        if (this.day < remindTime.day) {
            return ComDef.TIME_BEFORE;
        }
        if (this.day > remindTime.day) {
            return ComDef.TIME_AFTER;
        }

        if (this.hour < remindTime.hour) {
            return ComDef.TIME_BEFORE;
        }
        if (this.hour > remindTime.hour) {
            return ComDef.TIME_AFTER;
        }

        if (this.minute < remindTime.minute) {
            return ComDef.TIME_BEFORE;
        }
        if (this.minute > remindTime.minute) {
            return ComDef.TIME_AFTER;
        }

        if (this.second < remindTime.second) {
            return ComDef.TIME_BEFORE;
        }
        if (this.second > remindTime.second) {
            return ComDef.TIME_AFTER;
        }

        //take equal as before
        return ComDef.TIME_SAME;
    }
}
