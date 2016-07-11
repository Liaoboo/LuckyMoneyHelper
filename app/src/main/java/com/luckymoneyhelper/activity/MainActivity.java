package com.luckymoneyhelper.activity;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.luckymoneyhelper.R;
import com.luckymoneyhelper.constants.Const;
import com.luckymoneyhelper.services.LuckyMoneyMonitorService;

import java.util.List;

/**
 * 主函数
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, AccessibilityManager.AccessibilityStateChangeListener,CompoundButton.OnCheckedChangeListener {
    private Button btn_open_close;
    private AccessibilityManager mAaccessibilityManager;  //无障碍服务管理
    private CheckBox cb_prompt_tone,cb_unlock,cb_super;
    private SharedPreferences.Editor editor;

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
        cb_prompt_tone = (CheckBox) findViewById(R.id.cb_prompt_tone);
        cb_unlock = (CheckBox) findViewById(R.id.cb_unlock);
        cb_super = (CheckBox) findViewById(R.id.cb_super);
    }

    public void initParams() {
        mAaccessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        updateServiceStatus();
        // 开启服务
        Intent intent = new Intent(MainActivity.this, LuckyMoneyMonitorService.class);
        startService(intent);
        SharedPreferences sharedPreferences = getSharedPreferences(Const.CONFIG, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        boolean music = sharedPreferences.getBoolean(Const.IS_PROMPT_TONE, true);
        boolean unlock = sharedPreferences.getBoolean(Const.IS_UNLOCK, false);
        boolean super_modle = sharedPreferences.getBoolean(Const.IS_SUPER_MODEL, false);
        cb_prompt_tone.setChecked(music);
        cb_unlock.setChecked(unlock);
        cb_super.setChecked(super_modle);
    }

    public void initListener() {
        btn_open_close.setOnClickListener(this);
        cb_prompt_tone.setOnCheckedChangeListener(this);
        cb_unlock.setOnCheckedChangeListener(this);
        cb_super.setOnCheckedChangeListener(this);
        mAaccessibilityManager.addAccessibilityStateChangeListener(this);
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
     * 更新当前 LuckyMoneyDealService 显示状态
     */
    private void updateServiceStatus() {
        if (isServiceEnabled()) {
            btn_open_close.setText(R.string.app_service_close);
        } else {
            btn_open_close.setText(R.string.app_service_open);
        }
    }

    /**
     * 获取 LuckyMoneyDealService 是否启用
     *
     * @return
     */
    private boolean isServiceEnabled() {
        List<AccessibilityServiceInfo> accessibilityServices =
                mAaccessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(getPackageName() + "/.services.LuckyMoneyDealService")) {
                return true;
            }
        }
        return false;
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
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_prompt_tone:
                editor.putBoolean(Const.IS_PROMPT_TONE, isChecked);
                editor.commit();
                break;
            case R.id.cb_unlock:
                editor.putBoolean(Const.IS_UNLOCK, isChecked);
                editor.commit();
                break;
            case R.id.cb_super:
                editor.putBoolean(Const.IS_SUPER_MODEL, isChecked);
                editor.commit();
                break;
        }
    }
}
