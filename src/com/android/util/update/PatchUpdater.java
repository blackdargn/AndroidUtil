package com.android.util.update;

import java.io.File;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.android.util.manager.BaseManager;
import com.android.util.system.ActionHelper;
import com.android.util.system.AppHelper;
import com.android.util.system.Logger;
import com.android.util.system.SumUtil;
import com.android.util.thread.Executable;
import com.android.util.thread.ThreadHelper;
import com.android.util.thread.UINotifyListener;
import com.cundong.utils.PatchUtils;

public class PatchUpdater {
    
    static {
        System.loadLibrary("apkpatch");
    }
    
    public static void patchUpdate(final Context context, final String patchFile, final UINotifyListener<String> listener) {
        ThreadHelper.executeWithCallback(new Executable<String>() {
            @Override
            public String execute() throws Exception {
                // 获取原始APK信息
                PackageInfo appInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                File appFile = new File(appInfo.applicationInfo.sourceDir);
                
                String patchApk= AppHelper.getUpdatePath("AndroidUtil-patch.apk").getAbsolutePath();
                
                if( PatchUtils.patch(appFile.getAbsolutePath(), patchApk, patchFile) == 0) {
                    String sha1 = SumUtil.sum(patchApk, false);
                    String md5 = SumUtil.sum(patchApk, true);
                    BaseManager.callback(BaseManager.MSG_NOTIFY_ON_SUCCEED, listener, "sha1 = "+ sha1 + "\n");
                    BaseManager.callback(BaseManager.MSG_NOTIFY_ON_SUCCEED, listener, "md5 = "+ md5 + "\n");
                    
                    Logger.d("---> patchUpdate OK : " + patchApk + "\n md5 = " + md5 + "\n sha1 = " + sha1);
                    ActionHelper.callInstallApk( new File(patchApk), context);
                }else {
                    Logger.d("---> patchUpdate Fail : " + patchApk);
                }
                return null;
            }
        }, listener);
    }
    
}
