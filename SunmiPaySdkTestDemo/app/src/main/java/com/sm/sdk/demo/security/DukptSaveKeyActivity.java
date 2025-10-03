package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.DeviceUtil;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;

public class DukptSaveKeyActivity extends BaseAppCompatActivity {
    private EditText mEditKSN;
    private EditText mEditKeyIndex;
    private EditText mEditKeyValue;
    private EditText mEditCheckValue;

    private int mKeyType = AidlConstantsV2.Security.KEY_TYPE_DUPKT_IPEK;
    private int mKeyAlgType = AidlConstantsV2.Security.KEY_ALG_TYPE_3DES;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_dukpt_save_key);
        initToolbarBringBack(R.string.security_DuKpt_save_key);
        initView();
    }

    private void initView() {
        RadioGroup keyTypeRadioGroup = findViewById(R.id.key_type);
        keyTypeRadioGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_pek:
                            mKeyType = AidlConstantsV2.Security.KEY_TYPE_DUPKT_IPEK;
                            break;
                        case R.id.rb_bdk:
                            mKeyType = AidlConstantsV2.Security.KEY_TYPE_DUPKT_BDK;
                            break;
                    }
                }
        );

        RadioGroup keyAlgTypeRadioGroup = findViewById(R.id.key_alg_type);
        keyAlgTypeRadioGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_3des:
                            mKeyAlgType = AidlConstantsV2.Security.KEY_ALG_TYPE_3DES;
                            break;
                    }
                }
        );

        mEditKSN = findViewById(R.id.ksn);
        mEditKeyIndex = findViewById(R.id.key_index);
        mEditKeyValue = findViewById(R.id.key_value);
        mEditCheckValue = findViewById(R.id.check_value);
        findViewById(R.id.mb_ok).setOnClickListener(this);
        if (DeviceUtil.isBrazilCKD()) {
            mEditKeyIndex.setHint(getString(R.string.security_key_index) + "(0~99)");
        } else {
            mEditKeyIndex.setHint(getString(R.string.security_key_index) + "(0-9,1100-1199)");
        }
        mEditKSN.setText("FFFF9876543210E00000");
        mEditKeyValue.setText("86772A2D72A29EF0A4D03ED5074DB927");
        mEditCheckValue.setText("F2204B822FD84A65");
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ok:
                saveKey();
                break;
        }
    }

    private void saveKey() {
        try {
            String ksnStr = mEditKSN.getText().toString().trim();
            String keyValueStr = mEditKeyValue.getText().toString().trim();
            String keyIndexStr = mEditKeyIndex.getText().toString().trim();
            String checkValueStr = mEditCheckValue.getText().toString().trim();
            if (keyValueStr.length() == 0 || keyValueStr.length() % 8 != 0) {
                showToast(R.string.security_key_value_hint);
                return;
            }
            if (ksnStr.length() != 20) {
                showToast(R.string.security_ksn_hint);
                return;
            }
            int keyIndex;
            try {
                keyIndex = Integer.parseInt(keyIndexStr);
                if (!KeyIndexUtil.checkDukpt3DesKeyIndex(keyIndex)) {
                    showKeyIndexToast();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showKeyIndexToast();
                return;
            }
            byte[] ksnBytes = ByteUtil.hexStr2Bytes(ksnStr);
            byte[] keyValue = ByteUtil.hexStr2Bytes(keyValueStr);
            byte[] checkValue = ByteUtil.hexStr2Bytes(checkValueStr);
            addStartTimeWithClear("saveKeyDukpt()");
            int result = MyApplication.app.securityOptV2.saveKeyDukpt(mKeyType, keyValue, checkValue, ksnBytes, mKeyAlgType, keyIndex);
            addEndTime("saveKeyDukpt()");
            toastHint(result);
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showKeyIndexToast() {
        if (DeviceUtil.isBrazilCKD()) {
            showToast(R.string.security_duKpt_dea_key_index_hint);
        } else {
            showToast(R.string.security_duKpt_3des_key_hint);
        }
    }


}
