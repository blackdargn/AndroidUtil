package com.android.util.manager;

import java.util.concurrent.atomic.AtomicInteger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

public class FixTimeQueueExcuter <T> extends QueueExcuter<T> {
    private Context mContext;
    private PendingIntent sender;
    private Alarmreceiver timerReceiver;
    private String timerAction;
    private int time;
    private static AtomicInteger timer_index = new AtomicInteger();
    private final static String TIMER_ACTION = "timer_action_";
    
    public FixTimeQueueExcuter(Context context, int fixTime) {
        mContext = context;
        timerAction = TIMER_ACTION + timer_index.incrementAndGet();
        time = fixTime;
    }

    public  synchronized void start() {
        if(timerReceiver == null){
            timerReceiver = new Alarmreceiver();
        }
        if(sender == null){
            Intent intent =new Intent();
            intent.setAction(timerAction);
            sender = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            try {
                mContext.registerReceiver(timerReceiver, new IntentFilter(timerAction));
            }catch(Exception e) {
                e.printStackTrace();
            }
            AlarmManager alarm = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
            long firstime = SystemClock.elapsedRealtime();
            alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, firstime, time, sender);
        }
    }
    
    public synchronized void stop() {
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
   
   private class Alarmreceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(timerAction)){
                    excute();
                }
            }
    }
}