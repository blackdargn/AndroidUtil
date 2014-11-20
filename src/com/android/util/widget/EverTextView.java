package com.android.util.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-11-22
 * @see : 当需要滚动时，不获取焦点也滚动
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class EverTextView extends TextView
{
    private boolean isFoucsed = false;
    
    public EverTextView(Context context)
    {
        super(context);
    }
    
    public EverTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public boolean isFocused()
    {
        return isFoucsed;
    }
    
    public void setFoucsed(boolean isFoucsed) {
        this.isFoucsed = isFoucsed;
    }
}