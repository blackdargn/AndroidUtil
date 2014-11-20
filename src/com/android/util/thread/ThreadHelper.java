/**************************************************************
 * @作者: 				zhaohua
 * @创建时间:		2012-8-30  上午10:46:30
 * @功能描述:		主要实现以下功能
 *                          1. executeWithCallback 在单独线程中执行execuable， 并根据listener是
 *                          UINotifyListener还是NotifyListener决定在当前线程回调还是在UI线程回调
 * @版权声明:本程序版权归 深圳市时代纬科技有限公司所有 Copy right 2010-2012
 **************************************************************/
package com.android.util.thread;

import com.android.util.manager.BaseManager;
import com.android.util.system.Logger;

public class ThreadHelper {
    
    /** 返回Thread 或者 AsyncTask*/
    public static Object executeWithCallback(final Executable<?> executable, NotifyListener<?> listener) {
        if (executable == null) {
            return null;
        }
        // 默认在后台队列线程回调
        if (listener == null) {
            return executeInQThreadPool(executable, null);
        }
        // 从小到大过滤类型
        else if (listener instanceof NUINotifyListener) {
            return executeNewThread(executable, listener);
        }else if(listener instanceof UINotifyListener){
            return executeInUI(executable, (UINotifyListener<?>)listener);
        }else if(listener instanceof NNotifyListener) {
            return executeNewThread(executable, listener);
        }else if(listener instanceof TNotifyListener) {
            return executeInTThreadPool(executable, listener);
        }else if (listener instanceof NotifyListener) {
            return executeInQThreadPool(executable, listener);
        }  else {
            return executeInQThreadPool(executable, listener);
        }
    }
    
    /** 在后台队列中执行*/
    static AsyncTask<Void, Object, Object> executeInQThreadPool(final Executable<?> executable, final NotifyListener<?> listener) {
        AsyncTask<Void, Object, Object> task = new AsyncTask<Void, Object, Object>(listener != null ? listener.getTag() : null) {
            @Override
            protected Object doInBackground(Void... params) {
                Object object = null;
                try {
                    object = executable.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    object = e;
                }
                if (listener != null) {
                    if (object != null && object instanceof Exception) {
                        listener.notify(object, false);
                    } else {
                        try {
                            listener.notify(object, true);
                        }catch(Exception e) {
                            e.printStackTrace();
                            listener.notify("操作失败", false);
                        }
                    }
                }
                return null;
            }
        };
        
        try {
            task.execute();
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.notify("操作失败", false);
            }
        }
        return task;
    }
    
    /** 在后台栈中执行*/
    static TAsyncTask<Void, Object, Object> executeInTThreadPool(final Executable<?> executable, final NotifyListener<?> listener) {
        TAsyncTask<Void, Object, Object> task = new TAsyncTask<Void, Object, Object>(listener != null ? listener.getTag() : null) {
            @Override
            protected Object doInBackground(Void... params) {
                Object object = null;
                try {
                    object = executable.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    object = e;
                }
                if (listener != null) {
                    if (object != null && object instanceof Exception) {
                        listener.notify(object, false);
                    } else {
                        try {
                            listener.notify(object, true);
                        }catch(Exception e) {
                            e.printStackTrace();
                            listener.notify("操作失败", false);
                        }
                    }
                }
                return null;
            }
        };
        
        try {
            task.execute();
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.notify("操作失败", false);
            }
        }
        return task;
    }
    
    /** 在前台栈中执行*/
    static TAsyncTask<Void, Object, Object> executeInUI(final Executable<?> executable, final UINotifyListener<?> listener) {
        TAsyncTask<Void, Object, Object> task = new TAsyncTask<Void, Object, Object>(listener != null ? listener.getTag() : null) {
            protected void onPostExecute(Object result) {
                if (listener != null) {
                    if (result != null && result instanceof Exception) {
                        listener.notify(result, false);
                    } else {
                        try {
                            listener.notify(result, true);
                        }catch(Exception e) {
                            e.printStackTrace();
                            listener.notify("操作失败", false);
                        }
                    }
                }
            }

            @Override
            protected Object doInBackground(Void... params) {
                try {
                    return executable.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    return e;
                }
            }
        };
        try {
            task.execute();
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.notify("操作失败", false);
            }
        }
        return task;
    }

    /**  新开线程进行执行*/
    static Thread executeNewThread(final Executable<?> executable, final NotifyListener<?> listener) {
        Thread one = new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.d(" --> new Thread " + Thread.currentThread().getId());
                Object object = null;
                try {
                    object = executable.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    object = e;
                }
                if (listener != null) {
                    if (object != null && object instanceof Exception) {
                        BaseManager.callback(BaseManager.MSG_NOTIFY_ON_ERROR, listener, object);
                    } else {
                        BaseManager.callback(BaseManager.MSG_NOTIFY_ON_SUCCEED, listener, object);
                    }
                }
            }
        });
        one.start();
        return one;
    }
}