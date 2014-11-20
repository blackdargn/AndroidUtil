package com.android.util.fragment;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;

import com.android.util.R;
import com.android.util.system.Logger;
import com.android.util.widget.CUTabHost;

public class CUTabHostFragment extends BaseFragment implements OnTabChangedListener{
    private CUTabHost mTabhost;
    
    @Override
    protected int getLayoutId() {
        return R.layout.activity_cutabhost_top;
    }
    
    protected View newTabView(int paramInt) {
        View localView = inflate(R.layout.view_tab_item);
        ((ImageView) localView.findViewById(R.id.tab_item_image)).setBackgroundResource(paramInt);
        return localView;
    }

    protected void addTab(Class<? extends Fragment> tag, View tabView, String id, int index) {
        initTabHost();
        mTabhost.addTab(mTabhost.newTabSpec(id).setIndicator(tabView), tag, index);
    }

    private void initTabHost() {
        if (mTabhost == null) {
            mTabhost = (CUTabHost) getViewById(android.R.id.tabhost);
            mTabhost.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);
            mTabhost.setOnTabChangedListener(this);
            mTabhost.setIsHide(false);
        }
    }

    protected void setCurrentTab(int index) {
        mTabhost.setCurrentTab(index);
    }
    
    @Override
    public void onTabChanged(String odlTabId, String newTabId) {
        Logger.d("--->onTabChanged : " + odlTabId + "-->" + newTabId);
        Fragment fragment = getChildFragmentManager().findFragmentByTag(odlTabId);
        if(fragment != null && fragment instanceof BaseFragment) {
            ((BaseFragment)fragment).onPause();
        }
        fragment = getChildFragmentManager().findFragmentByTag(newTabId);
        if(fragment != null && fragment instanceof BaseFragment) {
            ((BaseFragment)fragment)._onResume();
        }
    }
}
