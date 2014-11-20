/*******************************************************
 * @作者: zhaohua
 * @日期: 2012-6-27
 * @描述: 内存缓存，通过文件全路径String寻找图片Bitmap，适合于图片缓存加载。
 * @声明: copyrights reserved by Petfone 2007-2011
 *******************************************************/
package com.android.util.cache;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.graphics.Bitmap;

import com.android.util.system.Log;

public class MemoryCache
{

    private static final String TAG = "MemoryCache";

    // Last argument true for LRU ordering
    private Map<String, Bitmap> cache = Collections
            .synchronizedMap(new LinkedHashMap<String, Bitmap>(10, 1.5f, true));

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
        Log.i(TAG, "MemoryCache will use up to " + limit / 1024. / 1024. + "MB");
    }

    public Bitmap get(String id)
    {
        try
        {
            if (!cache.containsKey(id)) return null;
            // NullPointerException sometimes happen here
            // http://code.google.com/p/osmdroid/issues/detail?id=78
            return cache.get(id);
        } catch (NullPointerException ex)
        {
            return null;
        }
    }

    public void put(String id, Bitmap bitmap)
    {
        if (bitmap == null)
        {
            return;
        }
        try
        {
            if (cache.containsKey(id)) size -= getSizeInBytes(cache.get(id));
            cache.put(id, bitmap);
            size += getSizeInBytes(bitmap);
            checkSize();
        } catch (Throwable th)
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
            cache.remove(id);
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
                size -= getSizeInBytes(entry.getValue());
                if(!entry.getValue().isRecycled())
                {
                    entry.getValue().recycle();
                }
                iter.remove();
                if (size <= limit) break;
            }
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
