package com.sm.sdk.demo.basic;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;

public class BasicActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);
        initToolbarBringBack(R.string.basic);
        initView();
    }

    private void initView() {
        View item = findViewById(R.id.get_sys_param);
        TextView leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_get_sys_param);
        item.setOnClickListener(this);

        item = findViewById(R.id.set_sys_param);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_set_sys_param);
        item.setOnClickListener(this);

        item = findViewById(R.id.buzzer);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_buzzer);
        item.setOnClickListener(this);

        item = findViewById(R.id.led);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_led);
        item.setOnClickListener(this);

        item = findViewById(R.id.screen_mode);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_screen_mode);
        item.setOnClickListener(this);

        item = findViewById(R.id.dynamic_perm_wifi_proxy);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_dynamic_perm_wifi_proxy);
        item.setOnClickListener(this);

        item = findViewById(R.id.ca_cert_manager);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_ca_cert_manager);
        item.setOnClickListener(this);

        item = findViewById(R.id.cpu_info);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_cpu_info);
        item.setOnClickListener(this);

        item = findViewById(R.id.schedule_reboot);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_schedule_reboot);
        item.setOnClickListener(this);

        item = findViewById(R.id.customize_function_key);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_customize_function_key);
        item.setOnClickListener(this);

        item = findViewById(R.id.lmk_package);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_lmk_package);
        item.setOnClickListener(this);

        item = findViewById(R.id.emv_callback_time);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_emv_callback_timeout_time);
        item.setOnClickListener(this);

        item = findViewById(R.id.pin_anti_exhaustive_mode);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.pin_anti_exhaustive_mode);
        item.setOnClickListener(this);

        item = findViewById(R.id.sys_set_wakeup);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_sys_set_wakeup);
        item.setOnClickListener(this);

        item = findViewById(R.id.kb_beep_mode);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_kb_beep_mode);
        item.setOnClickListener(this);

        item = findViewById(R.id.shard_lib);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_shared_lib_test);
        item.setOnClickListener(this);

        item = findViewById(R.id.data_transmission);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.test_transmission_stress_test);
        item.setOnClickListener(this);

        item = findViewById(R.id.liteso);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_liteso_test);
        item.setOnClickListener(this);

        item = findViewById(R.id.network_manage);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_network_manage);
        item.setOnClickListener(this);

        item = findViewById(R.id.phone_call_manage);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_phone_manage);
        item.setOnClickListener(this);

        item = findViewById(R.id.device_manage);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_device_manager);
        item.setOnClickListener(this);

        item = findViewById(R.id.ped_test);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_ped_test);
        item.setOnClickListener(this);

        item = findViewById(R.id.rtc_info);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_rtc_battery_info);
        item.setOnClickListener(this);
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.get_sys_param:
                openActivity(GetSysParamActivity.class);
                break;
            case R.id.set_sys_param:
                openActivity(SetSysParamActivity.class);
                break;
            case R.id.buzzer:
                openActivity(BuzzerActivity.class);
                break;
            case R.id.led:
                openActivity(LedActivity.class);
                break;
            case R.id.screen_mode:
                openActivity(ScreenModelActivity.class);
                break;
            case R.id.dynamic_perm_wifi_proxy:
                openActivity(DynamicPermissionAndWifiProxyActivity.class);
                break;
            case R.id.ca_cert_manager:
                openActivity(CACertManageActivity.class);
                break;
            case R.id.cpu_info:
                openActivity(CPUInfoActivity.class);
                break;
            case R.id.schedule_reboot:
                openActivity(ScheduleRebootActivity.class);
                break;
            case R.id.customize_function_key:
                openActivity(CustomizeFunctionKeyActivity.class);
                break;
            case R.id.lmk_package:
                openActivity(LowMemoryKillerActivity.class);
                break;
            case R.id.emv_callback_time:
                openActivity(EMVCallbackTimeActivity.class);
                break;
            case R.id.pin_anti_exhaustive_mode:
                openActivity(PinAntiExhaustiveMode.class);
                break;
            case R.id.sys_set_wakeup:
                openActivity(SysSetWakeupActivity.class);
                break;
            case R.id.kb_beep_mode:
                openActivity(KBBeepModeActivity.class);
                break;
            case R.id.shard_lib:
                openActivity(SharedLibActivity.class);
                break;
            case R.id.data_transmission:
                openActivity(TransmissionTestActivity.class);
                break;
            case R.id.liteso:
                openActivity(LitesoActivity.class);
                break;
            case R.id.network_manage:
                openActivity(NetworkManageActivity.class);
                break;
            case R.id.phone_call_manage:
                openActivity(PhoneManageActivity.class);
                break;
            case R.id.device_manage:
                openActivity(DeviceManageActivity.class);
                break;
            case R.id.ped_test:
                openActivity(PedActivity.class);
                break;
            case R.id.rtc_info:
                openActivity(RTCBatterVolActivity.class);
                break;
        }
    }


}
