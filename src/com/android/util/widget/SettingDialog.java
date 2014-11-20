package com.android.util.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.util.R;
import com.android.util.system.AppHelper;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-12-26
 * @see : 设置弹出通用Dialog
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class SettingDialog extends Dialog implements android.view.View.OnClickListener
{
    public static final int BTN_OK     = 0;
    public static final int BTN_CANCEL = 1;
    private DialogListener<?> mClickListener;
    private EditText edit_input;
    private RadioGroup radio_group;
    private Button btn_ok;
    private Button btn_cancel;
    
    private LinearLayout dg_content_lay;
    private View dg_lay;
    private TextView dg_title;
    private TextView dg_message;
    private View dg_btn_lay;
    /** 是否是提示框：提示框：黑色的主题，输入框：灰白色的主题*/
    private boolean isTipMode = true;
    
    private SettingDialog(Context context, 
            int theme, 
            boolean istip)
    {
        super(context, theme);
        this.isTipMode = istip;
        setContentView(R.layout.popview_setting);
        initViews();
    }
    
    private void initViews()
    {
        btn_ok = (Button)findViewById(R.id.btn_ok);
        btn_cancel = (Button)findViewById(R.id.btn_cancel);
        edit_input = (EditText)findViewById(R.id.edit_input);
        radio_group = (RadioGroup)findViewById(R.id.radio_group);
        
        dg_content_lay = (LinearLayout)findViewById(R.id.dg_content_lay);
        dg_lay = findViewById(R.id.dg_lay);
        dg_title = (TextView)findViewById(R.id.dg_title);
        dg_btn_lay = findViewById(R.id.dg_btn_lay);
        dg_message = (TextView)findViewById(R.id.dg_message);
        
        dg_lay.setBackgroundResource(isTipMode ? R.drawable.bg_b_dialog : R.drawable.bg_w_dialog);       
        dg_btn_lay.setBackgroundResource(isTipMode ? R.drawable.bg_dialog_bbtn : R.drawable.bg_dialog_wbtn);
        
        int gray = getContext().getResources().getColor(R.color.dialog_tx_color);
        dg_title.setTextColor(isTipMode ? Color.WHITE : gray);
        
        dg_message.setTextColor(isTipMode ? Color.WHITE : gray);
        btn_ok.setTextColor(isTipMode ? Color.WHITE : gray);
        btn_cancel.setTextColor(isTipMode ? Color.WHITE : gray);
        
        btn_ok.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }
    
    public SettingDialog setEditMode(String title,
            String hintText, 
            String text, 
            int inputtype, 
            int maxLen,
            int maxLines,
            DialogListener<String> listener)
    {
        setTitle(title);
        edit_input.setVisibility(View.VISIBLE);
        radio_group.setVisibility(View.GONE);
        dg_btn_lay.setVisibility(View.VISIBLE);
        
        if(!TextUtils.isEmpty(hintText)) 
        {
            edit_input.setHint(hintText);
        }else
        {
            edit_input.setHint("");
        }
        if(maxLen > 0)
        {
            edit_input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLen)});
        }else
        {
            edit_input.setFilters(null);
        }
        if(maxLines > 0)
        {
            edit_input.setSingleLine(false);
            edit_input.setMaxLines(maxLines);
            inputtype |= InputType.TYPE_TEXT_FLAG_MULTI_LINE;
        }else
        {
            edit_input.setSingleLine(true);
        }
        edit_input.setInputType(inputtype);
        if(!TextUtils.isEmpty(text)) 
        {
            edit_input.setText(text);
            CharSequence edit = edit_input.getText();
            if (edit instanceof Spannable) {
                Spannable spanText = (Spannable)edit;
                Selection.setSelection(spanText, edit.length());
            }
        }else
        {
            edit_input.setText("");
        }
        
        mClickListener = listener;
        return this;
    }
    
    public SettingDialog setSingleChoiceItems(String[] items, int checkPos, DialogListener<Integer> listener)
    {
        if(items == null || items.length < 2) return this;
        radio_group.setVisibility(View.VISIBLE);
        dg_btn_lay.setVisibility(View.VISIBLE);
        edit_input.setVisibility(View.GONE);
        mClickListener = listener;
        radio_group.removeAllViews();
        
        int gray = getContext().getResources().getColor(R.color.dialog_tx_color);
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.leftMargin = AppHelper.dip2px(getContext(), 10);
        params.topMargin = AppHelper.dip2px(getContext(), 8);
        params.bottomMargin = AppHelper.dip2px(getContext(), 8);
        
        for(int i = 0; i<items.length; i++) {
            RadioButton item = new RadioButton(getContext());
            item.setId(i);
            item.setText(items[i]);
            item.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            item.setTextColor(isTipMode ? Color.WHITE : gray);
            item.setButtonDrawable(R.drawable.bg_radio);
            item.setLayoutParams(params);
            if(i == checkPos ) {
                item.setChecked(true);
            }
            radio_group.addView(item, params);
        }
        return this;
    }
    
    public SettingDialog setSelectItems(String[] items, DialogListener<String> listener) {
        if(items == null || items.length == 0) return this;
        radio_group.setVisibility(View.VISIBLE);
        edit_input.setVisibility(View.GONE);
        dg_btn_lay.setVisibility(View.GONE);
        dg_title.setTextColor(Color.WHITE);
        dg_lay.setBackgroundResource(R.drawable.bg_op_dialog);
        
        mClickListener = listener;
        radio_group.removeAllViews();
  
        int gray = getContext().getResources().getColor(R.color.dialog_tx_color);
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        
        for(int i = 0; i<items.length; i++) {
            TextView item = new TextView(getContext());
            item.setId(i);
            item.setTag(items[i]);
            item.setText(items[i]);
            item.setPadding(8, 12, 8, 12);
            item.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            item.setTextColor(isTipMode ? Color.WHITE : gray);
            item.setTextSize(18);
            item.setBackgroundResource(R.drawable.bg_cate_item);
            item.setLayoutParams(params);
            radio_group.addView(item,params);            
            if(i != items.length -1) {
                ImageView line = new ImageView(getContext());
                line.setBackgroundResource(R.drawable.ic_input_line);
                line.setLayoutParams(params);
                radio_group.addView(line,params);
            }
            item.setOnClickListener(this);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onClick(View v)
    {
        if(v == btn_ok) {
                    if(edit_input.getVisibility() == View.VISIBLE)
                    {
                        String content = edit_input.getEditableText().toString();
                        if(mClickListener != null) ((DialogListener<String>)mClickListener).onClick(BTN_OK, content, (Dialog)this);
                    }else
                    if(radio_group.getVisibility() == View.VISIBLE)
                    {
                        int id = radio_group.getCheckedRadioButtonId();
                        if(mClickListener != null) ((DialogListener<Integer>)mClickListener).onClick(BTN_OK, id, (Dialog)this);
                    }
                    if(OKListener != null) OKListener.onClick(this, DialogInterface.BUTTON_POSITIVE);
            }
        else if(v == btn_cancel)
            {
                if(mClickListener != null) mClickListener.onClick(BTN_CANCEL, null, (Dialog)this);
                if(cancelListener != null) cancelListener.onClick(this, DialogInterface.BUTTON_NEGATIVE);
                dismiss();
            }
        else{
                // 非动作按钮点击
                if(mClickListener != null) {
                      ((DialogListener<String>)mClickListener).onClick(BTN_OK, (String)v.getTag(), (Dialog)this);
                }
                dismiss();
            }
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mClickListener != null) mClickListener.onClick(BTN_CANCEL, null, (Dialog)this);
        if(cancelListener != null) cancelListener.onClick(this, DialogInterface.BUTTON_NEGATIVE);
    }
    
    @Override
    public void setTitle(CharSequence title) {
        if(TextUtils.isEmpty(title)) {
            dg_title.setVisibility(View.GONE);
        } else {
            dg_title.setText(title);
            dg_title.setVisibility(View.VISIBLE);
        }
    }
    
    public void setMessage(CharSequence title)
    {
        dg_message.setText(title);
        dg_message.setVisibility(View.VISIBLE);
    }
    
    public void setEditText(String text) {
        edit_input.setText(text);
    }
    
    public void setContentView2(View content) {
        dg_content_lay.removeAllViews();
        dg_content_lay.addView(content, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }
    
    private DialogInterface.OnClickListener cancelListener;
    private DialogInterface.OnClickListener OKListener;
    public void setNegativeButton(String cancelbtnName, DialogInterface.OnClickListener listener)
    {
        if(!TextUtils.isEmpty(cancelbtnName)) {
            btn_cancel.setText(cancelbtnName);
            btn_cancel.setVisibility(View.VISIBLE);
        }else {
            btn_cancel.setVisibility(View.GONE);
        }
        cancelListener = listener;
    }
    
    public void setPositiveButton(String okbtnName, DialogInterface.OnClickListener listener)
    {
        if(!TextUtils.isEmpty(okbtnName)) {
            btn_ok.setText(okbtnName);
            btn_ok.setVisibility(View.VISIBLE);
        }else {
            btn_ok.setVisibility(View.GONE);
        }
        OKListener = listener;
    }
    
    public static interface DialogListener<T>
    {
        public void onClick(int btn, T result, Dialog dialog);
    }

    public static SettingDialog builder(Activity activity, boolean isTip)
    {
        SettingDialog one = new SettingDialog(activity, R.style.Translucent_Dialog, isTip);
        return one;
    }
    
    public static SettingDialog builderTip(Activity activity)
    {
        SettingDialog one = new SettingDialog(activity, R.style.Translucent_Dialog, true);
        return one;
    }
    
    public static SettingDialog builderInput(Activity activity)
    {
        SettingDialog one = new SettingDialog(activity, R.style.Translucent_Dialog, false);
        return one;
    }
    
    public static SettingDialog showDialog(String title, String msg,
            String cancelbtnName, String okbtnName, Activity activity,
            DialogInterface.OnClickListener okbtnListener,
            DialogInterface.OnClickListener cancelbtnListener)
    {
        SettingDialog one = new SettingDialog(activity, R.style.Translucent_Dialog, true);
        one.setTitle(title);
        one.setMessage(msg);
        one.setPositiveButton(okbtnName, okbtnListener);
        one.setNegativeButton(cancelbtnName, cancelbtnListener);
        return one;
    }
    
    public static SettingDialog showDialog(String title, String msg,
            int cancelbtnName, int okbtnName, Activity activity,
            DialogInterface.OnClickListener okbtnListener,
            DialogInterface.OnClickListener cancelbtnListener)
    {
        return showDialog(title, msg, activity.getString(cancelbtnName), activity.getString(okbtnName), activity, okbtnListener, cancelbtnListener);
    }
    
    public static SettingDialog showDialog(String title, View msg,
            String cancelbtnName, String okbtnName, Activity activity,
            DialogInterface.OnClickListener okbtnListener,
            DialogInterface.OnClickListener cancelbtnListener)
    {
        SettingDialog one = new SettingDialog(activity, R.style.Translucent_Dialog, true);
        one.setTitle(title);
        one.setContentView2(msg);
        one.setPositiveButton(okbtnName, okbtnListener);
        one.setNegativeButton(cancelbtnName, cancelbtnListener);
        return one;
    }
    
    public static SettingDialog showDialog(String title, View msg,
            int cancelbtnName, int okbtnName, Activity activity,
            DialogInterface.OnClickListener okbtnListener,
            DialogInterface.OnClickListener cancelbtnListener)
    {
        return showDialog(title, msg, activity.getString(cancelbtnName), activity.getString(okbtnName), activity, okbtnListener, cancelbtnListener);
    }   
}