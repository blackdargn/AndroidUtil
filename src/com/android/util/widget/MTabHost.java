package com.android.util.widget;

import android.content.Context;
import android.util.AttributeSet;

public class MTabHost extends FragmentTabHost {
    private OnTabSelectionListener onTabSelectionListener;

    public MTabHost(Context paramContext) {
        super(paramContext);
    }

    public MTabHost(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
    }

    public void setCurrentTab(int paramInt) {
        int i = getTabWidget().getTabCount();
        if ((paramInt < 0) || (paramInt >= i)) {
            return;
        }
        int j = getCurrentTab();
        super.setCurrentTab(paramInt);
        if (this.onTabSelectionListener != null) {
            this.onTabSelectionListener.onTabSelect(j, paramInt, this);
        }
    }

    public void setOnTabSelectionListener(OnTabSelectionListener paramOnTabSelectionListener) {
        this.onTabSelectionListener = paramOnTabSelectionListener;
    }

    public abstract interface OnTabSelectionListener {
        public abstract void onTabSelect(int oldTab, int newTab, MTabHost tabHost);
    }
}