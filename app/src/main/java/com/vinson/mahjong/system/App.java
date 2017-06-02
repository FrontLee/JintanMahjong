package com.vinson.mahjong.system;

import android.app.Application;
import android.content.Context;

public class App extends Application {

    private static Context instance;

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = getApplicationContext();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(instance);
    }

    public static Context getAppContext() {
        return instance;
    }
    public Context getContext()
    {
        return this;
    }
}
