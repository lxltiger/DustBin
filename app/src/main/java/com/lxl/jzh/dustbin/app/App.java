package com.lxl.jzh.dustbin.app;

import android.app.Application;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.lxl.jzh.base.AppExecutors;


public class App extends Application {
    private static final String TAG = "LockerApp";
    private static App app;
    private String deviceId = "";
    private AppExecutors appExecutors;
    private HomeRepository homeRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        deviceId = getUniqueId(this);
        Log.d(TAG, "getUniqueId: " + deviceId);
        appExecutors = new AppExecutors();
        homeRepository=new HomeRepository(appExecutors);
    }





    public static App get() {
        return app;
    }

    public AppExecutors getAppExecutors() {
        return appExecutors;
    }

    public HomeRepository getHomeRepository() {
        return homeRepository;
    }

    public String deviceId() {
        return deviceId;
    }

    private String getUniqueId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

    }

   }
