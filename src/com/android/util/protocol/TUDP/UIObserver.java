/*******************************************************
 * @作者: zhaohua
 * @日期: 2011-12-19
 * @描述: 数据包监视器，用于监视数据包收发状态, 可用于UI线程
 * @声明: copyrights reserved by Petfone 2007-2011
*******************************************************/
package com.android.util.protocol.TUDP;
public class UIObserver implements IPacketObserver
{
    @Override
    public void onReceived(InPacket packet)
    {
    }
    @Override
    public void onTimeout(OutPacket packet)
    {
        
    }
}