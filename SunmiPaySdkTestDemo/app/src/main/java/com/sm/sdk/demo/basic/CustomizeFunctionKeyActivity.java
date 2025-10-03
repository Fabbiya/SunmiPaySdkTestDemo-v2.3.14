package com.sm.sdk.demo.basic;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.DeviceUtil;
import com.sm.sdk.demo.utils.LogUtil;

public class CustomizeFunctionKeyActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_function_key);
        initToolbarBringBack(R.string.basic_customize_function_key);
        findViewById(R.id.btn_volume_up_1).setOnClickListener(this);
        findViewById(R.id.btn_volume_down_1).setOnClickListener(this);
        findViewById(R.id.btn_native_1).setOnClickListener(this);
        findViewById(R.id.btn_volume_up_2).setOnClickListener(this);
        findViewById(R.id.btn_volume_down_2).setOnClickListener(this);
        findViewById(R.id.btn_native_2).setOnClickListener(this);
        if (DeviceUtil.isP2Mini()) {
            findViewById(R.id.card_view_2).setVisibility(View.VISIBLE);
        }
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_volume_up_1://left function key
                setAsVolumeUp("volume_2");
                break;
            case R.id.btn_volume_down_1://left function key
                setAsVolumeDown("volume_2");
                break;
            case R.id.btn_native_1://left function key
                setAsNative("volume_2");
                break;
            case R.id.btn_volume_up_2://right function key
                setAsVolumeUp("volume_1");
                break;
            case R.id.btn_volume_down_2://right function key
                setAsVolumeDown("volume_1");
                break;
            case R.id.btn_native_2://right function key
                setAsNative("volume_1");
                break;
        }
    }

    private void setAsVolumeUp(String key) {
        try {
            Bundle bundle = new Bundle();
            //customize function key
            bundle.putString("key", key);
            //set volume2 key's function
            bundle.putString("type", "function");
            //set value as volume up
            bundle.putString("value", "volume_up");
            int code = MyApplication.app.basicOptV2.customizeFunctionKey(bundle);
            LogUtil.e(TAG, "setAsVolumeUp code:" + code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAsVolumeDown(String key) {
        try {
            Bundle bundle = new Bundle();
            //customize function key
            bundle.putString("key", key);
            //set volume2 key's function
            bundle.putString("type", "function");
            //set value as volume up
            bundle.putString("value", "volume_down");
            int code = MyApplication.app.basicOptV2.customizeFunctionKey(bundle);
            LogUtil.e(TAG, "setAsVolumeDown code:" + code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAsNative(String key) {
        try {
            Bundle bundle = new Bundle();
            //customize function key
            bundle.putString("key", key);
            //set volume2 key's function
            bundle.putString("type", "native");
            //set value as native
            if (DeviceUtil.isP2Mini()) {
                bundle.putString("value", "native_action");
            } else {
                bundle.putString("value", "native");
            }
            int code = MyApplication.app.basicOptV2.customizeFunctionKey(bundle);
            LogUtil.e(TAG, "setAsNative code:" + code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
