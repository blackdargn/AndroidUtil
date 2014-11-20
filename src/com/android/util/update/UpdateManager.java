
package com.android.util.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.util.R;
import com.android.util.system.MyApplication;
import com.android.util.thread.NotifyListener;
import com.android.util.update.Downloader.OnDownListener;
import com.android.util.update.Downloader.OnDownOKListener;

/*******************************************************
 * @author: zhaohua
 * @version: 2012-08-21
 * @see: 软件更新后台任务管理器
 *      检测版本,
            1.如有必须更新的版本则弹出更新加载框，
            2.如有可更新的版本，则提示用户有更新的新版本，也可取消此次更新，以后更新
            3.如果没有更新，则什么都不做
 * @Copyright: copyrights reserved by personal 2007-2012
*******************************************************/
public class UpdateManager
{
	public static final String TAG = "UpdateManager";
	
	private Downloader updateLoader;
	/** 单列模式*/
	private static UpdateManager instance;
	private Context context;
	private RemoteViews notifyViews;
	private NotificationManager nm;
	private Notification notification;
	private int notificationId=8888;
	private String downPrecntStr = "已下载%s";
	private PackageInfo pinfo;
	/** 检测结果枚举*/
	public enum CHECK_RESULT
	{
		/** 无需更新*/
		NONE,
		/** 可以更新*/
		HAVE,
		/** 必须更新*/
		NEED,
		/** 更新失败*/
		FAIL
	}
	
	public static UpdateManager getInstacnce(Context context)
	{
		if(instance == null)
		{
			instance = new UpdateManager(context);
		}
				
		return instance;
	}
	
	private UpdateManager(Context context)
	{
		this.context = context;
		String pName = context.getPackageName();            
        try
        {
            pinfo = context.getPackageManager().getPackageInfo(pName, PackageManager.GET_CONFIGURATIONS);
        }catch(NameNotFoundException e)
        {                   
            e.printStackTrace();
        }
	}
	
	/** 开始更新检测 */
	public void startCheck(final String checkPath, final boolean jsonOrXml, final OnUpdateListener updateListener)
	{
		new AsyncTask<Void, Void, UpdateInfo>()
		{			
			@Override
			protected UpdateInfo doInBackground(Void... params)
			{        
			    // 推送自身版本信息到指定接口，接口返回更新信息
		        String pName = context.getPackageName();            
		        try
		        {
		            pinfo = context.getPackageManager().getPackageInfo(pName, PackageManager.GET_CONFIGURATIONS);
		        }catch(NameNotFoundException e)
		        {		            
		            e.printStackTrace();
		            return null;
		        }

                URL url = null;
				try
				{
					url = new URL(checkPath);
				}
				catch (MalformedURLException e)
				{
					e.printStackTrace();
					return null;
				}
                                
                HttpURLConnection conn = null;
    			InputStream ins = null;   			
    			try
				{
					conn = (HttpURLConnection) url.openConnection();
					conn.setReadTimeout(3000);
					ins = conn.getInputStream();
					if(jsonOrXml)
					{
					    return parseJson(ins);
					}else
					{
					    return parseXml(ins);
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();					
					return null;
				}finally
				{
					if(conn != null)
					{
						conn.disconnect();
						conn = null;
					}
					
					if(ins != null)
					{
						try
						{
							ins.close();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						
						ins = null;
					}
				}
			}
			
			protected void onPostExecute(UpdateInfo result)
			{			
			    CHECK_RESULT mode = null;
				if(result == null)
				{
					// 检测出错， 弹出出错提示框
				    mode = CHECK_RESULT.FAIL;
				}else
				{
				    // 检测更新模式				    
				    if(pinfo.versionCode == result.version)
				    {
				        mode = CHECK_RESULT.NONE;
				    }else
				    if(pinfo.versionCode < result.version)
				    {
				        mode = CHECK_RESULT.NEED;
				    }else
				    {
				        mode = CHECK_RESULT.HAVE;
				    }					
				}
				// 将更新结果给监听者处理
				if(updateListener != null)
				{
				    updateListener.onUpdate(mode, result);
				}
			};
		}.execute();
	}
	
	/** 开始更新检测 */
	public void startCheck(final OnUpdateListener updateListener)
	{
	    /** 
	    VersionAction action = new VersionAction();
	    action.setActionListener(new ActionListener<Version>()
        {
            @Override
            public void onSucceed(Version result)
            {
                if(result != null)
                {                    
                    UpdateInfo updateInfo = new UpdateInfo();
                    updateInfo.path = result.downUrl;
                    updateInfo.desc = result.content;
                    updateInfo.size = result.sysSize;
                    updateInfo.time = result.updateTime;
                    updateInfo.version = result.versionCode;
                    updateInfo.versionName = result.versionName;
                    if(pinfo.versionCode < result.versionCode)
                    {
                        updateListener.onUpdate(CHECK_RESULT.HAVE, updateInfo);
                    }else
                    {
                        updateListener.onUpdate(CHECK_RESULT.NONE, null);
                    }
                }else
                {
                    updateListener.onUpdate(CHECK_RESULT.NONE, null);
                }
            }
            @Override
            public void onError(int resultCode)
            {
                updateListener.onUpdate(CHECK_RESULT.FAIL, null);
            }
        });
	    ProtocolManager.getProtocolManager().submitAction(action);
	    */
	}
	
    void checkUpdate(final Activity activity,final NotifyListener<Boolean> listener)
    {
        UpdateManager.getInstacnce(activity).startCheck(new OnUpdateListener()
        {
            @Override
            public void onUpdate(CHECK_RESULT result, UpdateInfo info)
            {
                if( info != null)
                {
                    if(result == CHECK_RESULT.NEED)
                    {
                        UpdateActivity.startNeedActivity(activity, info);
                    }else
                    if(result == CHECK_RESULT.HAVE)
                    {
                        UpdateActivity.startHaveActivity(activity, info);
                    }else
                    if(result == CHECK_RESULT.NONE)
                    {
                        if(listener != null)
                        {
                            listener.notify(true, true);
                        }
                    }else
                    {
                        if(listener != null)
                        {
                            listener.notify(-1,false);
                        }
                    }
                }
            }
        });
    }
	
	/** 开始更新操作*/
	public boolean startUpdate(String url,String saveDir,OnDownListener downListener)
	{
	    if(!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
	    {
	        return false;
	    }		
		if(updateLoader == null)
		{
			updateLoader = new Downloader(downListener);
		}	
		updateLoader.start(url, 3, saveDir);
		updateLoader.setOnDownOKListener(new OnDownOKListener()
        {
            @Override
            public void onOK(String filePath)
            {
                nm.cancel(notificationId);
                
                File apkFile = new File(filePath);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(apkFile),"application/vnd.android.package-archive");

                context.startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));                                
            }
        });
		return true;
	}
	
	public boolean startUpdateNotify(String url,String saveDir,final Intent intent)
	{
	    if(nm == null)
	    {
	        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	    }
	    return startUpdate(url, saveDir, new OnDownListener()
        {           
            @Override
            public void onStart(int size)
            {
                notification = new Notification();
                notification.icon = android.R.drawable.stat_sys_download;
                notification.tickerText = context.getString(R.string.app_name)
                        + "更新";
                notification.when = System.currentTimeMillis();
                notification.defaults = Notification.DEFAULT_LIGHTS;

                // 设置任务栏中下载进程显示的views
                notifyViews = new RemoteViews(context.getPackageName(),
                        R.layout.notifybar_download);
                notifyViews.setTextViewText(R.id.name,context.getString(R.string.app_name));
                String precent = String.format(downPrecntStr, 0+ "%");
                notifyViews.setTextViewText(R.id.precent, precent);
                notifyViews.setProgressBar(R.id.progressBar_down, 100, 0, false);
                notification.contentView = notifyViews;

                PendingIntent contentIntent = PendingIntent.getActivity(
                        context, 0, intent, 0);
                notification.setLatestEventInfo(context, "", "", contentIntent);

                // 将下载任务添加到任务栏中
                nm.notify(notificationId, notification);
            }

            @Override
            public void onProcess(final int done_size, final int size)
            {
                if (notifyViews != null)
                {
                    int donePrecent = (int) (((done_size + 0.0f) / size) * 100);
                    String precent = String.format(downPrecntStr, donePrecent
                            + "%");
                    notifyViews.setTextViewText(R.id.precent, precent);
                    notifyViews.setProgressBar(R.id.progressBar_down, 100,
                            donePrecent, false);
                    notification.contentView = notifyViews;
                    nm.notify(notificationId, notification);
                }
            }
	        
	        @Override
	        public void onFail()
	        {
	            MyApplication.getContext().mHandler.post(new Runnable()
	            {
	                @Override
	                public void run()
	                {
	                    Toast.makeText(context, "更新失败！", Toast.LENGTH_LONG).show();
	                    nm.cancel(notificationId);
	                }
	            });
	        }
        });
	}
	
	/** 停止更新操作*/
	public void cancelUpdate()
	{
		if(updateLoader != null)
		{
			updateLoader.stop();
		}
		if(nm != null)
		{
		    nm.cancel(notificationId);
		}
	}
	
	public void setOnDownListener(OnDownListener downListener)
	{
	    if(updateLoader != null)
        {
	        updateLoader.setOnDownListener(downListener);
        }
	}
	
	public void destory()
	{
	    cancelUpdate();
	}
	
	/** JSON解析*/
	private UpdateInfo parseJson(InputStream ins)
	{
	    try
        {
	        StringBuilder data = null;
            String line = null;
	        data = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(ins));
            while ((line = br.readLine()) != null)
            {
                data.append(line);
            }
            
            JSONObject json = new JSONObject(data.toString());
                      
            UpdateInfo undateInfo = new UpdateInfo();
            undateInfo.version = json.getInt("u");
            undateInfo.path = json.getString("p");
            undateInfo.size = json.getString("s");
            undateInfo.versionName = json.getString("n");
            undateInfo.time = json.getString("t");
            undateInfo.desc = json.getString("d");
            
            return undateInfo;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        } catch (IOException e) 
        {
            e.printStackTrace();
        }
        
        return null;
	}
	
	/** XML解析*/
	private UpdateInfo parseXml(InputStream ins)
	{
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setIgnoringComments(true);
	    factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            
            Document document = builder.parse(ins);
            Element root = document.getDocumentElement();
            NodeList childs = root.getChildNodes();
            int len = childs.getLength();
            UpdateInfo undateInfo = new UpdateInfo();
            for (int i =0 ; i < len; i++) 
            {
                Node node = childs.item(i);
                if(node instanceof Element)
                {
                    Element ele = (Element)node;
                    String name = ele.getNodeName();
                    String value = ele.getNodeValue();
                    if (name.equals("u")) {
                        undateInfo.version = Integer.parseInt(value);
                    } else if (name.equals("p")) {
                        undateInfo.path = value;
                    } else if (name.equals("s")) {
                        undateInfo.size = value;
                    } else if (name.equals("n")) {
                        undateInfo.versionName = value;
                    } else if (name.equals("t")) {
                        undateInfo.time = value;
                    } else if (name.equals("d")) {
                        undateInfo.desc = value;
                    }
                }
            }
            return undateInfo;
        } catch (ParserConfigurationException e) 
        {
            e.printStackTrace();
        } catch (SAXException e) 
        {
            e.printStackTrace();
        } catch (IOException e) 
        {
            e.printStackTrace();
        }       
	    return null;
	}
		
	/** 更新监听器 */
	public static interface OnUpdateListener
	{
	    public void onUpdate(CHECK_RESULT result, UpdateInfo info);
	}	

	/*******************************************************
	 * @author: zhaohua
	 * @version: 2012-8-21
	 * @see: 更新信息
	 * @Copyright: copyrights reserved by personal 2007-2012
	*******************************************************/
	public static class UpdateInfo implements Serializable
	{
	    private static final long serialVersionUID = -2607538219806426510L;

	    /** 服务器最新版本代码 */
	    public int version;
	    /** 版本名称*/
	    public String versionName ="";
	    /** 更新时间*/
	    public String time = "";
	    /** 版本大小*/
	    public String size = "";
	    /** 版本路径*/
	    public String path = "";
	    /** 更新描述*/
	    public String desc = "";
	}
}
