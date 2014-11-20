package com.android.util.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;

import com.android.util.R;
import com.android.util.system.Logger;
import com.android.util.widget.CUTabHost;

public class CUTabHostFragmentActivity extends FragmentActivity implements OnTabChangedListener {

    private CUTabHost mTabhost;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_cutabhost);
    }

    protected View newTabView(int paramInt) {
        View localView = getLayoutInflater().inflate(R.layout.view_tab_item, null);
        ((ImageView) localView.findViewById(R.id.tab_item_image)).setBackgroundResource(paramInt);
        return localView;
    }
    
    protected void addTab(Class<? extends Fragment> tag, View tabView, int index) {
        initTabHost();
        mTabhost.addTab(mTabhost.newTabSpec(tag.getName()).setIndicator(tabView), tag, index);
    }

    private void initTabHost() {
        if (mTabhost == null) {
            mTabhost = (CUTabHost) findViewById(android.R.id.tabhost);
            mTabhost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
            mTabhost.setOnTabChangedListener(this);
            mTabhost.setIsHide(true);
        }
    }

    protected void setCurrentTab(int index) {
        mTabhost.setCurrentTab(index);
    }

    @Override
    public void onTabChanged(String odlTabId, String newTabId) {
        Logger.d("--->onTabChanged : " + odlTabId + "-->" + newTabId);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(odlTabId);
        if(fragment != null && fragment instanceof BaseFragment) {
            ((BaseFragment)fragment).onPause();
        }
        fragment = getSupportFragmentManager().findFragmentByTag(newTabId);
        if(fragment != null && fragment instanceof BaseFragment) {
            ((BaseFragment)fragment)._onResume();
        }
    }
}
