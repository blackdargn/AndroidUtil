package com.android.util.widget;

import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.util.R;
/*********************************************************
 * @author : zhaohua
 * @version : 2012-12-13
 * @see : 动画类型
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class AnimImageView extends ImageView implements Runnable
{
    private AtomicBoolean isPlay = new AtomicBoolean();
    private int curIndex;
    
    public AnimImageView(Context context)
    {
        this(context, null);
    }
    
    public AnimImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    public void start() {
        start(null);
    }
    
    /**
     * 启动当前图片控件的动画
     */
    public void start(String tag)
    {
        isPlay.set(true);
        setTag(R.id.animID, tag);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                updateStatu();
            }
        }, 100);
    }
    
    @Override
    public void setTag(Object tag) {
        super.setTag(tag);
        Object animTag = getTag(R.id.animID);
        if(animTag != null && animTag.equals(tag)) {
            isPlay.set(true);
        }else {
            isPlay.set(false);
        }
    }
    
    private void updateStatu() {
        Drawable bg = getDrawable();
        if (bg != null && bg instanceof AnimationDrawable) {
            AnimationDrawable anmi = ((AnimationDrawable) bg);
            if(isPlay.get()) {
                curIndex++;
                if(curIndex >= anmi.getNumberOfFrames()) {
                    curIndex = 0;
                }
                setImageDrawable(anmi);
                anmi.selectDrawable(curIndex);
                postDelayed(this, anmi.getDuration(curIndex));
            }else {
                curIndex = 0;
                anmi.selectDrawable(0);
            }
        }
    }
    
    /**
     * 结束当前图片控件的动画
     */
    public void stop()
    {
        isPlay.set(false);
        updateStatu();
    }

    @Override
    public void run() {
        updateStatu();
    }
}