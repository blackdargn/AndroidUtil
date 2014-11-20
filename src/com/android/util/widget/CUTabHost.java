package com.android.util.widget;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

public class CUTabHost extends LinearLayout implements OnClickListener {
	private LayoutInflater mLayoutInflater;
	private OnTabChangedListener mTabSelectedListener;
	private FragmentManager mfm;
	private int containLayout;
	private SparseArray<TabSpc> mFragmentMap;
	private int lastIndex = 0;
	private boolean isAutoFull;
	private Boolean isHide = null;

	public CUTabHost(Context context, AttributeSet attrs) {
		super(context, attrs);
		mLayoutInflater = ((Activity) getContext()).getLayoutInflater();
		mFragmentMap = new SparseArray<TabSpc>();
	}

	/**
	 * 初始化tab host ,将activity用来放置Fragment的容器放入
	 * 
	 * @param contain
	 * @param fragmentTransaction
	 */
	public void setup(Context context, FragmentManager fragmentManager,int contain) {
		containLayout = contain;
		mfm = fragmentManager;
	}

	public void setIsHide(boolean ishide) {
	    if(isHide == null) {
	        isHide = ishide;
	    }
	}
	
	public boolean isHide() {
	    return isHide != null ? isHide : false;
	}
	
	public void destryTabHost() {
		mFragmentMap.clear();
		mFragmentMap = null;
	}

	/**
	 * 自动填满高度
	 */
	public void setAutoFull(boolean autoFull){
		isAutoFull = autoFull;
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
	    LayoutParams params = new LayoutParams(0, isAutoFull ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        
        addView(tabSpc.tabView, index, params);
	}

	/**
	 * 添加tab按键
	 * 
	 * @param name
	 * @param image
	 * @param background
	 */
	public void addTab(TabSpc tabSpc, Class<? extends Fragment> fragmentClass, int index) {
	    if (tabSpc == null || fragmentClass == null || (TextUtils.isEmpty(tabSpc.name) && tabSpc.tabView == null)) {
            return;
        }
	    addTab(tabSpc, index, this);
		if (mFragmentMap.size() == 0) {
			Fragment fragment = Fragment.instantiate(getContext(), fragmentClass.getName());
			if (fragment != null) {
				FragmentTransaction  ft = mfm.beginTransaction();
				tabSpc.mFragment = fragment;
				ft.add(containLayout, fragment, tabSpc.getTag());
				ft.commit();
				lastIndex = 0;
				TextView curTx = (TextView)tabSpc.tabView.findViewById(R.id.tab_body);
		        if(curTx != null){
		            curTx.setCompoundDrawablesWithIntrinsicBounds(0, tabSpc.downIcon, 0, 0);
		            curTx.setTextColor(getContext().getResources().getColor(R.color.tab_selected));
		        }else {
		            tabSpc.tabView.setSelected(true);
		        }
			}
		}
		tabSpc.classT = fragmentClass;
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

		FragmentTransaction ft = mfm.beginTransaction();

		if (oldTabSpc.mFragment != null) {
		    if(isHide()) {		        
		        ft.hide(oldTabSpc.mFragment);
		    }else {
		        ft.detach(oldTabSpc.mFragment);
		    }
		}
		if (newTabSpc.mFragment == null) {
			newTabSpc.mFragment = Fragment.instantiate(getContext(), newTabSpc.classT.getName());
			ft.add(containLayout, newTabSpc.mFragment,newTabSpc.getTag());
		} else {
		    if(isHide()) {
		        ft.show(newTabSpc.mFragment);
		    }else {
		        ft.attach(newTabSpc.mFragment);
		    }
		}
		ft.commit();
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
        
        if(lastTab.mFragment instanceof OnTabPrePauseListener && lastTab.mFragment.isResumed()) {
            ( (OnTabPrePauseListener)lastTab.mFragment ).onTabPrePause();
            lastTab.tabView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doTabChanged(index);
                }
            }, 500);
         }else {
             doTabChanged(index);
         }
	}
	
    /**
     * 设置按键监听
     */
    public void setOnTabChangedListener(OnTabChangedListener listener) {
        mTabSelectedListener = listener;
    }
	
	public interface OnTabPrePauseListener{
	    void onTabPrePause();
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
		
		private Class<? extends Fragment> classT;
		private Fragment mFragment;
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
		
		private String getTag() {
		    return TextUtils.isEmpty(tag) ? (tag = ""+hashCode()) : tag;
		}
		
		public TabSpc setIndicator(View tabView) {
		    this.tabView = tabView;
		    return this;
		}
	}
}
