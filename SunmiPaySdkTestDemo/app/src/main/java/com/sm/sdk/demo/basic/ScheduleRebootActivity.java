package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;

public class ScheduleRebootActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_schedule_reboot);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.basic_schedule_reboot);
        findViewById(R.id.btn_set_schedule).setOnClickListener(v -> setScheduleReboot());
        findViewById(R.id.btn_clear_schedule).setOnClickListener((v) -> clearScheduleReboot());
    }

    private void setScheduleReboot() {
        try {
            EditText editText = findViewById(R.id.edt_hour);
            String hour = editText.getText().toString();
            if (TextUtils.isEmpty(hour)) {
                showToast("hour should not be empty!");
                editText.requestFocus();
                return;
            }
            editText = findViewById(R.id.edt_minute);
            String minute = editText.getText().toString();
            if (TextUtils.isEmpty(minute)) {
                showToast("minute should not be empty!");
                editText.requestFocus();
                return;
            }
            editText = findViewById(R.id.edt_second);
            String second = editText.getText().toString();
            if (TextUtils.isEmpty(second)) {
                showToast("second should not be empty!");
                editText.requestFocus();
                return;
            }
            editText = findViewById(R.id.edt_millisecond);
            String millisecond = editText.getText().toString();
            if (TextUtils.isEmpty(millisecond)) {
                showToast("millisecond should not be empty!");
                editText.requestFocus();
                return;
            }
            int h = Integer.parseInt(hour);
            int m = Integer.parseInt(minute);
            int s = Integer.parseInt(second);
            int ms = Integer.parseInt(millisecond);
            int code = MyApplication.app.basicOptV2.setScheduleReboot(h, m, s, ms);
            showToast(code == 0 ? "success" : "failed,code:" + code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearScheduleReboot() {
        try {
            int code = MyApplication.app.basicOptV2.clearScheduleReboot();
            showToast(code == 0 ? "success" : "failed,code:" + code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
