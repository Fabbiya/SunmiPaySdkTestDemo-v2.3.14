package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;

public class SysSetWakeupActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_sys_set_wakeup);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.basic_sys_set_wakeup);
        findViewById(R.id.btn_ok).setOnClickListener((v) -> setSystemWakeup());
    }

    /**
     * Set system wakeup source
     * <br/> There are 3 types wakeup source represented by
     * <br/> param channel: 1-IC card wakeup, 2-Magnetic stripe card wakeup, 3-Physical key wakeup
     * <br/> param mode indicate each channel status, 0-disable, 1-enable
     * <br/> param attr is RFU(reserved for future use)
     */
    private void setSystemWakeup() {
        try {
            EditText edtChannel = findViewById(R.id.edt_channel);
            String channelStr = edtChannel.getText().toString();
            if (TextUtils.isEmpty(channelStr) || !TextUtils.isDigitsOnly(channelStr)) {
                showToast("channel should not be empty");
                edtChannel.requestFocus();
                return;
            }
            int channel = Integer.parseInt(channelStr);
            if (channel < 0 || channel > 3) {
                showToast("channel should be in [1,3]");
                edtChannel.requestFocus();
                return;
            }
            RadioButton rdoDisable = findViewById(R.id.rdo_mode_disable);
            int mode = rdoDisable.isChecked() ? 0 : 1;
            int code = MyApplication.app.basicOptV2.sysSetWakeup(channel, mode, new Bundle());
            String msg = "set system wakeup source " + (code == 0 ? "success" : "failed,code:" + code);
            LogUtil.e(Constant.TAG, msg);
            showToast(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
