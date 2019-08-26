package com.lxl.jzh.locker;

import android.content.Context;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import com.example.x6.serial.LockerConstant;

/**
 * 串口是可选的，所以需要记录当前使用的串口地址
 * 波特率是固定为9600的
 */
 class LockerPreferenceUtil {
    private static final String LOCKER_SERIAL_PATH = "locker_serial_path";

    private LockerPreferenceUtil() {
    }

    public static void saveSerialPath(Context context, String path) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(LOCKER_SERIAL_PATH, path)
                .apply();
    }

    public static String getSerialPath(@NonNull Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(LOCKER_SERIAL_PATH, LockerConstant.PATH);

    }
}
