package com.sm.sdk.demo.basic;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.SettingUtil;

public class EMVCallbackTimeActivity extends BaseAppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emv_callback_time);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.basic_emv_callback_timeout_time);
        findViewById(R.id.btn_max_app_select_time).setOnClickListener(this);
        findViewById(R.id.btn_max_app_final_select_time).setOnClickListener(this);
        findViewById(R.id.btn_max_confirm_card_no_time).setOnClickListener(this);
        findViewById(R.id.btn_max_input_pin_time).setOnClickListener(this);
        findViewById(R.id.btn_max_signature_time).setOnClickListener(this);
        findViewById(R.id.btn_max_cert_verify_time).setOnClickListener(this);
        findViewById(R.id.btn_max_online_time).setOnClickListener(this);
        findViewById(R.id.btn_max_data_exchange_time).setOnClickListener(this);
        findViewById(R.id.btn_max_term_risk_time).setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_max_app_select_time:
                setMaxAppSelectTime();
                break;
            case R.id.btn_max_app_final_select_time:
                setMaxAppFinalSelectTime();
                break;
            case R.id.btn_max_confirm_card_no_time:
                setMaxConfirmCardNoTime();
                break;
            case R.id.btn_max_input_pin_time:
                setMaxInputPINTime();
                break;
            case R.id.btn_max_signature_time:
                setMaxSignatureTime();
                break;
            case R.id.btn_max_cert_verify_time:
                setMaxCertVerifyTime();
                break;
            case R.id.btn_max_online_time:
                setMaxOnlineTime();
                break;
            case R.id.btn_max_data_exchange_time:
                setMaxDataExchangeTime();
                break;
            case R.id.btn_max_term_risk_time:
                setMaxTermRiskTime();
                break;
        }
    }

    /** Set EMV max app select timeout time */
    private void setMaxAppSelectTime() {
        EditText input = findViewById(R.id.edt_max_app_select_time);
        if (!checkInput(input, "maxAppSelectTime")) {
            return;
        }
        int time = Integer.parseInt(input.getText().toString());
        int code = SettingUtil.setEmvMaxAppSelectTime(time);
        showLog("maxAppSelectTime", code);
    }

    /** Set EMV max app final select timeout time */
    private void setMaxAppFinalSelectTime() {
        EditText input = findViewById(R.id.edt_max_app_final_select_time);
        if (!checkInput(input, "maxAppFinalSelectTime")) {
            return;
        }
        int time = Integer.parseInt(input.getText().toString());
        int code = SettingUtil.setEmvMaxAppFinalSelectTime(time);
        showLog("maxAppFinalSelectTime", code);
    }

    /** Set EMV max app confirm card NO. timeout time */
    private void setMaxConfirmCardNoTime() {
        EditText input = findViewById(R.id.edt_max_confirm_card_no_time);
        if (!checkInput(input, "maxConfirmCardNoTime")) {
            return;
        }
        int time = Integer.parseInt(input.getText().toString());
        int code = SettingUtil.setEmvMaxConfirmCardNoTime(time);
        showLog("maxConfirmCardNoTime", code);
    }

    /** Set EMV max input PIN timeout time */
    private void setMaxInputPINTime() {
        EditText input = findViewById(R.id.edt_max_input_pin_time);
        if (!checkInput(input, "maxInputPinTime")) {
            return;
        }
        int time = Integer.parseInt(input.getText().toString());
        int code = SettingUtil.setEmvMaxInputPinTime(time);
        showLog("maxInputPinTime", code);
    }

    /** Set EMV max signature timeout time */
    private void setMaxSignatureTime() {
        EditText input = findViewById(R.id.edt_max_signature_time);
        if (!checkInput(input, "maxSignatureTime")) {
            return;
        }
        int time = Integer.parseInt(input.getText().toString());
        int code = SettingUtil.setEmvMaxSignatureTime(time);
        showLog("maxSignatureTime", code);
    }

    /** Set EMV max cert verify timeout time */
    private void setMaxCertVerifyTime() {
        EditText input = findViewById(R.id.edt_max_cert_verify_time);
        if (!checkInput(input, "maxCertVerifyTime")) {
            return;
        }
        int time = Integer.parseInt(input.getText().toString());
        int code = SettingUtil.setEmvMaxCertVerifyTime(time);
        showLog("maxCertVerifyTime", code);
    }

    /** Set EMV max online timeout time */
    private void setMaxOnlineTime() {
        EditText input = findViewById(R.id.edt_max_online_time);
        if (!checkInput(input, "maxOnlineTime")) {
            return;
        }
        int time = Integer.parseInt(input.getText().toString());
        int code = SettingUtil.setEmvMaxOnlineTime(time);
        showLog("maxOnlineTime", code);
    }

    /** Set EMV max data exchange timeout time */
    private void setMaxDataExchangeTime() {
        EditText input = findViewById(R.id.edt_max_data_exchange_time);
        if (!checkInput(input, "maxDataExchangeTime")) {
            return;
        }
        int time = Integer.parseInt(input.getText().toString());
        int code = SettingUtil.setEmvMaxDataExchangeTime(time);
        showLog("maxDataExchangeTime", code);
    }

    /** Set EMV max data exchange timeout time */
    private void setMaxTermRiskTime() {
        EditText input = findViewById(R.id.edt_max_term_risk_time);
        if (!checkInput(input, "maxTermRiskTime")) {
            return;
        }
        int time = Integer.parseInt(input.getText().toString());
        int code = SettingUtil.setEmvMaxTermRiskTime(time);
        showLog("maxTermRiskTime", code);
    }

    private boolean checkInput(EditText edtInput, String hint) {
        String input = edtInput.getText().toString();
        if (TextUtils.isEmpty(input)) {
            showToast(hint + " should not be empty");
            edtInput.requestFocus();
            return false;
        }
        try {
            int time = Integer.parseInt(input);
            if (time < 0) {
                showToast(hint + " should >=0");
                edtInput.requestFocus();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("illegal " + hint);
            edtInput.requestFocus();
            return false;
        }
        return true;
    }

    private void showLog(String hint, int resultCode) {
        String msg = "set " + hint;
        if (resultCode == 0) {
            msg += " success";
        } else {
            msg += " failed, code:" + resultCode;
        }
        showToast(msg);
        LogUtil.e(TAG, msg);
    }
}
