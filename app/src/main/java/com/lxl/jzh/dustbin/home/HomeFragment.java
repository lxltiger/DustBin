package com.lxl.jzh.dustbin.home;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.lxl.jzh.dustbin.R;
import com.lxl.jzh.dustbin.app.BaseFragment;
import com.lxl.jzh.dustbin.databinding.HomeFragmentBinding;

public class HomeFragment extends BaseFragment<HomeFragmentBinding> {
    private static final String TAG = "HomeFragment";
    private HomeViewModel viewModel;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }


    @Override
    protected int layout() {
        return R.layout.home_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: ");
        viewModel = ViewModelProviders.of(getActivity()).get(HomeViewModel.class);
        binding.setListener(this::onViewClicked);
        binding.getRoot().postDelayed(showAds, 30 * 1000);
        binding.setting.setOnLongClickListener(view -> {
            navTo(R.id.action_to_adminFragment);
            return true;
        });
    }

    private Runnable showAds=new Runnable() {
        @Override
        public void run() {
            navTo(R.id.action_to_adsFragment);
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.getRoot().removeCallbacks(showAds);
    }

    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.residual_waste:
                viewModel.handleResidualWaste();
                navTo(R.id.action_overFragment);
                break;
            case R.id.hazardous_waste:
                viewModel.handleHazardousWaste();
                navTo(R.id.action_overFragment);

                break;
            case R.id.recyclable_waste:
//                viewModel.handleHazardousWaste();
                break;
            case R.id.household_waste:
//                viewModel.handleHazardousWaste();
                break;
        }
    }
}
