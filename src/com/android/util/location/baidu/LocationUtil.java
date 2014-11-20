package com.android.util.location.baidu;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.android.util.system.Logger;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.utils.CoordinateConvert;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.baidu.platform.comapi.map.Projection;

/*******************************************************
 * @author : zhaohua
 * @version : 2012-6-6
 * @see : 位置相关帮助方法
 * @Copyright : copyrights reserved by personal 2007-2012
*******************************************************/
public class LocationUtil
{
	/**
	 * Checks if a given location is a valid (i.e. physically possible) location
	 * on Earth. Note: The special separator locations (which have latitude =
	 * 100) will not qualify as valid. Neither will locations with lat=0 and lng=0
	 * as these are most likely "bad" measurements which often cause trouble.
	 *
	 * @param location the location to test
	 * @return true if the location is a valid location.
	 */
	public static boolean isValidLocation(Location location)
	{
		// 不能为0
		return  location != null
				&& Math.abs(location.getLatitude())  <= 90
				&& Math.abs(location.getLatitude())  > 0
				&& Math.abs(location.getLongitude()) <= 180
				&& Math.abs(location.getLongitude()) > 0;
	}

	/**
	 * Gets a location from a GeoPoint.
	 *
	 * @param p a GeoPoint
	 * @return the corresponding location
	 */
	public static Location getLocation(GeoPoint p)
	{
		if(p == null)
		{
			return null;
		}
		
		Location result = new Location("");
		result.setLatitude(p.getLatitudeE6() / 1.0E6);
		result.setLongitude(p.getLongitudeE6() / 1.0E6);
		return result;
	}

	/**
	 * Gets a GeoPoint from a Location
	 * @param location
	 * @return
	 */
	public static GeoPoint getGeoPoint(Location location)
	{
		return new GeoPoint((int) (location.getLatitude() * 1E6),
				(int) (location.getLongitude() * 1E6));
	}
	
	/**
	 * 将经纬度翻译成屏幕上的XY坐标
	 * @param mapView
	 * @param gp
	 * @return
	 */
	public static android.graphics.Point getPoint(MapView mapView,GeoPoint gp)
	{
		Projection projettion = mapView.getProjection();
		android.graphics.Point p = new android.graphics.Point();
        projettion.toPixels(gp, p);
        return p;
	}
	
	/**
	 * 将屏幕上的XY坐标翻译成经纬度
	 * @param mapView
	 * @param x
	 * @param y
	 * @return
	 */
	public static GeoPoint getGeoPoint(MapView mapView,int x,int y)
	{
		return mapView.getProjection().fromPixels(x,y);
	}
	
	/**
	 * @see 获取 原位置 与 目的位置的偏移角度
	 * @param srcLoc 原位置
	 * @param DestLoc 目的位置
	 * @return 偏移角度
	 */
	public static float getAngleBetween(Location srcLoc, Location DestLoc)
	{
		// 原坐标
		double x1 = srcLoc.getLatitude();
		double y1 = srcLoc.getLongitude();
		// 目的坐标
		double x2 = DestLoc.getLatitude();
		double y2 = DestLoc.getLongitude();
		// 三角形
		float[] d1 = new float[1];
		Location.distanceBetween(x1,y1, x1,y2,d1);
		
		float[] d2 = new float[1];
		Location.distanceBetween(x1,y2, x2,y2,d2);
		// tanA = d1[0]/d2[0]; --> 
		float degree = -Math.round(Math.toDegrees(Math.atan(d1[0]/d2[0])))+90;
		
		return degree;
	}
	
	/**
	 * 移动到目标位置
	 * @param mapView
	 * @param DestLoc
	 * @param byVisible 是否考虑可见性， true：考虑，可见就不移动， false：不考虑，移动
	 * @param animable true:滚动 ， false:定位
	 * @return
	 */
	public static boolean moveTo(MapView mapView, Location loc, boolean byVisible, boolean animable)
	{
		if(mapView != null && loc != null && LocationUtil.isValidLocation(loc))
		{
			GeoPoint point = LocationUtil.getGeoPoint(loc);
			return moveTo(mapView, point, byVisible, animable);
		}else
		{
			return false;
		}
	}
	
	/**
	 * 移动到目标位置
	 * @param mapView
	 * @param point
	 * @param byVisible 是否考虑可见性， true：考虑，可见就不移动， false：不考虑，移动
	 * @param animable true:滚动 ， false:定位
	 * @return
	 */
	public static boolean moveTo(MapView mapView, GeoPoint point, boolean byVisible, boolean animable)
	{
		if(point != null && mapView != null && LocationUtil.isValidGeoPoint(point))
		{
			boolean visible = false;			
			if(byVisible)
			{
				visible = LocationUtil.isVisibleIn(mapView, point);
			}			
			if(!visible)
			{
				MapController controller = mapView.getController();
				if(controller != null)
				{
					if(animable)
					{
						controller.animateTo(point);
					}else
					{
						controller.setCenter(point);
					}
				}else
				{
					return false;
				}
			}
			return true;
		}else
		{
			return false;
		}
	}
	
	/**
	 * 返回两点间的距离，单位米
	 * @param startLa 开始计算的
	 * @param startlo
	 * @param endLa 结束
	 * @param endLo
	 * @return float 两点间的距离
	 */
	public static float getDistanceData(double startLa, double startlo, double endLa,
			double endLo){
		float[] results = new float[1];
		Location.distanceBetween(startLa,startlo,endLa,endLo, results);
		return results[0];
	}
	
	/**
     * 多个节点全部显示地图上
     * @param mapController
     * @param points GPS点
     */
    public static GeoPoint centerPoints(MapController mapController,List<GeoPoint> points)
    { 
        int nwLat = -90 * 1000000;  
        int nwLng = 180 * 1000000;  
        int seLat = 90 * 1000000;  
        int seLng = -180 * 1000000;  
         
        for (GeoPoint point : points) {  
            nwLat = Math.max(nwLat, point.getLatitudeE6());   
            nwLng = Math.min(nwLng, point.getLongitudeE6());  
            seLat = Math.min(seLat, point.getLatitudeE6());  
            seLng = Math.max(seLng, point.getLongitudeE6());  
        }  
        GeoPoint center = new GeoPoint((nwLat + seLat) / 2, (nwLng + seLng) / 2); 
        int spanLatDelta = (int) (Math.abs(nwLat - seLat) * 1.1);  
        int spanLngDelta = (int) (Math.abs(seLng - nwLng) * 1.1);  
        
        mapController.animateTo(gps2Baidu(center));  
        mapController.zoomToSpan(spanLatDelta, spanLngDelta);
        return center;
    }
	
    /** 判断GPS是否打开，如果打开则返回true，
     *  未打开则返回false，
     *  若openable为true，直接打开 GPS 设置界面
     * **/
    public static boolean isGPSOpen(Activity activity,boolean autoOpen, boolean showDialog)
    {    
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER))
        {
            return true;
        }
        // 是否 自动打开到GPS界面
        if(autoOpen)
        {
            Toast.makeText(activity, "必须打开GPS才能进行下步操作", Toast.LENGTH_LONG).show();
            // 转至 GPS 设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activity.startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));          
        }else 
        // 是否弹出提示，引导用户打开窗口
        if(showDialog)
        {
            openGPSDialog(activity);
        }
        
        return false;
    }
    
    /** 打开询问GPS弹出窗口 */
    public static Dialog openGPSDialog(final Activity activity)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("提示");
        builder.setMessage("GPS未开启影响定位精确度，是否设置打开GPS?");
        builder.setPositiveButton("确定", new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // 转至 GPS 设置界面
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", null);      
        Dialog dialog = builder.create();
        return dialog;
    }
    
    /** gps-->baidu*/
    static GeoPoint gps2Baidu(double latitude, double longitude)
    {
        if(latitude > 0 && longitude > 0)
        {
            // 有效的
            return CoordinateConvert.fromWgs84ToBaidu(new GeoPoint((int) (latitude * 1E6),(int) (longitude * 1E6)));
        }else
        {
            // 非法的
            return new GeoPoint(0,0);
        }
    }
    
    /** gcj --> baidu*/
    static GeoPoint gcj2Baidu(double latitude, double longitude)
    {
        if(latitude > 0 && longitude > 0)
        {
            // 有效的
            return CoordinateConvert.fromGcjToBaidu( new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6)));
        }else
        {
            // 非法的
            return new GeoPoint(0,0);
        }
    }
    
    /** gps-->baidu*/
    public static GeoPoint gps2Baidu(GeoPoint gp)
    {
        if(isValidGeoPoint(gp))
        {
            // 有效的
            return CoordinateConvert.fromWgs84ToBaidu(gp);
        }else
        {
            // 非法的
            return new GeoPoint(0,0);
        }
    }
    
    /** gcj --> baidu*/
    public static GeoPoint gcj2Baidu(GeoPoint gp)
    {
        if(isValidGeoPoint(gp))
        {
            // 有效的
            return CoordinateConvert.fromGcjToBaidu(gp);
        }else
        {
            // 非法的
            return new GeoPoint(0,0);
        }
    }
    
    /** baidu --> gcj*/
    public static Location baidu2Gcj(float latitude, float longitude)
    {
        Location location2 = getLocation(gcj2Baidu(latitude, longitude));
        double dx = location2.getLatitude() - latitude;
        double dy = location2.getLongitude() - longitude;
        Location location0 = new Location("orgi");
        location0.setLatitude(latitude - dx);
        location0.setLongitude(longitude - dy);
        return location0;
    }
    
    /**
     * 将定位到的Location对象转换成地图显示用的GeoPoint
     * @param location
     * @return
     */
    public static GeoPoint locationToGeoPoint(Location location)
    {
        return new GeoPoint((int)(location.getLatitude()*1E6), (int)(location.getLongitude()*1E6));
    }
    
    /**
     * 打开ＧＰＳ设置界面
     * @param context
     */
    public static void openGpsSetting(Context context)
    {
        Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        context.startActivity(intent);
    }
    
    public static boolean isValidGeoPoint(GeoPoint geoPoint)
    {
        if(geoPoint == null) return false;
        // 不能为0
        return     Math.abs(geoPoint.getLatitudeE6())  < 90E6
                && Math.abs(geoPoint.getLatitudeE6())  > 0
                && Math.abs(geoPoint.getLongitudeE6()) <= 180E6
                && Math.abs(geoPoint.getLongitudeE6()) > 0;
    }
       
    /** 获取go点为中心,view pc%为半径的有效 Rect*/
    public static Rect getGpointViewRect(MapView mapView,GeoPoint gp, float pc)
    {
        int wh = 0;
        try{
            wh = (int)(mapView.getLongitudeSpan() * pc);
        }catch(Exception e){
            //捕获百度mapapi 在某些时候无法获取LongitudeSpan抛出的空指针
            Log.w("LocationUtil", "百度mapview 未获取到边界。");
        }
        int cx = gp.getLongitudeE6(); 
        int cy = gp.getLatitudeE6();
        return new Rect(cx - wh / 2, cy - wh / 2, cx + wh / 2, cy + wh / 2);
    }
    
    /** 获取map view 的有效 Rect*/
    public static Rect getMapViewRect(MapView mapView)
    {
        if(mapView == null)
        {
            return new Rect();
        }
        try{
            int w = mapView.getLongitudeSpan();
            int h = mapView.getLatitudeSpan();
            int cx = mapView.getMapCenter().getLongitudeE6();
            int cy = mapView.getMapCenter().getLatitudeE6();
            return new Rect(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2);
        }catch(Exception e) {
            e.printStackTrace();
        }
        return new Rect();
    }
    
    /** 获取map view 70%的有效 Rect*/
    public static Rect getMapViewRect70PC(MapView mapView)
    {
        if(mapView == null)
        {
            return new Rect();
        }
        try{
            int w = (int)(mapView.getLongitudeSpan() * 0.7);
            int h = (int)(mapView.getLatitudeSpan() * 0.7);
            int cx = mapView.getMapCenter().getLongitudeE6();
            int cy = mapView.getMapCenter().getLatitudeE6();
            return new Rect(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2);
        }catch(Exception e) {
            e.printStackTrace();
        }
        return new Rect();
    }
    
    /** geoPoint 是否在指定屏幕可见范围之内 */
    public static boolean isVisibleIn(Rect viewRect,GeoPoint lastPoint)
    {
        if(lastPoint == null) return true;
        
        return viewRect.contains(lastPoint.getLongitudeE6(),lastPoint.getLatitudeE6());
    }
    
    /** geoPoint 是否在地图屏幕可见范围之内 */
    public static boolean isVisibleIn(MapView mapView,GeoPoint lastPoint)
    {
        if(lastPoint == null || mapView == null) return true;
        
        return isVisibleIn(getMapViewRect(mapView),lastPoint);
    }
    
    private static final double EARTH_RADIUS = 6378137.0;
    /**
     * 不通过系统Location.distanceBetween计算2点距离
     * 系统Location.distanceBetween方法在有些手机上同一数据，不同时刻计算的距离不同，奇怪！！！
     * @return 两点间的距离，单位米
     */
    public static double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
           double radLat1 = (lat_a * Math.PI / 180.0);
           double radLat2 = (lat_b * Math.PI / 180.0);
           double a = radLat1 - radLat2;
           double b = (lng_a - lng_b) * Math.PI / 180.0;
           double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                  + Math.cos(radLat1) * Math.cos(radLat2)
                  * Math.pow(Math.sin(b / 2), 2)));
           s = s * EARTH_RADIUS;
           s = Math.round(s * 10000) / 10000;
           return s;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////
    /**　过滤地图上的path */  
    public static List<List<GeoPoint>> filterPaths(MapView mapView, List<GeoPointRender> originalPoints) {
        if (originalPoints == null || originalPoints.size() < 2) {
            return null;
        }
        Logger.d("--> filterPaths start");
        // 可见范围
        Rect mapRect = getMapViewRect(mapView);
        float degree3px = mapRect.width() * 3 / 540f;
        List<List<GeoPoint>> paths = new ArrayList<List<GeoPoint>>();
        List<GeoPoint> thisPath = new ArrayList<GeoPoint>();
        GeoPoint lastValidPoint = null;
        int size = originalPoints.size();
        boolean isThisVisible = true;
        boolean is3px = true;
        GeoPoint geoPoint1 = null, geoPoint2 = null;
        Rect out = new Rect();
        
        for (int i = 0; i < size; i++) {
            geoPoint2  = originalPoints.get(i).getPoint();
            if (geoPoint2 == null) {
                continue;
            }
            if(geoPoint1 == null) {
                geoPoint1 = geoPoint2;
                continue;
            }
            // 是否在可见范围
            isThisVisible = isLineOverLapClip(mapRect, out, geoPoint1, geoPoint2);
            if (isThisVisible) {
                if (lastValidPoint == null) {
                    // 一段轨迹开始
                    thisPath = new ArrayList<GeoPoint>();
                    // 检查是否有上一个不可见点
                    if (i > 0) {
                        thisPath.add(originalPoints.get(i - 1).getPoint());
                    }
                    thisPath.add(geoPoint2);
                    lastValidPoint = geoPoint2;
                } else {
                    // 与上一个点的像素距离是否大于3px
                    is3px = true;
                    if (lastValidPoint != null && size > 100) {
                        int xSpan = Math.abs(geoPoint2.getLatitudeE6() - lastValidPoint.getLatitudeE6());
                        int ySpan = Math.abs(geoPoint2.getLongitudeE6() - lastValidPoint.getLongitudeE6());
                        if (xSpan < degree3px && ySpan < degree3px) {
                            is3px = false;
                        }
                    }
                    if (is3px) {
                        thisPath.add(geoPoint2);
                        lastValidPoint = geoPoint2;
                    }
                }
            } else {
                if (lastValidPoint != null) {
                    // 上一点可见而这一点不可见，一段轨迹结束
                    paths.add(thisPath);
                    lastValidPoint = null;
                    thisPath = null;
                }
            }
            // 重置前一个节点
            geoPoint1 = geoPoint2;
        }
        // 结束得加上最后一段轨迹
        if (thisPath != null && !thisPath.isEmpty()) {
            paths.add(thisPath);
        }
        Logger.d("--> filterPaths end");
        return paths;
    }
    
    /** 
     * 功能：判断线段和矩形是否相交　
       1.先判断线段的俩个端点是否在矩形的内部，在就必然相交
       2.其次判断线段的包围盒是否和矩形相交，不相交的话线段和矩形肯定也不相交   
       3.最后判断，矩形的四个顶点是否位于线段的两侧，是则必然相交，否则就不相交
    */
    public static boolean isLineOverLapClip(Rect mapRect, Rect outRect, GeoPoint p1, GeoPoint p2) {
        // 其中任何一个端点在矩形内，说明这个线段必然相交
        if(mapRect.contains(p1.getLongitudeE6(), p1.getLatitudeE6()) || mapRect.contains(p2.getLongitudeE6(), p2.getLatitudeE6())) {
            return true;
        }
        // 其次判断线段的包围盒是否和矩形相交，不相交的话线段和矩形肯定也不相交
        outRect.left = Math.min(p1.getLongitudeE6(), p2.getLongitudeE6());
        outRect.right = Math.max(p1.getLongitudeE6(), p2.getLongitudeE6());
        outRect.top = Math.min(p1.getLatitudeE6(), p2.getLatitudeE6());
        outRect.bottom = Math.max(p1.getLatitudeE6(), p2.getLatitudeE6());
        if(!Rect.intersects(mapRect, outRect)) {
            return false;
        }
        // 矩形的四个顶点是否位于线段的两侧，是则必然相交，否则就不相交
        return true;
    }
    
    /** 绘制path*/
    public static void drawPaths(Canvas canvas,Projection projection,List<List<GeoPoint>> ps,Paint pathPaint){
        if(ps != null && !ps.isEmpty()){
            Path path = new Path();
            Point thisPt = new Point();
            for(List<GeoPoint> p : ps){
                if(p != null && p.size() > 1){
                    path.reset();
                    int size = p.size();
                    Logger.d("--> draw path size = " + size );
                    for(int i = 0 ; i < size ; i++){
                        GeoPoint gp = p.get(i);
                        //耗性能地方
                        projection.toPixels(gp, thisPt);
                        if(i == 0){
                            path.moveTo(thisPt.x, thisPt.y);
                        }else{
                            path.lineTo(thisPt.x, thisPt.y);
                        }
                    }
                    canvas.drawPath(path, pathPaint);
                    Logger.d("--> draw path end" );
                }
            }
        }
    }
   
    /** GeoPoint 获取接口*/
    public static interface GeoPointRender{
        public GeoPoint getPoint();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////
      
    /**
     * 返回两点间的距离，单位米
     * @param startLa 开始计算的
     * @param startlo
     * @param endLa 结束
     * @param endLo
     * @return float 两点间的距离
     */
    public static float getDistance(double startLa, double startlo, double endLa,
            double endLo){
        float[] results = new float[1];
        Location.distanceBetween(startLa,startlo,endLa,endLo, results);
        return results[0];
    }
    
    public static float getDistance(GeoPoint p1, GeoPoint p2)
    {
        return getDistance(p1.getLatitudeE6()/1E6,p1.getLongitudeE6()/1E6,p2.getLatitudeE6()/1E6,p2.getLongitudeE6()/1E6);
    }
    
    /**
     * 计算方位角，0-360度
     * @return 返回2相对1的方位角
     */
    public static double computeAzimuth(double lat1, double lon1, double lat2, double lon2) {
        double result = 0.0;
        int ilat1 = (int) (0.50 + lat1 * 360000.0);
        int ilat2 = (int) (0.50 + lat2 * 360000.0);
        int ilon1 = (int) (0.50 + lon1 * 360000.0);
        int ilon2 = (int) (0.50 + lon2 * 360000.0);
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);
        if ((ilat1 == ilat2) && (ilon1 == ilon2)) {
            return result;
        } else if (ilon1 == ilon2) {
            if (ilat1 > ilat2)
                result = 180.0;
        } else {
            double c = Math
                    .acos(Math.sin(lat2) * Math.sin(lat1) + Math.cos(lat2)
                            * Math.cos(lat1) * Math.cos((lon2 - lon1)));
            double A = Math.asin(Math.cos(lat2) * Math.sin((lon2 - lon1))
                    / Math.sin(c));
            result = Math.toDegrees(A);
            if ((ilat2 > ilat1) && (ilon2 > ilon1)) {

            } else if ((ilat2 < ilat1) && (ilon2 < ilon1)) {
                result = 180.0 - result;
            } else if ((ilat2 < ilat1) && (ilon2 > ilon1)) {
                result = 180.0 - result;
            } else if ((ilat2 > ilat1) && (ilon2 < ilon1)) {
                result += 360.0;
            }
        }
        if(Double.isNaN(result)){
            result = 0;
        }
        return result;
    }
}