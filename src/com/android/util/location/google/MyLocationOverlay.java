///*******************************************************
// * @作者: zhaohua
// * @日期: 2011-11-17
// * @描述: 我的位置图层，用于替换google的，适配不同的我的位置
// * @声明: copyrights reserved by Petfone 2007-2011
//*******************************************************/
//package com.android.util.location.google;
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Point;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.LevelListDrawable;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.location.LocationProvider;
//import android.os.Bundle;
//
//import com.android.util.R;
//import com.android.util.system.Log;
//import com.google.android.maps.GeoPoint;
//import com.google.android.maps.MapView;
//import com.google.android.maps.Overlay;
//import com.google.android.maps.Projection;
//
///**
// * @author zhaohua
// *
// */
//public class MyLocationOverlay extends Overlay implements LocationListener
//{
//	private static final String TAG = "MyLocationOverlay";
//	private int picWidth;
//	private int picHeight;
//	private int animRid = R.drawable.ic_current_position_anim;
//	private int picCount = 4;
//	private LevelListDrawable myLocationDraw;
//	
//	private boolean  isStart = false;
//	private boolean  isAlwaysVisible = true;
//	
//	private GeoPoint curGeo;
//	private Context context;
//	private MapView mapView;
//	private MyLocationManager locationManager;
//	
//	public MyLocationOverlay(Context context, MapView mapView)
//	{
//		this.context = context;
//		this.mapView = mapView;
//		locationManager = MyLocationManager.getInstance(context);
//	}
//	
//	/**
//	 * @see 设置位置动画资源，默认为google的动画
//	 * @param myRID 必须是level-list的资源
//	 * @param count 动画的幁数
//	 */
//	public void setAnim(int myRID, int count)
//	{
//		animRid = myRID;
//		picCount = count;
//		myLocationDraw = (LevelListDrawable) context.getResources().getDrawable(animRid);
//		picWidth = myLocationDraw.getCurrent().getIntrinsicWidth();
//		picHeight = myLocationDraw.getCurrent().getIntrinsicHeight();
//	}
//	
//	/**
//	 * 开启我的位置定位
//	 */
//	public void enableMyLocation()
//	{
//		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
//		// 如果没有设置的话，就默认初始化
//		if(myLocationDraw == null)
//		{
//			setAnim(animRid,picCount);
//		}
//	}
//	
//	/**
//	 * 关闭我的位置定位
//	 */
//	public void disableMyLocation()
//	{
//		locationManager.removeUpdates(this);
//	}
//	
//	/**
//	 * @see 设置我的位置是否一直可见，默认为可见
//	 * @param always
//	 */
//	public void setAlwaysVivible(boolean always)
//	{
//		isAlwaysVisible = always;
//	}
//	
//	@Override
//	public void draw(Canvas canvas, MapView mapView, boolean shadow)
//	{
//		if(curGeo == null) return;
//		// 将经纬度翻译成屏幕上的XY坐标  
//        Point p= getPoint(mapView);
//        // 在坐标指定位置绘制气球   LocationBasedServicesV2.mBoll
//        canvas.drawBitmap( 
//        		((BitmapDrawable)myLocationDraw.getCurrent()).getBitmap(),
//        		p.x-picWidth/2, p.y-picHeight/2, null);
//          
//        // 调用父类绘制  
//        super.draw(canvas, mapView, shadow);
//        // 下次闪烁
//        nextFlash(mapView);
//	}
//	
//	/** 下次闪烁*/
//    private void nextFlash(final MapView mapView)
//    {
//    	if(isStart) return;
//    	
//    	isStart = true;
//    	mapView.postDelayed(new Runnable()
//		{
//			public void run()
//			{
//				Point p= getPoint(mapView);
//				myLocationDraw.setLevel((myLocationDraw.getLevel() + 1) % picCount);
//				
//				mapView.invalidate(p.x-picWidth/2, p.y-picHeight,p.x+picWidth, p.y+picHeight);
//				
//				isStart = false;
//			}
//		}, 300);
//    }
//    
//    // 将经纬度翻译成屏幕上的XY坐标  
//    private Point getPoint(MapView mapView)
//    {
//        Projection projettion = mapView.getProjection();
//        Point p = new Point();
//        projettion.toPixels(curGeo, p);
//        return p;
//    }
//   
//    /** 显示位置 */
//	private void showLocation(Location location)
//	{
//		if(location == null) return;
//		// 校验
//		LocationUtils.checkLocation(location);
//		
//		curGeo = new GeoPoint((int) (location.getLatitude() * 1E6),(int) (location.getLongitude() * 1E6));
//		mapView.postInvalidate();
//		// 如果总是可见，则移动到当前位置
//		if (isAlwaysVisible)
//		{
//			LocationUtils.moveTo(mapView, curGeo, true, true);
//		}
//	}
//	
//    public void onStatusChanged(String provider, int status, Bundle extras) 
//    {
//        Log.d(TAG, provider + "---> status changed :" + (status == LocationProvider.AVAILABLE));
//    }
//
//    public void onProviderEnabled(String provider) 
//    {
//        Log.d(TAG, provider + "---> Enabled :");
//    }
//
//    public void onProviderDisabled(String provider) 
//    {
//        Log.d(TAG, provider + "---> Disabled :");
//        if (LocationManager.GPS_PROVIDER.equals(provider)) 
//        {
//            Log.d(TAG, provider + "---> convert to network :");
//            locationManager.requestLocationUpdates(
//                    LocationManager.NETWORK_PROVIDER, 60 * 1000, 50,
//                    this);
//        }
//    }
//
//    public void onLocationChanged(Location location) 
//    {
//        showLocation(location);
//    }
//}