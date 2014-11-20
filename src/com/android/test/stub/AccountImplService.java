package com.android.test.stub;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.android.test.TestActivity;
import com.android.util.R;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-10-25
 * @see : AccountImpl.aidl的Stub服务实现者,也可以同时实现多个，在onBing区分。
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class AccountImplService extends Service  
{
    private static final String ACTION_ACCOUNT_IMPL = "android.intent.action.account_impl";
    private static final String ACTION_START_TOP_LEVLE= "android.intent.action.START_TOP_LEVLE";
    private static final String ACTION_STOP_TOP_LEVLE= "android.intent.action.STOP_TOP_LEVLE";
    private static final int    NotificationID_TOPLEVLE = 0x111012;
    private static boolean isRunning = false;
    //////////////////////////////////////////////////////////////////////////////////////
    @Override
    public IBinder onBind(Intent intent)
    {
        return mAccountStub;
    }
    
    @Override
    public boolean onUnbind(Intent intent)
    {
        return super.onUnbind(intent);
    }

    private AccountImpl.Stub mAccountStub = new AccountImpl.Stub()
    {
        @Override
        public String getName() throws RemoteException
        {
            return "Hello world!";
        }

        @Override
        public long getNum(int type) throws RemoteException
        {
            return (long)(type*Math.random());
        }
    };
    //////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate()
    {
        super.onCreate();
        isRunning = true;
    }
        
    @Override
    public void onDestroy()
    {
        isRunning = false;
        super.onDestroy();
    }
       
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        String action = intent.getAction();
        if(action != null)
        {
            if(action.equals(ACTION_START_TOP_LEVLE))
            {
                showBackNotification(true);
            }
            else
            if(action.equals(ACTION_STOP_TOP_LEVLE))
            {
                showBackNotification(false);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    
    private void showBackNotification(boolean backable)
    {
        if (backable)
        {
            Notification notification = new Notification(
                    R.drawable.ic_launcher, null, System.currentTimeMillis());
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, TestActivity.class), 0);
            notification.setLatestEventInfo(this, getString(R.string.app_name),
                    "后台运行中...", contentIntent);           
            notification.flags += Notification.FLAG_NO_CLEAR;           
            startForeground(NotificationID_TOPLEVLE, notification);
        } else
        {
            stopForeground(true);
        }
    }    
    ////////////////////////////////////////////////////////////////////////////////////
    public static void startTopLevle(Context context)
    {
        context.startService(new Intent(ACTION_START_TOP_LEVLE));
    }
    
    public static void stopTopLevle(Context context)
    {
        context.startService(new Intent(ACTION_STOP_TOP_LEVLE));
    }

    public static void stopService(Context context)
    {
        if(isRunning)
        {
            context.stopService(new Intent(context, AccountImplService.class));
        }
    }
    
    public static void startService(Context context)
    {
        if(!isRunning)
        {
            context.startService(new Intent(context, AccountImplService.class));
        }
    }
}
