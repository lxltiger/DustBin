package com.lxl.jzh.dustbin.admin;


import android.os.Bundle;

import androidx.annotation.Nullable;

import com.lxl.jzh.dustbin.R;
import com.lxl.jzh.dustbin.app.BaseFragment;
import com.lxl.jzh.dustbin.databinding.FragmentOverBinding;

/**
 */
public class OverFragment extends BaseFragment<FragmentOverBinding> {
    public OverFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.setFinish(false);
        binding.tvCountDown.setListener(() -> {
            binding.setFinish(true);
            binding.getRoot().postDelayed(this::navUp, 2000);
        });
    }


    @Override
    protected int layout() {
        return R.layout.fragment_over;
    }

}
