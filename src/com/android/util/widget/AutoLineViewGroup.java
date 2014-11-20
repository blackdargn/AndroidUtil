package com.android.util.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class AutoLineViewGroup extends ViewGroup {
    final static String TAG = "AutoLineViewGroup";

    private final static int VIEW_COL_MARGIN  = 6;
    private final static int VIEW_ROW_MARGIN = 14;
    private int mRow;
    private int mMaxRow = 10;
    private boolean isChildFixable = false;
    private int colMargin = VIEW_COL_MARGIN;
    
    public AutoLineViewGroup(Context context) {
        super(context);
    }

    public AutoLineViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void setChildFixable(boolean enable, int colMargin) {
        isChildFixable = enable;
        if(!isChildFixable) {
            this.colMargin = colMargin;
        }
    }

    public void setMaxRow(int maxRow){
        mMaxRow = maxRow;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int count = getChildCount();
        for (int i = 0; i < count; ++i) {
            final View child = getChildAt(i);
            child.measure(0, 0);
        }
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        if(specMode != MeasureSpec.UNSPECIFIED) {
            mRow = 0;
            int lengthX = 0;
            int lengthY = 0;
            int widthspec = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            for (int i = 0; i < count; i++) {
                final View child = this.getChildAt(i);
                if(child.getVisibility() == View.GONE) {
                    continue;
                }
                int width = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();
                
                lengthX += width  + VIEW_COL_MARGIN;
                lengthY = mRow * (height + VIEW_ROW_MARGIN) + height;
                // if it can't drawing on a same line , skip to next line
                if (lengthX > widthspec) {
                    lengthX = width + VIEW_COL_MARGIN;
                    mRow++;
                    if(mRow >= mMaxRow ) {
                        break;
                    }
                    lengthY = mRow * (height + VIEW_ROW_MARGIN) + height;
                }
            }
            super.setMeasuredDimension(widthspec, lengthY + getPaddingTop() + getPaddingBottom());
        }else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
    
    @Override
    protected void onLayout(boolean changed, int arg1, int arg2, int arg3, int arg4) {
        if(changed && isChildFixable) {
            colMargin = VIEW_COL_MARGIN;
        }
        final int count = getChildCount();
        mRow = 0;
        arg1 = getPaddingLeft();
        arg3  -= getPaddingRight();
        int lengthX = arg1;
        arg2 = getPaddingTop();
        int lengthY = arg2;
               
        if(isChildFixable) {
            int sumWidth = 0;
            View child = null;
            int sumSize = 0;
            for (int i = 0; i < count; ++i) {
                child = getChildAt(i);
                if(child.getVisibility() == View.GONE) {
                    continue;
                }
                sumWidth += child.getMeasuredWidth() + colMargin;
                sumSize++;
                if(sumWidth - colMargin > arg3 - arg1 && (i-1) > 0) {
                    // 超出
                    colMargin = (arg3 - arg1 - sumWidth + child.getMeasuredWidth() + colMargin * sumSize)/(i-1);
                    break;
                }
            }
            if(sumWidth - colMargin <= arg3 - arg1 && sumSize-1 > 0) {
                // 不够
                colMargin = (arg3 - arg1 - sumWidth + colMargin * sumSize)/(sumSize-1);
            }
        }
        
        int colNum = 0;
        for (int i = 0; i < count; i++) {
            final View child = this.getChildAt(i);
            if(child.getVisibility() == View.GONE) {
                continue;
            }
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            if(isChildFixable && mRow > 0) {
                // 定长
                lengthX = getChildAt(i - colNum).getLeft() + width;
            }else {
                // 变长
                lengthX += width;
            }
            lengthY = mRow * (height + VIEW_ROW_MARGIN)  + height + arg2;
            // if it can't drawing on a same line , skip to next line
            if (lengthX > (isChildFixable ? arg3 + 12 : arg3) ) {
                lengthX = width + arg1;
                mRow++;
                if(colNum == 0) {
                    colNum = i;
                }
                lengthY = mRow * (height + VIEW_ROW_MARGIN)  + height + arg2;
            }
            if(mRow >= mMaxRow ) {
                child.setVisibility(View.GONE);
            }else {
                child.layout(lengthX - width, lengthY - height, lengthX, lengthY);
                lengthX += colMargin;
            }
        }
    }
}