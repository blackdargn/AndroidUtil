package com.android.util.adapter;
import java.util.List;

/*******************************************************
 * @author: zhaohua
 * @version: 2012-8-23
 * @see: 后台的一种数据载入句柄，用于提供数据模型， 控制器类型
 * @Copyright : copyrights reserved by personal 2007-2012
 * @param <T>
*******************************************************/
public interface DataLoaderHandler<T> 
{
    /**
     * 最大载入数量，这个方法应该仅仅在初始化下载发生后调用
     * @return 最大载入数量.
     */
    int getMaxItems();
    
    /**
     * 获取下一次载入的值，获取更多操作
     * @param 数据有效到达时，通知回调刷新视图
     */
    void getNext(DataLoadedCallback<T> callback);

    /**
     * 后台处理中，标示是否正在载入，获取是否载入完成
     * @return
     */
    boolean isLoading();
    
    /**
     * 是否还有下一组数据可获取
     * @return
     */
    boolean haveNext();
    
    /**
     * 数据有效时，视图刷新回调
     * @param <T>
     */
    interface DataLoadedCallback<T> 
    {
        /**
         * 通知数据有效时，视图处理
         * @param 有效数据
         */
        void onLoaded(List<T> values);
               
        /**
         * 载入出错的时候
         */
        void onError();
    }
}
