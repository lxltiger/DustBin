package com.lxl.jzh.dustbin.app;

import android.Manifest;
import android.app.Activity;
import android.os.Environment;

import androidx.lifecycle.LifecycleOwner;

import com.lxl.jzh.base.BaseLifecycleObserver;
import com.lxl.jzh.base.BuildConfig;
import com.lxl.jzh.base.HomeListener;
import com.lxl.jzh.base.UsbStatusHandler;
import com.tencent.mars.xlog.Log;
import com.tencent.mars.xlog.Xlog;

public class DustbinObserver extends BaseLifecycleObserver {
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public DustbinObserver(HomeListener homeListener, UsbStatusHandler usbStatusHandler, LifecycleOwner lifecycleOwner, Activity activity) {
        super(homeListener, usbStatusHandler, lifecycleOwner, activity);
    }

    @Override
    protected void setupLog() {
        final String logPath = Environment.getExternalStorageDirectory().getPath() + "/xlog";
        final String cachePath = context.getFilesDir() + "/xlog";
        final String PUB_KEY = "";
        String prefix = String.format("%s_%s",App.get().deviceId(), context.getPackageName());
        //cacheDay设为0，不然会长时间缓存在内存中不会写到磁盘上 这是旧版本 没有cacheDay
        if (BuildConfig.DEBUG) {
            Xlog.open(true, Xlog.LEVEL_DEBUG, Xlog.AppednerModeAsync, cachePath, logPath, prefix, PUB_KEY);
            Xlog.setConsoleLogOpen(true);
        } else {
            Xlog.open(true, Xlog.LEVEL_DEBUG, Xlog.AppednerModeAsync, cachePath, logPath, prefix, PUB_KEY);
            Xlog.setConsoleLogOpen(false);
        }
        Log.setLogImp(new Xlog());
    }

    @Override
    protected String[] getNeededPermissions() {
        return NEEDED_PERMISSIONS;
    }

}
