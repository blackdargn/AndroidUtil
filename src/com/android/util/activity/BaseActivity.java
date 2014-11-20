package com.android.util.activity;

import com.android.util.system.Log;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.Toast;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-8-6
 * @see : 基础Activity
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class BaseActivity extends Activity
{
    private ProgressDialog progress;
	private Dialog         dialog;
	private PopupWindow    popwin;
	private WakeLock wakeLock;
	private boolean isResumed = false;
	protected boolean isNeedLogin = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume()
	{	    
	    super.onResume();
	    isResumed = true;
	}
	
	@Override
	protected void onPause()
	{
	    isResumed = false;
		hideLoding();
		hideDialog();
		hidePopWin();
		super.onPause();
	}
    	
	@Override
    public void onBackPressed() 
    {
	    hidePopWin();
	    super.onBackPressed();
    }
	
	@SuppressWarnings("unchecked")
    protected <T extends View> T getViewById(int id)
    {
        View view = findViewById(id);
        return (T)view;
    }
	
	@SuppressWarnings("unchecked")
    protected <T extends View> T inflate(int res)
    {
        return (T)LayoutInflater.from(this).inflate(res, null);
    }
    
	/** 设置禁止锁屏，在setContentView前调用*/
	protected void setUnLockScreen()
	{
		// 屏幕常亮，禁止锁屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	/** 设置屏幕的状态 可和setUnLockScreen功能相同 */
	protected void acquireWakeLock()
	{
		if (wakeLock == null)
		{
			/**
			PARTIAL_WAKE_LOCK:保持CPU 运转，屏幕和键盘灯有可能是关闭的。
			SCREEN_DIM_WAKE_LOCK：保持CPU 运转，允许保持屏幕显示但有可能是灰的，允许关闭键盘灯
			SCREEN_BRIGHT_WAKE_LOCK：保持CPU 运转，允许保持屏幕高亮显示，允许关闭键盘灯
			FULL_WAKE_LOCK：保持CPU 运转，保持屏幕高亮显示，键盘灯也保持亮度
			*/
			Log.d("weak lock", "Acquiring wake lock");
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, this.getClass().getCanonicalName());
			wakeLock.acquire();
		}
	}

	protected void releaseWakeLock()
	{
		if (wakeLock != null && wakeLock.isHeld())
		{
			wakeLock.release();
			wakeLock = null;
		}
	}

	/** 显示进度窗体*/
    public void showLoding(final String title, final String message)
    {
        showLoding(title, message, null);
    }
    
    /** 显示进度窗体,并可监听取消的事件*/
    public void showLoding(final String title, final String message, final OnCancelListener listener) 
    {
        if(!isResumed) return;
        runOnUiThread(new Runnable() 
        {
            public void run() 
            {
                if (progress == null) 
                {
                    progress = ProgressDialog.show(BaseActivity.this, title, message);
                    progress.setCancelable(true);
                } else 
                {
                    progress.setTitle(title);
                    progress.setMessage(message);                    
                }
                progress.setOnCancelListener(listener);
            }
        });
    }
    
    /** 关闭进度窗体*/
    public void hideLoding() 
    {
        if (progress == null)
            return;
        runOnUiThread(new Runnable() 
        {
            public void run() 
            {
                if (progress != null) 
                {
                    progress.dismiss();
                    progress = null;
                }
            }
        });
    }
    
    /** 进度窗体是否在展示*/
    public boolean isLodingShow()
    {
        return (progress != null && progress.isShowing());
    }
    
    /** 显示Toast*/
    public void showToast(final String msg) 
    {
        runOnUiThread(new Runnable() 
        {
            public void run() 
            {
                Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }
    /** 显示Toast*/
    public void showToast(final int msg)
    {
        runOnUiThread(new Runnable() 
        {
            public void run() 
            {
                Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /** 显示自定义弹出窗体*/
	public void showDialog(String title, String msg,View contentView,
            String cancelbtnName, DialogInterface.OnClickListener cancelbtnListener,
            String okbtnName,DialogInterface.OnClickListener okbtnListener)
	{	    
	    // 先关闭之前的窗体
	    hideDialog();
	    if(!isResumed) return;
	    // 在创建新的窗体
	    Builder builder = new AlertDialog.Builder(this);
        if(title != null)
        {
            builder.setTitle(title);
        }
        if(msg != null)
        {
            builder.setMessage(msg);
        }
        if(cancelbtnName != null)
        {
            builder.setNegativeButton(cancelbtnName,cancelbtnListener);
        }
        if(okbtnName != null)
        {
            builder.setPositiveButton(okbtnName, okbtnListener);
        }
        if(contentView != null)
        {
            builder.setView(contentView);
        }
                
        dialog = builder.create();
        dialog.show();
	}
	
	public void showDialog(String title, String msg,
            int cancelbtnName, int okbtnName,
            DialogInterface.OnClickListener okbtnListener)
	{
	    showDialog(title,msg,null,getString(cancelbtnName),null,getString(okbtnName),okbtnListener);
	}
	
	public void showDialog(String title, String msg,
            String cancelbtnName, String okbtnName,
            DialogInterface.OnClickListener okbtnListener)
	{
	    showDialog(title,msg,null,cancelbtnName,null,okbtnName,okbtnListener);
	}
	
	public void showDialog(String title, String msg,
            int okbtnName, DialogInterface.OnClickListener okbtnListener)
	{
	    showDialog(title,msg,null,null,null,getString(okbtnName),okbtnListener);
	}
	
	public void showDialog(String title, View v,
	        String cancelbtnName,String okbtnName, 
	        DialogInterface.OnClickListener okbtnListener)
    {
        showDialog(title,null,v,cancelbtnName,null,okbtnName,okbtnListener);
    }
	
	/** 显示弹出窗体的activity，即窗口消失时关闭activity*/
	public void showDialogActivity(String title, View v,
            String cancelbtnName,String okbtnName, 
            DialogInterface.OnClickListener okbtnListener)
    {
	    showDialog(title,null,v,cancelbtnName,null,okbtnName,okbtnListener);
	    if(dialog !=null)
	    {
    	    dialog.setOnDismissListener(new OnDismissListener() 
    	    {
                @Override
                public void onDismiss(DialogInterface dialog) 
                {
                    finish();
                }
            });
	    }
    }
	
	/** 管理其它窗体 */
	public void showDialog(Dialog dialog)
	{	    
        if(dialog != null)
        {
            // 先关闭之前的窗体
            hideDialog();
            this.dialog = dialog;
            // 没有显现，则显现
            if(!dialog.isShowing())
            {
                dialog.show();
            }
        }
	}
	
	/** 关闭自定义弹出窗体 */   
	private void hideDialog()
	{
	    if(dialog != null && dialog.isShowing())
        {
            dialog.dismiss();
            dialog = null;
        }
	}
	
	/** 持有PopWin*/
	public void setPopWin(PopupWindow popwin)
	{
	    if(popwin != null)
	    {
	        this.popwin = popwin;
	    }
	}
	
	/** 获取PopWin*/
	public PopupWindow getPopupWin()
	{
	    return popwin;
	}
	
	/** 关闭PopWin*/
	public void hidePopWin()
    {
        if(popwin != null && popwin.isShowing())
        {
            popwin.dismiss();
        }
    }
}
