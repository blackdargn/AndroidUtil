package com.android.test.stub;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.util.R;
import com.android.util.activity.BaseActivity;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-11-14
 * @see : 动画测试
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class TestAnimActivity extends BaseActivity implements OnClickListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.test_anim);
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.pop_show_image:
            {
                break;
            }
            case R.id.pop_show_vedio:
            {                
                break;
            }
        }
    }
}