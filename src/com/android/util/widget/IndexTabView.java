package com.android.util.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.util.R;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-8-14
 * @see : TODO
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class IndexTabView extends LinearLayout
{
    private ImageView  tab_tx;
    private ImageView tab_indac;
    
    public IndexTabView(Context paramContext, AttributeSet paramAttributeSet)
    {
      super(paramContext, paramAttributeSet);
      View row = inflate(getContext(), R.layout.view_bottom_tab, null);
      addView(row,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
      tab_tx = (ImageView)findViewById(R.id.tab_tx);
      tab_indac = (ImageView)findViewById(R.id.tab_indac);
      setGravity(Gravity.CENTER);
    }
    
    public void setImageResource(int image)
    {
        tab_tx.setBackgroundResource(image);
    }
    
    public void setClick()
    {
        tab_indac.setVisibility(View.VISIBLE);
    }
    
    public void reset(int normalImage)
    {
    	tab_tx.setBackgroundResource(normalImage);
        tab_indac.setVisibility(View.GONE);
    }
}
