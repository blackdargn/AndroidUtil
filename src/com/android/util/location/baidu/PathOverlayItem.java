package com.android.util.location.baidu;

import com.android.util.location.baidu.LocationUtil.GeoPointRender;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class PathOverlayItem extends OverlayItem implements GeoPointRender{

    public PathOverlayItem(GeoPoint arg0, String arg1, String arg2) {
        super(arg0, arg1, arg2);
    }
    
    @Override
    public GeoPoint getPoint() {
        return super.getPoint();
    }
}