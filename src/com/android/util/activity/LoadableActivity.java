/**
 * NearShop
 * LoadableActivity.java
 * com.android.nearshop.app.activity.common
 * 
 */
package com.android.util.activity;

import android.os.Bundle;

import com.android.util.R;
import com.android.util.widget.LoadableView;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-8-14
 * @see : 有载入提示功能的模板activity
 * @Copyright : copyrights reserved by personal 2007-2012
 **********************************************************/
public class LoadableActivity extends TemplateActivity
{
    /** 载入视图集合*/
    protected LoadableView mLoadingView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void setContentView(int layoutResID)
    {
        // 设置模板
        super.setContentView(R.layout.activity_loadable);
        // 初始化模拟成员
        mLoadingView = (LoadableView)findViewById(R.id.loadView);
        // 将内容放入容器
        mLoadingView.setMainView(layoutResID);
        
        mLoadingView.setActivity(this);
    }
}
