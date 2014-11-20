/*******************************************************
 * @作者: zhaohua
 * @日期: 2012-6-27
 * @描述: 文件缓存，通过文件全路径String寻找File，适合于HTTP图片与文件缓存
 * @声明: copyrights reserved by Petfone 2007-2011
 *******************************************************/
package com.android.util.cache;

import java.io.File;

import android.content.Context;
import android.webkit.URLUtil;

public class FileCache
{
    private File cacheDir;

    public FileCache(Context context, String imgDir)
    {
        // Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED))
        {
            cacheDir = new File(imgDir);
        } else cacheDir = context.getCacheDir();
        if (!cacheDir.exists()) cacheDir.mkdirs();
    }

    /** 通过uil获取file*/
    public File getFile(String url)
    {
        boolean http = URLUtil.isHttpUrl(url);
        File f = null;
        // web image
        if (http)
        {
            // I identify images by hashcode. Not a perfect solution, good for
            // the demo.
            String filename = String.valueOf(url.hashCode()) + ".jpg";
            // Another possible solution (thanks to grantland)
            // String filename = URLEncoder.encode(url);
            f = new File(cacheDir, filename);
        } else
        // local image
        {
            f = new File(url);
        }
        return f;
    }

    /** 清除文件*/
    public void clear()
    {
        File[] files = cacheDir.listFiles();
        if (files == null) return;
        for (File f : files)
            f.delete();
    }
}
