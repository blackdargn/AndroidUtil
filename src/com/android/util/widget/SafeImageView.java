package com.android.util.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.util.system.Logger;

public class SafeImageView extends ImageView {

    public SafeImageView(Context context) {
        super(context);
    }
    
    public SafeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        }catch (Exception e) {
            Logger.e(""+e.getMessage());
        }
    }
}