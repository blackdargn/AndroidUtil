package com.android.test.stub;

import android.view.View;

import com.android.util.R;
import com.android.util.fragment.LineTabPagerFragment;

public class FragmentD extends LineTabPagerFragment{
    
    protected String getLogTag() {
        return "FragmentD";
    }

    @Override
    protected void initViews() {
        super.initViews();
        
        View localView1 = newTabView(R.drawable.tab_icon_contact_selector);
        View localView2 = newTabView(R.drawable.tab_icon_conversation_selector);
        View localView3 = newTabView(R.drawable.tab_icon_leba_selector);
        
        addTab(FragmentB1.class, localView1,"1", 0);
        addTab(FragmentB2.class, localView2,"2", 1);
        addTab(FragmentB3.class, localView3,"3", 2);
        
        setCurrentTab(0);
    }
    
}
