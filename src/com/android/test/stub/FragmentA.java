package com.android.test.stub;

import java.util.HashMap;

import android.view.View;

import com.android.util.R;
import com.android.util.fragment.CUTabHostFragment;

public class FragmentA extends CUTabHostFragment {

    private HashMap<String, View> tabMap;
    
    @Override
    protected int getLayoutId() {
        return R.layout.activity_cutabhost_top;
    }
 
    protected String getLogTag() {
        return "FragmentA";
    }
    
    @Override
    protected void initViews() {
        super.initViews();
        
        View localView1 = newTabView(R.drawable.tab_icon_contact_selector);
        View localView2 = newTabView(R.drawable.tab_icon_conversation_selector);
        View localView3 = newTabView(R.drawable.tab_icon_leba_selector);
        
        tabMap = new HashMap<String, View>();
        tabMap.put("1", localView1);
        tabMap.put("2", localView2);
        tabMap.put("3", localView3);
        
        addTab(FragmentB.class, localView1,"1", 0);
        addTab(FragmentC.class, localView2,"2", 1);
        addTab(FragmentD.class, localView3,"3", 2);
    }
}
