package com.android.util.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

public  abstract class LoadableListAdapter<T> extends AlpaIndexListAdapter<T> 
implements AbsListView.OnScrollListener{
    
    private LoadView mLoadView;
    private OnRefreshListener mPullListener;
    private boolean pullMode = true;
    private boolean pullable = true;
    
    public LoadableListAdapter(Context context)
    {
        this(context, null, null, null, false);
    }
    
    public LoadableListAdapter(Context context, ListView listView, LoadView  loadView, OnRefreshListener listener, boolean pullup) {
        super(context);
        mListView = listView;
        mLoadView = loadView;
        mPullListener = listener;
        pullMode = pullup;
        
        if(mListView != null) {
            if(loadView != null) {
                if(pullup) {
                    if(mListView.getHeaderViewsCount() == 0)
                    {
                        View line = new View(context);
                        line.setVisibility(View.GONE);
                        line.setLayoutParams(new ListView.LayoutParams(0, 0));
                        mListView.addHeaderView(line);
                    }
                    mListView.addFooterView(loadView.getView());
                }else {
                    mListView.addHeaderView(loadView.getView());
                }
                loadView.hide();
            }
            if(listener != null) {
                mListView.setOnScrollListener(this);
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // 不是list view时也无效
        if( mListView == null) return;
        // 下拉触发时，有效
        switch (scrollState)
        {
        case SCROLL_STATE_IDLE:
            {
                 
                if(!pullable) return;
                // list view中可见的地item的位置
                final int firstVisibleItem = mListView.getFirstVisiblePosition() - 1;
                // list view一屏幕可显示item的数量
                final int visibleItemCount = mListView.getChildCount();
                // 当前list view载入的item的总数
                final int totalItemCount = getCount();
                
                if(pullMode) {
                    if(firstVisibleItem + visibleItemCount < totalItemCount - 1) {
                        return;
                    }else {
                        if(mPullListener != null) {
                            if(mPullListener.isLoading()) {
                                return;
                            }else {
                                if(mLoadView != null) {
                                    mLoadView.showLoding();
                                }
                                mPullListener.onPullUpToRefresh();
                            }
                        }
                    }
                }else {
                    if(firstVisibleItem > 0) {
                        return;
                    }else {
                        if(mPullListener != null) {
                            if(mPullListener.isLoading()) {
                                return;
                            }else {
                                if(mLoadView != null) {
                                    mLoadView.showLoding();
                                }
                                mPullListener.onPullDownToRefresh();
                            }
                        }
                    }
                }
                break;
            }
        }
    }
    
    public void onRefreshComplete() {
        if(mLoadView != null) {
            mLoadView.hide();
        }
    }
    
    public void setPullable(boolean pullable) {
        this.pullable = pullable;
    }
    
    public void setNoDate(String msg) {
        if(mLoadView != null) {
            mLoadView.loadFinish(msg);
        }
    }
    
    public void preLoding() {
        if(mLoadView != null) {
            mLoadView.showLoding();
        }
    }
    
    @Override
    public void setList(List<T> list) {
        super.setList(list);
        onRefreshComplete();
    }
    
    @Override
    public void addItems(List<T> list) {
        super.addItems(list);
        onRefreshComplete();
    }
    
    public static interface OnRefreshListener{
        public void onPullDownToRefresh();
        public void onPullUpToRefresh();
        public boolean isLoading();
    }
    
    public interface LoadView<V extends View>{       
        public View getView();
        public void hide();
        public void showLoding();
        public void loadFinish(String text);
    } 
}