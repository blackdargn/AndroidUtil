package com.android.util.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.util.R;
import com.android.util.system.Logger;
import com.android.util.widget.CULinePageIndiactor;

public abstract class LineTabPagerFragment extends BaseFragment implements OnTabChangedListener{

    protected ViewPager mViewPager;
    protected CULinePageIndiactor mTabIndactor;
    private   LinearLayout mExtTab;
    protected MyFragmentPagerAdpater mPageAdpter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_linetabpager;
    }
    
    protected View newTabView(int paramInt) {
        View localView = inflate(R.layout.view_tab_item);
        ((ImageView) localView.findViewById(R.id.tab_item_image)).setBackgroundResource(paramInt);
        return localView;
    }
    
    protected View newTabView(String paramInt) {
        View localView = inflate(R.layout.view_text_tab);
        ((TextView) localView.findViewById(R.id.text)).setText(paramInt);
        return localView;
    }

    protected void addTab(Class<? extends Fragment> tag, View tabView, String id, int index ) {
        mTabIndactor.getCUPagerHost().addTab(mTabIndactor.getCUPagerHost().newTabSpec(id).setIndicator(tabView), false, index);
        mPageAdpter.addFragment(Fragment.instantiate(getActivity(), tag.getName(), null));
    }
    
    protected void addExtTab(View tabView,OnClickListener listener) {
        mExtTab.addView(tabView,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT));
        tabView.setOnClickListener(listener);
    }

    public void setTabBackground(int bgRes){
        getViewById(R.id.tab_lay).setBackgroundResource(bgRes);
    }
    
    @Override
    protected void initViews() {
        super.initViews();
        mViewPager = getViewById(R.id.viewPager);
        mTabIndactor = getViewById(android.R.id.tabhost);
        mExtTab = getViewById(R.id.ext_tab);
        mPageAdpter = new MyFragmentPagerAdpater(getChildFragmentManager());
        mViewPager.setAdapter(mPageAdpter);
        mTabIndactor.setViewPager(mViewPager);
        mTabIndactor.getCUPagerHost().setOnTabChangedListener(this);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Logger.d("--->onResume");
        updateCurFragment();
    }
    
    @Override
    public void onTabChanged(String oldTabId, String newTabid) {
        updateCurFragment();
    }
    
    public void setCurrentTab(int index) {
        mTabIndactor.setCurrentTab(index);
    }
  
    protected void updateCurFragment(){
        mPageAdpter.updateFragment(-1, mViewPager.getCurrentItem());
    }
}
