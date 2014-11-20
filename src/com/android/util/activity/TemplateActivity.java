package com.android.util.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import com.android.util.R;
import com.android.util.widget.LoadableView.IdisplayNetErrAndRefreshView;
import com.android.util.widget.TitleBar;

/*******************************************************
 * @author : zhaohua
 * @version: 2012-08-14
 * @see:     继承自这个Activity的界面有统一的标题栏
 * @Copyright: copyrights reserved by personal 2007-2012
*******************************************************/
public class TemplateActivity extends BaseActivity implements IdisplayNetErrAndRefreshView
{
	/**标题栏 */
	protected TitleBar mTitleBar;
	/**内容容器*/
	private LinearLayout mContainer;
	/** 底部栏*/
    protected LinearLayout mBottomBar;

	public void setContentView(View view)
	{
		initLayout(view);
	}
	
	@Override
	public void setContentView(int layoutResID)
	{
		View content = getLayoutInflater().inflate(layoutResID, null);
		initLayout(content);
	}

	private void initLayout(View content) {
		// 设置模板
		super.setContentView(R.layout.activity_template);
		// 初始化模拟成员
		initComponents();		
		// 将内容放入容器		
		mContainer.addView(content,new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		// 设置模板的标题
		setupTitleBar(mTitleBar);
		// 设置模板的底部栏
        setupBottomBar(mBottomBar);
	}
	
	/**
	 * 初始化三大块：头部的标题栏，中间的内容容器
	 */
	private void initComponents()
	{
	    mTitleBar = (TitleBar)findViewById(R.id.titleBar);	
	    mContainer = (LinearLayout)findViewById(R.id.container);
	    mBottomBar = (LinearLayout)findViewById(R.id.bottomBar);
	}
	
	/** 设置标题栏*/
	protected void setupTitleBar(TitleBar titleBar){}

	/** 设置底部栏*/
    protected void setupBottomBar(LinearLayout bottomBar){}
	
    @Override
    public void refreshView()
    {
        
    }

    @Override
    public void showNoNetworkDialog()
    {
        showDialog("网络设置提示", "网络连接不可用,是否进行设置?", "取消", "设置", 
                new DialogInterface.OnClickListener()
        {           
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
              startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }
        });
    }
}
