package com.liz.whatsai.logic;

/**
 * Reminder:
 * Created by liz on 2018/10/22.
 */

public class Reminder {
    public static void checkRemind(Node node) {
        switch (node.getRemindType()) {
            case ComDef.REMIND_TYPE_DAILY_TIME:
                //####@: SysUtils.setDailyAlarm(MyApp.getAppContext(), node.getName(), node.remindTime.hour, node.remindTime.minute, node.remindTime.second);
                DataLogic.registerAlarm(node);
                break;
            default:
                break;
        }
    }
}
