package pl.droidsonroids.gif;

import android.content.Context;
import android.text.method.MovementMethod;
import android.util.AttributeSet;

public class FaceTextView extends FaceEditText {
    
    public FaceTextView(Context context) {
        this(context, null, 0);
    }
    
    public FaceTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.style.Widget_TextView);
    }
    
    public FaceTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    protected boolean getDefaultEditable() {
        return false;
    }

    protected MovementMethod getDefaultMovementMethod() {
        return null;
    }
}