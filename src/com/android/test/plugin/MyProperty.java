package com.android.test.plugin;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apkplug.app.PropertyInstance;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;

public class MyProperty implements PropertyInstance{
	private   Context context;
	private static MyProperty _instance=null;
	public MyProperty(Context context){
		this.context=context;
	}
	synchronized public static MyProperty getInstance(Context context){
    if(_instance==null){
    _instance=new MyProperty(context);
    }
    return _instance;
    } 

	public String getProperty(String key) {
		SharedPreferences sharedata = PreferenceManager.getDefaultSharedPreferences(this.context);
		String data = sharedata.getString(key, null);
		return data;
	}
	public void setProperty(String key, String v) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.context); 
		Editor edit = settings.edit();
		edit.putString(key, v);
		edit.commit();
	}
	public String[] AutoInstall() {
		return null;
	}
	public String[] AutoStart() {
		//把BundleDemo1.apk从assets文件夹中移至应用安装目录中
		File f=null,f1=null,f2=null,f3=null,f4=null,f5=null,f6=null,f7=null,f8=null;
		String rootFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/apk/";
		File root = new File(rootFile);
		if(!root.exists()) {
		    root.mkdirs();
		}
		/**
        try {
			f=new File(rootFile,"BundleDemoOSGIService1.apk");
			if(!f.exists())
			copy(context.getAssets().open("BundleDemoOSGIService1.apk"), f);
		} catch (IOException e) {
			e.printStackTrace();
		}
        //应该先启动 服务 再启动获取服务的插件
        try {
			f1=new File(rootFile,"BundleDemoOSGIService2.apk");
			if(!f1.exists())
			copy(context.getAssets().open("BundleDemoOSGIService2.apk"), f1);
		} catch (IOException e) {
			e.printStackTrace();
		}
        try {
			f2=new File(rootFile,"BundleDemoJni.apk");
			if(!f2.exists())
			copy(context.getAssets().open("BundleDemoJni.apk"), f2);
		} catch (IOException e) {
			e.printStackTrace();
		}
        //插件Activiyt
        try {
			f3=new File(rootFile,"BundleDemoStartActivity1.apk");
			if(!f3.exists())
			copy(context.getAssets().open("BundleDemoStartActivity1.apk"), f3);
		} catch (IOException e) {
			e.printStackTrace();
		}
      //应该先启动 服务 再启动获取服务的插件
        try {
			f4=new File(rootFile,"BundleDemoTheme.apk");
			if(!f4.exists())
			copy(context.getAssets().open("BundleDemoTheme.apk"), f4);
		} catch (IOException e) {
			e.printStackTrace();
		}
      //应该先启动 服务 再启动获取服务的插件
        try {
			f5=new File(rootFile,"BundleDemoShow.apk");
			if(!f5.exists())
			copy(context.getAssets().open("BundleDemoShow.apk"), f5);
		} catch (IOException e) {
			e.printStackTrace();
		}
      //插件托管服务测试
        try {
			f6=new File(rootFile,"BundleDemoApkplugService.apk");
			if(!f6.exists())
			copy(context.getAssets().open("BundleDemoApkplugService.apk"), f6);
		} catch (IOException e) {
			e.printStackTrace();
		}
        //插件托管服务测试
        try {
			f7=new File(rootFile,"BundleDemoUpdate.apk");
			if(!f7.exists())
			copy(context.getAssets().open("BundleDemoUpdate.apk"), f7);
		} catch (IOException e) {
			e.printStackTrace();
		}
        //插件托管服务测试
        try {
			f8=new File(rootFile,"ActivityForResultDemo.apk");
			if(!f8.exists())
			copy(context.getAssets().open("ActivityForResultDemo.apk"), f8);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	return null;
	/**   ,"file:"+f1.getAbsolutePath(),"file:"+f2.getAbsolutePath(),"file:"
           +f3.getAbsolutePath(),"file:"+f4.getAbsolutePath(),"file:"
           +f5.getAbsolutePath(),"file:"+f6.getAbsolutePath() ,"file:"
           +f7.getAbsolutePath(),"file:"+f8.getAbsolutePath()*/
	}
	
	void copy(InputStream is, File outputFile) throws IOException
	    {
	        OutputStream os = null;

	        try
	        {	            
	            os = new BufferedOutputStream(
	                new FileOutputStream(outputFile),4096);
	            byte[] b = new byte[4096];
	            int len = 0;
	            while ((len = is.read(b)) != -1)
	                os.write(b, 0, len);
	        }
	        finally
	        {
	            if (is != null) is.close();
	            if (os != null) os.close();
	        }
	    }
	@Override
	public boolean Debug() {
		return false;
	}
}
