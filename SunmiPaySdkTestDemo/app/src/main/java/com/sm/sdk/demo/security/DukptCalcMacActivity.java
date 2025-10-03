package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.Arrays;

public class DukptCalcMacActivity extends BaseAppCompatActivity {
    private EditText mEditData;
    private EditText mEditKeyIndex;
    private EditText mEditKeyLen;
    private EditText mMacData;
    private TextView mTvInfo;
    private RadioGroup mMacOptGroup;
    private int mCalcType = Security.MAC_ALG_X9_19;
    private int mKeySelect = Security.DUKPT_KEY_SELECT_KEY_MAC_BOTH;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_dukpt_calc_mac);
        initToolbarBringBack(R.string.security_DuKpt_calc_mac);
        initView();
    }

    private void initView() {
        final LinearLayout macDataLay = findViewById(R.id.mac_data_lay);
        mMacOptGroup = findViewById(R.id.mac_opt_group);
        mMacOptGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_mac_opt_calc:
                    macDataLay.setVisibility(View.GONE);
                    break;
                case R.id.rb_mac_opt_verify:
                    macDataLay.setVisibility(View.VISIBLE);
                    break;
            }
        });
        mMacOptGroup.check(R.id.rb_mac_opt_calc);
        RadioGroup keySelectGroup = findViewById(R.id.key_select_group);
        keySelectGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_key_select_data_both:
                    mKeySelect = Security.DUKPT_KEY_SELECT_KEY_MAC_BOTH;
                    break;
                case R.id.rb_key_select_data_rsp:
                    mKeySelect = Security.DUKPT_KEY_SELECT_KEY_MAC_RSP;
                    break;
            }
        });

        RadioGroup macTypeGroup = findViewById(R.id.mac_type);
        macTypeGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_mac_type1:
                            mCalcType = Security.MAC_ALG_X9_19;
                            break;
                        case R.id.rb_mac_type2:
                            mCalcType = Security.MAC_ALG_FAST_MODE_INTERNATIONAL;
                            break;
                        case R.id.rb_mac_type3:
                            mCalcType = Security.MAC_ALG_CBC_INTERNATIONAL;
                            break;
                        case R.id.rb_mac_type4:
                            mCalcType = Security.MAC_ALG_HMAC_SHA1;
                            break;
                        case R.id.rb_mac_type5:
                            mCalcType = Security.MAC_ALG_HMAC_SHA256;
                            break;
                        case R.id.rb_mac_type6:
                            mCalcType = Security.MAC_ALG_CMAC;
                            break;
                    }
                }
        );

        mEditData = findViewById(R.id.source_data);
        mEditKeyIndex = findViewById(R.id.key_index);
        mEditKeyLen = findViewById(R.id.mac_key_len);
        mMacData = findViewById(R.id.mac_data);
        mTvInfo = findViewById(R.id.tv_info);
        mEditData.setText("123456789012345678901234");
        findViewById(R.id.mb_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ok:
                onOKButtonClick();
                break;
        }
    }

    private void onOKButtonClick() {
        if (mMacOptGroup.getCheckedRadioButtonId() == R.id.rb_mac_opt_calc) {
            calcMac();
        } else if (mMacOptGroup.getCheckedRadioButtonId() == R.id.rb_mac_opt_verify) {
            verifyMac();
        }
    }

    /** Calculate Mac */
    private void calcMac() {
        try {
            String keyIndexStr = mEditKeyIndex.getText().toString();
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast(R.string.security_duKpt_key_index_hint);
                mEditKeyIndex.requestFocus();
                return;
            }
            int keyIndex = Integer.parseInt(keyIndexStr);
            if (!KeyIndexUtil.checkDukptKeyIndex(keyIndex)) {
                if (DeviceUtil.isBrazilCKD()) {
                    showToast(R.string.security_duKpt_key_index_hint);
                } else {
                    showToast(R.string.security_dukpt_key_index_hint);
                }
                mEditKeyIndex.requestFocus();
            }
            String keyLenStr = mEditKeyLen.getText().toString();
            if (TextUtils.isEmpty(keyLenStr)) {
                showToast("key length shouldn't be empty");
                mEditKeyLen.requestFocus();
                return;
            }
            int keyLength = Integer.parseInt(keyLenStr);
            String dataStr = mEditData.getText().toString();
            if (TextUtils.isEmpty(dataStr)) {
                showToast(R.string.security_source_data_hint);
                mEditData.requestFocus();
                return;
            }
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataStr);
            if (dataIn.length > 1016) {
                showToast("Dukpt-extension key calculate MAC input data length should <=1016");
                mEditData.requestFocus();
                return;
            }
            byte[] dataOut = new byte[16];
            Bundle bundle = new Bundle();
            bundle.putInt("keySelect", mKeySelect);
            bundle.putInt("keyIndex", keyIndex);
            bundle.putInt("keyLength", keyLength);
            bundle.putInt("macType", mCalcType);
            bundle.putByteArray("dataIn", dataIn);
            addStartTimeWithClear("calcMacDukptExtended()");
            int code = MyApplication.app.securityOptV2.calcMacDukptExtended(bundle, dataOut);
            addEndTime("calcMacDukptExtended()");
            String macHexStr = null;
            if (code == 0) {
                if (KeyIndexUtil.checkDukpt3DesKeyIndex(keyIndex)) {//dukpt-3DES, 8 bytes Mac data
                    macHexStr = ByteUtil.bytes2HexStr(Arrays.copyOf(dataOut, 8));
                } else if (KeyIndexUtil.checkDukptAesKeyIndex(keyIndex)) {//Dukpt-AES, 16 bytes Mac data
                    macHexStr = ByteUtil.bytes2HexStr(dataOut);
                }
                mTvInfo.setText(macHexStr);
            } else {
                toastHint(code);
            }
            LogUtil.e(TAG, "calculate MAC dukpt,dataOut:" + macHexStr);
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Verify Mac */
    private void verifyMac() {
        try {
            String sourceDataStr = mEditData.getText().toString();
            String macDataStr = mMacData.getText().toString();
            String keyIndexStr = mEditKeyIndex.getText().toString();
            if (TextUtils.isEmpty(sourceDataStr) | TextUtils.isEmpty(macDataStr)) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast(R.string.security_duKpt_key_index_hint);
                return;
            }
            int keyIndex = Integer.parseInt(keyIndexStr);
            if (!KeyIndexUtil.checkDukptKeyIndex(keyIndex)) {
                if (DeviceUtil.isBrazilCKD()) {
                    showToast(R.string.security_duKpt_key_index_hint);
                } else {
                    showToast(R.string.security_dukpt_key_index_hint);
                }
                mEditKeyIndex.requestFocus();
            }
            byte[] sourceData = ByteUtil.hexStr2Bytes(sourceDataStr);
            byte[] macData = ByteUtil.hexStr2Bytes(macDataStr);
            addStartTimeWithClear("verifyMacDukptEx()");
            int code = MyApplication.app.securityOptV2.verifyMacDukptEx(mKeySelect, keyIndex, mCalcType, sourceData, macData);
            addEndTime("verifyMacDukptEx()");
            mTvInfo.setText(code == 0 ? getString(R.string.success) : getString(R.string.fail));
            LogUtil.e(TAG, "verifyMac code:" + code);
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
