package com.android.test.stub;

import java.util.HashMap;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.android.util.R;
import com.android.util.fragment.CUTabHostFragmentActivity;

public class TestTabHostActivity extends CUTabHostFragmentActivity{
    private HashMap<String, View> tabMap ;
    
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        
        View localView1 = newTabView(R.drawable.tab_icon_contact_selector);
        View localView2 = newTabView(R.drawable.tab_icon_conversation_selector);
        View localView3 = newTabView(R.drawable.tab_icon_leba_selector);
        View localView4 = newTabView(R.drawable.tab_icon_setting_selector);
        ((ImageView)localView1.findViewById(R.id.tab_item_selector_left)).setImageResource(R.drawable.tab_first_item_selector_left);
        ((ImageView)localView4.findViewById(R.id.tab_item_selector_right)).setImageResource(R.drawable.tab_last_item_selector_right);
        
        tabMap = new HashMap<String, View>();
        tabMap.put("消息", localView1);
        tabMap.put("联系人", localView2);
        tabMap.put("动态", localView3);
        tabMap.put("设置", localView4);
        
        addTab(FragmentA.class, localView1, 0);
        addTab(FragmentB.class, localView2, 1);
        addTab(FragmentC.class, localView3, 2);
        addTab(FragmentD.class, localView4, 3);
        
        setCurrentTab(0);
    }
}
