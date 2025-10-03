package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;

import java.util.Arrays;

public class APACSMacTestActivity extends BaseAppCompatActivity {
    private TextView tvResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_apacs_mac);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_apacs_mac);
        findViewById(R.id.mb_ok).setOnClickListener(this);
        tvResult = findViewById(R.id.tv_result);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_ok:
                testApacsMac();
                break;
        }
    }


    private void testApacsMac() {
        try {
            tvResult.setText(null);
            EditText edtInitMackey = findViewById(R.id.init_mac_key);
            String macKeyStr = edtInitMackey.getText().toString();
            if (TextUtils.isEmpty(macKeyStr) || macKeyStr.length() % 8 != 0) {
                showToast(R.string.security_key_value_hint);
                edtInitMackey.requestFocus();
                return;
            }
            int initMacKeyIndex = getKeyIndex(R.id.init_mac_key_index);
            int macKeyIndex = getKeyIndex(R.id.mac_key_index);
            int pinKeyIndex = getKeyIndex(R.id.pin_key_index);
            if (initMacKeyIndex < 0 || macKeyIndex < 0 || pinKeyIndex < 0) {
                return;
            }
            EditText edtABBlock = findViewById(R.id.ab_block);
            String abBlockStr = edtABBlock.getText().toString();
            if (TextUtils.isEmpty(abBlockStr)) {
                showToast("ab block should not be empty");
                edtABBlock.requestFocus();
                return;
            }
            EditText edtAuthParam = findViewById(R.id.auth_param);
            String authParamStr = edtAuthParam.getText().toString();
            if (TextUtils.isEmpty(abBlockStr)) {
                showToast("Auth param block should not be empty");
                return;
            }
            EditText edtReqData = findViewById(R.id.request_data);
            String requestDataStr = edtReqData.getText().toString();
            if (TextUtils.isEmpty(abBlockStr)) {
                showToast("Request data should not be empty");
                return;
            }
            EditText edtRespData = findViewById(R.id.response_data);
            String responseDataStr = edtRespData.getText().toString();
            if (TextUtils.isEmpty(abBlockStr)) {
                showToast("Response data should not be empty");
                return;
            }

            String PAN = "999998200000113004";
            byte[] macKey = ByteUtil.hexStr2Bytes(macKeyStr);
            byte[] abBlock = ByteUtil.hexStr2Bytes(abBlockStr);
            byte[] authParam = ByteUtil.hexStr2Bytes(authParamStr);
            byte[] requestData = ByteUtil.hexStr2Bytes(requestDataStr);
            byte[] responseData = ByteUtil.hexStr2Bytes(responseDataStr);
            String msg = null;

            //1.save initialize MAC key
            int code = MyApplication.app.securityOptV2.savePlaintextKey(Security.KEY_TYPE_MAK, macKey, null, Security.KEY_ALG_TYPE_3DES, initMacKeyIndex);
            if (code < 0) {
                msg = "Save mak failed, code: " + code;
                concatText(msg);
                LogUtil.e(TAG, msg);
                return;
            }

            //2.OWF(init_MAK, ABBlock)= session key(6B7581B2E59E9551)
            byte[] dataOut = new byte[256];
            int len = MyApplication.app.securityOptV2.apacsMac(initMacKeyIndex, macKeyIndex, pinKeyIndex, Security.KEY_CTRL_PANPARA, abBlock, dataOut);
            if (len < 0) {
                msg = "Generate session key failed, code: " + len;
                concatText(msg);
                LogUtil.e(TAG, msg);
                return;
            }
            byte[] sessionKey = Arrays.copyOf(dataOut, 8);
            msg = "Session key: " + ByteUtil.bytes2HexStr(sessionKey);
            concatText(msg);
            LogUtil.e(TAG, msg);

            //3.calculate Request Mac(E43B0C1D8D954A1E)
            len = MyApplication.app.securityOptV2.calcMac(macKeyIndex, Security.MAC_ALG_X9_19_DEA, requestData, dataOut);
            if (len < 0) {
                msg = "Calculate request mac failed, code: " + len;
                concatText(msg);
                LogUtil.e(TAG, msg);
                return;
            }
            byte[] requestMac = Arrays.copyOf(dataOut, 8);
            msg = "Request mac: " + ByteUtil.bytes2HexStr(requestMac);
            concatText(msg);
            LogUtil.e(TAG, msg);
            byte[] reqMacResidue = Arrays.copyOfRange(requestMac, 4, requestMac.length);
            msg = "Request mac residue: " + ByteUtil.bytes2HexStr(reqMacResidue);
            concatText(msg);
            LogUtil.e(TAG, msg);

            //4.OWF(sessionKey, authParam)=Auth Param(D5820B4F8CD08332)
            len = MyApplication.app.securityOptV2.apacsMac(initMacKeyIndex, macKeyIndex, pinKeyIndex, Security.KEY_CTRL_AUTHPARA, authParam, dataOut);
            if (len < 0) {
                msg = "OWF generate new auth param failed, code: " + len;
                concatText(msg);
                LogUtil.e(TAG, msg);
                return;
            }
            byte[] owfAuthParam = Arrays.copyOf(dataOut, len);
            msg = "OWF auth param: " + ByteUtil.bytes2HexStr(owfAuthParam);
            concatText(msg);
            LogUtil.e(TAG, msg);

            //5.calculate Response Mac
            byte[] tmpData = ByteUtil.concatByteArrays(reqMacResidue, responseData);
            if (tmpData.length % 8 != 0) {
                tmpData = Arrays.copyOf(tmpData, (tmpData.length / 8 + 1) * 8);
            }
            tmpData = ByteUtil.concatByteArrays(tmpData, owfAuthParam);
            len = MyApplication.app.securityOptV2.calcMac(macKeyIndex, Security.MAC_ALG_X9_19_DEA, tmpData, dataOut);
            if (len < 0) {
                msg = "Calculate response mac failed, code: " + len;
                concatText(msg);
                LogUtil.e(TAG, msg);
                return;
            }
            byte[] responseMac = Arrays.copyOf(dataOut, 8);
            msg = "Response mac: " + ByteUtil.bytes2HexStr(responseMac);
            concatText(msg);
            LogUtil.e(TAG, msg);
            byte[] resMacResidue = Arrays.copyOfRange(responseMac, 4, responseMac.length);
            msg = "Response mac residue: " + ByteUtil.bytes2HexStr(resMacResidue);
            concatText(msg);
            LogUtil.e(TAG, msg);

            //6.Generate next MAC key(0E49541FC8E766B1)
            byte[] dataIn = ByteUtil.concatByteArrays(reqMacResidue, resMacResidue);
            len = MyApplication.app.securityOptV2.apacsMac(initMacKeyIndex, macKeyIndex, pinKeyIndex, Security.KEY_CTRL_APACSMAC, dataIn, dataOut);
            if (len < 0) {
                msg = "Generate next mac key code: " + len;
                concatText(msg);
                LogUtil.e(TAG, msg);
                return;
            }
            byte[] nextMacKey = Arrays.copyOf(dataOut, len);
            msg = "Next mac key: " + ByteUtil.bytes2HexStr(nextMacKey);
            concatText(msg);
            LogUtil.e(TAG, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void concatText(CharSequence msg) {
        runOnUiThread(() -> {
            String preMsg = tvResult.getText().toString();
            tvResult.setText(TextUtils.concat(preMsg, "\n", msg));
        });
    }

    private int getKeyIndex(int resId) {
        try {
            EditText edt = findViewById(resId);
            String keyIndexStr = edt.getText().toString();
            int keyIndex = Integer.parseInt(keyIndexStr);
            if (keyIndex < 0 || keyIndex > 199) {
                showToast(R.string.security_mksk_key_index_hint);
                edt.requestFocus();
                return -1;
            }
            return keyIndex;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
