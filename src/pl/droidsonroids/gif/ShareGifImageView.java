package pl.droidsonroids.gif;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

import com.android.util.system.Logger;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

public class ShareGifImageView extends GifImageView {

    private static final Handler UI_HANDLER = new Handler( Looper.getMainLooper() );
    private HashMap<Integer, SoftReference<GifDrawable>> gifDrawables = new HashMap<Integer, SoftReference<GifDrawable>>();
    private ArrayList<SoftReference<GifDrawable>> gifLists= new ArrayList<SoftReference<GifDrawable>>();
    private int gifId = -1;
    private int duration = -1;
    
    public ShareGifImageView(Context context) {
        super(context);
    }
    
    public ShareGifImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public ShareGifImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @TargetApi ( Build.VERSION_CODES.JELLY_BEAN )
    @SuppressWarnings ( "deprecation" )
    //new method not avalilable on older API levels
    void setResource ( boolean isSrc, int resId, Resources res )
    {
        GifDrawable drawable = null;
        SoftReference<GifDrawable> one = null;
        try
        {
            gifId = resId;
            
            if(gifDrawables.containsKey(resId)) {
                drawable = gifDrawables.get(resId).get();
            }else {
                drawable = null;
            }
            if(drawable == null) {
                drawable = new GifDrawable(res, resId);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                drawable.stop();
                
                one = new SoftReference<GifDrawable>(drawable);
                gifDrawables.put(resId, one);
                gifLists.add(one);
            }
            if ( isSrc )
                setImageDrawable( drawable );
            else if ( Build.VERSION.SDK_INT >= 16 )
                setBackground( drawable );
            else
                setBackgroundDrawable( drawable );
            
            mInvalidaTask.run();
            
            return;
        }
        catch ( IOException e )
        {
            //ignored
        }
        catch ( NotFoundException e )
        {
            //ignored
        }
        if ( isSrc )
            super.setImageResource( resId );
        else
            super.setBackgroundResource( resId );
    }
    
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if(visibility ==View.VISIBLE) {
            mInvalidaTask.run();
        }else {
            UI_HANDLER.removeCallbacks(mInvalidaTask);
        }
    }
    
    private final Runnable mInvalidaTask = new Runnable()
    {
        @Override
        public void run ()
        {
            if( gifId > 0 && gifDrawables != null) {
                SoftReference<GifDrawable> one = null;
                one = gifDrawables.get(gifId);
                if(one != null && one.get() != null) {
                   one.get().nextFrame();
                   duration = one.get().getFrameDuration();
                }
                invalidate();
                if(duration >= 0) {
                    UI_HANDLER.postDelayed(mInvalidaTask, duration);
                }
            }
        }
    };
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(gifLists != null && !gifLists.isEmpty()) {
            for(SoftReference<GifDrawable> one : gifLists) {
                if(one != null && one.get() != null) {
                    one.get().recycle();
                }
            }
            gifDrawables.clear();
            gifLists.clear();
            gifId = -1;
            gifDrawables = null;
            gifLists = null;
            duration = -1;
            UI_HANDLER.removeCallbacks(mInvalidaTask);
            Logger.d("--->ShareGifImageView onDetachedFromWindow");
        }
    }

}
