package com.android.util.protocol.TUDP;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class OutPacket extends Packet
{
	/** 发送模式  0：实时；1：延时*/
	protected byte sendMode = 0;
	/** 初始发送时间 */
	private long initSendTime;
	/** 上次发送时间 */
	private long preSendTime;
	/** 重发次数*/
	private int restSendCount;
	/** 是否已发送，防止超时与重发的错误性 */
	private boolean isSend;
	/** 是否已收到回复, 默认为false*/
	public AtomicBoolean isReplyed = new AtomicBoolean(false);
	
	/**
	 * 组装器
	 */
	protected IPacketCreator creator;
	
	public OutPacket(IPacketCreator creator)
	{		
		this.creator = creator;
	}
	
	/** 获取协议包的输入流*/
	public Object getBody()
	{
		// 先清除
		creator.clear();
		// 在组装
		put(creator);
		// 后获取
		return creator.getBuffer();
	}
	
	/** 组装 协议业务数据 部分，由子类基础*/
	protected abstract void put(IPacketCreator creator);
	
	/** 是否是实时包*/
	public boolean isReal()
	{
		return sendMode == 0;
	}
	
	/** 是否需要回复，要回复的有状态与重发机制，不要的就不管了*/
	public boolean isNeedReply()
	{
		return true;
	}
	
	/** 是否可以重发*/
	public boolean isReSentable(int timeout)
	{
		if(!isSend)
		{
			// 还没发送呢,不记重发
			return false;
		}
		long detal = System.currentTimeMillis() - preSendTime;
		boolean r = ( detal >= timeout);
		if(r)
		{
			// 要重发，重置状态
			isSend = false;
		}
		return r;
	}
	
	/** 是否超时，包括时间超时 和 发送次数超时 */
	public boolean isTimeOutable(int timeout, int sendout)
	{
		if(!isSend)
		{
			// 还没发送呢,不记超时
			return false;
		}
		long detal = System.currentTimeMillis() - initSendTime;
		return (detal >= timeout || restSendCount >= sendout);
	}
	
	/** 当这个包，发送之前进行时间更新*/	
	public void updateTime()
	{
		if(initSendTime == 0)
		{
			// 第一次发送
			initSendTime = System.currentTimeMillis();
			preSendTime  = initSendTime;
		}else
		{
			// 重发
			preSendTime = System.currentTimeMillis();
			restSendCount++;
		}
		// 已发送
		isSend = true;
	}	
}