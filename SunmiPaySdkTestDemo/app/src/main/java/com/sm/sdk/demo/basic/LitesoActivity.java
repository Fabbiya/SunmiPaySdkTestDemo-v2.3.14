package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.Utility;

import java.util.Arrays;
import java.util.regex.Pattern;

public class LitesoActivity extends BaseAppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liteso);
        intView();
    }

    private void intView() {
        initToolbarBringBack(R.string.basic_liteso_test);
        findViewById(R.id.btn_install_ok).setOnClickListener((v) -> installLiteso());
        findViewById(R.id.btn_remove_ok).setOnClickListener((v) -> removeLiteso());
        findViewById(R.id.btn_run_ok).setOnClickListener((v) -> runLiteso());
        findViewById(R.id.btn_info_ok).setOnClickListener((v) -> getLitesoInfo());
        findViewById(R.id.btn_running_info_ok).setOnClickListener((v) -> getRunningLitesoInfo());
        findViewById(R.id.btn_custom_cmd_ok).setOnClickListener((v) -> sendCustomizeLitesoCmd());
    }

    /** Install a liteso */
    private void installLiteso() {
        try {
            EditText edtIndex = findViewById(R.id.edt_install_index);
            EditText edtPath = findViewById(R.id.edt_install_file_path);
            String indexStr = edtIndex.getText().toString();
            if (!checkIndex(indexStr)) {
                showToast("liteso index should in [0,13]");
                edtIndex.requestFocus();
                return;
            }
            String filePath = edtPath.getText().toString();
            if (TextUtils.isEmpty(filePath)) {
                showToast("liteso file path shouldn't be empty");
                edtPath.requestFocus();
                return;
            }
            if (filePath.length() > 128) {
                showToast("liteso file path length should <=128");
                edtPath.requestFocus();
                return;
            }
            int index = Integer.parseInt(indexStr);
            int code = MyApplication.app.basicOptV2.litesoInstaller(index, filePath);
            if (code == 0) {
                showToast("install liteso success");
            } else {
                showToast("install liteso failed, code:" + code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Remove liteso */
    private void removeLiteso() {
        try {
            EditText edtIndex = findViewById(R.id.edt_remove_index);
            String indexStr = edtIndex.getText().toString();
            if (!checkIndex(indexStr)) {
                showToast("liteso index should in [0,13]");
                edtIndex.requestFocus();
                return;
            }
            int index = Integer.parseInt(indexStr);
            int code = MyApplication.app.basicOptV2.litesoRemove(index);
            if (code == 0) {
                showToast("remove liteso success");
            } else {
                showToast("remove liteso failed, code:" + code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Run an installed liteso */
    private void runLiteso() {
        try {
            EditText edtIndex = findViewById(R.id.edt_run_index);
            String indexStr = edtIndex.getText().toString();
            if (!checkIndex(indexStr)) {
                showToast("liteso index should in [0,13]");
                edtIndex.requestFocus();
                return;
            }
            int index = Integer.parseInt(indexStr);
            int code = MyApplication.app.basicOptV2.litesoRun(index);
            if (code == 0) {
                showToast("run liteso success");
            } else {
                showToast("run liteso failed, code:" + code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Get specified liteso info */
    private void getLitesoInfo() {
        try {
            EditText edtIndex = findViewById(R.id.edt_info_index);
            String indexStr = edtIndex.getText().toString();
            if (!checkIndex(indexStr)) {
                showToast("liteso index should in [0,13]");
                edtIndex.requestFocus();
                return;
            }
            int index = Integer.parseInt(indexStr);
            Bundle out = new Bundle();
            int code = MyApplication.app.basicOptV2.litesoInfo(index, out);
            if (code != 0) {
                showToast("get liteso info failed, code:" + code);
                return;
            }
            showToast("get liteso info success");
            TextView result = findViewById(R.id.liteso_info);
            String msg = Utility.formatStr("Name:%s\nDesc:%s\nVendor:%s\nVersion:%s",
                    out.getString("name"),
                    out.getString("desc"),
                    out.getString("vendor"),
                    out.getString("version"));
            result.setText(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Get current running liteso info */
    private void getRunningLitesoInfo() {
        try {
            Bundle out = new Bundle();
            int code = MyApplication.app.basicOptV2.litesoRunInfo(out);
            if (code != 0) {
                showToast("get running liteso info failed, code:" + code);
                return;
            }
            showToast("get running liteso info success");
            TextView result = findViewById(R.id.running_liteso_info);
            String msg = Utility.formatStr("Name:%s\nDesc:%s\nVendor:%s\nVersion:%s",
                    out.getString("name"),
                    out.getString("desc"),
                    out.getString("vendor"),
                    out.getString("version"));
            result.setText(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Send customized liteso command */
    private void sendCustomizeLitesoCmd() {
        try {
            EditText edtCmd = findViewById(R.id.edt_custom_cmd);
            EditText edtCmdData = findViewById(R.id.edt_custom_cmd_data);
            String cmdStr = edtCmd.getText().toString();
            String cmdDataStr = edtCmdData.getText().toString();
            if (TextUtils.isEmpty(cmdStr) || !checkHexValue(cmdDataStr)) {
                showToast("cmd should be Hex value");
                edtCmd.requestFocus();
                return;
            }
            if (!TextUtils.isEmpty(cmdDataStr) && !checkHexValue(cmdDataStr)) {
                showToast("cmd data should be Hex string");
                edtCmdData.requestFocus();
                return;
            }
            int cmd = Integer.parseInt(cmdStr);
            byte[] cmdData = ByteUtil.hexStr2Bytes(cmdDataStr);
            byte[] buffer = new byte[2048];
            int len = MyApplication.app.basicOptV2.litesoCustomCmd(cmd, cmdData, buffer);
            if (len < 0) {
                showToast("send liteso customize cmd failed, code:" + len);
                return;
            }
            showToast("run liteso success");
            byte[] valid = Arrays.copyOf(buffer, len);
            TextView result = findViewById(R.id.custom_cmd_result);
            result.setText("output:" + ByteUtil.bytes2HexStr(valid));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Check liteso index */
    private boolean checkIndex(String indexStr) {
        try {
            int index = Integer.parseInt(indexStr);
            return index >= 0 && index <= 13;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean checkHexValue(String src) {
        return Pattern.matches("[0-9a-fA-F]+", src);
    }


}
