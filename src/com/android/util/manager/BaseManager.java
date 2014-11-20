/*******************************************************
 * @作者: zhaohua
 * @日期: 2012-6-5
 * @描述: 管理器接口，用于处理初始化 与 销毁工作
 * @声明: copyrights reserved by Petfone 2007-2011
*******************************************************/
package com.android.util.manager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.android.util.thread.NotifyListener;
import com.android.util.thread.UINotifyListener;

public abstract class BaseManager
{
	public static final int MSG_NOTIFY_ON_ERROR = 1;
	public static final int MSG_NOTIFY_ON_SUCCEED = 2;
	public static class NotifyHolder
	{
		public UINotifyListener<?> listener;
		public Object data;
	}
	
	public static void callback(int what, NotifyListener<?> listener, Object data)
	{
	    if(listener == null) return;
	    
		if(listener instanceof UINotifyListener)
		{
			NotifyHolder holder = new NotifyHolder();
			holder.listener =(UINotifyListener<?>) listener;
			holder.data = data;
			Message msg = new Message();
			msg.what = what;
			msg.obj = holder;
			
			mainHandler.sendMessage(msg);
		}
		else
		{
		    try {
    			switch(what)
    			{
        			case MSG_NOTIFY_ON_ERROR:
        			{
        				listener.notify(data,false);
        				break;
        			}
        			case MSG_NOTIFY_ON_SUCCEED:
        			{
        				listener.notify(data,true);
        				break;
        			}
    			}
		    }catch(Exception e) {
		        e.printStackTrace();
		    }
		}
	}
	protected static MainHandler mainHandler;
	protected static class MainHandler extends Handler
	{
		public MainHandler()
		{
			super(Looper.getMainLooper());
		}
		
		public void handleMessage(android.os.Message msg) 
		{
			NotifyHolder holder = (NotifyHolder)msg.obj;
			try {
    			switch(msg.what)
    			{
        			case MSG_NOTIFY_ON_ERROR:
        			{
        				holder.listener.notify(holder.data, false);
        				break;
        			}
        			case MSG_NOTIFY_ON_SUCCEED:
        			{
        				holder.listener.notify(holder.data, true);
        				break;
        			}
    			}
			}catch(Exception e) {
			    e.printStackTrace();
			}
		};
	};
	
	public BaseManager()
	{
	    initHandle();
	}
	
	/** 初始化 */
	public abstract void init();

	/** 销毁 */
	public abstract void destory();
	
	/** 初始化Handler*/
	public static void initHandle() {
	    if(mainHandler == null) {
            mainHandler = new MainHandler();
        }
	}
	
	/** 全局初始化*/
	public static void initAll()
	{
	    // TODO
	    
	}
	
	/** 全局销毁*/
	public static void destoryAll()
	{
	    // TODO
	}
}