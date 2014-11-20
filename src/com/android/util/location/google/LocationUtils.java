///*******************************************************
// * @作者: zhaodh
// * @日期: 2011-12-19
// * @描述: 位置相关帮助方法
// * @声明: copyrights reserved by Petfone 2007-2011
//*******************************************************/
//package com.android.util.location.google;
//
//import android.graphics.Rect;
//import android.location.Location;
//
//import com.google.android.maps.GeoPoint;
//import com.google.android.maps.MapController;
//import com.google.android.maps.MapView;
//import com.google.android.maps.Projection;
//
///**
// * Utility class for decimating tracks at a given level of precision.
// *
// * @author Leif Hendrik Wilden
// */
//public class LocationUtils
//{
//	/**
//	 * Test if a given GeoPoint is valid, i.e. within physical bounds.
//	 *
//	 * @param geoPoint the point to be tested
//	 * @return true, if it is a physical location on earth.
//	 */
//	public static boolean isValidGeoPoint(GeoPoint geoPoint)
//	{
//		// 不能为0
//		return     Math.abs(geoPoint.getLatitudeE6())  < 90E6
//				&& Math.abs(geoPoint.getLatitudeE6())  > 0
//				&& Math.abs(geoPoint.getLongitudeE6()) <= 180E6
//				&& Math.abs(geoPoint.getLongitudeE6()) > 0;
//	}
//
//	/**
//	 * Checks if a given location is a valid (i.e. physically possible) location
//	 * on Earth. Note: The special separator locations (which have latitude =
//	 * 100) will not qualify as valid. Neither will locations with lat=0 and lng=0
//	 * as these are most likely "bad" measurements which often cause trouble.
//	 *
//	 * @param location the location to test
//	 * @return true if the location is a valid location.
//	 */
//	public static boolean isValidLocation(Location location)
//	{
//		// 不能为0
//		return  location != null
//				&& Math.abs(location.getLatitude())  <= 90
//				&& Math.abs(location.getLatitude())  > 0
//				&& Math.abs(location.getLongitude()) <= 180
//				&& Math.abs(location.getLongitude()) > 0;
//	}
//
//	/**
//	 * Gets a location from a GeoPoint.
//	 *
//	 * @param p a GeoPoint
//	 * @return the corresponding location
//	 */
//	public static Location getLocation(GeoPoint p)
//	{
//		if(p == null)
//		{
//			return null;
//		}
//		
//		Location result = new Location("");
//		result.setLatitude(p.getLatitudeE6() / 1.0E6);
//		result.setLongitude(p.getLongitudeE6() / 1.0E6);
//		return result;
//	}
//
//	/**
//	 * Gets a GeoPoint from a Location
//	 * @param location
//	 * @return
//	 */
//	public static GeoPoint getGeoPoint(Location location)
//	{
//		return new GeoPoint((int) (location.getLatitude() * 1E6),
//				(int) (location.getLongitude() * 1E6));
//	}
//	
//	/**
//     * Gets a GeoPoint from a Location
//     * @param location
//     * @return
//     */
//    public static com.baidu.mapapi.GeoPoint locationToGeoPoint(Location location)
//    {
//        return new com.baidu.mapapi.GeoPoint((int) (location.getLatitude() * 1E6),
//                (int) (location.getLongitude() * 1E6));
//    }
//	
//	/**
//	 * 将经纬度翻译成屏幕上的XY坐标
//	 * @param mapView
//	 * @param gp
//	 * @return
//	 */
//	public static android.graphics.Point getPoint(MapView mapView,GeoPoint gp)
//	{
//		Projection projettion = mapView.getProjection();
//		android.graphics.Point p = new android.graphics.Point();
//        projettion.toPixels(gp, p);
//        return p;
//	}
//	
//	/**
//	 * 将屏幕上的XY坐标翻译成经纬度
//	 * @param mapView
//	 * @param x
//	 * @param y
//	 * @return
//	 */
//	public static GeoPoint getGeoPoint(MapView mapView,int x,int y)
//	{
//		return mapView.getProjection().fromPixels(x,y);
//	}
//	
//	/** 获取map view 的有效 Rect*/
//	public static Rect getMapViewRect(MapView mapView)
//	{
//		if(mapView == null)
//		{
//			return new Rect();
//		}
//		
//		int w = mapView.getLongitudeSpan();
//		int h = mapView.getLatitudeSpan();
//		int cx = mapView.getMapCenter().getLongitudeE6();
//		int cy = mapView.getMapCenter().getLatitudeE6();
//		return new Rect(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2);
//	}
//	
//	/** geoPoint 是否在指定屏幕可见范围之内 */
//	public static boolean isVisibleIn(Rect viewRect,GeoPoint lastPoint)
//	{
//		if(lastPoint == null) return true;
//		
//		return viewRect.contains(lastPoint.getLongitudeE6(),lastPoint.getLatitudeE6());
//	}
//	
//	/** geoPoint 是否在地图屏幕可见范围之内 */
//	public static boolean isVisibleIn(MapView mapView,GeoPoint lastPoint)
//	{
//		if(lastPoint == null || mapView == null) return true;
//		
//		return isVisibleIn(getMapViewRect(mapView),lastPoint);
//	}
//	
//	/**
//	 * @see 获取 原位置 与 目的位置的偏移角度
//	 * @param srcLoc 原位置
//	 * @param DestLoc 目的位置
//	 * @return 偏移角度
//	 */
//	public static float getAngleBetween(Location srcLoc, Location DestLoc)
//	{
//		// 原坐标
//		double x1 = srcLoc.getLatitude();
//		double y1 = srcLoc.getLongitude();
//		// 目的坐标
//		double x2 = DestLoc.getLatitude();
//		double y2 = DestLoc.getLongitude();
//		// 三角形
//		float[] d1 = new float[1];
//		Location.distanceBetween(x1,y1, x1,y2,d1);
//		
//		float[] d2 = new float[1];
//		Location.distanceBetween(x1,y2, x2,y2,d2);
//		// tanA = d1[0]/d2[0]; --> 
//		float degree = -Math.round(Math.toDegrees(Math.atan(d1[0]/d2[0])))+90;
//		
//		return degree;
//	}
//	
//	/**
//	 * 获取 纠偏后的位置
//	 * @param orin
//	 * @return
//	 */
//	public static void checkLocation(Location orin)
//	{		
//		if(orin == null)
//		{
//			return;
//		}
//		Converter c = new Converter();
//		Converter.Point pt = c.getEncryPoint(orin.getLongitude(), orin.getLatitude());
//		if(pt != null)
//		{
//			orin.setLongitude(pt.getX());
//			orin.setLatitude( pt.getY());
//		}
//	}
//	
//	/**
//	 * 移动到目标位置
//	 * @param mapView
//	 * @param DestLoc
//	 * @param byVisible 是否考虑可见性， true：考虑，可见就不移动， false：不考虑，移动
//	 * @param animable true:滚动 ， false:定位
//	 * @return
//	 */
//	public static boolean moveTo(MapView mapView, Location loc, boolean byVisible, boolean animable)
//	{
//		if(mapView != null && loc != null && LocationUtils.isValidLocation(loc))
//		{
//			GeoPoint point = LocationUtils.getGeoPoint(loc);
//			return moveTo(mapView, point, byVisible, animable);
//		}else
//		{
//			return false;
//		}
//	}
//	
//	/**
//	 * 移动到目标位置
//	 * @param mapView
//	 * @param point
//	 * @param byVisible 是否考虑可见性， true：考虑，可见就不移动， false：不考虑，移动
//	 * @param animable true:滚动 ， false:定位
//	 * @return
//	 */
//	public static boolean moveTo(MapView mapView, GeoPoint point, boolean byVisible, boolean animable)
//	{
//		if(point != null && mapView != null && LocationUtils.isValidGeoPoint(point))
//		{
//			boolean visible = false;			
//			if(byVisible)
//			{
//				visible = LocationUtils.isVisibleIn(mapView, point);
//			}			
//			if(!visible)
//			{
//				MapController controller = mapView.getController();
//				if(controller != null)
//				{
//					if(animable)
//					{
//						controller.animateTo(point);
//					}else
//					{
//						controller.setCenter(point);
//					}
//				}else
//				{
//					return false;
//				}
//			}
//			return true;
//		}else
//		{
//			return false;
//		}
//	}
//	
//	/**
//	 * 返回两点间的距离，单位米
//	 * @param startLa 开始计算的
//	 * @param startlo
//	 * @param endLa 结束
//	 * @param endLo
//	 * @return float 两点间的距离
//	 */
//	public static float getDistanceData(double startLa, double startlo, double endLa,
//			double endLo){
//		float[] results = new float[1];
//		Location.distanceBetween(startLa,startlo,endLa,endLo, results);
//		return results[0];
//	}
//	
//	/**
//	 * This is a utility class w/ only static members.
//	 */
//	private LocationUtils()
//	{
//	}
//}
