package com.android.util.map.baidu;

import java.io.Serializable;

import com.android.util.system.Util;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-8-9
 * @see : 搜索线路的条件信息
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class SearchLine implements Serializable
{
    private static final long serialVersionUID = -5990966446144580130L;
    
    /** 终点地址*/
    public String endArea;
    /** 终点城市*/
    public String endCity;
    /** 终点经纬度*/
    public int endX;
    public int endY;
    /** 线路类型*/
    public int routeType;
    /** 起点地址*/
    public String startArea;
    /** 起点城市*/
    public String startCity;
    /** 起点经纬度*/
    public int startX;
    public int startY;
    
    public boolean isValid()
    {
        return !Util.isEmpty(startArea) && !Util.isEmpty(endArea)
                && Math.abs(startY)  < 90E6
                && Math.abs(startY)  > 0
                && Math.abs(startX) <= 180E6
                && Math.abs(startX) > 0
                && Math.abs(endY)  < 90E6
                && Math.abs(endY)  > 0
                && Math.abs(endX) <= 180E6
                && Math.abs(endX) > 0;
    }
}
