package com.android.util.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import com.android.util.R;
import com.android.util.system.Logger;
import com.android.util.widget.CUPagerHost;

public abstract class TabPagerFragment extends BaseFragment implements OnTabChangedListener{

    protected ViewPager mViewPager;
    protected CUPagerHost mTabhost;
    protected MyFragmentPagerAdpater mPageAdpter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_tabpager;
    }
    
    protected View newTabView(int paramInt) {
        View localView = inflate(R.layout.view_tab_item);
        ((ImageView) localView.findViewById(R.id.tab_item_image)).setBackgroundResource(paramInt);
        return localView;
    }

    protected void addTab(Class<? extends Fragment> tag, View tabView, String id, int index ) {
        mTabhost.addTab(mTabhost.newTabSpec(id).setIndicator(tabView), false, index);
        mPageAdpter.addFragment(Fragment.instantiate(getActivity(), tag.getName(), null));
    }
    
    @Override
    protected void initViews() {
        super.initViews();
        mViewPager = getViewById(R.id.viewPager);
        mTabhost = getViewById(android.R.id.tabhost);
        mPageAdpter = new MyFragmentPagerAdpater(getChildFragmentManager());
        mViewPager.setAdapter(mPageAdpter);
        mTabhost.setViewPager(mViewPager);
        mTabhost.setOnTabChangedListener(this);
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
  
    protected void updateCurFragment(){
        mPageAdpter.updateFragment(-1, mViewPager.getCurrentItem());
    }
}
