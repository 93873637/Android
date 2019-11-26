package com.liz.multidialerdaemon.logic;

/**
 * Common Definitions
 * Created by liz on 2018/3/8.
 */

@SuppressWarnings("unused, WeakerAccess")
public class ComDef {

    /**
     *  CONSTANT DEFINITIONS
     */
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public static final String APP_NAME = "MultiDialerDaemon";

    public static final long DAEMON_TIMER_DELAY = 1000L;
    public static final long DAEMON_TIMER_PERIOD = 5000L;

    public static final String LIFE_BROADCAST_MSG = "com.liz.multidialer.LIFE_BROADCAST";
    public static final int MIN_LIFE_COUNT = 0;
    public static final int MAX_LIFE_COUNT = 3;
}
