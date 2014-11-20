package com.android.test.stub;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.android.util.R;

public class TestViewPagerActivity extends ActionBarActivity {

    private SpinnerAdapter mSpinnerAdapter;
    private final String[] strings = new String[] {"Horizontal","Vertical"};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_viewpager);

        mSpinnerAdapter = new ArrayAdapter<String>(this, 
                android.R.layout.simple_spinner_dropdown_item, 
                strings);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(mSpinnerAdapter,
                new ActionBar.OnNavigationListener() {
                    @Override
                    public boolean onNavigationItemSelected(int i, long l) {
                        Fragment f = null;
                        if (strings[i].equals("Horizontal")) {
                            f = new HorizontalPagingFragment();
                        } else {
                            f = new VerticalPagingFragment();
                        }

                        FragmentTransaction ft = getSupportFragmentManager()
                                .beginTransaction();

                        ft.replace(R.id.container, f, strings[i]);
                        ft.commit();
                        return true;
                    }
                }
        );

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new VerticalPagingFragment())
                    .commit();
        }
    }
}