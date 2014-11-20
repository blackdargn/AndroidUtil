package com.android.util.image;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

public class RoundedDrawable extends Drawable {

    protected final float cornerRadius;
    protected final float margin;
    protected final RectF mRect = new RectF(), mBitmapRect, mOrinRect;
    protected final BitmapShader bitmapShader;
    protected final Paint paint;
    protected Bitmap frameBmp;

    public RoundedDrawable(Bitmap bitmap, float cornerRadius, float margin, Bitmap frame ) {
        this.cornerRadius = cornerRadius;
        this.margin = margin;
        this.frameBmp = frame;
        
        bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mBitmapRect = new RectF(margin, margin, bitmap.getWidth() - margin, bitmap.getHeight() - margin);
        mOrinRect = new RectF(0, 0, frame.getWidth(), frame.getHeight());
        mRect.set(margin, margin, frame.getWidth() - margin, frame.getHeight() - margin);
        
        Matrix shaderMatrix = new Matrix();
        shaderMatrix.setRectToRect(mBitmapRect, mRect, Matrix.ScaleToFit.FILL);
        bitmapShader.setLocalMatrix(shaderMatrix);
        
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(bitmapShader);
        
        setBounds(0, 0, frame.getWidth(), frame.getHeight());
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
    }
    
    @Override
    public int getIntrinsicHeight() {
        return frameBmp.getHeight();
    }
    
    @Override
    public int getIntrinsicWidth() {
        return frameBmp.getWidth();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRoundRect(mRect, cornerRadius, cornerRadius, paint);
        canvas.drawBitmap(frameBmp, null, mOrinRect, null);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }
}