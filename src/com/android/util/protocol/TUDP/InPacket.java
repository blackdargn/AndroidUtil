/*******************************************************
 * 作者: zhaohua
 * 日期: 2012-05-30
 * 描述: 接收到的包的基类
 * 声明: copyrights reserved by Petfone 2007-2011
 *******************************************************/
package com.android.util.protocol.TUDP;

public abstract class InPacket extends Packet
{
	/** 解析协议包*/
	public abstract void parse(IPacketParser parser);
	
	/** 获取回复对应的发送包的关键字*/
	public abstract int getReplyKey();
	
	/** 是否是邮件包,如果是邮件的话，重写这个方法 */
	public boolean isMail()
	{
		return false;
	}
}