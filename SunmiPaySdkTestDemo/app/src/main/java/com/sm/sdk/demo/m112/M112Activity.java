package com.sm.sdk.demo.m112;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;

import java.util.Arrays;
import java.util.regex.Pattern;

public class M112Activity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m112_layout);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.etc_trade_test);
        findViewById(R.id.mb_reset).setOnClickListener((v) -> onResetClick());
        findViewById(R.id.mb_get_version).setOnClickListener((v) -> onGetVersionClick());
        findViewById(R.id.mb_get_cpu_id).setOnClickListener((v) -> onGetCPUIdClick());
        findViewById(R.id.mb_query_tag_in_field).setOnClickListener((v) -> onQueryTagInFieldClick());
        findViewById(R.id.mb_enable_auto_detect_mode).setOnClickListener((v) -> onEnableAutoDetectClick());
        findViewById(R.id.mb_disable_auto_detect_mode).setOnClickListener((v) -> onDisableAutoDetectClick());
        findViewById(R.id.mb_write_t5557).setOnClickListener((v) -> onWriteT5557BlockClick());
    }

    /** 复位系统 */
    private void onResetClick() {
        try {
            EditText edtSrcAddr = findViewById(R.id.edt_rest_src_addr);
            EditText edtDestAddr = findViewById(R.id.edt_reset_dest_addr);
            if (!checkAddress(edtSrcAddr, edtDestAddr)) {
                return;
            }
            int srcAddr = Integer.parseInt(edtSrcAddr.getText().toString());
            int destAddr = Integer.parseInt(edtDestAddr.getText().toString());
            int code = MyApplication.app.rfidOptV2.m112Reset(srcAddr, destAddr);
            showToast("Reset " + (code == 0 ? "success" : "failed"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 获取版本 */
    @SuppressLint("SetTextI18n")
    private void onGetVersionClick() {
        try {
            EditText edtSrcAddr = findViewById(R.id.edt_get_version_src_addr);
            EditText edtDestAddr = findViewById(R.id.edt_get_version_dest_addr);
            if (!checkAddress(edtSrcAddr, edtDestAddr)) {
                return;
            }
            int srcAddr = Integer.parseInt(edtSrcAddr.getText().toString());
            int destAddr = Integer.parseInt(edtDestAddr.getText().toString());
            byte[] out = new byte[1024];
            int len = MyApplication.app.rfidOptV2.m112GetVersion(srcAddr, destAddr, out);
            if (len < 0) {
                showToast("Get version failed code: " + len);
                return;
            }
            byte[] valid = Arrays.copyOf(out, len);
            String fullVersion = new String(valid);
            String[] list = fullVersion.split("\\s+");
            LogUtil.e(Constant.TAG, "get version result:" + fullVersion);
            TextView result = findViewById(R.id.txt_get_version_result);
            if (list.length != 3) {//返回数据格式错误
                showToast("Get version failed, version format error");
            } else {
                result.setText(Utility.formatStr("Type：%s\nSV：%s\nHV：%s", list[0], list[1], list[2]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 获取CPU唯一ID号 */
    private void onGetCPUIdClick() {
        try {
            EditText edtSrcAddr = findViewById(R.id.edt_get_cpu_id_src_addr);
            EditText edtDestAddr = findViewById(R.id.edt_get_cpu_id_dest_addr);
            if (!checkAddress(edtSrcAddr, edtDestAddr)) {
                return;
            }
            int srcAddr = Integer.parseInt(edtSrcAddr.getText().toString());
            int destAddr = Integer.parseInt(edtDestAddr.getText().toString());
            byte[] out = new byte[1024];
            int len = MyApplication.app.rfidOptV2.m112GetCPUId(srcAddr, destAddr, out);
            if (len < 0) {
                showToast("Get CPU id failed code: " + len);
                return;
            }
            byte[] valid = Arrays.copyOf(out, len);
            String cpuId = ByteUtil.bytes2HexStr(valid);
            LogUtil.e(Constant.TAG, "Get CPU id result:" + cpuId);
            TextView result = findViewById(R.id.txt_get_cpu_id_result);
            result.setText(Utility.formatStr("CPU ID：%s", cpuId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 查询场内标签 */
    @SuppressLint("SetTextI18n")
    private void onQueryTagInFieldClick() {
        try {
            EditText edtSrcAddr = findViewById(R.id.edt_query_tag_in_field_src_addr);
            EditText edtDestAddr = findViewById(R.id.edt_query_tag_in_field_dest_addr);
            if (!checkAddress(edtSrcAddr, edtDestAddr)) {
                return;
            }
            int srcAddr = Integer.parseInt(edtSrcAddr.getText().toString());
            int destAddr = Integer.parseInt(edtDestAddr.getText().toString());
            byte[] out = new byte[1024];
            int len = MyApplication.app.rfidOptV2.m112QueryTagInField(srcAddr, destAddr, out);
            if (len < 0) {
                showToast("Query tag in field failed code: " + len);
                return;
            }
            byte[] valid = Arrays.copyOf(out, len);
            String tagUID = ByteUtil.bytes2HexStr(valid);
            LogUtil.e(Constant.TAG, "Query tag in field result UID:" + tagUID);
            TextView result = findViewById(R.id.txt_query_tag_in_field_result);
            if (TextUtils.isEmpty(tagUID)) {//未查询到标签
                showToast("Query tag in field failed, no tag found");
            } else {
                result.setText(Utility.formatStr("Tag UID:%s", tagUID));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 开启自动盘点模式 */
    private void onEnableAutoDetectClick() {
        try {
            EditText edtSrcAddr = findViewById(R.id.edt_enable_auto_detect_src_addr);
            EditText edtDestAddr = findViewById(R.id.edt_enable_auto_detect_dest_addr);
            EditText edtFreq = findViewById(R.id.edt_enable_auto_detect_freq);
            if (!checkAddress(edtSrcAddr, edtDestAddr)) {
                return;
            }
            String freqStr = edtFreq.getText().toString();
            if (TextUtils.isEmpty(freqStr)) {
                showToast("Frequency should not be empty");
                return;
            }
            int freq = Integer.parseInt(freqStr);
            if (freq < 1 || freq > 255) {
                showToast("Frequency should be in [1,255]");
                return;
            }
            int srcAddr = Integer.parseInt(edtSrcAddr.getText().toString());
            int destAddr = Integer.parseInt(edtDestAddr.getText().toString());
            int code = MyApplication.app.rfidOptV2.m112EnableAutoDetectMode(srcAddr, destAddr, freq);
            showToast("Enable auto detect mode " + (code == 0 ? "success" : "failed"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 关闭自动盘点模式 */
    private void onDisableAutoDetectClick() {
        try {
            EditText edtSrcAddr = findViewById(R.id.edt_disable_auto_detect_src_addr);
            EditText edtDestAddr = findViewById(R.id.edt_disable_auto_detect_dest_addr);
            if (!checkAddress(edtSrcAddr, edtDestAddr)) {
                return;
            }
            int srcAddr = Integer.parseInt(edtSrcAddr.getText().toString());
            int destAddr = Integer.parseInt(edtDestAddr.getText().toString());
            int code = MyApplication.app.rfidOptV2.m112DisableAutoDetectMode(srcAddr, destAddr);
            showToast("Disable auto detect mode " + (code == 0 ? "success" : "failed"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 写T5557标签块数据 */
    private void onWriteT5557BlockClick() {
        try {
            EditText edtSrcAddr = findViewById(R.id.edt_write_t5557_src_addr);
            EditText edtDestAddr = findViewById(R.id.edt_write_t5557_dest_addr);
            if (!checkAddress(edtSrcAddr, edtDestAddr)) {
                return;
            }
            EditText edtPage = findViewById(R.id.edt_write_t5557_page);
            EditText edtBlock = findViewById(R.id.edt_write_t5557_block);
            EditText edtLockFlag = findViewById(R.id.edt_write_t5557_lock_flag);
            EditText edtData = findViewById(R.id.edt_write_t5557_data);
            EditText edtPwdFlag = findViewById(R.id.edt_write_t5557_pwd_flag);
            EditText edtPwd = findViewById(R.id.edt_write_t5557_pwd);
            if (TextUtils.isEmpty(edtPage.getText())) {
                showToast("page should not be empty");
                edtPage.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(edtBlock.getText())) {
                showToast("block should not be empty");
                edtBlock.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(edtLockFlag.getText())) {
                showToast("lock flag should not be empty");
                edtLockFlag.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(edtData.getText()) || !checkHexValue(edtData.getText().toString())) {
                showToast("block data should be 8 hex characters!");
                edtData.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(edtPwdFlag.getText())) {
                showToast("password flag should not be empty");
                edtPwdFlag.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(edtPwd.getText()) || !checkHexValue(edtData.getText().toString())) {
                showToast("password should be 8 hex characters!");
                edtPwd.requestFocus();
                return;
            }
            int srcAddr = Integer.parseInt(edtSrcAddr.getText().toString());
            int destAddr = Integer.parseInt(edtDestAddr.getText().toString());
            int page = Integer.parseInt(edtPage.getText().toString());
            int block = Integer.parseInt(edtBlock.getText().toString());
            int lockFlag = Integer.parseInt(edtLockFlag.getText().toString());
            byte[] data = ByteUtil.hexStr2Bytes(edtData.getText().toString());
            int pwdFlag = Integer.parseInt(edtPwdFlag.getText().toString());
            byte[] pwd = ByteUtil.hexStr2Bytes(edtPwd.getText().toString());
            int code = MyApplication.app.rfidOptV2.m112WriteT557Block(srcAddr, destAddr, page, block, lockFlag, data, pwdFlag, pwd);
            showToast("write t5557 block " + (code == 0 ? "success" : "failed"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkAddress(EditText edtSrcAddr, EditText edtDestAddr) {
        String srcAddr = edtSrcAddr.getText().toString();
        String destAddr = edtDestAddr.getText().toString();
        if (TextUtils.isEmpty(srcAddr) || srcAddr.length() != 4 || !checkHexValue(srcAddr)) {
            edtSrcAddr.requestFocus();
            showToast("src address should be 4 hex characters!");
            return false;
        }
        if (TextUtils.isEmpty(destAddr) || destAddr.length() != 4 || !checkHexValue(destAddr)) {
            edtDestAddr.requestFocus();
            showToast("dest address should be 4 hex characters!");
            return false;
        }
        return true;
    }

    private boolean checkHexValue(String src) {
        return Pattern.matches("[0-9a-fA-F]+", src);
    }
}
