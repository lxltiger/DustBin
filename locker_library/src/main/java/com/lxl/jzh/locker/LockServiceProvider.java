package com.lxl.jzh.locker;

import android.content.Context;

import com.example.x6.serial.Grid;
import com.example.x6.serial.GridListener;
import com.example.x6.serial.LockerService;

public class LockServiceProvider {


    private LockServiceProvider() {
    }


    public static<T extends Grid> LockerService<T> get( Context context) {
        return new LockerManager<>(context);
    }
}
