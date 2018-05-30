package com.kekland.voicedetection;

/**
 * Created by kkerz on 30-May-18.
 */

public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "com.kekland.voicedetection.action.main";
        public static String STARTFOREGROUND_ACTION = "com.kekland.voicedetection.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.kekland.voicedetection.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
