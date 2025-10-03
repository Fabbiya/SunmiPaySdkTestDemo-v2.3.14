package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;

public class DeviceManageActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manage);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.basic_device_manager);
        findViewById(R.id.btn_get_card_usage_count).setOnClickListener(this);
        findViewById(R.id.btn_set_module_accessibility).setOnClickListener(this);
        findViewById(R.id.btn_get_module_accessibility).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_get_card_usage_count:
                getCardUsageCount();
                break;
            case R.id.btn_set_module_accessibility:
                setModuleAccessibility();
                break;
            case R.id.btn_get_module_accessibility:
                getModuleAccessibility();
                break;
        }
    }

    private void getCardUsageCount() {
        try {
            int cardType = CardType.MAGNETIC.getValue();
            RadioGroup group = findViewById(R.id.rdo_group_card_type);
            int checkedId = group.getCheckedRadioButtonId();
            TextView result = findViewById(R.id.tv_card_usage_result);
            if (checkedId == R.id.rdo_card_mag) {
                cardType = CardType.MAGNETIC.getValue();
            } else if (checkedId == R.id.rdo_card_icc) {
                cardType = CardType.IC.getValue();
            } else if (checkedId == R.id.rdo_card_picc) {
                cardType = CardType.NFC.getValue();
            }
            addStartTime("getCardUsageCount()");
            int successCount = MyApplication.app.basicOptV2.getCardUsageCount(cardType, true);
            int failureCount = MyApplication.app.basicOptV2.getCardUsageCount(cardType, false);
            addEndTime("getCardUsageCount()");
            String label = this.<RadioButton>findViewById(checkedId).getText().toString();
            String msg = Utility.formatStr("%s usage count:\nSuccess: %d\nFailure: %d", label, successCount, failureCount);
            result.setText(msg);
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setModuleAccessibility() {
        try {
            RadioGroup group = findViewById(R.id.rdo_group_accessibility);
            int checkedId = group.getCheckedRadioButtonId();
            int module = -1;
            if (checkedId == R.id.rdo_module_mag) {
                module = 1;
            } else if (checkedId == R.id.rdo_module_icc) {
                module = 2;
            } else if (checkedId == R.id.rdo_module_picc) {
                module = 3;
            } else if (checkedId == R.id.rdo_module_ped) {
                module = 4;
            }
            RadioButton rdoEnable = findViewById(R.id.rdo_enable);
            int accessibility = rdoEnable.isChecked() ? 1 : 0;
            addStartTime("setModuleAccessibility()");
            int code = MyApplication.app.basicOptV2.setModuleAccessibility(module, accessibility);
            addEndTime("setModuleAccessibility()");
            showToast(code == 0 ? "Success" : "Failed");
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getModuleAccessibility() {
        try {
            TextView result = findViewById(R.id.tv_module_accessibility_result);
            int magValue = MyApplication.app.basicOptV2.getModuleAccessibility(1);
            int iccValue = MyApplication.app.basicOptV2.getModuleAccessibility(2);
            int piccValue = MyApplication.app.basicOptV2.getModuleAccessibility(3);
            int pedValue = MyApplication.app.basicOptV2.getModuleAccessibility(4);
            StringBuilder sb = new StringBuilder("Module accessibility:");
            sb.append("\nMAG module: ").append(getAccessibilityString(magValue))
                    .append("\nICC module: ").append(getAccessibilityString(iccValue))
                    .append("\nPICC module: ").append(getAccessibilityString(piccValue))
                    .append("\nPinPad module: ").append(getAccessibilityString(pedValue));
            result.setText(sb);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private String getAccessibilityString(int value) {
        return value == 1 ? "Enabled" : "Disabled";
    }
}
