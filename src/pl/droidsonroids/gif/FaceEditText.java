package pl.droidsonroids.gif;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.util.R;
import com.android.util.system.Logger;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

public class FaceEditText extends EditText {

    private static final Handler UI_HANDLER = new Handler( Looper.getMainLooper() );
    private HashMap<Integer, SoftReference<GifDrawable>> gifDrawables;
    private ArrayList<SoftReference<GifDrawable>> gifLists;
    private ArrayList<Integer> gifIds;
    private static Pattern p=Pattern.compile("f\\d{1,3}");
    private int duration = -1;
    
    public FaceEditText(Context context) {
        super(context);
    }
    
    public FaceEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public FaceEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public boolean onTextContextMenuItem(int id) {
        boolean r = super.onTextContextMenuItem(id);
        if(id == android.R.id.paste) {
            setText(getText().toString());
            Selection.setSelection((Spannable) getText(), getText().length());
        }
        return r;
    }
    
    /** 模拟DEL键*/
    public void animDelDown() {
        KeyEvent keyEventDown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);  
        onKeyDown(KeyEvent.KEYCODE_DEL, keyEventDown);       
    }
    
    /** 将图像插入到EditText控件光标的位置 */
    void insertSelection( Spannable spannableString) {
        int selStart = getSelectionStart();
        int selEnd = getSelectionEnd();
        Editable mText = getText();
        if (!isFocused()) {
          selStart = 0;
          selEnd = mText.length();
         }
        int min = Math.min(selStart, selEnd);
        int max = Math.max(selStart, selEnd);
        if (min < 0) {
            min = 0;
        }
        if (max < 0) {
            max = 0;
        }
        Selection.setSelection((Spannable) mText, max);
        ((Editable) mText).replace(min, max, spannableString);
    }
    
    /** EditText插入一个图片*/
    public void insertFace(int faceId) {
        try{
            //  根据资源ID获得资源图像的Bitmap对象
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), faceId);
            //  根据Bitmap对象创建ImageSpan对象
            ImageSpan imageSpan = new ImageSpan(getContext(), bitmap);
            //  创建一个SpannableString对象，以便插入用ImageSpan对象封装的图像
            String imgFace = getResources().getResourceEntryName(faceId);
            SpannableString spannableString = new SpannableString(imgFace);
            //  用ImageSpan对象替换face
            spannableString.setSpan(imageSpan, 0, imgFace.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //  将图像插入到EditText控件光标的位置
            insertSelection(spannableString);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void setText(String spanString) {
        SpannableString ss = new SpannableString(spanString);        
        Matcher m=p.matcher(spanString);
        Resources res = getResources();
        int faceId = 0;
        String igmStr = null;
        Field field = null;
        ImageSpan span = null;
        GifDrawable drawable = null;
        SoftReference<GifDrawable> one = null;
        if(gifIds != null) {
            gifIds.clear();
        }
            
        while(m.find()){
            igmStr = m.group();
            faceId = 0;
            try {
                field =R.drawable.class.getField(igmStr);
                faceId = field.getInt(new R.drawable());
            }catch(Exception e) {
                e.printStackTrace();
            }
            if(faceId == 0) {
                continue;
            }
            try {
                if(gifDrawables == null) {
                    gifDrawables = new HashMap<Integer, SoftReference<GifDrawable>>();
                    gifLists = new ArrayList<SoftReference<GifDrawable>>();
                    gifIds = new ArrayList<Integer>();
                }
                
                if(gifIds.indexOf(faceId) == -1) {
                    gifIds.add(faceId);
                    
                    if(gifDrawables.containsKey(faceId)) {
                        drawable = gifDrawables.get(faceId).get();
                    }else {
                        drawable = null;
                    }
                    if(drawable == null) {
                        drawable = new GifDrawable(res, faceId);
                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                        drawable.stop();
                        
                        one = new SoftReference<GifDrawable>(drawable);
                        gifDrawables.put(faceId, one);
                        gifLists.add(one);
                    }
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
            if(drawable == null) {
                continue;
            }
            span = new ImageSpan(drawable);
            ss.setSpan(span, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        setText(ss);
        mInvalidaTask.run();
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
            if(gifIds != null && gifDrawables != null && !gifIds.isEmpty()) {
                SoftReference<GifDrawable> one = null;
                for(Integer id : gifIds) {
                    one = gifDrawables.get(id);
                    if(one != null && one.get() != null) {
                        one.get().nextFrame();
                        duration = one.get().getFrameDuration() >= 0 ? one.get().getFrameDuration() : duration;
                    }
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
            gifIds.clear();
            gifDrawables = null;
            gifLists = null;
            gifIds = null;
            duration = -1;
            UI_HANDLER.removeCallbacks(mInvalidaTask);
            Logger.d("--->FaceEditText onDetachedFromWindow");
        }
    }

}
