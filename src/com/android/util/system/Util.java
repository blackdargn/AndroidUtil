package com.android.util.system;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.View;

import com.android.util.widget.TipText;
import com.baidu.platform.comapi.basestruct.GeoPoint;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-8-8
 * @see : 通用不好分类的工具类
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class Util
{

    public static boolean isEmpty(String str)
    {
      return ((str == null) || (str.length() == 0) || (str.equals("null")));
    }
    
    public static void showToast(Context paramContext, int paramInt)
    {
      TipText.createTipText(paramContext, paramContext.getResources().getString(paramInt), 0).show();
    }
    
    public static void showToast(Context paramContext, String paramString)
    {
      TipText.createTipText(paramContext, paramString, 0).show();
    }

    public static GeoPoint locationToGeoPoint(Location paramLocation)
    {
      if (paramLocation == null) return null;
      return new GeoPoint((int)(1000000.0D * paramLocation.getLatitude()), (int)(1000000.0D * paramLocation.getLongitude()));
    }
    
    public static GeoPoint toGeoPoint(double lon, double lat)
    {
      return new GeoPoint((int)(1000000.0D * lat), (int)(1000000.0D * lon));
    }

    public static String getExternDir(String dir)
    {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        path += dir;
        return path;
    }

    public static boolean isConnectionFast(int type, int subType)
    {
        if (type == ConnectivityManager.TYPE_WIFI)
        {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE)
        {
            switch (subType)
            {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return false; // ~ 14-64 kbps
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return true;  // ~ 400-1000 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return true;  // ~ 600-1400 kbps
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return false; // ~ 100 kbps
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                 return true; // ~ 2-14 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPA:
                 return true; // ~ 700-1700 kbps
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                 return true; // ~ 1-23 Mbps
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return true;  // ~ 400-7000 kbps
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return false; // ~25 kbps
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return false;
            default:
                return false;
            }
        } else
        {
            return false;
        }
    }
    
    // 判断是否是 3G 或者 wifi 网络
    public static boolean isWifi3GNetwork(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);  
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        TelephonyManager tele = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if (activeNetInfo != null)
        { 
            if(activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) 
            {  
                return true;  
            }else
            if(activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE)
            {
                return Util.isConnectionFast(activeNetInfo.getType(), tele.getNetworkType());
            }else 
            {
                return false;
            }
        }else
        {
            return false;
        }
    }

    /**
     * @see 获取控件在屏幕中相对于左下的位置,可用于在控件上方弹出窗口用.
     * @param  achor 控件,
     * @param  context activity
     * @return 距离底部的高度，achor如为null，则返回屏幕的宽与高
     */
    public static int[] getLocationOnScreenAtB(View achor, Activity context)
    {
        int[] cordi = new int[2];
        if(achor != null)
        {
            achor.getLocationOnScreen(cordi);
        }
        DisplayMetrics dm=new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        cordi[1] = dm.heightPixels - cordi[1];
        
        return cordi;
    }
    
    /**
     * @see 获取控件在屏幕中相对于左上的位置,可用于在控件下方弹出窗口用.
     * @param achor 控件,
     * @param context activity
     * @return 距离底部的高度，achor如为null，则返回屏幕的宽与高
     */
    public static int[] getLocationOnScreenAtT(View achor, Activity context)
    {
        int[] cordi = new int[2];
        if(achor != null)
        {
            achor.getLocationOnScreen(cordi);
        }
        cordi[1] += achor.getHeight();
        return cordi;
    }
    
    /** 获取控件的x坐标*/
    public static int getXCordi(View achor)
    {
        int[] cordi = new int[2];
        achor.getLocationOnScreen(cordi);
        
        return cordi[0];
    }
}
