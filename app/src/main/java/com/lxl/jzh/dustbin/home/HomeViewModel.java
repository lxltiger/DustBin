package com.lxl.jzh.dustbin.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.lxl.jzh.base.HomeListener;
import com.lxl.jzh.base.UsbStatusHandler;
import com.lxl.jzh.dustbin.app.App;
import com.lxl.jzh.dustbin.app.HomeRepository;

public class HomeViewModel extends AndroidViewModel implements HomeListener, UsbStatusHandler {
    private HomeRepository homeRepository;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        homeRepository = ((App) application).getHomeRepository();

    }

    public void handleResidualWaste() {
        homeRepository.handleResidualWaste();
    }

    public void handleHazardousWaste() {
        homeRepository.handleHazardousWaste();

    }

    @Override
    public void initialize() {
        homeRepository.initialize();
    }

    @Override
    public void release() {
        homeRepository.release();
    }

    @Override
    public void handleDetached() {

    }

    @Override
    public void handleAttached() {

    }
}
