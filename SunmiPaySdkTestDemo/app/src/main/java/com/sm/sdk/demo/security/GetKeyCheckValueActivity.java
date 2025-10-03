package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.DeviceUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;

public class GetKeyCheckValueActivity extends BaseAppCompatActivity {
    private int keySystem = Security.SEC_MKSK;
    private int kcvMode = Security.KCV_MODE_CHK0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_get_kcv);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_get_kcv);
        RadioGroup group = findViewById(R.id.key_system_group);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            if (checkedId == R.id.rdo_sys_mksk) {
                keySystem = Security.SEC_MKSK;
            } else if (checkedId == R.id.rdo_sys_dukpt) {
                keySystem = Security.SEC_DUKPT;
            }
        });
        group.check(R.id.rdo_sys_mksk);
        group = findViewById(R.id.kcv_mode_group);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            switch (checkedId) {
                case R.id.kcv_mode_nochk:
                    kcvMode = Security.KCV_MODE_NOCHK;
                    break;
                case R.id.kcv_mode_chk0:
                    kcvMode = Security.KCV_MODE_CHK0;
                    break;
                case R.id.kcv_mode_chkfix:
                    kcvMode = Security.KCV_MODE_CHKFIX;
                    break;
                case R.id.kcv_mode_chkmac:
                    kcvMode = Security.KCV_MODE_CHKMAC;
                    break;
                case R.id.kcv_mode_chkcmac:
                    kcvMode = Security.KCV_MODE_CHKCMAC;
                    break;
                case R.id.kcv_mode_chkfix_16:
                    kcvMode = Security.KCV_MODE_CHKFIX_16;
                    break;
                case R.id.kcv_mode_chkbuf:
                    kcvMode = Security.KCV_MODE_CHK_BUF;
                    break;
                case R.id.kcv_mode_chkcmac_buf:
                    kcvMode = Security.KCV_MODE_CHKCMAC_BUF;
                    break;
            }
        });
        group.check(R.id.kcv_mode_chk0);
        findViewById(R.id.mb_get_kcv).setOnClickListener((v) -> getKcv());
    }

    private void getKcv() {
        try {
            String targetPkgName = this.<EditText>findViewById(R.id.edt_target_pkg_name).getText().toString();
            String keyIndexStr = this.<EditText>findViewById(R.id.key_index).getText().toString();
            int keyIndex = -1;
            if (keySystem == Security.SEC_MKSK) {
                if (TextUtils.isEmpty(keyIndexStr)) {
                    showToast(R.string.security_mksk_key_index_hint);
                    return;
                }
                keyIndex = Integer.parseInt(keyIndexStr);
                if (keyIndex > 199 || keyIndex < 0) {
                    showToast(R.string.security_mksk_key_index_hint);
                    return;
                }
            } else if (keySystem == Security.SEC_DUKPT) {
                if (TextUtils.isEmpty(keyIndexStr)) {
                    showKeyIndexToast();
                    return;
                }
                keyIndex = Integer.parseInt(keyIndexStr);
                if (!KeyIndexUtil.checkDukptKeyIndex(keyIndex)) {
                    showKeyIndexToast();
                    return;
                }
            }
            byte[] dataOut = new byte[4];
            addStartTimeWithClear("getKeyCheckValue()");
            Bundle bundle = new Bundle();
            bundle.putInt("keySystem", keySystem);
            bundle.putInt("keyIndex", keyIndex);
            bundle.putInt("kcvMode", kcvMode);
            if (!TextUtils.isEmpty(targetPkgName)) {
                bundle.putString("targetAppPkgName", targetPkgName);
            }
            int code = MyApplication.app.securityOptV2.getKeyCheckValueEx(bundle, dataOut);
            addEndTime("getKeyCheckValue()");
            if (code < 0) {
                String msg = "Get kcv error:" + code;
                LogUtil.e(TAG, msg);
                showToast(msg);
            } else {
                String hexKcv = ByteUtil.bytes2HexStr(dataOut);
                this.<TextView>findViewById(R.id.tv_info).setText("KCV:" + hexKcv);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
            showToast("key illegal key index");
        }
    }

    private void showKeyIndexToast() {
        if (DeviceUtil.isBrazilCKD()) {
            showToast(R.string.security_duKpt_key_index_hint);
        } else {
            showToast(R.string.security_dukpt_key_index_hint);
        }
    }

}
