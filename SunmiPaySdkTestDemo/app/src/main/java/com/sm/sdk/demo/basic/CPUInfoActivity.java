package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.Utility;

public class CPUInfoActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_cpu_info);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.basic_cpu_info);
        findViewById(R.id.btn_get_usage).setOnClickListener(v -> getCpuUsage());
        findViewById(R.id.btn_get_temp).setOnClickListener((v) -> getCpuTemperature());
    }

    private void getCpuUsage() {
        try {
            TextView textView = findViewById(R.id.txt_cpu_usage);
            String usage = MyApplication.app.basicOptV2.getCpuUsage();
            String info = Utility.formatStr("%s %s%%", getText(R.string.basic_cpu_usage), usage);
            textView.setText(info);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getCpuTemperature() {
        try {
            TextView textView = findViewById(R.id.txt_cpu_temp);
            String temp = MyApplication.app.basicOptV2.getCpuTemperature();
            String info = Utility.formatStr("%s %s℃", getText(R.string.basic_cpu_temp), temp);
            textView.setText(info);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
