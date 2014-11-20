package com.android.util.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

import com.android.util.system.Logger;
import com.android.util.thread.NotifyListener;

public class FixTimeExcuter{

    private Context mContext;
    private PendingIntent sender;
    private FixAlarmreceiver timerReceiver;
    private NotifyListener<Boolean> mListener;
    private long mfixTime;
    
    public final static String TIMER_ACTION = "timer_action_fixtime";
    
    public FixTimeExcuter(Context context) {
        mContext = context;
    }
    
    public void start(long fixTime, NotifyListener<Boolean> listener ) {
        mListener = listener;
        mfixTime = fixTime;
        startTimer();
    }
    
    public void stop() {
        stopTimer();
    }
    
    private synchronized void startTimer() {
        if(timerReceiver == null){
            timerReceiver = new FixAlarmreceiver();
        }
        // 先暂停之前的
        stopTimer();
        if(sender == null){
            Intent intent =new Intent();
            intent.setAction(TIMER_ACTION);
            sender = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            try {
                mContext.registerReceiver(timerReceiver, new IntentFilter(TIMER_ACTION));
                Logger.d("--> registerReceiver FixTimeExcuter");
            }catch(Exception e) {
                Logger.e("--> " + e.getLocalizedMessage());
                stopTimer();
                try {
                mContext.registerReceiver(timerReceiver, new IntentFilter(TIMER_ACTION));
                Logger.d("--> registerReceiver FixTimeExcuter");
                }catch(Exception e1) {
                    Logger.e("--> " + e1.getLocalizedMessage());
                }
            }
            AlarmManager alarm = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
            long time = SystemClock.elapsedRealtime() + mfixTime;
            alarm.set(AlarmManager.ELAPSED_REALTIME, time, sender);
        }
    }
    
    private synchronized void stopTimer() {
        if(sender != null){
            AlarmManager alarm=(AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(sender);
            try {
                mContext.unregisterReceiver(timerReceiver);
                Logger.d("--> unregisterReceiver FixTimeExcuter");
            }catch(Exception e) {
                Logger.e("--> " + e.getLocalizedMessage());
            }
            sender = null;
        }
    }
   
    private class FixAlarmreceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(TIMER_ACTION)){
                   if(mListener != null) {
                       mListener.notify(true, true);                       
                   }
                   stopTimer();
            }
        }
    }
}