package com.android.util.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TabWidget;

public class MTabWidget extends TabWidget {
    private static final int MOVE_MINI_LENGHT = 50;
    private float lastX = 0.0F;
    private float lastY = 0.0F;
    private boolean isTouchMove = false;
    private OnTabWidgetTouchMoveListener onTabWidgetTouchMoveListener;

    public MTabWidget(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
    }

    public MTabWidget(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
    }

    public boolean onInterceptTouchEvent(MotionEvent paramMotionEvent) {
        if (paramMotionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            float f1 = paramMotionEvent.getX();
            float f2 = paramMotionEvent.getY();
            if ((f2 < this.lastY) && (this.lastY - f2 > MOVE_MINI_LENGHT)
                    && (this.lastY - f2 > Math.abs(this.lastX - f1)) && (this.onTabWidgetTouchMoveListener != null)
                    && (!this.isTouchMove)) {
                this.isTouchMove = true;
                this.onTabWidgetTouchMoveListener.onTouchMove();
            } else {
                this.isTouchMove = false;
                this.lastX = paramMotionEvent.getX();
                this.lastY = paramMotionEvent.getY();
            }
        }
        return super.onInterceptTouchEvent(paramMotionEvent);
    }

    public void setTabWidgetMoveListener(OnTabWidgetTouchMoveListener paramonTabWidgetTouchMoveListener) {
        this.onTabWidgetTouchMoveListener = paramonTabWidgetTouchMoveListener;
    }

    public abstract interface OnTabWidgetTouchMoveListener {
        public abstract void onTouchMove();
    }
}