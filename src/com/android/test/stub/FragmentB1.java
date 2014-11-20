package com.android.test.stub;

import android.widget.TextView;

import com.android.util.R;
import com.android.util.fragment.BaseFragment;

public class FragmentB1 extends BaseFragment {

    private TextView tx_show;
    
    public FragmentB1() {}
    
    @Override
    protected int getLayoutId() {
        return R.layout.test_fragment;
    }
    
    @Override
    public void _onResume() {
        super._onResume();
        tx_show.setText(this.toString() + " showing..." + count++);
    }
    
    protected String getLogTag() {
        return "FragmentB1";
    }
    
    @Override
    protected void initViews() {
        super.initViews();
        
        tx_show = getViewById(R.id.tx_show);
    }
}
