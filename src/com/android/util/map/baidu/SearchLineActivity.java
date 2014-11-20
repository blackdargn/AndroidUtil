package com.android.util.map.baidu;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.util.R;
import com.android.util.location.baidu.BDLocationProvider;
import com.android.util.location.baidu.BDLocationProvider.BaseSearchListenerImpl;
import com.android.util.location.baidu.LocationUtil;
import com.android.util.system.MyApplication;
import com.android.util.system.Util;
import com.android.util.thread.CycledThread;
import com.android.util.thread.CycledThread.OnTimeoutListener;
import com.android.util.widget.LoadableView;
import com.android.util.widget.TitleBar;
import com.baidu.mapapi.map.RouteOverlay;
import com.baidu.mapapi.map.TransitOverlay;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPlanNode;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-8-8
 * @see : 线路搜索路径展示以及地图线路展示 
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class SearchLineActivity extends MapActivity
{
    protected static final String TAG = "SearchLineActivity";
    
    public static final String TAG_SEARCH_LINE = "_search_line";
    
    private static final String STATUS_BYBUS;
    private static final String STATUS_DRIVING;
    private static final String[] STATUS_INFO_ARRAY;
    private static final String STATUS_WALKING;
    private static final int VIEW_STATUS_LIST = 0;
    private static final int VIEW_STATUS_MAPVIEW = 1;
    static
    {
        STATUS_BYBUS = MyApplication.getContext().getString(R.string.bus_route);
        STATUS_WALKING = MyApplication.getContext().getString(
                R.string.walk_route);
        STATUS_DRIVING = MyApplication.getContext().getString(
                R.string.drive_route);
        STATUS_INFO_ARRAY = new String[3];
        STATUS_INFO_ARRAY[0] = STATUS_DRIVING;
        STATUS_INFO_ARRAY[1] = STATUS_BYBUS;
        STATUS_INFO_ARRAY[2] = STATUS_WALKING;
    }

    private ImageView mChangeImageView;
    private TextView mDistanceInfoTextView;
    private TextView mDistanceTextView;
    private ListView mListView;
    private View mFooterView;
    private View mHeaderView;

    private String mCurStatus = STATUS_BYBUS;
    private String mDistance = "...";
    private ArrayList<String> mList = new ArrayList<String>();
    private MKSearch mMKSearch = new MKSearch();
    private MySearchListener mMySearchListener = new MySearchListener();
    private LoadableView mNormalLoadingView;
    private SearchLineAdapter mSearchLineAdapter;
    private LayoutInflater mInflater;
    private int mViewStatus = VIEW_STATUS_LIST;
    private SearchLine mSearchLineIntentModel;
    private MKPlanNode mStartNode = new MKPlanNode();
    private MKPlanNode mEndNode = new MKPlanNode();

    private CycledThread mTimeLimitTask;
    
    @Override
    protected void onCreate(Bundle arg0)
    {
        if (!handIntent())
        {
            finish();
            return;
        }
        super.onCreate(arg0);
        setContentView( R.layout.activity_search_line);
        initViews();
        search(mCurStatus);
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar)
    {
        titleBar.setTitle(R.string.title_shopline);
        titleBar.setRigthButtonText(R.drawable.see_map);
        titleBar.setOnTitleBarClickListener(titleBar.new OnTitleBarClickListener()
        {
            public void onRightButtonClick()
            {
                if(findViewById(R.id.map_view).getVisibility() == View.VISIBLE)
                {
                    // 地图可见时
                    findViewById(R.id.map_view).setVisibility(View.GONE);
                    findViewById(R.id.content_view).setVisibility(View.VISIBLE);
                    mTitleBar.setRightButtonImage(R.drawable.see_map);
                }else
                {
                    // 地图不可见时
                    findViewById(R.id.content_view).setVisibility(View.GONE);
                    findViewById(R.id.map_view).setVisibility(View.VISIBLE);
                    mTitleBar.setRightButtonImage(R.drawable.see_map_for_list);
                }
            }
        },  false, true);
    }
    
    private boolean handIntent()
    {
        Intent it = getIntent();
        mSearchLineIntentModel = (SearchLine) it.getSerializableExtra(TAG_SEARCH_LINE);
        if (mSearchLineIntentModel == null)
        {
            Util.showToast(this, R.string.data_err);
            return false;
        }
        initDefaultStatus(mSearchLineIntentModel.routeType);        
        this.mStartNode.name = this.mSearchLineIntentModel.startArea;
        this.mStartNode.pt = new GeoPoint(this.mSearchLineIntentModel.startY,this.mSearchLineIntentModel.startX);
        this.mEndNode.name = this.mSearchLineIntentModel.endArea;
        this.mEndNode.pt = new GeoPoint(this.mSearchLineIntentModel.endY,this.mSearchLineIntentModel.endX);
        return true;
    }

    private void initViews()
    {       
        mMKSearch.init(BDLocationProvider.getInstance().getBMapManager(),mMySearchListener);
        this.mInflater = LayoutInflater.from(this);
        this.mNormalLoadingView = (LoadableView)findViewById(R.id.loadView);
        findViewById(R.id.btn01).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.tv_info)).setText(R.string.no_match_route);
        ((TextView) findViewById(R.id.tv_info)).setTextColor(0xFF888888);
        this.mListView = ((ListView) findViewById(R.id.listView));
        this.mChangeImageView = ((ImageView) findViewById(R.id.header_change_img));
        this.mDistanceTextView = ((TextView) findViewById(R.id.header_hint_tv));
        this.mDistanceInfoTextView = ((TextView) findViewById(R.id.header_title_tv));
        this.mChangeImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder localBuilder = new AlertDialog.Builder(
                        SearchLineActivity.this);
                ArrayList<String> localArrayList = new ArrayList<String>();
                for (int i = 0; i < STATUS_INFO_ARRAY.length; i++)
                {
                    if (!STATUS_INFO_ARRAY[i].equals(mCurStatus))
                    {
                        localArrayList.add(STATUS_INFO_ARRAY[i]);
                    }
                }
                final String[] arrayOfString = new String[localArrayList.size()];
                localArrayList.toArray(arrayOfString);
                localBuilder.setTitle(R.string.change_route);
                localBuilder.setItems(arrayOfString, new OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mCurStatus = arrayOfString[which];
                        search(mCurStatus);
                    }
                });
                if (!SearchLineActivity.this.isFinishing())
                {
                    localBuilder.create().show();
                }
            }
        });
        if (this.mCurStatus.equals(STATUS_DRIVING))
        {
            this.mChangeImageView.setImageResource(R.drawable.qi_che_icon);
        } else if (this.mCurStatus.equals(STATUS_BYBUS))
        {
            this.mChangeImageView.setImageResource(R.drawable.gong_jiao_icon);
        } else if (!this.mCurStatus.equals(STATUS_WALKING))
        {
            this.mChangeImageView.setImageResource(R.drawable.bu_xing_icon);
        }
        this.mHeaderView = this.mInflater.inflate(
                R.layout.list_item_search_line, null);
        this.mHeaderView.findViewById(R.id.search_line_img)
                .setBackgroundResource(R.drawable.xian_lu_qi_dian);
        this.mFooterView = this.mInflater.inflate(
                R.layout.list_item_search_line, null);
        this.mFooterView.findViewById(R.id.search_line_img)
                .setBackgroundResource(R.drawable.xian_lu_zhong_dian);

        this.mMapController.setCenter(this.mStartNode.pt);

        ((TextView) this.mHeaderView.findViewById(R.id.search_line_tv))
                .setText(this.mSearchLineIntentModel.startArea);
        ((TextView) this.mFooterView.findViewById(R.id.search_line_tv))
                .setText(this.mSearchLineIntentModel.endArea);
        TextView localTextView = this.mDistanceInfoTextView;
        String str = getString(R.string.from_to_format);
        Object[] arrayOfObject = new Object[2];
        arrayOfObject[0] = this.mSearchLineIntentModel.startArea;
        arrayOfObject[1] = this.mSearchLineIntentModel.endArea;
        localTextView.setText(String.format(str, arrayOfObject));
        this.mSearchLineAdapter = new SearchLineAdapter(this, this.mList);
        this.mListView.addHeaderView(this.mHeaderView);
        this.mListView.setAdapter(this.mSearchLineAdapter);
        this.mListView.addFooterView(this.mFooterView);        
    }

    private void initDefaultStatus(int paramInt)
    {
        switch (paramInt)
        {
        default:
        {
            this.mCurStatus = STATUS_BYBUS;
            break;
        }
        case 1:
        {
            this.mCurStatus = STATUS_BYBUS;
            break;
        }
        case 2:
        {
            this.mCurStatus = STATUS_DRIVING;
            break;
        }
        case 3:
        {
            this.mCurStatus = STATUS_WALKING;
            break;
        }
        }
    }

    public void changeViewStatus()
    {
        if (this.mCurStatus == STATUS_BYBUS)
        {
            mViewStatus = VIEW_STATUS_LIST;
        } else
        {
            mViewStatus = VIEW_STATUS_MAPVIEW;
        }
    }

    public void search(String paramString)
    {
        if (Util.isEmpty(paramString)) { return; }
        changeViewStatus();
        this.mNormalLoadingView.showLoadingView();
        
        stopCheck();
        if (this.mCurStatus.equals(STATUS_DRIVING))
        {
            this.mMKSearch.drivingSearch(this.mSearchLineIntentModel.startCity,
                    this.mStartNode, this.mSearchLineIntentModel.endCity,
                    this.mEndNode);
            this.mChangeImageView.setImageResource(R.drawable.qi_che_icon);
        }
        if (this.mCurStatus.equals(STATUS_BYBUS))
        {
            this.mMKSearch.transitSearch(this.mSearchLineIntentModel.startCity,
                    this.mStartNode, this.mEndNode);
            this.mChangeImageView.setImageResource(R.drawable.gong_jiao_icon);
        }
        if (this.mCurStatus.equals(STATUS_WALKING))
        {
            this.mMKSearch.walkingSearch(this.mSearchLineIntentModel.startCity,
                    this.mStartNode, this.mSearchLineIntentModel.endCity,
                    this.mEndNode);
            this.mChangeImageView.setImageResource(R.drawable.bu_xing_icon);
        }
        startCheck();
    }

    public void showNoResult()
    {
        this.mNormalLoadingView.showLoadingErrView(true);
        ((TextView) findViewById(R.id.tv_info)).setText(R.string.has_no_line);
    }

    public String getDistance(int paramInt)
    {
        return paramInt + getString(R.string.meter);
    }
    
    public void reflash()
    {
        if (this.mList.size() == 0)
        {
            showNoResult();
            return;
        }
        this.mNormalLoadingView.showLoadingView();
        this.mSearchLineAdapter.notifyDataSetChanged();
        this.mDistanceTextView.setText(this.mDistance);
        this.mNormalLoadingView.showMainView();
        if (this.mViewStatus == VIEW_STATUS_LIST)
        {
            findViewById(R.id.content_view).setVisibility(View.VISIBLE);
            findViewById(R.id.map_view).setVisibility(View.GONE);
        } else
        {
            findViewById(R.id.content_view).setVisibility(View.GONE);
            findViewById(R.id.map_view).setVisibility(View.VISIBLE);
        }
    }

    private void showStartPosition()
    {
        LocationUtil.moveTo(mMapView, mStartNode.pt, true, true);
    }
    
    private void stopCheck()
    {
        if(mTimeLimitTask != null)
        {
            mTimeLimitTask._stop();
            mTimeLimitTask = null;
        }
    }
    
    private void startCheck()
    {
        if(mTimeLimitTask == null)
        {
            mTimeLimitTask = new CycledThread(100, 10000, new OnTimeoutListener()
            {
                @Override
                public void onTimeout()
                {
                    mNormalLoadingView.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            showNoResult();
                        }
                    });
                }
            });
            mTimeLimitTask.start();
        }
    }
    
    public class MySearchListener extends BaseSearchListenerImpl
    {
        public MySearchListener()
        {}

        public void onGetDrivingRouteResult(
                MKDrivingRouteResult paramMKDrivingRouteResult, int paramInt)
        {
            stopCheck();
            if ((paramMKDrivingRouteResult == null)
                    || (paramMKDrivingRouteResult.getNumPlan() == 0)
                    || (paramMKDrivingRouteResult.getPlan(0).getNumRoutes() == 0))
            {
                showNoResult();
                return;
            }
            RouteOverlay localRouteOverlay = new RouteOverlay(
                    SearchLineActivity.this, mMapView);
            mMapView.getOverlays().clear();
            localRouteOverlay.setData(paramMKDrivingRouteResult.getPlan(0)
                    .getRoute(0));
            mMapView.getOverlays().add(localRouteOverlay);
            showStartPosition();
            if (paramMKDrivingRouteResult.getNumPlan() > 0)
            {
                mDistance = getDistance(paramMKDrivingRouteResult.getPlan(0)
                        .getDistance());
                if (paramMKDrivingRouteResult.getPlan(0).getNumRoutes() > 0)
                {
                    int i = paramMKDrivingRouteResult.getPlan(0).getRoute(0)
                            .getNumSteps();
                    mList.removeAll(mList);
                    for (int j = 0; j < i; j++)
                    {
                        String str = paramMKDrivingRouteResult.getPlan(0)
                                .getRoute(0).getStep(j).getContent();
                        mList.add(str);
                    }
                }
            }
            reflash();
        }

        public void onGetTransitRouteResult(
                MKTransitRouteResult paramMKTransitRouteResult, int paramInt)
        {
            stopCheck();
            if ((paramMKTransitRouteResult == null)
                    || (paramMKTransitRouteResult.getNumPlan() == 0)
                    || (paramMKTransitRouteResult.getPlan(0).getNumLines() == 0))
            {
                showNoResult();
                return;
            }
            TransitOverlay localTransitOverlay = new TransitOverlay(
                    SearchLineActivity.this, mMapView);
            mMapView.getOverlays().clear();
            localTransitOverlay.setData(paramMKTransitRouteResult.getPlan(0));
            mMapView.getOverlays().add(localTransitOverlay);
            showStartPosition();
            if ((paramMKTransitRouteResult.getNumPlan() > 0)
                    && (paramMKTransitRouteResult.getPlan(0).getNumLines() > 0))
            {
                int i = paramMKTransitRouteResult.getPlan(0).getNumLines();
                mList.removeAll(mList);
                int j = 0;
                for (int k = 0; k < i; k++)
                {
                    StringBuffer localStringBuffer = new StringBuffer();
                    j += paramMKTransitRouteResult.getPlan(0).getLine(k)
                            .getDistance();
                    localStringBuffer.append(paramMKTransitRouteResult
                            .getPlan(0).getLine(k).getGetOnStop().name);
                    localStringBuffer.append(paramMKTransitRouteResult
                            .getPlan(0).getLine(k).getTitle());
                    localStringBuffer.append(paramMKTransitRouteResult
                            .getPlan(0).getLine(k).getGetOffStop().name);
                    mList.add(localStringBuffer.toString());
                }
                mDistance = getDistance(j);
            }
            reflash();
        }

        public void onGetWalkingRouteResult(
                MKWalkingRouteResult paramMKWalkingRouteResult, int paramInt)
        {
            stopCheck();
            if ((paramMKWalkingRouteResult == null)
                    || (paramMKWalkingRouteResult.getNumPlan() == 0)
                    || (paramMKWalkingRouteResult.getPlan(0).getNumRoutes() == 0))
            {
                showNoResult();
                return;
            }
            RouteOverlay localRouteOverlay = new RouteOverlay(
                    SearchLineActivity.this, mMapView);
            mMapView.getOverlays().clear();
            localRouteOverlay.setData(paramMKWalkingRouteResult.getPlan(0)
                    .getRoute(0));
            mMapView.getOverlays().add(localRouteOverlay);
            showStartPosition();
            if (paramMKWalkingRouteResult.getNumPlan() > 0)
            {
                mDistance = getDistance(paramMKWalkingRouteResult.getPlan(0)
                        .getDistance());
                if (paramMKWalkingRouteResult.getPlan(0).getNumRoutes() > 0)
                {
                    int i = paramMKWalkingRouteResult.getPlan(0).getRoute(0)
                            .getNumSteps();
                    mList.removeAll(mList);
                    for (int j = 0; j < i; j++)
                    {
                        String str = paramMKWalkingRouteResult.getPlan(0)
                                .getRoute(0).getStep(j).getContent();
                        mList.add(str);
                    }
                }
            }
            reflash();
        }
    }

    class SearchLineAdapter extends ArrayAdapter<String>
    {
        public SearchLineAdapter(Context context, List<String> list)
        {
            super(context, 0, list);
        }

        public View getView(int paramInt, View paramView,
                ViewGroup paramViewGroup)
        {
            if (paramView == null)
            {
                paramView = mInflater.inflate(R.layout.list_item_search_line,
                        null);
            }
            TextView localTextView1 = (TextView) paramView
                    .findViewById(R.id.search_line_img);
            TextView localTextView2 = (TextView) paramView
                    .findViewById(R.id.search_line_tv);
            localTextView1.setText((paramInt + 1) + "");
            localTextView2.setText((CharSequence) getItem(paramInt));
            return paramView;
        }
    }
    
    public static boolean startActivity(Context context, SearchLine param)
    {
        if(param == null || !param.isValid()) return false;
        Intent it = new Intent(context, SearchLineActivity.class);
        it.putExtra(TAG_SEARCH_LINE, param);
        context.startActivity(it);
        return true;
    }
}
