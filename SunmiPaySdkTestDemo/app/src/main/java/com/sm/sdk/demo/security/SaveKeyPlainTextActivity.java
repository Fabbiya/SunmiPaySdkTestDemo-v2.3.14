package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;

public class SaveKeyPlainTextActivity extends BaseAppCompatActivity {

    private EditText mEditKeyIndex;
    private EditText mEditKeyValue;
    private EditText mEditCheckValue;
    private EditText mEditKeyVariant;

    private int mKeyType = Security.KEY_TYPE_KEK;
    private int mKeyAlgType = Security.KEY_ALG_TYPE_3DES;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_save_key_plain_text);
        initToolbarBringBack(R.string.security_save_plaintext_key);
        initView();
    }

    private void initView() {
        RadioGroup keyTypeRadioGroup = findViewById(R.id.key_type);
        keyTypeRadioGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_kek:
                            mKeyType = Security.KEY_TYPE_KEK;
                            break;
                        case R.id.rb_tmk:
                            mKeyType = Security.KEY_TYPE_TMK;
                            break;
                        case R.id.rb_pik:
                            mKeyType = Security.KEY_TYPE_PIK;
                            break;
                        case R.id.rb_tdk:
                            mKeyType = Security.KEY_TYPE_TDK;
                            break;
                        case R.id.rb_mak:
                            mKeyType = Security.KEY_TYPE_MAK;
                            break;
                        case R.id.rb_rec_key:
                            mKeyType = Security.KEY_TYPE_REC;
                            break;
                    }
                }
        );

        RadioGroup keyAlgTypeRadioGroup = findViewById(R.id.key_alg_type);
        keyAlgTypeRadioGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_3des:
                            mKeyAlgType = Security.KEY_ALG_TYPE_3DES;
                            break;
                        case R.id.rb_sm4:
                            mKeyAlgType = Security.KEY_ALG_TYPE_SM4;
                            break;
                        case R.id.rb_aes:
                            mKeyAlgType = Security.KEY_ALG_TYPE_AES;
                            break;
                    }
                }
        );

        mEditKeyValue = findViewById(R.id.key_value);
        mEditKeyIndex = findViewById(R.id.key_index);
        mEditCheckValue = findViewById(R.id.check_value);
        mEditKeyVariant = findViewById(R.id.key_variant);

        findViewById(R.id.mb_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ok:
                savePlainTextKey();
                break;
        }
    }

    private void savePlainTextKey() {
        try {
            String keyValueStr = mEditKeyValue.getText().toString().trim();
            String keyIndexStr = mEditKeyIndex.getText().toString().trim();
            String checkValueStr = mEditCheckValue.getText().toString().trim();
            String keyVariantStr = mEditKeyVariant.getText().toString().trim();

            if (keyValueStr.length() == 0 || keyValueStr.length() % 8 != 0) {
                showToast(R.string.security_key_value_hint);
                return;
            }

            if (checkValueStr.length() != 0) {
                if (mKeyAlgType == Security.KEY_ALG_TYPE_SM4) {
                    if (checkValueStr.length() > 32 || checkValueStr.length() % 4 != 0) {
                        showToast(R.string.security_check_value_hint);
                        return;
                    }
                } else {
                    if (checkValueStr.length() > 16 || checkValueStr.length() % 4 != 0) {
                        showToast(R.string.security_check_value_hint);
                        return;
                    }
                }
            }
            if (!TextUtils.isEmpty(keyVariantStr) && !Utility.checkHexValue(keyVariantStr)) {
                showToast("key variant should be hex string");
                return;
            }

            int keyIndex;
            try {
                keyIndex = Integer.parseInt(keyIndexStr);
                if (keyIndex < 0 || keyIndex > 199) {
                    showToast(R.string.security_mksk_key_index_hint);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast(R.string.security_mksk_key_index_hint);
                return;
            }

            byte[] keyValue = ByteUtil.hexStr2Bytes(keyValueStr);
            byte[] checkValue = ByteUtil.hexStr2Bytes(checkValueStr);
            byte[] keyVariant = ByteUtil.hexStr2Bytes(keyVariantStr);

            Bundle bundle = new Bundle();
            bundle.putInt("keyType", mKeyType);
            bundle.putByteArray("keyValue", keyValue);
            bundle.putByteArray("checkValue", checkValue);
            bundle.putInt("encryptIndex", 0);
            bundle.putInt("keyAlgType", mKeyAlgType);
            bundle.putInt("keyIndex", keyIndex);
            bundle.putInt("variantUsage", Security.KEY_VARIANT_XOR);
            bundle.putByteArray("keyVariant", keyVariant);

            addStartTimeWithClear("savePlaintextKey()");
            int result = MyApplication.app.securityOptV2.saveKeyEx(bundle);
            addEndTime("savePlaintextKey()");
            toastHint(result);
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
