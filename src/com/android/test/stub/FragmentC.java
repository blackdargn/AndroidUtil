package com.android.test.stub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.util.R;
import com.android.util.fragment.BaseFragment;
import com.android.util.widget.CUTabHost.OnTabPrePauseListener;
import com.baidu.mapapi.map.MapView;

public class FragmentC extends BaseFragment implements OnTabPrePauseListener{

    private MapView bmapView;
    private FrameLayout mapLay;
    private View mapMask;
    
    protected int getLayoutId() {
        return R.layout.activity_map;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), null);
        mapLay = (FrameLayout)view.findViewById(R.id.mapLay);
        mapLay.addView(bmapView = new MapView(getActivity()), 0);
        mapMask = view.findViewById(R.id.mapMask);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    
    @Override
    public void _onResume() {
        super._onResume();        
        bmapView.onResume();
        mapMask.setVisibility(View.GONE);
        bmapView.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onTabPrePause() {
        mapMask.setVisibility(View.VISIBLE);
        mapMask.postDelayed(new Runnable() {
            @Override
            public void run() {
                bmapView.setVisibility(View.GONE);
            }
        }, 100);
    }
    
    @Override
    public void onPause() {
        bmapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        bmapView.destroy();
        super.onDestroy();
    }
    
    protected String getLogTag() {
        return "FragmentC";
    }
}
