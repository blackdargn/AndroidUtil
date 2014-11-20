package com.android.util.system;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-12-10
 * @see : 所有的日期处理在此文件中
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class DateUtil
{
    private static final String DATA_FORMAT = "HH:mm:ss";
    private static final SimpleDateFormat sdf  = new SimpleDateFormat(DATA_FORMAT,Locale.CHINESE);
    private static final String DATA_FORMAT_2 = "HH:mm";
    private static final SimpleDateFormat sdf2 = new SimpleDateFormat(DATA_FORMAT_2,Locale.CHINESE);
    private static final String DATA_FORMAT_3 = "MM-dd";
    private static final SimpleDateFormat sdf3  = new SimpleDateFormat(DATA_FORMAT_3,Locale.CHINESE);
    private static final String DATA_FORMAT_4 = "MM-dd HH:mm:ss";
    private static final SimpleDateFormat sdf4  = new SimpleDateFormat(DATA_FORMAT_4,Locale.CHINESE);
    private static final String DATA_FORMAT_5 = "MM-dd HH:mm";
    private static final SimpleDateFormat sdf5  = new SimpleDateFormat(DATA_FORMAT_5,Locale.CHINESE);
    private static final String DATA_FORMAT_6 = "_yyyyMMdd_HHmm";
    private static final SimpleDateFormat sdf6  = new SimpleDateFormat(DATA_FORMAT_6,Locale.CHINESE);
    private static final String DATA_FORMAT_7 = "yyyy-MM-dd-HH-mm-ss";
    private static final SimpleDateFormat sdf7  = new SimpleDateFormat(DATA_FORMAT_7,Locale.CHINESE);
    private static final String DATA_FORMAT_8 = "yyyy-MM-dd HH:mm:ss";
    private static final SimpleDateFormat sdf8  = new SimpleDateFormat(DATA_FORMAT_8,Locale.CHINESE);
    private static final String DATA_FORMAT_9 = "yyyy-MM-dd";
    private static final SimpleDateFormat sdf9  = new SimpleDateFormat(DATA_FORMAT_9,Locale.CHINESE);

    /**
     * 将毫秒转换为类似于：1小时5分1秒的字符串
     */
    public static String getTimeDesc(long ms)
    {
        long hour = ms/3600000;
        long rest = ms%3600000;
        long min = rest/60000;
        rest = rest%60000;
        long sec = rest/1000;
        
        String ret = "";
        if(hour>0)
        {
            if(hour < 24) {
                ret=hour+"小时";
            }else {
                ret=hour/24+"天";
            }
        }
        if(min>0)
        {
            ret=ret+min+"'";
        }
        ret = ret+(sec<=0 ? "" : sec +"''") ;
        
        return ret;
    }
    
    /** 获取 多少时间前 的格式化*/
    public static String getTimeDescPre(long time)
    {
        if(time == 0 ) return ""; 
        long ms = System.currentTimeMillis() - time;
        if(ms < 0) return "";
        
        long hour = ms/3600000;
        long rest = ms%3600000;
        long min = rest/60000;
        rest = rest%60000;
        long sec = rest/1000;
        
        String ret = "";
        if(hour>0)
        {
            if(hour < 24) {
                ret=hour+"小时前";
            }else {
                ret=hour/24+"天前";
            }
            return ret;
        }
        if(min>0)
        {
            ret=ret+min+"分钟前";
            return ret;
        }
        if(sec == 0) {
            return "刚刚";
        }else {
            ret = ret+sec+"秒钟前";
        }
        return ret;
    }
    
    public static String getTimeFormat(long time)
    {
        return getTimeFormat2(time);
    }
    
    /** 按照 1天 2天前 HH:mm格式化*/
    public static String getTimeFormat1(long time)
    {
        if(time == 0 ) return "";
        
        Calendar tt  = Calendar.getInstance();
        int nowDay = tt.get(Calendar.DAY_OF_MONTH);
        tt.setTimeInMillis(time);
        int timeDay = tt.get(Calendar.DAY_OF_MONTH);
        
        int dxDay = nowDay - timeDay;
        long dxTime = (System.currentTimeMillis() - time)/3600000l;
        
        // 24小时之内的显示时分
        if(dxTime < 24) {
            return sdf2.format(tt.getTime());
        }else
        if(dxDay >= 0){
	        if(dxDay == 0){
	        	return sdf2.format(tt.getTime());
	        }else
	        if(dxDay > 2){
	        	return sdf3.format(tt.getTime());
	        }else
	        if(dxDay == 2){
	            return "2天前";
	        }else{
	        	return "1天前";
	        }
        }else{
        	if(dxTime < 24)
        	{
        		return "1天前";
        	}else
        	if(dxTime < 2*24)
            {
            	return "2天前";
            }else{
            	return sdf3.format(tt.getTime());
            }
        }
    }
    
    /** 按照 昨天 前天 HH:mm格式化*/
    public static String getTimeFormat2(long time) {
        if(time == 0 ) return "";
        
        Calendar tt  = Calendar.getInstance();
        int nowDay = tt.get(Calendar.DAY_OF_YEAR);
        tt.setTimeInMillis(time);
        int timeDay = tt.get(Calendar.DAY_OF_YEAR);
        int dxDay = nowDay - timeDay;

        if(dxDay >= 0){
            // 今天
            if(dxDay == 0){
                return sdf2.format(tt.getTime());
            }else
            // 几天前
            if(dxDay > 2){
                return sdf5.format(tt.getTime());
            }else
            if(dxDay == 2){
            // 前天
                return "前天 " + sdf2.format(tt.getTime());
            }else{
            // 昨天
                return "昨天 " + sdf2.format(tt.getTime());
            }
        }else{
            long dxTime = (System.currentTimeMillis() - time)/3600000l;
            if(dxTime > 0) {
                // 跨年
                if(dxTime < 24)
                {
                    return "昨天 " + sdf2.format(tt.getTime());
                }else
                if(dxTime < 2*24)
                {
                    return "前天 " + sdf2.format(tt.getTime());
                }else{
                    return sdf5.format(tt.getTime());
                }
            }else {
                // 未来时间
                return sdf5.format(tt.getTime());
            }
        }
    }
    
    /** 获取HH:mm格式的时间*/
    public static String getHM(long timestamp){
        return sdf2.format(new Date(timestamp));
    }
    
    /** 获取MM:dd格式的时间*/
    public static String getMD(long timestamp){
        return sdf3.format(new Date(timestamp));
    }
    
    /** 获取HH:mm:ss格式的时间*/
    public static String getHMS(long timestamp){
        return sdf.format(new Date(timestamp));
    }
    
    /** 获取MM-dd HH:mm格式的时间*/
    public static String getMDHM(long timestamp){
        return sdf5.format(new Date(timestamp));
    }
    
    /** 获取MM-dd HH:mm:ss格式的时间*/
    public static String getMDHMS(long timestamp){
        return sdf4.format(new Date(timestamp));
    }
    
    /** 获取 _yyyyMMdd_HH:mm 格式的时间*/
    public static String get_yyyyMD_HM(long timestamp){
        return sdf6.format(new Date(timestamp));
    }
    
    /** 获取 yyyy-MM-dd-HH-mm-ss 格式的时间*/
    public static String getTimeFile(long timestamp){
        return sdf7.format(new Date(timestamp));
    }
    
    /** 转化 yyyy-MM-dd HH:mm:ss 为时间戳*/
    public static long parseTime(String time) {
        try {
            return sdf8.parse(time).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return System.currentTimeMillis();
    }

    /** 转化 yyyy-MM-dd 为时间戳*/
    public static String getYMD(long timestamp) {
        return sdf9.format(new Date(timestamp));
    }
}