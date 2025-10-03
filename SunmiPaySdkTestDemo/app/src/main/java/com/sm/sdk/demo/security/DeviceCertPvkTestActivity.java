package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;
import com.sunmi.pay.hardware.aidl.AidlErrorCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeviceCertPvkTestActivity extends BaseAppCompatActivity {
    private EditText edtEncKey;
    private EditText edtEncKeyIndex;
    private EditText edtPubKeyModule;
    private EditText edtPubKeyExponent;
    private EditText edtPvtKeyExponent;
    private EditText edtCertKeyIndex;
    private EditText edtGetCertKeyIndex;
    private TextView txtGetCertResult;
    private EditText edtRecoverCertKeyIndex;
    private TextView txtRecoverResult;
    private EditText edtRecoverSourceData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_cert_pvt_key_test);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.hsm_device_cert_pvt_key_test);
        edtEncKey = findViewById(R.id.edt_encrypt_key);
        edtEncKeyIndex = findViewById(R.id.edt_encrypt_key_index);
        edtPubKeyModule = findViewById(R.id.edt_pub_key_module);
        edtPubKeyExponent = findViewById(R.id.edt_pub_key_exponent);
        edtPvtKeyExponent = findViewById(R.id.edt_pvt_key_exponent);
        edtCertKeyIndex = findViewById(R.id.edt_save_cert_key_index);
        edtGetCertKeyIndex = findViewById(R.id.edt_get_cert_key_index);
        txtGetCertResult = findViewById(R.id.get_cert_result);
        edtRecoverCertKeyIndex = findViewById(R.id.edt_recover_cert_key_index);
        txtRecoverResult = findViewById(R.id.txt_cert_recover_result);
        edtRecoverSourceData = findViewById(R.id.edt_recover_source_data);
        findViewById(R.id.btn_save_cert_key).setOnClickListener(this);
        findViewById(R.id.btn_get_cert_data).setOnClickListener(this);
        findViewById(R.id.btn_cert_recover).setOnClickListener(this);
        edtEncKey.setText("00112233445566778899AABBCCDDEEFF");
        edtEncKeyIndex.setText("1");
        edtPubKeyModule.setText("AE424600AA134385C6E061627C5C2D7B67E314120D4C31C5AC5102BC26BA7A02FD95835C3690095B9407DDCCE32AB33A35A3A8F168EED8967369C151FA0E81163BA280869B637A0A1D0ABA6D4F765AE4C80A4BADDC0FBE524B032C7235C4DE744CFAD7B830C6EAC21A5164AF75DDB80A861725D9DA7B7201F7D921855C9973F25F9177E92154EAC7AA5BF0C548B81E9328DA8E84B84D21DDBEE9CD8FADC9634DF0885EBBC3830D7A417887B1D0ABC83CA47C54E232B0347D3D0DB679D381FCB931DF81F6BA4916E969FF3C68B0AF7CCD6240DCDD3CF6A040B6ADF373F67BC36A19F0B59B3D0CA6AD8EFA000A6B01BA0F32E2AA35483E7236F9FBBB5310D9434D");
        edtPubKeyExponent.setText("00010001");
        edtPvtKeyExponent.setText("06B1F92A915E481BDDB64547996B993BC29410F3589B72D61B76C95A1D4AD0E14888F41118EF93CC76F58E6A0ED857268765105AA6237722E0B051832ACE5FDB50D33F88EC7377FEE77AC00AEF20A7015F4635FE2A1458C5A4A82C8EFECDDF962C56FEEECEC0F5C81B66C12D94A3BE2C79566E57DE731BC0439B8E1427A8A5B1BBF88AE5F6340B990F9CDE20AA09A35F92F75F2E52EE98C54124690BE1BD3D3DEFB1B452DCD662F55A0223AA85F74DBCE9BEA8E37227881A2AEB3DD569B0C68A141C9130D055B18FCCDB18ECA4C768E2D79B5D864977FD779CAA39ECA4731EEB2F1863B40BC11959D8A9C83BD52D11476FE9EBABCC57CE1879F6ABCD3E0BF229");
        edtCertKeyIndex.setText("9001");
        edtGetCertKeyIndex.setText("9001");
        edtRecoverCertKeyIndex.setText("9001");
        edtRecoverSourceData.setText("B859D678065F2A6B7575FF174158083F50F6ED8993297B26161C19E881A8B3D209731385D29CD98D960C274DF8A4CC7BFE96A170395B1136CDB8E53CCEFED5A5590A7ED9E26CBC6C9E8DE656BC90F6E83CE49A5DC565C24C8800E1A034973B5EDDCF5A40C029871DA32B4E5AAA58A8DEDA18CAB3416E3BE91C77C5E864BAC2E7");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save_cert_key:
                saveDeviceCertPrivateKey();
                break;
            case R.id.btn_get_cert_data:
                getDeviceCertKeyData();
                break;
            case R.id.btn_cert_recover:
                deviceCertKeyRecover();
                break;
        }
    }

    /** Store device cert private key */
    private void saveDeviceCertPrivateKey() {
        try {
            //1.save encryption key
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
            addStartTimeWithClear("savePlaintextKey()");
            int code = MyApplication.app.securityOptV2.savePlaintextKey(Security.KEY_TYPE_REC, encKeyValue, null, Security.KEY_ALG_TYPE_3DES, encKeyIndex);
            addEndTime("savePlaintextKey()");
            if (code < 0) {
                String msg = "save encryption key failed, code:" + code;
                showToast(msg);
                LogUtil.e(TAG, msg);
                return;
            }
            //2. parse device cert key data
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

            //3. build RSA_PubKey (for CERT)
            SPRsaKey pk = new SPRsaKey();
            pk.key_type = 0;
            pk.ModulusLen = 2048;
            pk.Modulus = pubKeyMod;
            pk.ExponentLen = 32;
            pk.Exponent = pubKeyExp;
            byte[] certData = pk.toBytes();

            //4.build RSA_PriKey
            SPRsaKey sk = new SPRsaKey();
            sk.key_type = 1;
            sk.ModulusLen = 2048;
            sk.Modulus = pubKeyMod;
            sk.ExponentLen = sk.ModulusLen;
            sk.Exponent = PvtKeyExp;
            byte[] pvtKey = sk.toBytes();

            //5.encrypt PrvKey
            byte[] pvtKeyEnc = new byte[pvtKey.length];
            addStartTimeWithClear("dataEncrypt()");
            code = MyApplication.app.securityOptV2.dataEncrypt(encKeyIndex, pvtKey, Security.DATA_MODE_ECB, null, pvtKeyEnc);
            addEndTime("dataEncrypt()");
            LogUtil.e(TAG, "encrypt private key , code:" + code);
            if (code < 0) {
                return;
            }
            LogUtil.e(TAG, "pvtKeyEnc length:" + pvtKeyEnc.length);
            LogUtil.e(TAG, "cert length:" + certData.length);
            //6. store certificate private key
            addStartTimeWithClear("storeDeviceCertPrivateKey()");
            code = MyApplication.app.securityOptV2.storeDeviceCertPrivateKey(certKeyIndex, 4, encKeyIndex, certData, pvtKeyEnc);
            addEndTime("storeDeviceCertPrivateKey()");
            LogUtil.e(TAG, "storeDeviceCertPrivateKey(), code:" + code + ",msg:" + AidlErrorCode.valueOf(code).getMsg());
            showToast(code == 0 ? "success" : "failed,code:" + code);
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Get device cert key */
    private void getDeviceCertKeyData() {
        try {
            //1.parse cert key index
            String certKeyIndexStr = edtGetCertKeyIndex.getText().toString();
            if (TextUtils.isEmpty(certKeyIndexStr)) {
                showToast("cert key index should not be empty");
                edtGetCertKeyIndex.requestFocus();
                return;
            }
            int certKeyIndex = Integer.parseInt(certKeyIndexStr);
            if (certKeyIndex < 9001 || certKeyIndex > 9008) {
                showToast("cert key index should be in [9001-9008]");
                edtGetCertKeyIndex.requestFocus();
                return;
            }
            //2. get cert data
            byte[] buffer = new byte[2048];
            addStartTime("getDeviceCertificate()");
            int len = MyApplication.app.securityOptV2.getDeviceCertificate(certKeyIndex, buffer);
            addEndTime("getDeviceCertificate()");
            LogUtil.e(TAG, " getDeviceCertificate(), code:" + len);
            if (len < 0) {
                showToast("failed, code:" + len);
                return;
            }
            String certData = ByteUtil.bytes2HexStr(Arrays.copyOf(buffer, len));
            LogUtil.e(TAG, "getDeviceCertificate(), certData:" + certData);
            txtGetCertResult.setText(certData);
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deviceCertKeyRecover() {
        try {
            //1.parse cert key index
            String certKeyIndexStr = edtRecoverCertKeyIndex.getText().toString();
            if (TextUtils.isEmpty(certKeyIndexStr)) {
                showToast("cert key index should not be empty");
                edtRecoverCertKeyIndex.requestFocus();
                return;
            }
            int certKeyIndex = Integer.parseInt(certKeyIndexStr);
            if (certKeyIndex < 9001 || certKeyIndex > 9008) {
                showToast("cert key index should be in [9001-9008]");
                edtRecoverCertKeyIndex.requestFocus();
                return;
            }
            String dataInStr = edtRecoverSourceData.getText().toString();
            if (TextUtils.isEmpty(dataInStr) || !Utility.checkHexValue(dataInStr)) {
                showToast("source data should be HEX characters");
                edtRecoverSourceData.requestFocus();
                return;
            }
            //2. cert private recover
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataInStr);
            byte[] buffer = new byte[2048];
            addStartTime("devicePrivateKeyRecover()");
            int len = MyApplication.app.securityOptV2.devicePrivateKeyRecover(certKeyIndex, 0, 1, dataIn, buffer);
            addEndTime("devicePrivateKeyRecover()");
            LogUtil.e(TAG, "devicePrivateKeyRecover(), code:" + len);
            if (len < 0) {
                showToast("failed, code:" + len);
                return;
            }
            String outData = ByteUtil.bytes2HexStr(Arrays.copyOf(buffer, len));
            LogUtil.e(TAG, "devicePrivateKeyRecover(), out:" + outData);
            txtRecoverResult.setText(outData);
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
