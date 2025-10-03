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
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;
import com.sunmi.pay.hardware.aidl.AidlErrorCode;

import java.util.ArrayList;
import java.util.List;

public class InjectDeviceCertPvkActivity extends BaseAppCompatActivity {
    private EditText edtTargetAppPkgName;
    private EditText edtEncKey;
    private EditText edtEncKeyIndex;
    private EditText edtPubKeyModule;
    private EditText edtPubKeyExponent;
    private EditText edtPvtKeyExponent;
    private EditText edtCertKeyIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inject_device_cert_pvt_key);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.hsm_inject_device_pvt_key);
        edtTargetAppPkgName = findViewById(R.id.edt_target_app_pkg_name);
        edtEncKey = findViewById(R.id.edt_encrypt_key);
        edtEncKeyIndex = findViewById(R.id.edt_encrypt_key_index);
        edtPubKeyModule = findViewById(R.id.edt_pub_key_module);
        edtPubKeyExponent = findViewById(R.id.edt_pub_key_exponent);
        edtPvtKeyExponent = findViewById(R.id.edt_pvt_key_exponent);
        edtCertKeyIndex = findViewById(R.id.edt_save_cert_key_index);
        findViewById(R.id.btn_ok).setOnClickListener(this);
        edtTargetAppPkgName.setText("com.sunmi.sdk.demov2");
        edtEncKey.setText("00112233445566778899AABBCCDDEEFF");
        edtEncKeyIndex.setText("1");
        edtPubKeyModule.setText("AE424600AA134385C6E061627C5C2D7B67E314120D4C31C5AC5102BC26BA7A02FD95835C3690095B9407DDCCE32AB33A35A3A8F168EED8967369C151FA0E81163BA280869B637A0A1D0ABA6D4F765AE4C80A4BADDC0FBE524B032C7235C4DE744CFAD7B830C6EAC21A5164AF75DDB80A861725D9DA7B7201F7D921855C9973F25F9177E92154EAC7AA5BF0C548B81E9328DA8E84B84D21DDBEE9CD8FADC9634DF0885EBBC3830D7A417887B1D0ABC83CA47C54E232B0347D3D0DB679D381FCB931DF81F6BA4916E969FF3C68B0AF7CCD6240DCDD3CF6A040B6ADF373F67BC36A19F0B59B3D0CA6AD8EFA000A6B01BA0F32E2AA35483E7236F9FBBB5310D9434D");
        edtPubKeyExponent.setText("00010001");
        edtPvtKeyExponent.setText("06B1F92A915E481BDDB64547996B993BC29410F3589B72D61B76C95A1D4AD0E14888F41118EF93CC76F58E6A0ED857268765105AA6237722E0B051832ACE5FDB50D33F88EC7377FEE77AC00AEF20A7015F4635FE2A1458C5A4A82C8EFECDDF962C56FEEECEC0F5C81B66C12D94A3BE2C79566E57DE731BC0439B8E1427A8A5B1BBF88AE5F6340B990F9CDE20AA09A35F92F75F2E52EE98C54124690BE1BD3D3DEFB1B452DCD662F55A0223AA85F74DBCE9BEA8E37227881A2AEB3DD569B0C68A141C9130D055B18FCCDB18ECA4C768E2D79B5D864977FD779CAA39ECA4731EEB2F1863B40BC11959D8A9C83BD52D11476FE9EBABCC57CE1879F6ABCD3E0BF229");
        edtCertKeyIndex.setText("9001");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                injectDeviceCertPrivateKey();
                break;
        }
    }

    /** Store device cert private key */
    private void injectDeviceCertPrivateKey() {
        try {
            //1.parse target app name
            String targetAppPkgName = edtTargetAppPkgName.getText().toString();
            if (TextUtils.isEmpty(targetAppPkgName)) {
                showToast(" target App name should not be empty");
                edtTargetAppPkgName.requestFocus();
                return;
            }
            //2. parse encryption key
            String encKeyValueStr = edtEncKey.getText().toString();
            if (TextUtils.isEmpty(encKeyValueStr) || !Utility.checkHexValue(encKeyValueStr)) {
                showToast("encryption key data should be HEX characters");
                edtEncKey.requestFocus();
                return;
            }
            byte[] encKeyValue = ByteUtil.hexStr2Bytes(encKeyValueStr);
            String encKeyIndexStr = edtEncKeyIndex.getText().toString();
            if (TextUtils.isEmpty(encKeyIndexStr)) {
                showToast("encryption key index should not be empty");
                edtEncKeyIndex.requestFocus();
                return;
            }
            int encKeyIndex = Integer.parseInt(encKeyIndexStr);
            if (encKeyIndex < 0 || encKeyIndex > 199) {
                showToast("encryption key index should be in [0-199]");
                return;
            }
            //3. save encryption key
            addStartTimeWithClear("savePlaintextKey()");
            int code = MyApplication.app.securityOptV2.savePlaintextKey(Security.KEY_TYPE_REC, encKeyValue, null, Security.KEY_ALG_TYPE_3DES, encKeyIndex);
            addEndTime("savePlaintextKey()");
            if (code < 0) {
                String msg = "save encryption key failed, code:" + code;
                showToast(msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //4. parse device cert key data
            String pukModuleStr = edtPubKeyModule.getText().toString();
            if (TextUtils.isEmpty(pukModuleStr) || !Utility.checkHexValue(pukModuleStr)) {
                showToast("public key module should be HEX characters");
                edtPubKeyModule.requestFocus();
                return;
            }
            String pukExponentStr = edtPubKeyExponent.getText().toString();
            if (TextUtils.isEmpty(pukExponentStr) || !Utility.checkHexValue(pukModuleStr)) {
                showToast("public key exponent should be HEX characters");
                edtPubKeyExponent.requestFocus();
                return;
            }
            String pvkExponentStr = edtPvtKeyExponent.getText().toString();
            if (TextUtils.isEmpty(pvkExponentStr) || !Utility.checkHexValue(pvkExponentStr)) {
                showToast("private key exponent should be HEX characters");
                edtPubKeyExponent.requestFocus();
                return;
            }
            String certKeyIndexStr = edtCertKeyIndex.getText().toString();
            if (TextUtils.isEmpty(certKeyIndexStr)) {
                showToast("cert key index should not be empty");
                edtCertKeyIndex.requestFocus();
                return;
            }
            int certKeyIndex = Integer.parseInt(certKeyIndexStr);
            if (certKeyIndex < 9001 || certKeyIndex > 9008) {
                showToast("cert key index should be in [9001-9008]");
                edtCertKeyIndex.requestFocus();
                return;
            }
            byte[] pubKeyMod = ByteUtil.hexStr2Bytes(pukModuleStr);
            byte[] pubKeyExp = ByteUtil.hexStr2Bytes(pukExponentStr);
            byte[] PvtKeyExp = ByteUtil.hexStr2Bytes(pvkExponentStr);

            //5. build RSA_PubKey (for CERT)
            SPRsaKey pk = new SPRsaKey();
            pk.key_type = 0;
            pk.ModulusLen = 2048;
            pk.Modulus = pubKeyMod;
            pk.ExponentLen = 32;
            pk.Exponent = pubKeyExp;
            byte[] certData = pk.toBytes();

            //6.build RSA_PriKey
            SPRsaKey sk = new SPRsaKey();
            sk.key_type = 1;
            sk.ModulusLen = 2048;
            sk.Modulus = pubKeyMod;
            sk.ExponentLen = sk.ModulusLen;
            sk.Exponent = PvtKeyExp;
            byte[] pvtKey = sk.toBytes();

            //7.encrypt PrvKey
            byte[] pvtKeyEnc = new byte[pvtKey.length];
            addStartTimeWithClear("dataEncrypt()");
            code = MyApplication.app.securityOptV2.dataEncrypt(encKeyIndex, pvtKey, AidlConstants.Security.DATA_MODE_ECB, null, pvtKeyEnc);
            addEndTime("dataEncrypt()");
            LogUtil.e(TAG, "encrypt private key , code:" + code);
            if (code < 0) {
                return;
            }
            LogUtil.e(TAG, "pvtKeyEnc length:" + pvtKeyEnc.length);
            LogUtil.e(TAG, "cert length:" + certData.length);

            //8. inject encryption key
            addStartTimeWithClear("injectPlaintextKey()");
            code = MyApplication.app.securityOptV2.injectPlaintextKey(targetAppPkgName, Security.KEY_TYPE_REC, encKeyValue, null, Security.KEY_ALG_TYPE_3DES, encKeyIndex);
            addEndTime("injectPlaintextKey()");
            if (code < 0) {
                String msg = "inject encryption key failed, code:" + code;
                showToast(msg);
                LogUtil.e(TAG, msg);
                return;
            }

            //9. inject certificate private key
            Bundle bundle = new Bundle();
            bundle.putString("targetAppPkgName", targetAppPkgName);
            bundle.putInt("certIndex", certKeyIndex);
            bundle.putInt("mode", 4);
            bundle.putBoolean("isEncrypt", true);
            bundle.putInt("encryptIndex", encKeyIndex);
            bundle.putByteArray("certData", certData);
            bundle.putByteArray("pvkData", pvtKeyEnc);

            addStartTimeWithClear("injectDeviceCertPrivateKey()");
            code = MyApplication.app.securityOptV2.injectDeviceCertPrivateKey(bundle);
            addEndTime("injectDeviceCertPrivateKey()");
            LogUtil.e(TAG, "injectDeviceCertPrivateKey(), code:" + code + ",msg:" + AidlErrorCode.valueOf(code).getMsg());
            showToast(code == 0 ? "success" : "failed,code:" + code);
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class SPRsaKey {
        private int ModulusLen;
        private byte[] Modulus = new byte[256];
        private int ExponentLen;
        private byte[] Exponent = new byte[256];
        private int key_type;
        private byte[] RFU = new byte[52];

        private byte[] toBytes() {
            List<byte[]> list = new ArrayList<>();
            list.add(ByteUtil.int2BytesLE(ModulusLen));
            list.add(Modulus);
            list.add(ByteUtil.int2BytesLE(ExponentLen));
            list.add(Exponent);
            list.add(ByteUtil.int2BytesLE(key_type));
            list.add(RFU);
            return ByteUtil.concatByteArrays(list);
        }
    }
}
