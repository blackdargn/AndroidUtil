package com.android.util.system;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.android.util.bus.MessageBus;
import com.android.util.db.DB;
import com.android.util.image.ImageLoader;
import com.android.util.location.baidu.BDLocationProvider;
import com.android.util.manager.BaseManager;
import com.android.util.update.UpdateManager;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-8-6
 * @see : 应用上下文
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class MyApplication extends Application 
{
    private static MyApplication instance;
    public Handler mHandler;
    public SharedPreferences mPreference;
    public boolean isFirst = true;
    public boolean isGpsTip = false;
    public boolean isShortCutTip = false;
    public boolean isLastVer = false;
    private DisplayMetrics dm;
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;
        mHandler = new Handler();
        mPreference = PreferenceManager.getDefaultSharedPreferences(this);
        MessageBus.getBusFactory();
        BaseManager.initHandle();
        BDLocationProvider.getInstance().init(this);
        DB.initDB(this);
        ImageLoader.init(this, 0, Util.getExternDir("/nearshop/img/"));
    }
    
    @Override
    public void onTerminate()
    {
        BDLocationProvider.getInstance().destory();
        DB.closeDB();
        super.onTerminate();
    }
    
    public void exit()
    {
        BDLocationProvider.getInstance().destory();
        UpdateManager.getInstacnce(this).destory();
        Process.killProcess(Process.myPid());
    }
    
    public static MyApplication getContext()
    {
        return instance;
    }

    public boolean isNetWorkAvailable()
    {
        ConnectivityManager connectMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectMgr.getActiveNetworkInfo() != null)
        {
            return connectMgr.getActiveNetworkInfo().isAvailable();
        }else
        {
            return false;
        }
    }    
        
    // 是否快捷开关
    public boolean isShortCut()
    {
        return mPreference.getBoolean("short_cut", false);
    }    
    // 设置快捷开关
    public void setShortCut(boolean have)
    {
        mPreference.edit().putBoolean("short_cut", have).commit();
    }
    
    public float getDensity()
    {
        return dm != null ? dm.density : 1.0f;
    }

    public void setDensity(DisplayMetrics dm)
    {
        this.dm = dm;
    }
    
    public int getDmWidth()
    {
        return dm != null ? dm.widthPixels : 480;
    }
    
    public int getDmHigh()
    {
        return dm != null ? dm.heightPixels : 800;
    }
}
