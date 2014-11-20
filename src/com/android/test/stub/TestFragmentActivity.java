package com.android.test.stub;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.util.R;
import com.android.util.fragment.PagerFragmentActivity;

public class TestFragmentActivity extends PagerFragmentActivity implements OnClickListener {

    private View btn0,btn1,btn2,btn3;
    
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        
        setContentView(R.layout.test_fragmentadapter);
        
        btn0 = getViewById(R.id.btn0);
        btn1 = getViewById(R.id.btn1);
        btn2 = getViewById(R.id.btn2);
        btn3 = getViewById(R.id.btn3); 
        
        onTabSelected(0);
    }
    
    @Override
    public void onTabChanged(int id) {
        setTabFouces(id);
    }
    
    @Override
    public Fragment newFragment(int id) {
        switch(id) {
        case 0:
            return new FragmentB1();
        case 1:
            return new FragmentB2();
        case 2:
            return new FragmentB3();
        case 3:
            return new FragmentB3();
        }
        return new FragmentB1();
    }

    @Override
    protected ViewPager getViewPager() {
        return (ViewPager)findViewById(R.id.viewpager1);
    }

    @Override
    public void onClick(View v) {
        
        switch(v.getId()) {
        case R.id.btn0:
            onTabSelected(0);
            setTabFouces(0);
            break;
        case R.id.btn1:
            onTabSelected(1);
            setTabFouces(1);
            break;
        case R.id.btn2:
            onTabSelected(2);
            setTabFouces(2);
            break;
        case R.id.btn3:
            onTabSelected(3);
            setTabFouces(3);
            break;
        }
    }
    
    private void setTabFouces(int id){
        btn0.setSelected(0 == id);
        btn1.setSelected(1 == id);
        btn2.setSelected(2 == id);
        btn3.setSelected(3 == id);
    }
}
