package com.android.util.fragment;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MyFragmentPagerAdpater extends FragmentPagerAdapter {

    private ArrayList<Fragment> mFragments;
    
    public MyFragmentPagerAdpater(FragmentManager fm, int size, OnTabListener listener) {
        super(fm);
        mFragments = new ArrayList<Fragment>(size);
        for(int i =0; i<size; i++) {
            addFragment(listener.newFragment(i));
        }
    }
    
    public MyFragmentPagerAdpater(FragmentManager fm) {
        super(fm);
        mFragments = new ArrayList<Fragment>();
    }
    
    public void addFragment(Fragment item) {
        if(item instanceof BaseFragment) {
            ((BaseFragment)item).isHostTab = false;
        }
        mFragments.add(item);
        notifyDataSetChanged();
    }
    
    public void updateFragment(int oldId, int newId) {
        if(newId >= 0 &&  newId < mFragments.size()) {
            Fragment item = getItem(newId);
            if(item instanceof BaseFragment) {
                BaseFragment fragment = (BaseFragment)item;
                if(fragment.isVisible()) {
                    fragment._onResume();
                }else {
                    fragment.checkDelayResume();
                }
            }
        }
        if(oldId >= 0 &&  oldId < mFragments.size()) {
            Fragment item = getItem(oldId);
            if(item instanceof BaseFragment) {
                BaseFragment fragment = (BaseFragment)item;
                fragment.onPause();
            }
        }
    }
    
    @Override
    public Fragment getItem(int index) {
        if(index < 0 || index >= mFragments.size()) return null;
        return mFragments.get(index);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}