/*******************************************************
 * @作者: zhaohua
 * @日期: 2011-11-10
 * @描述: 系统消息总线,新增了UI线程执行的接收者
 * @声明: copyrights reserved by Petfone 2007-2011
 *******************************************************/

package com.android.util.bus;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.android.util.bus.MessageBus.MMessage;

public class MessageBus extends AbstractBus<MMessage, Integer, MessageBus.MessageStrategy>
{	
    public static final String KEY = "key_receiver";
	/** UI处理句柄*/
	private final static Handler handler = new Handler()
	{
		public void handleMessage(Message msg) 
		{
		    MMessage message = (MMessage)msg.obj;
		    @SuppressWarnings("unchecked")
            Receiver<MMessage> receiver = (Receiver<MMessage>)msg.getData().getSerializable(KEY);
		    try {
		        receiver.onReceive(message);
		    }catch(Exception e) {
		        e.printStackTrace();
		    }
		}
	};
	
	private MessageBus()
	{
		super();
		setStrategy(new MessageStrategy());
	}
	
    /** 单列模式 */
    private static MessageBus instance = new MessageBus();
    public static MessageBus getBusFactory()
    {
        return instance;
    }
	
	/** UIReceiver的就在UI线程中处理，否则默认处理 */
	protected void onReceive(
	        Receiver<MMessage> receiver,
			final MMessage message)
	{
		if(receiver instanceof UIReceiver && Looper.myLooper() != Looper.getMainLooper())
		{
			Message msg = message.toMessage(receiver);
			handler.sendMessage(msg);
		}else
		{
		    try {
		        receiver.onReceive(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
		}
	}
	
	/** 发送消息*/
	public void send(int what, Object obj, int arg1, int arg2)
	{
		super.send(what, new MMessage(what, obj, arg1, arg2));
	}
	
	/** 发送消息*/
    public void send(int what, Object obj, int arg1)
    {
        send(what, obj, arg1, 0);
    }
    
    /** 发送消息*/
    public void send(int what, int arg1, int arg2)
    {
        send(what, null, arg1, arg2);
    }
    
    /** 发送消息*/
    public void send(int what, int arg1)
    {
        send(what, null, arg1, 0);
    }
    
    /** 发送消息*/
    public void send(int what, Object obj)
    {
        send(what, obj, 0, 0);
    }
    
    /** 发送消息*/
    public void send(int what)
    {
        send(what, null, 0, 0);
    }
	
	/**
	 * UI线程消息接收者
	 * @param <M>
	 */
	public static abstract class UIReceiver extends Receiver<MMessage>
	{
		private static final long serialVersionUID = 8595418220936525439L;
	}
	
	/** 消息策略：由过滤器 filter == message.what 匹配*/
	static class MessageStrategy implements AbstractBus.Strategy<MMessage, Integer>
	{
		public boolean isMatch(MMessage message, Integer filter)
		{
			return (filter != null && message != null && filter.equals(message.what));
		}
	}
	
	/** 系统 Message的替身，为了防止出现， Message in use 异常*/
	public class MMessage
	{
		 int what;
		 Object obj;
		 int arg1;
		 int arg2;
		
		MMessage(int what, Object obj, int arg1, int arg2)
		{
			this.what = what;
			this.obj = obj;
			this.arg1 = arg1;
			this.arg2 = arg2;
		}
		
		public int what() {
		    return what;
		}
		
		public Object obj() {
		    return obj;
		}
		
		public int arg1() {
		    return arg1;
		}
		
		public int arg2() {
		    return arg2;
		}
		
		Message toMessage(Receiver<MMessage> receiver)
		{
			Message msg = handler.obtainMessage();	
			msg.obj = this;
			msg.getData().putSerializable(KEY, receiver);
			return msg;
		}
	}
}
