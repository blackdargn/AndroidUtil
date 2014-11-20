package com.android.util.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

import com.android.util.system.Logger;

public class AlarmTimeManager {

    private static AlarmTimeManager instance;
    private AlarmTimeManager(Context context) {
        mContext = context;
    }
    public static AlarmTimeManager getInstance(Context context) {
        if(instance == null) {
            instance = new AlarmTimeManager(context);
        }
        return instance;
    }
    
    private int mTime;
    private OnTimeListener mListener;
    private Context mContext;
    private PendingIntent sender;
    private Alarmreceiver timerReceiver; 
    public final static String TIMER_ACTION = "timer_action";
    
    public synchronized void startTimer(int reachTime, OnTimeListener listener) {
        mListener = listener;
        if(timerReceiver == null){
            timerReceiver = new Alarmreceiver();
        }
        if(sender == null){
            mTime = reachTime;
            Intent intent =new Intent();
            intent.setAction(TIMER_ACTION);
            sender = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            try {
                mContext.registerReceiver(timerReceiver, new IntentFilter(TIMER_ACTION));
            }catch(Exception e) {
                e.printStackTrace();
                stopTimer();
                try {
                    mContext.registerReceiver(timerReceiver, new IntentFilter(TIMER_ACTION));
                }catch(Exception e1) {
                    Logger.e("--> " + e1.getLocalizedMessage());
                }
            }
            AlarmManager alarm = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
            long firstime = SystemClock.elapsedRealtime();
            alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime, 1000, sender);
        }
    }
    
    public synchronized void stopTimer() {
        if(sender != null){
            AlarmManager alarm=(AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(sender);
            try {
                mContext.unregisterReceiver(timerReceiver);
            }catch(Exception e) {
                e.printStackTrace();
            }
            sender = null;
        }
    }
    
    public static interface OnTimeListener
    {
        public void onTime(int time);
        public void onFinish();
    }
    
    private class Alarmreceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(TIMER_ACTION)){                
                    if(mTime == 0) {
                        stopTimer();
                        if(mListener != null) {
                            mListener.onFinish();
                        }
                    }else {
                        if(mListener != null) {
                            mListener.onTime(--mTime);
                        }
                    }
                }
            }
    }
}
