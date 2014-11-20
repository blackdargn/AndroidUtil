/**************************************************************
 * @作者: 				zhaohua
 * @创建时间:		2012-8-30  上午10:48:20
 * @功能描述:		当前线程回调
 *
 * @版权声明:本程序版权归 深圳市时代纬科技有限公司所有 Copy right 2010-2012
 **************************************************************/
package com.android.util.thread;

public class NotifyListener<T extends Object> {
    
    private String tag;
    
    public NotifyListener() {}
    
    public NotifyListener(String tag) {
        this.tag = tag;
    }
    
    public String getTag() {
        return tag;
    }
    
    /** 成功调用 */
    protected void onSucceed(T object) {}
    /** 出错调用 */
    protected void onError(Object object) {}
    
    @SuppressWarnings("unchecked")
    public void notify(Object object, boolean isSuceecd) {
        if(isSuceecd) {
            onSucceed((T)object);
        }else {
            onError(object);
        }
    }
}