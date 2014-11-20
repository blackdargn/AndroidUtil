package com.android.util.widget;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class AdjustTextView extends TextView {

    public AdjustTextView(Context context) {
        super(context);
    }
    
    public AdjustTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start,
            int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);       
        int width = getWidth();
        if(width == 0) {
            return;
        }else {
            TextPaint paint = getPaint();
            String str = text.toString();
            boolean change = false;
            int strWidth = (int)paint.measureText(str);
            if(strWidth > width) {
                width -=  (int)paint.measureText("...");
            }
            while(strWidth > width) {
                str = str.substring(0, str.length()-1);
                strWidth = (int)paint.measureText(str);
                change = true;
            }
            
            if(change ) {
                setText(str +"...");
                Log.d(VIEW_LOG_TAG, "--> adjust text = " + str);
            }
        }
    }
}