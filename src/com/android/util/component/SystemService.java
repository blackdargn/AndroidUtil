package com.android.util.component;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.android.util.bus.MessageBus;
import com.android.util.bus.MessageBus.MMessage;
import com.android.util.bus.MessageBus.UIReceiver;
import com.android.util.system.Logger;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-10-24
 * @see : 系统服务，暂时用于提升应用程序的等级，防止拍照被Kill。
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class SystemService extends Service{
    private static final String TAG = "SystemService";
    
    private static final String ACTION_START_TOP_LEVLE = "android.intent.START_TOP_LEVLE";
    private static final String ACTION_STOP_TOP_LEVLE = "android.intent.STOP_TOP_LEVLE";

    private static final int NotificationID_TOPLEVLE = 0x111012;
    private static final int MSG_START_TOP_LEVLE = 0x111013;
    private static final int MSG_STOP_TOP_LEVLE = 0x111014;

    private static boolean isRunning = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        MessageBus.getBusFactory().register(NotificationID_TOPLEVLE, receiver);
    }

    @Override
    public void onDestroy() {
        MessageBus.getBusFactory().unregister(NotificationID_TOPLEVLE, receiver);
        isRunning = false;
        startService(this);
        Logger.d("--->onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(ACTION_START_TOP_LEVLE)) {
                    showBackNotification(true);
                    Log.d(TAG, "showBackNotification: ACTION_START_TOP_LEVLE");
                } else if (action.equals(ACTION_STOP_TOP_LEVLE)) {
                    showBackNotification(false);
                    Log.d(TAG, "showBackNotification: ACTION_STOP_TOP_LEVLE");
                } 
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void showBackNotification(boolean backable) {
        if (backable) {
            Notification notification = new Notification(0, null, System.currentTimeMillis());
            /**
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,new Intent(this, MainMapActivity2.class), 0);
            notification.setLatestEventInfo(this, getString(R.string.app_name), "", contentIntent);
            */
            notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
            
            startForeground(NotificationID_TOPLEVLE, notification);
        } else {
            stopForeground(true);
        }
    }

    // 控制接收器
    private UIReceiver receiver = new UIReceiver() {
        private static final long serialVersionUID = 1373117125787882373L;

        @Override
        public void onReceive(MMessage message) {
            switch (message.arg1()) {
            case MSG_START_TOP_LEVLE: {
                showBackNotification(true);
                Log.d(TAG, "showBackNotification: MSG_START_TOP_LEVLE");
                break;
            }
            case MSG_STOP_TOP_LEVLE: {
                showBackNotification(false);
                Log.d(TAG, "showBackNotification: MSG_STOP_TOP_LEVLE");
                break;
            }
            }
        }
    };
    
    // //////////////////////////////////////////////////////////////////////////////////
    public static void startTopLevle(Context context) {
        if (isRunning) {
            MessageBus.getBusFactory().send(NotificationID_TOPLEVLE, MSG_START_TOP_LEVLE);
        } else {
            context.startService(new Intent(ACTION_START_TOP_LEVLE));
        }
    }

    public static void stopTopLevle(Context context) {
        /** 长期保持服务
        if (isRunning) {
            MMessage msg = MessageBus.getBusFactory().createMessage(NotificationID_TOPLEVLE);
            msg.arg1 = MSG_STOP_TOP_LEVLE;
            MessageBus.getBusFactory().send(msg);
        }*/
    }

    public static void stopService(Context context) {
        if (isRunning) {
            context.stopService(new Intent(context, SystemService.class));
        }
    }

    public static void startService(Context context) {
        if (!isRunning) {
            context.startService(new Intent(context, SystemService.class));
        }
    }
}