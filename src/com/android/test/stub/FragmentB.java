package com.android.test.stub;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.util.R;
import com.android.util.fragment.PagerFragment;

public class FragmentB extends PagerFragment implements OnClickListener {
    
    private View btn0,btn1,btn2;
    
    @Override
    protected int getLayoutId() {
        return R.layout.test_fragmentadapter;
    }
    
    protected String getLogTag() {
        return "FragmentB";
    }
    
    @Override
    protected void initViews() {
        super.initViews();
        
        btn0 = getViewById(R.id.btn0);
        btn1 = getViewById(R.id.btn1);
        btn2 = getViewById(R.id.btn2);
        
        btn0.setOnClickListener(this);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        getViewById(R.id.btn3).setOnClickListener(this);
        
        onClick(btn0);
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
        }
    }

    @Override
    public void onTabChanged(int id)
    {
        setTabFouces(id);
    }

    @Override
    public Fragment newFragment(int id)
    {
        switch(id) {
        case 0:
            return new FragmentB1();
        case 1:
            return new FragmentB2();
        case 2:
            return new FragmentB3();
        }
        return new FragmentB1();
    }

    @Override
    protected ViewPager getViewPager()
    {
        return (ViewPager)getViewById(R.id.viewpager1);
    }

    @Override
    protected int getPagerSize()
    {
        return 3;
    }
    
    private void setTabFouces(int id){
        btn0.setSelected(0 == id);
        btn1.setSelected(1 == id);
        btn2.setSelected(2 == id);
    }
}
