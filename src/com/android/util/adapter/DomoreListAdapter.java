package com.android.util.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.android.util.R;
import com.android.util.adapter.DataLoaderHandler.DataLoadedCallback;
import com.android.util.widget.LoadableView;

/*******************************************************
 * @author: zhaohua
 * @version: 2012-8-23
 * @see: 可以显示查看更多的一种ArrayListAdapter
 * @Copyright : copyrights reserved by personal 2007-2012
 * @param <T>
*******************************************************/
public abstract class DomoreListAdapter<T> 
        extends ArrayListAdapter<T>
        implements DataLoadedCallback<T>
{
    // /////////////////////////////////////////////////////////////////////////
    /** 数据载入handler */
    protected DataLoaderHandler<T> mDataLoaderHandler;
    private LoadableView mLoadingView;
    /** 数据缓冲列表*/
    private ArrayList<T> mCacheData;
    /** do more 显示下一页的数量,默认4项*/
    private int mPreNum = 5;
    /** 第一次载入显示标志*/
    private boolean mFirstShow = true;
    protected boolean isDoMoreMode = true;
    
    /**
     * 构造器
     * @param context--上下文
     * @param listView--list view
     * @param loadingViewResId--正在载入 layout id
     * @param handler--数据载入器
     * @param domoreViewResId--获得更多 layout id
     */
    public DomoreListAdapter(Context context, 
            ListView listView,DataLoaderHandler<T> handler)
    {
        super(context);
        mContext = context;
        mListView = listView;
        mDataLoaderHandler = handler;
        mLoadingView = new LoadableView(context);
        mCacheData = new ArrayList<T>();
        
        if(mListView.getHeaderViewsCount() == 0)
        {
        View line = new View(context);
        line.setVisibility(View.GONE);
        line.setLayoutParams(new ListView.LayoutParams(0, 0));
        mListView.addHeaderView(line);
        }
        mListView.addFooterView(mLoadingView);
        
        mLoadingView.setOnRetryClickListner(mClickLoadListener);
    }
    
    protected void setDataLoaderHandler(DataLoaderHandler<T> handler)
    {
        mDataLoaderHandler = handler;
    }
    
    /**
     * loading view show act
     * @param show--loading view show flag
     */
    public void showLoading()
    {       
        mLoadingView.showLoadingView();
    }
    
    /**
     * do more view show act
     * @param show -- do more view show flag
     */
    private void showDoMore()
    {        
        mLoadingView.showMoreView();
    }
    
    /**
     * load error view show act
     * @param show -- load error  view show flag
     */
    private void showLoadError()
    {       
        mLoadingView.showLoadingErrView(getCount()!=0);
    }
    
    /** check 是否还有数据可载入 */
    protected boolean isLoadable()
    {
        // 最大显示数量
        final int maxItems = mDataLoaderHandler.getMaxItems();
        // 当前list view载入的item的总数
        final int total = getCount();
        // 无数据时， 不显示 do more
        if(total == 0) return false;
        // 没有到达显示 上限时
        if(total < maxItems)
        {
            // 缓冲区是否 还有数据
            int size = mCacheData.size();
            if( size > 0)
            { 
                // 只要缓冲区还有数据就显示
                return true;
                /**
                if(size >= mPreNum)
                {
                    // 还有多余一屏的缓冲
                    return true;
                }else
                {
                    // 没有 则 由 DataLoaderHandler 判定
                    return mDataLoaderHandler.haveNext();
                }
                */
            }else
            {
                // 没有缓冲, 则 由 DataLoaderHandler 判定
                return mDataLoaderHandler.haveNext();
            }
        }else
        {
            // 到达显示 上限
            return false;
        }
    }
    
    /** 预先显示下一页，
     *  返回true则显示缓存数据，
     *  返回false则无缓存数据显示*/
    public void loadShowNext()
    {
        if(isLoadable())
        {
            if(mCacheData.size() > 0)
            {
                showNext();
            }else
            {
                if(mDataLoaderHandler.haveNext())
                {
                    showLoading();
                    mDataLoaderHandler.getNext(DomoreListAdapter.this);
                }
            }
        }
    }
    
    /** 数据到达时，载入数据 */
    public synchronized void onLoaded(List<T> values)
    {
        Log.d("###Data", "dataLoaded");
        if(values == null || values.size() == 0) 
        {
            // 当没有数据时,这里不做没结果的处理，由list view的同级处理
            mLoadingView.showLoadingErrView(R.string.no_result,getCount()!=0);
            return;
        }
        // 填充 缓冲
        mCacheData.addAll(values);
        // 显示 next
        showNext();
    }
    
    /** 载入出错*/
    public void onError()
    {
        showLoadError();
    }
    
    /**
     * 初始化,清空数据但是不加载数据
     */
    public void resetAdapterParam(){
    	 mLoadingView.hiddenView();
         // 重新设置
         mFirstShow = true;
         // 清空缓存数据
         mCacheData.clear();
         // 清空适配器数据
    }
    
    /** 清空数据，重新载入*/
    public void reload()
    {
        mLoadingView.hiddenView();
        // 重新设置
        mFirstShow = true;
        // 清空缓存数据
        mCacheData.clear();
        // 清空适配器数据
        removeAll();
    }
    
    /** 显示Next视图*/
    public void showNext()
    {
        // 第一次载入设置
        int showNum = mFirstShow ? 10 : mPreNum;
        if(mFirstShow)
        {
        	if(mList != null)
            {
                mList.clear();
            }
        }
        // 获取next的数据
        ArrayList<T> values = new ArrayList<T>(showNum);
        int index = mCacheData.size();
        if(index >= showNum)
        {
            index = showNum;
        }
        for(int i=0 ; i< index; ++i)
        {
            values.add(mCacheData.get(i));
        }
        // next数据 清除缓冲
        synchronized (mCacheData)
        {
            mCacheData.removeAll(values);
        }       
        // 数据到达后， waiter process remove
        mLoadingView.hiddenView();
        // add items to mList
        super.addItems(values);
        // 当do more时，check 是否还有数据可载入
        if(isDoMoreMode && isLoadable())
        {
            showDoMore();
        }
        
        mFirstShow = false;
    }
    
    private OnClickListener mClickLoadListener = new OnClickListener()
    {
        /** click 限制*/
        private boolean mClickable = true;
        
        public void onClick(View v)
        {
            synchronized (this)
            {
                if(!mClickable)
                {
                    return;
                }
                mClickable = false;
            }
            // do more --> waiter process
            showLoading();
            if(mCacheData.size() < mPreNum)
            {
                // 缓冲不足时
                // 如果 还有下一页数据
                if(mDataLoaderHandler.haveNext())
                {
                    // 获取下一数据
                    mDataLoaderHandler.getNext(DomoreListAdapter.this);
                }else
                {
                    // 显示 缓冲 剩余 数据
                    showNext();
                }
            }else
            {
                // 有缓冲时，show next
                showNext();
            }
            
            synchronized (this)
            {
                mClickable = true;
            }
        }
    };
}
