package com.android.util.component;

import java.util.concurrent.atomic.AtomicBoolean;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.util.Log;

import com.android.util.system.AppHelper;

/**
 * 监听网络状态
 *
 */
public class NetworkStateReceiver extends BroadcastReceiver
{
	/** 网络是否可用*/
    private static AtomicBoolean networkEnable = new AtomicBoolean();
    /** 是否显示提示窗口*/
    private static boolean showDialog = true;
    /** 网络模块类型 */
    private static int netMode = -1;
    /** 网络分页数 */
    private static int netPageNum = 10;
    
    public NetworkStateReceiver()
    {
    	networkEnable.set(AppHelper.isNetAvaliable());
    }
    
    /** 监听的网络是否可用*/
    public static boolean isNetEnable()
    {
        return networkEnable.get();
    }
    
	@Override
	public void onReceive(Context context, Intent intent)
	{
		State wifiState = null, mobileState = null;
		ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);			
		
		if(cManager == null) return;
		NetworkInfo mobile = cManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if(mobile != null) mobileState = mobile.getState();
		NetworkInfo wifi   = cManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if(wifi   != null) wifiState = wifi.getState();
		
		if((State.CONNECTING == mobileState || State.CONNECTING == wifiState))
		{
		    // 正在连接中 ...
		}else 
		if ((State.CONNECTED == mobileState || State.CONNECTED == wifiState))
		{
			// 网络连接成功
		    networkEnable.set(true);
		    showDialog = true;		   
		    int mode = State.CONNECTED == mobileState ? ConnectivityManager.TYPE_MOBILE : ConnectivityManager.TYPE_WIFI;
		    if(netMode == -1){
		    	netMode = mode;
		    }else{		    	
		    	if(netMode != mode){
		    		// 网络改变
		    	}
		    	netMode = mode;
		    }
		    
		    // 根据网络速率来定分页的数量
		    netPageNum = AppHelper.isWifi3GNetwork(context) ? 20 : 10;
		    Log.d("network", "--> is connected!");
		}else
		{
            // 网络无效 进行提示并提示是否打开网络设置
            networkEnable.set(false);
            netMode = -1;
            
            // 转至 网络 设置界面
            if(showDialog){
                /**
                ToastHelper.showInfo("网络不可用，请确认是否连接网络！");
                Intent it = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                context.startActivity(it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                */
                showDialog = false;
            }
            Log.d("network", "--> is disConnected!");
        }
	}
	
	public static int getNetPageNum() {
	    return netPageNum;
	}
	
	public static boolean isWifiConnect() {
	    return netMode == ConnectivityManager.TYPE_WIFI;
	}
	
	public static boolean isMobileConnect() {
	    return netMode == ConnectivityManager.TYPE_MOBILE;
	}
}