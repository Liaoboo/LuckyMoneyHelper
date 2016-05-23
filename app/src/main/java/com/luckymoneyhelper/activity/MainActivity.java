package com.luckymoneyhelper.activity;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.Toast;

import com.luckymoneyhelper.R;

import java.util.List;

/**
 * 主函数
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, AccessibilityManager.AccessibilityStateChangeListener {
    private Button btn_open_close;
    private AccessibilityManager mAaccessibilityManager;  //无障碍服务 管理

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        initViews();
        initParams();
        initListener();
    }

    public void initViews() {
        btn_open_close = (Button) findViewById(R.id.btn_open_close);
    }

    public void initParams() {
        mAaccessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        updateServiceStatus();
    }

    public void initListener() {
        btn_open_close.setOnClickListener(this);
        mAaccessibilityManager.addAccessibilityStateChangeListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_close:
                intentSet();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        //移除监听服务
        mAaccessibilityManager.removeAccessibilityStateChangeListener(this);
        super.onDestroy();
    }

    /**
     *  跳转设置
     */
    public void intentSet() {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.app_error_tip), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onAccessibilityStateChanged(boolean enabled) {
        updateServiceStatus();
    }

    /**
     * 更新当前 LuckyMoneyService 显示状态
     */
    private void updateServiceStatus() {
        if (isServiceEnabled()) {
            btn_open_close.setText(R.string.app_service_close);
        } else {
            btn_open_close.setText(R.string.app_service_open);
        }
    }

    /**
     * 获取 LuckyMoneyService 是否启用
     *
     * @return
     */
    private boolean isServiceEnabled() {
        List<AccessibilityServiceInfo> accessibilityServices =
                mAaccessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(getPackageName() + "/.services.LuckyMoneyService")) {
                return true;
            }
        }
        return false;
    }
}
