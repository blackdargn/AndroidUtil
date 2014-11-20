package com.android.util.map.baidu;

import android.location.Location;
import android.os.Bundle;

import com.android.util.R;
import com.android.util.activity.TemplateActivity;
import com.android.util.location.baidu.BDLocationProvider;
import com.android.util.location.baidu.BDLocationProvider.OnLocationListener;
import com.android.util.location.baidu.LocationUtil;
import com.android.util.system.Util;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.search.MKAddrInfo;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-8-6
 * @see : 地图基类， 1.提供我的位置显示
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class MapActivity extends TemplateActivity implements OnLocationListener
{
    protected static final String TAG = "MapActivity";
    
    protected MapController mMapController;
    protected MapView mMapView;
    private   MyLocationOverlay mLocationOverlay;
    protected Location mMyLocation;
    protected MKAddrInfo mMyAddress;
    protected BDLocationProvider mMapManager;
    
    @Override
    protected void onCreate(Bundle arg0)
    {
        super.onCreate(arg0);
        mMapManager = BDLocationProvider.getInstance();
        setContentView(R.layout.activity_map);
        // 初始化地图元素
        initMapViews();
    }
    
    @Override
    protected void onResume()
    {      
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause()
    {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        mMapView.destroy();
        super.onDestroy();
    }

    private void initMapViews()
    {
        this.mMapView = ((MapView) findViewById(R.id.map_view));
        this.mMapView.setBuiltInZoomControls(true);
        this.mMapController = this.mMapView.getController();
        this.mMapController.setZoom(12);
    }
    
    protected void reLocation(boolean updateable)
    {
        if(!updateable && mMapManager.getMyLoation() != null)
        {
            // 当前不用在请求
            mMyLocation = mMapManager.getMyLoation();
            mMyAddress =  mMapManager.getMyAddrInfo();
        }else
        {
            mMyLocation = null;
            mMyAddress = null;
            mMapManager.setRealOnLocationListener(this);
            mMapManager.startLocation(true);
        }
    }

    protected void showMyPosition(boolean visilable)
    {
        showMyPosition(visilable, true);
    }
    
    protected void showMyPosition(boolean visilable, boolean isMoveto)
    {
        if(visilable)
        {
            if(mLocationOverlay == null)
            {
                this.mLocationOverlay = new MyLocationOverlay(mMapView);
                this.mMapView.getOverlays().add(this.mLocationOverlay);
                this.mMapView.refresh();
            }
            mLocationOverlay.setLocationMode(com.baidu.mapapi.map.MyLocationOverlay.LocationMode.NORMAL);            
        }else
        {
            if(mLocationOverlay != null)
            {
                mLocationOverlay.setLocationMode(com.baidu.mapapi.map.MyLocationOverlay.LocationMode.COMPASS);
            }
        }
    }
    
    protected void moveToMyLocation()
    {
        if(mLocationOverlay != null && mLocationOverlay.getMyLocation() != null)
        {
            Location location = new Location("");
            location.setLatitude(mLocationOverlay.getMyLocation().latitude);
            location.setLongitude(mLocationOverlay.getMyLocation().longitude);
            LocationUtil.moveTo(mMapView, location, true, false);
        }else
        {
            Util.showToast(this, R.string.locating);
        }
    }
    
    @Override
    public void onError()
    {
        mMyLocation = null;
        mMyAddress = null;
        Util.showToast(this, R.string.loc_not_found);
    }

    @Override
    public void onLocation(Location loc)
    {
        mMyLocation = loc;
        mMyAddress = null;
    }

    @Override
    public void onGetAddr(MKAddrInfo addr)
    {
        mMyAddress = addr;
    }
}
