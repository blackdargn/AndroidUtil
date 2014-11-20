package com.android.util.image;

import android.graphics.Bitmap;
import android.graphics.ComposeShader;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;

public class RoundedVignetteDrawable extends RoundedDrawable {

    public RoundedVignetteDrawable(Bitmap bitmap, int cornerRadius, int margin) {
        super(bitmap, cornerRadius, margin, null);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        RadialGradient vignette = new RadialGradient(mRect.centerX(), mRect.centerY() * 1.0f / 0.7f,
                mRect.centerX() * 1.3f, new int[] { 0, 0, 0x7f000000 }, new float[] { 0.0f, 0.7f, 1.0f },
                Shader.TileMode.CLAMP);

        Matrix oval = new Matrix();
        oval.setScale(1.0f, 0.7f);
        vignette.setLocalMatrix(oval);

        paint.setShader(new ComposeShader(bitmapShader, vignette, PorterDuff.Mode.SRC_OVER));
    }
}