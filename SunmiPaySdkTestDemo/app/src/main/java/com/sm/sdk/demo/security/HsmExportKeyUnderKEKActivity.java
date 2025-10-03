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
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;

import java.util.Arrays;

public class HsmExportKeyUnderKEKActivity extends BaseAppCompatActivity {
    private TextView tvResult;
    private int paddingMode = Security.NOTHING_PADDING;
    private int keySystem = Security.SEC_MKSK;
    private int kekKeySystem = Security.SEC_MKSK;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hsm_export_key_under_kek);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.hsm_export_key_under_kek);
        RadioGroup group = findViewById(R.id.rg_key_system);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_1_mksk:
                    keySystem = Security.SEC_MKSK;
                    break;
                case R.id.rb_1_rsa:
                    keySystem = Security.SEC_RSA_KEY;
                    break;
            }
        });
        group = findViewById(R.id.rg_kek_key_system);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_2_mksk:
                    kekKeySystem = Security.SEC_MKSK;
                    break;
                case R.id.rb_3_rsa:
                    kekKeySystem = Security.SEC_RSA_KEY;
                    break;
            }
        });
        group = findViewById(R.id.rg_padding_mode);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_mode_none:
                    paddingMode = Security.NOTHING_PADDING;
                    break;
                case R.id.rb_mode_pkcs1:
                    paddingMode = Security.PKCS1_PADDING;
                    break;
                case R.id.rb_mode_pkcs7:
                    paddingMode = Security.PKCS7_PADDING;
                    break;
                case R.id.rb_mode_pkcs5:
                    paddingMode = Security.PKCS5_PADDING;
                    break;
                case R.id.rb_mode_pkcs1_oaep:
                    paddingMode = Security.PKCS1_OAEP_PADDING;
                    break;
            }
        });
        tvResult = findViewById(R.id.tv_result);
        findViewById(R.id.btn_ok).setOnClickListener((v) -> exportKeyUnderKEK());
    }

    private void exportKeyUnderKEK() {
        try {
            EditText edtKeyIndex = findViewById(R.id.edt_key_index);
            EditText edtKEKIndex = findViewById(R.id.edt_kek_index);
            String keyIndexStr = edtKeyIndex.getText().toString();
            String kekIndexStr = edtKEKIndex.getText().toString();
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast(R.string.security_mksk_key_index_hint);
                edtKeyIndex.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(kekIndexStr)) {
                showToast(R.string.security_mksk_key_index_hint);
                edtKEKIndex.requestFocus();
                return;
            }
            int keyIndex = Integer.parseInt(keyIndexStr);
            int kekIndex = Integer.parseInt(kekIndexStr);

            Bundle bundle = new Bundle();
            bundle.putInt("keySystem", keySystem);
            bundle.putInt("keyIndex", keyIndex);
            bundle.putInt("kekKeySystem", kekKeySystem);
            bundle.putInt("kekIndex", kekIndex);
            bundle.putInt("paddingMode", paddingMode);
            byte[] buffer = new byte[2048];
            int len = MyApplication.app.securityOptV2.hsmExportKeyUnderKEKEx(bundle, buffer);
            String msg = null;
            if (len < 0) {
                msg = "hsmExportKeyUnderKEKEx() failed,code:" + len;
                LogUtil.e(TAG, msg);
                showToast(msg);
            }
            byte[] valid = Arrays.copyOf(buffer, len);
            String keyData = ByteUtil.bytes2HexStr(valid);
            tvResult.setText("keyData:" + keyData);
            LogUtil.e(TAG, "keyData:" + keyData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
