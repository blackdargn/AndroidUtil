package com.android.util.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

/*********************************************************
 * @author : zhaohua
 * @version : 2013-1-11
 * @see : 模仿Paht Button的样式
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class PathButtonGroup extends RelativeLayout
{    
    private int mRadis  = 0;   // R最好计算出来
    private int mStartA = 90;  // 伞形的起始弧度
    private boolean mIsOpen = false;
    private View mCenterButton;
    private List<TouchObject> mButtons;
    private OnClickListener mButtonListener;
    
    public PathButtonGroup(Context context)
    {
        super(context);
    }
    
    public PathButtonGroup(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        mCenterButton = getChildAt( getChildCount() - 1);
        if(mCenterButton != null)
        {
            mCenterButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(mIsOpen)
                    {
                        closeView(300);
                        mCenterButton.startAnimation(getRotateAnimation(-270, 0, 300));
                    }else
                    {
                        openView(300);
                        mCenterButton.startAnimation(getRotateAnimation(0, -270, 300));
                    }
                }
            });
        }
        
        adjustWidth();
    }
    
    public void openDelay() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                openView(300);
                mCenterButton.startAnimation(getRotateAnimation(0, -270, 300));
            }
        }, 500);
    }
    
    public void closeDelay() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                closeView(300);
                mCenterButton.startAnimation(getRotateAnimation(-270, 0, 300));
            }
        }, 100);
    }
    
    public void adjustWidth()
    {
    	DisplayMetrics dm = getResources().getDisplayMetrics();
    	int width = (int)(225*dm.density);
        setMinimumHeight(width);
        setMinimumWidth(width);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	int width = ((View)getChildAt(1)).getWidth();
        if(width != 0 && mRadis == 0) mRadis = (int)(getWidth() - 1.5*width);
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {        
        super.onLayout(changed, l, t, r, b);
        if(mButtons != null && changed) {
            mButtons.clear();
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                if (mButtons != null)
                {
                    TouchObject click = getClick(x, y);
                    onClick(click);
                }
                if(mIsOpen)
                {
                    closeView(300);
                }
                break;
            }
        }
        return super.onTouchEvent(event);
    }
    
    public void setOnClickListener(OnClickListener listener)
    {
        this.mButtonListener = listener;
    }
    
    public void openView(int durationMillis)
    {
        preTouch();
        int size = getChildCount()-1;
        // 对称扇形2*mStartA 还是非对称 mStartA
        int modeDegree = mStartA;
        double detA = (180 - modeDegree)/(size-1);
        int dx = 0, dy = 0;
        for (int i = 0; i < size; i++)
        {
            final View inoutimagebutton = getChildAt(i);
            double a = Math.toRadians( mStartA + detA * i);
            
            inoutimagebutton.setVisibility(0);
            double marginTop = Math.sin(a) * mRadis;
            double marginRight = Math.cos(a) * mRadis;
            if(dx == 0) dx = (mCenterButton.getWidth() - inoutimagebutton.getWidth())/2;
            if(dy == 0) dy = (mCenterButton.getHeight() - inoutimagebutton.getHeight())/2;
            Animation animation = new TranslateAnimation(dx, -(int) marginRight, -dy, -(int) marginTop);
            animation.setFillAfter(true);
            animation.setDuration(durationMillis);
            animation.setStartOffset((i * 100)/ (-1 + size));
            animation.setInterpolator(new OvershootInterpolator(2F));

            inoutimagebutton.startAnimation(animation);
        }
        mIsOpen = true;
    }
    
    public void closeView(int durationMillis)
    {
        int size = getChildCount() -1;
        // 对称扇形2*mStartA 还是非对称 mStartA
        int modeDegree = mStartA;
        double detA = (180 - modeDegree)/(size-1);
        int dx = 0, dy = 0;
        for (int i = 0; i < size; i++)
        {
            final View inoutimagebutton = (View)getChildAt(i);
            double a = Math.toRadians( mStartA + detA * i);
            
            double marginTop = Math.sin(a) * mRadis;
            double marginRight = Math.cos(a) * mRadis;
            if(dx == 0) dx = (mCenterButton.getWidth() - inoutimagebutton.getWidth())/2;
            if(dy == 0) dy = (mCenterButton.getHeight() - inoutimagebutton.getHeight())/2;
            
            Animation animation = new TranslateAnimation(-(int) marginRight, dx, -(int) marginTop, -dy);
            animation.setFillAfter(true);
            animation.setDuration(durationMillis);
            animation.setStartOffset(((size - i)* 100) / (-1 + size));// 顺序倒一下比较舒服
            animation.setInterpolator(new AnticipateInterpolator(2F));

            inoutimagebutton.startAnimation(animation);
        }
        
        mIsOpen = false;
    }
    
    public Animation getRotateAnimation(float fromDegrees,float toDegrees, int durationMillis)
    {
        RotateAnimation rotate = new RotateAnimation(fromDegrees, toDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        rotate.setDuration(durationMillis);
        rotate.setFillAfter(true);
        return rotate;
    }
    
    private void preTouch()
    {
        if(mRadis == 0 || mButtons != null && mButtons.size() > 0) return;
        
        if(mButtons == null) {
            mButtons = new ArrayList<TouchObject>();
        }
        int size = getChildCount() -1;
        // 对称扇形2*mStartA 还是非对称 mStartA
        int modeDegree = mStartA;
        double detA = (180 - modeDegree)/(size-1);
        
        for (int i = 0; i < size; i++)
        {
            final View inoutimagebutton = (View)getChildAt(i);
            double a = Math.toRadians( mStartA + detA * i);
            
            double marginTop = Math.sin(a) * mRadis;
            double marginRight = Math.cos(a) * mRadis;
            
            Point point = new Point((int) marginRight, (int) marginTop);
            Rect animationRect = getAnimationRect(inoutimagebutton, point);
            TouchObject obj = new TouchObject();
            obj.setTouchView(inoutimagebutton);
            obj.setTouchArea(animationRect);
            mButtons.add(obj);
        }
    }
    
    private TouchObject getClick(float x, float y)
    {
        TouchObject obj = null;
        for (TouchObject o : mButtons)
        {
            if (o.getTouchArea().contains((int) x, (int) y))
            {
                obj = o;
            }
        }
        return obj;
    }

    private Rect getAnimationRect(View btn, Point point)
    {
        Rect r = new Rect();
        r.left = btn.getLeft() - point.x;
        r.top = btn.getTop() - point.y;
        r.right = btn.getRight() - point.x;
        r.bottom = btn.getBottom() - point.y;
        return r;
    }
    
    private void onClick(TouchObject obj)
    {
        if (obj != null && mIsOpen)
        {
            if(mButtonListener != null)
            {
                mButtonListener.onClick(obj.getTouchView());
            }
        }
    }
    
    private static class TouchObject 
    {
        private View touchView;
        private Rect touchArea;

        public void setTouchView(View touchView)
        {
            this.touchView = touchView;
        }
        
        public View getTouchView()
        {
            return touchView;
        }

        public Rect getTouchArea() 
        {
            return touchArea;
        }

        public void setTouchArea(Rect touchArea) 
        {
            this.touchArea = touchArea;
        }
    }
}