package com.android.util.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import com.android.util.system.Logger;

public class BaseFragmentActivity extends FragmentActivity {

    protected ProgressDialog loadingDialog = null;
    protected boolean isResumed = false;
    protected  Dialog mDialog;
    
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Logger.d("-->onCreate");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
    }
    
    @Override
    protected void onPause() {
        isResumed = false;
        dismissLoading();
        if(mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
            mDialog = null;
        }
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        Logger.d("-->onDestroy");
        super.onDestroy();
    }
    
    public void showLoading(String msg) {
        if (!isResumed) return;
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(this);
        }
        loadingDialog.setMessage(msg);
        loadingDialog.show();
    }
    
    public void dismissLoading() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    /** 任意线程都可显示toast */
    public void showToastInfo(final String text) {
        showToastInfo(text, false);
    }

    /** 任意线程都可显示toast */
    public void showToastInfo(final String text, final boolean isLong) {
        if (!isResumed) return;
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(BaseFragmentActivity.this, text,
                        isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    public void manageDialog(Dialog dialog) {
        if(dialog == null) return;      
        this.mDialog = dialog;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends View> T getViewById(int id) {
        View view = findViewById(id);
        return (T) view;
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T inflate(int res) {
        return (T) LayoutInflater.from(this).inflate(res, null);
    }
}
