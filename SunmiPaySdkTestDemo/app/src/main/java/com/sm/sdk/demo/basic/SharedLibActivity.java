package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;

import java.io.File;

public class SharedLibActivity extends BaseAppCompatActivity {
    private EditText edtDirInstall;
    private EditText edtDirDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_lib);
        initView();
        initData();
    }

    private void initView() {
        initToolbarBringBack(R.string.basic_shared_lib_test);
        edtDirInstall = findViewById(R.id.edt_install_libs_dir);
        edtDirDelete = findViewById(R.id.edt_delete_libs_dir);
        findViewById(R.id.btn_install).setOnClickListener(this);
        findViewById(R.id.btn_delete).setOnClickListener(this);
    }

    private void initData() {
        File dir = MyApplication.app.getExternalFilesDir(null);
        File libDir = new File(dir, "emvlib");
        if (!libDir.exists() || !libDir.isDirectory()) {
            libDir.mkdirs();
        }
        edtDirInstall.setText(libDir.getAbsolutePath());
        edtDirDelete.setText(libDir.getAbsolutePath());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_install:
                installSharedLibs();
                break;
            case R.id.btn_delete:
                deleteSharedLibs();
                break;
        }
    }

    private void installSharedLibs() {
        try {
            String libDir = edtDirInstall.getText().toString();
            if (TextUtils.isEmpty(libDir)) {
                showToast("Shared lib directory shouldn't be empty");
                return;
            }
            String[] libNames = new File(libDir).list((dir, name) -> name.endsWith(".so"));
            if (libNames == null || libNames.length == 0) {
                showToast("Not found shared libs in directory " + libDir);
                return;
            }
            int count = 0;
            for (String name : libNames) {
                String path = libDir + File.separator + name;
                int code = MyApplication.app.basicOptV2.installSharedLib(path);
                LogUtil.e(TAG, "install shared lib " + path + ",code:" + code);
                if (code < 0) {
                    String msg = "install shared lib " + name + " failed, code:" + code;
                    showToast(msg);
                    LogUtil.e(TAG, msg);
                    break;
                }
                count++;
            }
            if (count == libNames.length) {
                showToast("install " + count + " libs success");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteSharedLibs() {
        try {
            String libDir = edtDirDelete.getText().toString();
            if (TextUtils.isEmpty(libDir)) {
                showToast("Shared lib directory shouldn't be empty");
                return;
            }
            String[] libNames = new File(libDir).list((dir, name) -> name.endsWith(".so"));
            if (libNames == null || libNames.length == 0) {
                showToast("Not found shared libs in directory " + libDir);
                return;
            }
            int count = 0;
            for (String name : libNames) {
                int code = MyApplication.app.basicOptV2.deleteSharedLib(name);
                LogUtil.e(TAG, "delete shared lib " + name + ",code:" + code);
                if (code < 0) {
                    String msg = "delete shared lib" + name + " failed, code:" + code;
                    showToast(msg);
                    LogUtil.e(TAG, msg);
                    break;
                }
                count++;
            }
            if (count == libNames.length) {
                showToast("delete " + count + " libs success");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}