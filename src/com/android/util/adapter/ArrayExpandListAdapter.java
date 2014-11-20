package com.android.util.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

/*******************************************************
 * @author: zhaohua
 * @version: 2012-6-12
 * @see: 普通展开适配器
 * @Copyright: copyrights reserved by personal 2007-2012
*******************************************************/
public abstract class ArrayExpandListAdapter<G,C> extends BaseExpandableListAdapter
{
	/** model data */
	private List<ExpandData<G,C>> mList;
	 /** parent context */
    protected Context mContext;
	/** view */
    protected LayoutInflater inflater;
    
	public ArrayExpandListAdapter(Context context)
	{
		this.mContext = context;
        mList = new ArrayList<ExpandData<G,C>>(0);
        inflater = LayoutInflater.from(context);
	}
	
	public void setList(List<ExpandData<G,C>> list)
	{
		mList = list;
		notifyDataSetChanged();
	}
	
	public List<ExpandData<G,C>> getList()
	{
		return mList;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		if(mList == null || mList.size() == 0)
		{
			return null;
		}else
		{
			List<C> list = mList.get(groupPosition).chiles;
			if(list == null || list.size() == 0)
			{
				return null;
			}else
			{
				return list.get(childPosition);
			}
		}
	}

	@Override
	public long getChildId(int groupPosition, int childPosition)
	{
		return -1;
	}

	@Override
	public abstract View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent);
	
	@Override
	public int getChildrenCount(int groupPosition)
	{
		if(mList == null || mList.size() == 0)
		{
			return 0;
		}else
		{
			List<C> list = mList.get(groupPosition).chiles;
			if(list == null || list.size() == 0)
			{
				return 0;
			}else
			{
				return list.size();
			}
		}
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		if(mList == null || mList.size() == 0)
		{
			return null;
		}else
		{
			return mList.get(groupPosition).group;
		}
	}

	@Override
	public int getGroupCount()
	{
		if(mList == null || mList.size() == 0)
		{
			return 0;
		}else
		{
			return mList.size();
		}
	}

	@Override
	public long getGroupId(int groupPosition)
	{
		return -1;
	}
	
	@Override
	public abstract View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent);

	@Override
	public boolean hasStableIds()
	{
		return false;
	}
	
	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return true;
	}
	
	public static class ExpandData<Group,Child>
	{
		public ExpandData(Group group, List<Child> childs)
		{
			this.group = group;
			this.chiles = childs;
		}
		public Group group;
		public List<Child> chiles;
	}
}