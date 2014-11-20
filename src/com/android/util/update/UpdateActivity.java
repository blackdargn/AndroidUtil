package com.android.util.update;

import java.text.DecimalFormat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.util.R;
import com.android.util.activity.BaseActivity;
import com.android.util.update.Downloader.OnDownListener;
import com.android.util.update.UpdateManager.UpdateInfo;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-8-21
 * @see : 更新activity
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class UpdateActivity extends BaseActivity implements OnClickListener,OnDownListener
{
    private static final String TAG_UPDATEINFO = "_updateinfo";
    private static final String TAG_UPDATEMODE = "_updatemode";
    
    private static enum UPDATE_MODE
    {
        /** 下载进度*/
        MODE_PROCESS,
        /** 必须升级*/
        MODE_NEED,
        /** 可以升级*/
        MODE_HAVE
    }
    private UPDATE_MODE mode = UPDATE_MODE.MODE_PROCESS;    
    // 更新进度 ui members
    private TextView down_tip;
    private TextView done_txt;
    private TextView total_txt;
    private ProgressBar processbar;
    private Button cancel;  
    // 更新提示 ui members
    private TextView version_name;
    private TextView time;
    private TextView size;
    private TextView info;
    
    private UpdateInfo updateInfo;
    private String downProcessTxt = "正在下载安装包，此步骤将比较耗时，已下载 {0}%";
    private View  dialogView;
    private DecimalFormat floatFormater;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);       
        // 无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(R.color.background);
        if(!handleIntent())
        {
            finish();
            return;
        }        
        int layId = mode == UPDATE_MODE.MODE_PROCESS ? R.layout.dialog_update_process : R.layout.dialog_update_tip;        
        dialogView = getLayoutInflater().inflate(layId, null);
        floatFormater = new DecimalFormat("0.00");
        setupViews();
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        
        if(mode == UPDATE_MODE.MODE_PROCESS)
        {
            UpdateManager.getInstacnce(this).setOnDownListener(this);
            showDialogActivity("软件更新", dialogView, null, null, null);
        }else 
        {
            showDialogActivity("更新提示", dialogView, 
                    mode == UPDATE_MODE.MODE_NEED?null:"以后再说", 
                    mode == UPDATE_MODE.MODE_NEED?"确定":"更新",
                    new DialogInterface.OnClickListener() 
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) 
                        {
                            startUpdate();
                            dialog.dismiss();
                        }
                    });
        }
    }
    
    @Override
    protected void onPause()
    {
        if(mode == UPDATE_MODE.MODE_PROCESS)
        {
            UpdateManager.getInstacnce(this).setOnDownListener(null);
        }
        super.onPause();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {            
            
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public void onClick(View v)
    {
        if(v == cancel) {
            UpdateManager.getInstacnce(getApplicationContext()).cancelUpdate();
            finish();
        }
    }
    
    private void setupViews()
    {        
        if(mode == UPDATE_MODE.MODE_PROCESS)
        {
            down_tip = (TextView)dialogView.findViewById(R.id.down_tip);
            done_txt = (TextView)dialogView.findViewById(R.id.done_txt);
            total_txt = (TextView)dialogView.findViewById(R.id.total_txt);
            
            processbar = (ProgressBar)dialogView.findViewById(R.id.processbar);
            cancel = (Button)dialogView.findViewById(R.id.cancel);
            
            cancel.setOnClickListener(this);
            
            processbar.setProgress(0);
            down_tip.setText(downProcessTxt.replace("{0}", 0+""));
        }else
        {
            version_name = (TextView)dialogView.findViewById(R.id.version_name);
            time = (TextView)dialogView.findViewById(R.id.time);
            size = (TextView)dialogView.findViewById(R.id.size); 
            info = (TextView)dialogView.findViewById(R.id.info);
            
            version_name.setText(updateInfo.versionName);
            time.setText(updateInfo.time);
            size.setText(updateInfo.size);
            info.setText(updateInfo.desc);
        }
    }

    private boolean handleIntent()
    {
        Intent it = getIntent();
        mode = (UPDATE_MODE)it.getSerializableExtra(TAG_UPDATEMODE);      
        if(mode == UPDATE_MODE.MODE_NEED)
        {
            updateInfo = (UpdateInfo)it.getSerializableExtra(TAG_UPDATEINFO);            
            return (updateInfo != null);
        }
        else
        if(mode == UPDATE_MODE.MODE_HAVE)
        {
            updateInfo = (UpdateInfo)it.getSerializableExtra(TAG_UPDATEINFO);            
            return (updateInfo != null);
        }else
        {
            mode = UPDATE_MODE.MODE_PROCESS;
        }       
        return true;
    }
    
    private void startUpdate()
    {
        String saveDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        saveDir += "/update/";
        UpdateManager.getInstacnce(this).startUpdate(updateInfo.path, saveDir, this);
        startProcessActivity(this);
        finish();
    }
    
    /** 格式化文件的大小为MB，KB，B*/
    public String formatByte(int bytes)
    {        
        float r = (bytes+0.0f)/(1<<20);
        if(r > 1.0f)
        {
            // MB
            return floatFormater.format(r) + "MB";
        }else
        {
            r = (bytes+0.0f)/(1<<10);
            if(r > 1.0f)
            {
                // KB
                return floatFormater.format(r) + "KB";
            }else
            {
                // B
                return bytes + "B";
            }
        }
    }
    
    @Override
    public void onStart(final int size)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(total_txt != null)
                {
                    total_txt.setText(formatByte(size));
                    done_txt.setText("");
                    processbar.setProgress(0);
                }
            }
        });
    }

    @Override
    public void onProcess(final int done_size, final int size)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(total_txt != null)
                {
                    int done = done_size;
                    int totle = size;
                    int donePrecent = (int)(((done+0.0f)/totle)*100);
                    
                    total_txt.setText(formatByte(totle));
                    done_txt.setText(formatByte(done));
                    down_tip.setText(downProcessTxt.replace("{0}", donePrecent+""));
                    
                    processbar.setProgress(donePrecent);
                }
            }
        });
    }
    
    @Override
    public void onFail()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(UpdateActivity.this, "更新失败！", Toast.LENGTH_LONG).show();
            }
        });
    }
    
    public static void startNeedActivity(Context context, UpdateInfo info)
    {
        Intent it = new Intent(context, UpdateActivity.class);
        it.putExtra(TAG_UPDATEMODE, UPDATE_MODE.MODE_NEED);
        it.putExtra(TAG_UPDATEINFO, info);
        context.startActivity(it);
    }
    
    public static void startProcessActivity(Context context)
    {
        Intent it = new Intent(context, UpdateActivity.class);
        it.putExtra(TAG_UPDATEMODE, UPDATE_MODE.MODE_PROCESS);
        context.startActivity(it);
    }
    
    public static void startHaveActivity(Context context, UpdateInfo info)
    {
        Intent it = new Intent(context, UpdateActivity.class);
        it.putExtra(TAG_UPDATEMODE, UPDATE_MODE.MODE_HAVE);
        it.putExtra(TAG_UPDATEINFO, info);
        context.startActivity(it);
    }
}
