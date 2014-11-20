package com.android.util.fragment;

import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.android.util.system.Logger;

public abstract class PagerFragment extends BaseFragment implements OnPageChangeListener,OnTabListener{

    protected ViewPager mViewPager;
    protected MyFragmentPagerAdpater mPageAdpter;
    private int mCurIndex = -1;

    protected abstract ViewPager getViewPager();
    
    protected abstract int getPagerSize();
    
    @Override
    protected void initViews() {
        super.initViews();
        mViewPager = getViewPager();
        mPageAdpter = new MyFragmentPagerAdpater(getChildFragmentManager(), getPagerSize(), this);
        mViewPager.setAdapter(mPageAdpter);
        mViewPager.setOnPageChangeListener(this);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Logger.d("--->onResume");
        updateCurFragment();
    }
    
    @Override
    public void onTabSelected(int id) {
        if(mCurIndex == id) return;
        
        mPageAdpter.updateFragment(mCurIndex,id);
        mCurIndex = id;
        mViewPager.setCurrentItem(id);
    }
    
    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {}
    
    @Override
    public void onPageScrollStateChanged(int arg0) { }
    
    @Override
    public void onPageSelected(final int index) {
        Logger.d("-->onPageSelected :" + index);
        if(mCurIndex == index) return;
        
        mPageAdpter.updateFragment(mCurIndex == index ? -1 : mCurIndex, index);
        mCurIndex = index;
        onTabChanged(index);
    }
    
    protected void updateCurFragment(){
        mPageAdpter.updateFragment(-1, mViewPager.getCurrentItem());
    }
}
