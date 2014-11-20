package com.android.util.thread;
import java.util.concurrent.atomic.AtomicBoolean;
/*******************************************************
 * @author : zhaohua
 * @version : 2012-6-6
 * @see :: 自定义循环执行线程,Runnable为循环执行体
 * @Copyright : copyrights reserved by personal 2007-2012
*******************************************************/
public class CycledThread extends Thread
{
    private boolean stopThrad;
    private AtomicBoolean lock = new AtomicBoolean();
    /** 长睡眠时间*/
    private int longSleepTime;
    /** 短睡眠时间,默认为100ms*/
    private int shortSleepTime = 100;
    /** 是否要睡眠*/
    private boolean longOrShortAble = true;
    private boolean isOneTime = false;
    /** 循环超时时间: 0 默认没有超时*/
    private int timeoutTime = 0;
    /** 超时监听器*/
    private OnTimeoutListener onTimeoutListener;
    
    /** 无限循环构造器*/
    public CycledThread(Runnable runnable, int longSleepTime)
    {
        this(runnable,longSleepTime, 0, null);
    }
    
    /** 超时循环构造器*/
    public CycledThread(Runnable runnable, int longSleepTime, int timeout, OnTimeoutListener listener)
    {
        super(runnable);
        this.longSleepTime = longSleepTime;
        this.timeoutTime = timeout;
        this.onTimeoutListener = listener;
        setName((timeoutTime == 0 ? "cycled-":"timeout cycled-")+Thread.activeCount()+1);
    }
    
    /** 一次循环超时构造器*/
    public CycledThread(Runnable runnable, int timeout, OnTimeoutListener listener)
    {
        super(runnable);
        this.longSleepTime = 200;
        this.isOneTime = true;
        this.timeoutTime = timeout;
        this.onTimeoutListener = listener;
        setName((timeoutTime == 0 ? "cycled-":"timeout cycled-")+Thread.activeCount()+1);
    }
    
    /** 超时循环构造器*/
    public CycledThread(int longSleepTime, int timeout, OnTimeoutListener listener)
    {
        this(null,longSleepTime, timeout, listener);
    }
    
    /** 设置大周期长睡眠时间*/
    public void setLongSleepTime(int sleep)
    {
        this.longSleepTime = sleep;
    }
    
    /** 设置小周期短睡眠时间*/
    public void setShortSleepTime(int sleep)
    {
        this.shortSleepTime = sleep;
    }
    
    /** 设置是否要指定休眠；true则长睡眠，否则短睡眠*/
    public void setSleepable(boolean longOrShort)
    {
        longOrShortAble = longOrShort;
    }
    
    @Override
    public void run()
    {
        int time = 0;
        int sleepTime = 0;
        int count = 0;
        
        while(!stopThrad)
        {
            // 是否被暂停
            _ifWait();
            // 执行工作
            if(isOneTime)
            {
                if( count == 0)
                {
                    super.run();
                    count++;
                }
            }else
            {
                super.run();
            }
            // 睡眠
            if(longOrShortAble)
            {
                // 指定休眠
                sleepTime = longSleepTime;
            }else
            {
                // 基本不休眠
                sleepTime = shortSleepTime;
            }       
            try
            {               
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            // 超时判断
            if(timeoutTime > 0)
            {
                time += sleepTime;
                if(time >= timeoutTime)
                {
                    // 超时通知
                    if(onTimeoutListener != null)
                    {
                        onTimeoutListener.onTimeout();
                    }
                    // 超时，停止线程
                    _stop();
                }
            }
        }
    }
    
    private void _ifWait()
    {
        synchronized (lock)
        {
            if(lock.get())
            {
                try
                {
                    lock.wait();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void _stop()
    {
        synchronized (lock)
        {
            stopThrad = true;
            if(lock.getAndSet(false))
            {
                lock.notify();
            }
        }
    }
    
    public void _pause()
    {
        synchronized (lock)
        {
            lock.set(true);         
        }
    }
    
    public void _resume()
    {
        synchronized (lock)
        {
            if(lock.getAndSet(false))
            {
                lock.notify();
            }
        }
    }
    
    /** 超时监听器*/
    public static interface OnTimeoutListener
    {
        public void onTimeout();
    }
}