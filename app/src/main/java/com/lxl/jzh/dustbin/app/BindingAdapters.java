package com.lxl.jzh.dustbin.app;

import android.view.View;

import androidx.databinding.BindingAdapter;


public class BindingAdapters {
    private static final String TAG = "BindingAdapters";
    public static final int NORMAL = 4 * 60 * 60 * 1000;
    public static final int TIME_OUT = 8 * 60 * 60 * 1000;



    @BindingAdapter("visibleGone")
    public static void setVisible(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }





}
