package com.android.util.widget;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.util.R;
import com.android.util.fragment.OnTabChangedListener;

public class CUPagerHost extends LinearLayout implements OnClickListener,OnPageChangeListener {
	private LayoutInflater mLayoutInflater;
	private OnTabChangedListener mTabSelectedListener;
	private OnPageChangeListener mOnPageChangeListener;
	private SparseArray<TabSpc> mFragmentMap;
	private ViewPager viewPager;
	private int lastIndex = 0;

	public CUPagerHost(Context context) {
	    this(context, null);
	}
	
	public CUPagerHost(Context context, AttributeSet attrs) {
		super(context, attrs);
		mLayoutInflater = ((Activity) getContext()).getLayoutInflater();
		mFragmentMap = new SparseArray<TabSpc>();
	}
	
	public void setViewPager(ViewPager viewPager) {
	    this.viewPager = viewPager;
	    this.viewPager.setOnPageChangeListener(this);
	}

	public void destryTabHost() {
		mFragmentMap.clear();
		mFragmentMap = null;
	}
	
	/**
	 * 添加带按键事件的tab
	 * @param tabSpc
	 * @param index
	 * @param listener
	 */
	public void addTab(TabSpc tabSpc, int index, OnClickListener listener){
	    if (tabSpc == null || (TextUtils.isEmpty(tabSpc.name) && tabSpc.tabView == null)) {
            return;
        }
	    if(tabSpc.tabView == null) {
            View tabView = mLayoutInflater.inflate(R.layout.view_tabitem, null);
            if (tabSpc.background > 0) {
                tabView.setBackgroundResource(tabSpc.background);
            }		
            TextView text = (TextView) tabView.findViewById(R.id.tab_body);             
            if(index > 0){
                text.setTextColor(getContext().getResources().getColor(R.color.white));
                text.setCompoundDrawablesWithIntrinsicBounds(0, tabSpc.icon, 0, 0);
            }else{
                text.setTextColor(getContext().getResources().getColor(R.color.tab_selected));
                text.setCompoundDrawablesWithIntrinsicBounds(0, tabSpc.downIcon, 0, 0);
            }
            text.setText(tabSpc.name);
            tabSpc.tabView = tabView;
	    }
	    tabSpc.tabView.setTag(index);
	    tabSpc.tabView.setOnClickListener(listener);				
		addTab(tabSpc,index);
	}
	
	private void addTab(TabSpc tabSpc, int index) {
	    LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
        addView(tabSpc.tabView, index, params);
	}

	/**
	 * 添加tab按键
	 */
	public void addTab(TabSpc tabSpc, boolean nul, int index) {
	    if (tabSpc == null || (TextUtils.isEmpty(tabSpc.name) && tabSpc.tabView == null)) {
            return;
        }
	    addTab(tabSpc, index, this);
		if (mFragmentMap.size() == 0) {
				lastIndex = 0;
				TextView curTx = (TextView)tabSpc.tabView.findViewById(R.id.tab_body);
		        if(curTx != null){
		            curTx.setCompoundDrawablesWithIntrinsicBounds(0, tabSpc.downIcon, 0, 0);
		            curTx.setTextColor(getContext().getResources().getColor(R.color.tab_selected));
		        }else {
		            tabSpc.tabView.setSelected(true);
		        }
		}
		mFragmentMap.put(index, tabSpc);
	}
	
	public void setCurrentTab(int index) {
	    if (index == lastIndex) {	        
            return;
        }
	    TabSpc tab = mFragmentMap.get(index);
	    if(tab != null) {
	        onClick(tab.tabView);
	    }
	}

	private void doTabChanged(int index) {
		if (index == lastIndex) {
			return;
		}
		TabSpc newTabSpc = mFragmentMap.get(index);
		TabSpc oldTabSpc = mFragmentMap.get(lastIndex);
		lastIndex = index;
		if (mTabSelectedListener != null) {
            mTabSelectedListener.onTabChanged(oldTabSpc.tag, newTabSpc.tag);
        }
	}

	@Override
	public void onClick(View v) {
		final int index = (Integer) v.getTag();
		if (index == lastIndex) {
            return;
        }
		
		TabSpc tab = mFragmentMap.get(index);
		TabSpc lastTab = mFragmentMap.get(lastIndex);
		
		TextView curTx = (TextView) v.findViewById(R.id.tab_body);
		if(curTx != null){
			curTx.setCompoundDrawablesWithIntrinsicBounds(0, tab.downIcon, 0, 0);
			curTx.setTextColor(getContext().getResources().getColor(R.color.tab_selected));
		}else {
		    tab.tabView.setSelected(true);
		}
        TextView preTx = (TextView) lastTab.tabView.findViewById(R.id.tab_body);
        if(preTx != null){
            preTx.setCompoundDrawablesWithIntrinsicBounds(0, lastTab.icon, 0, 0);
            preTx.setTextColor(getContext().getResources().getColor(R.color.white));
        }else {
            lastTab.tabView.setSelected(false);
        }
        viewPager.setCurrentItem(index, false);
        doTabChanged(index);
	}
	
    /**
     * 设置按键监听
     */
    public void setOnTabChangedListener(OnTabChangedListener listener) {
        mTabSelectedListener = listener;
    }
    
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }
	
	public TabSpc newTabSpec(String tag) {
	    return new TabSpc(null, 0, 0, 0, tag);
	}

	/**
	 * tab描述结构
	 */
	public static class TabSpc {
		private String name;
		private int icon;
		private int downIcon;
		private int background;
		
		private String tag;
		private View tabView;

		public TabSpc(String name, int icon, int downIcon, int background, String tag) {
			super();
			this.name = name == null ? tag : name;
			this.icon = icon;
			this.background = background;
			this.downIcon = downIcon;
			this.tag = tag;
		}
		
		public String getTag() {
		    return TextUtils.isEmpty(tag) ? (tag = ""+hashCode()) : tag;
		}
		
		public TabSpc setIndicator(View tabView) {
		    this.tabView = tabView;
		    return this;
		}
	}

    @Override
    public void onPageScrollStateChanged(int arg0) {
        if(mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrollStateChanged(arg0);
        }
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        if(mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrolled(arg0, arg1, arg2);
        }
    }

    @Override
    public void onPageSelected(int arg0) {
        setCurrentTab(arg0);
        if(mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageSelected(arg0);
        }
    }
}
