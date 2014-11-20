package com.android.util.widget;

import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-9-25
 * @see : 通用水平滑动tab索引视图
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class TabIndexView extends HorizontalScrollView implements OnClickListener
{
    private Vector<TabView> mTabIndexViews = new Vector<TabView>();
    private OnTabChangedListener mOnTabListener;
    private LinearLayout mContainer;
    private int mCurTabId;
    
    public TabIndexView(Context context)
    {
        super(context);
        mContainer = new LinearLayout(context);
        addView(mContainer);
    }
    
    public TabIndexView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mContainer = new LinearLayout(context);
        addView(mContainer);
    }
    
    public void setOnTabChangedListener(OnTabChangedListener listener)
    {
        mOnTabListener = listener;
    }
    
    public void addTab(int id, View tabView, Intent intent)
    {
        mTabIndexViews.add(new TabView(id,tabView,intent));
        mContainer.addView(tabView);
        tabView.setOnClickListener(this);
    }

    public void showTab(int id)
    {
        if(mCurTabId != id)
        {
            for(TabView one : mTabIndexViews)
            {
                if(one.id == id)
                {
                    onClick(one.view);
                }
            }
        }
    }
    
    public void setTabVisible(int id, boolean visible)
    {
        for(TabView one : mTabIndexViews)
        {
            if(one.id == id)
            {
                one.view.setVisibility(visible ? View.VISIBLE : GONE);
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        for(TabView one : mTabIndexViews)
        {
            if(one.view != v)
            {
                one.view.setPressed(false);
            }else
            {
                v.setPressed(true);
                mCurTabId = one.id;
                if(mOnTabListener != null)
                {
                    mOnTabListener.onTabChanged(one.id, one.intent);
                }
            }
        }
    }
    
    public class TabView
    {
        int id;
        View view;
        Intent intent;
        
        public TabView(int id,View view,Intent intent)
        {
            this.id = id;
            this.view = view;
            this.intent = intent;
        }
    }
    
    public interface OnTabChangedListener
    {
        public void onTabChanged(int id, Intent subWindows);
    }
}
