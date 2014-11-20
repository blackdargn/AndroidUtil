/*******************************************************
 * @作者: zhaohua
 * @日期: 2011-12-7
 * @描述: 系统公用 提醒管理者，用于 铃声、震动提醒
 * @声明: copyrights reserved by Petfone 2007-2011
 *******************************************************/
package com.android.util.system;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.RemoteViews;

/**
 * @author zhaohua
 *
 */
public class MyNotification
{
	private static final String TAG = "MyNotification";
	
	/**
	 * @see 显示通知
	 * @param context -- 上下文
	 * @param notifyId -- 通知ID，自定义
	 * @param iconRes -- 通知的图标RID
	 * @param ticker -- 通知在状态栏的滚动信息
	 * @param clickIntent -- 点击通知的跳转事件Intent
	 * @param customView -- 自定义通知的视图，下面的timeMillis，description，title就无关紧要了。
	 * @param timeMillis -- 系统显示时间
	 * @param description -- 通知展开后的描述
	 * @param title -- 通知展开后的标题
	 * @param isVibrate --是否振动
	 * @param ringtone -- 设置提示声音，null，没声音
	 */
	private static void showNotification(
			Context context,
			int notifyId,
			int iconRes, 
			CharSequence ticker, 
			Intent clickIntent, 
			RemoteViews customView,
			long timeMillis, 
			String description, 
			String title,
			boolean isVibrate,
			String ringtone,
			int flag)
	{
		Log.d(TAG, "updateNotification");
		
		// 初始化一个通知
		Notification notification = new Notification(iconRes, ticker,timeMillis);

		// 获取点击通知后的转向动作Intent
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// 设置事件信息
		notification.setLatestEventInfo(context, title, description, pendingIntent);

		// 设置自定义视图
		if(customView != null)
		{
			notification.contentView = customView;
		}
		
		// 设置振动
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		boolean nowSilent = audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
		if (isVibrate || nowSilent)
		{
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}
		// 设置声音，如果设置了的话
		notification.sound = TextUtils.isEmpty(ringtone) ? null : Uri.parse(ringtone);
		// 设置通知的其它属性
		// 点亮屏幕
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		// 设置通知的生命周期属性
		notification.flags |= flag;
		// 设置闪光提示
		notification.defaults |= Notification.DEFAULT_LIGHTS;

		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		nm.notify(notifyId, notification);
	}
	
	/**
	 * @see 显示系统通知，可被清楚。
	 * @param context -- 上下文
	 * @param notifyId -- 通知ID，自定义
	 * @param iconRes -- 通知的图标RID
	 * @param ticker -- 通知在状态栏的滚动信息
	 * @param clickIntent -- 点击通知的跳转事件Intent
	 * @param timeMillis -- 系统显示时间
	 * @param description -- 通知展开后的描述
	 * @param title -- 通知展开后的标题
	 * @param isVibrate --是否振动
	 * @param ringtone -- 设置提示声音，null，没声音
	 */
	public static void showNotification(
			Context context,
			int notifyId,
			int iconRes, 
			CharSequence ticker, 
			Intent clickIntent, 
			long timeMillis, 
			String description, 
			String title,
			boolean isVibrate,
			String ringtone)
	{
		showNotification(context,notifyId,iconRes,ticker,clickIntent,null,timeMillis,description,title,isVibrate,ringtone
				,Notification.FLAG_AUTO_CANCEL);
	}
	
	/**
	 * @see 显示自定义通知，以后台正在运行存在，直到用户响应才取消。
	 * @param context -- 上下文
	 * @param notifyId -- 通知ID，自定义
	 * @param iconRes -- 通知的图标RID
	 * @param ticker -- 通知在状态栏的滚动信息
	 * @param clickIntent -- 点击通知的跳转事件Intent
	 * @param customView -- 自定义通知的视图
	 * @param isVibrate --是否振动
	 * @param ringtone -- 设置提示声音，null，没声音
	 */
	public static void showRunningNotification(
			Context context,
			int notifyId,
			int iconRes, 
			CharSequence ticker, 
			Intent clickIntent, 
			RemoteViews customView,
			boolean isVibrate,
			String ringtone)
	{
		showNotification(context,notifyId,iconRes,ticker,clickIntent,customView,0,null,null,isVibrate,ringtone
				,Notification.FLAG_NO_CLEAR);
	}
	
	/**
	 * @see 显示消息到达通知。
	 * @param context -- 上下文
	 * @param notifyId -- 通知ID，自定义
	 * @param iconRes -- 通知的图标RID
	 * @param ticker -- 通知在状态栏的滚动信息
	 * @param clickIntent -- 点击通知的跳转事件Intent
	 * @param customView -- 自定义通知的视图
	 * @param isVibrate --是否振动
	 * @param ringtone -- 设置提示声音，null，没声音
	 */
	public static void showMsgNotification(
			Context context,
			int notifyId,
			int iconRes, 
			CharSequence ticker, 
			Intent clickIntent, 
			RemoteViews customView,
			boolean isVibrate,
			String ringtone)
	{
		showNotification(context,notifyId,iconRes,ticker,clickIntent,customView,0,null,null,isVibrate,ringtone
				,Notification.FLAG_AUTO_CANCEL);
	}
	
	/**
	 * @see 显示自定义通知,以服务形式一直存在，直到主动退出
	 * @param context -- 上下文
	 * @param notifyId -- 通知ID，自定义
	 * @param iconRes -- 通知的图标RID
	 * @param ticker -- 通知在状态栏的滚动信息
	 * @param clickIntent -- 点击通知的跳转事件Intent
	 * @param customView -- 自定义通知的视图
	 * @param isVibrate --是否振动
	 * @param ringtone -- 设置提示声音，null，没声音
	 */
	public static void showServiceNotification(
			Context context,
			int notifyId,
			int iconRes, 
			CharSequence ticker, 
			Intent clickIntent, 
			RemoteViews customView,
			boolean isVibrate,
			String ringtone)
	{
		showNotification(context,notifyId,iconRes,ticker,clickIntent,customView,0,null,null,isVibrate,ringtone
				,Notification.FLAG_INSISTENT);
	}
	
    /**
     * @see 取消通知
     * @param context -- 上下文
     * @param notificationId -- 显示通知的id
     */
    public static void cancelNotification(Context context, int notificationId) 
    {	
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        nm.cancel(notificationId);
    }
}
