package com.android.test.stub;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.util.activity.BaseActivity;
import com.android.util.system.AppHelper;
import com.android.util.update.PatchUpdater;

public class TestDaymChangeTheme extends BaseActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
              
    }

    @Override
    public void onClick(View v) {
        String patchFile = AppHelper.getUpdatePath("patch-1-2.apk").getAbsolutePath();
        PatchUpdater.patchUpdate(getApplicationContext(), patchFile, null);
    }
}
