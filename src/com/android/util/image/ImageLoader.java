/*******************************************************
 * @作者: zhaodh
 * @日期: 2012-12-6
 * @描述: 图片加载器
 * @声明: copyrights reserved by personal 2007-2011
 *******************************************************/
package com.android.util.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.util.system.Log;

public class ImageLoader
{
    private static final String TAG = "ImageLoader";
    private int stub_id = 0;
    private MemoryCache memoryCache = new MemoryCache();
    private FileCache fileCache;
    private ConcurrentHashMap<String, List<ImageView>> viewHash = new ConcurrentHashMap<String, List<ImageView>>();
    
    private static ImageLoader instance;
    /**
     * 获取单例
     * @return
     */
    public static ImageLoader getInstance()
    {       
        return instance;
    }
    // 初始化，使用前必须先初始化
    public static void init(Context context,int studRId, String imgDir)
    {
        if(instance == null)
        {
            instance = new ImageLoader(context, studRId, imgDir);
        }
    }
    
    private ImageLoader(Context context,int studRId, String imgDir)
    {
        stub_id = studRId;
        fileCache = new FileCache(context,imgDir);
    }
    
    public void DisplayImage(Imager imager)
    {
        ImageView imageView = imager.imageView;
        
        if(!imager.isVisible())
        {
            // 图片不可见，可先不加载
            Log.d(TAG, "the image is invisible, not load it");
            return;
        }
        if(imager.getFileUrl() == null)
        {
            // 非法的
            imageView.setImageResource(imager.stubId > 0 ? imager.stubId : stub_id);
            return;
        }   
        int statu = imageLoding(imager.getMemUrl());
        if(statu > 0)
        {
            // 已载入完成
            Bitmap bitmap = memoryCache.get(imager.getMemUrl());
            imageView.setImageBitmap(bitmap);
            return;
        }else
        if(statu == 0)
        {
            // 正在载入
            if(viewHash.containsKey(imager.getMemUrl()))
            {
                List<ImageView> list = viewHash.get(imager.getMemUrl());
                if(!list.contains(imageView))
                {
                    list.add(imageView);
                }
            }
            imageView.setImageResource(imager.stubId > 0 ? imager.stubId : stub_id);
            return;
        }else
        {   
            // 还未载入
            if(!viewHash.containsKey(imager.getMemUrl()))
            {
                viewHash.put(imager.getMemUrl(), new ArrayList<ImageView>());
            }
            memoryCache.prePut(imager.getMemUrl(), null);
            viewHash.get(imager.getMemUrl()).add(imageView);
            queuePhoto(imager);
            imageView.setImageResource(imager.stubId > 0 ? imager.stubId : stub_id);
        }
    }
    
    /** 清理指定的缓存 */
    public void clearBmp(Imager imager)
    {
        memoryCache.remove(imager.getMemUrl());
    }
	
	/** 清理指定的缓存 */
    public void clearCache(String url)
    {
        viewHash.remove(url);
        memoryCache.remove(url);
    }

    /** 清理缓存 */
    public void clearCache()
    {
        viewHash.clear();
        memoryCache.clear();
    }
    
    /** 清理文件*/
    public void clearFile(int day)
    {
        fileCache.clear(day);
    }
    
    /** 获取本地的图片的路径，从url*/
    public File getImgPath(String url)
    {
        return fileCache.getFile(url);
    }
    
    private void queuePhoto(final Imager imager)
    {
        new AsyncTask<Void, Void, Bitmap>()
        {
            @Override
            protected Bitmap doInBackground(Void... params)
            {
                Log.d(TAG, "loading image = " + imager.getFileUrl());
                Bitmap bmp = getBitmap(imager);
                if(bmp != null)
                {
                    Log.d(TAG, "loading OK");
                    memoryCache.put(imager.getMemUrl(), bmp);
                }else
                {
                    Log.d(TAG, "loading FAIL");
                    memoryCache.remove(imager.getMemUrl());
                }
                return bmp;
            }
            
            protected void onPostExecute(Bitmap bitmap)
            {
                List<ImageView> list = viewHash.get(imager.getMemUrl());
                if(list != null)
                {
                    for(ImageView imageView : list)
                    {
                        if (bitmap != null)
                        {
                            imageView.setImageBitmap(bitmap);
                        }
                        else
                        {
                            imageView.setImageResource(imager.stubId);
                        }
                    }
                    viewHash.remove(imager.getMemUrl());
                }
            };
        }.execute();
    }

    public Bitmap getBitmap(Imager imager)
    {
        File f = fileCache.getFile(imager.getFileUrl());

        // from SD cache
        if(f.exists() && f.length() == 0)
        {
            // delete invalid image
            f.delete();
        }
        Bitmap b = decodeFile(f,imager.isInSample, imager.requiredSize);
        if (b != null) return b;

        // from web
        try
        {
            Bitmap bitmap = null;
            URL imageUrl = new URL(imager.getFileUrl());
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(10000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f, imager.isInSample, imager.requiredSize);
            return bitmap;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    // decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f, boolean isInSample, int requiredSize)
    {
        if(!f.exists() || f.length() == 0) return null;
        try
        {
            int scale = 1;
            if(isInSample)
            {
                // decode image size
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(new FileInputStream(f), null, o);
    
                // Find the correct scale value. It should be the power of 2.
                int REQUIRED_SIZE = requiredSize;
                int width_tmp = o.outWidth, height_tmp = o.outHeight;               
                while (true)
                {
                    if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) break;
                    width_tmp /= 2;
                    height_tmp /= 2;
                    scale *= 2;
                }
            }
            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        }
        catch (FileNotFoundException e)
        {
        }
        return null;
    }
    
    int imageLoding(String url)
    {
        boolean in = memoryCache.isExist(url);
        if(in)
        {
            if(memoryCache.get(url) != null)
            {
                // 已载入完成
                return 1;
            }else
            {
                // 正在载入
                return 0;
            }
        }else
        {
            // 还未载入
            return -1;
        }
    }
    
    private static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size = 1024;
        try
        {
            byte[] bytes = new byte[buffer_size];
            for (;;)
            {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1) break;
                os.write(bytes, 0, count);
            }
        }
        catch (Exception ex)
        {
        }
    }
    
    public static class Imager
    {
        private String url;
        private String memUrl;
        public ImageView imageView;
        // 局部默认图片
        public int stubId;
        public boolean isInSample = true;
        public int requiredSize = 70;
        // 所在listView的位置,用于不可见时不加载，可见时加载
        int position;
        ListView listView;
        
        /** 默认压缩的图*/
        public Imager(String url,ImageView imageView)
        {
            init(url,imageView,requiredSize,stubId);
        }
        
        /** 指定压缩的图，当requiredSize无效时，不压缩*/
        public Imager(String url,ImageView imageView, int requiredSize, int stubId)
        {
            init(url, imageView, requiredSize, stubId);
        }
        
        /** 初始化*/
        private void init(String url,ImageView imageView, int requiredSize, int stubId)
        {
            this.url = url;
            this.imageView = imageView;
            this.requiredSize = requiredSize;
            this.stubId = stubId;
            this.isInSample = requiredSize <=0 ? false : true;          
            this.memUrl = isInSample ? ("["+ requiredSize +"]" + url) : url;
        }
        
        /** 获取原始文件的URL*/
        public String getFileUrl()
        {
            return url;
        }
        
        /** 获取内存缓存文件的URL*/
        public String getMemUrl()
        {
            return memUrl;
        }
        
        /** 设置ListView参数*/
        public void setListView(ListView listView, int postion)
        {
            this.listView = listView;
            this.position = postion;
        }
        
        /** 改图片是否可见*/
        boolean isVisible()
        {
            if(listView != null)
            {
                if (position < listView.getFirstVisiblePosition()-1
                    || position > listView.getLastVisiblePosition()+1)
                {
                    return false;
                }
            }
            return true;
        }
    }
    
    private class FileCache
    {
        private File cacheDir;

        public FileCache(Context context, String imgDir)
        {
            // Find the dir to save cached images
            if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            { 
                cacheDir = new File(imgDir);
            }
            else cacheDir = context.getCacheDir();
            if (!cacheDir.exists()) cacheDir.mkdirs();
        }

        public File getFile(String url)
        {
            boolean http = URLUtil.isHttpUrl(url);
            File f = null;
            // web image
            if(http)
            {
                // I identify images by hashcode. Not a perfect solution, good for the demo.
                String filename = String.valueOf(url.hashCode()) + ".jpg";
                // Another possible solution (thanks to grantland)
                // String filename = URLEncoder.encode(url);
                f = new File(cacheDir, filename);
            }else
            // local image
            {
                f = new File(url);
            }
            return f;
        }

        public void clear()
        {
            File[] files = cacheDir.listFiles();
            if (files == null) return;
            for (File f : files)
                f.delete();
        }
        
        public void clear(int day)
        {
            if(day < 0)
            {
                clear();
            }else
            {
                long curTime = System.currentTimeMillis();
                long oneTime = 24 * 60 * 60 * 1000L;
                File[] files = cacheDir.listFiles();
                if (files == null) return;
                for (File f : files)
                {
                    if( (curTime - f.lastModified()) > (day * oneTime))
                    {
                        f.delete();
                    }
                }
            }
        }
    }

    private class MemoryCache
    {

        private static final String TAG = "MemoryCache";
        // Last argument true for LRU ordering
        private Map<String, Bitmap> cache = Collections
                .synchronizedMap(new LinkedHashMap<String, Bitmap>(10, 1.5f,true));
        // current allocated size
        private long size = 0;
        // max memory in bytes
        private long limit = 1000000;

        public MemoryCache()
        {
            // use 25% of available heap size
            setLimit(Runtime.getRuntime().maxMemory() / 4);
        }

        public void setLimit(long new_limit)
        {
            limit = new_limit;
            Log.i(TAG, "MemoryCache will use up to " + limit / 1024. / 1024.+ "MB");
        }

        public Bitmap get(String id)
        {
            try
            {
                if (!cache.containsKey(id)) return null;
                // NullPointerException sometimes happen here
                // http://code.google.com/p/osmdroid/issues/detail?id=78
                return cache.get(id);
            }
            catch (NullPointerException ex)
            {
                return null;
            }
        }
        
        public void put(String id, Bitmap bitmap)
        {
            if(bitmap == null)
            {
                return;
            }
            try
            {
                if (cache.containsKey(id))
                    size -= getSizeInBytes(cache.get(id));
                cache.put(id, bitmap);
                size += getSizeInBytes(bitmap);
                checkSize();
            }
            catch (Throwable th)
            {
                th.printStackTrace();
            }
        }
        
        public void prePut(String id, Bitmap bitmap)
        {
            if (!cache.containsKey(id))
            {
                cache.put(id, bitmap);
            }
        }
        
        public void remove(String id)
        {
            if (cache.containsKey(id))
            {
                Bitmap bmp = cache.remove(id);
                if(bmp != null && !bmp.isRecycled())
                {
                    size -= getSizeInBytes(bmp);
                    checkSize();
                    bmp.recycle();
                    bmp = null;
                    System.gc();
                    System.gc();
                }
            }
        }
        
        private void checkSize()
        {
            Log.i(TAG, "cache size=" + size + " length=" + cache.size());
            if (size > limit)
            {
                // least recently accessed item will be the first one iterated
                Iterator<Entry<String, Bitmap>> iter = cache.entrySet().iterator();
                while (iter.hasNext())
                {
                    Entry<String, Bitmap> entry = iter.next();
                    iter.remove();
                    size -= getSizeInBytes(entry.getValue());
                    if(!entry.getValue().isRecycled())
                    {
                        entry.getValue().recycle();
                    }                   
                    if (size <= limit) break;
                }
                System.gc();
                System.gc();
                Log.i(TAG, "Clean cache. New size " + cache.size());
            }
        }

        public void clear()
        {
            Iterator<Bitmap> itor = cache.values().iterator();

            Bitmap obj = null;
            while (itor.hasNext())
            {
                obj = itor.next();
                itor.remove();
                if (obj != null && !obj.isRecycled())
                {
                    size -= getSizeInBytes(obj);
                    obj.recycle();
                }
                obj = null;
            }
            cache.clear();
            System.gc();
            System.gc();
        }

        long getSizeInBytes(Bitmap bitmap)
        {
            if (bitmap == null) return 0;
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
        
        public boolean isExist(String url)
        {
            return cache.containsKey(url);
        }               
    }
}