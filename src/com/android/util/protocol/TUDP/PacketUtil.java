/*******************************************************
 * @作者: zhaodh
 * @日期: 2011-12-19
 * @描述: 协议工具
 * @声明: copyrights reserved by 2007-2011
 *******************************************************/
package com.android.util.protocol.TUDP;

import java.io.UnsupportedEncodingException;

public class PacketUtil
{
    
    /**
     * 将字符串转化为指定编码的指定长度的字节数组，
     * 超过则截取最大长度，编码非法则为0的数组
     * @param str 字符串
     * @param len 数组长度
     * @param code 编码
     * @return
     */
    public static byte[] string2Byte(String str, int len, String code)
    {
        byte[] bytes = new byte[len];
        byte[] strBytes = null;
        try {
            if(str != null)
            {
                strBytes = str.getBytes(code);
                int strBytesLen = strBytes.length;
                System.arraycopy(strBytes, 0, bytes, 0, strBytesLen < len ? strBytesLen : len);
            }
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return bytes;
    }
    
    /** 字节数组转化为指定编码的字符串*/
    public static String byte2String(byte[] bytes, String code)
    {
        try
        {
            return new String(bytes, code).trim();
        }
        catch (UnsupportedEncodingException e1)
        {
            e1.printStackTrace();
        }
        return "";
    }
}
