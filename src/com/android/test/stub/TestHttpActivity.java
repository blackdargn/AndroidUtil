/**
 * my-android-util
 * TestHttpActivity.java
 * com.android.test.stub
 * 
 */
package com.android.test.stub;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.android.util.R;
import com.android.util.activity.BaseActivity;
import com.android.util.protocol.http.AsyncOkHttpClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

/*********************************************************
 * @author : zhaohua
 * @version : 2014-3-16
 * @see : 
 * @Copyright : copyrights reserved by personal 2007-2012
 **********************************************************/
public class TestHttpActivity extends BaseActivity implements OnClickListener
{
    private TextView info;

    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
    private AsyncOkHttpClient asyncOkHttpClient = new AsyncOkHttpClient();
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.test_http);
        
        info = getViewById(R.id.info);
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId()){
        case R.id.get1:
            get1();
            break;
        case R.id.post1:
            post1();
            break;
        case R.id.get2:
            get2();
            break;
        case R.id.post2:
            post2();
            break;
        case R.id.getCache:
            getCache();
            break;
        }
    }
    
    @Override
    protected void onPause()
    {
        asyncHttpClient.cancelRequests(this, true);
        asyncOkHttpClient.cancelRequests(this, true);
        super.onPause();
    }
    
    private static final String GET_URL = "http://blog.csdn.net/feng88724/article/details/6170021";
    
    private void get1() {
        asyncHttpClient.get(this, GET_URL, null, null, httpResponseHandler);
    }    
    private void post1() {
            
    }
    private void get2() {
        asyncOkHttpClient.get(this, GET_URL, new Header[] {new BasicHeader("offline-cache","1")}, null, httpResponseHandler);
    }
    private void post2() {
        
    }
    private void getCache() {
        asyncOkHttpClient.get(this, GET_URL, new Header[] {new BasicHeader("offline","1")}, null, httpResponseHandler);
    } 
    
    private AsyncHttpResponseHandler httpResponseHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onSuccess(int statusCode, Header[] headers,byte[] responseBody)
        {
            info.setText("" + new String(responseBody));
        }

        @Override
        public void onFailure(int statusCode, Header[] headers,
                byte[] responseBody, Throwable error)
        {
            info.setText(statusCode + "\n");
            if(responseBody != null) {
                info.append("" + new String(responseBody));
            }
        }
        
    };
    
}
