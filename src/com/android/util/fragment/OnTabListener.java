package com.android.util.fragment;

import android.support.v4.app.Fragment;

public interface OnTabListener {

    public void onTabSelected(int id);

    public void onTabChanged(int id);

    public Fragment newFragment(int id);
}
