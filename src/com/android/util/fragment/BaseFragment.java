package com.android.util.fragment;

import com.android.util.system.Logger;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment {

    protected static int count;
    private View rootView;
    public  Dialog mDialog;
    boolean isHostTab = true;
    boolean isDelayResume = false;
    
    void checkDelayResume() {
          isDelayResume = true;
    }
    
    public BaseFragment(){}
    
    protected abstract int getLayoutId();
    
    protected void initViews() { }
    
    @SuppressWarnings("unchecked")
    protected <T extends View> T getViewById(int id)
    {
        View view = rootView.findViewById(id);
        return (T)view;
    }
    
    protected String getLogTag() {
        return "";
    }
       
    protected View inflate(int rid) {
        return LayoutInflater.from(getActivity()).inflate(rid, null);
    }
    ////////////////////////////////////////////////////////////////////////
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Logger.d(getLogTag()+"-->onAttach");
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(getLogTag()+"-->onCreate");
    }
    //////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.d(getLogTag()+"-->onCreateView");
        if(rootView == null) {
            rootView = inflater.inflate(getLayoutId(), container, false);
            initViews();
        }else {
            ViewGroup parent = (ViewGroup) rootView.getParent();  
            if (parent != null) {  
                parent.removeView(rootView);  
            }
        }
        return rootView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.d(getLogTag()+"-->onActivityCreated");
        onRestoreInstanceState(savedInstanceState);
    }
    
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.d(getLogTag()+"-->onActivityResult");
    }
    
    @Override
    public void onStart() {
        super.onStart();
        Logger.d(getLogTag()+"-->onStart");
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Logger.d(getLogTag()+"-->onResume");
        if( (isHostTab && !isHidden()) || isDelayResume) {
            isDelayResume = false;
            _onResume();
        }
    }
    
    public void _onResume() {
        Logger.d(getLogTag()+"-->_onResume");
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Logger.d(getLogTag()+"-->onPause");
        if(mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
            mDialog = null;
        }
    }
    
    @Override
    public void onStop() {
        super.onStop();
        Logger.d(getLogTag()+"-->onStop");
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.d(getLogTag()+"-->onSaveInstanceState");
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Logger.d(getLogTag()+"-->onDestroyView");
    }
    ////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d(getLogTag()+"-->onDestroy");
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        Logger.d(getLogTag()+"-->onDetach");
    }
    ///////////////////////////////////////////////////////////////////////////////
}
