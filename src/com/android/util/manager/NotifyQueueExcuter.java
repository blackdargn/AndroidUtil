package com.android.util.manager;

import java.util.concurrent.ConcurrentHashMap;

public class NotifyQueueExcuter<T> extends QueueExcuter<T>{
   
    private static ConcurrentHashMap<String, NotifyQueueExcuter<?>> instanceMaps = 
            new ConcurrentHashMap<String, NotifyQueueExcuter<?>>();
    private NotifyQueueExcuter() {}
    
    @SuppressWarnings("unchecked")
    public static <F> NotifyQueueExcuter<F> getInstance(String tag){
        if(tag == null){
            // new 
            return new NotifyQueueExcuter<F>();
        }else
        if(instanceMaps.containsKey(tag)) {
            return (NotifyQueueExcuter<F>)instanceMaps.get(tag);
        }else {
            NotifyQueueExcuter<F> one = new NotifyQueueExcuter<F>();
            instanceMaps.put(tag, one);
            return one;
        }
    }
    
    @Override
    public void start() {
        excute();
    }
    
    @Override
    public void add(QueueExcuter.KeyITask<T> one) {
        super.add(one);
        tryExcute();
    }

    @Override
    public void stop() {
        cancel();
    }
}