/*******************************************************
 * @作者: zhaohua
 * @日期: 2012-7-18
 * @描述: 低耗电量位置提供者
 * @声明: copyrights reserved by Petfone 2007-2011
*******************************************************/
package com.android.util.location.google;

import java.util.Vector;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.android.util.system.Log;
import com.android.util.thread.CycledThread;
import com.android.util.thread.CycledThread.OnTimeoutListener;

/**
 * @author zhaohua
 *
 */
public class LowPowerLocationProvider
{
	private static final String TAG = "LowPowerLocationProvider";
	/** 有效时间差值*/
	private static final int VALID_TIME = 1000 * 60 * 2;
	/** 有效精度差值*/
	private static final int VALID_ACCACURY = 200;
	/** 当前最好一个有效的位置*/
	private Location currentBestLocation;
	/** 省电定时器，获取最新位置*/
	private CycledThread locThread;
	/** 位置更新间隔时间*/
	private int updatTime = 5*60*1000;
	
	private MyLocationManager locManager;
	private LastLocListener   lastLocListener;
	private Vector<OnLocationChangedListener> listeners;
	private static LowPowerLocationProvider instance;
	public static LowPowerLocationProvider getInstance(Context context)
	{
		if(instance == null)
		{
			instance = new LowPowerLocationProvider(context);
		}		
		return instance;
	}
	private LowPowerLocationProvider(Context context)
	{
		locManager = MyLocationManager.getInstance(context);
		listeners = new Vector<LowPowerLocationProvider.OnLocationChangedListener>();
	}
	
	public static interface OnLocationChangedListener
	{
	    /** 如果location为null，则获取不到或者超时*/
		public void onLocationChanged(Location location);
	}
	
	public void registerListerer(OnLocationChangedListener listener)
	{
		if(listener == null)
		{
			return;
		}
		if(!listeners.contains(listener))
		{
			listeners.add(listener);
		}
		scheduleTask();
	}
	
	public void unregisterListerer(OnLocationChangedListener listener)
	{
		if(listener == null)
		{
			return;
		}
		if(listeners.contains(listener))
		{
			listeners.removeElement(listener);
		}
		if(listeners.isEmpty())
		{
			shutdown();
		}
	}
	
	/** 获取最新的位置,如果服务不可用，则返回null，且只支持最后一个监听器有效*/
    public void getLastLocation(final OnLocationChangedListener listener)
    {
        getLastLocation(listener, 5000);
    }
    
	/** 获取最新的位置,如果服务不可用，则返回null，且只支持最后一个监听器有效*/
	public void getLastLocation(final OnLocationChangedListener listener,int timeout)
	{
	    // 非法参数
	    if(listener == null) return;
	    // 非法则默认
	    if(timeout <= 0)
	    {
	        timeout = 5000;
	    }
		// 先获取最新有效位置
		Location loc = locManager.getLastKnownLocation();
		if(isBetterLocation(loc))
		{
			currentBestLocation = loc;
			listener.onLocationChanged(loc);
			Log.d(TAG, "--> getLastKnownLocation");
			return;
		}
		// 在定位获取有效位置
		if(lastLocListener == null)
		{ 
		    lastLocListener = new LastLocListener();
		}
		// 设置新的监听器
		lastLocListener.setOnLocationChangedListener(listener,timeout);
	}
	
	/** 停止获取最新位置，与 getLastLocation 成对出现*/
	public void pauseLastLocation()
	{
	    if(lastLocListener != null && lastLocListener.isRequest())
	    {
	        lastLocListener.stopListener();
	    }
	}
	
	private synchronized void scheduleTask()
	{
		if(locThread == null)
		{
			locThread = new CycledThread(new Runnable()
			{				
				@Override
				public void run()
				{
					locManager.removeUpdates(mLocListener);
					locManager.requestLocationUpdates(locManager.getBestProvider(), 3000L, 0, mLocListener);
				}
			}, updatTime);
			locThread.start();
		}
	}
	
	private synchronized void shutdown()
	{
		if (locThread != null)
		{
			locThread._stop();
			locThread = null;
		}
	}
	
	/** Determines whether one Location reading is better than the current Location fix 
	  * @param location  The new Location that you want to evaluate 
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one 
	  */
	protected boolean isBetterLocation(Location location)
	{
		if(location == null)
		{
			return false;
		}
		if (currentBestLocation == null)
		{
			// A new location is always better than no location			
			return true;
		}
		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > VALID_TIME;
		boolean isSignificantlyOlder = timeDelta < -VALID_TIME;
		boolean isNewer = timeDelta > 0;
		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer)
		{
			return true;			
		}
		else 
		// If the new location is more than two minutes older, it must be worse
		if (isSignificantlyOlder)
		{
			return false;
		}
		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > VALID_ACCACURY;
		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),currentBestLocation.getProvider());
		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate)
		{
			return true;
		}
		else if (isNewer && !isLessAccurate)
		{
			return true;
		}
		else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
		{
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2)
	{
		if (provider1 == null)
		{
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
	
	/** 长时间的位置监听器，用于记录轨迹用 */
	private LocationListener mLocListener = new LocationListener()
	{
		@Override
		public void onLocationChanged(Location location)
		{
			if(isBetterLocation(location))
			{
				// good for currentBestLocation
				currentBestLocation = location;
				// notify listeners
				for(OnLocationChangedListener listener : listeners)
				{
					listener.onLocationChanged(location);
				}
				// 获取后就去除
				locManager.removeUpdates(mLocListener);
			}
		}
		@Override
		public void onProviderDisabled(String provider)
		{
			
		}
		@Override
		public void onProviderEnabled(String provider)
		{
			
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
			
		}
	};
	
	/** 获取最新位置监听器，用于获取当时的位置 */
	private class LastLocListener implements LocationListener
    {
	    private OnLocationChangedListener listener;
	    private boolean isRequest = false;
	    private CycledThread timeoutThread;
	    private Location mLocation;
	    
	    public boolean setOnLocationChangedListener(OnLocationChangedListener listener, int timeout)
	    {
	        // 之前有监听，则通知超时
	        if(this.listener != null)
	        {
	            if(this.listener != listener)
	            {
	                doTimeout();
	            }else
	            {
	                stopTimeout();
	            }
	        }
	        // 重置现在的状态	            
	        this.listener = listener;
	        this.mLocation = null;
	        if(!isRequest)
	        {
	            // 进行双向定位，以GPS为主
	            boolean gps = locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	            boolean network = locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
	            if( !gps && !network)
	            {
	                listener.onLocationChanged(null);
	                return false;
	            }else
	            {
	                isRequest = true;
	                startTimeout(timeout);
	                return true;
	            }
	        }else
	        {
	            startTimeout(timeout);
	            return true;
	        }
	    }
	    
	    public boolean isRequest()
	    {
	        return isRequest;
	    }
	    
	    private void doTimeout()
	    {
	        if(listener != null)
	        {
	            listener.onLocationChanged(locManager.getLastKnownLocation());
	            listener = null;
	            stopTimeout();
	        }
	    }
	    
	    private synchronized void stopTimeout()
	    {
            if(timeoutThread != null)
            {
                timeoutThread._stop();
                timeoutThread = null;
            }
	    }
	    
	    private synchronized void startTimeout(int timeout)
	    {
	         stopTimeout();
	         timeoutThread = new CycledThread(200, timeout, new OnTimeoutListener()
	         {
	              @Override
	              public void onTimeout()
	              {
	                  if(listener != null)
	                  {	       
	                      // 如果此次定位到了则返回此次的，否则返回缓存最新的位置
	                      listener.onLocationChanged(mLocation == null ? locManager.getLastKnownLocation() : mLocation);
	                  }
	                  stopListener();
	              }
	          });
	         timeoutThread.start();
	    }
	    
	    public void stopListener()
	    {
	        stopTimeout();
	        listener=null;
	        locManager.removeUpdates(this);
	        isRequest=false;
	    }
	    
        @Override
        public void onLocationChanged(Location location)
        {            
            // GPS 定位OK
            if(LocationManager.GPS_PROVIDER.equalsIgnoreCase(location.getProvider()))
            {
                currentBestLocation = location;
                if(listener != null) listener.onLocationChanged(location);
                stopListener();
                Log.d(TAG, "--> onLocationChanged gps");
            }else
            // NETWORK 定位OK，等待 GPS超时，如超时就返回 此定位位置，但不是最精确的
            if(LocationManager.NETWORK_PROVIDER.equalsIgnoreCase(location.getProvider()))
            {
                mLocation = location;
                Log.d(TAG, "--> onLocationChanged network");
            }
        }

        @Override    
        public void onProviderDisabled(String provider)
        {
            if(LocationManager.GPS_PROVIDER.equalsIgnoreCase(provider))
            {
                if(listener != null) listener.onLocationChanged(null);
                stopListener();
                Log.d(TAG, "--> onProviderDisabled gps");
            }else
            // NETWORK 定位OK，等待 GPS超时，如超时就返回 此定位位置，但不是最精确的
            if(LocationManager.NETWORK_PROVIDER.equalsIgnoreCase(provider))
            {
               Log.d(TAG, "--> onProviderDisabled network");
            }
        }
        
        @Override
        public void onProviderEnabled(String provider){}
        @Override
        public void onStatusChanged(String provider, int status,Bundle extras){}
    }
}
