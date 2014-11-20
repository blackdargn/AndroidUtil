package com.android.util.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.util.R;
import com.android.util.system.MyApplication;
import com.android.util.system.Util;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-8-24
 * @see : 可载入视图集，包括正在载入，载入出错，载入无结果，以及主视图
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class LoadableView extends LinearLayout
{  
    private Button mButton;
    private Button mButton_0;
    private View mLoadingErrView;
    private View mLoadingErrView_0;
    private View mLoadingView;
    private View mLoadMoreView;
    
    private LinearLayout mMainView;
    private int mMainLayoutId;
    private TextView mTextView;
    private TextView mTextView_0;
    private TextView mMoreTextView;
    
    private Activity mAct;
    private View.OnClickListener reTrybuttonClickListener;
    
    public LoadableView(Context context)
    {
        super(context);
        initComponents();
    }
    
    public LoadableView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        TypedArray styledAttrs = context.obtainStyledAttributes(attrs,R.styleable.LoadableView);
        mMainLayoutId = styledAttrs.getResourceId(R.styleable.LoadableView_layout, 0);
        styledAttrs.recycle();
        initComponents();
    }
    
    // 初始化组件
    private void initComponents()
    {
        View row = inflate(getContext(), R.layout.view_loadable, null);
        addView(row,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
        
        this.mLoadingView = findViewById(R.id.base_loading);
        this.mLoadingErrView = findViewById(R.id.base_loading_err);
        this.mLoadMoreView = findViewById(R.id.base_load_more);
        this.mLoadingErrView_0 = findViewById(R.id.base_loading_err_0);
        
        this.mMainView = (LinearLayout)findViewById(R.id.main);
        this.mButton = ((Button)findViewById(R.id.btn01));
        this.mButton_0 = ((Button)findViewById(R.id.btn01_0));
        this.mTextView = ((TextView)findViewById(R.id.tv_info));
        this.mTextView_0 = ((TextView)findViewById(R.id.tv_info_0));
        this.mMoreTextView = ((TextView)findViewById(R.id.tv_more_info));
        
        if(mMainLayoutId > 0)
        {
            setMainView(mMainLayoutId);
        }
        // 默认为接口上的重试监听器
        this.reTrybuttonClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onRetryClick();
            }
        };
    }
    
    /** 设置重试监听器*/
    public void setOnRetryClickListner(OnClickListener listner)
    {
        reTrybuttonClickListener = listner;
    }
    
    /** 设置activity*/
    public void setActivity(Activity act)
    {
        this.mAct = act;
    }
    
    /** 设置主视图*/
    public void setMainView(int layRid)
    {
        View main = inflate(getContext(), layRid, null);
        setMainView(main);
    }
    
    /** 设置主视图*/
    public void setMainView(View main)
    {
        mMainView.removeAllViews();
        mMainView.addView(main,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
    }
    
    /** 显示网络错误
     * @param nextable true:分页； false：整页
     * */
    public void showNetErrView(boolean nextable)
    {
        showLoadingErrView(R.string.not_network,nextable);
    }
    /** 显示载入错误
     * @param nextable true:分页； false：整页
     */
    public void showLoadingErrView(boolean nextable)
    {
        showLoadingErrView(R.string.loading_err,nextable);
    }
    /** 显示指定错误
     * @param nextable true:分页； false：整页
     */
    public void showLoadingErrView(int errStrId,boolean nextable)
    {
        showLoadingErrView(getContext().getString(errStrId),nextable);
    }
    /** 显示指定错误
     * @param nextable true:分页； false：整页
     * */
    public void showLoadingErrView(String errStr,boolean nextable)
    {
        if(nextable)
        {
            this.mTextView.setText(errStr);
        }else
        {
            this.mTextView_0.setText(errStr);
        }
        showErrView(nextable);
    }
    /** 显示更多*/
    public void showLoadMoreView(int moreStrId)
    {
        mMoreTextView.setText(moreStrId);
        showMoreView();
    }
    /** 显示正在载入视图*/
    public void showLoadingView()
    {
        this.mLoadingView.setVisibility(View.VISIBLE);
        this.mLoadingErrView.setVisibility(View.GONE);
        this.mLoadingErrView_0.setVisibility(View.GONE);
        this.mLoadMoreView.setVisibility(View.GONE);
        this.mMainView.setVisibility(View.INVISIBLE);       
    }
    /** 载入完成后显示主视图*/
    public void showMainView()
    {
        this.mLoadingView.setVisibility(View.GONE);
        this.mLoadingErrView.setVisibility(View.GONE);
        this.mLoadingErrView_0.setVisibility(View.GONE);
        this.mLoadMoreView.setVisibility(View.GONE);
        this.mMainView.setVisibility(View.VISIBLE);
    }
    /** 显示获取更多的视图*/
    public void showMoreView()
    {
        this.mLoadingView.setVisibility(View.GONE);
        this.mLoadingErrView.setVisibility(View.GONE);
        this.mLoadingErrView_0.setVisibility(View.GONE);
        this.mMainView.setVisibility(View.GONE);
        this.mLoadMoreView.setVisibility(View.VISIBLE);
        this.mLoadMoreView.setOnClickListener(reTrybuttonClickListener);
    }
    /** 隐藏所有视图*/
    public void hiddenView()
    {
        this.mLoadingView.setVisibility(View.GONE);
        this.mLoadingErrView.setVisibility(View.GONE);
        this.mLoadingErrView_0.setVisibility(View.GONE);
        this.mMainView.setVisibility(View.GONE);
        this.mLoadMoreView.setVisibility(View.GONE);
    }
    /** 显示错误重新刷新视图
     * @param nextable true:分页； false：整页
     * */
    private void showErrView(boolean nextable)
    {
        if(nextable)
        {
            this.mButton.setOnClickListener(this.reTrybuttonClickListener);
            this.mLoadingErrView.setVisibility(View.VISIBLE);
            this.mLoadingErrView_0.setVisibility(View.GONE);
        }else
        {
            this.mButton_0.setOnClickListener(this.reTrybuttonClickListener);
            this.mLoadingErrView.setVisibility(View.GONE);
            this.mLoadingErrView_0.setVisibility(View.VISIBLE);
        }
        this.mLoadingView.setVisibility(View.GONE);
        this.mLoadMoreView.setVisibility(View.GONE);        
        this.mMainView.setVisibility(View.INVISIBLE);
    }

    /** 重新刷新监听器*/
    private void onRetryClick()
    {
        if(mAct == null)
        {
            return;
        }
        
        if (!MyApplication.getContext().isNetWorkAvailable())
        {
            Util.showToast(mAct, R.string.not_network);
            if ((mAct.getParent() != null)
                 && (mAct.getParent().getParent() != null)
                 && ((mAct.getParent().getParent() instanceof IdisplayNetErrAndRefreshView)))
            {
                ((IdisplayNetErrAndRefreshView) mAct.getParent().getParent()).showNoNetworkDialog();
            }
            if ((mAct.getParent() != null)
                 && ((mAct.getParent() instanceof IdisplayNetErrAndRefreshView)))
            {
                ((IdisplayNetErrAndRefreshView) mAct.getParent()).showNoNetworkDialog();
            }
            if ((mAct instanceof IdisplayNetErrAndRefreshView))
            {
                ((IdisplayNetErrAndRefreshView) mAct).showNoNetworkDialog();
            }
        } else
        {
            if ((mAct instanceof IdisplayNetErrAndRefreshView))
            {
                ((IdisplayNetErrAndRefreshView) mAct).refreshView();
            }
        }
    }
    
    /** 刷新视图 和 显示 网络异常窗口接口 */
    public static interface IdisplayNetErrAndRefreshView
    {
        public void refreshView();

        public void showNoNetworkDialog();
    }
}
