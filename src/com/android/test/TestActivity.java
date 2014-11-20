package com.android.test;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.test.plugin.TestPluginActivity;
import com.android.test.stub.ClearActivity;
import com.android.test.stub.TestAidlActivity;
import com.android.test.stub.TestAnimActivity;
import com.android.test.stub.TestDaymChangeTheme;
import com.android.test.stub.TestFragmentActivity;
import com.android.test.stub.TestGifTextView;
import com.android.test.stub.TestHttpActivity;
import com.android.test.stub.TestImageMakeActivity;
import com.android.test.stub.TestPanels;
import com.android.test.stub.TestPatchUpdate;
import com.android.test.stub.TestSurfaceView;
import com.android.test.stub.TestTabHostActivity;
import com.android.test.stub.TestViewPager2Activity;
import com.android.test.stub.TestViewPagerActivity;

public class TestActivity extends ListActivity {

    private ArrayAdapter<Item> adapter;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Item[] items = {
                new Item(TestViewPager2Activity.class, "Test ViewPager2Activity"),
                new Item(TestTabHostActivity.class, "Test TabHostActivity"),
                new Item(TestViewPagerActivity.class, "Test ViewPagerActivity"),
                new Item(TestHttpActivity.class, "Test HttpActivity"),
                new Item(TestPluginActivity.class, "Test PluginActivity"),
//                new Item(TestGifActivity.class, "Test GifActivity"),
                new Item(TestGifTextView.class, "Test GifTextView"),
                new Item(TestDaymChangeTheme.class, "Test DaymChangeTheme"),
                new Item(TestPatchUpdate.class, "Test PatchUpdate"),
                new Item(TestFragmentActivity.class, "Test FragmentPageAdpater"),
                new Item(TestPanels.class, "Test Panle 抽屉"),
        		new Item(TestSurfaceView.class, "Test SurfaceView"),
        		new Item(ClearActivity.class, "Test GLSurfaceView"),
        		new Item(TestAidlActivity.class, "Test AidlActivity"),
        		new Item(TestAnimActivity.class, "Test AnimActivity"),
        		new Item(TestImageMakeActivity.class, "Test ImageMakeActivity"),
        };
        
		adapter = new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1, items);
		setListAdapter(adapter);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
		startActivity(adapter.getItem(position));
    }
    
    class Item extends Intent {
    	String s;
    	public Item(Class<?> c, String s) {
    		super(TestActivity.this, c);
    		this.s = s;
		}
    	
    	public Item(Intent it, String s) {
            super(it);
            this.s = s;
        }
    	
    	@Override
    	public String toString() {
    		return s;
    	}
    }
}
