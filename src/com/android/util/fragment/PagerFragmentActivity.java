package com.android.util.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;

import com.android.util.system.Logger;

public abstract class PagerFragmentActivity extends FragmentActivity 
implements OnPageChangeListener,OnTabListener{

    private ViewPager mViewPager;
    protected MyFragmentPagerAdpater mPageAdpter;
    private int mCurIndex = -1;
    
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Logger.d("--->onResume");
        updateCurFragment();
    }
    
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mViewPager = getViewPager();
        mPageAdpter = new MyFragmentPagerAdpater(getSupportFragmentManager(), 4, this);
        mViewPager.setAdapter(mPageAdpter);
        mViewPager.setOnPageChangeListener(this);
    }
    
    protected abstract ViewPager getViewPager();
    
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
    
    @SuppressWarnings("unchecked")
    protected <T extends View> T getViewById(int id)
    {
        View view = findViewById(id);
        return (T)view;
    }
}
