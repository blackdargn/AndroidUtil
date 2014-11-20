package com.android.util.component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadCastReceiver extends BroadcastReceiver {   
    
    public static final String ACTION_CHECK_TIMER = "android.intent.ACTION_CHECK_TIMER";
    
    @Override
    public void onReceive(Context context, Intent intent) {
            // 开机启动
            SystemService.startTopLevle(context);

    }
}