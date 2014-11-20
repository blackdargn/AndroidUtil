package com.android.util.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.util.R;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-8-15
 * @see : ImageView 与 TextView 的组合组件 
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class ImageTextButton extends RelativeLayout
{
    private ImageView img;
    private TextView  text;
    
    public ImageTextButton(Context paramContext)
    {
      super(paramContext);
    }
    
    public ImageTextButton(Context paramContext, AttributeSet paramAttributeSet)
    {
      super(paramContext, paramAttributeSet);
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        text = (TextView)findViewById(R.id.text);
        img = (ImageView)findViewById(R.id.img);
    }
       
    /** 设置背景*/
    public void setBg(int bgId)
    {
        setBackgroundResource(bgId);
    }
    
    /** 设置图片*/
    public void setImage(int imgId)
    {
        img.setImageResource(imgId);
        text.setVisibility(View.GONE);
        img.setVisibility(View.VISIBLE);
    }
    
    /** 设置文本*/
    public void setText(int textId)
    {
        text.setText(textId);
        img.setVisibility(View.GONE);
        text.setVisibility(View.VISIBLE);
    }
    
    /** 设置文本*/
    public void setText(String textId)
    {
        text.setText(textId);
        img.setVisibility(View.GONE);
        text.setVisibility(View.VISIBLE);
    }
   
    /** 重置状态*/
    public void reset()
    {
        text.setText("");
        text.setVisibility(View.GONE);
        img.setImageResource(0);
        img.setVisibility(View.GONE);
    }
    
    /**　获取TextView*/
    public TextView getTextView()
    {
        return text;
    }
    
    /**　获取ImageView*/
    public ImageView getImageView()
    {
        return img;
    }
    
    /** 设置自定义视图*/
    public void setCustomView(View customView)
    {
        removeAllViews();
        addView(customView);
    }
}