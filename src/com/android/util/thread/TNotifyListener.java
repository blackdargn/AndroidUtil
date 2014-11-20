/**************************************************************
 * @作者: 				zhaohua
 * @创建时间:		2012-8-30  上午10:48:20
 * @功能描述:		当前线程回调
 *
 * @版权声明:本程序版权归 深圳市时代纬科技有限公司所有 Copy right 2010-2012
 **************************************************************/
package com.android.util.thread;

public class TNotifyListener<T> extends NotifyListener<T> {
    public TNotifyListener() {}
    
    public TNotifyListener(String tag) {
        super(tag);
    }
}