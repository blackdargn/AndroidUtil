/***
 * Copyright (c) 2008-2009 CommonsWare, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package com.android.util.bus;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.android.util.system.Logger;

/**
 * @see 抽象消息总线
 * @author zhaohua
 *
 * @param <M> 消息
 * @param <F> 过滤器, 只能是final常规类型：Integer ... String...
 * @param <S> 过滤策略
 */
abstract public class AbstractBus<M, F, S extends AbstractBus.Strategy<M, F>>
{
	/** 策略*/
	private S strategy = null;
	/** 注册集合*/
	private ConcurrentHashMap<F, ConcurrentLinkedQueue<Registration> > regs = new ConcurrentHashMap<F, ConcurrentLinkedQueue<Registration> >();
	/** 测试计数*/
	private volatile int count = 0;
	
	/** @see 设置策略*/
	void setStrategy(S strategy)
	{
		this.strategy = strategy;
	}
	
	/** @see 同步发送消息*/
	protected void send(F filter, M... messages)
	{
		sendActual(filter, messages);
	}
	
	/**
	 * @see 注册，保证  键值<filter,receiver,tag>唯一
	 * @param filter 过滤器
	 * @param receiver 接受者
	 */
	public void register(F filter, Receiver<M> receiver)
	{
		if(filter == null || receiver == null)
		{
			return;
		}
		
		if( !regs.containsKey(filter) )
		{
			regs.put(filter, new ConcurrentLinkedQueue<Registration>());
		}
		
		ConcurrentLinkedQueue<Registration> list = regs.get(filter);
		if(list != null)
		{		
			for(Registration r : list)
			{
				if(r.filter.equals(filter) && r.receiver == receiver)
				{
					// 已经存在相同的
					return;
				}
			}
			// 没有
			list.add(new Registration(filter, receiver));
			Logger.d("###register ++"+ (++count));
		}
	}


	/**
	 * @see 注销所有接收者为receiver的注册者，移除接收者 或者 监听者
	 * @param receiver 接受者
	 */
	public void unregister(F filter, Receiver<M> receiver)
	{
		if(filter == null)
		{
			return;
		}
		
		if(regs.containsKey(filter))
		{
			ConcurrentLinkedQueue<Registration> list = regs.get(filter);
			for(Registration r : list)
			{
				if(r.filter.equals(filter) && r.receiver == receiver)
				{
					list.remove(r);
					Logger.d("###unregister --"+(--count));
					return;
				}
			}
		}
	}
	
	/**
	 * 确切发送消息
	 * @param messages 消息不定参
	 */
	private void sendActual(F filter, M... messages)
	{
		if(regs.containsKey(filter))
		{
			ConcurrentLinkedQueue<Registration> list = regs.get(filter);
			for (M message : messages)
			{
				for(Registration r : list)
				{
					synchronized (r)
					{
						r.tryToSend(message);
					}
				}
			}
		}
	}
	
	/**
	 * @see 消息接收者
	 * @param <M> 消息
	 */
	public static abstract class Receiver<M> implements Serializable
	{	
		private static final long serialVersionUID = -695371538346262828L;

		public abstract void onReceive(M message);
	}

	/**
	 * @see 接收者进行消息处理,可由子类覆盖进行不同方式的处理
	 * @param receiver
	 * @param message
	 */
	protected void onReceive(Receiver<M> receiver, M message)
	{
		receiver.onReceive(message);
	}
	
	/**
	 * @see 过滤策略
	 * @param <M> 消息
	 * @param <F> 过滤器
	 */
	public interface Strategy<M, F>
	{
		boolean isMatch(M message, F filter);
	}

	/**
	 * @see 注册者
	 * @param <M> 消息
	 * @param <F> 过滤器
	 */
	private class Registration
	{
		// 过滤器
		F filter = null;
		// 接收者
		Receiver<M> receiver = null;

		Registration(F filter, Receiver<M> receiver)
		{
			this.filter = filter;
			this.receiver = receiver;
		}
		
		/** @see 尝试发送消息，如果匹配的话 */
		void tryToSend(M message)
		{
			if (strategy.isMatch(message, filter))
			{
				// 有则发送给接收者
				onReceive(receiver,message);
			}
		}
	}
}