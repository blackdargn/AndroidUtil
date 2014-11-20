package com.android.util.widget;

import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.util.R;
import com.android.util.location.baidu.BDLocationProvider;
import com.android.util.location.baidu.BDLocationProvider.OnLocationListener;
import com.android.util.location.google.LowPowerLocationProvider.OnLocationChangedListener;
import com.android.util.thread.NotifyListener;
import com.baidu.mapapi.search.MKAddrInfo;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-12-31
 * @see : 当前位置视图
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class CurLocView extends LinearLayout 
implements View.OnClickListener, OnLocationListener
{
    
    private BDLocationProvider locProvider;
    private OnLocationChangedListener listener;
    private NotifyListener<MKAddrInfo> addrListener;
    
    private TextView address;
    private ImageView address_refresh;
    private ProgressBar loc_loading;
    
    public CurLocView(Context context)
    {
        this(context,null);
    }
    
    public CurLocView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initView();
    }
    
    public void startLocation()
    {
        locProvider.setRealOnLocationListener(this);
        updateViews();
    }
    
    public void stopLocation()
    {
        locProvider.setRealOnLocationListener(null);
    }
    
    private void initView()
    {
        locProvider = BDLocationProvider.getInstance();
        View row = inflate(getContext(), R.layout.view_curloc_info, null);
        addView(row,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        
        address = (TextView)findViewById(R.id.address);
        address_refresh = (ImageView)findViewById(R.id.address_refresh);
        loc_loading = (ProgressBar)findViewById(R.id.loc_loading);
        
        setOnClickListener(this);
        address_refresh.setOnClickListener(this);                
    }
    
    public void getLastLocation(OnLocationChangedListener listener, int timeout)
    {
        this.listener = listener;
        locProvider.startLocation(true);
        post(new Runnable()
        {
            @Override
            public void run()
            {
                address.setText("正在定位...");        
                startAnim();
            }
        });
    }
    
    public void getLastLocation(OnLocationChangedListener listener)
    {
        getLastLocation(listener, 20000);
    }
    
    public void setAddrNotifyListener(NotifyListener<MKAddrInfo> listener)
    {
        this.addrListener = listener;
    }

    @Override
    public void onClick(View v)
    {
        address.setText("正在定位...");
        locProvider.startLocation(true);
        startAnim();
    }
    
    private void updateViews()
    {
        MKAddrInfo addr = locProvider.getMyAddrInfo();
        if(locProvider.isLocating())
        {
            address.setText("正在定位...");
            startAnim();
        }else
        if(addr != null)
        {
            address.setText(addr.strAddr);
            stopAnim();
        }else
        {
            if(locProvider.getMyLoation() == null)
            {             
                address.setText("正在定位...");
                locProvider.startLocation(true);              
            }else
            {
                address.setText("定位成功,正在获取地址...");              
                locProvider.startGetMyAddr();
            }
            startAnim();
        }
    }
        
    private void startAnim()
    {       
        address_refresh.setVisibility(View.GONE);
        loc_loading.setVisibility(View.VISIBLE);        
    }
    
    private void stopAnim()
    {
        address_refresh.setVisibility(View.VISIBLE);
        loc_loading.setVisibility(View.GONE);
    }

    @Override
    public void onError()
    {
        address.setText("定位失败！");
        stopAnim();
        if(listener != null)
        {
            listener.onLocationChanged(null);
        }
    }

    @Override
    public void onLocation(Location loc)
    {
        address.setText("定位成功,正在获取地址...");
        if(listener != null)
        {
            listener.onLocationChanged(loc);
        }
    }

    @Override
    public void onGetAddr(MKAddrInfo addr)
    {
        if(addr != null)
        {
            address.setText(addr.strAddr);            
        }else
        {
            address.setText("获取地址失败！");
        }
        stopAnim();
        if(addrListener != null)
        {
            addrListener.notify(addr, true);
        }
    }
}