package com.android.test.plugin;

import java.util.List;

import org.apkplug.Bundle.BundleControl;
import org.apkplug.Bundle.installCallback;
import org.apkplug.app.FrameworkFactory;
import org.apkplug.app.FrameworkInstance;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.android.util.R;
import com.android.util.system.ActionHelper;

/**
 * 0.0.1 版本新增安装本地插件接口 MainActivity.install(String path,installCallback callback)
 * 
 * @author 梁前武 QQ 1587790525 www.apkplug.com
 */
public class TestPluginActivity extends Activity implements OnClickListener {
    private FrameworkInstance frame = null;

    private List<org.osgi.framework.Bundle> bundles = null;

    private ListView bundlelist = null;

    private BoundListAdapter adapter = null;

    private TextView info = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_plugin);
        try {          
            frame = FrameworkFactory.getInstance().
            start(new java.util.ArrayList<BundleActivator>(), 
                    TestPluginActivity.this,
                    new MyProperty(this.getApplicationContext()));
        } catch (Exception ex) {
            System.err.println("Could not create : " + ex);
            int nPid = android.os.Process.myPid();
            android.os.Process.killProcess(nPid);
        }
        info = (TextView) this.findViewById(R.id.info);
        
        initBundleList();
        // 监听插件安装状态已动态更新列表
        ListenerBundleEvent();
    }
    
    @Override
    public void onClick(View v) {
        ActionHelper.startAcquireFile(this);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri file = ActionHelper.onActivityResult(requestCode, resultCode, data);
        try {
            install(file.toString(), new myinstallCallback());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 安装插件回调函数
     */
    class myinstallCallback implements installCallback {

        @Override
        public void callback(int arg0, org.osgi.framework.Bundle arg1) {
            if (arg0 == installCallback.stutas5 || arg0 == installCallback.stutas7) {
                info.setText("插件安装成功 ：\n" + showBundle(arg1));
                return;
            } else {
                info.setText("插件安装失败 ：" + this.stutasToStr(arg0));
            }
        }

        /**
         * 信息由 http://www.apkplug.com/javadoc/bundledoc1.5.3/ org.apkplug.Bundle
         * 接口 installCallback 提供
         * 
         * @param stutas
         * @return
         */
        private String stutasToStr(int stutas) {
            if (stutas == installCallback.stutas) {
                return "缺少SymbolicName";
            } else if (stutas == installCallback.stutas1) {
                return "已是最新版本";
            } else if (stutas == installCallback.stutas2) {
                return "版本号不正确";
            } else if (stutas == installCallback.stutas3) {
                return " 版本相等";
            } else if (stutas == installCallback.stutas4) {
                return "无法获取正确的证书";
            } else if (stutas == installCallback.stutas5) {
                return "更新成功";
            } else if (stutas == installCallback.stutas6) {
                return "证书不一致";
            } else if (stutas == installCallback.stutas7) {
                return "安装成功";
            }
            return "状态信息不正确";
        }
    }

    /**
     * 初始化显示已安装插件的UI
     */
    public void initBundleList() {
        // 已安装插件列表
        bundlelist = (ListView) findViewById(R.id.bundlelist);
        bundles = new java.util.ArrayList<org.osgi.framework.Bundle>();
        BundleContext context = frame.getSystemBundleContext();
        for (int i = 0; i < context.getBundles().length; i++) {
            // 获取已安装插件
            bundles.add(context.getBundles()[i]);
        }
        adapter = new BoundListAdapter(this);
        adapter.setList(bundles);
        bundlelist.setAdapter(adapter);
    }

    /**
     * 安装本地插件服务调用 详细接口参见 http://www.apkplug.com/javadoc/bundledoc1.5.3/
     * org.apkplug.Bundle 接口 BundleControl
     * 
     * @param path
     * @param callback
     *            安装插件的回掉函数
     * @throws Exception
     */
    public void install(String path, installCallback callback) throws Exception {
        System.out.println("安装 :" + path);
        BundleContext mcontext = frame.getSystemBundleContext();
        ServiceReference reference = mcontext.getServiceReference(BundleControl.class.getName());
        if (null != reference) {
            BundleControl service = (BundleControl) mcontext.getService(reference);
            if (service != null) {
                service.install(mcontext, path, callback);
            }
            mcontext.ungetService(reference);
        }
    }

    /**
     * 监听插件安装事件，当有新插件安装或卸载时成功也更新一下
     */
    public void ListenerBundleEvent() {
        frame.getSystemBundleContext().addBundleListener(new SynchronousBundleListener() {
            public void bundleChanged(BundleEvent event) {
                // 把插件列表清空
                bundles.clear();
                BundleContext context = frame.getSystemBundleContext();
                for (int i = 0; i < context.getBundles().length; i++) {
                    bundles.add(context.getBundles()[i]);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    public String showBundle(org.osgi.framework.Bundle b) {
        StringBuffer sb = new StringBuffer();
        sb.append("\n插件名称:" + b.getName());
        sb.append("\n插件应用名称:" + b.getSymbolicName());
        sb.append("\n插件版本:" + b.getVersion());
        sb.append("\n插件ID:" + b.getBundleId());
        sb.append("\n插件当前状态:" + b.getState());
        sb.append("\n插件启动Activity:" + b.getBundleActivity());
        return sb.toString();
    }
}
