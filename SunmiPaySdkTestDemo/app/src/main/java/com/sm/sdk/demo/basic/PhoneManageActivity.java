package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;

public class PhoneManageActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_manage);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.basic_phone_manage);
        RadioGroup group = findViewById(R.id.rdo_group_phone_call);
        if (getPhoneCallStatus()) {
            group.check(R.id.rdo_enable);
        } else {
            group.check(R.id.rdo_disable);
        }
        findViewById(R.id.btn_ok).setOnClickListener(v -> setPhoneCallStatus());
    }

    /** Get phone call status */
    private boolean getPhoneCallStatus() {
        try {
            int value = Settings.Secure.getInt(MyApplication.app.getContentResolver(), "is_open_phone_sms", 0);
            LogUtil.e(TAG, "is_open_phone_sms: " + value);
            return value == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** enable or disable phone call */
    private void setPhoneCallStatus() {
        try {
            RadioButton rdoEnable = findViewById(R.id.rdo_enable);
            int code = MyApplication.app.basicOptV2.enablePhoneCall(rdoEnable.isChecked());
            LogUtil.e(TAG, "enablePhoneCall code:" + code);
            String msg = null;
            if (code == 0) {//success
                if (rdoEnable.isChecked()) {
                    msg = "enable phone call success";
                } else {
                    msg = "disable phone call success";
                }
            } else {//failed
                if (rdoEnable.isChecked()) {
                    msg = "enable phone call failed";
                } else {
                    msg = "disable phone call failed";
                }
            }
            showToast(msg);
            LogUtil.e(TAG, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
