package com.lxl.jzh.locker;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.example.x6.serial.DustBin;
import com.example.x6.serial.Grid;
import com.example.x6.serial.LockerService;
import com.example.x6.serial.SerialPort;
import com.tencent.mars.xlog.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Objects;

import static com.example.x6.serial.LockerConstant.BAUDRATE;


/*
 * 每次返回主页面查询柜子的开关情况 更新状态
 * 打开柜子的时候，如何柜门被压住导致打开失败返回的操作码会从02变成03
 * T 格口接口的实现类
 * */
class LockerManager<T extends Grid> implements LockerService<T> {
    private static final String TAG = "LockManager";
    //锁控板操作验证时长，当执行一个锁控板操作的同时发送一个延迟的失败消息到队列，如果在这间隔内锁控板没有反应，这个失败的消息就会执行
    private static final int MSG_CHECK_DELAY = 1000;

    private SerialPort mSerialPort;
    private boolean isStart = false;
    private volatile OutputStream outputStream;
    private volatile InputStream inputStream;
    private ReadThread reader;
    private LockerHandler handler;
    //是否正在关闭 需要检测异常
    private volatile boolean isDoor1Closing = false;
    private volatile boolean isDoor2Closing = false;
    //门1 是否在操作，从开始开门到停止关门都是在操作，此时间段不允许再次触发同样操作
    private volatile boolean isDoor1Operating = false;
    //同门1
    private volatile boolean isDoor2Operating = false;
    //对格口操作结果的监听，主要是对状态的监听
//    private GridListener<T> gridListener;
    private Context context;
    private int stuck_door1_times = 0;
    private int stuck_door2_times = 0;
    private DustBin door1 = new DustBin("1", "01050000ff008c3a", "010500000000cdca", "01050001ff00ddfa", "0105000100009c0a");
    private DustBin door2 = new DustBin("2", "01050002ff002Dfa", "0105000200006c0a", "01050003ff007c3a", "0105000300003dca");


    /**
     * 缓存锁控板返回的字节数组
     */
    private byte[] buffer = new byte[64];


    //我们在构造函数中开启了线程，在项目启动的时候只初始化一次，并在应用结束的时候释放资源
    public LockerManager(/*GridListener<T> listener,*/ @NonNull Context context) {
        Log.d(TAG, "LockerManager: ");
        this.context = Objects.requireNonNull(context).getApplicationContext();
//        gridListener = listener;
        HandlerThread handlerThread = new HandlerThread("locker_service");
        handlerThread.start();
        handler = new LockerHandler(handlerThread.getLooper(), this);
    }


    /**
     * 打开垃圾桶的门
     *
     * @param
     */
    public void handleOpenDoor1() {
        if (isDoor1Operating) {
            return;
        }
        isDoor1Operating=true;
        isDoor1Closing = false;
        if (startSerialPortIfNeed()) {
            Log.d(TAG, "handleOpenDoor1: open");
            handler.sendEmptyMessage(LockerHandler.MSG_DOOR1_START_OPEN);
        }
    }

    public void handleOpenDoor2() {
        if (isDoor2Operating) {
            return;
        }
        isDoor2Operating=true;
        isDoor2Closing = false;
        if (startSerialPortIfNeed()) {
            Log.d(TAG, "handleOpenDoor2: open");
            handler.sendEmptyMessage(LockerHandler.MSG_DOOR2_START_OPEN);
        }
    }


/*    @Override
    public void startOpen(T grid) {
        isDoor1Closing = false;
        if (null == grid) return;
        if (startSerialPortIfNeed()) {
            handler.post(() -> executeOpen(grid.startOpenCommand()));
        }
    }*/

   /* @Override
    public void stopOpen(T grid) {
        if (null == grid) return;
        if (startSerialPortIfNeed()) {
            handler.post(() -> executeOpen(grid.stopOpenCommand()));
        }
    }*/

    /*@Override
    public void startClose(T grid) {
        isDoor1Closing = false;
        if (null == grid) return;
        if (startSerialPortIfNeed()) {
            handler.post(() -> executeOpen(grid.startCloseCommand()));
            isDoor1Closing = true;
            //检测关门结果
            handler.sendEmptyMessageDelayed(LockerHandler.MSG_KEEP_CHECK, 500);
        }
    }*/

   /* @Override
    public void stopClose(T grid) {
        isDoor1Closing = false;
        if (null == grid) return;
        if (startSerialPortIfNeed()) {
            handler.post(() -> executeOpen(grid.stopCloseCommand()));
        }
    }*/

    @Override
    public void check(String command) {
        if (startSerialPortIfNeed()) {
            handler.post(() -> execute(command));
        }
    }


    //停止当前可能的耗时操作
    @Override
    public void stopCurrentOperation() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    /**
     */
    private static class LockerHandler extends Handler {
        private static final int MSG_KEEP_CHECK = 0;
        private static final int DELAY = 10 * 1000;

        private static final int REPEAT_OPEN = 1;
        private static final int REPEAT_QUERY = 3;
        private static final int MSG_FAIL_TO_OPEN = 4;
        private static final int MSG_FAIL_TO_QUERY = 5;
        private static final int MSG_DOOR1_START_OPEN = 6;
        private static final int MSG_DOOR1_STOP_OPEN = 7;
        private static final int MSG_DOOR1_START_CLOSE = 8;
        private static final int MSG_DOOR1_STOP_CLOSE = 9;

        private static final int MSG_DOOR2_START_OPEN = 10;
        private static final int MSG_DOOR2_STOP_OPEN = 11;
        private static final int MSG_DOOR2_START_CLOSE = 12;
        private static final int MSG_DOOR2_STOP_CLOSE = 13;

        private WeakReference<LockerManager> reference;

        public LockerHandler(Looper looper, LockerManager lockHelper) {
            super(looper);
            this.reference = new WeakReference<>(lockHelper);

        }

        public void clear() {
            reference.clear();
        }

        @Override
        public void handleMessage(Message msg) {
            LockerManager lockHelper = reference.get();
            if (lockHelper == null) {
                return;
            }
            switch (msg.what) {
                case MSG_DOOR1_START_OPEN:
                    Log.d(TAG, "handleMessage: MSG_DOOR1_START_OPEN");
                    lockHelper.execute(lockHelper.door1.startOpenCommand());
                    sendEmptyMessageDelayed(MSG_DOOR1_STOP_OPEN, DELAY);
                    break;
                case MSG_DOOR1_STOP_OPEN:
                    Log.d(TAG, "handleMessage: MSG_DOOR1_STOP_OPEN");
                    lockHelper.execute(lockHelper.door1.stopOpenCommand());
                    if (lockHelper.stuck_door1_times++ < 3) {
                        sendEmptyMessageDelayed(MSG_DOOR1_START_CLOSE, DELAY);
                    }
                    break;
                case MSG_DOOR1_START_CLOSE:
                    Log.d(TAG, "handleMessage: MSG_DOOR1_START_CLOSE");
                    //开始关门1
                    lockHelper.isDoor1Closing = true;
                    lockHelper.execute(lockHelper.door1.startCloseCommand());
//                    需要检测是否夹手
                    if (!hasMessages(MSG_KEEP_CHECK)) {
                        sendEmptyMessageDelayed(MSG_KEEP_CHECK, 500);
                    }
                    sendEmptyMessageDelayed(MSG_DOOR1_STOP_CLOSE, DELAY);
                    break;
                case MSG_DOOR1_STOP_CLOSE:
                    Log.d(TAG, "handleMessage: MSG_DOOR1_STOP_CLOSE");
                    lockHelper.isDoor1Closing = false;
                    lockHelper.isDoor1Operating=false;
                    lockHelper.stuck_door1_times=0;
                    lockHelper.execute(lockHelper.door1.stopCloseCommand());
                    break;
                case MSG_DOOR2_START_OPEN:
                    Log.d(TAG, "handleMessage: MSG_DOOR2_START_OPEN");
                    lockHelper.execute(lockHelper.door2.startOpenCommand());
                    sendEmptyMessageDelayed(MSG_DOOR2_STOP_OPEN, DELAY);
                    break;
                case MSG_DOOR2_STOP_OPEN:
                    Log.d(TAG, "handleMessage: MSG_DOOR2_STOP_OPEN");
                    lockHelper.execute(lockHelper.door2.stopOpenCommand());
                    if (lockHelper.stuck_door2_times++ < 3) {
                        sendEmptyMessageDelayed(MSG_DOOR2_START_CLOSE, DELAY);
                    }
                    break;
                case MSG_DOOR2_START_CLOSE:
                    Log.d(TAG, "handleMessage: MSG_DOOR2_START_CLOSE");
                    lockHelper.isDoor2Closing = true;
                    lockHelper.execute(lockHelper.door2.startCloseCommand());
                    if (!hasMessages(MSG_KEEP_CHECK)) {
                        sendEmptyMessageDelayed(MSG_KEEP_CHECK, 500);
                    }
                    sendEmptyMessageDelayed(MSG_DOOR2_STOP_CLOSE, DELAY);
                    break;
                case MSG_DOOR2_STOP_CLOSE:
                    Log.d(TAG, "handleMessage: MSG_DOOR2_STOP_CLOSE");
                    lockHelper.isDoor2Closing = false;
                    lockHelper.isDoor2Operating=false;
                    lockHelper.stuck_door2_times=0;
                    lockHelper.execute(lockHelper.door2.stopCloseCommand());
                    break;
                case MSG_KEEP_CHECK:
                    Log.d(TAG, "MSG_KEEP_CHECK");
                    lockHelper.keepCheck();
                    break;
            }
        }

    }


    private void keepCheck() {
        if (isDoor1Closing || isDoor2Closing) {
            execute("01020000000479c9");
        }
    }


    //当更换串口地址，会停止串口服务，这样再启动的时候获取的就是最新的串口地址
    @Override
    public void stopLockerService() {
        stopCurrentOperation();
        if (isStart) {
            if (reader != null) {
                reader.stopRead();
                reader = null;
            }
        }
    }


    //启动之前需要从本地获取串口配置信息
    private void startSerialPort() throws IOException,SecurityException {
        String path = LockerPreferenceUtil.getSerialPath(context);
        mSerialPort = new SerialPort(new File(path), BAUDRATE, 0);
        outputStream = mSerialPort.getOutputStream();
        inputStream = mSerialPort.getInputStream();
        isStart = true;
        reader = new ReadThread();
        reader.start();
        Log.d(TAG, "locker service startSerialPort: ");
    }


    @Override
    public void exit() {
        stopLockerService();
        if (handler != null) {
            handler.clear();
            handler.getLooper().quitSafely();
        }
    }


    /**
     * 如果串口没启动的话，先启动，否则直接返回true
     * 注意：这是一个同步操作，开锁或查询操作依赖于此
     *
     * @return 如果串口打开成功返回true  否则返回false
     */
    private boolean startSerialPortIfNeed() {
        boolean succeed = true;
        if (!isStart) {
            try {
                startSerialPort();
            } catch (IOException e) {
                succeed = false;
                Log.d(TAG, "startSerialPortIfNeed: io exception");
                e.printStackTrace();
            } catch (SecurityException e) {
                succeed=false;
                Log.d(TAG, "startSerialPortIfNeed: SecurityException exception");
            }
        }
        return succeed;
    }


    /**
     * 执行开锁命令
     * 同时发送一个开锁失败的延迟消息到队列
     *
     * @param command 开锁命令
     */
    private void executeOpen(String command) {
        try {
            if (mSerialPort != null && outputStream != null) {
                Log.d(TAG, "executeOpen: " + command);
                outputStream.write(hexToByteArr(command));
//                handler.sendMessageDelayed(handler.obtainMessage(LockerHandler.MSG_FAIL_TO_OPEN, command), MSG_CHECK_DELAY);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void execute(String command) {
        try {
            if (mSerialPort != null && outputStream != null) {
                Log.d(TAG, "execute: " + command);
                outputStream.write(hexToByteArr(command));
//                handler.sendMessageDelayed(handler.obtainMessage(LockerHandler.MSG_FAIL_TO_OPEN, command), MSG_CHECK_DELAY);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //1字节转2个Hex字符
    private String byte2Hex(Byte inByte) {
        return String.format("%02x", inByte).toUpperCase();
    }

    //hex字符串转字节数组
    private byte[] hexToByteArr(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (isOdd(hexlen) == 1) {//奇数
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {//偶数
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = HexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    private int isOdd(int num) {
        return num & 0x1;
    }

    //Hex字符串转byte
    private byte HexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }

    private class ReadThread extends Thread {

        private StringBuilder stringBuilder = new StringBuilder();
        private boolean exit = false;

        private void stopRead() {
            exit = true;
            super.interrupt();
        }

        @Override
        public void run() {
            while (!exit) {
                if (inputStream == null) {
                    break;
                }
                try {
                    //当没有数据的时候就休眠，这样不至于没有数据的时候阻塞线程
                    if (inputStream.available() > 0) {
                        int size = inputStream.read(buffer);
                        if (size > 0) {
                            for (int i = 0; i < size; i++) {
                                String hex = byte2Hex(buffer[i]);
                                stringBuilder.append(hex);
                            }
                            Log.d(TAG, "response: " + stringBuilder.toString());
                            if (stringBuilder.length() >= 12) {
                                handleResponse(stringBuilder.toString());
//                                gridListener.handleResponse(stringBuilder.toString());
                                //如果关门检测无障碍则需要继续检测
                                if ("01020100A188".equals(stringBuilder.toString())) {
                                    handler.sendEmptyMessageDelayed(LockerHandler.MSG_KEEP_CHECK, 500);
                                }
                                stringBuilder.setLength(0);
                            }
                        }
                    } else {
                        sleep(100);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Log.d(TAG, "read: Interrupted");

                }
            }
            isStart = false;

            if (outputStream != null) {
                try {
                    outputStream.close();
//                    outputStream=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
//                    inputStream=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (mSerialPort != null) {
                mSerialPort.close();
                mSerialPort = null;
            }

            Log.d(TAG, "run: end");

        }

    }

    private void handleResponse(String response) {
        if (12 == response.length()) {
            String code = response.substring(6, 8);
            switch (code) {
                case "02":
                    //1门关被卡 停止关门 执行开门操作 20s后继续关
                    onDoor1Stuck();
                    break;
                //2门被卡
                case "08":
                    onDoor2Stuck();
                    break;
                //1和2门都被卡
                case "0a":
                    onDoor1Stuck();
                    onDoor2Stuck();
                    break;
            }

        }
    }

    //门1卡住了，停止关门，然后开门 20秒后再关门 持续3次
    private void onDoor1Stuck() {
        Log.d(TAG, "onDoor1Stuck: ");
//        dataBinding.tvReponse.append(" 门1卡住了");
        //需要立马停止关门 先把延迟消息移除，改成立马执行
        handler.removeMessages(LockerHandler.MSG_DOOR1_STOP_CLOSE);
//        dataBinding.tvReponse.append(" 先停止关门1");
        handler.post(()->{ execute(door1.stopCloseCommand());});
        isDoor1Operating=false;
        handler.postDelayed(this::handleOpenDoor1, 500);



    }

    private void onDoor2Stuck() {
        Log.d(TAG, "onDoor2Stuck: ");
//        dataBinding.tvReponse.append(" 门2卡住了");
        //需要立马停止关门 先把延迟消息移除，改成立马执行
        handler.removeMessages(LockerHandler.MSG_DOOR2_STOP_CLOSE);
//        dataBinding.tvReponse.append(" 先停止关门2");
        handler.post(()->{ execute(door2.stopCloseCommand());});
        isDoor2Operating=false;
        handler.postDelayed(this::handleOpenDoor2, 500);
    }

}

