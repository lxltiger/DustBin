package com.lxl.jzh.dustbin.app;


import com.example.x6.serial.DustBin;
import com.example.x6.serial.LockerService;
import com.lxl.jzh.base.AppExecutors;
import com.lxl.jzh.base.utils.DeviceUtil;
import com.lxl.jzh.dustbin.UsbSerialService;
import com.lxl.jzh.locker.LockServiceProvider;
import com.tencent.mars.xlog.Log;

import java.util.concurrent.atomic.AtomicBoolean;


public class HomeRepository {
    private static final String TAG = "HomeRepository";
    private App lockerApp;
    private AppExecutors appExecutors;
    //
    private LockerService<DustBin> lockerService;
    private UsbSerialService usbSerialService;

    private AtomicBoolean started = new AtomicBoolean(false);

    public HomeRepository(AppExecutors appExecutors) {
        lockerApp = App.get();
        this.appExecutors = appExecutors;
    }

    //    因为需要权限的处理 所以不能在构造函数中直接初始化
    public void initialize() {
        if (started.compareAndSet(false, true)) {
            Log.d(TAG, "initialize: ");
//            DeviceUtil.hideNavigation();

            lockerService = LockServiceProvider.get(lockerApp);

            //超声波测距
            usbSerialService = new UsbSerialService(lockerApp, data -> {
                if (data.length == 4) {
                    int result = data[1] * 256 + data[2];
                }
            });
            usbSerialService.searchUsbSerialToOpen();


        }

    }

    public void release() {
        if (started.compareAndSet(true, false)) {
            Log.d(TAG, "release: ");
            DeviceUtil.showNavigation();

            lockerService.exit();
            lockerService = null;

            usbSerialService.exit();
            usbSerialService = null;

            Log.appenderClose();

        }
    }


    UsbSerialService.Listener usbSerialPortListener = data -> {
//         updateReceivedData(data);
        if (data.length == 4) {
         /*   String s = HexDump.dumpHexString(data);
            Log.d(TAG, "S:= " + s);
            runOnUiThread((Runnable) () -> {
                dataBinding.tvReponse.setText(s);
            });*/

//        int first = data[1];
//        int second = data[2];
            int result = data[1] * 256 + data[2];
            /*runOnUiThread((Runnable) () -> {
                dataBinding.tvDistance.setText("距离" + result);
            });*/
//            Log.d(TAG, "result: " + result);
        }

    };

    public void handleResidualWaste() {
        Log.d(TAG, "handleResidualWaste: open door 1");
        lockerService.handleOpenDoor1();
    }

    public void handleHazardousWaste() {
        Log.d(TAG, "handleResidualWaste: open door 2");
        lockerService.handleOpenDoor2();
    }
}