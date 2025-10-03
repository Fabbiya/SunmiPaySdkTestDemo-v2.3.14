package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;

public class InjectCiphertextKeyUnderRsaActivity extends BaseAppCompatActivity {
    private EditText edtTargetPkgName;
    private EditText edtKeyIndex;
    private EditText edtRsaKeyIndex;
    private EditText edtKeyData;
    private EditText edtCheckValue;
    private int keyType = Security.KEY_TYPE_KEK;
    private int keyAlgType = Security.KEY_ALG_TYPE_3DES;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inject_ciphertext_key_under_rsa);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_inject_ciphertext_key_under_rsa);
        RadioGroup group = findViewById(R.id.rg_key_type);
        group.setOnCheckedChangeListener((g, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_kek:
                    keyType = Security.KEY_TYPE_KEK;
                    break;
                case R.id.rb_tmk:
                    keyType = Security.KEY_TYPE_TMK;
                    break;
                case R.id.rb_pik:
                    keyType = Security.KEY_TYPE_PIK;
                    break;
                case R.id.rb_tdk:
                    keyType = Security.KEY_TYPE_TDK;
                    break;
                case R.id.rb_mak:
                    keyType = Security.KEY_TYPE_MAK;
                    break;
                case R.id.rb_rec_key:
                    keyType = Security.KEY_TYPE_REC;
                    break;
            }
        });
        group = findViewById(R.id.rg_key_alg_type);
        group.setOnCheckedChangeListener((g, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_3des:
                    keyAlgType = Security.KEY_ALG_TYPE_3DES;
                    break;
                case R.id.rb_sm4:
                    keyAlgType = Security.KEY_ALG_TYPE_SM4;
                    break;
                case R.id.rb_aes:
                    keyAlgType = Security.KEY_ALG_TYPE_AES;
                    break;
            }
        });
        edtTargetPkgName = findViewById(R.id.edt_target_pkg_name);
        edtKeyIndex = findViewById(R.id.edt_key_index);
        edtRsaKeyIndex = findViewById(R.id.edt_rsa_key_index);
        edtCheckValue = findViewById(R.id.edt_key_check_value);
        edtKeyData = findViewById(R.id.edt_key_data);
        findViewById(R.id.btn_ok).setOnClickListener((v) -> injectCiphertextKeyUnderRSA());

        //test data
//        edtKeyData.setText("2BA10762510D396E22785695CD815ACA3ED62E0D92BE0ECE66D640B9D7CB7CC06BEE5F6D90ECF8FA770C5DE051E068FF22B7944EE470318EF4AE3A99DB1CE32D82CEA6860491E8C6CE3692396AEF2A7961AAAA44B0BE2E1B1A2AAFFC40B7D0C3A6C3990A089EF0BBAB7AB5E3033E42B937C50100DD94AB8ED69B70F3EFBE21D5F3D0B2BC8D0AC8F150E6F0D449098321C302A1A31C2ECC197C55B395B82DCB1C2CDE034E887A2DDA6EAEA5B50D2D21A9591AD1C66088AD683E46C5152E16DC0E9F6AD752421C6F491C469023EAF17A901C2EC6EFBEA572C5CB6CBEC7B175E8492FE3E6AFD0484C545112B9424B0D252517442A22F835B527469C7A49C92FBB80");
//        edtCheckValue.setText("B0B563C2");
    }

    /** Inject a ciphertext mksk key, the key's decrypt key is a RSA private key */
    private void injectCiphertextKeyUnderRSA() {
        try {
            String targetPkgName = edtTargetPkgName.getText().toString();
            if (TextUtils.isEmpty(targetPkgName)) {
                showToast("target pkg name should not be empty");
                edtTargetPkgName.requestFocus();
                return;
            }
            String keyIndexStr = edtKeyIndex.getText().toString();
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast("key index should not be empty");
                edtKeyIndex.requestFocus();
                return;
            }
            int keyIndex = Integer.parseInt(keyIndexStr);
            if (keyIndex < 0 || keyIndex > 199) {
                showToast("key index should in [0,199]");
                edtKeyIndex.requestFocus();
                return;
            }
            String rsaKeyIndexStr = edtRsaKeyIndex.getText().toString();
            if (TextUtils.isEmpty(rsaKeyIndexStr)) {
                showToast("rsa key index should not be empty");
                edtRsaKeyIndex.requestFocus();
                return;
            }
            int rsaKeyIndex = Integer.parseInt(rsaKeyIndexStr);
            if (rsaKeyIndex < 0 || rsaKeyIndex > 19) {
                showToast("key index should in [0,19]");
                edtRsaKeyIndex.requestFocus();
                return;
            }
            String keyDataStr = edtKeyData.getText().toString();
            if (TextUtils.isEmpty(keyDataStr) || !Utility.checkHexValue(keyDataStr)) {
                showToast(" key data should be HEX characters");
                edtKeyData.requestFocus();
                return;
            }
            byte[] keyData = ByteUtil.hexStr2Bytes(keyDataStr);
            String checkValueStr = edtCheckValue.getText().toString();
            if (!TextUtils.isEmpty(checkValueStr) && !Utility.checkHexValue(checkValueStr)) {
                showToast(" key check value should be HEX characters");
                edtCheckValue.requestFocus();
                return;
            }
            byte[] checkValue = null;
            if (!TextUtils.isEmpty(checkValueStr)) {
                checkValue = ByteUtil.hexStr2Bytes(checkValueStr);
            }
            addStartTime("injectCiphertextKeyUnderRSA()");
            int code = MyApplication.app.securityOptV2.injectCiphertextKeyUnderRSA(targetPkgName, keyIndex, rsaKeyIndex, keyType, keyAlgType, checkValue, keyData);
            addEndTime("injectCiphertextKeyUnderRSA()");
            String msg = null;
            if (code == 0) {
                msg = "injectCiphertextKeyUnderRSA() success";
            } else {
                msg = "injectCiphertextKeyUnderRSA() failed, code:" + code;
            }
            LogUtil.e(TAG, msg);
            showToast(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
