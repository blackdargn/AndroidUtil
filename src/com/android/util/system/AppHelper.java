package com.android.util.system;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.util.component.SystemService;
import com.android.util.file.FileUtil;
import com.android.util.image.ImageUtil;

public class AppHelper
{
	private static final String TAG = "AppHelper";
	
	public static final String FileSaveRoot=  "/autil/";
    public static final String ExportSaveRoot=  "/export/";
    public static final String DownFileDir =  "downCache/";
    public static final String VoiceFileDir = DownFileDir + "voice/";
    public static final String ImageFileDir = DownFileDir + "image/";
    public static final String KMLFileDir = DownFileDir + "kml/";
    public static final String VideoFileDir = DownFileDir + "video/";
    public static final String PhareFileDir = DownFileDir + "phare/";
    public static final String PhizFileDir = DownFileDir + "phiz/";
    public static final String UpdateFileDir = DownFileDir + "update/";
    
	// 如果使资源让用户不可见，可将后缀置为空
	public static final String IMG_Fix     = "";//".jpg";
	public static final String AUDIO_Fix = "";//".amr";
	public static final String VEDIO_Fix  = "";//".mp4";
	public static final String KML_Fix     = "";//".kml";
	
	 /** 裁剪图片的大小*/
    public static final int CROP_IMG_SIZE = 100;
    
	/** 获取工程的根目录*/
	public static String getFileSaveRoot() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + FileSaveRoot;
    }
	
	/**
	 * 获取数据库的存放路径，策略为
	 * 1.有卡，则主DB存放在应用程序的目录下，辅DB防止卡的目录下。
	 * 2.无卡，则主辅DB都放在应用程序目录下。
	 * @return
	 */
	public static String getDBPath(boolean mainAble, String dbName)
	{
		if(isSdcardExist())
		{
			// 有卡
			if(mainAble)
			{
				return MyApplication.getContext().getDatabasePath(dbName).getAbsolutePath();
			}else
			{
				String path = getFileSaveRoot() + "databases/";
				
				File dir = new File(path);
				if (!dir.exists())
				{
					if (!dir.mkdirs())
					{
						Log.e(TAG, "--->create databases dir fail!");
						return null;
					}
				}
				return path + dbName;
			}
		}else
		{
			// 无卡
			return MyApplication.getContext().getDatabasePath(dbName).getAbsolutePath();
		}
	}
	
	/** 判断SDcard是否存在 */
	public static boolean isSdcardExist()
	{
		return Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}
	
	public static boolean isCanDown(long size){
        long ava = getSdcardAvailableSize();
        if(ava > size){
            return true;
        }
        return false;
    }
	
	public static long getSdcardAvailableSize() {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long blockSize = sf.getBlockSize();
            long availableCount = sf.getAvailableBlocks();
            return availableCount * blockSize;
        }else {
            return 0;
        }
    }
	
	/** 获取应用下filed对应的图片的文件或者名称*/
	public static String getAppFiledImg(long fileId)
	{
	    if(fileId == 0) return null;
	    String root = FileUtil.makeDir(getFileSaveRoot() + ImageFileDir);
        return root + fileId + IMG_Fix;
	}
	
	/** 获取应用下filed对应的图片的文件或者名称*/
    public static String getAppFiledImg(long fileId, boolean isSample)
    {
        if(fileId == 0) return null;
        String root = FileUtil.makeDir(getFileSaveRoot() + ImageFileDir);
        return root + fileId + (isSample ? "_s" : "") + IMG_Fix;
    }
	
	/** 获取应用下filed对应的图片的文件*/
	public static File getAppFiledImgFile(long fileId)
	{
	    String file = getAppFiledImg(fileId);
	    return file == null ? null : new File(file);
	}
	
	/** 获取应用下对应的图片的文件*/
	public static File getAppFiledImgFile(String fileName)
	{
		if(fileName == null || fileName.trim().length() == 0) return null;
	    String root = FileUtil.makeDir(getFileSaveRoot() + ImageFileDir);
	    String file = root + fileName;
	    return file == null ? null : new File(file);
	}
	
	/** 获取应用下filed对应的图片的文件或者名称*/
    public static File getAppImg(String imgName)
    {
        String root = FileUtil.makeDir(getFileSaveRoot() + ImageFileDir);
        return new File(root + imgName);
    }
	
	/** 将目标图片重命名为filed对应的图片
	 *  如果fileId则返回null*/
	public static String mvFiledImg(String img, long fileId)
	{
	    if(img == null) return null;
	    File src = new File(img);
	    if(!src.exists())
	    {
	        return null;
	    }
	    File dest = getAppFiledImgFile(fileId);
	    if(dest == null) return null;
	    if(src.renameTo(dest))
	    {
	        return dest.getAbsolutePath();
	    }else
	    {
	        return null;
	    }
	}
	
	/** 获取应用下filed对应的语音的文件或者名称*/
    public static File getAppFiledAmr(long fileId)
    {
        String root = FileUtil.makeDir(getFileSaveRoot() + VoiceFileDir);
        return new File(root + fileId + AUDIO_Fix);
    }
    
    /** 获取应用下filed对应的语音的文件或者名称*/
    public static File getAppAmr(String amrName)
    {
        String root = FileUtil.makeDir(getFileSaveRoot() + VoiceFileDir);
        return new File(root + amrName);
    }
    
    /** 将目标语音重命名为filed对应的语音*/
    public static String mvFiledAmr(String amr, long fileId)
    {
        if(amr == null) return null;
        File src = new File(amr);
        if(!src.exists())
        {
            return null;
        }
        File dest = getAppFiledAmr(fileId);
        if(dest != null && src.renameTo(dest))
        {
            return dest.getAbsolutePath();
        }else
        {
            return null;
        }
    }
    
    /** 获取应用下filed对应的轨迹的文件或者名称*/
    public static File getAppFiledKml(long fileId)
    {
        String root = FileUtil.makeDir(getFileSaveRoot() + KMLFileDir);
        return new File(root + fileId + KML_Fix);
    }
    
    /** 获取应用下filed对应的轨迹的文件或者名称*/
    public static File getAppKml(String kmlName)
    {
        String root = FileUtil.makeDir(getFileSaveRoot() + KMLFileDir);
        return new File(root + kmlName);
    }
    
    /** 将目标语音重命名为filed对应的轨迹*/
    public static String mvFiledKml(String kml, long fileId)
    {
        if(kml == null) return null;
        File src = new File(kml);
        if(!src.exists())
        {
            return null;
        }
        File dest = getAppFiledKml(fileId);
        if(dest != null && src.renameTo(dest))
        {
            return dest.getAbsolutePath();
        }else
        {
            return null;
        }
    }
    
    /** 获取应用下filed对应的视频的文件或者名称*/
    public static File getAppFiledVideo(long fileId)
    {
        String root = FileUtil.makeDir(getFileSaveRoot() + VideoFileDir);
        return new File(root + fileId + VEDIO_Fix);
    }
    
    /** 获取应用下filed对应的视频的文件或者名称*/
    public static File getAppVideo(String amrName)
    {
        String root = FileUtil.makeDir(getFileSaveRoot() + VideoFileDir);
        return new File(root + amrName);
    }
    
    /** 将目标语音重命名为filed对应的视频*/
    public static String mvFiledVideo(String video, long fileId)
    {
        if(video == null) return null;
        File src = new File(video);
        if(!src.exists())
        {
            return null;
        }
        File dest = getAppFiledVideo(fileId);
        if(dest != null && src.renameTo(dest))
        {
            return dest.getAbsolutePath();
        }else
        {
            return null;
        }
    }
    
    /** 获取应用下id对应的短语文件*/
    public static File getUserPhare(long userid)
    {
        String root = FileUtil.makeDir(getFileSaveRoot() + PhareFileDir);
        return new File(root + userid);
    }
    
    /** 获取应用下id对应的自定义文件*/
    public static File getUserPhiz(long userid)
    {
        String root = FileUtil.makeDir(getFileSaveRoot() + PhizFileDir);
        return new File(root + userid);
    }
    
    /** 获取应用log文件*/
    public static File getAppLog()
    {
        String root = FileUtil.makeDir(getFileSaveRoot() + "logs/");
        return new File(root + "log.txt");
    }
    
    /** 获取应用logBack文件*/
    public static File getAppBackLog()
    {
        String root = FileUtil.makeDir(getFileSaveRoot() + "logs/");
        return new File(root + "log-"+System.currentTimeMillis() +".txt");
    }
    
    /** 获取应用trafficLog文件*/
    public static File getAppTrafficLog()
    {
        String root = FileUtil.makeDir(getFileSaveRoot() + "logs/");
        return new File(root + "trafficLog.txt");
    }
    
    /** 获取应用trafficLogBack文件*/
    public static File getAppTrafficBackLog()
    {
        String root = FileUtil.makeDir(getFileSaveRoot() + "logs/");
        return new File(root + "trafficLog-"+System.currentTimeMillis() +".txt");
    }
    	
	/** 获取临时图片文件*/
	public static File getTemImgFile()
	{
	    return getAppImg(".__"+ System.currentTimeMillis() +"_.tmp");
	}
	
	/** 获取导出图片的文件全路径*/
	public static File getExportImgFile() {
	    String root = FileUtil.makeDir(Environment.getExternalStorageDirectory().getAbsolutePath() + ExportSaveRoot);
        return new File(root + "2bulu-"+ DateUtil.getTimeFile(System.currentTimeMillis()) + ".jpg");
	}
	
	/** 获取更新APK的文件目录*/
	public static File getUpdatePath(String name) {
	    String root = FileUtil.makeDir(getFileSaveRoot() + UpdateFileDir);
        return new File(root + name);
	}
	
	/** 获取httpCache文件目录*/
    public static File getHttpCachePath() {
        String root = FileUtil.makeDir(getFileSaveRoot() + "cache/");
        return new File(root);
    }
	
	/** 判断是否是 3G 或者 wifi 网络 */
    public static boolean isWifi3GNetwork(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);  
        if(connectivityManager == null) return false;
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        TelephonyManager tele = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if (activeNetInfo != null)
        { 
            if(activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) 
            {  
                return true;  
            }else
            if(activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE)
            {
                return isConnectionFast(activeNetInfo.getType(), tele.getNetworkType());
            }else 
            {
                return false;
            }
        }else
        {
            return false;
        }
    }
    
    /** 是否是快连接网络*/
    public static boolean isConnectionFast(int type, int subType)
    {
        if (type == ConnectivityManager.TYPE_WIFI)
        {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE)
        {
            switch (subType)
            {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return false; // ~ 14-64 kbps
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return true;  // ~ 400-1000 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return true;  // ~ 600-1400 kbps
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return false; // ~ 100 kbps
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                 return true; // ~ 2-14 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPA:
                 return true; // ~ 700-1700 kbps
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                 return true; // ~ 1-23 Mbps
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return true;  // ~ 400-7000 kbps
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return false; // ~25 kbps
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return false;
            default:
                return false;
            }
        } else
        {
            return false;
        }
    }

    public static int getVerCode(Context context) {
        int verCode = -1;
        try {
            verCode = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return verCode;
    }
    
    public static String getVerCodeName(Context context) {
        String verCode = "";
        try {
            verCode = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return verCode;
    }
    
    public static String getAppPackageName(Context context) {
        String pName = "";
        try {
            pName = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).packageName;
        } catch (NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return pName;
    }
    
    /** 获取相册图片*/
    public static void takeImg(Activity context, int reqestCode)
    {
        Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        innerIntent.setType("image/*");
        Intent wrapperIntent = Intent.createChooser(innerIntent, null);
        SystemService.startTopLevle(context);
        try {
            context.startActivityForResult(wrapperIntent, reqestCode);
        }catch(ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    /** 获取视频文件*/
    public static void takeVedio(Activity context, int reqestCode)
    {
        Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        innerIntent.setType("video/*");
        Intent wrapperIntent = Intent.createChooser(innerIntent, null);
        SystemService.startTopLevle(context);       
        try {
            context.startActivityForResult(wrapperIntent, reqestCode);
        }catch(ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    /** 获取相册裁剪图片 */
    public static File takeImgCrop(Activity context, int reqestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
        File tempFile = AppHelper.getTemImgFile();
        intent.putExtra("output", Uri.fromFile(tempFile));
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);// 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", CROP_IMG_SIZE);// 输出图片大小
        intent.putExtra("outputY", CROP_IMG_SIZE);
        SystemService.startTopLevle(context);
        try {
            context.startActivityForResult(intent, reqestCode);
        }catch(ActivityNotFoundException e) {
            e.printStackTrace();
        }
        return tempFile;
    }
    
    /** 获取拍照裁剪图片*/
    public static File captureImgCrop(Activity context, int reqestCode){
    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    	File tempFile = AppHelper.getTemImgFile();
        intent.putExtra("output", Uri.fromFile(tempFile));
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);// 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", CROP_IMG_SIZE);// 输出图片大小
        intent.putExtra("outputY", CROP_IMG_SIZE);
        SystemService.startTopLevle(context);
        try {
            context.startActivityForResult(intent, reqestCode);
        }catch(ActivityNotFoundException e) {
            e.printStackTrace();
        }
        return tempFile;
    }
    
    /** 获取拍照图片*/
    public static File captureImg(Activity context, int reqestCode)
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imgFile = AppHelper.getTemImgFile();
        Uri outputFileUri = Uri.fromFile(imgFile);
        intent.putExtra(MediaStore.Images.Media.ORIENTATION,0);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        SystemService.startTopLevle(context);       
        try {
            context.startActivityForResult(intent, reqestCode);
        }catch(ActivityNotFoundException e) {
            e.printStackTrace();
        }
        return imgFile;
    }
    
    /** 获取拍摄视频*/
    public static void captureVideo(Activity context, int reqestCode){
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 20*1024*1024);
        try {
            context.startActivityForResult(intent, reqestCode);
        }catch(ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    /** 获取拍照图片返回*/
    public static File captureImgOnActivityResult(Activity context,int resultCode, Intent data, File tmpFile){   	 
    	 File okImgFile = null;
    	 SystemService.stopTopLevle(context);
    	 if (Activity.RESULT_OK == resultCode) {
    		 String filePath = null;
        	 Object object = null;
	    	 if(data!=null){
	             //HTC
	             if (data.getData() != null) {
	                     //根据返回的URI获取对应的SQLite信息
	                     Cursor cursor = context.getContentResolver().query(data.getData(), null,null, null, null);
	                     if (cursor != null && cursor.moveToFirst()) {
	                    	 // 获取绝对路径
	                         filePath = cursor.getString(cursor.getColumnIndex("_data"));
	                     }
	                     if(cursor != null) cursor.close();
	             }else{
	            	 //三星  小米(小米手机不会自动存储DCIM...  这点让哥又爱又恨...)
	                 object = (data.getExtras() == null ? null : data.getExtras().get("data"));
	             }
	    	 }
	    	 //直接强转报错  这个主要是为了去高宽比例	    	 
	    	 Bitmap bitmap = object==null?null:(Bitmap)object;
	    	 filePath = !TextUtils.isEmpty(filePath) ? filePath : (tmpFile != null ? tmpFile.getAbsolutePath() : null);
	    	 //检测是否成功
	    	 //存在文件
	    	 if(filePath != null){
		    	 File imgFile = new File(filePath);
		    	 if(imgFile != null && imgFile.exists())
		    	 {
		    		 okImgFile = imgFile;
		    	 }
	    	 }
	    	 //存在对象
	    	 if(okImgFile == null){
	    		 if(bitmap != null){
	    			 File saveFile = tmpFile != null ? tmpFile : AppHelper.getTemImgFile();
	    			 if(ImageUtil.saveImag(saveFile.getAbsolutePath(), bitmap, CompressFormat.PNG)){
	    				 okImgFile = saveFile;
	    			 }
	    		 }
	    	 }
    	 }
    	 return okImgFile; 
    }
    
    public static String getIMEI(Context context)
    {
    	TelephonyManager telephonyManager=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if(telephonyManager == null) return System.currentTimeMillis() + "";
        
    	String imei =telephonyManager.getDeviceId();
        if(TextUtils.isEmpty(imei))
        {
            imei = telephonyManager.getSubscriberId();
            if(TextUtils.isEmpty(imei))
            {
                imei = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
            }
        }
        // 都没有取到
        if(TextUtils.isEmpty(imei))
        {
            imei = System.currentTimeMillis() + "";
        }
        return imei;
    }
    
    public static String getIMSI(Context context) {
        TelephonyManager telephonyManager=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if(telephonyManager == null) return System.currentTimeMillis() + "";
        String imsi = telephonyManager.getSubscriberId();
        if(TextUtils.isEmpty(imsi)) {
            imsi = System.currentTimeMillis() + "";
        }
        return imsi;
    }
    
    public static boolean isGpsEnable(Context context)
    {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);  
        if(lm == null) return false;
        boolean GPS_status = false;
        try {
            GPS_status = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception e) {
            e.printStackTrace();
        }
        return GPS_status;
    }
    
    /**
     * 检测网络是否可用
     * 
     * @return
     */
    public static boolean isNetAvaliable()
    {
        final ConnectivityManager connMgr = (ConnectivityManager) MyApplication
                .getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connMgr == null) return false;
        final android.net.NetworkInfo wifi = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if ( (wifi == null || !wifi.isAvailable()) && ( mobile == null || !mobile.isAvailable()))
        {
            return false;
        }
        return true;
    }
    
    /** 获取联网IP地址*/
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "218.17.161.71";
    }
    
    /** * 根据手机的分辨率从dp 的单位 转成为px(像素) */ 
    public static int dip2px(Context context, float dpValue) { 
         final float scale = context.getResources().getDisplayMetrics().density; 
         return (int) (dpValue * scale + 0.5f); 
    } 

    /** * 根据手机的分辨率从px(像素) 的单位 转成为dp */ 
    public static int px2dip(Context context, float pxValue) { 
         final float scale = context.getResources().getDisplayMetrics().density; 
         return (int) (pxValue / scale + 0.5f); 
    } 
    
    /** 指定的软件是否可用
     * @return ==0,符合最小版本，软件存在; <0,软件不存在; >0,软件存在，但版本不符合最小版本
     * */
    public static int appAvaliable(Context context, String pkg, int minVersion) {
        try {
            PackageInfo app = context.getPackageManager().getPackageInfo(pkg, 0);
            if(app.versionCode >= minVersion) {
                return 0;
            }else {
                return 1;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    /** 打开视频文件*/
    public static void viewVedio(Context context , String file) {
        if(file == null) return;
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(file));
        intent.setDataAndType(uri, "video/*");
        SystemService.startTopLevle(context);
        try {
            context.startActivity(intent);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /** 通过蓝牙传输文件*/
    public static boolean sendFileByBt(Context context, File file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        
        List<ResolveInfo> mApps = context.getPackageManager().queryIntentActivities(intent,PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        ResolveInfo blueApp = null;
        for(ResolveInfo one : mApps) {
            if(one.activityInfo.packageName.contains("bluetooth") || one.activityInfo.name.toLowerCase().contains("bluetooth")) {
                blueApp = one;
                break;
            }
        }
        if(blueApp != null) {
            intent.setClassName(blueApp.activityInfo.packageName, blueApp.activityInfo.name);
            context.startActivity(intent);
            return true;
        }else {
            return false;
        }
    }
    
    /** 发送本软件通过蓝牙*/
    public static void sendAppByBt(Context context) {
        try {
            PackageInfo appInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            File appFile = new File(appInfo.applicationInfo.sourceDir);
            if( !AppHelper.sendFileByBt(context, appFile)) {
                Toast.makeText(context, "抱歉，没有发现蓝牙应用，无法发送!", Toast.LENGTH_LONG).show();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}