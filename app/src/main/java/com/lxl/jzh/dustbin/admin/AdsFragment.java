package com.lxl.jzh.dustbin.admin;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lxl.jzh.base.BaseLifecycleObserver;
import com.lxl.jzh.dustbin.R;
import com.lxl.jzh.dustbin.app.BaseFragment;
import com.lxl.jzh.dustbin.databinding.FragmentAdsBinding;

/**
 * 首页没人点击进入广告页面
 * 广告页面点击回到首页
 */
public class AdsFragment extends BaseFragment<FragmentAdsBinding> {

    private SparseIntArray ads = new SparseIntArray(8);
    private int index=0;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fillResource();
        binding.getRoot().post(runnable);
        binding.getRoot().setOnClickListener(view -> navUp());
    }

    private void fillResource() {
        ads.put(0, R.drawable.one);
        ads.put(1, R.drawable.two);
        ads.put(2, R.drawable.three);
        ads.put(3, R.drawable.four);
    }

    private Runnable runnable=new Runnable() {
        @Override
        public void run() {
            binding.ivAd.setBackgroundResource(ads.get((index++)%4));
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                binding.getRoot().postDelayed(runnable, 10 * 1000);
            }
        }
    };
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.getRoot().removeCallbacks(runnable);
        ads.clear();
    }

    @Override
    protected int layout() {
        return R.layout.fragment_ads;
    }

}
