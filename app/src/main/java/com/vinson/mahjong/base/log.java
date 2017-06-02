package com.vinson.mahjong.base;

import android.util.Log;

public class log {
    public static boolean DEBUG = false;
    public static boolean JUNIT_TEST = false;

    public static void i(String msg) {
        if (JUNIT_TEST) {
            System.out.println(msg);
        } else if (DEBUG) {
            Log.i(Constant.TAG, msg);
        }
    }

    public static void e(String msg, Exception e) {
        if (JUNIT_TEST) {
            System.out.println(msg);
            e.printStackTrace();
        } else if (DEBUG) {
            Log.e(Constant.TAG, msg, e);
        }
    }

    public static void e(String msg) {
        if (JUNIT_TEST) {
            System.out.println(msg);
        } else if (DEBUG) {
            Log.e(Constant.TAG, msg);
        }
    }
}
