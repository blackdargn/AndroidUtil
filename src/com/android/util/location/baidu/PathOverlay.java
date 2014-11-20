package com.android.util.location.baidu;

import java.util.ArrayList;
import java.util.List;

import com.android.util.location.baidu.LocationUtil.GeoPointRender;
import com.android.util.system.Logger;
import com.android.util.system.MyApplication;
import com.baidu.mapapi.map.Geometry;
import com.baidu.mapapi.map.Graphic;
import com.baidu.mapapi.map.GraphicsOverlay;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Symbol;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class PathOverlay extends GraphicsOverlay {

    protected  List<GeoPointRender> posItemsList = new ArrayList<GeoPointRender>();
    private Symbol lineSymbol;
    private MapView mMapView;
    
    public PathOverlay(MapView mapView) {
        super(mapView);
        
        mMapView = mapView;
        
        //�趨��ʽ
        lineSymbol = new Symbol();
        Symbol.Color lineColor = lineSymbol.new Color();
        lineColor.red = 0xff;
        lineColor.green = 0x46;
        lineColor.blue = 0xf7;
        lineColor.alpha = 0xff;
        lineSymbol.setLineSymbol(lineColor, (int)(4*MyApplication.getContext().getDensity()));
    }
    
    public void setList(List<PathOverlayItem> list) {
        posItemsList.clear();
        if(list != null){
            for(PathOverlayItem item: list){
                posItemsList.add(item);
            }
        }
        populate();
    }
    
    public void add(PathOverlayItem item) {
        posItemsList.add(item);
        populate();
    }
    
    public void clearPath()
    {
        posItemsList.clear();
        removeAll();
        mMapView.refresh();
    }
    
    protected void populate() {
        removeAll();
        List<List<GeoPoint>> ps = LocationUtil.filterPaths(mMapView, posItemsList);
        if(ps != null && !ps.isEmpty()){
            for(List<GeoPoint> p : ps){
                if(p != null && p.size() > 1){
                    int size = p.size();
                    Logger.d("--> draw path size = " + size );
                    Geometry lineGeometry = new Geometry();
                    GeoPoint[] linePoints = new GeoPoint[size];
                    p.toArray(linePoints);
                    lineGeometry.setPolyLine(linePoints);
                    Graphic lineGraphic = new Graphic(lineGeometry, lineSymbol);
                    setData(lineGraphic);
                    Logger.d("--> draw path end" );
                }
            }
        }
        mMapView.refresh();
    }
}