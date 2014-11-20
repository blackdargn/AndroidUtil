package com.android.util.widget;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.util.R;
import com.android.util.adapter.LoadableListAdapter.LoadView;

public class MyListLoadingView extends RelativeLayout implements LoadView<RelativeLayout>{
    
    private View pbProgress = null;
    private TextView tvMsg = null;

    public MyListLoadingView(Context context) {
        super(context);
        inflate(context, R.layout.view_listloading, this);
        pbProgress = findViewById(R.id.pbProgress);
        tvMsg = (TextView)findViewById(R.id.tvMsg);
    }
    
    @Override
    public View getView() {
        return this;
    }
    
    public void hide(){
        setVisibility(View.GONE);
    }
    
    public void showLoding(){
        setVisibility(View.VISIBLE);
        pbProgress.setVisibility(View.VISIBLE);
        tvMsg.setVisibility(View.GONE);
    }
    
    public void loadFinish(String msg){
        setVisibility(View.VISIBLE);
        pbProgress.setVisibility(View.GONE);
        tvMsg.setVisibility(View.VISIBLE);
        tvMsg.setText(msg);
    }  
}