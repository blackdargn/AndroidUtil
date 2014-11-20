package com.android.util.update;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;

import com.android.util.update.DownloadDao.DownloadInfo;
/*******************************************************
 * @author: zhaohua
 * @version: 2012-08-21
 * @see: 多线程断点续传文件下载工具
 * @Copyright: copyrights reserved by personal 2007-2012
 *******************************************************/
public class Downloader
{
	private static final String TAG = "Downloader";
	/** 已下載總量*/
	private AtomicInteger done = new AtomicInteger();
	/** 文件总长度*/
	private int fileLen;
	/** 文件全路径*/
	private String fileUri;
	/** 数据库操作*/
	private DownloadDao dao;
	/** 线程暂停 标志*/
	private AtomicBoolean isPause = new AtomicBoolean(false);
	/** 线程终止标志*/
	private AtomicBoolean isStop = new AtomicBoolean(false);
	/** 运行锁*/
	private AtomicBoolean lock = new AtomicBoolean(false);
	
	private Vector<Thread> workThreadList = new Vector<Thread>();	
	private OnDownListener downListener;
	private OnDownOKListener okListener;
	
	public Downloader(OnDownListener listener)
	{
		dao = new DownloadDao();
		downListener = listener;
	}
	
	public void setOnDownListener(OnDownListener listener)
	{
	    downListener = listener;
	}
	
	public void setOnDownOKListener(OnDownOKListener listener)
    {
	    okListener = listener;
    }

	/**
	 * 多线程下载
	 * @param path 下载路径
	 * @param thCount 需要开启多少个线程
	 * @param saveDir 下载文件保存路径
	 * @throws Exception
	 */
	public boolean start(final String path, final int thCount, final String saveDir)
	{				
		if(lock.get())
		{
			// 启动线程正在运行
			return false;
		}
		
		if(isRunning())
		{
			// 子线程正在运行
			return false;
		}
		
		// 开始之前，初始化原状态值
		isPause.set(false);
		isStop.set(false);
		done.set(0);
		workThreadList.clear();
		
		new Thread(new Runnable()
		{		
			@Override
			public void run()
			{
				lock.set(true);
				
				URL url = null;
				try
				{
					url = new URL(path);
				}
				catch (MalformedURLException e)
				{
					doFail();					
					e.printStackTrace();
					
					lock.set(false);
					return;
				}
				HttpURLConnection conn = null;
				try
				{
					conn = (HttpURLConnection) url.openConnection();
					// 设置超时时间
	                conn.setConnectTimeout(5000);
	                conn.connect();
				}catch (IOException e)
				{
					doFail();
					e.printStackTrace();
					
					lock.set(false);
					return;
				}
							
				try
				{
					if (conn.getResponseCode() == 200)
					{
						fileLen = conn.getContentLength();
						conn.disconnect();
						
						String name = path.substring(path.lastIndexOf("/") + 1);
						File dir = new File(saveDir);
						if (!dir.exists())
						{
							dir.mkdirs();
						}
						if (!dir.exists())
						{
							Log.e(TAG, "--------->param saveDir is not valid dirctory! please check!");
							doFail();
							
							lock.set(false);
							return;
						}
						
						File file = new File(saveDir, name);
						RandomAccessFile raf = new RandomAccessFile(file, "rws");
						raf.setLength(fileLen);
						raf.close();

						fileUri = file.getAbsolutePath();
						
						// Handler发送消息，主线程接收消息，获取数据的长度
						doStart();
						
						// 计算每个线程下载的字节数
						int tNum = thCount;
						if(!isCanContinue(url)) {
						    tNum = 1;
						}
						int partLen = (fileLen + tNum - 1) / tNum;
                        for (int i = 0; i < tNum; i++)
                        {
                            Thread thread = new DownloadThread(url, file, partLen, i);
                            workThreadList.add(thread);
                            thread.start();
                        }
					}
					else
					{
						doFail();
					}
				}
				catch (FileNotFoundException e)
				{
					doFail();
					e.printStackTrace();					
				}
				catch (IOException e)
				{
					doFail();
					e.printStackTrace();
				}
				catch (Exception e)
				{
					doFail();
					e.printStackTrace();
				}
				
				lock.set(false);
				return;
			}
		}).start();
		
		return true;
	}
	
	/** 检查是否支持断点续传*/
	private boolean isCanContinue(URL url) {
	    HttpURLConnection conn = null;
	    try {
    	    conn = (HttpURLConnection) url.openConnection();
    	    conn.setConnectTimeout(5000);          
            // 获取指定位置的数据，Range范围如果超出服务器上数据范围, 会以服务器数据末尾为准
            conn.setRequestProperty("Range", "bytes=" + 0 + "-" + 100);
            conn.connect();
            // 如果是206则支持断点续传，否则不支持,从头下载
            return conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL;
	    }catch(Exception e) {
	        e.printStackTrace();
	    }finally {
	        if(conn != null) {
	            conn.disconnect();
	        }
	    }
	    return false;
	}

	/** 下载任务子线程*/
	private final class DownloadThread extends Thread
	{
		/** 下载路径*/
		private URL url;
		/** 保存文件*/
		private File file;
		/** 该段的下载长度*/
		private int partLen;
		/** 下载标志*/
		private int id;
		/** 重试次数， 默认为 3次*/
		private int retry = 0;

		public DownloadThread(URL url, File file, int partLen, int id)
		{
			this.url = url;
			this.file = file;
			this.partLen = partLen;
			this.id = id;
		}

		/**
		 * 写入操作
		 */
		public void run()
		{
			// 判断上次是否有未完成任务
			DownloadInfo info = dao.query(url.toString(), id);
			if (info != null)
			{
				// 如果有, 读取当前线程已下载量
				if(retry == 0)
				{
					// 第一次时，才增加已下载的done
					done.addAndGet(info.getDone());
				}
				// partLen 为该段总下载数
				if(info.getDone() == partLen)
				{
					// 已下载完成，则退出不用下载了
					Log.i(TAG, getId() + "---> have done ok !");
					checkDownFinish(info.getPath());
					return;
				}
			}
			else
			{
				// 如果没有, 则创建一个新记录存入
				info = new DownloadInfo(url.toString(), id, 0, partLen);
				dao.insert(info);
			}

			// 开始位置 += 已下载量
			int start = id * partLen + info.getDone(); 
			// 结束位置 = N*partLen -1;
			int end = (id + 1) * partLen - 1;
			
			// start <= end
			if(start > end)
			{
				// 非法 或者 已下载完成
				Log.i(TAG, getId() + "---> have done ok !");
				checkDownFinish(info.getPath());
				return;
			}
			
			HttpURLConnection conn = null;
			InputStream in = null;
			RandomAccessFile raf = null;
			
			try
			{
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(3000);
				// 获取指定位置的数据，Range范围如果超出服务器上数据范围, 会以服务器数据末尾为准
				conn.setRequestProperty("Range", "bytes=" + start + "-" + end);
				conn.connect();
				if(conn.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
				    // 不支持断点续传
				    start = 0;
				}
				raf = new RandomAccessFile(file, "rws");
				raf.seek(start);
				// 开始读写数据
				in = conn.getInputStream();
				byte[] buf = new byte[1024 * 10];
				int len;
				while ((len = in.read(buf)) != -1)
				{
					if (isPause.get())
					{
						// 使用线程锁锁定该线程
						synchronized (dao)
						{
							try
							{
								dao.wait();
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}
						}
					}
					if(isStop.get())
					{
						break;
					}
					
					raf.write(buf, 0, len);
					done.addAndGet(len);
					info.setDone(info.getDone() + len);
					// 记录每个线程已下载的数据量
					dao.update(info);
					// 新线程中用Handler发送消息，主线程接收消息
					doProcess();
				}
				// 删除下载记录,当下载完成后
				if( dao.deleteAll(info.getPath(), fileLen))
				{
					// 新线程中用Handler发送消息，主线程接收消息
					doOK();
				}
			}
			catch (IOException e)
			{
				// 中途下载失败，重试三次后，在抛出失败异常
				e.printStackTrace();
				close(conn, in, raf);
				if(retry < 3 && done.get() < fileLen)
				{
					Log.d(TAG, "---id = " + getId() + ": retry = " + retry);
					++retry;
					run();
				}else
				{
					doFail();
				}
			}finally
			{
				close(conn, in, raf);
			}
			
			return;
		}
		
		/** 关闭资源*/
		private void close(HttpURLConnection conn, InputStream in, RandomAccessFile raf)
		{
			if(conn != null)
			{
				conn.disconnect();
				conn = null;
			}
			if(in != null)
			{
				try
				{
					in.close();
					in = null;
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			if(raf != null)
			{
				try
				{
					raf.close();
					raf = null;
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}	
			}
		}
	
		/** 检测是否下载完成*/
		private void checkDownFinish(String path) {
		    // 删除下载记录,当下载完成后
            if( dao.deleteAll(path, fileLen))
            {
                // 新线程中用Handler发送消息，主线程接收消息
                doOK();
            }
		}
	}
	
	/**暂停下载*/
	public void pause()
	{
		isPause.set(true);
	}

	/**继续下载*/
	public void resume()
	{
		isPause.set(false);
		// 恢复所有线程
		synchronized (dao)
		{
			dao.notifyAll();
		}
	}

	/**终止下载*/
	public void stop()
	{
		isStop.set(true);
		// 恢复所有线程
		synchronized (dao)
		{
			dao.notifyAll();
		}
	}

	/** 所有子线程是否正在运行*/
	public boolean isRunning()
	{
		for(Thread thread: workThreadList)
		{
			if(thread.isAlive())
			{
				return true;
			}
		}
		
		return false;
	}
	
	/** 报告失败*/
	private void doFail()
	{
		if(downListener != null)
		{
		    downListener.onFail();
		}
		// 下载失败时，通知其它子线程终止下载， 即停止下载
		stop();
	}

	private void doStart()
	{
	    if(downListener != null)
        {
	        downListener.onStart(fileLen);
        }
	}
	
	private void doProcess()
	{
	    if(downListener != null)
        {
	        downListener.onProcess(done.get(), fileLen);
        }
	}
	
	private void doOK()
	{
	    if(okListener != null)
        {
	        okListener.onOK(fileUri);
        }
	}
	
	/** 下载监听器,在子线程触发 */
    public static interface OnDownListener
    {
        /** 开始连接下载时，报告文件的大小*/
        public void onStart(int size);
        /** 正在下载进度，报告已下载大小和文件大小*/
        public void onProcess(int done_size,int size);
        /** 下载失败 */
        public void onFail(); 
    }
    
    public static interface OnDownOKListener
    {
        /** 下载完成，报告下载文件的路径*/
        public void onOK(String filePath);
    }
}
