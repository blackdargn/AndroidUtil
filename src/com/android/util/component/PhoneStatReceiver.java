package com.android.util.component;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStatReceiver extends BroadcastReceiver {

    private static final String TAG = "PhoneStatReceiver";
    private static boolean incomingFlag = false;
    private static boolean isPhoneActive = false;
    
    /** 电话是否在活动状态*/
    public static boolean isPhoneActive() {
        return isPhoneActive;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // 如果是拨打电话
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            incomingFlag = false;
            isPhoneActive = true;
            Log.i(TAG, "call OUT:");
        } else {
            // 如果是来电
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            switch (tm.getCallState()) {
            case TelephonyManager.CALL_STATE_RINGING:
                incomingFlag = true;// 标识当前是来电
                isPhoneActive = true;
                Log.i(TAG, "RINGING :");
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                if (incomingFlag) {
                    Log.i(TAG, "incoming ACCEPT :");
                }
                isPhoneActive = true;
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if (incomingFlag) {
                    Log.i(TAG, "incoming IDLE");
                }
                isPhoneActive = false;
                break;
            }
        }
    }
}
