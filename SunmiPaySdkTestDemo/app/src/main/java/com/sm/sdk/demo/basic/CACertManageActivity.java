package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.IOUtil;
import com.sm.sdk.demo.utils.ThreadPoolUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CACertManageActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_ca_certificate_manage);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.basic_ca_cert_manager);
        findViewById(R.id.btn_install).setOnClickListener(v -> installCACertificate());
        findViewById(R.id.btn_uninstall).setOnClickListener((v) -> uninstallCACertificate());
        ThreadPoolUtil.executeInCachePool(() -> {
            InputStream is = null;
            ByteArrayOutputStream os = null;
            try {
                os = new ByteArrayOutputStream();
                is = getResources().openRawResource(R.raw.ca);
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                byte[] certData = os.toByteArray();
                runOnUiThread(() -> {
                    EditText editText = findViewById(R.id.edt_install_cert_name);
                    editText.setText("TestCert");
                    editText = findViewById(R.id.edt_install_cert_contents);
                    editText.setText(new String(certData));
                    editText = findViewById(R.id.edt_uninstall_cert_name);
                    editText.setText("TestCert");
                });
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtil.close(is);
                IOUtil.close(os);
            }
        });
    }

    private void installCACertificate() {
        try {
            EditText editText = findViewById(R.id.edt_install_cert_name);
            String certName = editText.getText().toString();
            if (TextUtils.isEmpty(certName)) {
                showToast("certificate name should not be empty!");
                editText.requestFocus();
                return;
            }
            editText = findViewById(R.id.edt_install_cert_contents);
            String certContents = editText.getText().toString();
            if (TextUtils.isEmpty(certContents)) {
                showToast("certificate contents should not be empty!");
                editText.requestFocus();
                return;
            }
            int code = MyApplication.app.basicOptV2.installApplicationCertificate(certName, certContents);
            showToast(code == 0 ? "success" : "failed,code:" + code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uninstallCACertificate() {
        try {
            EditText editText = findViewById(R.id.edt_uninstall_cert_name);
            String certName = editText.getText().toString();
            if (TextUtils.isEmpty(certName)) {
                showToast("certificate name should not be empty!");
                editText.requestFocus();
                return;
            }
            int code = MyApplication.app.basicOptV2.uninstallApplicationCertificate(certName);
            showToast(code == 0 ? "success" : "failed,code:" + code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
