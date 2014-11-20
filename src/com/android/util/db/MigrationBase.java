package com.android.util.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.database.sqlite.SQLiteDatabase;
/**
 * @see SQLite supports a limited subset of ALTER TABLE. The ALTER TABLE command in SQLite 
 *   allows the user to rename a table or to add a new column to an existing table. 
 *   It is not possible to rename a column, remove a column, or add or remove constraints from a table.
 */
public  class MigrationBase
{
    public static final String TAG = "MigrationBase";
    public static final String DROP_BASE = "DROP TABLE if exists ";
    public static final String ALERT_BASE = "ALTER TABLE ";
    
    /**
     * 执行合并操作
     * @param db
     * @param newVersion
     * @param oldVersion
     */
    public static void excute(SQLiteDatabase db, int newVersion, int oldVersion)
    {
        if(newVersion > oldVersion)
        {
            onUpgrade(db, newVersion,oldVersion);
        }else
        if(newVersion < oldVersion)
        {
            onDowngrade(db, oldVersion);
        }
    }
    
	/**
	 * 向上兼容
	 * @param db
	 */
	public static void onUpgrade(SQLiteDatabase db, int newVersion, int oldVersion)
	{
	    int version = oldVersion +1;
	    while(version <= newVersion) {
    	    try {
                Method method = MigrationBase.class.getMethod("onUpgrade_"+version, new Class[] {SQLiteDatabase.class});         
                method.invoke(null, new Object[] {db});         
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } 
            version++;
	    }
	}
	
	/**
     * 向下兼容,不支持，重新初始化
     * @param db
     */
	public static void onDowngrade(SQLiteDatabase db,int oldVersion)
	{
	    dropTables(db);
	    createTables(db);
	}
	
	public static void createTables(SQLiteDatabase database)
    {

    }
    
    public static void dropTables(SQLiteDatabase database)
    {

    }
}