package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;

public class NetworkManageActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_manage);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.basic_network_manage);
        findViewById(R.id.btn_set_preferred_network).setOnClickListener((v) -> onSetPreferredNetworkModeClick());
        findViewById(R.id.btn_get_support_network_type).setOnClickListener((v) -> onGetSupportedNetworkTypeClick());
        findViewById(R.id.btn_set_airplane_mode).setOnClickListener((v) -> onSetAirplaneModeClick());
        findViewById(R.id.btn_set_data_roaming).setOnClickListener((v) -> onSetDataRoamingEnableClick());
    }

    /**
     * <pre>
     * Set preferred network type
     * Network type can be following values:
     * 0-GSM/WCDMA (WCDMA preferred)
     * 1-GSM only
     * 2-WCDMA only
     * 3-GSM/WCDMA (auto mode, according to PRL) AVAILABLE Application Settings menu
     * 4-CDMA and EvDo (auto mode, according to PRL) AVAILABLE Application Settings menu
     * 5-CDMA only
     * 6-EvDo only
     * 7-GSM/WCDMA, CDMA, and EvDo (auto mode, according to PRL) AVAILABLE Application Settings menu
     * 8-LTE, CDMA and EvDo
     * 9-LTE, GSM/WCDMA
     * 10-LTE, CDMA, EvDo, GSM/WCDMA
     * 11-LTE Only mode.
     * 12-LTE/WCDMA
     * 13-TD-SCDMA only
     * 14-TD-SCDMA and WCDMA
     * 15-TD-SCDMA and LTE
     * 16-TD-SCDMA and GSM
     * 17-TD-SCDMA,GSM and LTE
     * 18-TD-SCDMA, GSM/WCDMA
     * 19-TD-SCDMA, WCDMA and LTE
     * 20-TD-SCDMA, GSM/WCDMA and LTE
     * 21-TD-SCDMA,EvDo,CDMA,GSM/WCDMA
     * 22-TD-SCDMA/LTE/GSM/WCDMA, CDMA, and EvDo
     * 30-LTE/GSM
     * 31-LTE TDD Only mode.
     * 32-CDMA,GSM(2G Global)
     * 33-CDMA,EVDO,GSM
     * 34-LTE,CDMA,EVDO,GSM(4G Global, 4M)
     * </pre>
     */
    private void onSetPreferredNetworkModeClick() {
        try {
            EditText edtMode = findViewById(R.id.edt_preferred_net_mode);
            EditText edtSimSlot = findViewById(R.id.edt_preferred_sim_slot);
            if (TextUtils.isEmpty(edtMode.getText())) {
                showToast("network mode should not be empty");
                edtMode.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(edtSimSlot.getText())) {
                showToast("SIM card slot index should not be empty");
                edtMode.requestFocus();
                return;
            }
            int mode = Integer.parseInt(edtMode.getText().toString());
            int slotIndex = Integer.parseInt(edtSimSlot.getText().toString());
            int code = MyApplication.app.basicOptV2.setPreferredNetworkMode(mode, slotIndex);
            String msg = null;
            if (code == 0) {//success
                msg = Utility.formatStr("set SIM %d preferred network mode success", slotIndex);
            } else {//failed
                msg = Utility.formatStr("set SIM %d preferred network mode failed", slotIndex);
            }
            showToast(msg);
            LogUtil.e(TAG, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Get SIM card supported network type */
    private void onGetSupportedNetworkTypeClick() {
        try {
            TextView txtResult = findViewById(R.id.txt_get_supported_network_result);
            EditText edtSimSlot = findViewById(R.id.edt_support_net_type_sim_slot);
            if (TextUtils.isEmpty(edtSimSlot.getText())) {
                showToast("SIM card slot index should not be empty");
                edtSimSlot.requestFocus();
                return;
            }
            int slotIndex = Integer.parseInt(edtSimSlot.getText().toString());
            String networkType = MyApplication.app.basicOptV2.getSupportedNetworkType(slotIndex);
            String msg = null;
            if (TextUtils.isEmpty(networkType)) {
                msg = Utility.formatStr("get SIM %d support network type failed", slotIndex);
            } else {
                msg = Utility.formatStr("SIM %d support network type: %s", slotIndex, networkType);
            }
            txtResult.setText(msg);
            showToast(msg);
            LogUtil.e(TAG, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Enable/Disable airplane mode */
    private void onSetAirplaneModeClick() {
        try {
            RadioButton rdoEnable = findViewById(R.id.rdo_enable_airplane_mode);
            int code = MyApplication.app.basicOptV2.setAirplaneMode(rdoEnable.isChecked());
            String msg = null;
            if (code == 0) {//success
                if (rdoEnable.isChecked()) {
                    msg = "enable airplane mode success";
                } else {
                    msg = "disable airplane mode success";
                }
            } else {//failed
                if (rdoEnable.isChecked()) {
                    msg = "enable airplane mode failed";
                } else {
                    msg = "disable airplane mode failed";
                }
            }
            showToast(msg);
            LogUtil.e(TAG, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Enable/Disable SIM card data roaming */
    private void onSetDataRoamingEnableClick() {
        try {
            EditText edtSimSlot = findViewById(R.id.edt_data_roaming_sim_slot);
            if (TextUtils.isEmpty(edtSimSlot.getText())) {
                showToast("SIM card slot index should not be empty");
                edtSimSlot.requestFocus();
                return;
            }
            RadioButton rdoEnable = findViewById(R.id.rdo_enable_data_roaming);
            int slotIndex = Integer.parseInt(edtSimSlot.getText().toString());
            int code = MyApplication.app.basicOptV2.setDataRoamingEnable(slotIndex, rdoEnable.isChecked());
            LogUtil.e(TAG, "setDataRoamingEnable code:" + code);
            String msg = null;
            if (code == 0) {//success
                if (rdoEnable.isChecked()) {
                    msg = Utility.formatStr("enable SIM %d data roaming success", slotIndex);
                } else {
                    msg = Utility.formatStr("disable SIM %d data roaming success", slotIndex);
                }
            } else {//failed
                if (rdoEnable.isChecked()) {
                    msg = Utility.formatStr("enable SIM %d data roaming failed", slotIndex);
                } else {
                    msg = Utility.formatStr("disable SIM %d data roaming failed", slotIndex);
                }
            }
            showToast(msg);
            LogUtil.e(TAG, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
