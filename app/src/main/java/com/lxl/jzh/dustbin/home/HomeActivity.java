package com.lxl.jzh.dustbin.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.lxl.jzh.dustbin.R;
import com.lxl.jzh.dustbin.app.DustbinObserver;
import com.tencent.mars.xlog.Log;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private DustbinObserver dustbinObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        hideStatusBar();
        setContentView(R.layout.activity_home);
        HomeViewModel viewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        dustbinObserver = new DustbinObserver(viewModel, viewModel, this, this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        dustbinObserver.onNewIntent(intent);

    }

    //    在setContentView之前调用
    private void hideStatusBar() {
        Window _window = getWindow();
        WindowManager.LayoutParams params = _window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        _window.setAttributes(params);
    }


    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        dustbinObserver.handlePermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        dustbinObserver.handleActivityResult( requestCode, resultCode, data);
    }

}
