
package com.android.util.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.util.R;

/*******************************************************
 * @author : zhaohua
 * @version: 2012-8-11
 * @see : 标题栏 
 * @Copyright: copyrights reserved by personal 2007-2011
*******************************************************/
public class TitleBar extends RelativeLayout
{
    private ImageTextButton left_lay;
    private ImageTextButton right_lay;
    private LinearLayout title_lay;
    private TextView title_tv;
    
    public TitleBar(Context context)
    {
        super(context);
    }
    
    public TitleBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        initViews();
    }
    
    /**　设置中间的标题文本*/
    public void setTitle(int resId)
    {
        title_tv.setText(resId);
    }
    /**　设置中间的标题文本*/
    public void setTitle(String resId)
    {
        title_tv.setText(resId);
    }
    
    /** 设置TitleBar背景*/
    public void setTitleBarBg(int bgId) {
        setBackgroundResource(bgId);
    }
    
    /** 设置左边区域的背景*/
    public void setLeftButtonBg(int bgId)
    {
        left_lay.setBg(bgId);
    }
    /** 设置右区域的背景*/
    public void setRightButtonBg(int bgId)
    {
        right_lay.setBg(bgId);
    }
    
    /** 设置左区域的图片,并隐藏文本*/
    public void setLeftButtonImage(int imgId)
    {
        left_lay.setImage(imgId);
    }
    /** 设置右区域的图片,并隐藏文本*/
    public void setRightButtonImage(int imgId)
    {
        right_lay.setImage(imgId);
    }
    
    /** 设置左区域的文本,并隐藏图片*/
    public void setLeftButtonText(int textId)
    {
        left_lay.setText(textId);
    }
    /** 设置右区域的文本,并隐藏图片*/
    public void setRigthButtonText(int textId)
    {
        right_lay.setText(textId);
    }
    
    /** 设置左区域的文本,并隐藏图片*/
    public void setLeftButtonText(String textId)
    {
        left_lay.setText(textId);
    }
    /** 设置右区域的文本,并隐藏图片*/
    public void setRightButtonText(String textId)
    {
        right_lay.setText(textId);
    }
    
    public TextView getLeftTextView()
    {
        return left_lay.getTextView();
    }
    
    public TextView getRightTextView()
    {
        return right_lay.getTextView();
    }
    
    public TextView getTitleTextView() {
        return title_tv;
    }
    
    public ImageView getLeftImgView()
    {
        return left_lay.getImageView();
    }
    
    public ImageView getRightImgView()
    {
        return right_lay.getImageView();
    }
    
    /** 设置左区域的可见性*/
    public void setLeftButtonVisible(boolean visible)
    {
        left_lay.setVisibility(visible ? View.VISIBLE: View.GONE);
    }
    
    /** 设置右区域的可见性*/
    public void setRightButtonVisible(boolean visible)
    {
        right_lay.setVisibility(visible ? View.VISIBLE: View.GONE);
    }

    /** 重置左边区域的视图*/
    public void setCustomLeftView(View customView)
    {
        left_lay.setCustomView(customView);
    }
    
    /** 重置右边区域的视图*/
    public void setCustomRightView(View customView)
    {
        right_lay.setCustomView(customView);
    }
    
    /** 重置中间区域的视图*/
    public void setCustomCenterView(View customView)
    {
        title_lay.removeAllViews();
        title_lay.addView(customView);
    }
    
    /** 设置左右按钮的事件监听器
     *  @param leftenable 左按钮是否设置监听，默认是设置了finish的监听动作的，
     *                     如果此为返回，则设置为false
     *  @param rightenable 右按钮是否设置监听，默认是没有设置任何监听动作的
     * */
    public void setOnTitleBarClickListener(OnTitleBarClickListener listener,
            boolean leftenable, boolean rightenable)
    {
        if(leftenable) left_lay.setOnClickListener(listener);
        if(rightenable) right_lay.setOnClickListener(listener);
    }
    
    public void setOnTitleBarClickListener(OnTitleBarClickListener listener) {
        setOnTitleBarClickListener(listener, true, true);
    }
    
    private void initViews()
    {        
        left_lay = (ImageTextButton)findViewById(R.id.left_lay);
        right_lay = (ImageTextButton)findViewById(R.id.right_lay);
        title_lay = (LinearLayout)findViewById(R.id.title_lay);
        title_tv = (TextView)findViewById(R.id.title_tv);
        if(isInEditMode()) return;
        // 默认给左边设置为 返回动作按钮
        left_lay.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Context context = getContext();
                if(context instanceof Activity)
                {
                    ((Activity)context).finish();
                }
            }
        });
    }

    public abstract class OnTitleBarClickListener implements OnClickListener
    {
        public void onLeftButtonClick(){};
        public abstract void onRightButtonClick();
        
        @Override
        public void onClick(View v)
        {
            if (v == left_lay) {
                onLeftButtonClick();
            } else if (v== right_lay) {
                onRightButtonClick();
            }
        }
    }
}