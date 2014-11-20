/*******************************************************
 * @作者: zhaohua
 * @日期: 2012-05-29
 * @描述: 基本包
 * @声明: copyrights reserved by Petfone 2007-2011
*******************************************************/
package com.android.util.protocol.TUDP;

import com.android.util.system.Dumper;

public abstract class Packet extends Dumper
{	   
	public int getCmd()
    {
        return getCmd(this.getClass());
    }
    
    public static int getCmd(@SuppressWarnings("rawtypes") Class obj)
    {
        try
        {
            String cls = obj.getSimpleName();
            byte m = (byte)cls.charAt(0);
            byte s = Byte.parseByte(cls.substring(1));
            int cmd = m<<8 | s;
            
            return cmd;
        }
        catch (Exception e)
        {
            return 0;
        }
    }
    
    public char getMainCmd()
    {
        try
        {
            String cls = getClass().getSimpleName();
            char m = cls.charAt(0);
            return m;
        }
        catch (Exception e)
        {
            return 0;
        }
    }
    
    public byte getSubCmd()
    {
        try
        {
            String cls = getClass().getSimpleName();
            byte s = Byte.parseByte(cls.substring(1));
            return s;
        }
        catch (Exception e)
        {
            return 0;
        }
    }
}