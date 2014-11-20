package com.android.test.plugin;

import org.osgi.framework.Bundle;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.util.R;
import com.android.util.adapter.ArrayListAdapter;

public class BoundListAdapter extends ArrayListAdapter <Bundle> implements OnClickListener,OnLongClickListener{

       
    public BoundListAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.test_list_item_plugin, null);
            holder = new ViewHolder();
            holder.icon = (ImageView)convertView.findViewById(R.id.icon);
            holder.name = (TextView)convertView.findViewById(R.id.name);
            holder.install = ( Button)convertView.findViewById(R.id.install);
            
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        Bundle item = getItem(position);
        holder.name.setText(item.getName());
        holder.install.setOnClickListener(this);
        holder.install.setTag(R.id.install, item);
        convertView.setTag(R.id.icon, item);
        convertView.setOnLongClickListener(this);
        
        return convertView;
    }
    
    private static class ViewHolder{
        ImageView icon;
        TextView name;
        Button install;
    }

    @Override
    public void onClick(View v) {
        Bundle ab = (Bundle) v.getTag(R.id.install);
        //ab为org.osgi.framework.Bundle
        if(ab.getState()!=Bundle.ACTIVE){
            //判断插件是否已启动
            try {
                ab.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(ab.getBundleActivity()!=null){
            //插件设置了启动 Activity
            //在宿主应用中我们需要通过 activity启动服务来启动插件的activit
            Intent i=new Intent();
            i.setClassName(mContext, ab.getBundleActivity());
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);
        }else{
            Toast.makeText(mContext, "该插件没有配置BundleActivity",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        Bundle ab = (Bundle) v.getTag(R.id.icon);
       //直接使用 Bundle.uninstall()卸载
        try {
            ab.uninstall();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
