package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.system.BasicOptV2;

public class ScreenModelActivity extends BaseAppCompatActivity {
    private final BasicOptV2 basicOptV2 = MyApplication.app.basicOptV2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_screen_model);
        initView();
    }

    private void initView() {
        getWindow().getDecorView().setKeepScreenOn(true);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.basic_screen_mode);
        toolbar.setNavigationIcon(R.drawable.ic_back_white);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(
                v -> onBackPressed()
        );
        findViewById(R.id.mb_set_screen_monopoly).setOnClickListener(this);
        findViewById(R.id.mb_clear_screen_monopoly).setOnClickListener(this);
        findViewById(R.id.mb_disable_status_bar_drop_down).setOnClickListener(this);
        findViewById(R.id.mb_enable_status_bar_drop_down).setOnClickListener(this);
        findViewById(R.id.mb_hide_nav_bar).setOnClickListener(this);
        findViewById(R.id.mb_show_nav_bar).setOnClickListener(this);
        findViewById(R.id.mb_hide_nav_back).setOnClickListener(this);
        findViewById(R.id.mb_hide_nav_home).setOnClickListener(this);
        findViewById(R.id.mb_hide_nav_recent).setOnClickListener(this);
        findViewById(R.id.mb_hide_nav_back_and_home).setOnClickListener(this);
        findViewById(R.id.mb_hide_nav_back_and_recent).setOnClickListener(this);
        findViewById(R.id.mb_hide_nav_home_and_recent).setOnClickListener(this);
        findViewById(R.id.mb_hide_nav_all).setOnClickListener(this);
        findViewById(R.id.mb_show_nav_all).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.mb_set_screen_monopoly:
                    setScreenMode(AidlConstantsV2.SystemUI.SET_SCREEN_MONOPOLY);
                    break;
                case R.id.mb_clear_screen_monopoly:
                    setScreenMode(AidlConstantsV2.SystemUI.CLEAR_SCREEN_MONOPOLY);
                    break;
                case R.id.mb_disable_status_bar_drop_down:
                    setStatusBarDropDownMode(AidlConstantsV2.SystemUI.DISABLE_STATUS_BAR_DROP_DOWN);
                    break;
                case R.id.mb_enable_status_bar_drop_down:
                    setStatusBarDropDownMode(AidlConstantsV2.SystemUI.ENABLE_STATUS_BAR_DROP_DOWN);
                    break;
                case R.id.mb_hide_nav_bar:
                    setNavigationBarVisibility(AidlConstantsV2.SystemUI.HIDE_NAV_BAR);
                    break;
                case R.id.mb_show_nav_bar:
                    setNavigationBarVisibility(AidlConstantsV2.SystemUI.SHOW_NAV_BAR);
                    break;
                case R.id.mb_hide_nav_back:
                    setHideNavigationBarItems(AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_BACK_KEY);
                    break;
                case R.id.mb_hide_nav_home:
                    setHideNavigationBarItems(AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_HOME_KEY);
                    break;
                case R.id.mb_hide_nav_recent:
                    setHideNavigationBarItems(AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_RECENT_KEY);
                    break;
                case R.id.mb_hide_nav_back_and_home:
                    setHideNavigationBarItems(AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_BACK_KEY
                            | AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_HOME_KEY);
                    break;
                case R.id.mb_hide_nav_back_and_recent:
                    setHideNavigationBarItems(AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_BACK_KEY
                            | AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_RECENT_KEY);
                    break;
                case R.id.mb_hide_nav_home_and_recent:
                    setHideNavigationBarItems(AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_HOME_KEY
                            | AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_RECENT_KEY);
                    break;
                case R.id.mb_hide_nav_all:
                    setHideNavigationBarItems(AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_BACK_KEY
                            | AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_HOME_KEY
                            | AidlConstantsV2.SystemUI.HIDE_NAV_ITEM_RECENT_KEY);
                    break;
                case R.id.mb_show_nav_all:
                    setHideNavigationBarItems(0);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setScreenMode(int key) {
        try {
            addStartTimeWithClear("setScreenMode()");
            basicOptV2.setScreenMode(key);
            addEndTime("setScreenMode()");
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setStatusBarDropDownMode(int key) {
        try {
            addStartTimeWithClear("setStatusBarDropDownMode()");
            basicOptV2.setStatusBarDropDownMode(key);
            addEndTime("setStatusBarDropDownMode()");
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setNavigationBarVisibility(int key) {
        try {
            addStartTimeWithClear("setNavigationBarVisibility()");
            basicOptV2.setNavigationBarVisibility(key);
            addEndTime("setNavigationBarVisibility()");
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setHideNavigationBarItems(int key) {
        try {
            addStartTimeWithClear("setHideNavigationBarItems()");
            basicOptV2.setHideNavigationBarItems(key);
            addEndTime("setHideNavigationBarItems()");
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            basicOptV2.setScreenMode(AidlConstantsV2.SystemUI.CLEAR_SCREEN_MONOPOLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
