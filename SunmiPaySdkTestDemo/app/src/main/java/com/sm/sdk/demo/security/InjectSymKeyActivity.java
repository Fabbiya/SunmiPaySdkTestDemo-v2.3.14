package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;

public class InjectSymKeyActivity extends BaseAppCompatActivity {
    private EditText edtMtmkIndex;
    private EditText edtMtmkValue;
    private EditText edtTseedValue;
    private EditText edtGenTmkIndex;
    private EditText edtKmdebIndex;
    private EditText edtKmdebValue;
    private EditText edtCsncanValue;
    private EditText edtGenKidbIndex;
    private EditText edtBseedIndex;
    private EditText edtBseedValue;
    private EditText edtGkmdebIndex;
    private EditText edtGkmdebValue;
    private EditText edtGenKmdebIndex;
    private EditText edtKiDebIndex;
    private EditText edtKiDebValue;
    private EditText edtInputData;
    private EditText edtSessionKeyIndex;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inject_sym_key);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_inejct_symmetric_key);
        edtMtmkIndex = findViewById(R.id.edt_mtmk_index);
        edtMtmkValue = findViewById(R.id.edt_mtmk_value);
        edtTseedValue = findViewById(R.id.edt_tseed_value);
        edtGenTmkIndex = findViewById(R.id.edt_gen_tmk_index);
        findViewById(R.id.btn_owf2_test).setOnClickListener(this);
        edtKmdebIndex = findViewById(R.id.edt_kmdeb_index);
        edtKmdebValue = findViewById(R.id.edt_kmdeb_value);
        edtCsncanValue = findViewById(R.id.edt_csncan_value);
        edtGenKidbIndex = findViewById(R.id.edt_gen_kidb_index);
        findViewById(R.id.btn_owf3_test).setOnClickListener(this);
        edtBseedIndex = findViewById(R.id.edt_bseed_index);
        edtBseedValue = findViewById(R.id.edt_bseed_value);
        edtGkmdebIndex = findViewById(R.id.edt_gkmdeb_index);
        edtGkmdebValue = findViewById(R.id.edt_gkmdeb_value);
        edtGenKmdebIndex = findViewById(R.id.edt_gen_kmdeb_index);
        findViewById(R.id.btn_gowf_test).setOnClickListener(this);
        edtKiDebIndex = findViewById(R.id.edt_kideb_index);
        edtKiDebValue = findViewById(R.id.edt_kideb_value);
        edtInputData = findViewById(R.id.edt_input_data);
        edtSessionKeyIndex = findViewById(R.id.edt_session_key_index);
        findViewById(R.id.btn_gen_session_key).setOnClickListener(this);

        edtMtmkIndex.setText("1");
        edtMtmkValue.setText("DA0137AB801549E5233FEBFF879FE3D6");
        edtTseedValue.setText("551C76B02C07B7F5794D03A24C6D97E6");
        edtGenTmkIndex.setText("2");

        edtKmdebIndex.setText("3");
        edtKmdebValue.setText("D21F020C18195154D21F020C18195154D21F020C18195154");
        edtCsncanValue.setText("B20D52A034081C181111737999504495");
        edtGenKidbIndex.setText("4");

        edtBseedIndex.setText("5");
        edtBseedValue.setText("111111111111111111111111111111111111111111111111");
        edtGkmdebIndex.setText("6");
        edtGkmdebValue.setText("19293CE45ED7F8DA5C83FBA0A1C2E00849A1A690A8A74AF1");
        edtGenKmdebIndex.setText("7");

        edtKiDebIndex.setText("8");
        edtKiDebValue.setText("DA0137AB801549E5233FEBFF879FE3D6");
        edtInputData.setText("551C76B02C07B7F5794D03A24C6D97E6");
        edtSessionKeyIndex.setText("9");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_owf2_test:
                testOWF2Algorithm();
                break;
            case R.id.btn_owf3_test:
                testOWF3Algorithm();
                break;
            case R.id.btn_gowf_test:
                testGOEFAlgorithm();
                break;
            case R.id.btn_gen_session_key:
                testGenerateSessionKey();
                break;
        }
    }

    /** OWF2 algorithm test */
    private void testOWF2Algorithm() {
        try {
            if (!checkKeyIndex(edtMtmkIndex, 0, 199, "mtmk")
                    || !checkKeyValue(edtMtmkValue, 32, "mtmk")
                    || !checkKeyValue(edtTseedValue, 32, "tseed")
                    || !checkKeyIndex(edtGenTmkIndex, 0, 199, "tmk")) {
                return;
            }
            int mtmkIndex = Integer.parseInt(edtMtmkIndex.getText().toString());
            byte[] mtmk = ByteUtil.hexStr2Bytes(edtMtmkValue.getText().toString());
            byte[] tseed = ByteUtil.hexStr2Bytes(edtTseedValue.getText().toString());
            int genTmkIndex = Integer.parseInt(edtGenTmkIndex.getText().toString());
            LogUtil.e(TAG, "start testOWF2Algorithm()...");
            //Case:测试 OWF2 算法 -- 模拟 TSEED 和 MTMK 经过 OWF2 算法生成 TMK
            //1.保存MTMK
            Bundle bundle = new Bundle();
            bundle.putInt("keyType", Security.KEY_TYPE_KEK);
            bundle.putByteArray("keyValue", mtmk);
            bundle.putByteArray("checkValue", null);
            bundle.putInt("keyAlgType", Security.KEY_ALG_TYPE_3DES);
            bundle.putInt("keyLength", mtmk.length);
            bundle.putInt("keyIndex", mtmkIndex);
            int code = MyApplication.app.securityOptV2.saveKeyEx(bundle);
            LogUtil.e(TAG, "save mtmk code:" + code);
            if (code < 0) {
                showToast("save mtmk failed, code:" + code);
                return;
            }
            //2.MTMK+TSEED经OWF2算法算法生成TMK
            bundle.clear();
            bundle.putInt("keyIndex", genTmkIndex);
            bundle.putInt("keyType", Security.KEY_TYPE_TMK);
            bundle.putByteArray("keyValue", tseed);
            bundle.putInt("keyAlgType", Security.KEY_ALG_TYPE_3DES);
            bundle.putInt("keyLength", tseed.length);
            bundle.putInt("encryptIndex1", mtmkIndex);
            bundle.putInt("injectMode", Security.INJECT_DERIVER_OWF2);
            code = MyApplication.app.securityOptV2.injectSymKeyEx(bundle);
            LogUtil.e(TAG, "MTMK+TSEED经OWF2算法算法生成TMK code:" + code);
            if (code < 0) {
                showToast("OWF2 algorithm generate TMK failed,code:" + code);
                return;
            }
            showToast("OWF2 algorithm generate tmk success");
            LogUtil.e(TAG, "testOWF2Algorithm() success...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** OWF3 algorithm test */
    private void testOWF3Algorithm() {
        try {
            if (!checkKeyIndex(edtKmdebIndex, 0, 199, "kmdeb")
                    || !checkKeyValue(edtKmdebValue, 48, "kmdeb")
                    || !checkKeyValue(edtCsncanValue, 32, "csncan")
                    || !checkKeyIndex(edtGenKidbIndex, 0, 199, "kideb")) {
                return;
            }
            int kmdebIndex = Integer.parseInt(edtKmdebIndex.getText().toString());
            byte[] kmdebValue = ByteUtil.hexStr2Bytes(edtKmdebValue.getText().toString());
            byte[] casnCan = ByteUtil.hexStr2Bytes(edtCsncanValue.getText().toString());
            int genKidbIndex = Integer.parseInt(edtGenKidbIndex.getText().toString());
            LogUtil.e(TAG, "start testOWF3Algorithm()...");
            //Case:测试 OWF3 算法 -- 模拟 CSN||CAN 和 KmDeb 经过 OWF3 算法生成 KiDeb
            //1.保存KmDeb
            Bundle bundle = new Bundle();
            bundle.putInt("keyType", Security.KEY_TYPE_TMK);
            bundle.putByteArray("keyValue", kmdebValue);
            bundle.putByteArray("checkValue", null);
            bundle.putInt("keyAlgType", Security.KEY_ALG_TYPE_3DES);
            bundle.putInt("keyLength", kmdebValue.length);
            bundle.putInt("keyIndex", kmdebIndex);
            int code = MyApplication.app.securityOptV2.saveKeyEx(bundle);
            LogUtil.e(TAG, "save kmdeb code:" + code);
            if (code < 0) {
                showToast("save kmdeb failed, code:" + code);
                return;
            }
            //2. KmDeb+CSN||CAND经OWF3算法算法生成 KiDeb
            bundle.clear();
            bundle.putInt("keyIndex", genKidbIndex);
            bundle.putInt("keyType", Security.KEY_TYPE_REC);
            bundle.putByteArray("keyValue", casnCan);
            bundle.putInt("keyAlgType", Security.KEY_ALG_TYPE_3DES);
            bundle.putInt("keyLength", casnCan.length);
            bundle.putInt("encryptIndex1", kmdebIndex);
            bundle.putInt("injectMode", Security.INJECT_DERIVER_OWF3);
            code = MyApplication.app.securityOptV2.injectSymKeyEx(bundle);
            LogUtil.e(TAG, "KmDeb+CSN||CAND经OWF3算法算法生成 KiDeb code:" + code);
            if (code < 0) {
                showToast("OWF3 algorithm generate KiDeb failed,code:" + code);
                return;
            }
            showToast("OWF3 algorithm generate KiDeb success");
            LogUtil.e(TAG, "testOWF3Algorithm() success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** GOWF algorithm test */
    private void testGOEFAlgorithm() {
        try {
            if (!checkKeyIndex(edtBseedIndex, 0, 199, "bseed")
                    || !checkKeyValue(edtBseedValue, 48, "bseed")
                    || !checkKeyIndex(edtGkmdebIndex, 0, 199, "gkmdeb")
                    || !checkKeyValue(edtGkmdebValue, 48, "gkmdeb")
                    || !checkKeyIndex(edtGenKmdebIndex, 0, 199, "kmdeb")) {
                return;
            }
            int bseedIndex = Integer.parseInt(edtBseedIndex.getText().toString());
            byte[] bseedValue = ByteUtil.hexStr2Bytes(edtBseedValue.getText().toString());
            int gkmdebIndex = Integer.parseInt(edtGkmdebIndex.getText().toString());
            byte[] gkmdebValue = ByteUtil.hexStr2Bytes(edtGkmdebValue.getText().toString());
            int genKmdebIndex = Integer.parseInt(edtGenKmdebIndex.getText().toString());
            LogUtil.e(TAG, "start testGOEFAlgorithm()...");
            //Case:测试 GOWF 算法 -- 模拟 GkmDeb 和 BSEED 经过 GOWF 算法生成 KmDeb
            //1.保存BSEED
            Bundle bundle = new Bundle();
            bundle.putInt("keyType", Security.KEY_TYPE_TMK);
            bundle.putByteArray("keyValue", bseedValue);
            bundle.putByteArray("checkValue", null);
            bundle.putInt("keyAlgType", Security.KEY_ALG_TYPE_3DES);
            bundle.putInt("keyLength", bseedValue.length);
            bundle.putInt("keyIndex", bseedIndex);
            int code = MyApplication.app.securityOptV2.saveKeyEx(bundle);
            LogUtil.e(TAG, "save bseed code:" + code);
            if (code < 0) {
                showToast("save bseed failed, code:" + code);
                return;
            }
            //2.保存GKmDeb
            bundle.clear();
            bundle.putInt("keyType", Security.KEY_TYPE_TMK);
            bundle.putByteArray("keyValue", gkmdebValue);
            bundle.putByteArray("checkValue", null);
            bundle.putInt("keyAlgType", Security.KEY_ALG_TYPE_3DES);
            bundle.putInt("keyLength", gkmdebValue.length);
            bundle.putInt("keyIndex", gkmdebIndex);
            code = MyApplication.app.securityOptV2.saveKeyEx(bundle);
            LogUtil.e(TAG, "save gkmdeb code:" + code);
            if (code < 0) {
                showToast("save gkmdeb failed, code:" + code);
                return;
            }
            //3. GkmDeb 和 BSEED 经过 GOWF 算法生成 KmDeb
            bundle.clear();
            bundle.putInt("keyIndex", genKmdebIndex);
            bundle.putInt("keyType", Security.KEY_TYPE_REC);
            bundle.putInt("keyAlgType", Security.KEY_ALG_TYPE_3DES);
            bundle.putInt("keyLength", gkmdebValue.length);
            bundle.putInt("encryptIndex1", bseedIndex);
            bundle.putInt("encryptIndex2", gkmdebIndex);
            bundle.putInt("injectMode", Security.INJECT_DERIVER_GOWF);
            code = MyApplication.app.securityOptV2.injectSymKeyEx(bundle);
            LogUtil.e(TAG, "GkmDeb 和 BSEED 经过 GOWF 算法生成 KmDeb code:" + code);
            if (code < 0) {
                showToast("GOWF algorithm generate KmDeb failed,code:" + code);
                return;
            }
            showToast("GOWF algorithm generate KmDeb success");
            LogUtil.e(TAG, "testGOEFAlgorithm() success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** KiDeb encrypt input data to generate session key */
    private void testGenerateSessionKey() {
        try {
            if (!checkKeyIndex(edtKiDebIndex, 0, 199, "kideb")
                    || !checkKeyValue(edtKiDebValue, 48, "kideb")
                    || !checkKeyValue(edtInputData, 48, "input data")
                    || !checkKeyIndex(edtSessionKeyIndex, 0, 199, "session key index")) {
                return;
            }
            int kidebIndex = Integer.parseInt(edtKiDebIndex.getText().toString());
            byte[] kidebValue = ByteUtil.hexStr2Bytes(edtKiDebValue.getText().toString());
            byte[] inputData = ByteUtil.hexStr2Bytes(edtInputData.getText().toString());
            int sessionKeyIndex = Integer.parseInt(edtSessionKeyIndex.getText().toString());

            LogUtil.e(TAG, "start testGOEFAlgorithm()...");
            // Case:测试 ENC 算法 -- 模拟 KiDeb 使用 3DES_ECB/CBC 对 buffer 运算生成 SessionKey
            //1.保存KiDeb
            Bundle bundle = new Bundle();
            bundle.putInt("keyType", Security.KEY_TYPE_TMK);
            bundle.putByteArray("keyValue", kidebValue);
            bundle.putByteArray("checkValue", null);
            bundle.putInt("keyAlgType", Security.KEY_ALG_TYPE_3DES);
            bundle.putInt("keyLength", kidebValue.length);
            bundle.putInt("keyIndex", kidebIndex);
            int code = MyApplication.app.securityOptV2.saveKeyEx(bundle);
            LogUtil.e(TAG, "save kideb code:" + code);
            if (code < 0) {
                showToast("save kideb failed, code:" + code);
                return;
            }
            //2.KiDeb 使用 3DES_ECB 对 buffer 运算生成 SessionKey
            bundle.clear();
            bundle.putInt("keyIndex", sessionKeyIndex);
            bundle.putInt("keyType", Security.KEY_TYPE_REC);
            bundle.putByteArray("keyValue", inputData);
            bundle.putByteArray("checkValue", null);
            bundle.putInt("keyAlgType", Security.KEY_ALG_TYPE_3DES);
            bundle.putInt("keyLength", inputData.length);
            bundle.putInt("encryptIndex1", kidebIndex);
            bundle.putInt("injectMode", Security.INJECT_DERIVER_ENC);
            bundle.putInt("dataMode", Security.DATA_MODE_ECB);
            code = MyApplication.app.securityOptV2.injectSymKeyEx(bundle);
            LogUtil.e(TAG, "KiDeb ECB模式生成SessionKey code:" + code);
            if (code < 0) {
                showToast("KiDeb+ ECB mode generate session key failed,code:" + code);
                return;
            }
            showToast("KiDeb+ ECB mode generate session key success");
            //3.KiDeb 使用 3DES_CBC 对 buffer 运算生成 SessionKey
            bundle.putInt("dataMode", Security.DATA_MODE_CBC);
            bundle.putByteArray("iv", new byte[8]);
            code = MyApplication.app.securityOptV2.injectSymKeyEx(bundle);
            LogUtil.e(TAG, "KiDeb+ CBC mode generate session key, code:" + code);
            if (code < 0) {
                showToast("KiDeb+ CBC mode generate session key failed,code:" + code);
                return;
            }
            showToast("KiDeb+ CBC mode generate session key success");
            LogUtil.e(TAG, "testGOEFAlgorithm() success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkKeyIndex(EditText edtKeyIndex, int min, int max, String tip) {
        String keyIndexStr = edtKeyIndex.getText().toString();
        if (TextUtils.isEmpty(keyIndexStr)) {
            String msg = Utility.formatStr("%s index should not be empty", tip);
            showToast(msg);
            edtKeyIndex.requestFocus();
            return false;
        }
        int keyIndex = Integer.parseInt(keyIndexStr);
        if (keyIndex < min || keyIndex > max) {
            String msg = Utility.formatStr("%s index should be in [%d-%d]", tip, min, max);
            showToast(msg);
            return false;
        }
        return true;
    }

    private boolean checkKeyValue(EditText edtKeyIndex, int maxLen, String tip) {
        String keyValueStr = edtKeyIndex.getText().toString();
        if (TextUtils.isEmpty(keyValueStr) || !Utility.checkHexValue(keyValueStr)) {
            String msg = Utility.formatStr("%s value should be HEX characters", tip);
            showToast(msg);
            edtKeyIndex.requestFocus();
            return false;
        }
        if (keyValueStr.length() > maxLen) {
            String msg = Utility.formatStr("%s value length should <= %d", tip, maxLen);
            showToast(msg);
            return false;
        }
        return true;
    }

}
