package com.android.util.map.baidu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.util.R;
import com.android.util.location.baidu.BDLocationProvider;
import com.android.util.location.baidu.BDLocationProvider.BaseSearchListenerImpl;
import com.android.util.system.Util;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.platform.comapi.basestruct.GeoPoint;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-8-9
 * @see : 路线搜索窗口
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class RouteSearDialog extends Dialog
{
    private String MY_LOCATION;
    private AutoCompleteTextView mEndAutoCompleteTextView;
    private AutoCompleteTextView mStartAutoCompleteTextView;
    private ImageView mLocateEndImageView;
    private ImageView mLocateStartImageView;
    private ImageView mSwitchImageView;
    private ImageView mWalkRouteImageView;
    private ImageView mBusRouteImageView;
    private ImageView mDriveRouteImageView;
    private ImageView mSearchTextView;
    
    private int mRouteType = 1;
    private boolean mIsStartSugSelected;
    private boolean mIsEndSugSelected;
    private BMapManager mBMapManager;
    private MKSearch mSearch;
    private MKPoiInfo mStartPostion;
    private MKPoiInfo mEndPostion;
    private LocationSearchListener mStartLocationListener = new LocationSearchListener(0);
    private LocationSearchListener mEndLocationListener = new LocationSearchListener(1);
    private MKPoiResult mEndSuggestionResult;
    private MKPoiResult mStartSuggestionResult;
    private SuggestionListAdapter mStartSuggestionAdapter;
    private SuggestionListAdapter mEndSuggestionAdapter;
    private RouteTypeClickListener mRouteTypeClickListener = new RouteTypeClickListener();
    private ArrayList<HashMap<String, Object>> mStartSuggestionList = new ArrayList<HashMap<String, Object>>();
    private ArrayList<HashMap<String, Object>> mEndSuggestionList = new ArrayList<HashMap<String, Object>>();
    
    private Context mContext;
    private DelayLocationListener mDelayListener;

    private RouteSearDialog(Context context)
    {
        super(context);
        mContext = context;
        MY_LOCATION = getContext().getString(R.string.my_location);
        init();
    }

    private RouteSearDialog(Context context, int theme, String myLocName)
    {
        super(context, theme);
        mContext = context;
        if(!TextUtils.isEmpty(myLocName))
        {
            MY_LOCATION = myLocName;
        }else
        {
            MY_LOCATION = getContext().getString(R.string.my_location);
        }
        init();
    }
    
    private void setDelayLocationListener(DelayLocationListener listener)
    {
        mDelayListener = listener;
        if (this.mShopName() != null)
        {
            this.mEndAutoCompleteTextView.setText(this.mShopName());
            if (this.mEndPostion == null)
            {
                this.mEndPostion = new MKPoiInfo();
            }
            this.mEndPostion.name = this.mShopName();
        }
    }

    public static RouteSearDialog createRouteSearDialog(Activity activity,
            DelayLocationListener listener,String myLocName)
    {
        RouteSearDialog dialog = new RouteSearDialog(activity,R.style.CustomDialogTheme,myLocName);
        dialog.setDelayLocationListener(listener);
        return dialog;
    }

    private void init()
    {
        mBMapManager = BDLocationProvider.getInstance().getBMapManager();
        mBMapManager.start();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_search_route_layout);
        this.mStartAutoCompleteTextView = ((AutoCompleteTextView) findViewById(R.id.start_addr));
        this.mEndAutoCompleteTextView = ((AutoCompleteTextView) findViewById(R.id.end_addr));
        this.mSwitchImageView = ((ImageView) findViewById(R.id.switch_location));
        this.mSearchTextView = ((ImageView) findViewById(R.id.route_search));
        this.mLocateStartImageView = ((ImageView) findViewById(R.id.location_start));
        this.mLocateEndImageView = ((ImageView) findViewById(R.id.location_end));
        this.mBusRouteImageView = ((ImageView) findViewById(R.id.bus_route));
        this.mDriveRouteImageView = ((ImageView) findViewById(R.id.drive_route));
        this.mWalkRouteImageView = ((ImageView) findViewById(R.id.walk_route));

        initAutoCompleteTextView();
        this.mSwitchImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramView)
            {
                switchStartAndEndLocation();
            }
        });
        this.mSearchTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramView)
            {
                searchRoute();
            }
        });
        this.mBusRouteImageView
                .setOnClickListener(this.mRouteTypeClickListener);
        this.mDriveRouteImageView
                .setOnClickListener(this.mRouteTypeClickListener);
        this.mWalkRouteImageView
                .setOnClickListener(this.mRouteTypeClickListener);
        this.mLocateStartImageView
                .setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View paramView)
                    {
                        mStartAutoCompleteTextView.setText(MY_LOCATION);
                        if (mEndAutoCompleteTextView.getEditableText()
                                .toString().equals(MY_LOCATION)) mEndAutoCompleteTextView
                                .setText("");
                    }
                });
        this.mLocateEndImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramView)
            {
                mEndAutoCompleteTextView.setText(MY_LOCATION);
                if (mStartAutoCompleteTextView.getEditableText().toString()
                        .equals(MY_LOCATION)) mStartAutoCompleteTextView
                        .setText("");
            }
        });
        refreshRouteTypeButton();
        
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        getWindow().setGravity(Gravity.TOP);
        WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
        localLayoutParams.x = 0;
        localLayoutParams.y = 0;
        getWindow().setAttributes(localLayoutParams);
        
        findViewById(R.id.touch_diss).setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                dismiss();
                return false;
            }
        });
    }

    private void initAutoCompleteTextView()
    {
        this.mStartAutoCompleteTextView.setOnItemClickListener(new OnLocationItemClickListener(0));
        this.mStartSuggestionAdapter = new SuggestionListAdapter(this.mStartSuggestionList);
        this.mStartAutoCompleteTextView.setAdapter(this.mStartSuggestionAdapter);
        this.mStartAutoCompleteTextView.setSelectAllOnFocus(true);
        this.mStartAutoCompleteTextView.setThreshold(1);
        this.mStartAutoCompleteTextView.addTextChangedListener(new SearchTextWatcher(0));
        this.mStartAutoCompleteTextView.setText(MY_LOCATION);
        
        this.mEndAutoCompleteTextView.setOnItemClickListener(new OnLocationItemClickListener(1));
        this.mEndSuggestionAdapter = new SuggestionListAdapter(this.mEndSuggestionList);
        this.mEndAutoCompleteTextView.setAdapter(this.mEndSuggestionAdapter);
        this.mStartAutoCompleteTextView.setThreshold(1);
        this.mEndAutoCompleteTextView.setSelectAllOnFocus(true);
        this.mEndAutoCompleteTextView.addTextChangedListener(new SearchTextWatcher(1));
    }

    private GeoPoint mMyLocation()
    {
        if (mDelayListener != null)
        {
            return mDelayListener.getGeoPoint(0);
        } else
        {
            return null;
        }
    }

    private String mMyAddress()
    {
        if (mDelayListener != null)
        {
            return mDelayListener.getAddrInfo(0);
        } else
        {
            return null;
        }
    }
    
    String mMyLocName()
    {
        if (mDelayListener != null)
        {
            return mDelayListener.getAddrInfo(2);
        } else
        {
            return null;
        }
    }

    private String mShopName()
    {
        if (mDelayListener != null)
        {
            return mDelayListener.getAddrInfo(1);
        } else
        {
            return null;
        }
    }

    private GeoPoint mShopPoint()
    {
        if (mDelayListener != null)
        {
            return mDelayListener.getGeoPoint(1);
        } else
        {
            return null;
        }
    }

    protected boolean checkStartLocation()
    {
        if (this.mStartPostion == null)
        {
            this.mStartPostion = new MKPoiInfo();
        }
        String str = this.mStartAutoCompleteTextView.getEditableText().toString();
        if (Util.isEmpty(str.trim()))
        {
            Util.showToast(mContext, R.string.start_empty);
            return false;
        }
        // 起点是 我的位置 时
        if (str.equals(MY_LOCATION))
        {
            this.mStartPostion.name = MY_LOCATION;
            if (this.mMyLocation() != null)
            {
                this.mStartPostion.pt = this.mMyLocation();
                if (this.mMyAddress() != null)
                {
                    this.mStartPostion.city = this.mMyAddress();
                }
            }
        } else 
        // 起点有地址时
        if (this.mStartPostion.name != null)
        {
            // 有经纬度 且 原来的相同时
            if (this.mStartPostion.pt != null && (str.equals(this.mStartPostion.name)))
            {
                return true;
            }
            // 如果起点与商定名相同时，则设置商定的经纬度
            if(this.mStartPostion.name.equals(this.mShopName()))
            {
                this.mStartPostion.pt = this.mShopPoint();              
            }else 
            {
                this.mStartPostion.name = str;
            }                       
        }else 
        {
            this.mStartPostion.name = str;
        }
        // 如果没有经纬度，则查询
        if (this.mStartPostion.pt == null)
        {
            searchLocation(this.mStartPostion.name);
            return false;
        } else
        {
            return true;
        }
    }

    protected boolean checkEndLocation()
    {
        if (this.mEndPostion == null)
        {
            this.mEndPostion = new MKPoiInfo();
        }
        String str = this.mEndAutoCompleteTextView.getEditableText().toString();
        if (Util.isEmpty(str.trim()))
        {
            Util.showToast(mContext, R.string.end_empty);
            return false;
        }
        // 终点是 我的位置 时
        if (str.equals(MY_LOCATION))
        {
            this.mEndPostion.name = MY_LOCATION;
            if (this.mMyLocation() != null)
            {
                this.mEndPostion.pt = this.mMyLocation();
                if (this.mMyAddress() != null)
                {
                    this.mEndPostion.city = this.mMyAddress();
                }
            }
        } else 
        // 终点有地址时
        if (this.mEndPostion.name != null)
        {
            // 有经纬度 且 原来的相同时
            if (this.mEndPostion.pt != null && (str.equals(this.mEndPostion.name)))
            {
                return true;
            }
            // 如果终点与商定名相同时，则设置商定的经纬度
            if(this.mEndPostion.name.equals(this.mShopName()))
            {
                this.mEndPostion.pt = this.mShopPoint();              
            }else 
            {
                this.mEndPostion.name = str;
            }                       
        }else 
        {
            this.mEndPostion.name = str;
        }
        // 如果没有经纬度，则查询
        if (this.mEndPostion.pt == null)
        {
            searchLocation(this.mEndPostion.name);
            return false;
        } else
        {
            return true;
        }
    }

    private void searchRoute()
    {
        if(mDelayListener != null && mDelayListener.isLocating())
        {
            Util.showToast(mContext, R.string.locating_info);
            return;
        }
        if (!checkStartLocation()) { return; }
        if (checkEndLocation())
        {
            gotoSearchRouteActivity();
        }
    }

    private void showSelectPositionDialog(final List<MKPoiInfo> paramList)
    {

        String str = null;
        if (this.mStartPostion.pt == null)
        {
            str = mContext.getString(R.string.start_is);
        } else
        {
            str = mContext.getString(R.string.end_is);
        }
        new AlertDialog.Builder(mContext)
                .setTitle(str)
                .setSingleChoiceItems(
                        new PositionAdapter(mContext, R.layout.list_item_sug,
                                paramList), 0,
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(
                                    DialogInterface paramDialogInterface,
                                    int paramInt)
                            {
                                MKPoiInfo localMKPoiInfo = (MKPoiInfo) paramList.get(paramInt);
                                if (mStartPostion.pt == null)
                                {
                                    setStartLocation(localMKPoiInfo);
                                } else
                                {
                                    setEndLocation(localMKPoiInfo);
                                }
                                paramDialogInterface.dismiss();
                                searchRoute();
                                return;
                            }
                        }).show();
    }

    private void gotoSearchRouteActivity()
    {
        SearchLine param = new SearchLine();
        if (mStartPostion.pt != null)
        {
            if(!Util.isEmpty(this.mStartPostion.city))
            {
                param.startCity = this.mStartPostion.city;
            }else
            {
                if(this.mMyAddress() != null)
                {
                    param.startCity = mMyAddress();
                }
            }

            param.startArea = this.mStartPostion.name;            
            param.startX = this.mStartPostion.pt.getLongitudeE6();
            param.startY = this.mStartPostion.pt.getLatitudeE6();
            param.routeType = this.mRouteType;
            
          if(mEndPostion.pt != null)
          {
              if(!Util.isEmpty(this.mEndPostion.city))
              {
                  param.endCity = this.mEndPostion.city;
              }else
              {
                  param.endCity = param.startCity;
              }    
              param.endArea = this.mEndPostion.name;
              param.endX = this.mEndPostion.pt.getLongitudeE6();
              param.endY = this.mEndPostion.pt.getLatitudeE6();
          }
          
          SearchLineActivity.startActivity(mContext, param);
        }
    }

    // 查询 起始点输入框 的位置信息
    private void searchLocation(int paramInt, String paramString)
    {
        if (this.mSearch == null)
        {
            this.mSearch = new MKSearch();
        }      
        LocationSearchListener localLocationSearchListener = null;
        if (paramInt == 0)
        {
            localLocationSearchListener = mStartLocationListener;
        } else
        {
            localLocationSearchListener = mEndLocationListener;
        }
        mSearch.init(mBMapManager, localLocationSearchListener);
        this.mSearch.poiSearchInCity("", paramString);
    }

    private void switchStartAndEndLocation()
    {
        String str1 = this.mStartAutoCompleteTextView.getEditableText()
                .toString();
        String str2 = this.mEndAutoCompleteTextView.getEditableText()
                .toString();
        if (this.mStartPostion == null)
        {
            if (this.mEndPostion != null)
            {
                this.mStartPostion = this.mEndPostion;
                this.mEndPostion = null;
            } else
            {
                return;
            }
        } else
        {
            if (this.mEndPostion == null)
            {
                this.mEndPostion = this.mStartPostion;
                this.mStartPostion = null;
            } else
            {
                MKPoiInfo localMKPoiInfo = this.mStartPostion;
                this.mStartPostion = this.mEndPostion;
                this.mEndPostion = localMKPoiInfo;
            }
        }
        setStartAutoCompleteText(str2);
        setEndAutoCompleteText(str1);
    }

    private void refreshSuggestionView(int paramInt,
            List<HashMap<String, Object>> paramList)
    {
        if (paramInt == 0)
        {
            if (this.mIsStartSugSelected)
            {
                this.mStartAutoCompleteTextView.dismissDropDown();
            } else
            {
                this.mStartSuggestionList.clear();
                this.mStartSuggestionList.addAll(paramList);
                this.mStartSuggestionAdapter.notifyDataSetChanged();
                this.mStartAutoCompleteTextView.showDropDown();
            }
        } else
        {
            if (this.mIsEndSugSelected)
            {
                this.mEndAutoCompleteTextView.dismissDropDown();
            } else
            {
                this.mEndSuggestionList.clear();
                this.mEndSuggestionList.addAll(paramList);
                this.mEndSuggestionAdapter.notifyDataSetChanged();
                this.mEndAutoCompleteTextView.showDropDown();
            }
        }
    }

    private void refreshRouteTypeButton()
    {
        this.mBusRouteImageView
                .setImageResource(mRouteType == 1 ? R.drawable.bus_pressed
                        : R.drawable.bus_normal);
        this.mDriveRouteImageView
                .setImageResource(mRouteType == 2 ? R.drawable.drive_pressed
                        : R.drawable.drive_normal);
        this.mWalkRouteImageView
                .setImageResource(mRouteType == 3 ? R.drawable.walk_pressed
                        : R.drawable.walk_normal);
    }

    private void onGetSuggFinished(int paramInt, MKPoiResult paramMKPoiResult)
    {
        if ((paramMKPoiResult == null)|| (paramMKPoiResult.getNumPois() == 0)) 
        { 
            return; 
        }
        ArrayList<MKPoiInfo> localArrayList1 = paramMKPoiResult.getAllPoi();
        if (localArrayList1.isEmpty()) { return; }
        if (paramInt == 0)
        {
            mStartSuggestionResult = paramMKPoiResult;
        } else
        {
            mEndSuggestionResult = paramMKPoiResult;
        }
        ArrayList<HashMap<String, Object>> localArrayList2 = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < localArrayList1.size(); i++)
        {
            HashMap<String, Object> localHashMap = new HashMap<String, Object>();
            localHashMap.put("ItemTitle",
                    ((MKPoiInfo) localArrayList1.get(i)).name);
            localHashMap.put("ItemText",
                    ((MKPoiInfo) localArrayList1.get(i)).address);
            localArrayList2.add(localHashMap);
        }
        refreshSuggestionView(paramInt, localArrayList2);
    }

    private void setStartLocation(MKPoiInfo paramMKPoiInfo)
    {
        this.mStartPostion = paramMKPoiInfo;
        setStartAutoCompleteText(paramMKPoiInfo.name);
        this.mIsStartSugSelected = true;
    }

    private void setEndLocation(MKPoiInfo paramMKPoiInfo)
    {
        this.mEndPostion = paramMKPoiInfo;
        setEndAutoCompleteText(paramMKPoiInfo.name);
        this.mIsEndSugSelected = true;
    }

    @SuppressWarnings("rawtypes")
    private void setStartAutoCompleteText(String paramString)
    {
        this.mStartAutoCompleteTextView.setAdapter((ArrayAdapter) null);
        this.mStartAutoCompleteTextView.setText(paramString);
        this.mStartAutoCompleteTextView.setAdapter(this.mStartSuggestionAdapter);
    }

    @SuppressWarnings("rawtypes")
    private void setEndAutoCompleteText(String paramString)
    {
        this.mEndAutoCompleteTextView.setAdapter((ArrayAdapter) null);
        this.mEndAutoCompleteTextView.setText(paramString);
        this.mEndAutoCompleteTextView.setAdapter(this.mEndSuggestionAdapter);
    }
    // 查询 起始点的 位置信息
    private void searchLocation(String paramString)
    {
        if (paramString == null || paramString.equals(MY_LOCATION))
        {
            Util.showToast(mContext, R.string.locating_info);
            return;
        }
        if (this.mSearch == null)
        {
            this.mSearch = new MKSearch();
        }
        this.mSearch.init(mBMapManager,
                new BaseSearchListenerImpl()
                {
                    public void onGetPoiResult(MKPoiResult paramMKPoiResult,int type, int iError)
                    {
                        if (type != 0 || paramMKPoiResult == null|| paramMKPoiResult.getNumPois() == 0)
                        {
                            Util.showToast(mContext, R.string.loc_not_found);
                            return;
                        }
                        if (paramMKPoiResult.getNumPois() == 1)
                        {
                            MKPoiInfo localMKPoiInfo = paramMKPoiResult.getPoi(0);
                            if (mStartPostion.pt == null)
                            {
                                mStartPostion.pt = localMKPoiInfo.pt;
                                mStartPostion.city = localMKPoiInfo.city;
                            } else
                            {
                                mEndPostion.city = localMKPoiInfo.city;
                                mEndPostion.pt = localMKPoiInfo.pt;
                            }
                        }else
                        {
                            showSelectPositionDialog(paramMKPoiResult.getAllPoi());
                        }
                    }
                });
        this.mSearch.poiSearchInCity("", paramString);
    }

    class OnLocationItemClickListener implements
            AdapterView.OnItemClickListener
    {
        private int type;

        public OnLocationItemClickListener(int type)
        {
            this.type = type;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id)
        {
            if (type == 0)
            {
                if (mStartSuggestionResult != null && mStartSuggestionResult.getNumPois() > 0)
                {
                    if (mStartSuggestionResult.getNumPois() > position)
                    {
                        setStartLocation(mStartSuggestionResult.getPoi(position));
                    }
                }
            } else
            {
                if (mEndSuggestionResult != null && mEndSuggestionResult.getNumPois() > 0)
                {
                    if (mEndSuggestionResult.getAllPoi().size() > position)
                    {
                        setEndLocation(mEndSuggestionResult.getPoi(position));
                    }
                }
            }
            String str1 = mStartAutoCompleteTextView.getText().toString();
            String str2 = mEndAutoCompleteTextView.getText().toString();
            if ((str1.length() == 0) && (str2.length() == 0))
            {
                mSwitchImageView.setEnabled(false);
            } else
            {
                mSwitchImageView.setEnabled(true);
            }
            if ((str1.length() != 0) && (str2.length() != 0))
            {
                mSearchTextView.setEnabled(true);
            }
        }
    }

    class LocationSearchListener extends BaseSearchListenerImpl
    {
        private int searchType;

        public LocationSearchListener(int i)
        {
            this.searchType = i;
        }

        public void onGetPoiResult(MKPoiResult paramMKPoiResult, int type, int iError)
        {
            if (iError == 0)
            {
                onGetSuggFinished(searchType, paramMKPoiResult);
            }
        }
    }

    class SuggestionListAdapter extends SimpleAdapter implements Filterable
    {
        public ArrayList<HashMap<String, Object>> mAllData;

        public ArrayList<HashMap<String, Object>> mDataShown;

        VernacularFilter vernacular_filter = new VernacularFilter();

        public SuggestionListAdapter(ArrayList<HashMap<String, Object>> list)
        {
            super(mContext, list, R.layout.list_item_sug, new String[] {
                    "ItemTitle", "ItemText" }, new int[] { R.id.ItemTitle,
                    R.id.ItemText });
            this.mDataShown = list;
        }

        public Filter getFilter()
        {
            return this.vernacular_filter;
        }

        class VernacularFilter extends Filter
        {
            VernacularFilter()
            {

            }

            @SuppressWarnings("unchecked")
            public String convertResultToString(Object paramObject)
            {
                return (String) ((HashMap<String, Object>) paramObject)
                        .get("ItemTitle");
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint)
            {
                Filter.FilterResults localFilterResults = new Filter.FilterResults();
                ArrayList<HashMap<String, Object>> localArrayList = mDataShown;
                localFilterResults.values = localArrayList;
                localFilterResults.count = localArrayList.size();
                return localFilterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint,
                    FilterResults results)
            {
                if ((results != null) && (results.count > 0))
                {
                    notifyDataSetChanged();
                }
            }
        }
    }

    class SearchTextWatcher implements TextWatcher
    {
        private int type;

        public SearchTextWatcher(int type)
        {
            this.type = type;
        }

        public void afterTextChanged(Editable paramEditable)
        {
            if (paramEditable.length() == 0) { return; }
            switch (type)
            {
            case 0:
            {
                if ((mStartPostion != null)
                        && (paramEditable.toString().equals(mStartPostion.name)))
                {
                    mStartAutoCompleteTextView.dismissDropDown();
                } else
                {
                    if(paramEditable.toString().equals(MY_LOCATION))
                    {
                        return;
                    }else
                    {
                        mIsStartSugSelected = false;
                        searchLocation(type, paramEditable.toString());
                    }
                }
                break;
            }
            case 1:
            {
                if ((mEndPostion != null)
                        && (paramEditable.toString().equals(mEndPostion.name)))
                {
                    mEndAutoCompleteTextView.dismissDropDown();
                } else
                {
                    mIsEndSugSelected = false;
                    searchLocation(type, paramEditable.toString());
                }
                break;
            }
            }
        }

        public void beforeTextChanged(CharSequence paramCharSequence,
                int paramInt1, int paramInt2, int paramInt3)
        {}

        public void onTextChanged(CharSequence paramCharSequence,
                int paramInt1, int paramInt2, int paramInt3)
        {}
    }

    class RouteTypeClickListener implements View.OnClickListener
    {
        RouteTypeClickListener()
        {}

        public void onClick(View paramView)
        {
            if (paramView == mBusRouteImageView)
            {
                if (mRouteType != 1)
                {
                    mRouteType = 1;
                }
            } else if (paramView == mDriveRouteImageView)
            {
                if (mRouteType != 2)
                {
                    mRouteType = 2;
                }
            } else if (paramView == mWalkRouteImageView)
            {
                if (mRouteType != 3)
                {
                    mRouteType = 3;
                }
            }
            refreshRouteTypeButton();
        }
    }

    class PositionAdapter extends ArrayAdapter<MKPoiInfo>
    {
        List<MKPoiInfo> positions;

        public PositionAdapter(Context context, int lay, List<MKPoiInfo> list)
        {
            super(context, lay, list);
        }

        public View getView(int paramInt, View paramView,
                ViewGroup paramViewGroup)
        {
            if (paramView == null)
            {
                paramView = getLayoutInflater().inflate(
                        R.layout.list_item_sug, null);
            }

            TextView localTextView1 = (TextView) paramView
                    .findViewById(R.id.ItemTitle);
            TextView localTextView2 = (TextView) paramView
                    .findViewById(R.id.ItemText);
            MKPoiInfo localMKPoiInfo = (MKPoiInfo) getItem(paramInt);
            localTextView1.setText(localMKPoiInfo.name);
            localTextView2.setText(localMKPoiInfo.address);
            return paramView;
        }
    }

    interface DelayLocationListener
    {
        public GeoPoint getGeoPoint(int type);

        public String getAddrInfo(int type);
        
        public boolean isLocating();
    }
}
