/*******************************************************
 * @作者: zhaohua
 * @日期: 2011-12-02
 * @描述: 日志统一入口类，管理整个应用程序的日志调式开关！
 * @声明: copyrights reserved by personal 2007-2011
 *******************************************************/
package com.android.util.system;

public class Log
{
	public static final boolean DEBUG = true;
	
	public static void d(String tag,String msg)
	{
		if(DEBUG)
		{
			// 下面这么做是为了在非Android环境下测试时打印LOG，下同
			try
			{
				android.util.Log.d(tag,msg);
			}
			catch (Exception e)
			{
				System.out.println( tag + "\t" + msg);
			}
		}
	}

	public static void i(String tag,String msg)
	{
		if(DEBUG)
		{
			try
			{
				android.util.Log.i(tag,msg);
			}
			catch (Exception e)
			{
				System.out.println(tag + "\t" + msg);
			}
		}
	}
	
	public static void e(String tag,String msg)
	{
		if(DEBUG)
		{
			try
			{
				android.util.Log.e(tag,msg);
			}
			catch (Exception e)
			{
				System.out.println(tag + "\t" + msg);
			}
		}
	}
	
	public static void w(String tag,String msg)
	{
		if(DEBUG)
		{
			try
			{
				android.util.Log.w(tag,msg);
			}
			catch (Exception e)
			{
				System.out.println(tag + "\t" + msg);
			}
		}
	}
	
	public static void v(String tag,String msg)
	{
		if(DEBUG)
		{
			try
			{
				android.util.Log.v(tag,msg);
			}
			catch (Exception e)
			{
				System.out.println(tag + "\t" + msg);
			}
		}
	}
	
	public static void d(String tag,String msg, Throwable e)
	{
		if(DEBUG)
		{
			try
			{
				android.util.Log.d(tag,msg);
			}
			catch (Exception ex)
			{
				System.out.println(tag + "\t" + msg);
			}
		}
	}

	public static void i(String tag,String msg, Throwable e)
	{
		if(DEBUG)
		{
			try
			{
				android.util.Log.i(tag,msg);
			}
			catch (Exception ex)
			{
				System.out.println(tag + "\t" + msg);
			}
		}
	}
	
	public static void e(String tag,String msg, Throwable e)
	{
		if(DEBUG)
		{
			try
			{
				android.util.Log.e(tag,msg);
			}
			catch (Exception ex)
			{
				System.out.println(tag + "\t" + msg);
			}
		}
	}
	
	public static void w(String tag,String msg, Throwable e)
	{
		if(DEBUG)
		{
			try
			{
				android.util.Log.w(tag,msg);
			}
			catch (Exception ex)
			{
				System.out.println(tag + "\t" + msg);
			}
		}
	}
	
	public static void v(String tag,String msg, Throwable e)
	{
		if(DEBUG)
		{
			try
			{
				android.util.Log.v(tag,msg);
			}
			catch (Exception ex)
			{
				System.out.println(tag + "\t" + msg);
			}
		}
	}
}
