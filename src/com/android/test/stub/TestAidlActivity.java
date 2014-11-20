package com.android.test.stub;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.util.R;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-10-25
 * @see : 测试Aidl服务
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class TestAidlActivity extends Activity
{
    private AccountImpl mAccountImpl;
    private AccountImplConn mAccountImplConn;
    private TextView showResult;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.test_aidl);
        
        showResult = (TextView)findViewById(R.id.showResult);
    }
    
    @Override
    protected void onDestroy()
    {
        disConnect(null);
        super.onDestroy();
    }
    
    public void getName(View v)
    {
        connect(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    showResult.setText(mAccountImpl.getName());
                } catch (RemoteException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
    
    public void getNum(View v)
    {
        connect(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    int type = Integer.parseInt(((EditText)findViewById(R.id.num1)).getText().toString());
                    showResult.setText(mAccountImpl.getNum(type) + "");
                } catch (RemoteException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
    
    private boolean connect(Runnable callback)
    {
        if(mAccountImpl == null)
        {
            mAccountImplConn = new AccountImplConn();
            mAccountImplConn.mCallBack = callback;
            bindService(new Intent("android.intent.action.account_impl"), mAccountImplConn, BIND_AUTO_CREATE);
            return false;
        }else
        {
            if(callback != null)
            {
                callback.run();
            }
            return true;
        }
    }
    
    private boolean disConnect(Runnable callback)
    {
        if(mAccountImpl != null && mAccountImplConn != null)
        {
            mAccountImplConn.mCallBack = callback;
            unbindService(mAccountImplConn);
            return false;
        }else
        {
            if(callback != null)
            {
                callback.run();
            }
            return true;
        }
    }
    
    private class AccountImplConn implements ServiceConnection
    {
        public Runnable mCallBack;
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mAccountImpl = AccountImpl.Stub.asInterface(service);
            
            if(mCallBack != null)
            {
                runOnUiThread(mCallBack);
            }
            
            if(mAccountImpl instanceof AccountImpl.Stub)
            {
                Log.d("", "Local process call");
            }else
            {
                Log.d("", "remote process call");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mAccountImpl = null;
        }
    };
}
