package com.android.util.location.baidu;

/** 地址实体 */
public class Address
{
    /** 中国广东省深圳市福田区福中三路深圳市人民政府 */
    public String address = null;
    /** 广东省 */
    public String province = "";
    /** 深圳市 */
    public String city = "";
    /** 福田区 */
    public String county = "";
    /** 福中三路 */
    public String route = "";
    /** 深圳市人民政府 */
    public String line = "";

    public String getFixAdr()
    {
        return province + city + county;
    }

    public String getActAdr()
    {
        return (route != null ? route : "") + (line != null ? line : "");
    }

    public String getAddress()
    {
        return  province + city + county + (route != null ? route : "") + (line != null ? line : "");
    }
}