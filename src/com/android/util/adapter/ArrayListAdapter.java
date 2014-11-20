/*******************************************************
 * @作者: zhaodh
 * @日期: 2011-12-19
 * @描述: 数组ListView适配器
 * @声明: copyrights reserved by Petfone 2007-2011
*******************************************************/
package com.android.util.adapter;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

/**
 * @see 线性列表适配器，用于ListView,GridView,TableView。
 * @param <T>
 */
public abstract class ArrayListAdapter<T> extends BaseAdapter
{
    /** model data */
    protected List<T> mList;
    /** parent context */
    protected Context mContext;
    /** view */
    protected ListView mListView;
    /** view */
    protected LayoutInflater inflater;
    /** 当设置的list为null时代理*/
    private List<T> nilList = new ArrayList<T>(0);
    
    public ArrayListAdapter(Context context)
    {
        this.mContext = context;
        mList = nilList;
        inflater = LayoutInflater.from(context);
    }
    /** 获取适配器中的size*/
    public int getCount()
    {
        if (mList != null) 
        {
        	return mList.size();
        }
        else
        {
        	return 0;
        }
    }
    /** 获取适配器中该索引的Item*/
    public T getItem(int position)
    {
        // 检验
        if (position < 0 || position >= mList.size())
        {
            return null;
        }
        // 获取
        try
        {
            return mList == null ? null : mList.get(position);
        } catch (ArrayIndexOutOfBoundsException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    /** 获取适配器中该索引位置的Item的id，默认 position*/
    public long getItemId(int position)
    {
        return position;
    }
    /** 获取适配器中该position位置的视图*/
    abstract public View getView(int position, View convertView,
            ViewGroup parent);

    // ///////////////////////////////////////////////////////////////////////
    // exchange list
    /** 设置模型 */
    public void setList(List<T> list)
    {
        if (list == null)
        {
            nilList.clear();
        	mList = nilList;        	
        }else
        {
        	this.mList = list;
        }
        notifyDataSetChanged();
    }
    /** 设置模型 */
    public void setList(T[] array)
    {
        if (array == null || array.length == 0)
        {
        	setList((List<T>)null);
        }else
        {
	        ArrayList<T> list = new ArrayList<T>();
	        for ( T t : array )
	        {
	            list.add(t);
	        }
	        setList(list);
        }
    }

    /** 设置对象*/
    public void setItem(T one, int pos)
    {
    	 if (mList == null || mList.size() == 0 || pos < 0 || pos >= mList.size())
         {
         	return;
         }else
         {
 	        synchronized (mList)
 	        {
 	        	mList.set(pos, one);
 	        }
         }
         notifyDataSetChanged();
    }
    /** add list */
    public void addItems(List<T> list)
    {
        if (list == null || list.size() == 0)
        {
        	
        }else
        {
	        synchronized (list)
	        {
	            this.mList.addAll(list);
	        }
        }
        notifyDataSetChanged();
    }
    /** add list at header*/
    public void addItemsPre(List<T> list)
    {
        if (list != null && list.size() > 0)
        {
        	synchronized (list)
            {
                this.mList.addAll(0,list);
            }           
        }
        notifyDataSetChanged();
    }
    /** 恢复2个列表的连接*/
    public void setRestoreList(List<T> list1, List<T> list2)
    {
        ArrayList<T> list = new ArrayList<T>();
        if(list1 != null) 
        {
            for ( T t : list1)
            {
                list.add(t);
            }
        }
        if(list2 != null) 
        {
            for ( T t : list2)
            {
                list.add(t);
            }
        }
        this.mList = list;
        notifyDataSetChanged();
    }
    
    /** add list */
    public void addItems(T[] array)
    {
        if (array == null || array.length == 0)
        {
        	
        }else
        {
	        for ( T value : array )
	        {
	            this.mList.add(value);
	        }
        }
        notifyDataSetChanged();
    }
    /** add list */
    public void addItem(T item)
    {
        if (item == null) return;
        this.mList.add(item);
        notifyDataSetChanged();
    }
    /** remove list */
    public void removeItem(T item)
    {
        if (item == null) return;
        this.mList.remove(item);
        notifyDataSetChanged();
    }
    /** remove all list */
    public void removeAll()
    {
        if(mList != null)
        {
            mList.clear();
        }
        notifyDataSetChanged();
    }
    /** get List clone */
    public ArrayList<T> getCList()
    {
        ArrayList<T> list = new ArrayList<T>();
        if(mList != null)
        {
        	list.addAll(mList);
        }
        return list;
    }
    // //////////////////////////////////////////////////////////////////////  
    @Override
    public void notifyDataSetChanged() 
    {
    	super.notifyDataSetChanged();
    }
}