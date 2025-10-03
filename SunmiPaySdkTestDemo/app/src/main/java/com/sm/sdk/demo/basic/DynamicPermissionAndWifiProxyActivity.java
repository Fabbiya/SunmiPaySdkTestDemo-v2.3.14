package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;

public class DynamicPermissionAndWifiProxyActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_dynamic_permission_and_wifi_proxy);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.basic_dynamic_perm_wifi_proxy);
        findViewById(R.id.btn_set_dynamic_perm).setOnClickListener(v -> setDynamicPermission());
        findViewById(R.id.btn_set_wifi_proxy).setOnClickListener((v) -> setGlobalWifiProxy());
    }

    private void setDynamicPermission() {
        try {
            EditText editText = findViewById(R.id.edt_pkg_name);
            String pkgName = editText.getText().toString();
            if (TextUtils.isEmpty(pkgName)) {
                showToast("package name should not be empty!");
                editText.requestFocus();
                return;
            }
            int code = MyApplication.app.basicOptV2.allowDynamicPermission(pkgName);
            showToast(code == 0 ? "success" : "failed,code:" + code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setGlobalWifiProxy() {
        try {
            EditText editText = findViewById(R.id.edit_proxy);
            String proxy = editText.getText().toString();
            if (TextUtils.isEmpty(proxy)) {
                showToast("proxy should not be empty!");
                editText.requestFocus();
                return;
            }
            String[] items = proxy.split(":");
            if (items.length != 2) {
                showToast("proxy format error!");
                return;
            }
            int code = MyApplication.app.basicOptV2.setGlobalProxy(proxy);
            showToast(code == 0 ? "success" : "failed,code:" + code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
