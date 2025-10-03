package com.sm.sdk.demo.security;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.text.method.ReplacementTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;

import java.util.Arrays;

public class HsmKeyShareTestActivity extends BaseAppCompatActivity {

    public static final int LOCAL_KEY_LEN_128 = 1;
    public static final int LOCAL_KEY_LEN_192 = 2;
    public static final int LOCAL_KEY_COMPONENT_1_INDEX = 11;
    public static final int LOCAL_KEY_COMPONENT_2_INDEX = 12;
    public static final int LOCAL_KEY_COMPONENT_3_INDEX = 13;

    public int MAX_LEN_KEY_INDEX = 2;
    public int MAX_LEN_KSN = 20;
    public int KEY_LEN_32 = 32;
    public int KEY_LEN_48 = 48;
    public int mCurrentKeyLen = KEY_LEN_32;
    private int mKeyType = AidlConstantsV2.Security.KEY_TYPE_TMK;
    private int mAlgType = AidlConstantsV2.Security.KEY_ALG_TYPE_3DES;
    private int mKeyLen = AidlConstantsV2.Security.KEY_ALG_TYPE_3DES;

    public int KEY_COMPONENT_TYPE_1 = 1;
    public int KEY_COMPONENT_TYPE_2 = 2;
    public int KEY_COMPONENT_TYPE_3 = 3;
    public int mCurrentKeyComponent = KEY_COMPONENT_TYPE_1;

    TextInputEditText etKeyIndex1;
    TextInputEditText etKeyIndex2;
    TextInputEditText etKeyIndex3;

    TextInputEditText keyComponent1;
    TextInputEditText keyComponent2;
    TextInputEditText keyComponent3;
    TextInputLayout txKeyComponent1;
    TextInputLayout txKeyComponent2;
    TextInputLayout txKeyComponent3;

    TextInputEditText etKeyKsn1;
    TextInputEditText etKeyKsn2;
    TextInputEditText etKeyKsn3;
    TextInputLayout tvKsn1;
    TextInputLayout tvKsn2;
    TextInputLayout tvKsn3;
    MaterialButton btKeyComponent1Confirm;
    MaterialButton btKeyComponent2Confirm;
    MaterialButton btKeyComponent3Confirm;
    MaterialButton btCombineKeyComponentConfirm;
    TextInputEditText keyCombineIndex;

    private boolean isKeyComponent1InjectSuccess = false;
    private boolean isKeyComponent2InjectSuccess = false;
    private boolean isKeyComponent3InjectSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hsm_key_share_test);
        initToolbarBringBack(R.string.hsm_key_share_test);
        initView();
    }

    private void initView() {
        keyComponent1 = findViewById(R.id.key_component_1);
        keyComponent2 = findViewById(R.id.key_component_2);
        keyComponent3 = findViewById(R.id.key_component_3);
        txKeyComponent1 = findViewById(R.id.tx_key_component_1);
        txKeyComponent2 = findViewById(R.id.tx_key_component_2);
        txKeyComponent3 = findViewById(R.id.tx_key_component_3);

        etKeyKsn1 = findViewById(R.id.ksn_1);
        etKeyKsn2 = findViewById(R.id.ksn_2);
        etKeyKsn3 = findViewById(R.id.ksn_3);
        tvKsn1 = findViewById(R.id.layout_ksn_1);
        tvKsn2 = findViewById(R.id.layout_ksn_2);
        tvKsn3 = findViewById(R.id.layout_ksn_3);

        btKeyComponent1Confirm = findViewById(R.id.btn_key_component_1_confirm);
        btKeyComponent2Confirm = findViewById(R.id.btn_key_component_2_confirm);
        btKeyComponent3Confirm = findViewById(R.id.btn_key_component_3_confirm);
        btCombineKeyComponentConfirm = findViewById(R.id.btn_combine_key_component);
        etKeyIndex1 = findViewById(R.id.key_index);
        etKeyIndex2 = findViewById(R.id.key_index_2);
        etKeyIndex3 = findViewById(R.id.key_index_3);
        keyCombineIndex = findViewById(R.id.key_combine_index);

        RadioGroup keyTypeRadioGroup = findViewById(R.id.key_type);
        keyTypeRadioGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_tmk:
                            mKeyType = AidlConstantsV2.Security.KEY_TYPE_TMK;
                            showKsn();
                            break;
                        case R.id.rb_bdk:
                            mKeyType = AidlConstantsV2.Security.KEY_TYPE_DUPKT_BDK;
                            showKsn();
                            break;
                    }
                }
        );

        RadioGroup keyAlgTypeRadioGroup = findViewById(R.id.key_alg_type);
        keyAlgTypeRadioGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_3des:
                            mAlgType = AidlConstantsV2.Security.KEY_ALG_TYPE_3DES;
                            break;
                        case R.id.rb_aes:
                            mAlgType = AidlConstantsV2.Security.KEY_ALG_TYPE_AES;
                            break;
                    }
                }
        );

        RadioGroup keyLenRadioGroup = findViewById(R.id.key_len);
        keyLenRadioGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_key_len_128:
                            mKeyLen = LOCAL_KEY_LEN_128;
                            changeKeyComponentLen();
                            break;
                        case R.id.rb_key_len_192:
                            mKeyLen = LOCAL_KEY_LEN_192;
                            changeKeyComponentLen();
                            break;
                    }
                }
        );

        etKeyKsn1.setTransformationMethod(new AllCapTransformationMethod(true));
        etKeyKsn2.setTransformationMethod(new AllCapTransformationMethod(true));
        etKeyKsn3.setTransformationMethod(new AllCapTransformationMethod(true));
        keyComponent1.setTransformationMethod(new AllCapTransformationMethod(true));
        keyComponent2.setTransformationMethod(new AllCapTransformationMethod(true));
        keyComponent3.setTransformationMethod(new AllCapTransformationMethod(true));
        keyComponent1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mCurrentKeyLen)});
        keyComponent2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mCurrentKeyLen)});
        keyComponent3.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mCurrentKeyLen)});
        keyCombineIndex.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_LEN_KEY_INDEX)});
        etKeyIndex1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_LEN_KEY_INDEX)});
        etKeyIndex2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_LEN_KEY_INDEX)});
        etKeyIndex3.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_LEN_KEY_INDEX)});
        etKeyKsn1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_LEN_KSN)});
        etKeyKsn2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_LEN_KSN)});
        etKeyKsn3.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_LEN_KSN)});
        showKsn();
        btKeyComponent1Confirm.setOnClickListener(this);
        btKeyComponent2Confirm.setOnClickListener(this);
        btKeyComponent3Confirm.setOnClickListener(this);
        btCombineKeyComponentConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_key_component_1_confirm:
                mCurrentKeyComponent = KEY_COMPONENT_TYPE_1;
                confirmKeyComponent();
                break;
            case R.id.btn_key_component_2_confirm:
                mCurrentKeyComponent = KEY_COMPONENT_TYPE_2;
                confirmKeyComponent();
                break;
            case R.id.btn_key_component_3_confirm:
                mCurrentKeyComponent = KEY_COMPONENT_TYPE_3;
                confirmKeyComponent();
                break;
            case R.id.btn_combine_key_component:
                combineKeyComponent();
                break;
        }
    }

    private void showKsn() {
        if (mKeyType == AidlConstantsV2.Security.KEY_TYPE_TMK) {
            tvKsn1.setVisibility(View.GONE);
            tvKsn2.setVisibility(View.GONE);
            tvKsn3.setVisibility(View.GONE);
        } else {
            tvKsn1.setVisibility(View.VISIBLE);
            tvKsn2.setVisibility(View.VISIBLE);
            tvKsn3.setVisibility(View.VISIBLE);
        }
    }

    public void changeKeyComponentLen() {
        if (mKeyLen == LOCAL_KEY_LEN_128) {
            mCurrentKeyLen = KEY_LEN_32;
        } else {
            mCurrentKeyLen = KEY_LEN_48;
        }
        txKeyComponent1.setCounterMaxLength(mCurrentKeyLen);
        keyComponent1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mCurrentKeyLen)});
        txKeyComponent2.setCounterMaxLength(mCurrentKeyLen);
        keyComponent2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mCurrentKeyLen)});
        txKeyComponent3.setCounterMaxLength(mCurrentKeyLen);
        keyComponent3.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mCurrentKeyLen)});
    }

    private void combineKeyComponent() {
        if (isKeyComponent1InjectSuccess && isKeyComponent2InjectSuccess && isKeyComponent3InjectSuccess) { // todo 保留2组密钥分量
            String indexStr = keyCombineIndex.getText().toString();
            if (TextUtils.isEmpty(indexStr)) {
                showToast("KeyIndex不合法，请重新输入");
                return;
            }
            Integer keyIndex = Integer.valueOf(indexStr);
            String kcv = combineKeyComponent(keyIndex, mKeyType, mAlgType);
            if (TextUtils.isEmpty(kcv)) {
                showToast("合成密钥分量失败，请重新尝试");
                return;
            }

            new AlertDialog.Builder(HsmKeyShareTestActivity.this).setTitle("KCV").setMessage(kcv)
                    .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String keyKsnStr = etKeyKsn1.getText().toString();
                            finish();
                        }
                    })
                    .setPositiveButton("取消", null)
                    .show();
        } else {
            showToast("请先输入正确的密钥分量");
        }
    }

    private void confirmKeyComponent() {
        if (mCurrentKeyComponent == KEY_COMPONENT_TYPE_1) {
            confirmKeyComponent(LOCAL_KEY_COMPONENT_1_INDEX, keyComponent1, etKeyKsn1, btKeyComponent1Confirm);
        } else if (mCurrentKeyComponent == KEY_COMPONENT_TYPE_2) {
            confirmKeyComponent(LOCAL_KEY_COMPONENT_2_INDEX, keyComponent2, etKeyKsn2, btKeyComponent2Confirm);
        } else {
            confirmKeyComponent(LOCAL_KEY_COMPONENT_3_INDEX, keyComponent3, etKeyKsn3, btKeyComponent3Confirm);
        }
    }

    private void confirmKeyComponent(int index, TextInputEditText keyComponent, TextInputEditText keyKsn, Button btComfirm) {
        String keyKsnStr = keyKsn.getText().toString();
        if ((mKeyType == AidlConstantsV2.Security.KEY_TYPE_DUPKT_BDK) && (TextUtils.isEmpty(keyKsnStr) || keyKsnStr.length() != MAX_LEN_KSN)) {
            showToast("KSN不合法，请重新输入");
            return;
        }
        String keyCom = keyComponent.getText().toString();
        if (TextUtils.isEmpty(keyCom) || keyCom.length() != mCurrentKeyLen) {
            showToast("密钥分量不合法，请重新输入");
            return;
        }

        int result = injectKeyComponent(index, mKeyType, mAlgType, mKeyLen, keyKsnStr, keyCom);
        if (result >= 0) {
            btComfirm.setEnabled(false);
            //keyIndex.setEnabled(false);
            keyKsn.setEnabled(false);
            keyComponent.setTransformationMethod(PasswordTransformationMethod.getInstance());//隐藏密钥
            keyComponent.setEnabled(false);
            setInjectStatus();
            String kcv = getKeyComponentCheckValue(index);
            new AlertDialog.Builder(HsmKeyShareTestActivity.this).setTitle("KCV").setMessage(kcv)
                    .setNegativeButton("确定", null)
                    .setPositiveButton("取消", null)
                    .show();
        } else {
            showToast("密钥分量保存失败，请重新输入");
        }
    }

    private String getKeyComponentCheckValue(int keyIndex) {
        try {
            byte[] kcvBytes = new byte[4];
            addStartTimeWithClear("getKeyCheckValue()");
            int result = MyApplication.app.securityOptV2.getKeyCheckValue(AidlConstantsV2.Security.SEC_MKSK, keyIndex, kcvBytes);
            addEndTime("getKeyCheckValue()");
            LogUtil.e(TAG, "getKeyComponentCheckValue--->>> result = " + result);
            showSpendTime();
            if (result == 0) {
                String kcv = ByteUtil.bytes2HexStr(kcvBytes).substring(0, 6);
                return kcv;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return "";
    }

    private void setInjectStatus() {
        if (mCurrentKeyComponent == KEY_COMPONENT_TYPE_1) {
            isKeyComponent1InjectSuccess = true;
        } else if (mCurrentKeyComponent == KEY_COMPONENT_TYPE_2) {
            isKeyComponent2InjectSuccess = true;
        } else {
            isKeyComponent3InjectSuccess = true;
        }
    }

    public static int injectKeyComponent(int keyIndex, int keyType, int keyAlgType, int mKeyLen, String keyKsnStr, String keyCom) {
        int result = -1;
        try {
            result = MyApplication.app.securityOptV2.hsmSaveKeyShare(keyType, ByteUtil.hexStr2Bytes(keyCom), null, keyAlgType, keyIndex);
            LogUtil.e(TAG, "injectKeyComponent result = " + result);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String combineKeyComponent(Integer keyIndex, int keyType, int keyAlgType) {
        byte[] kcv = new byte[4];
        int keyComponent1Index = LOCAL_KEY_COMPONENT_1_INDEX;
        int keyComponent2Index = LOCAL_KEY_COMPONENT_2_INDEX;
        int keyComponent3Index = LOCAL_KEY_COMPONENT_3_INDEX;
        try {
            addStartTimeWithClear("hsmCombineKeyShare()");
            int result = MyApplication.app.securityOptV2.hsmCombineKeyShare(keyType, keyAlgType, keyIndex, keyComponent1Index, keyComponent2Index, keyComponent3Index, kcv);
            addEndTime("hsmCombineKeyShare()");
            LogUtil.e(TAG, "combineKeyShare result = " + result);
            LogUtil.e(TAG, "kcv = " + ByteUtil.bytes2HexStr(kcv));
            showSpendTime();
            if (result == 0) {
                return ByteUtil.bytes2HexStr(Arrays.copyOfRange(kcv, 0, 3));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static class AllCapTransformationMethod extends ReplacementTransformationMethod {

        private char[] lower = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
                'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        private char[] upper = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
                'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        private boolean allUpper = false;

        public AllCapTransformationMethod(boolean needUpper) {
            this.allUpper = needUpper;
        }

        @Override
        protected char[] getOriginal() {
            if (allUpper) {
                return lower;
            } else {
                return upper;
            }
        }

        @Override
        protected char[] getReplacement() {
            if (allUpper) {
                return upper;
            } else {
                return lower;
            }
        }
    }

}