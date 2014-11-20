package com.android.util.manager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.android.util.system.Logger;

public abstract class QueueExcuter <T>{
    private ConcurrentLinkedQueue<KeyITask<T>> mQueue;
    private ConcurrentHashMap<Long, KeyITask<T>> mMap;
    private OnTimeShotListener<T> mListener;
    private boolean isStart = false;
    
    public QueueExcuter() {
        mQueue = new ConcurrentLinkedQueue<KeyITask<T>>();
        mMap    = new ConcurrentHashMap<Long, KeyITask<T>>();
    }
    
    public abstract void start();
    public abstract void stop();
    
    public void setOnTimeShotListener(OnTimeShotListener<T> listener ) {
        mListener = listener;
    }
    
    /** 添加到队列中前面*/
    public void add(KeyITask<T> one) {
        if(one == null) return;
        Logger.d("#-->add ");
        if(!mMap.containsKey(one.getKey())) {
            mMap.put(one.getKey(), one);
            mQueue.offer(one);
        }
    }
    
    /** 尝试开始，如果未开始，则开始，已开始，则无作用*/
    public void tryExcute() {
        if(!isStart) {
            isStart = true;
            excute();
        }
    }
    
    /** 取消延时容量*/
    public void cancel() {
        Logger.d("#-->cancel ");
        mQueue.clear();
        mMap.clear();
        isStart = false;
    }
    
    /** 执行*/
    protected void excute() {
        Logger.d("#-->excute ");
        if(mQueue.isEmpty()) {
            stop();
            isStart = false;
            if(mListener != null) {
                mListener.onTimeEnd();
            }
        }else {
            KeyITask<T> delay = mQueue.peek();
            if(mListener != null) {
                int r = mListener.onTimeShot(delay.getData());
                if(  r > 0 ) {
                    // 已正常执行
                    mQueue.poll();
                    mMap.remove(delay.getKey());
                }else 
                if(r < 0) {
                    // 非正常 丢弃
                    mQueue.poll();
                    mMap.remove(delay.getKey());
                    excute();
                }else{
                    // 未执行，下次催促执行
                    isStart = false;
                    Logger.d("#-->excute undo");
                }
            }
        }
    }
    
    public static interface OnTimeShotListener<T>{
        public int onTimeShot(T one);
        public void onTimeEnd();
    }
    
    public static interface KeyITask<T>{
        public long   getKey();
        public T        getData();
    }
}