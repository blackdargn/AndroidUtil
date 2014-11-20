package com.android.util.image;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

public class BitmapCache extends LruCache<Long, Bitmap>
{
	private static final String TAG = "BitmapCache";
	public BitmapCache(Context context)
	{
		this(1024*1024*((ActivityManager) context.getSystemService(
	            Context.ACTIVITY_SERVICE)).getMemoryClass()/8);
	}
	
	public BitmapCache(int maxSize)
	{
		super(maxSize);
		
	}
	
	@Override
	protected void entryRemoved(boolean evicted, Long key, Bitmap oldValue,
			Bitmap newValue)
	{
		super.entryRemoved(evicted, key, oldValue, newValue);
		
		if(oldValue != null && !oldValue.isRecycled())
		{
			Log.d(TAG, "A bitmap is recycled");
			oldValue.recycle();
			oldValue = null;
		}
	}
	
	@Override
	protected int sizeOf(Long key, Bitmap value)
	{
		if(value!=null)
		{
			return value.getRowBytes();
		}
		
		return 0;
	}
	
	public Bitmap getB(Long key) {
	    Bitmap bmp = get(key);
	    if(bmp != null) {
	        if(bmp.isRecycled()) {
	            remove(key);
	            bmp = null;
	        }
	    }
	    return bmp;
	}
}