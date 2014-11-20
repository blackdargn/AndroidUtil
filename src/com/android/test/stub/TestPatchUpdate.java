package com.android.test.stub;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.android.util.R;
import com.android.util.activity.BaseActivity;
import com.android.util.system.AppHelper;
import com.android.util.thread.UINotifyListener;
import com.android.util.update.PatchUpdater;

public class TestPatchUpdate extends BaseActivity implements OnClickListener {

    private TextView tx_show;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_patch_update);
        
        tx_show = getViewById(R.id.tx_show);
    }

    @Override
    public void onClick(View v) {
        String patchFile = AppHelper.getUpdatePath("patch-1-2.apk").getAbsolutePath();
        PatchUpdater.patchUpdate(getApplicationContext(), patchFile, new UINotifyListener<String>() {
            @Override
            protected void onSucceed(String object) {
                tx_show.append(object);
            }
        });
    }
}
