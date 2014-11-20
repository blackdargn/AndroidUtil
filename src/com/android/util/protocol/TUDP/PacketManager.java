package com.android.util.protocol.TUDP;

import java.util.concurrent.ConcurrentHashMap;

import android.os.Handler;
import android.os.Message;

public class PacketManager
{
	static final String TAG = "PacketManager";
	private ConcurrentHashMap<Integer, IPacketObserver> observerMap = new ConcurrentHashMap<Integer, IPacketObserver>();
	private ProtocolFactory protocolFactory;
	
	public PacketManager(IProtocolProxy proxy)
	{
	    protocolFactory = new ProtocolFactory(proxy, this);
	}
	
	public ProtocolFactory getProtocolProxy()
	{
	    return protocolFactory;
	}
	
	/** 发送协议包 out_packet*/
	public void addOutgointPacket(OutPacket packet,IPacketObserver observer)
	{
		if(observer != null)
		{
			observerMap.put(packet.getCmd(), observer);
		}
		protocolFactory.addSendQueue(packet);
	}
	
	/**
	 * 移除观察者 当不使用时
	 * @param key
	 */
	public void removeObserver(int key)
	{
		observerMap.remove(key);
	}
	
	/** 添加邮件观察者 */
	public void addMailObserver(int key, IPacketObserver observer)
	{
		if(observer != null)
		{
			observerMap.put(key, observer);
		}
	}
	
	/** 处理接收包 in_packet, 分发给响应的观察者 */
	public void onReceivedPacket(InPacket inpacket)
	{
		int key = inpacket.getReplyKey();
		if(observerMap.containsKey(key))
		{
			 IPacketObserver observer = observerMap.get(key);
			 if(observer instanceof UIObserver)
			 {
				Message msg = handler.obtainMessage();
				msg.what = MSG_ONRECEIVE;
				msg.obj = inpacket;
				handler.sendMessage(msg);
			 }else
			 {
				 if(!inpacket.isMail())
				 {
					 // 不是邮件包，则移除
					 observerMap.remove(key);
				 }
				 observer.onReceived(inpacket);
			 }
		}
	}
	
	/** 发送包超时处理  */
	public void onTimeoutPacket(OutPacket packet)
	{
		int key = packet.getCmd();
		if(observerMap.containsKey(key))
		{
			IPacketObserver observer = observerMap.get(key);
			 if(observer instanceof UIObserver)
			 {
				Message msg = handler.obtainMessage();
				msg.what = MSG_ONTIMEOUT;
				msg.obj = packet;
				handler.sendMessage(msg);
			 }else
			 {
				 observerMap.remove(key);
				 observer.onTimeout(packet);
			 }
		}
	}
	
	/** UI处理句柄*/
	private static final int MSG_ONRECEIVE = 0x8801;
	private static final int MSG_ONTIMEOUT = 0x8802;
	
	private Handler handler = new Handler()
	{
		public void handleMessage(Message msg) 
		{
			switch(msg.what)
			{
				case MSG_ONRECEIVE:
				{
					InPacket inpacket = (InPacket)msg.obj;
					int key = inpacket.getReplyKey();
					if(observerMap.containsKey(key))
					{
						 IPacketObserver observer = observerMap.get(key);
						 if( !inpacket.isMail())
						 {
							 // 不是邮件包，则移除
							 observerMap.remove(key);
						 }
						 observer.onReceived(inpacket);
					}
					break;
				}
				case MSG_ONTIMEOUT:
				{
					OutPacket packet = (OutPacket)msg.obj;
					int key = packet.getCmd();
					if(observerMap.containsKey(key))
					{
						 IPacketObserver observer = observerMap.get(key);
						 observerMap.remove(key);
						 observer.onTimeout(packet);
					}
					break;
				}
			}
		};
	};
}