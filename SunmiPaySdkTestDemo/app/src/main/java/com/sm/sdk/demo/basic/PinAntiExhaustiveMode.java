package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;

public class PinAntiExhaustiveMode extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_antiexhaustive_mode);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.pin_anti_exhaustive_mode);
        findViewById(R.id.btn_set).setOnClickListener(this);
        findViewById(R.id.btn_get).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_set:
                setPinAntiExhaustiveProtectionMode();
                break;
            case R.id.btn_get:
                getPinAntiExhaustiveProtectionMode();
                break;
        }
    }

    /**
     * Set PIN anti-exhaustive mode
     * <br/>There're 5 levels can be set:
     * <br/>1-2 min 4 times
     * <br/>2-6 min 12 times
     * <br/>3-15 min 30 times
     * <br/>4-30 min 60 times
     * <br/>5-60 min 120 times
     */
    private void setPinAntiExhaustiveProtectionMode() {
        try {
            EditText edt = findViewById(R.id.edt_set_mode);
            String levelStr = edt.getText().toString();
            if (TextUtils.isEmpty(levelStr)) {
                showToast("level should not be empty");
                edt.requestFocus();
                return;
            }
            int level = Integer.parseInt(levelStr);
            if (level < 1 || level > 5) {
                showToast("level should in [1,5]");
                edt.requestFocus();
                return;
            }
            addStartTimeWithClear("setAntiExhaustiveProtectionMode()");
            int waitTime = MyApplication.app.pinPadOptV2.setAntiExhaustiveProtectionMode(level);
            addEndTime("setAntiExhaustiveProtectionMode()");
            String msg = null;
            if (waitTime < 0) {
                msg = Utility.formatStr("failed, code: %d", waitTime);
            } else {
                msg = Utility.formatStr("success, new mode will take effect after %d minutes", waitTime);
            }
            showToast(msg);
            LogUtil.e(TAG, msg);
            showSpendTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Get PIN anti-exhaustive mode(current level,range 1-5) */
    private void getPinAntiExhaustiveProtectionMode() {
        try {
            addStartTimeWithClear("getAntiExhaustiveProtectionMode()");
            int level = MyApplication.app.pinPadOptV2.getAntiExhaustiveProtectionMode();
            addEndTime("getAntiExhaustiveProtectionMode()");
            String msg = null;
            if (level < 0) {
                msg = Utility.formatStr("failed, code: %d", level);
            } else {
                EditText edt = findViewById(R.id.edt_get_mode);
                edt.setText(String.valueOf(level));
                msg = Utility.formatStr("success, current level: %d", level);
            }
            showToast(msg);
            LogUtil.e(TAG, msg);
            showSpendTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
