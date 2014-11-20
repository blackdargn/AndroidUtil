package com.android.util.update;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.android.util.db.DB;

/*******************************************************
 * @author: zhaohua
 * @version: 2012-08-21
 * @see: 多线程分段断点续传数据库操作
 * @Copyright: copyrights reserved by personal 2007-2012
 *******************************************************/
public class DownloadDao extends DB
{
	private static final String TAG = "InfoDao";
	
	public static final String TABLE_NAME = "download";
	public static final String CREATE_TABLE = 
		"CREATE TABLE ["+TABLE_NAME+"](path VARCHAR(1024), thid INTEGER, done INTEGER, partlen INTEGER, PRIMARY KEY(path, thid));";	
	public static final String DROP_TABLE = "DROP TABLE if exists " + TABLE_NAME ;
	
	public void insert(DownloadInfo info)
	{
		SQLiteDatabase db = getDB(true);
		db.execSQL("INSERT INTO "+TABLE_NAME+"(path, thid, done, partlen) VALUES(?, ?, ?, ?)",
				new Object[] { info.getPath(), info.getThid(), info.getDone(), info.getPartLen() });
	}

	public void delete(String path, int thid)
	{
		SQLiteDatabase db = getDB(true);
		db.execSQL("DELETE FROM "+TABLE_NAME+" WHERE path=? AND thid=?", new Object[] {
				path, thid });
	}

	public void update(DownloadInfo info)
	{
		SQLiteDatabase db = getDB(true);
		db.execSQL("UPDATE "+TABLE_NAME+" SET done=? WHERE path=? AND thid=?",
				new Object[] { info.getDone(), info.getPath(), info.getThid() });
	}

	public DownloadInfo query(String path, int thid)
	{
		SQLiteDatabase db = getDB(false);
		Cursor c = null;
		DownloadInfo info = null;
		
		try{
			c = db.rawQuery(
					"SELECT path, thid, done, partlen FROM "+TABLE_NAME+" WHERE path=? AND thid=?",
					new String[] { path, String.valueOf(thid) });
									
			if (c.moveToNext())
			{
				info = new DownloadInfo(c.getString(0), c.getInt(1), c.getInt(2), c.getInt(3));
			}	
		}
		catch(SQLiteException e)
		{
			e.printStackTrace();
		}finally
		{
			if(c != null)
			{
				c.close();
				c = null;
			}
		}
		
		return info;
	}

	public boolean deleteAll(String path, int len)
	{
		SQLiteDatabase db = getDB(true);
		Cursor c = null;
		
		try{
			c = db.rawQuery("SELECT SUM(done) FROM "+TABLE_NAME+" WHERE path=?", new String[] { path });
			
			if (c.moveToNext())
			{
				int result = c.getInt(0);
				
				c.close();
				c = null;
				
				if (result == len)
				{
					db.execSQL("DELETE FROM "+TABLE_NAME+" WHERE path=? ", new Object[] { path });
					return true;
				}else
				{
					return false;
				}
			}
		}catch(SQLiteException e)
		{
			e.printStackTrace();
		}finally
		{
			if(c != null)
			{
				c.close();
			}
		}
		
		return false;
	}

	public List<String> queryUndone()
	{
		SQLiteDatabase db = getDB(false);
		Cursor c = null;
		List<String> pathList = new ArrayList<String>();
		
		try{
			c = db.rawQuery("SELECT DISTINCT path FROM "+TABLE_NAME, null);			
			while (c.moveToNext())
			{
				pathList.add(c.getString(0));
			}
		}catch(SQLiteException e)
		{
			e.printStackTrace();
		}finally
		{
			if(c != null)
			{
				c.close();
				c = null;
			}
		}
		return pathList;
	}
	
	public static void createTable(SQLiteDatabase db)
	{
		try{
			db.execSQL(CREATE_TABLE);
		}catch(SQLException e)
		{
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}

	public static void deleteTable(SQLiteDatabase db)
	{
		try{
			db.execSQL(DROP_TABLE);
		}catch(SQLException e)
		{
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}

	/*********************************************************
	 * @author : zhaohua
	 * @version : 2012-8-21
	 * @see :   下载信息
	 * @Copyright : copyrights reserved by personal 2007-2012
	 **********************************************************/
	static class DownloadInfo implements Serializable
	{
	    private static final long serialVersionUID = -7377794345620959589L;
	    private String path;
	    private int thid;
	    private int done;
	    private int partLen;

	    public DownloadInfo(String path, int tid, int doneSize, int partLen)
	    {
	        this.path = path;
	        this.thid = tid;
	        this.done = doneSize;
	        this.partLen = partLen;
	    }

	    /**
	     * @return
	     */
	    public String getPath()
	    {
	        return path;
	    }

	    /**
	     * @return
	     */
	    public int getDone()
	    {
	        return done;
	    }

	    /**
	     * @return
	     */
	    public int getThid()
	    {
	        return thid;
	    }

	    /**
	     * @return
	     */
	    public int getPartLen()
	    {
	        return partLen;
	    }

	    /**
	     * @param i
	     */
	    public void setDone(int done)
	    {
	        this.done = done;
	    }
	}
}