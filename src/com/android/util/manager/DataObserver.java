/*******************************************************
 * 作者:zengsb
 * 日期:2011-12-19
 * 描述: 数据库监视器，用于监视数据表变化
* 声明: copyrights reserved by Petfone 2007-2011
*******************************************************/
package com.android.util.manager;


public abstract class DataObserver
{
	///////////////////////////////////////////////////////////////////////////////////////////////////	

	///////////////////////////////////////////////////////////////////////////////////////////////////
	private String consumerName;
	private String event;
	
	/**
	 * 哪个类注册的
	 * @return
	 */
	public String getConsumerName()
	{
		return consumerName;
	}

	/**
	 * 观察哪个表
	 * @return
	 */
	public String getEventName()
	{
		return event;
	}
	
	public DataObserver(String consumerName, String event)
	{
		this.consumerName = consumerName;
		this.event = event;
	}
	public abstract void onChange();
}
