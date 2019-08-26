package com.lxl.jzh.dustbin;

import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/*
 * 负责和串口USB通讯
 *
 * */
public class UsbSerialService {
    private static final String TAG = "UsbSerialService";
    private UsbManager mUsbManager;
    private UsbSerialPort usbSerialPort;
    private SerialInputOutputManager mSerialIoManager;
    private Context application;
    private static final String ACTION_USB_PERMISSION = "com.android.usb.USB_PERMISSION";
    private Executor executor = Executors.newSingleThreadExecutor();
    private  Listener listener;

    public UsbSerialService(Context context, Listener listener) {
        this.application = context.getApplicationContext();
        this.listener = Objects.requireNonNull(listener);
        mUsbManager = (UsbManager) application.getSystemService(Context.USB_SERVICE);

    }

    public Context getApplication() {
        return application;
    }

    public void searchUsbSerialToOpen() {
        final List<UsbSerialDriver> drivers =
                UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);

        final List<UsbSerialPort> result = new ArrayList<>();
        for (final UsbSerialDriver driver : drivers) {
            final List<UsbSerialPort> ports = driver.getPorts();
            result.addAll(ports);
        }
        if (result.size() > 0) {
            usbSerialPort = result.get(0);
            UsbDevice device = usbSerialPort.getDriver().getDevice();
            if (mUsbManager.hasPermission(device)) {
                openDevice(device);
            } else {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplication(), 0, new Intent(ACTION_USB_PERMISSION), 0);
                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                getApplication().registerReceiver(mUsbReceiver, filter);
                mUsbManager.requestPermission(device, pendingIntent);
            }
        } else {
            Log.e(TAG, "searchUsbSerialToOpen: 没有找到设备");
        }
    }

    private void openDevice(UsbDevice device) {
        UsbDeviceConnection connection = mUsbManager.openDevice(device);
        if (connection == null) {
            Log.d(TAG, "Opening device failed");
            return;
        }

        try {
            usbSerialPort.open(connection);
//            usbSerialPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            usbSerialPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

        } catch (IOException e) {
            Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
            try {
                usbSerialPort.close();
            } catch (IOException e2) {
                // Ignore.
            }
            usbSerialPort = null;
            return;
        }

        onDeviceStateChange();
    }

    private void startIoManager() {
        if (usbSerialPort != null) {
            Log.d(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(usbSerialPort, mListener);
            executor.execute(mSerialIoManager);

        }
    }

    private ByteBuffer byteBuffer = ByteBuffer.allocate(16);

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    if (data != null) {
                        final String message = "Read " + data.length + " bytes: \n" + HexDump.dumpHexString(data) + "\n\n";
                        Log.d(TAG, "updateReceivedData: " + message);
                        if ('T' == data[0] || 'F' == data[0]) {
                            listener.callBack(data);
                        } else if ('@' == data[0] || '+' == data[0]) {
                            byteBuffer.clear();
                            byteBuffer.put(data);
                            Log.d(TAG, "onNewData: " + byteBuffer.position());
                            if (byteBuffer.position() == 10) {
                                listener.callBack(byteBuffer.array());
                            }
                        } else if ((byte)0xff == data[0]) {
                            listener.callBack(data);
                        } else {
                            byteBuffer.put(data);
                            Log.d(TAG, "onNewData: " + byteBuffer.position());
                            if (byteBuffer.position() == 10) {
                                listener.callBack(byteBuffer.array());
                            }
                        }

                    }
                }
            };


    public void send(byte[] bytes) {
        if (mSerialIoManager != null) {
            mSerialIoManager.writeAsync(bytes);
        }

    }


    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }


    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.w(TAG, "receiver action: " + action);
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    getApplication().unregisterReceiver(mUsbReceiver);
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            openDevice(device);
                        }
                    } else {
                        Log.e(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

    public void exit() {
        stopIoManager();
        if (usbSerialPort != null) {
            try {
                usbSerialPort.close();
            } catch (IOException e) {
                // Ignore.
            }
            usbSerialPort = null;
        }

        if (listener != null) {
            listener=null;
        }


    }

    public interface Listener {
        void callBack(byte[] data);
    }

}
