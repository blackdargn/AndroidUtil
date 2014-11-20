package com.android.test.stub;

import pl.droidsonroids.gif.FaceEditText;
import pl.droidsonroids.gif.FaceTextView;
import pl.droidsonroids.gif.GifImageView;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.android.util.R;
import com.android.util.activity.TemplateActivity;
import com.android.util.adapter.ArrayListAdapter;
import com.android.util.system.ActionHelper;
import com.android.util.system.Logger;
import com.android.util.widget.TitleBar;

public class TestGifActivity extends TemplateActivity implements OnClickListener,OnLongClickListener{

    private ListView mListView;
    private FaceEditText et_chat;
    private View btn_sure;
    private TestGifListAdapter mListAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.test_gif);
        
        mListView = getViewById(R.id.listView);
        et_chat = getViewById(R.id.et_chat);
        btn_sure = getViewById(R.id.btn_sure);
        
        mListAdapter = new TestGifListAdapter(this);
        mListView.setAdapter(mListAdapter);
        mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
        
        btn_sure.setOnLongClickListener(this);
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if(et_chat.getText().length() == 0) {
            mListAdapter.addItem("");
        }else {
            mListAdapter.addItem(et_chat.getText().toString());
            et_chat.setText("");
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri file = ActionHelper.onActivityResult(requestCode, resultCode, data);
        if(file != null) {
            Intent intent = new Intent("android.intent.2bulu.IMG_SHARE", file);
            intent.putExtra("mode", 0);
            startActivity(intent);
        }
    }
    
    @Override
    public boolean onLongClick(View v) {
        et_chat.setText(et_chat.getText().toString());
        ActionHelper.startAcquireImage(this);
        return false;
    }
    
    static  class TestGifListAdapter extends ArrayListAdapter<String>{

        private int count = 0;
        private SparseIntArray picMaps = new SparseIntArray();
        
        public TestGifListAdapter(Context context) {
            super(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Logger.d("--->getView: " + position + " : " + convertView);
            ViewHolder holder = null;
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.test_list_item_gif, null);
                holder = new ViewHolder();
                holder.textView = (FaceTextView)convertView.findViewById(R.id.ItemTitle);
                holder.imgView = (GifImageView)convertView.findViewById(R.id.ItemImg);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder)convertView.getTag();
            }
            
            String item = getItem(position);
            if(TextUtils.isEmpty(item)) {
                int pic = 0;
                if(picMaps.indexOfKey(position) >= 0) {
                    pic = picMaps.get(position);
                }else {
                    picMaps.put(position, pic = getPic());
                }
                holder.imgView.setImageResource(pic);
                holder.textView.setVisibility(View.GONE);
                holder.imgView.setVisibility(View.VISIBLE);
            }else {
                holder.textView.setText(item);
                holder.textView.setVisibility(View.VISIBLE);
                holder.imgView.setVisibility(View.GONE);
            }
            
            return convertView;
        }
        
        private int getPic() {
            count ++;
            return count % 3 ==0 ? R.drawable.big03 : count%3 == 1 ? R.drawable.big01 : R.drawable.big02;
        }
        
        static class ViewHolder{
            FaceTextView textView;
            GifImageView imgView;
        }
    }
}
