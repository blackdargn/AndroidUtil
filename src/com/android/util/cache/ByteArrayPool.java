/*******************************************************
 * @作者: zhaohua
 * @日期: 2012-6-27
 * @描述: 字节数组池，用于解决文件传输数据块的内存的优化问题。
 * @声明: copyrights reserved by Petfone 2007-2011
*******************************************************/
package com.android.util.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.android.util.system.Log;

/**
 * @author zhaohua
 *
 */
public class ByteArrayPool
{
	private static final String TAG = "ByteArrayPool";
	/** 数组池*/
	private ConcurrentHashMap<Integer, MPool> arrayPool;
	
	/**实现单例*/
	private static class SingletonContainer
	{
		private static ByteArrayPool instance = new ByteArrayPool();
	}

	private ByteArrayPool()
	{
		arrayPool = new ConcurrentHashMap<Integer, MPool>();
	}

	/**获取单例*/
	public static ByteArrayPool getInstance()
	{
		return SingletonContainer.instance;
	}
	
	/**设置最大的池数,超过则释放一半的空间*/
	public void setMaxNum(int size, int max)
	{
		if(arrayPool.containsKey(size))
		{
			arrayPool.get(size).setMaxNum(max);
		}
	}
	
	public MByteArray newByteArray(int size)
	{
		if(!arrayPool.containsKey(size))
		{
			MPool one = new MPool(size, this);
			arrayPool.put(size, one);
			return one.get();
		}else
		{
			return arrayPool.get(size).get();
		}
	}
	
	public void destory(int size)
	{
		if(arrayPool.containsKey(size))
		{
			arrayPool.get(size).dataPool.clear();
			arrayPool.remove(size);
			System.gc();
		}
	}
	
	public void destory()
	{
		arrayPool.clear();
		System.gc();
	}
	
	private class MPool
	{
		/** 池的长度*/
		private int size;
		private ByteArrayPool pool;
		/** 池的队列*/
		private ConcurrentLinkedQueue<MByteArray> dataPool;
		/** 对象的存在最大数*/
		private int maxNum = 20;
		/** 对象的当前数*/
		private AtomicInteger curNum = new AtomicInteger(0);
		
		public MPool(int len,ByteArrayPool pool)
		{			
			this.size = len;
			this.pool = pool;
			this.dataPool = new ConcurrentLinkedQueue<MByteArray>();
		}
		
		/** 设置对象的存在最大数，超过则释放一半*/
		public void setMaxNum(int max)
		{
			maxNum = max;
		}
		
		public MByteArray get()
		{
			if(dataPool.isEmpty())
			{
				// 当池为空时，而其到达最大数目
				if(maxNum > 1 && curNum.get() > maxNum)
				{
					Log.d(TAG, "--> reach maxnum return null");
					return null;
				}else
				{
					Log.d(TAG, "--> new one");
					curNum.incrementAndGet();
					return new MByteArray(size, this);
				}
			}
			Log.d(TAG, "--> get one");
			return dataPool.poll();
		}
		
		public void collect(MByteArray p)
		{
			if(maxNum <= 0)
			{
				// 不用缓存,清理所有
				pool.destory(size);
				Log.d(TAG, "--> release when temp!");
			}else
			if(dataPool.size() >= maxNum)
			{
				// 需要清理一半的空间
				for(int i = 0; i < (maxNum/2); i++)
				{
					dataPool.poll();
					curNum.decrementAndGet();
				}
				Log.d(TAG, "--> release when reach max!");
				System.gc();
				System.gc();
			}else
			{
				// 需要归还
				Log.d(TAG, "--> back one!");
				dataPool.offer(p);
			}
		}
	}
	
	public static class MByteArray
	{
		private byte[] data;
		private MPool  owner;
		
		public MByteArray(int size, MPool pool)
		{
			data = new byte[size];
			owner = pool;
		}
		
		/** 获取空间*/
		public byte[] getArray()
		{
			return data;
		}
		
		/** 释放空间,必须确保改数据已读写完成*/
		public void release()
		{
			owner.collect(this);
		}
	}
}
