/*******************************************************
 * @作者: zhaohua
 * @日期: 2011-9-23,2011-10-11
 * @描述: 管理数据库的上下文单实例类
 * @声明: copyrights reserved by personal 2007-2011
 *******************************************************/
package com.android.util.db;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OpenHelperManager;

public class DB
{
	/**
	 * 主要数据库
	 */
	private static DatabaseHelper databaseHelper;
	
	private static Context context;
	
	public DatabaseHelper getHelper()
    {
        return databaseHelper;
    }
	
	/** 获取单实例 */
	public static synchronized boolean initDB(Context _context)
	{
	    if(_context == null) return false;
		if( databaseHelper == null && _context != null)
		{
		    context = _context;
			databaseHelper = new DatabaseHelper(context);
		}
		return true;
	}
	
	/**
	 * @see 获取数据库
	 * @param readorWrite : 
	 *        true:getWritableDatabase||false:getReadableDatabase
	 * @return
	 */
	public static SQLiteDatabase getDB(boolean readorWrite)
	{
	    if(databaseHelper == null) return null;
		if(readorWrite)
		{
			return databaseHelper.getWritableDatabase();
		}else
		{
			return databaseHelper.getReadableDatabase();
		}
	}
	
	/**
	 * @see 应用退出时，调用，关闭数据库
	 */
	public static void closeDB()
	{
		if (null != databaseHelper)
		{
		    OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
	}

	//------------------- 数据库中的重用操作集合---------------------//
	/** 索引关键字*/
	public static final String _ID = "_id";

	/**
	 * 获取集合的数量
	 * @param table 
	 * @param section
	 * @return
	 */
	public static int getCount(String table, String section)
	{
		SQLiteDatabase db = null;
		Cursor cursor = null;
		String sql = (table != null ? ("select count(1) from " + table + (section != null ? " where " + section : ""))
					 : section);
		try
		{
			db = getDB(false);
			cursor = db.rawQuery(sql, null);
			cursor.moveToFirst();
			return cursor.getInt(0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (null != cursor)
			{
				cursor.close();
			}
		}
		return 0;
	}
	
	/**
	 * 清除数据
	 * @param table 
	 * @param section
	 * @return
	 */
	public static boolean clear(String table, String section)
	{
		SQLiteDatabase db = null;
		String sql = "delete from " + table 
					+ (section != null ? " where " + section : "");		
		try
		{
			db = getDB(true);
			db.execSQL(sql);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 修改数据
	 * @param table
	 * @param value
	 * @param section
	 * @return
	 */
	public static boolean update(String table, String value,String section)
	{
		SQLiteDatabase db = null;
		String sql = " update  " + table +" set " + value + " where " + section ;		
		try
		{
			db = getDB(true);
			db.execSQL(sql);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/** 构造 in(1,2,3,4,5,6) 语句*/
    public static String makeInState(String colName, List<Integer> ids)
    {
        StringBuilder section = new StringBuilder();
        section.append(colName + " in(");
        for( int i = 0; i < ids.size(); i++)
        {
            if(i != 0)
            {
                section.append(",");
            }
            section.append(ids.get(i)+"");
        }
        section.append(") ");
        return section.toString();
    }
}
