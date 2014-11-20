package com.android.util.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.android.util.system.AppHelper;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "main.db";
    private static final int DATABASE_VERSION = 37;

    public DatabaseHelper(Context context) {
        super(context, AppHelper.getDBPath(false, DATABASE_NAME), null,
                DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database,
            ConnectionSource connectionSource) {
        try {
            MigrationBase.createTables(database);
        } catch (Exception e) {
            logger.error(e, "创建数据库失败");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database,
            ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {           
              MigrationBase.excute(database, newVersion, oldVersion);
        } catch (Exception e) {
            logger.error(e, "更新数据库失败");
        }
    }
}