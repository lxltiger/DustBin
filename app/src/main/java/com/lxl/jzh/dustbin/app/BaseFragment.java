package com.lxl.jzh.dustbin.app;

import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.tencent.mars.xlog.Log;
import com.tencent.mars.xlog.Xlog;


public abstract class BaseFragment<T extends ViewDataBinding> extends Fragment {

    protected T binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        binding = getBinding(inflater, container);
        binding = DataBindingUtil.inflate(inflater, layout(), container, false);
        return binding.getRoot();
    }

    protected abstract int layout();



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.appenderFlush(false);

    }

    protected void navTo(@IdRes int actionId) {
        Navigation.findNavController(binding.getRoot()).navigate(actionId);
    }


    protected void navUp() {
        Navigation.findNavController(binding.getRoot()).navigateUp();
    }

    protected Application appContext() {
        return App.get();
    }
}
