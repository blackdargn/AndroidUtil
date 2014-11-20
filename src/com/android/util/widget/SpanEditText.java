package com.android.util.widget;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.droidsonroids.gif.GifDrawable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import com.android.util.R;

public class SpanEditText extends EditText {
    
    private static final int ID_PASTE = android.R.id.paste;
    
    public SpanEditText(Context context) {
        super(context);
    }
    
    public SpanEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onTextContextMenuItem(int id) {
        boolean r = super.onTextContextMenuItem(id);
        if(id == ID_PASTE) {
            spanText(this, getText().toString());
            Selection.setSelection((Spannable) getText(), getText().length());
        }
        return r;
    }
    
    /** 将图像插入到EditText控件光标的位置 */
    public static void pasteSpanText(EditText et, Spannable spannableString) {
        int selStart = et.getSelectionStart();
        int selEnd = et.getSelectionEnd();
        Editable mText = et.getText();
        if (!et.isFocused()) {
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
    public static void insertImage(EditText et, int faceId) {
        try{
            //  根据资源ID获得资源图像的Bitmap对象
            Bitmap bitmap = BitmapFactory.decodeResource(et.getResources(), faceId);
            //  根据Bitmap对象创建ImageSpan对象
            ImageSpan imageSpan = new ImageSpan(et.getContext(), bitmap);
            //  创建一个SpannableString对象，以便插入用ImageSpan对象封装的图像
            String imgFace = et.getResources().getResourceEntryName(faceId);
            SpannableString spannableString = new SpannableString(imgFace);
            //  用ImageSpan对象替换face
            spannableString.setSpan(imageSpan, 0, imgFace.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //  将图像插入到EditText控件光标的位置
            pasteSpanText(et, spannableString);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /** 获取图文混排*/
    public static void spanText(TextView et, String spanString) {
        SpannableString ss = new SpannableString(spanString);
        Pattern p=Pattern.compile("smiley_\\d{1,3}_");
        Matcher m=p.matcher(spanString);
        Resources res = et.getResources();
        int faceId = 0;
        String igmStr = null;
        Field field = null;
        Bitmap bitmap = null;
        ImageSpan span = null;
        
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
            bitmap = BitmapFactory.decodeResource(res, faceId);
            if(bitmap == null) {
                continue;
            }
            span = new ImageSpan(et.getContext(), bitmap);
            ss.setSpan(span, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        et.setText(ss);
    }
    
    /** 获取图文混排*/
    public static void spanText2(TextView et, String spanString) {
        SpannableString ss = new SpannableString(spanString);
        Pattern p=Pattern.compile("f\\d{1,3}");
        Matcher m=p.matcher(spanString);
        Resources res = et.getResources();
        int faceId = 0;
        String igmStr = null;
        Field field = null;
        ImageSpan span = null;
        GifDrawable drawable = null;
        
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
                drawable = new GifDrawable(res, faceId);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            }catch(Exception e) {
                e.printStackTrace();
            }
            if(drawable == null) {
                continue;
            }
            span = new ImageSpan(drawable);
            ss.setSpan(span, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        et.setText(ss);
    }
    
    /** 模拟DEL键*/
    public static void animDelDown(EditText et) {
        KeyEvent keyEventDown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);  
        et.onKeyDown(KeyEvent.KEYCODE_DEL, keyEventDown);       
    }
}