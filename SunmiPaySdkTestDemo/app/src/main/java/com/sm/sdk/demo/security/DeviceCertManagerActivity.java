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
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;

import java.util.Arrays;

public class DeviceCertManagerActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hsmdev_cert_manager);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.hsm_device_cert_manager);
        findViewById(R.id.btn_hsm_gen_dev_cert).setOnClickListener(this);
        findViewById(R.id.btn_hsm_save_dev_cert).setOnClickListener(this);
        findViewById(R.id.btn_hsm_get_dev_cert_state).setOnClickListener(this);
        findViewById(R.id.btn_hsm_delete_dev_cert).setOnClickListener(this);
        EditText edtCertData = findViewById(R.id.edt_hsm_save_dev_cert_data);
        edtCertData.setText(ByteUtil.bytes2HexStr(new byte[1024]));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_hsm_gen_dev_cert:
                generateDeviceCert();
                break;
            case R.id.btn_hsm_save_dev_cert:
                saveDeviceCert();
                break;
            case R.id.btn_hsm_get_dev_cert_state:
                getDeviceCertState();
                break;
            case R.id.btn_hsm_delete_dev_cert:
                deleteDeviceCert();
                break;
        }
    }

    /** Generate a device certificate */
    private void generateDeviceCert() {
        try {
            EditText edtCertIndex = findViewById(R.id.edt_hsm_gen_dev_cert_index);
            String certIndexStr = edtCertIndex.getText().toString();
            if (TextUtils.isEmpty(certIndexStr)) {
                showToast("cert index should not be empty");
                edtCertIndex.requestFocus();
                return;
            }
            int certIndex = Integer.parseInt(certIndexStr);
            if (certIndex < 9001 || certIndex > 9008) {
                showToast("cert index should be in [9001-9008]");
                return;
            }
            String msg = null;
            byte[] buffer = new byte[2048];
            int len = MyApplication.app.devCertManagerV2.genDevKey(certIndex, Security.CERT_GENERATE_RSA2048_E65537_PVK_PUK, buffer);
            if (len < 0) {
                msg = "generate device cert failed, code:" + len;
                showToast(msg);
                LogUtil.e(TAG, msg);
                return;
            }
            String module = ByteUtil.bytes2HexStr(Arrays.copyOf(buffer, len));
            msg = "generate device cert success, pubKey module:" + module;
            LogUtil.e(TAG, msg);
            TextView result = findViewById(R.id.tv_hsm_gen_dev_cert_module);
            result.setText("Public key module:\n" + module);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Save a device certificate */
    private void saveDeviceCert() {
        try {
            EditText edtCertIndex = findViewById(R.id.edt_hsm_save_dev_cert_index);
            String certIndexStr = edtCertIndex.getText().toString();
            if (TextUtils.isEmpty(certIndexStr)) {
                showToast("cert index should not be empty");
                edtCertIndex.requestFocus();
                return;
            }
            int certIndex = Integer.parseInt(certIndexStr);
            if (certIndex < 9001 || certIndex > 9008) {
                showToast("cert index should be in [9001-9008]");
                return;
            }
            EditText edtCertData = findViewById(R.id.edt_hsm_save_dev_cert_data);
            String certDataStr = edtCertData.getText().toString();
            if (TextUtils.isEmpty(certDataStr)) {
                showToast("cert data should not be empty");
                edtCertData.requestFocus();
                return;
            }
            if (!Utility.checkHexValue(certDataStr)) {
                showToast("cert data should be Hex value");
                return;
            }
            byte[] certData = ByteUtil.hexStr2Bytes(certDataStr);
            String msg = null;
            int code = MyApplication.app.devCertManagerV2.saveDevCert(certIndex, certData);
            if (code < 0) {
                msg = "save device cert failed, code:" + code;
                showToast(msg);
                LogUtil.e(TAG, msg);
                return;
            }
            msg = "save device cert success";
            showToast(msg);
            LogUtil.e(TAG, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Get device certificate state */
    private void getDeviceCertState() {
        try {
            EditText edtCertIndex = findViewById(R.id.edt_hsm_get_dev_cert_state_index);
            String certIndexStr = edtCertIndex.getText().toString();
            if (TextUtils.isEmpty(certIndexStr)) {
                showToast("cert index should not be empty");
                edtCertIndex.requestFocus();
                return;
            }
            int certIndex = Integer.parseInt(certIndexStr);
            if (certIndex < 9001 || certIndex > 9008) {
                showToast("cert index should be in [9001-9008]");
                return;
            }
            String msg = null;
            int state = MyApplication.app.devCertManagerV2.getDevKeyState(certIndex);
            if (state < 0) {
                msg = "get device cert state failed, code:" + state;
                showToast(msg);
                LogUtil.e(TAG, msg);
                return;
            }
            msg = "get device cert state success";
            LogUtil.e(TAG, msg);
            TextView result = findViewById(R.id.tv_hsm_dev_cert_state);
            result.setText("Device cert state:" + state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Delete device certificate */
    private void deleteDeviceCert() {
        try {
            EditText edtCertIndex = findViewById(R.id.edt_hsm_delete_dev_cert_index);
            String certIndexStr = edtCertIndex.getText().toString();
            if (TextUtils.isEmpty(certIndexStr)) {
                showToast("cert index should not be empty");
                edtCertIndex.requestFocus();
                return;
            }
            int certIndex = Integer.parseInt(certIndexStr);
            if (certIndex < 9001 || certIndex > 9008) {
                showToast("cert index should be in [9001-9008]");
                return;
            }
            String msg = null;
            int code = MyApplication.app.devCertManagerV2.deleteKey(certIndex);
            if (code < 0) {
                msg = "delete device cert failed, code:" + code;
                showToast(msg);
                LogUtil.e(TAG, msg);
                return;
            }
            msg = "delete device cert success";
            showToast(msg);
            LogUtil.e(TAG, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
