package com.android.util.map.baidu;

import java.io.Serializable;

import com.android.util.system.Util;
import com.baidu.platform.comapi.basestruct.GeoPoint;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-8-9
 * @see : 地址封装，包括名称与经纬度
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class Addr implements Serializable
{
    private static final long serialVersionUID = -238860559554909452L;
    /** 全地址*/
    public String  address;
    /** 城市名称*/
    public String  city;
    /** 区县名称*/
    public String  district;
    /** 经度*/
    public double longitude;
    /** 维度*/
    public double latitude;
    
    public Addr()
    {
        
    }
    
    public Addr(double longitude,double latitude,String  name)
    {
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = name;
    }
    
    public GeoPoint toGeoPoint()
    {
        return Util.toGeoPoint(longitude, latitude);
    }

    public boolean isValid()
    {
        return Math.abs(latitude)  <= 90
        && Math.abs(latitude)  > 0
        && Math.abs(longitude) <= 180
        && Math.abs(longitude) > 0;
    }

    public String toString()
    {
        return longitude + "," + latitude + "," + address;
    }
}
