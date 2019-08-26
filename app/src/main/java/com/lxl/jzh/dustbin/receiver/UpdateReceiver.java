package com.lxl.jzh.dustbin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lxl.jzh.dustbin.home.HomeActivity;


public class UpdateReceiver extends BroadcastReceiver {
    private static final String TAG = UpdateReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
        if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            Intent i = new Intent(context, HomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
