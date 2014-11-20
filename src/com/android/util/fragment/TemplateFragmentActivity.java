package com.android.util.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import com.android.util.R;
import com.android.util.widget.TitleBar;

public abstract class TemplateFragmentActivity extends BaseFragmentActivity
{
	protected TitleBar titleBar;
	protected LinearLayout bottomBar;
	protected ViewGroup content;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	}
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	}
	   
	@Override
	public void setContentView(int layoutResID)
	{
		super.setContentView(R.layout.activity_template);
		titleBar = getViewById(R.id.titleBar);
		bottomBar = getViewById(R.id.bottomBar);
		content = getViewById(R.id.container);

        if(layoutResID > 0) {
            View view = getLayoutInflater().inflate(layoutResID, null);
            content.addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
		setupTitleBar(titleBar);
		setupBottomBar();
	}
	
	protected void setupTitleBar(TitleBar titleBar)
	{
		
	}
	
	protected void setupBottomBar()
	{
		
	}
}