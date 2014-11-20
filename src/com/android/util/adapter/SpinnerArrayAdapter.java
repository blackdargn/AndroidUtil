/*******************************************************
 * @作者: zhaohua
 * @日期: 2012-7-5
 * @描述: 城市Spinner适配器
 * @声明: copyrights reserved by Petfone 2007-2011
*******************************************************/
package com.android.util.adapter;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * @author zhaohua
 *
 */
@SuppressWarnings("rawtypes")
public class SpinnerArrayAdapter<T> extends ArrayAdapter
{ 
	public SpinnerArrayAdapter(Context context)
	{
		super(context, android.R.layout.simple_spinner_item);
		setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}
	
	@SuppressWarnings("unchecked")
    public void setList(List<T> list)
	{
	    if(list != null && list.size() > 0)
	    {
	        for(T one : list)
	        {
	            add(one);
	        }
	    }else
	    {
	        clear();
	    }
	}
}
