/*******************************************************
 * @作者: zhaohua
 * @日期: 2011-11-15
 * @描述: 综合定位管理器，包括基站、wifi、GPS定位。
 * @声明: copyrights reserved by Petfone 2007-2011
 *******************************************************/
package com.android.util.location.google;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;


/**
 * @author zhaohua
 *
 */
public class MyLocationManager
{
	private final static String TAG = "MyLocationManager";
	/** wifi 定位提供者名称*/
	public static final String WIFI_PROVIDER = "wifi";
	/** cell 定位提供者名称*/
	public static final String CELL_PROVIDER = "cell";
	/** 定位监听器: 适配 基站、wifi、gps定位 */
	private Vector<LocationListener> wifiListeners;
	private Vector<LocationListener> cellListeners;
	/** GPS定位管理者*/
	public LocationManager locationManager;
	private Context context;
	private Timer timer;
	private TimerTask locTask;
	/** 定位类型*/
	public static enum LOC_TYPE
	{
		/** WIFI定位 0*/
		WIFI,
		/** 基站定位 1*/
		CELL,
		/** GPS定位  2*/
		GPS
	}
	
	private static MyLocationManager instance;
	public static MyLocationManager getInstance(Context context)
	{
		if(instance == null)
		{
			instance = new MyLocationManager(context);
		}
		
		return instance;
	}
	private MyLocationManager(Context context)
	{
		this.context = context;
		wifiListeners  = new Vector<LocationListener>();
		cellListeners  = new Vector<LocationListener>();
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);		
	}
	
	/** 程序结束时，注销*/
	public void destory()
	{
		shutdown();
	}
	
	/**
	 * 注册位置监听器
	 * @param providerName 除了GPS，还有Cell与Wifi
	 * @param minTime 最小更新时间，0 自动
	 * @param minDist 最小更新距离，0 自动
	 * @param listener 监听器
	 * @return 是否监听成功，
	 * 		        如果GPS不可用，则失败；
	 * 		        如果Cell网络不可用，则返回失败;
	 * 		        如果Wifi网络不可用且Wifi没打开，则返回失败。
	 */
	public boolean requestLocationUpdates(String providerName, long minTime, int minDist, LocationListener listener)
	{
		if(listener == null || providerName == null) return false;
		
		LocationUpdate request = new LocationUpdate(providerName, minTime, minDist, listener);
		
		if(request.getType() == LOC_TYPE.GPS)
		{
			return gpsLocation(request);
		}else
		if(request.getType() == LOC_TYPE.WIFI)
		{
			return wifiLocation(request);
		}else
		if(request.getType() == LOC_TYPE.CELL)
		{
			return cellLocation(request);
		}
		return false;
	}
	
	/** 注销GPS位置监听器*/
	public void removeUpdates(LocationListener listener)
	{
		locationManager.removeUpdates(listener);
	}
	
	/** 注销WIFI,CELL位置监听器*/
	public void removeWCUpdates(LocationListener listener)
	{
		wifiListeners.remove(listener);
		cellListeners.remove(listener);
		if(wifiListeners.isEmpty() && cellListeners.isEmpty())
		{
			shutdown();
		}
	}
	
	/** 获取最新的位置，由GPS提供*/
	public Location getLastKnownLocation()
	{
		Location loc = null;
		loc = fromGps();
		if (loc != null) return loc;
		loc = fromNetwork();
		
		return loc;
	}
	
	/** 获取最新的位置，由wifi,cell获取, 可能很耗时*/
	public Location getLastKnownLocation(LOC_TYPE type)
	{
		if(type == LOC_TYPE.GPS)
		{
			return getLastKnownLocation();
		}else
		if(type == LOC_TYPE.WIFI)
		{
			return fromWifi();
		}else
		if(type == LOC_TYPE.CELL)
		{
			return fromCell();
		}
		
		return null;
	}
	
	/** 添加GpsStatusListener*/
	public void addGpsStatusListener(GpsStatus.Listener listener)
	{
		locationManager.addGpsStatusListener(listener);
	}
	
	/** 移除GpsStatusListener*/
	public void removeStatusListener(GpsStatus.Listener listener)
	{
		locationManager.removeGpsStatusListener(listener);
	}
	
	/**获取GPS的状态*/
	public GpsStatus getGpsStatus()
	{
		return locationManager.getGpsStatus(null);
	}
	
	/** 判断GPS是否打开，如果打开则返回true，
	 *  未打开则返回false，
	 *  若openable为true，直接打开 GPS 设置界面
	 * **/
    public boolean isGPSOpen(boolean openable)
    {       
		if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER))
		{
			return true;
		}
      
		if(openable)
		{
		    Toast.makeText(context, "必须打开GPS才能进行下步操作", Toast.LENGTH_LONG).show();
	        // 转至 GPS 设置界面
	        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	        context.startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));	        
		}
		
        return false;
    }
    
    /** 打开询问GPS弹出窗口 */
    public Dialog openGPSDialog(Activity activity)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("GPS未开启");
        builder.setMessage("是否设置打开GPS?");
        builder.setPositiveButton("确定", new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // 转至 GPS 设置界面
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", null);      
        Dialog dialog = builder.create();
        dialog.show();
        return dialog;
    }
    
    /** 从 NETWORK定位获取当前 Location */
	private Location fromNetwork()
	{
		Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		if (location == null)
		{
			Log.d(TAG, "--->fromNetwork is unvalid :" + location);
			return null;
		}
		else
		{
			Log.d(TAG, "--->fromNetwork is valid" + location);
			return location;
		}
	}
	
	/** 从 GPS定位获取当前 Location */
	
	/** 从 GPS定位获取当前 Location */
	private Location fromGps()
	{
		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (location == null)
		{
			Log.d(TAG, "--->fromGps is unvalid:" + location);
			return null;
		}
		else
		{
			Log.d(TAG, "--->fromGps is valid:" + location);
			return location;
		}
	}
	
	
	/** 从 Wifi定位获取当前 Location */
	private Location fromWifi()
	{
		ArrayList<WifiInfo> wifis;
		try
		{
			wifis = getWifiInfo(context);
			return callGear(wifiEntry(wifis));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	/** 从 Cell定位获取当前 Location */
	private Location fromCell()
	{
		ArrayList<CellIDInfo> cells;
		try
		{
			cells = getCellIDInfo(context);
			return callGear(cellEntry(cells));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean gpsLocation(LocationUpdate request)
	{
		LocationProvider locProvider = locationManager.getProvider(request.providerName);
		if (locProvider == null)
		{
			// 没有指定的服务提供者		
			return false;
		}
		else
		{
			// 检测 提供商是否可用
			if (locationManager.isProviderEnabled(request.providerName))
			{
				// 注册监听器
				locationManager.requestLocationUpdates(request.providerName, request.minTime, request.minDist, request.listener);
				Log.d(TAG, "-----> use location privider:" + request.providerName);
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	
	private boolean wifiLocation(LocationUpdate request)
	{
		if(!isNetValid()) return false;
		if(wifiListeners.contains(request.listener))
		{
			return true;
		}
		wifiListeners.add(request.listener);
		scheduleTask();
		return true;
	}
	
	private boolean cellLocation(LocationUpdate request)
	{
		if(!isNetValid()) return false;
		if(cellListeners.contains(request.listener))
		{
			return true;
		}
		cellListeners.add(request.listener);
		scheduleTask();
		return true;
	}
	
	private boolean isNetValid()
	{
		ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo network = connectMgr.getActiveNetworkInfo();
		return (network != null && network.isConnectedOrConnecting());
	}
	
	private synchronized void scheduleTask()
	{
		if(timer == null)
		{
			timer = new Timer();
		}
		if(locTask == null)
		{
			locTask = new TimerTask()
			{
				@Override
				public void run()
				{
					if(!wifiListeners.isEmpty())
					{
						Location loc = fromWifi();
						if(loc != null)
						{
							for(LocationListener one : wifiListeners)
							{
								one.onLocationChanged(loc);
							}
						}
					}
					if(!cellListeners.isEmpty())
					{
						Location loc = fromCell();
						if(loc != null)
						{
							for(LocationListener one : cellListeners)
							{
								one.onLocationChanged(loc);
							}
						}
					}
				}
			};
			timer.scheduleAtFixedRate(locTask, 100, 5*60*1000);
		}
	}
	
	private synchronized void shutdown()
	{
		if (timer != null)
		{
			locTask.cancel();
			locTask = null;
			timer.cancel();
			timer.purge();
			timer = null;			
		}
	}
	
	/** LocationUpdate */
	private static class LocationUpdate
	{
		public String providerName;
		public long minTime;
		public int minDist;
		public LocationListener listener;
		private LOC_TYPE type;
		
		public LocationUpdate(String providerName, long minTime, int minDist, LocationListener listener)
		{
			this.providerName = providerName;
			this.minTime = minTime;
			this.minDist = minDist;
			this.listener = listener;
			if(providerName.equalsIgnoreCase(WIFI_PROVIDER))
			{
				type = LOC_TYPE.WIFI;
			}else
			if(providerName.equalsIgnoreCase(CELL_PROVIDER))
			{
				type = LOC_TYPE.CELL;
			}else
			if(providerName.equalsIgnoreCase(LocationManager.GPS_PROVIDER))
			{
				type = LOC_TYPE.GPS;
			}else
			if(providerName.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER))
			{
				type = LOC_TYPE.GPS;
			}
		}
		
		public LOC_TYPE getType()
		{
			return type;
		}
	}
	
	/** Cell Info*/
	private static class CellIDInfo 
	{
		//基站id
		public int cellId;
		//mcc
		public String mobileCountryCode;
		//mnc
		public String mobileNetworkCode;
		//lac
		public int locationAreaCode;
		//gsm or cdma
		public String radioType;
	}
	
	/** Wifi Info*/
	private static class WifiInfo 
	{
		public String bssid= "";
		public int dBm = 8;
		public String ssid = "";
		
		public WifiInfo(String bssid, int dbm, String ssid)
		{
			this.bssid = bssid;
			this.dBm = dbm;
			this.ssid = ssid;
		}
		
		public WifiInfo(ScanResult scanresult)
		{
			bssid = scanresult.BSSID;
			dBm = scanresult.level;
			ssid = scanresult.SSID;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj == this)
			{
				return true;
			}else
			{
				if (obj instanceof WifiInfo)
				{
					WifiInfo wifiinfo = (WifiInfo) obj;
					if (wifiinfo.dBm == dBm)
					{
						if (wifiinfo.bssid.equals(bssid))
						{
							return true;
						}
					}
					return false;
				}
				else
				{
					return false;
				}
			}
		}
	}
	
	public String getBestProvider()
    {
        Criteria criteria = new Criteria();
        // 设置精度：细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 设置电力要求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        // 是否要海拔
        criteria.setAltitudeRequired(false);
        // 是否要方位
        criteria.setBearingRequired(false);
        // 是否要速度
        criteria.setSpeedRequired(false);
        // 是否允许付费
        criteria.setCostAllowed(false);

        String bestProvider = locationManager.getBestProvider(criteria, true);
        if( bestProvider != null && locationManager.isProviderEnabled(bestProvider))
        {
            return bestProvider;
        }else
        {
            return LocationManager.GPS_PROVIDER;
        }
    }
	
	/* 获取基站信息*/
	public static ArrayList<CellIDInfo> getCellIDInfo(Context context) 
		throws Exception
	{	
		TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		
		ArrayList<CellIDInfo> CellID = new ArrayList<CellIDInfo>();
		CellIDInfo currentCell = new CellIDInfo();

		int type = manager.getNetworkType();
		Log.d(TAG, "getCellIDInfo--> 		NetworkType = " + type);
		int phoneType = manager.getPhoneType();
		Log.d(TAG, "getCellIDInfo--> 		phoneType = " + phoneType);
		
		if (type == TelephonyManager.NETWORK_TYPE_GPRS				// GSM网
				|| type == TelephonyManager.NETWORK_TYPE_EDGE
				|| type == TelephonyManager.NETWORK_TYPE_HSDPA)
		{
			GsmCellLocation gsm = ((GsmCellLocation) manager.getCellLocation());
			if (gsm == null)
			{
				Log.e(TAG, "GsmCellLocation is null!!!");
				return null;
			}
			
			int lac = gsm.getLac();
			String mcc = manager.getNetworkOperator().substring(0, 3);
			String mnc = manager.getNetworkOperator().substring(3, 5);
			int cid = gsm.getCid();
			
			currentCell.cellId = cid;
			currentCell.mobileCountryCode = mcc;
			currentCell.mobileNetworkCode = mnc;
			currentCell.locationAreaCode = lac;			
			currentCell.radioType = "gsm";
			
			CellID.add(currentCell);
			
			// 获得邻近基站信息
			List<NeighboringCellInfo> list = manager.getNeighboringCellInfo();
			int size = list.size();
			for (int i = 0; i < size; i++) 
			{
				CellIDInfo info = new CellIDInfo();
				info.cellId = list.get(i).getCid();
				info.mobileCountryCode = mcc;
				info.mobileNetworkCode = mnc;
				info.locationAreaCode = lac;
			
				CellID.add(info);
			}
			
		}else if (type == TelephonyManager.NETWORK_TYPE_CDMA		// 电信cdma网
				|| type == TelephonyManager.NETWORK_TYPE_1xRTT
				|| type == TelephonyManager.NETWORK_TYPE_EVDO_0
				|| type == TelephonyManager.NETWORK_TYPE_EVDO_A)
		{			
			CdmaCellLocation cdma = (CdmaCellLocation) manager.getCellLocation();	
			if (cdma == null)
			{
				Log.e(TAG, "CdmaCellLocation is null!!!");
				return null;
			}
			
			int lac = cdma.getNetworkId();
			String mcc = manager.getNetworkOperator().substring(0, 3);
			String mnc = String.valueOf(cdma.getSystemId());
			int cid = cdma.getBaseStationId();
			
			currentCell.cellId = cid;
			currentCell.mobileCountryCode = mcc;
			currentCell.mobileNetworkCode = mnc;
			currentCell.locationAreaCode = lac;	
			currentCell.radioType = "cdma";
			
			CellID.add(currentCell);
			
			// 获得邻近基站信息
			List<NeighboringCellInfo> list = manager.getNeighboringCellInfo();
			int size = list.size();
			for (int i = 0; i < size; i++) 
			{
				CellIDInfo info = new CellIDInfo();
				info.cellId = list.get(i).getCid();
				info.mobileCountryCode = mcc;
				info.mobileNetworkCode = mnc;
				info.locationAreaCode = lac;
			
				CellID.add(info);
			}
		}
		
		return CellID;			
	}
	/** cellEntry */
	public static JSONObject cellEntry(List<CellIDInfo> cellID)
	{
		if (cellID == null || cellID.size() == 0) return null;
		JSONObject holder = new JSONObject();
		try
		{
			holder.put("version", "1.1.0");
			holder.put("host", "maps.google.com");
			holder.put("home_mobile_country_code",cellID.get(0).mobileCountryCode);
			holder.put("home_mobile_network_code",cellID.get(0).mobileNetworkCode);
			holder.put("radio_type", cellID.get(0).radioType);
			holder.put("request_address", true);
			if ("460".equals(cellID.get(0).mobileCountryCode)) 
			{
				holder.put("address_language", "zh_CN");
			}
			else 
			{
				holder.put("address_language", "en_US");
			}

			JSONObject data, current_data;
			JSONArray array = new JSONArray();
			current_data = new JSONObject();
			current_data.put("cell_id", cellID.get(0).cellId);
			current_data.put("location_area_code",cellID.get(0).locationAreaCode);
			current_data.put("mobile_country_code",cellID.get(0).mobileCountryCode);
			current_data.put("mobile_network_code",cellID.get(0).mobileNetworkCode);
			current_data.put("age", 0);
			current_data.put("signal_strength", -60);
			current_data.put("timing_advance", 5555);
			array.put(current_data);

			if (cellID.size() > 2)
			{
				for (int i = 1; i < cellID.size(); i++)
				{
					data = new JSONObject();
					data.put("cell_id", cellID.get(i).cellId);
					data.put("location_area_code",cellID.get(i).locationAreaCode);
					data.put("mobile_country_code",cellID.get(i).mobileCountryCode);
					data.put("mobile_network_code",cellID.get(i).mobileNetworkCode);
					data.put("age", 0);
					array.put(data);
				}
			}

			holder.put("cell_towers", array);
			return holder;
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		return null;
	}
	/* 获取WIFI信息*/
	public static ArrayList<WifiInfo> getWifiInfo(Context context)
		throws Exception
	{
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if( !wifiManager.isWifiEnabled())
		{
			return null;
		}
		android.net.wifi.WifiInfo curConnetInfo = wifiManager.getConnectionInfo();
		// 当前连接wifi
		WifiInfo currentWIFI = null;
		if(curConnetInfo != null && curConnetInfo.getBSSID() != null)
		{
			String s = curConnetInfo.getBSSID();
			int i = curConnetInfo.getRssi();
			String s1 = curConnetInfo.getSSID();
			currentWIFI = new WifiInfo(s, i, s1);
		}
		ArrayList<WifiInfo> allWifi = new ArrayList<WifiInfo>();
		if(currentWIFI != null)
		{
			allWifi.add(currentWIFI);
		}	
		// 扫描的wifi
		List<ScanResult> lsScanResult = wifiManager.getScanResults();
		for (ScanResult result : lsScanResult)
		{
			WifiInfo scanWIFI = new WifiInfo(result);
			if (!scanWIFI.equals(currentWIFI))
			{
				allWifi.add(scanWIFI);
			}
		}
		return allWifi;
	}
	/** wifiEntry */
	public static JSONObject wifiEntry(List<WifiInfo> wifis)
	{
		if(wifis == null || wifis.size() == 0) return null;
		JSONObject holder = new JSONObject();
		try{
			holder.put("version", "1.1.0");
			holder.put("host", "maps.google.com");
			holder.put("address_language", "zh_CN");
			holder.put("request_address", true);
					
			JSONArray array = new JSONArray();
			for(WifiInfo one : wifis)
			{
				JSONObject data = new JSONObject();
				data.put("mac_address", one.bssid);  
		        data.put("signal_strength", one.dBm);
		        data.put("ssid", one.ssid);
		        data.put("age", 0);
				array.put(data);
			}
			holder.put("wifi_towers", array);
			return holder;
		}catch (JSONException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	/** callGear */
	public static Location callGear(JSONObject holder)
	{
		if(holder == null) return null;
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost("http://www.google.com/loc/json");
		try
		{
			StringEntity se = new StringEntity(holder.toString());
			Log.d("Location send", holder.toString());
			post.setEntity(se);
			HttpResponse resp = client.execute(post);
			HttpEntity entity = resp.getEntity();
			BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
			StringBuffer sb = new StringBuffer();
			String result = br.readLine();
			while (result != null)
			{
				Log.d("Locaiton reseive-->", result);
				sb.append(result);
				result = br.readLine();
			}
			JSONObject data = new JSONObject(sb.toString());
			data = (JSONObject) data.get("location");
			Location loc = new Location(LocationManager.NETWORK_PROVIDER);
			loc.setLatitude((Double) data.get("latitude"));
			loc.setLongitude((Double) data.get("longitude"));
			loc.setAccuracy(Float.parseFloat(data.get("accuracy").toString()));
			loc.setTime(System.currentTimeMillis());
			return loc;
		}catch (JSONException e)
		{
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
