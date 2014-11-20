package com.android.util.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/*********************************************************
 * @author : zhaohua
 * @version : 2013-9-13
 * @see : 可设置最大高度的ListView
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class MHListView extends ListView {
    
    private int mMaxHeight;

    public void setMaxHeight(int maxHeight) {
        this.mMaxHeight = maxHeight;
    }

    public MHListView(Context context) {
        super(context);
    }

    public MHListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MHListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mMaxHeight > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}