/*******************************************************
 * 作者: zengsb
 * 日期: 2011-10-9
 * 描述: 用于管理数据监视器
 * 声明: copyrights reserved by Petfone 2007-2011
 *******************************************************/
package com.android.util.manager;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Hashtable;
import java.util.Iterator;

import android.text.TextUtils;

import com.android.util.system.Logger;

/**
 * 主线程注册/取消注册：registerObserver, unregisterObserver
 * 
 * 其它线程通知:notifyChange
 * 
 */
public class DataObserverManager extends BaseManager {
    /**
     * 一个URI可以有多个Observer, 是不是同一个Observer，是由Uri和名字一起决定的
     */
    private Hashtable<String, Hashtable<String, DataObserver>> observers;

    public void registerObserver(DataObserver... observer) {
        for (DataObserver dataObs : observer) {
            Hashtable<String, DataObserver> group = observers.get(dataObs
                    .getEventName());
            if (group == null) {
                group = new Hashtable<String, DataObserver>();
                group.put(dataObs.getConsumerName(), dataObs);
                observers.put(dataObs.getEventName(), group);
            } else {
                group.put(dataObs.getConsumerName(), dataObs);
            }
        }
    }

    /**
     * 取消注册观察者
     * 
     * @param event
     *            如果为NULL, 则移除consumerName注册的所有Observer,
     *            例如：DataObserver.EVENT_RECEIVED_POS
     * @param consumerName
     *            通常为调用registerObserver的类的class.getName();
     */
    public void unregisterObserver(String consumerName, String event) {
        Hashtable<String, DataObserver> group = null;
        if (!TextUtils.isEmpty(event)) {
            group = observers.get(event);
            unregisterOneObserver(group, consumerName);
        } else {
            for (Hashtable<String, DataObserver> g : observers.values()) {
                unregisterOneObserver(g, consumerName);
            }
        }
    }

    private void unregisterOneObserver(Hashtable<String, DataObserver> group,
            String consumerName) {
        if (group == null) {
            return;
        }
        group.remove(consumerName);
    }

    /**
     * 通知数据变更
     * 
     * @param tableName
     */
    public void notifyChange(String event) {
        processChange(event);
    }

    /**
     * 在UI线程中响应Observer的onChange事件
     * 
     * @param tableName
     */
    private void processChange(String event) {
        Hashtable<String, DataObserver> group = observers.get(event);
        try {
            if (group != null) {
                Collection<DataObserver> values = group.values();
                synchronized (values) {
                    Iterator<DataObserver> it = values.iterator();
                    try {
                        while (it.hasNext()) {
                            final DataObserver observer = it.next();
                            if (observer != null) {
                                // 主线程运行，防止子线程执行导致无效
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            observer.onChange();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Logger.e("-->" + e.getMessage());
                                        }
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.e("-->" + e.getMessage());
                    }
                }
            }
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
            Logger.e("-->" + e.getMessage());
        }
    }

    private static DataObserverManager instance;

    /**
     * 获取单例
     * 
     * @return
     */
    public static DataObserverManager getInstance() {
        if (null == instance) {
            instance = new DataObserverManager();
        }
        return instance;
    }

    private DataObserverManager() {
        observers = new Hashtable<String, Hashtable<String, DataObserver>>();
    }

    @Override
    public void init() {
    }

    @Override
    public void destory() {
        observers.clear();
    }
}
