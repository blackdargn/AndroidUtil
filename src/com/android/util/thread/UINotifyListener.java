/**************************************************************
 * 作者: 				zengsb
 * 创建时间:		2012-8-30  上午10:48:34
 * 功能描述:		UI线程中回调
 *
 * 版权声明:本程序版权归 深圳市时代纬科技有限公司所有 Copy right 2010-2012
 **************************************************************/
package com.android.util.thread;

public class UINotifyListener<T> extends TNotifyListener<T>
{   
    /** 只有UI线程中才会调用 */
    protected void onPostExecute() {}
    
    public void notify(Object object, boolean isSuceecd) {
        onPostExecute();
        super.notify(object, isSuceecd);
    };
    
    public UINotifyListener() {
        
    }
    
    public UINotifyListener(String tag) {
        super(tag);
    }
}