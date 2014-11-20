package com.android.test.stub;

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.android.util.R;
import com.android.util.activity.BaseActivity;
import com.android.util.image.RoundedDrawable;

/*********************************************************
 * @author : zhaohua
 * @version : 2013-2-22
 * @see : 图片处理测试
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class TestImageMakeActivity extends BaseActivity implements OnClickListener{

    private ImageView ic_marker;
    private ImageView ic_pic;
    private ImageView ic_result;
    private int count = 0;
    private int[] frameId = new int[] {R.drawable.ic_81_r, R.drawable.ic_100_b, R.drawable.ic_124_b, R.drawable.ic_146_r};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.test_imagemake);
        
        ic_marker = getViewById(R.id.ic_marker);
        ic_pic = getViewById(R.id.ic_pic);
        ic_result = getViewById(R.id.ic_result);            
    }

    @Override
    public void onClick(View v) {               
//        ic_result.setImageBitmap(ImageUtil.overlayFill(((BitmapDrawable)ic_pic.getDrawable()).getBitmap(),
//                ((BitmapDrawable)ic_marker.getDrawable()).getBitmap(),0xFF000000));
        ic_marker.setImageResource(frameId[(count++)%4]);
        ic_result.setImageDrawable(new RoundedDrawable(((BitmapDrawable)ic_pic.getDrawable()).getBitmap(), 
                34, 2,
                ((BitmapDrawable)ic_marker.getDrawable()).getBitmap()));
    }
}
