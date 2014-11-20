package com.android.util.widget;

import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.util.R;

public class PullToRefreshListView extends ListView
{
  // 完成 或者  初始化
  private static final int DONE = 3;
  // 向下拉
  private static final int PULL_To_REFRESH = 1;
  // 正在更新
  private static final int REFRESHING = 2;
  // 释放更新
  private static final int RELEASE_To_REFRESH = 0;
  
  private RotateAnimation animation;
  private ImageView arrowImageView;
  private int firstItemIndex;
  private int headContentHeight;
  private int headContentWidth;
  private LinearLayout headView;
  private LayoutInflater inflater;
  /** */
  private boolean isBack;
  /** */
  private boolean isRecored;
  private boolean isRefreshable;
  private TextView lastUpdatedTextView;
  private ProgressBar progressBar;
  private OnRefreshListener refreshListener;
  private RotateAnimation reverseAnimation;
  private int startY;
  private int state;
  private TextView tipsTextview;

  public PullToRefreshListView(Context paramContext)
  {
    super(paramContext);
    init(paramContext);
  }

  public PullToRefreshListView(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    init(paramContext);
  }

  private void changeHeaderViewByState()
  {
    switch (this.state)
    {
    case RELEASE_To_REFRESH:
    {
        this.arrowImageView.setVisibility(View.VISIBLE);
        this.progressBar.setVisibility(View.GONE);
        this.tipsTextview.setVisibility(View.VISIBLE);
        this.lastUpdatedTextView.setVisibility(View.VISIBLE);
        this.arrowImageView.clearAnimation();
        this.arrowImageView.startAnimation(this.animation);
        this.tipsTextview.setText(getContext().getString(R.string.pull_to_list_loosen_refresh));
        break;
    }
    case PULL_To_REFRESH:
    {
        this.progressBar.setVisibility(View.GONE);
        this.tipsTextview.setVisibility(View.VISIBLE);
        this.lastUpdatedTextView.setVisibility(View.VISIBLE);
        this.arrowImageView.clearAnimation();
        this.arrowImageView.setVisibility(View.VISIBLE);
        if (this.isBack)
        {
          this.isBack = false;
          this.arrowImageView.clearAnimation();
          this.arrowImageView.startAnimation(this.reverseAnimation);
          this.tipsTextview.setText(getContext().getString(R.string.pull_to_list_pull_refresh));
        }else
        {
            this.tipsTextview.setText(getContext().getString(R.string.pull_to_list_pull_refresh));
        }
        break;
    }
    case REFRESHING:
    {
        this.headView.setPadding(0, 0, 0, 0);
        this.progressBar.setVisibility(View.VISIBLE);
        this.arrowImageView.clearAnimation();
        this.arrowImageView.setVisibility(View.GONE);
        this.tipsTextview.setText(getContext().getString(R.string.pull_to_list_last_update));
        this.lastUpdatedTextView.setVisibility(View.VISIBLE);
        break;
    }
    case DONE:
    {
        this.headView.setPadding(0, -1 * this.headContentHeight, 0, 0);
        this.progressBar.setVisibility(View.GONE);
        this.arrowImageView.clearAnimation();
        this.arrowImageView.setImageResource(R.drawable.ic_pulltorefresh_arrow);
        this.tipsTextview.setText(getContext().getString(R.string.pull_to_list_pull_refresh));
        this.lastUpdatedTextView.setVisibility(View.VISIBLE);
        break;
    }
    }
  }

  private void init(Context paramContext)
  {
    setCacheColorHint(paramContext.getResources().getColor(android.R.color.transparent));
    this.inflater = LayoutInflater.from(paramContext);
    this.headView = ((LinearLayout)this.inflater.inflate(R.layout.pull_to_refresh_header, null));
    this.arrowImageView = ((ImageView)this.headView.findViewById(R.id.head_arrowImageView));
    this.arrowImageView.setMinimumWidth(70);
    this.arrowImageView.setMinimumHeight(50);
    this.progressBar = ((ProgressBar)this.headView.findViewById(R.id.head_progressBar));
    this.tipsTextview = ((TextView)this.headView.findViewById(R.id.head_tipsTextView));
    this.lastUpdatedTextView = ((TextView)this.headView.findViewById(R.id.head_lastUpdatedTextView));
    measureView(this.headView);
    this.headContentHeight = this.headView.getMeasuredHeight();
    this.headContentWidth = this.headView.getMeasuredWidth();
    this.headView.setPadding(0, -1 * this.headContentHeight, 0, 0);
    this.headView.invalidate();
    Log.v("size", "width:" + this.headContentWidth + " height:" + this.headContentHeight);
    addHeaderView(this.headView, null, false);
    this.animation = new RotateAnimation(0.0F, -180.0F, 1, 0.5F, 1, 0.5F);
    this.animation.setInterpolator(new LinearInterpolator());
    this.animation.setDuration(250L);
    this.animation.setFillAfter(true);
    this.reverseAnimation = new RotateAnimation(-180.0F, 0.0F, 1, 0.5F, 1, 0.5F);
    this.reverseAnimation.setInterpolator(new LinearInterpolator());
    this.reverseAnimation.setDuration(200L);
    this.reverseAnimation.setFillAfter(true);
    this.state = DONE;
    this.isRefreshable = false;
  }

  private void measureView(View child)
  {
      ViewGroup.LayoutParams p = child.getLayoutParams();
      if (p == null) 
      {
          p = new ViewGroup.LayoutParams(
                  ViewGroup.LayoutParams.FILL_PARENT,
                  ViewGroup.LayoutParams.WRAP_CONTENT);
      }

      int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
      int lpHeight = p.height;
      int childHeightSpec;
      if (lpHeight > 0) 
      {
          childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
      } else {
          childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
      }
      child.measure(childWidthSpec, childHeightSpec);
  }

  private void onRefresh()
  {
    if (this.refreshListener != null)
      this.refreshListener.onRefresh();
    onRefreshComplete();
  }

  public void onRefreshComplete()
  {
    this.state = DONE;
    this.lastUpdatedTextView.setText(getContext().getString(R.string.pull_to_list_last_update) + new Date().toLocaleString());
    changeHeaderViewByState();
  }

  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    this.firstItemIndex = getFirstVisiblePosition();
    if (this.isRefreshable)
    {
          int i = (int)paramMotionEvent.getY();
          switch (paramMotionEvent.getAction())
          {
              case MotionEvent.ACTION_UP:
              {          
                  if ((this.state != REFRESHING))
                  {
                    if (this.state == RELEASE_To_REFRESH)
                    {
                      this.state = REFRESHING;
                      changeHeaderViewByState();
                      onRefresh();
                    }else
                    {
                        this.state = DONE;
                        changeHeaderViewByState();
                    }
                  }
                  
                  this.isRecored = false;
                  this.isBack = false;
                  break;
              }
              case MotionEvent.ACTION_DOWN:
              {
                  if ((this.firstItemIndex == 0) &&  (!this.isRecored))
                  {
                      this.startY = (int)paramMotionEvent.getY();
                      this.isRecored = true;
                  }
                  break;
              }
              case MotionEvent.ACTION_MOVE:
              {
                  if ((this.firstItemIndex == 0) &&  (!this.isRecored))
                  {
                      this.startY = (int)paramMotionEvent.getY();
                      this.isRecored = true;
                  }
                  if((this.state == REFRESHING) || (!this.isRecored))
                  {
                      break;
                  }
                  if ((this.state == DONE) && (i - this.startY > 0))
                  {
                    this.state = PULL_To_REFRESH;
                    changeHeaderViewByState();
                  }else
                  if(this.state == RELEASE_To_REFRESH)
                  {
                      setSelection(0);
                      if (((i - this.startY) / 3 < this.headContentHeight) && (i - this.startY > 0))
                      {
                        this.state = PULL_To_REFRESH;
                        changeHeaderViewByState();
                      }
                  }else
                  if(this.state == PULL_To_REFRESH)
                  {
                      setSelection(0);
                      if ((i - this.startY) / 3 < this.headContentHeight)
                      {  
                          
                      }else
                      {
                          this.state = RELEASE_To_REFRESH;
                          this.isBack = true;
                          changeHeaderViewByState();
                      }
                  }
                                                     
                  if (i - this.startY > 0)
                  {
                      if (this.state == PULL_To_REFRESH)
                      {
                          this.headView.setPadding(0, (i - this.startY) / 3 - this.headContentHeight, 0, 0);                      
                      }else
                      if(this.state == RELEASE_To_REFRESH)
                      {
                          this.headView.setPadding(0, (i - this.startY) / 3 - this.headContentHeight, 0, 0);                      
                      }
                      break;
                  }else
                  {
                      this.state = DONE;
                      changeHeaderViewByState();
                  }
                  break;
              }
          }
    }   
    return super.onTouchEvent(paramMotionEvent);
  }

  public void setAdapter(BaseAdapter paramBaseAdapter)
  {
    this.lastUpdatedTextView.setText(getContext().getString(R.string.pull_to_list_last_update) + new Date().toLocaleString());
    super.setAdapter(paramBaseAdapter);
  }

  public void setOnRefreshListener(OnRefreshListener paramOnRefreshListener)
  {
    this.refreshListener = paramOnRefreshListener;
    this.isRefreshable = true;
  }

  public static abstract interface OnRefreshListener
  {
    public abstract void onRefresh();
  }
}