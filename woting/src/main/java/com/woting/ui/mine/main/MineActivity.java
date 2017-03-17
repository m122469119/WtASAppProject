package com.woting.ui.mine.main;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.WindowManager;

import com.woting.R;
import com.woting.common.util.SequenceUUID;
import com.woting.common.util.ToastUtils;
import com.woting.ui.main.MainActivity;

/**
 * 个人信息主页
 * 作者：xinlong on 2016/11/6 21:18
 * 邮箱：645700751@qq.com
 */
public class MineActivity extends FragmentActivity {
    private static MineActivity context;
    public static boolean isVisible = true;// 是否可见

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine);
        context = this;
        setType();

        MineActivity.open(new MineFragment());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setType() {
        String a = android.os.Build.VERSION.RELEASE;
        Log.e("系统版本号", a + "");
        Log.e("系统版本号截取", a.substring(0, a.indexOf(".")) + "");
        boolean v = false;
        if (Integer.parseInt(a.substring(0, a.indexOf("."))) >= 5) {
            v = true;
        }
        if (v) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);        // 透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);    // 透明导航栏
        }
    }


    // 打开新的 Fragment
    public static void open(Fragment frg) {
        context.getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_content, frg)
                .addToBackStack(SequenceUUID.getUUID())
                .commit();
        if (context.getSupportFragmentManager().getBackStackEntryCount() > 0) {
            MainActivity.hideOrShowTab(false);
            isVisible = false;
        }
    }

    // 关闭已经打开的 Fragment
    public static void close() {
        context.getSupportFragmentManager().popBackStackImmediate();
        if (context.getSupportFragmentManager().getBackStackEntryCount() == 1) {
            MainActivity.hideOrShowTab(true);
            isVisible = true;
        }
    }

    private long tempTime;

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            MainActivity.hideOrShowTab(true);
            long time = System.currentTimeMillis();
            if (time - tempTime <= 2000) {
                android.os.Process.killProcess(android.os.Process.myPid());
            } else {
                tempTime = time;
                ToastUtils.show_always(this, "再按一次退出");
            }
        } else {
            close();
        }
    }
}