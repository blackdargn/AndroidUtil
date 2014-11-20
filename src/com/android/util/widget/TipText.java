package com.android.util.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.util.R;
/*********************************************************
 * @author : zhaohua
 * @version : 2012-8-6
 * @see : 自定义Toast封装
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class TipText
{
    private final Context mContext;

    private int mDuration;

    private LayoutInflater mInflater;

    private LinearLayout mLayout;

    private String mMessage;

    private Toast mToast = null;

    private TextView mTxtMessage;

    public TipText(Context paramContext)
    {
        this.mContext = paramContext;
        this.mMessage = this.mContext.getString(R.string.loading_err);
        this.mDuration = 0;
    }

    public TipText(Context paramContext, String paramString)
    {
        this.mContext = paramContext;
        this.mMessage = paramString;
        this.mDuration = 0;
    }

    public static TipText createTipText(Context paramContext, int paramInt1,
            int paramInt2)
    {
        TipText localTipText = new TipText(paramContext,
                paramContext.getString(paramInt1));
        localTipText.setDuration(paramInt2);
        return localTipText;
    }

    public static TipText createTipText(Context paramContext,
            String paramString, int paramInt)
    {
        TipText localTipText = new TipText(paramContext, paramString);
        localTipText.setDuration(paramInt);
        return localTipText;
    }

    private void initialToast()
    {
        this.mInflater = LayoutInflater.from(mContext);
        this.mLayout = ((LinearLayout) this.mInflater.inflate(R.layout.view_tiptext, null));
        this.mTxtMessage = ((TextView) this.mLayout.findViewById(R.id.tiptext_text));
        this.mTxtMessage.setText(this.mMessage);
        this.mToast = new Toast(this.mContext.getApplicationContext());
        this.mToast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, this.mContext.getResources()
                .getDimensionPixelSize(R.dimen.tiptext_bottom_margin));
        this.mToast.setView(this.mLayout);
        this.mToast.setDuration(this.mDuration);
    }

    public void setDuration(int paramInt)
    {
        this.mDuration = paramInt;
    }

    public void setMessage(String paramString)
    {
        this.mMessage = paramString;
    }

    public void show()
    {
        initialToast();
        this.mToast.show();
    }

    public void show(int paramInt)
    {
        this.mDuration = paramInt;
        show();
    }
}