package com.android.util.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.android.util.system.MyApplication;

public class DrawView extends View
{
    private float preX;
    private float preY;
    private Path path;
    private Paint paint = null;
    private Paint bmpPaint;
    private Bitmap cacheBitmap = null;
    private Canvas cacheCanvas = null;

    
    public DrawView(Context context)
    {
        super(context); 
    }
    
    public DrawView(Context context, AttributeSet set)
    {
        super(context, set); 
    }

    public void init(int width, int height)
    {
        if(cacheBitmap == null)
        {
            try {
                cacheBitmap = Bitmap.createBitmap(width, height,Config.ARGB_8888);
            }catch (OutOfMemoryError e) {
               Log.e(VIEW_LOG_TAG, "-->OutOfMemoryError 1 ");
               try {
                   cacheBitmap = Bitmap.createBitmap(width, height,Config.ARGB_4444);
               }catch (OutOfMemoryError e2) {
                   Log.e(VIEW_LOG_TAG, "-->OutOfMemoryError 2");
                   try {
                       cacheBitmap = Bitmap.createBitmap(width, height,Config.RGB_565);
                    }catch (OutOfMemoryError e3) {
                        Log.e(VIEW_LOG_TAG, "-->OutOfMemoryError 3");
                        Toast.makeText(getContext(), "内存不足，无法绘图！", Toast.LENGTH_SHORT).show();
                        return;
                    }
               }
            }
            cacheCanvas = new Canvas();
    
            path = new Path();
            cacheCanvas.setBitmap(cacheBitmap);
    
            paint = new Paint(Paint.DITHER_FLAG);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth((int)(6 * MyApplication.getContext().getDensity() + 0.5f));
            paint.setAntiAlias(true);
            paint.setDither(true);
            
            bmpPaint = new Paint();
        }
    }
    
    /** 重绘*/
    public void redraw()
    {
        if(cacheCanvas != null)
        {
            cacheCanvas.drawColor(0x00000000, Mode.CLEAR);
        }
        invalidate();
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {       
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);       
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        init(getWidth(), getHeight());
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float x = event.getX();
        float y = event.getY();

        if(cacheCanvas != null) {
            switch (event.getAction())
            {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                preX = x;
                preY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                path.quadTo(preX, preY, x, y);
                preX = x;
                preY = y;
                break;
            case MotionEvent.ACTION_UP:
                cacheCanvas.drawPath(path, paint);
                path.reset();
                break;
            }
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas); 
        if(cacheCanvas != null && cacheBitmap != null) {
            canvas.drawBitmap(cacheBitmap, 0, 0, bmpPaint);
            canvas.drawPath(path, paint);
        }
    }
}