package com.lxl.jzh.dustbin.admin;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;

import com.lxl.jzh.base.fragment.AbstractAdminFragment;
import com.lxl.jzh.base.utils.Common;
import com.lxl.jzh.base.utils.DeviceUtil;
import com.lxl.jzh.base.utils.NetWorkUtil;
import com.lxl.jzh.dustbin.R;
import com.lxl.jzh.dustbin.databinding.FragmentAdminBinding;

/**
 * 柜子管理员界面
 * 主要用来测试、管理格口状态，
 * 升级或退出应用
 */
public class AdminFragment extends AbstractAdminFragment {
    private static final String TAG = "AdminFragment";
    private FragmentAdminBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_admin, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.setListener(this::onViewClicked);
        binding.setManager(false);
        binding.setShowProgressBar(false);
        binding.setVersion(Common.packageInfo(getContext()));
        binding.etPsw.addTextChangedListener(textWatcher);

    }


    public void handleUpdate() {
        if (NetWorkUtil.isNetworkAvailable(getContext())) {
            binding.setShowProgressBar(true);
            downloadManager.downloadApk(updateUrl(), "ignore", "ignore");
        }
    }

    public void cancelUpdate() {
        downloadManager.cancel();
        binding.progress.setProgress(0);
        binding.setShowProgressBar(false);
    }

    private String updateUrl() {
        return "http://gisea.cn:8088/apk/Server/DustBin/" + getApkName();
    }


    @Override
    protected String getApkName() {
        return "DustBin.apk";
    }

    @Override
    protected void updating(int progress) {
        binding.progress.setProgress(progress);

    }

    @Override
    protected void handleUpdateFail() {
        cancelUpdate();
    }

    @Override
    protected void handleCorrectPsw() {
        binding.setManager(true);
        DeviceUtil.hideInput(getContext(), binding.etPsw.getWindowToken());
    }

    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                Navigation.findNavController(binding.getRoot()).navigateUp();
                break;
            case R.id.iv_home:
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_home);
                break;
            case R.id.grid_manage:
                break;
            case R.id.app_setting:
                break;
            case R.id.exit:
                handleExit();
                break;
            case R.id.btn_cancle:
                cancelUpdate();
                break;
            case R.id.update:
                handleUpdate();
                break;
        }
    }
}
