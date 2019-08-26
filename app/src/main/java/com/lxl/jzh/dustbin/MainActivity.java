package com.lxl.jzh.dustbin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.example.x6.serial.DustBin;
import com.example.x6.serial.GridListener;
import com.example.x6.serial.LockerService;
import com.lxl.jzh.dustbin.databinding.ActivityMainBinding;
import com.lxl.jzh.locker.LockServiceProvider;

//使用的串口地址为/dev/ttymxc4
//Demo 演示页面
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int DELAY = 10;
    private LockerService<DustBin> lockerService;
    private DustBin door1;
    private DustBin door2;
    private int stuck_door1_times = 0;
    private int stuck_door2_times = 0;

    private Handler handler = new Handler();
    private ActivityMainBinding dataBinding;
    private UsbSerialService usbSerialService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

//        dataBinding.setDelay(DELAY);
        lockerService = LockServiceProvider.get(getApplicationContext());
        door1 = new DustBin("1", "01050000ff008c3a", "010500000000cdca", "01050001ff00ddfa", "0105000100009c0a");
        door2 = new DustBin("2", "01050002ff002Dfa", "0105000200006c0a", "01050003ff007c3a", "0105000300003dca");
        //超声波测距
        usbSerialService = new UsbSerialService(getApplicationContext(), usbSerialPortListener);
        usbSerialService.searchUsbSerialToOpen();
        registerUSBDetachReceiver();

    }

    //USB被拔掉的广播
    private void registerUSBDetachReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(detachReceiver, filter);
    }

    private GridListener<DustBin> listener = new GridListener<DustBin>() {
        @Override
        public void updateStatus(DustBin dustBin) {

        }

        @Override
        public void handleResponse(String response) {
            if (12 == response.length()) {
                String code = response.substring(6, 8);
                switch (code) {
                    case "02":
                        //1门关被卡 停止关门 执行开门操作 20s后继续关
                        handler.post(() -> {
                            onDoor1Stuck();
                        });
                        break;
                    //2门被卡
                    case "08":
                        handler.post(() -> {
                            onDoor2Stuck();
                        });
                        break;
                    //1和2门都被卡
                    case "0a":
                        handler.post(() -> {
                            onDoor1Stuck();
                            onDoor2Stuck();
                        });
                        break;
                }

            }

        }
    };

    //门1卡住了，停止关门，然后开门 20秒后再关门 持续3次
    private void onDoor1Stuck() {
        dataBinding.tvReponse.append(" 门1卡住了");
        handler.removeCallbacks(stopCloseDoor1Runnable);
        dataBinding.tvReponse.append(" 先停止关门1");
//        lockerService.stopClose(door1);

        handler.postDelayed(() -> {
            dataBinding.tvReponse.append(" 开始开门1");
//            lockerService.startOpen(door1);
        }, 500);

        handler.postDelayed(stopOpenDoor1Runnable, DELAY * 1000);
        if (stuck_door1_times++ < 3) {
            handler.postDelayed(() -> {
                door1_close(null);
            }, 20 * 1000);
        } else {
            dataBinding.tvReponse.append(" 已卡3次放弃关门1");

        }
    }

    private void onDoor2Stuck() {
        dataBinding.tvReponse.append(" 门2卡住了");
        handler.removeCallbacks(stopCloseDoor2Runnable);
        dataBinding.tvReponse.append("  先停止关门2");
//        lockerService.stopClose(door2);

        handler.postDelayed(() -> {
            dataBinding.tvReponse.append(" 开始开门2");
//            lockerService.startOpen(door2);
        }, 500);

        handler.postDelayed(stopOpenDoor2Runnable, DELAY * 1000);
        if (stuck_door2_times++ < 3) {

            handler.postDelayed(() -> {
                door2_close(null);
            }, 20 * 1000);
        } else {
            dataBinding.tvReponse.append(" 已卡3次放弃关门2");

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        USB插上的广播
//        homeViewModel.onNewIntent(intent);

    }

    BroadcastReceiver detachReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
                Log.d(TAG, "onOperationMsg: detach");
                /*UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    Log.d(TAG, "onOperationMsg: " + device.toString());
                }*/
            }

        }
    };

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
            runOnUiThread((Runnable) () -> {
                dataBinding.tvDistance.setText("距离" + result);
            });
//            Log.d(TAG, "result: " + result);
        }

    };


    private Runnable stopOpenDoor1Runnable = new Runnable() {
        @Override
        public void run() {
            dataBinding.tvReponse.append(" 停止开门1");
//            lockerService.stopOpen(door1);

        }
    };
    private Runnable stopOpenDoor2Runnable = new Runnable() {
        @Override
        public void run() {
            dataBinding.tvReponse.append(" 停止开门2");

//            lockerService.stopOpen(door2);

        }
    };

    private Runnable stopCloseDoor1Runnable = new Runnable() {
        @Override
        public void run() {
            dataBinding.tvReponse.append(" 停止关门1");
//            lockerService.stopClose(door1);
            stuck_door1_times = 0;
            Log.d(TAG, "run: stop close door 1");

        }
    };

    private Runnable stopCloseDoor2Runnable = new Runnable() {
        @Override
        public void run() {
            dataBinding.tvReponse.append(" 停止关门2");
//            lockerService.stopClose(door2);
            //关闭成功，恢复正常  3次关闭失败 放弃关闭
            stuck_door2_times = 0;

            Log.d(TAG, "run: stop close door 2");
        }
    };

    public void door1_open(View view) {
        dataBinding.tvReponse.append(" 开门1操作");
        dataBinding.door1CountDown.setBase(DELAY);
        dataBinding.door1CountDown.start();

        handler.removeCallbacks(stopCloseDoor1Runnable);
        dataBinding.tvReponse.append(" 先停止关门1");
//        lockerService.stopClose(door1);

        handler.postDelayed(() -> {
            dataBinding.tvReponse.append(" 开始开门1");
//            lockerService.startOpen(door1);
        }, 500);

        handler.postDelayed(stopOpenDoor1Runnable, DELAY * 1000);


    }

    public void door1_close(View view) {
        dataBinding.tvReponse.append(" 关门1操作");
        dataBinding.door1CountDown.setBase(DELAY);
        dataBinding.door1CountDown.start();

        handler.removeCallbacks(stopOpenDoor1Runnable);
        dataBinding.tvReponse.append(" 先停止开门1");
//        lockerService.stopOpen(door1);

        handler.postDelayed(() -> {
            dataBinding.tvReponse.append(" 开始关门1");
//            lockerService.startClose(door1);
        }, 500);
        handler.postDelayed(stopCloseDoor1Runnable, DELAY * 1000);

    }


    public void detect(View view) {
        byte[] data = new byte[1];
        data[0] = 0x01;
        usbSerialService.send(data);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(detachReceiver);
        usbSerialService.exit();
    }

    public void door2_open(View view) {
        dataBinding.tvReponse.append(" 开门2操作");

        dataBinding.door2CountDown.setBase(DELAY);
        dataBinding.door2CountDown.start();

        handler.removeCallbacks(stopCloseDoor2Runnable);
        dataBinding.tvReponse.append("  先停止关门2");
//        lockerService.stopClose(door2);

        handler.postDelayed(() -> {
            dataBinding.tvReponse.append(" 开始开门2");
//            lockerService.startOpen(door2);
        }, 500);

        handler.postDelayed(stopOpenDoor2Runnable, DELAY * 1000);

    }

    public void door2_close(View view) {
        dataBinding.tvReponse.append(" 关门2操作");
        dataBinding.door2CountDown.setBase(DELAY);
        dataBinding.door2CountDown.start();

        handler.removeCallbacks(stopOpenDoor2Runnable);
        dataBinding.tvReponse.append(" 先停止开门2");
//        lockerService.stopOpen(door2);

        handler.postDelayed(() -> {
            dataBinding.tvReponse.append(" 开始关门2");
//            lockerService.startClose(door2);
        }, 500);
        handler.postDelayed(stopCloseDoor2Runnable, DELAY * 1000);
    }

    public void clearLog(View view) {
        dataBinding.tvReponse.setText("");
    }
}
