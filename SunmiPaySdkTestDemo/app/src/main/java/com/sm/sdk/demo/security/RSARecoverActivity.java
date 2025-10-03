package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;

import java.util.Arrays;

public class RSARecoverActivity extends BaseAppCompatActivity {
    private EditText mEditData;
    private EditText mEditKeyIndex;
    private TextView mTvInfo;
    private int paddingMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rsa_recover);
        initToolbarBringBack(R.string.security_rsa_recover);
        initView();
    }

    private void initView() {
        RadioGroup rgPaddingMode = findViewById(R.id.rg_rsa_padding_mode);
        rgPaddingMode.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_no_padding:
                    paddingMode = Security.NOTHING_PADDING;
                    mEditData.setText("00D0FC4E7AAD42D94C28B94E6FB14B9992BF31E2BBD9347C8A85432D8FFF14761CAC8671ACF0D2985C97D6C316C97E0DD9AE97F1D61FA5B5996E676714F2612ED38E9294361DBA7634D54621C5100F2FF12C153DDC3F71D74C5761DDB9522079DD8D6D7E3DC7441162FEB6E6B2C6A18A4495AFF2C7EA7457BAE33A14174D4B90D0AF6C088D8BE5E33B4485649D41C95C5017D54598EACE073A85CB357C40275737345148E4C6B23E12E09994FDC1F0ED6522FC5E92934B0FD09135D7557257F5FE0AF53EFC390A8A9940210A212FA909B1DB55CDAFE62722BA47AD1358BCBB1464BE7F64990C657DD9E93923423524B9924E4B6B7C2E50EB2DE299CE582A82AD");
                    break;
                case R.id.rb_pkcs1_padding:
                    paddingMode = Security.PKCS1_PADDING;
                    mEditData.setText(null);
                    break;
                case R.id.rb_pkcs7_padding:
                    paddingMode = Security.PKCS7_PADDING;
                    mEditData.setText(null);
                    break;
                case R.id.rb_pkcs5_padding:
                    paddingMode = Security.PKCS5_PADDING;
                    mEditData.setText(null);
                    break;
                case R.id.rb_pkcs1_oaep_padding:
                    paddingMode = Security.PKCS1_OAEP_PADDING;
                    mEditData.setText(null);
                    break;
                case R.id.rb_pkcs1_v1_5_sha512_padding:
                    paddingMode = Security.PKCS1_V1_5_SHA512;
                    mEditData.setText(null);
                    break;
            }
        });
        mEditData = findViewById(R.id.source_data);
        mEditKeyIndex = findViewById(R.id.key_index);
        mTvInfo = findViewById(R.id.tv_info);
        findViewById(R.id.mb_ok).setOnClickListener(this);
        rgPaddingMode.check(R.id.rb_no_padding);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_ok:
                rsaRecover();
                break;
        }
    }

    private void rsaRecover() {
        try {
            String dataStr = mEditData.getText().toString();
            String keyIndexStr = mEditKeyIndex.getText().toString();

            int keyIndex;
            try {
                keyIndex = Integer.parseInt(keyIndexStr);
                if (keyIndex < 0 || keyIndex > 19) {
                    showToast(R.string.security_rsa_key_hint);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast(R.string.security_rsa_key_hint);
                return;
            }
            if (dataStr.trim().length() == 0) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            if (dataStr.length() % 2 != 0) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataStr);
            byte[] dataOut = new byte[2048];
            addStartTimeWithClear("rsaEncryptOrDecryptData()");
            int len = MyApplication.app.securityOptV2.rsaEncryptOrDecryptData(keyIndex, paddingMode, dataIn, dataOut); //sp签名
            addEndTime("rsaEncryptOrDecryptData()");
            showSpendTime();
            if (len < 0) {
                String msg = "rsaEncryptOrDecryptData() failed, code:" + len;
                showToast(msg);
                LogUtil.e(TAG, msg);
            } else {
                byte[] valid = Arrays.copyOf(dataOut, len);
                String hexStr = ByteUtil.bytes2HexStr(valid);
                LogUtil.e(TAG, "rsaRecover() output:" + hexStr);
                mTvInfo.setText(hexStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}