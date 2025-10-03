package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;

public class HsmAndDeviceCertificateTestActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_cert_test);
        initToolbarBringBack(R.string.security_hsm_device_cert_test);
        initView();
    }

    private void initView() {
        View view = findViewById(R.id.device_cert_pvt_key_test);
        TextView leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.hsm_device_cert_pvt_key_test);

        view = findViewById(R.id.inject_device_cert_pvt_key);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.hsm_inject_device_pvt_key);

        view = findViewById(R.id.get_set_device_cert);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_get_set_device_cert);

        view = findViewById(R.id.hsm_get_device_cert);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.hsm_get_device_cert);

        view = findViewById(R.id.hsm_device_pvk_recover);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.hsm_device_pvk_recover);

        view = findViewById(R.id.hsm_device_cert_manager);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.hsm_device_cert_manager);

        view = findViewById(R.id.hsm_rsa_keypair);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.hsm_rsa_keypair_test);

        view = findViewById(R.id.hsm_rsa_recover);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.hsm_rsa_recover);

        view = findViewById(R.id.hsm_key_share);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.hsm_key_share_test);

        view = findViewById(R.id.hsm_save_key_under_kek);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.hsm_save_key_under_kek);

        view = findViewById(R.id.hsm_export_key_under_kek);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.hsm_export_key_under_kek);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.device_cert_pvt_key_test:
                openActivity(DeviceCertPvkTestActivity.class);
                break;
            case R.id.inject_device_cert_pvt_key:
                openActivity(InjectDeviceCertPvkActivity.class);
                break;
            case R.id.get_set_device_cert:
                openActivity(GetSetDeviceCertificateActivity.class);
                break;
            case R.id.hsm_get_device_cert:
                openActivity(GetDeviceCertificateActivity.class);
                break;
            case R.id.hsm_device_pvk_recover:
                openActivity(DevicePvkRecoverActivity.class);
                break;
            case R.id.hsm_device_cert_manager:
                openActivity(DeviceCertManagerActivity.class);
                break;
            case R.id.hsm_rsa_keypair:
                openActivity(HsmRsaTestActivity.class);
                break;
            case R.id.hsm_rsa_recover:
                openActivity(RSARecoverActivity.class);
                break;
            case R.id.hsm_key_share:
                openActivity(HsmKeyShareTestActivity.class);
                break;
            case R.id.hsm_save_key_under_kek:
                openActivity(HsmSaveKeyUnderKEKActivity.class);
                break;
            case R.id.hsm_export_key_under_kek:
                openActivity(HsmExportKeyUnderKEKActivity.class);
                break;
        }
    }

}