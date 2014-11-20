package com.android.util.location.baidu;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.android.util.component.NetworkStateReceiver;
import com.android.util.map.algthm.Converter;
import com.android.util.map.algthm.Converter.Point;
import com.android.util.system.AppHelper;
import com.android.util.system.DateUtil;
import com.android.util.system.Logger;
import com.android.util.system.MyApplication;
import com.android.util.thread.CycledThread;
import com.android.util.thread.CycledThread.OnTimeoutListener;
import com.android.util.thread.NotifyListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKGeocoderAddressComponent;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-12-31
 * @see : 百度定位提供器
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class BDLocationProvider
{
    final  static String TAG = "MyBMapManager";
    public static final String mKey = "qnlUKA5EV0A2lm0e7iF172Ln";
    /** 百度MapAPI的管理类 */
    private BMapManager mBMapMan;
    boolean m_bKeyRight = false;
    
    private LocationClient mLocationClient;
    private LocationClientOption mLocOption;
    protected Context mContext;
    private Handler mHandler = new Handler();
    private CycledThread mLocTask;
    private CycledThread mAddrTask;
    // 实时位置监听器
    private OnLocationListener mRealOnLocationListener;
    // 定时位置监听器
    private OnLocationListener mKeepOnLocationListener;
    // 定位监听器
    private GetLocationListener mGetLocationListener = new GetLocationListener();

    /** 当前最好一个有效的位置 */
    private Location currentBestLocation;
    /** 当前最好一个有效的轨迹位置 */
    private Location currentBestTrackLocation;
    /** 当前最好一个有效的位置 */
    private BDLocation currentBestBDLocation;
    
    /** 当前位置的地址*/
    protected MKAddrInfo mMyAddress;
    /** 搜索引擎*/
    private MKSearch mKSearch;
    
    private volatile boolean isGpsOn= false;
    private volatile boolean isNetworkOn = false;
    private volatile boolean isReal = false;
    
    private static final int GPS_TIME = 2;
    private static final int NET_TIME = 30;
    /** 最大时速 m/s*/
    private static final int MAX_SPEED = 50;
    
    private Converter coorConverter = new Converter();
    
    /**
     * 获取单例
     * @param context 上下文
     * @return
     */
    public static class BDLocationProviderHolder{
        private static final BDLocationProvider INSTANCE = new BDLocationProvider();
    }
    
    private BDLocationProvider(){}
    
    public static BDLocationProvider getInstance() {
        return BDLocationProviderHolder.INSTANCE;
    }
    
    public void init(Context context) {
        mContext = context;
        getBMapManager();
        getLocationClient();
    }

    /**
     * 设置定位方式
     * @param networkOn 是否启用基站，null,则不处理 
     * @param gpsOn 是否启用GPS，null,则不处理 
     * @param isRealable 是否实时，决定定位的频率，null,则不处理 
     */
    public void setLocationType(Boolean gpsOn, Boolean networkOn, Boolean isRealable)
    {
        boolean isUpdate  = false;
        boolean isRealNeed = false;
        
    	if(gpsOn != null){
    	    if(isGpsOn != gpsOn) {
    	        isGpsOn = gpsOn;
    	        isUpdate = true;
    	    }
    	}
    	if(networkOn !=null){
    	    if(isNetworkOn != networkOn) {
    	        isNetworkOn = networkOn;
    	        isUpdate = true;
    	    }
    	}
    	if(isRealable != null) {
    	    if(isReal != isRealable) {
    	        isReal = isRealable;
    	        isRealNeed = true;
    	    }
    	}
    	if(isUpdate) {
    	    if(isGpsOn) {
                if(!mLocOption.isOpenGps()) {
                    mLocOption.setOpenGps(isGpsOn);
                }
                // 都打开为高精度定位
                if(isNetworkOn) {
                    mLocOption.setLocationMode(LocationMode.Hight_Accuracy);
                }else {
                // 仅仅打开GPS定位
                    mLocOption.setLocationMode(LocationMode.Device_Sensors);
                }
            }else {
                if(mLocOption.isOpenGps()) {
                    mLocOption.setOpenGps(isGpsOn);
                }
                // 仅仅网络定位
                mLocOption.setLocationMode(LocationMode.Battery_Saving);
            }
    	    
    	    if(isNetworkOn || isGpsOn) {
    	        if(mLocationClient != null && !mLocationClient.isStarted()) {
    	            mLocationClient.start();
    	        }
    	    }else {
    	        if(mLocationClient != null && mLocationClient.isStarted()) {
                    mLocationClient.stop();
                }
    	    }
    	}
    	if(isRealNeed) {
            mLocOption.setScanSpan(1000 * (isReal ? GPS_TIME : NET_TIME));
        }
    	if( (isUpdate || isRealNeed) && (mLocationClient != null && mLocationClient.isStarted()) ) {
        	mLocationClient.setLocOption(mLocOption);
    	}
    }
   
    /** 开启百度地图API*/
    public void startBdMap()
    {
        if (mBMapMan != null) {
            mBMapMan.start();
        }
    }
    
    public void stopBdMap() {
        if (mBMapMan != null) {
            mBMapMan.stop();
        }
    }
    
    /** 当APP整体退出时，才调用*/
    public void destoryBdMap()
    {
        if (mBMapMan != null) {
            mBMapMan.stop();
            mBMapMan.destroy();
            mBMapMan = null;
        }
    }
    
    /** 当不需要定位时时，才调用*/
    public void destoryLocation() {
        if(mLocationClient != null) {
            setLocationType(false,false,false);
            mLocationClient.unRegisterLocationListener(mGetLocationListener);
            mLocationClient.stop();
            mLocationClient = null;
        }
    }
    
    /** 初始化位置信息*/
    public void initLocation()
    {
        currentBestLocation = null;
        currentBestBDLocation = null;
        currentBestTrackLocation = null;
        mMyAddress = null;
    }
    
    /**
     *  当APP整体退出且用户登出时，才调用
     */
    public void destory()
    {
        destoryBdMap();
        destoryLocation();
    }

    /** 初始化时必须在主线程调用*/
    public BMapManager getBMapManager()
    {
        if(mBMapMan == null) {
            mBMapMan = new BMapManager(mContext);
            if(!mBMapMan.init(mGeneralListener)) {
                Toast.makeText(MyApplication.getContext().getApplicationContext(),  "BMapManager  初始化错误!", Toast.LENGTH_LONG).show();
                return mBMapMan;
            }
        }
        return mBMapMan;
    }
    
    /** 初始化时必须在主线程调用*/
    public LocationClient getLocationClient() {
        if(mLocationClient == null) {
            mLocationClient = new LocationClient(mContext);
            mLocationClient.registerLocationListener(mGetLocationListener);
            
            mLocOption = new LocationClientOption();
            mLocOption.setOpenGps(true);
            mLocOption.setCoorType("gcj02");//返回的定位结果是百度经纬度,默认值gcj02 bd09ll
            mLocOption.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
            mLocOption.disableCache(true);//禁止启用缓存定位
            mLocOption.setIsNeedAddress(false);//返回的定位结果包含地址信息
            mLocOption.setNeedDeviceDirect(false);//返回的定位结果包含手机机头的方向            
            mLocationClient.setLocOption(mLocOption);
        }
        return mLocationClient;
    }

    /** 设置实时位置监听器，运行在UI线程, 只对最后一个监听器有效 */
    public void setRealOnLocationListener(OnLocationListener listener)
    {
        this.mRealOnLocationListener = listener;
    }
    
    /** 设置定时位置监听器，运行在UI线程, 只对最后一个监听器有效 */
    public void setKeepOnLocationListener(OnLocationListener listener)
    {
        this.mKeepOnLocationListener = listener;
    }
    
    /** 开始定位，指定是否获取地址 */
    public void startLocation(boolean isGetAddress)
    {
        if(isLocating())
        {
            // 正在定位
            return;
        }
        if (mLocationClient != null ) {
            if(!mLocationClient.isStarted()) {
                mLocationClient.start();
            }
            mLocationClient.requestLocation();
            mLocTask = new CycledThread(200, 20000, new OnTimeoutListener()
            {
                @Override
                public void onTimeout()
                {
                    mLocTask = null;
                    onLocErr();
                }
            });
            mLocTask.start();
        }else {
           onLocErr();
           Log.d("LocSDK4", "locClient is null or not started");
        }
    }
    
    /** 开始获取自己位置的地址*/
    public void startGetMyAddr()
    {
        if(currentBestLocation != null)
        {
            if(mMyAddress != null)
            {
                if(mRealOnLocationListener != null)
                {
                    mRealOnLocationListener.onGetAddr(mMyAddress);
                }
            }else
            {
                doGetAddr();
            }
        }
    }
    
    /** 是否正在定位 */
    public boolean isLocating()
    {
        return (mLocTask != null && mLocTask.isAlive());
    }
    
    /** 获取最后有效的位置 */
    public Location getMyLoation()
    {
        return currentBestLocation;
    }
    
    /** 获取最后有效的位置对应的地址信息*/
    public MKAddrInfo getMyAddrInfo()
    {
        return mMyAddress;
    }
    
    private void onLocOK(int statu)
    {
        Log.d(TAG, "-->get location:" + currentBestLocation.getProvider() + " : "+ currentBestLocation.getLongitude() + "," + currentBestLocation.getLatitude());
        if (mRealOnLocationListener != null)
        {
            mRealOnLocationListener.onLocation(currentBestLocation);
        }
        if (mKeepOnLocationListener != null && statu == 1)
        {
            mKeepOnLocationListener.onLocation(currentBestTrackLocation);
        }
    }

    private void onLocErr()
    {
        Log.d(TAG, "-->get location timeout ");
        if (mRealOnLocationListener != null)
        {
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {                    
                    if(mRealOnLocationListener != null) mRealOnLocationListener.onError();
                    setLocationType(null, null, true);
                }
            });
        }
    }

    private void onGetAddr()
    {
        Log.d(TAG, "-->get loc Addr :" + (mMyAddress !=null ?  mMyAddress.strAddr : null));
        if (mRealOnLocationListener != null)
        {            
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {                   
                    if(mRealOnLocationListener != null) mRealOnLocationListener.onGetAddr(mMyAddress);
                }
            });
        }
    }
    
    private void doGetAddr()
    {
        // 重置状态
        mMyAddress = null;
        reverAddress(currentBestLocation.getLatitude(), currentBestLocation.getLongitude(), 
                     new NotifyListener<MKAddrInfo>()
                     {
                        @Override
                        public void onSucceed(MKAddrInfo object) {
                            mMyAddress = object;
                            onGetAddr();
                        }
                     });
    }
    
    private void reverAddress(final double lat, final double lon, final NotifyListener<MKAddrInfo> listener)
    {
        if (mKSearch == null)
        {
            mKSearch = new MKSearch();
            mKSearch.init(mBMapMan, new BaseSearchListenerImpl()
            {
                @Override
                public void onGetAddrResult(MKAddrInfo paramMKAddrInfo, int paramInt)
                {
                    if (paramInt == 0)
                    {
                        listener.notify(paramMKAddrInfo, true);
                    }
                    if(paramMKAddrInfo != null)
                    {
                        if(mAddrTask != null && mAddrTask.isAlive())
                        {
                            mAddrTask._stop();
                            mAddrTask = null;
                        }
                    }
                }
            });
        }
        
        if(AppHelper.isWifi3GNetwork(mContext))
        {
            mKSearch.reverseGeocode(locationToGeoPoint(lat, lon));
        }else
        {
            if(mAddrTask != null && mAddrTask.isAlive())
            {
                return;
            }
            mKSearch.reverseGeocode(locationToGeoPoint(lat, lon));
            mAddrTask = new CycledThread(200, 8000, new OnTimeoutListener()
            {
                @Override
                public void onTimeout()
                {                   
                    if(mAddrTask == null)
                    {
                        return;
                    }
                    Address addr = null;
                    try
                    {
                        addr = BDAPIManager.reverseGeocode(
                                currentBestLocation.getLatitude(),
                                currentBestLocation.getLongitude(),
                                mKey);
                    } catch (ClientProtocolException e)
                    {
                        e.printStackTrace();
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    mAddrTask = null;
                    if(addr != null)
                    {
                        // 获取成功
                        MKAddrInfo paramMKAddrInfo = new MKAddrInfo();
                        paramMKAddrInfo.geoPt = new GeoPoint((int)(lat*1E6), (int)(lon*1E6));
                        paramMKAddrInfo.strAddr = addr.getAddress();
                        paramMKAddrInfo.addressComponents = new MKGeocoderAddressComponent();
                        paramMKAddrInfo.addressComponents.city = addr.city;
                        paramMKAddrInfo.addressComponents.district = addr.county;
                        paramMKAddrInfo.addressComponents.province = addr.province;
                        paramMKAddrInfo.addressComponents.street = addr.line;
                        
                        listener.notify(paramMKAddrInfo, true);
                    }
                }
            });
            mAddrTask.start();
        }
    }
    
    public  void reverAddress2(final double lat, final double lon, final NotifyListener<MKAddrInfo> listener)
    {
        MKSearch mKSearch2 = new MKSearch();            
        mKSearch2.init(getBMapManager(), 
                    new BaseSearchListenerImpl()
            {
                @Override
                public void onGetAddrResult(MKAddrInfo paramMKAddrInfo, int paramInt)
                {
                    if (paramInt == 0)
                    {
                        listener.notify(paramMKAddrInfo, true);
                    }
                }
            });
        mKSearch2.reverseGeocode(locationToGeoPoint(lat, lon));
    }
    
    /** 将WGS坐标 转换为 百度的 GeoPoint*/
    public static GeoPoint locationToGeoPoint(Location location)
    {
        GeoPoint gp = new GeoPoint((int) (location.getLatitude() * 1E6),(int) (location.getLongitude() * 1E6));
        return LocationUtil.gps2Baidu(gp);
    }
    
    /** 将WGS坐标 转换为 百度的 GeoPoint*/
    public static GeoPoint locationToGeoPoint(double lat, double lon)
    {
        GeoPoint gp = new GeoPoint((int) (lat * 1E6),(int) (lon * 1E6));
        return LocationUtil.gps2Baidu(gp);
    }
    
    private Boolean isGps;
    private long timeDx;
    private double curDist;
    private boolean accValid;
    private float trackDist;
    /** 是否是最新的位置信息*/
    private int isBetter(BDLocation location)
    {
        if (location == null) { return -1; }
        Logger.d( "-->receive location:" + location.getLocType()+ " : "+ location.getLongitude() + "," + location.getLatitude());
        isGps = location.getLocType() == 61 ? Boolean.valueOf(true) : (location.getLocType() == 161 ? Boolean.valueOf(false) : null);
        if(isGps == null) {
            // 无效定位结果
            return -1;
        }
        
        // 基站定位，但又没有网络，视为无效
        if(!isGps && !NetworkStateReceiver.isNetEnable()) {
            return -1;
        }
        if (currentBestLocation == null) {
            // 更新我的位置状态
            currentBestLocation = toLocation(location);
            currentBestTrackLocation = currentBestLocation;
            currentBestBDLocation = location;
            return 1;
        }
        // 时间间隔
        timeDx = DateUtil.parseTime(location.getTime()) - currentBestLocation.getTime();
        if(timeDx == 0) {
            return -1;
        }
        if( Math.abs(currentBestBDLocation.getLatitude() - location.getLatitude()) < 0.000001 && 
            Math.abs(currentBestBDLocation.getLongitude() - location.getLongitude()) < 0.000001) {
            return -1;
        }
        // 距离间隔
        curDist = LocationUtil.gps2m(currentBestBDLocation.getLatitude(), currentBestBDLocation.getLongitude(), location.getLatitude(), location.getLongitude());
        // 精度有效性,GPS考虑精度，基站考虑平均时速
        accValid = location.hasRadius() ? location.getRadius() < (isGps ? 100 : 500) : ( curDist * 1000 / timeDx < MAX_SPEED); 
        // 定位有效
        if(accValid){
            // 更新我的位置状态
            currentBestLocation = toLocation(location);
            currentBestBDLocation = location;
            if(curDist < 1) {
                // 位置基本没变
                return -1;
            }else {
                trackDist = currentBestTrackLocation.distanceTo(currentBestLocation);
            	if(trackDist > 10){
            	    // 轨迹位置变化有效
            	    currentBestTrackLocation = currentBestLocation;
            		return 1;
            	}else{
            	    // 有效位置变化，给予通知
            		return 0;
            	}
            }
        }else {
            // 定位无效
            return -1;
        }
    }
    
    /** 位置监听器 */
    public static interface OnLocationListener
    {
        public void onError();

        public void onLocation(Location loc);
        
        public void onGetAddr(MKAddrInfo addr);
    }
    
    private Location toLocation(BDLocation loc) {
        if(loc == null) return null;
        Boolean isGps = loc.getLocType() == 61 ? true : (loc.getLocType() == 161 ? false : null);
        if(isGps == null) {
            // 无效定位结果
            return null;
        }
        Logger.d( "-->receive location: gcj02 " + " : "+ loc.getLongitude() + "," + loc.getLatitude());       
        Location location = new Location(isGps ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER);      
        
        Point point = coorConverter.gcj2Gps(loc.getLongitude(), loc.getLatitude());
        
        location.setLatitude(point.getLatitude());
        location.setLongitude(point.getLongitude());
        location.setAltitude(loc.getAltitude());
        location.setAccuracy(loc.getRadius());
        location.setSpeed(loc.getSpeed());
        location.setTime(DateUtil.parseTime(loc.getTime()));
        
        return location;
    }

    /** 获取位置的监听器 */
    private class GetLocationListener implements BDLocationListener
    {
        @Override
        public void onReceiveLocation(BDLocation  loc)
        {      	
            //接收异步返回的定位结果
            if (loc != null)
            {
                try {
                	int statu = isBetter(loc);
                	if(statu >= 0){
    	                if (mLocTask != null)
    	                {
    	                    mLocTask._stop();
    	                    mLocTask = null;
    	                }
    	                mMyAddress = null;
    	                onLocOK(statu);
                	}
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onReceivePoi(BDLocation arg0) {
            //接收异步返回的POI查询结果
            
        }
    }

    private MKGeneralListener mGeneralListener = new MKGeneralListener()
    {
        @Override
        public void onGetNetworkState(int iError) {
            if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
                Toast.makeText(MyApplication.getContext().getApplicationContext(), "您的网络出错啦！",
                    Toast.LENGTH_LONG).show();
            }
            else if (iError == MKEvent.ERROR_NETWORK_DATA) {
                Toast.makeText(MyApplication.getContext().getApplicationContext(), "输入正确的检索条件！",
                        Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onGetPermissionState(int iError) {
            //非零值表示key验证未通过
            if (iError != 0) {
                //授权Key错误：
//             Toast.makeText(MyApplication.getContext().getApplicationContext(), "请输入正确的授权Key,并检查您的网络连接是否正常！error: "+iError, Toast.LENGTH_LONG).show();
                m_bKeyRight = false;
            }
            else{
                m_bKeyRight = true;
                Toast.makeText(MyApplication.getContext().getApplicationContext(),  "key认证成功", Toast.LENGTH_LONG).show();
            }
        }
    };
   
    public static class BaseSearchListenerImpl implements MKSearchListener
    {
        @Override
        public void onGetAddrResult(MKAddrInfo paramMKAddrInfo, int paramInt)
        {
        }
        
        @Override
        public void onGetDrivingRouteResult(
                MKDrivingRouteResult paramMKDrivingRouteResult, int paramInt)
        {
        }
        
        @Override
        public void onGetPoiResult(MKPoiResult paramMKPoiResult, int paramInt1,
                int paramInt2)
        {
        }
        
        @Override
        public void onGetTransitRouteResult(
                MKTransitRouteResult paramMKTransitRouteResult, int paramInt)
        {
        }
        
        @Override
        public void onGetWalkingRouteResult(
                MKWalkingRouteResult paramMKWalkingRouteResult, int paramInt)
        {
        }

        @Override
        public void onGetBusDetailResult(MKBusLineResult arg0, int arg1)
        {
            
        }

        @Override
        public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1)
        {
            
        }
        
        @Override
        public void onGetPoiDetailSearchResult(int arg0, int arg1)
        {
            
        }

        @Override
        public void onGetShareUrlResult(MKShareUrlResult arg0, int arg1, int arg2) {
            
        }
    }
}