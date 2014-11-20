/*******************************************************
 * @作者: zhaohua
 * @日期: 2012-6-4
 * @描述: 系统Action帮助类
 * @声明: copyrights reserved by Petfone 2007-2011
*******************************************************/
package com.android.util.system;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.android.util.file.FileUtil;

public class ActionHelper
{
    private static final String TAG = "ActionHelper";
    /** 获取图片请求码*/
    public static final int REQUEST_CODE_TAKE_PICTURE = 0x1001;
    /** 拍照图片请求码*/
    public static final int REQUEST_CODE_CAPTURE_PICTURE = 0x1002;    
    /** 获取音频请求码*/
    public static final int REQUEST_CODE_TAKE_AUDIO = 0x1003;
    /** 音频录制请求码*/
    public static final int REQUEST_CODE_RECORD_AUDIO= 0x1004;    
    /** 获取视频请求码*/
    public static final int REQUEST_CODE_TAKE_VEDIO = 0x1005;
    /** 视频拍摄请求码*/
    public static final int REQUEST_CODE_CAPTURE_VEDIO = 0x1006;    
    /** 获取文件请求码*/
    public static final int REQUEST_CODE_TAKE_FILE = 0x1007;
    /** 存放文件的目录。可变动*/
    public static String DIR_FILE = "/file/";
    public static final String TMP_SUFIX = ".tmp";
    /** 音频文件的通用后缀*/
    public static final String VOICE_SUFIX = ".amr";
    /** 音频文件的通用后缀*/
    public static final String IMAGE_SUFIX = ".jpg";
    /** 视频文件的通用后缀*/
    public static final String VEDIO_SUFIX = ".3gp";
    
    /** 获取文件返回处理业务
     *  注意: 拍照获取的时候，data是为 null的。
     * */
    public static Uri onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(TAG,"onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode + ", data=" + data);
        
        // If there's no data (because the user didn't select a picture and
        // just hit BACK, for example), there's nothing to do.
        if (requestCode != REQUEST_CODE_CAPTURE_PICTURE)
        {
            if (data == null)
            {
                return null;
            }
        } else if (resultCode != Activity.RESULT_OK)
        {
            Log.d(TAG,"onActivityResult: bail due to resultCode=" + resultCode);
            return null;
        }
        
        switch(requestCode) 
        {
            // 拍照获取图片
            case ActionHelper.REQUEST_CODE_CAPTURE_PICTURE:
            {
                // create a file based uri and pass to addImage(). We want to read the JPEG
                // data directly from file (using UriImage) instead of decoding it into a Bitmap,
                // which takes up too much memory and could easily lead to OOM.
                File imgFile = getSpecFile(TMP_SUFIX + IMAGE_SUFIX);
                File destFile = getSpecFile(System.currentTimeMillis() + IMAGE_SUFIX);
                // 复制 copy 一份即可,即命名
                if(!imgFile.renameTo(destFile))
                {
                    Log.d(TAG, "---->activity result capture picture fail");
                    return null;
                }                
                Uri uri = Uri.fromFile(destFile);               
                return uri;
            }
            // 获取图片
            case ActionHelper.REQUEST_CODE_TAKE_PICTURE:
            // 获取录音文件
            case ActionHelper.REQUEST_CODE_TAKE_AUDIO:
            // 设备录音获取文件
            case ActionHelper.REQUEST_CODE_RECORD_AUDIO:
            // 获取视频文件
            case ActionHelper.REQUEST_CODE_TAKE_VEDIO:
            // 视频拍摄获取文件
            case ActionHelper.REQUEST_CODE_CAPTURE_VEDIO:
            // 获取文件
            case ActionHelper.REQUEST_CODE_TAKE_FILE:
            {
                Uri file = data.getData();
                return file;
            }
            default:
            {
                Log.d(TAG, "---->activity result unvaild request code !");
                return null;
            }
        }
    }
    /** 启动本地获取图片 */
    public static void startAcquireImage(Activity activity)
    {
        Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        innerIntent.setType("image/*");
        Intent wrapperIntent = Intent.createChooser(innerIntent, null);
        activity.startActivityForResult(wrapperIntent, REQUEST_CODE_TAKE_PICTURE);
    }
    /** 启动本地获取音频 */
    public static void startAcquireAudio(Activity activity)
    {
        Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        innerIntent.setType("audio/*");
        Intent wrapperIntent = Intent.createChooser(innerIntent, null);
        activity.startActivityForResult(wrapperIntent, REQUEST_CODE_TAKE_AUDIO);
    }
    /** 启动本地获取视频 */
    public static void startAcquireVedio(Activity activity)
    {
        Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        innerIntent.setType("video/*");
        Intent wrapperIntent = Intent.createChooser(innerIntent, null);
        activity.startActivityForResult(wrapperIntent, REQUEST_CODE_TAKE_VEDIO);
    }
    /** 启动本地获取文件 */
    public static void startAcquireFile(Activity activity)
    {
        Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        innerIntent.setType("*/*");
        Intent wrapperIntent = Intent.createChooser(innerIntent, null);
        activity.startActivityForResult(wrapperIntent, REQUEST_CODE_TAKE_FILE);
    }
    /** 启动拍照获取图片 */
    public static void startCaptureImage(Activity activity)
        throws Exception
    {
        if(!sdcardExist())
        {
            throw new Exception("no sdcard not capture!");
        }
        File imgFile = getSpecFile(TMP_SUFIX + IMAGE_SUFIX);
        if(imgFile == null)
        {
            throw new Exception("create file dir fail!");
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);              
        Uri outputFileUri = Uri.fromFile(imgFile);     
        intent.putExtra(MediaStore.Images.Media.ORIENTATION,0);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        
        activity.startActivityForResult(intent, REQUEST_CODE_CAPTURE_PICTURE);
    }
    /** 启动录音获取录音 */
    public static void startRecord(Activity activity)
    {
        Intent intent = new Intent(Media.RECORD_SOUND_ACTION);              
        activity.startActivityForResult(intent, REQUEST_CODE_RECORD_AUDIO);
    }
    /** 启动视频获取视频 */
    public static void startCaptureVedio(Activity activity)
    {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE); 
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); 
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
        
        activity.startActivityForResult(intent, REQUEST_CODE_CAPTURE_VEDIO);
    }   
    /** 调用相应的程序展示文件*/
    public static void viewFile(Context context, File f, String dateType)
    {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);

        String type = getMIMEType(f.getAbsolutePath());
        if (type == null)
        {
            // 找不到常规的类型
            if (dateType != null)
            {
                // 使用用户自定义的类型
                type = dateType;
            }
            else
            {
                // 使用泛型
                type = "*/*";
            }
        }
        intent.setDataAndType(Uri.fromFile(f), type);
        try
        {
            context.startActivity(intent);
        }catch(Exception e)
        {
            Log.e(TAG, "--> not found activity to view file, apply to */* to view!");
            
            intent.setDataAndType(Uri.fromFile(f), "*/*");
            context.startActivity(intent);
        }
    }
    /** SDCard是否存在 */
    public static boolean sdcardExist()
    {
        return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }   
    /** 获取文件的MIME类型 */
    public static String getMIMEType(String file)
    {
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String extension = MimeTypeMap.getFileExtensionFromUrl(file);
        if (TextUtils.isEmpty(extension))
        {
            // getMimeTypeFromExtension() doesn't handle spaces in filenames nor
            // can it handle
            // urlEncoded strings. Let's try one last time at finding the
            // extension.
            int dotPos = file.lastIndexOf('.');
            if (0 <= dotPos)
            {
                extension = file.substring(dotPos + 1);
            }
        }
        return mimeTypeMap.getMimeTypeFromExtension(extension);
    }
    /**返回到桌面*/
    public static void backHome(Context context)
    {
        Intent homeIntent = new Intent();    
        homeIntent.setAction(Intent.ACTION_MAIN);  
        homeIntent.addCategory(Intent.CATEGORY_HOME);     
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
        context.startActivity(homeIntent);
    }
    /** 调用APK安装器*/
    public static void callInstallApk(File apkFile, Context context)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkFile),"application/vnd.android.package-archive");

        context.startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
    
    /** 获取指定的文件 */
    private static File getSpecFile(String surfix)
    {
        return new File(getFileDir() + surfix);
    }
    /** 获取文件目录 */
    private static String getFileDir()
    {
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + DIR_FILE;
        if(FileUtil.newDir(dir) != null)
        {
            return dir;
        }else
        {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
    }
}
