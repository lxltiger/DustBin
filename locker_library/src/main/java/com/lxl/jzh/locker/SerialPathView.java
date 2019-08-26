package com.lxl.jzh.locker;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;

import com.example.x6.serial.SerialPortFinder;

import java.util.Arrays;

/**
 * 显示当前选择的串口，本地没有使用默认ttyS4
 * 可以选择当前设备所有可用的串口地址，并保存选择到本地
 */
public class SerialPathView extends AppCompatSpinner {
    private static final String TAG = "SerialPathView";
    private OnSerialPathChangeListener serialPathChangeListener;

    //调用父类方法 否则会漏掉某些设置
    public SerialPathView(Context context) {
        super(context, null);
        setUpSerialPort();

    }

    public SerialPathView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setUpSerialPort();

    }

    public SerialPathView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUpSerialPort();
    }

    public void setSerialPathChangeListener(OnSerialPathChangeListener serialPathChangeListener) {
        this.serialPathChangeListener = serialPathChangeListener;
    }

    private void setUpSerialPort() {
        SerialPortFinder serialPortFinder = new SerialPortFinder();
        //查询所有串口地址
        String[] allDevicesPath = serialPortFinder.getAllDevicesPath();
        //当前的串口地址
        String path = LockerPreferenceUtil.getSerialPath(getContext());
        int index = Arrays.asList(allDevicesPath).indexOf(path);
        ArrayAdapter<String> serialPortAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, allDevicesPath);
        serialPortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.setAdapter(serialPortAdapter);
        //设置当前选项，为了防止引起点击事件，设置animate为true，不知为何好使
        this.setSelection(index, true);
        this.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Log.d(TAG, "onItemSelected: ");
                //更新串口设置，保存参数到本地
                LockerPreferenceUtil.saveSerialPath(getContext(), allDevicesPath[pos]);
                if (serialPathChangeListener != null) {
                    serialPathChangeListener.onChange();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * 通知串口地址发生变化
     */
    public interface OnSerialPathChangeListener {
        void onChange();
    }

}
