/**************************************************************
 * @作者: 				zhaohua
 * @创建时间:		2012-8-30  上午10:44:56
 * @功能描述:		类似于Runnable的一个类，用于指定一段要执行的代码， 由ThreadHelper来执行，主要与ThreadHelper结合使用
*
* @版权声明:本程序版权归 深圳市时代纬科技有限公司所有 Copy right 2010-2012
**************************************************************/
package com.android.util.thread;

public abstract class Executable<T>
{
	public abstract T execute() throws Exception;
}