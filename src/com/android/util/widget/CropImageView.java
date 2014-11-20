package com.android.util.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CropImageView extends ImageViewTouchBase {
    ArrayList<HighlightView> HighlightViews = new ArrayList<HighlightView>();
    HighlightView mMotionHighlightView = null;
    float mLastX, mLastY;
    int mMotionEdge;
    boolean mSaving = false;
    private boolean mCircleCrop = false;
    private boolean mIsZoom = false;
    HighlightView mCrop;
    private View mBackView;
    
    public void setBackView(View v) {
        mBackView = v;
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mBitmapDisplayed.getBitmap() != null) {
            for (HighlightView hv : HighlightViews) {
                hv.mMatrix.set(getImageMatrix());
                hv.invalidate();
                if (hv.mIsFocused) {
                    centerBasedOnHighlightView(hv);
                }
            }
        }
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void zoomTo(float scale, float centerX, float centerY) {
        if(!mIsZoom) return;
        super.zoomTo(scale, centerX, centerY);
        for (HighlightView hv : HighlightViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
    }

    @Override
    protected void zoomIn() {
        if(!mIsZoom) return;
        super.zoomIn();
        for (HighlightView hv : HighlightViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
    }

    @Override
    protected void zoomOut() {
        if(!mIsZoom) return;
        super.zoomOut();
        for (HighlightView hv : HighlightViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
    }

    @Override
    protected void postTranslate(float deltaX, float deltaY) {
        super.postTranslate(deltaX, deltaY);
        for (int i = 0; i < HighlightViews.size(); i++) {
            HighlightView hv = HighlightViews.get(i);
            hv.mMatrix.postTranslate(deltaX, deltaY);
            hv.invalidate();
        }
    }
    
    @Override
    public void center(boolean horizontal, boolean vertical) {
        if(!mIsZoom) return;
        super.center(horizontal, vertical);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mSaving) {
            return mBackView != null ? mBackView.onTouchEvent(event) : false;
        }

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            for (int i = 0; i < HighlightViews.size(); i++) {
                HighlightView hv = HighlightViews.get(i);
                mMotionEdge = hv.getHit(event.getX(), event.getY());
                if (mMotionEdge != HighlightView.GROW_NONE) {
                    mMotionHighlightView = hv;
                    mLastX = event.getX();
                    mLastY = event.getY();
                    mMotionHighlightView
                            .setMode((mMotionEdge == HighlightView.MOVE) ? HighlightView.ModifyMode.Move
                                    : HighlightView.ModifyMode.Grow);
                    break;
                }
            }
            break;
        case MotionEvent.ACTION_UP:
            if (mMotionHighlightView != null) {
                centerBasedOnHighlightView(mMotionHighlightView);
                mMotionHighlightView.setMode(HighlightView.ModifyMode.None);
            }
            mMotionHighlightView = null;
            break;
        case MotionEvent.ACTION_MOVE:
            if (mMotionHighlightView != null) {                
                mMotionHighlightView.handleMotion(mMotionEdge, event.getX() - mLastX, event.getY() - mLastY);
                mLastX = event.getX();
                mLastY = event.getY();
                if (true) {
                    // This section of code is optional. It has some user
                    // benefit in that moving the crop rectangle against
                    // the edge of the screen causes scrolling but it means
                    // that the crop rectangle is no longer fixed under
                    // the user's finger.
                    ensureVisible(mMotionHighlightView);
                }
            }
            break;
        }

        switch (event.getAction()) {
        case MotionEvent.ACTION_UP:
            center(true, true);
            // 通知裁剪区移动了
            if(mMoveListener != null && mMotionEdge != HighlightView.GROW_NONE) {
                mMoveListener.onMoveFinish(this);
            }
            break;
        case MotionEvent.ACTION_MOVE:
            // if we're not zoomed then there's no point in even allowing
            // the user to move the image around. This call to center puts
            // it back to the normalized location (with false meaning don't
            // animate).
            if (getScale() == 1F) {
                center(true, true);
            }
            break;
        }
        
        return mMotionEdge != HighlightView.GROW_NONE ? true : (mBackView != null ? mBackView.onTouchEvent(event) : false);
    }

    // Pan the displayed image to make sure the cropping rectangle is visible.
    private void ensureVisible(HighlightView hv) {
        Rect r = hv.mDrawRect;

        int panDeltaX1 = Math.max(0, getLeft() - r.left);
        int panDeltaX2 = Math.min(0, getRight() - r.right);

        int panDeltaY1 = Math.max(0, getTop() - r.top);
        int panDeltaY2 = Math.min(0, getBottom() - r.bottom);

        int panDeltaX = panDeltaX1 != 0 ? panDeltaX1 : panDeltaX2;
        int panDeltaY = panDeltaY1 != 0 ? panDeltaY1 : panDeltaY2;

        if (panDeltaX != 0 || panDeltaY != 0) {
            panBy(panDeltaX, panDeltaY);
        }
    }

    // If the cropping rectangle's size changed significantly, change the
    // view's center and scale according to the cropping rectangle.
    private void centerBasedOnHighlightView(HighlightView hv) {
        Rect drawRect = hv.mDrawRect;

        float width = drawRect.width();
        float height = drawRect.height();

        float thisWidth = getWidth();
        float thisHeight = getHeight();

        float z1 = thisWidth / width * .6F;
        float z2 = thisHeight / height * .6F;

        float zoom = Math.min(z1, z2);
        zoom = zoom * this.getScale();
        zoom = Math.max(1F, zoom);

        if ((Math.abs(zoom - getScale()) / zoom) > .1) {
            float[] coordinates = new float[] { hv.mCropRect.centerX(),
                    hv.mCropRect.centerY() };
            getImageMatrix().mapPoints(coordinates);
            zoomTo(zoom, coordinates[0], coordinates[1], 300F);
        }

        ensureVisible(hv);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < HighlightViews.size(); i++) {
            HighlightViews.get(i).draw(canvas);
        }
    }

    public void add(HighlightView hv) {
        HighlightViews.add(hv);
        invalidate();
    }
    
    public void clear() {
        super.clear();
        HighlightViews.clear();
    }
    
    /** 测试点是否在裁剪区域内*/
    public boolean isIn(Point point) {
        if(mCrop!=null) {
            return mCrop.getHit(point.x, point.y) != HighlightView.GROW_NONE;
        }else {
            return false;
        }
    }
    
    public void startDetection() {
        if(mBitmap == null) return;
        mRunFaceDetection.run();
    }
    
    public void setIsCircleCrop(boolean isCircleCrop) {
        if(mCrop!=null) {
            mCrop.setIsCircle(isCircleCrop);
        }
        invalidate();
    }
    
    public boolean isCircleCrop() {
        return mCircleCrop;
    }
    
    private Runnable mRunFaceDetection = new Runnable() {
        float mScale = 1F;
        Matrix mImageMatrix;

        // Create a default HightlightView if we found no face in the picture.
        private void makeDefault() {
            if(mBitmap == null) return;
            HighlightView hv = new HighlightView(CropImageView.this);

            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();

            Rect imageRect = new Rect(0, 0, width, height);

            // make the default size about 4/5 of the width or height
            int cropWidth = Math.min(width, height) * 4 / 5;
            int cropHeight = cropWidth;

            int x = (width - cropWidth) / 2;
            int y = (height - cropHeight) / 2;

            RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
            hv.setup(mImageMatrix, imageRect, cropRect, mCircleCrop, mCircleCrop);
            CropImageView.this.add(hv);
        }

        public void run() {
            mImageMatrix = CropImageView.this.getImageMatrix();

            mScale = 1.0F / mScale;
            mHandler.post(new Runnable() {
                public void run() {
                    makeDefault();

                    CropImageView.this.invalidate();
                    if (CropImageView.this.HighlightViews.size() == 1) {
                        mCrop = CropImageView.this.HighlightViews.get(0);
                        mCrop.setFocus(true);
                    }
                }
            });
        }
    };
    
    private OnMoveListener mMoveListener;
    public void setOnMoveListener(OnMoveListener listener) {
        this.mMoveListener  = listener;
    }
    public static interface OnMoveListener{
        public void onMoveFinish(CropImageView view);
    }
}