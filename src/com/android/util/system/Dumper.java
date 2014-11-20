/*******************************************************
 * @作者: zhaohua
 * @日期: 2011-9-29
 * @描述: 继承自这个类的可以打印出自己的成员信息，调试用
 * @声明: copyrights reserved by personal 2007-2011
*******************************************************/
package com.android.util.system;

import java.lang.reflect.Field;
import java.util.Collection;

public abstract class Dumper
{
	public static final boolean DEBUG = true;
	
	public void dump()
	{
		if(DEBUG)
		{
			StringBuffer sb = new StringBuffer();
			dump(0, sb);
			Log.d("Dump", sb.toString());
		}
	}
	
	public void dump(int depth, StringBuffer sb)
	{		
		@SuppressWarnings("rawtypes")
        Class c = this.getClass();
		if(sb==null)
		{
			sb = new StringBuffer();
		}
		
		int currentDepth = depth;
		addTab(currentDepth, sb);
		sb.append(c.getSimpleName());
		sb.append(" begin");
		sb.append("\n");
		
		dumpFields(++depth, c, sb);
		
		addTab(currentDepth, sb);
		sb.append(c.getSimpleName());
		sb.append(" end");
	}
	
	private void dumpFields(int depth, @SuppressWarnings("rawtypes") Class c, StringBuffer sb)
	{
		if(c == null)
		{
			return;
		}
		
		@SuppressWarnings("rawtypes")
        Class parent = c.getSuperclass();
		if(parent!=null)
		{
			dumpFields(depth, parent, sb);
		}
		
		Field[] fields = c.getDeclaredFields();
		
		for(Field field : fields)
		{
			boolean accessable = field.isAccessible();
			
			field.setAccessible(true);
			try 
			{
				Object o = field.get(this);
				addTab(depth, sb);
				sb.append(field.getName());
				sb.append(":");
				if(o!=null)
				{
					if(o instanceof Dumper)
					{
						sb.append("\n");
						((Dumper) o).dump(++depth, sb);
					}
					else
					{
						if(o instanceof Collection)
						{
							@SuppressWarnings("rawtypes")
                            Collection list = (Collection)o;
							sb.append("\n-------------- "+field.getName()+" Begin --------------\n");
							depth++;
							int index=0;
							for(Object e:list)
							{
								if(e instanceof Dumper)
								{
									sb.append(++index+".\n");
									((Dumper) e).dump(depth, sb);
								}
								else
								{
									sb.append(e.toString()+"\n");
								}
							}
							sb.append("\n-------------- "+field.getName()+" End --------------\n");
						}
						else if(o instanceof short[])
						{
							short[] arr = (short[])o;
							StringBuffer temp = new StringBuffer();
							if(arr!=null)
							{
								for(short s:arr)
								{
									temp.append(s+" ");
								}
								
								sb.append(temp);
							}
						}
						else
						{
							sb.append(o.toString());
						}
					}
				}
				sb.append("\n");		
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
			field.setAccessible(accessable);
		}
	}
	
	private void addTab(int depth, StringBuffer sb)
	{
		for(int i=0;i<depth;i++)
		{
			sb.append("    ");
		}
	}
}
