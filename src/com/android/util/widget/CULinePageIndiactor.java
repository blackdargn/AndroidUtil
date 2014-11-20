package com.android.util.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class CULinePageIndiactor extends LinearLayout implements OnPageChangeListener,Runnable{

    private CUPagerHost mTabHost;
    private LinearLayout  mIndictor;
    private View              mLine;
    private int mLineWidth;
    private int mScrollState;
    private int mDx, mPositon, mLineMargin = -1;
    private Rect mLineRect = new Rect();
    
    public CULinePageIndiactor(Context context) {
        this(context, null);
    }
    
    public CULinePageIndiactor(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        setOrientation(LinearLayout.VERTICAL);
        mTabHost = new CUPagerHost(context);        
        addView(mTabHost, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mIndictor = new LinearLayout(context);
        mIndictor.setOrientation(LinearLayout.HORIZONTAL);
        addView(mIndictor, new LayoutParams(LayoutParams.MATCH_PARENT, 4));
        mLine = new View(context);
        mLine.setBackgroundColor(0xFFFF0000);
        mIndictor.addView(mLine, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
    }

    public void setLineBackgroundColor(int lineColor, int indictColor) {
        mLine.setBackgroundColor(lineColor);
        mIndictor.setBackgroundColor(indictColor);
    }

    public void setLineMargin(int lineMargin){
        mLineMargin = lineMargin;
    }

    public void setViewPager(ViewPager viewPager) {
        mTabHost.setViewPager(viewPager);
        mTabHost.setOnPageChangeListener(this);
    }

    public void setCurrentTab(int index) {
        mTabHost.setCurrentTab(index);
    }
    
    public CUPagerHost getCUPagerHost() {
        return mTabHost;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(mTabHost.getChildCount() > 0) {
            mLineWidth = getMeasuredWidth() / mTabHost.getChildCount();
            mLine.getLayoutParams().width = mLineWidth;
            mLine.requestLayout();
            if(mLineRect.right == 0){
                // 初始化
                mLineRect.set(0 , 0, mLineWidth, mLine.getMeasuredHeight());
            }
        }
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(mScrollState != ViewPager.SCROLL_STATE_IDLE) {
            updateLineLayout(mLineRect.left, mLineRect.top, mLineRect.right, mLineRect.bottom);
        }else {
            if(mDx != 0) {
                updateLineLayout(mPositon, mLineRect.top, mPositon + mLineWidth, mLineRect.bottom);
                if(mPositon == mLineRect.left ) {
                    mDx = 0;
                }else {
                    animToPositon();
                }
            }else {
                updateLineLayout(mLineRect.left, mLineRect.top, mLineRect.right, mLineRect.bottom);
            }
        }
    }

    private void updateLineLayout(int l, int t, int r, int b){
        mLine.layout(l + getLineMargin(), t, r - getLineMargin(), b);
    }

    private int getLineMargin(){
        return mLineMargin < 0 ? mLineWidth / 10 : mLineMargin;
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        mScrollState = arg0;
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        if(mScrollState != ViewPager.SCROLL_STATE_IDLE) {
            mLineRect.set( (int)(mLineWidth*( arg0 +arg1))  , 0, (int)(mLineWidth*(arg0 + arg1 +1)) , 4);
            requestLayout();
            invalidate();
        }
    }

    @Override
    public void onPageSelected(int arg0) {
        if(mScrollState == ViewPager.SCROLL_STATE_IDLE) {
            mPositon = mLineRect.left;
            mDx = (mLineWidth*arg0 - mPositon)/10;
            mLineRect.set(mLineWidth*arg0 , 0, mLineWidth*(arg0+1) , mLine.getMeasuredHeight());
            animToPositon();
        }
    }
    
    private void animToPositon() {
        postDelayed(this, 10);
    }

    @Override
    public void run() {
        mPositon += mDx;
        if(mDx > 0) {
            if(mPositon >= mLineRect.left) {
                mPositon = mLineRect.left;
            }
        }else
        if(mDx < 0){
            if(mPositon <= mLineRect.left) {
                mPositon = mLineRect.left;
            }
        }
        requestLayout();
        invalidate();
    }
}
