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

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;

public class HsmRsaTestActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hsm_rsa_test);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.hsm_rsa_keypair_test);
        findViewById(R.id.btn_hsm_gen_key_pair).setOnClickListener(this);
        findViewById(R.id.btn_hsm_inject_pub_key).setOnClickListener(this);
        findViewById(R.id.btn_hsm_inject_pvt_key).setOnClickListener(this);
        findViewById(R.id.btn_hsm_read_rsa_key).setOnClickListener(this);

        //test data
//        EditText edtPubSize = findViewById(R.id.edt_inject_pub_key_size);
//        EditText edtPubModule = findViewById(R.id.edt_inject_pub_key_module);
//        EditText edtPubKeyExp = findViewById(R.id.edt_inject_pub_key_exp);
//        EditText edtPvtSize = findViewById(R.id.edt_inject_pvt_key_size);
//        EditText edtPvtModule = findViewById(R.id.edt_inject_pvt_key_module);
//        EditText edtPvtKeyExp = findViewById(R.id.edt_inject_pvt_key_exp);
//        edtPubSize.setText("2048");
//        edtPubModule.setText("AE424600AA134385C6E061627C5C2D7B67E314120D4C31C5AC5102BC26BA7A02FD95835C3690095B9407DDCCE32AB33A35A3A8F168EED8967369C151FA0E81163BA280869B637A0A1D0ABA6D4F765AE4C80A4BADDC0FBE524B032C7235C4DE744CFAD7B830C6EAC21A5164AF75DDB80A861725D9DA7B7201F7D921855C9973F25F9177E92154EAC7AA5BF0C548B81E9328DA8E84B84D21DDBEE9CD8FADC9634DF0885EBBC3830D7A417887B1D0ABC83CA47C54E232B0347D3D0DB679D381FCB931DF81F6BA4916E969FF3C68B0AF7CCD6240DCDD3CF6A040B6ADF373F67BC36A19F0B59B3D0CA6AD8EFA000A6B01BA0F32E2AA35483E7236F9FBBB5310D9434D");
//        edtPubKeyExp.setText("010001");
//        edtPvtSize.setText("2048");
//        edtPvtModule.setText("AE424600AA134385C6E061627C5C2D7B67E314120D4C31C5AC5102BC26BA7A02FD95835C3690095B9407DDCCE32AB33A35A3A8F168EED8967369C151FA0E81163BA280869B637A0A1D0ABA6D4F765AE4C80A4BADDC0FBE524B032C7235C4DE744CFAD7B830C6EAC21A5164AF75DDB80A861725D9DA7B7201F7D921855C9973F25F9177E92154EAC7AA5BF0C548B81E9328DA8E84B84D21DDBEE9CD8FADC9634DF0885EBBC3830D7A417887B1D0ABC83CA47C54E232B0347D3D0DB679D381FCB931DF81F6BA4916E969FF3C68B0AF7CCD6240DCDD3CF6A040B6ADF373F67BC36A19F0B59B3D0CA6AD8EFA000A6B01BA0F32E2AA35483E7236F9FBBB5310D9434D");
//        edtPvtKeyExp.setText("06B1F92A915E481BDDB64547996B993BC29410F3589B72D61B76C95A1D4AD0E14888F41118EF93CC76F58E6A0ED857268765105AA6237722E0B051832ACE5FDB50D33F88EC7377FEE77AC00AEF20A7015F4635FE2A1458C5A4A82C8EFECDDF962C56FEEECEC0F5C81B66C12D94A3BE2C79566E57DE731BC0439B8E1427A8A5B1BBF88AE5F6340B990F9CDE20AA09A35F92F75F2E52EE98C54124690BE1BD3D3DEFB1B452DCD662F55A0223AA85F74DBCE9BEA8E37227881A2AEB3DD569B0C68A141C9130D055B18FCCDB18ECA4C768E2D79B5D864977FD779CAA39ECA4731EEB2F1863B40BC11959D8A9C83BD52D11476FE9EBABCC57CE1879F6ABCD3E0BF229");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_hsm_gen_key_pair:
                generateRsaKeyPair();
                break;
            case R.id.btn_hsm_inject_pub_key:
                injectRsaPublicKey();
                break;
            case R.id.btn_hsm_inject_pvt_key:
                injectRsaPrivateKey();
                break;
            case R.id.btn_hsm_read_rsa_key:
                readRsaKey();
                break;
        }
    }

    /**
     * Generate RSA keypair
     */
    private void generateRsaKeyPair() {
        try {
            EditText edt = findViewById(R.id.edt_gen_pvt_key_index);
            String pvtKeyIndexStr = edt.getText().toString();
            if (TextUtils.isEmpty(pvtKeyIndexStr)) {
                showToast("private key index should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_gen_pvt_key_size);
            String pvtKeySizeStr = edt.getText().toString();
            if (TextUtils.isEmpty(pvtKeySizeStr)) {
                showToast("private key size should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_gen_rsa_key_exp);
            String rsaKeyExpStr = edt.getText().toString();
            if (TextUtils.isEmpty(rsaKeyExpStr)) {
                showToast("rsa Key Exponent should not be empty");
                edt.requestFocus();
                return;
            }
            int pvtKeyIndex = Integer.parseInt(pvtKeyIndexStr);
            int pvtKeySize = Integer.parseInt(pvtKeySizeStr);

            byte[] dataOut = new byte[512];
            addStartTimeWithClear("hsmGenerateRSAKeypair()");
            int len = MyApplication.app.securityOptV2.hsmGenerateRSAKeypair(pvtKeyIndex, pvtKeySize, rsaKeyExpStr, dataOut);
            addEndTime("hsmGenerateRSAKeypair()");
            LogUtil.e(TAG, "hsmGenerateRSAKeypair len:" + len);
            showSpendTime();
            if (len >= 0) {
                String module = ByteUtil.bytes2HexStr(Arrays.copyOf(dataOut, len));
                LogUtil.e(TAG, "module = " + module);
                KeySpec spec = new RSAPublicKeySpec(new BigInteger(module, 16), new BigInteger(rsaKeyExpStr, 16));
                PublicKey rsa = KeyFactory.getInstance("RSA").generatePublic(spec);
                String publicKeyStr = ByteUtil.bytes2HexStr(rsa.getEncoded());
                LogUtil.e(TAG, "publicKey = " + publicKeyStr);
                if (publicKeyStr.contains(module)) {
                    showToast("hsm generate RSA keypair success");
                } else {
                    showToast("hsm generate RSA keypair failed");
                }
            } else {
                showToast("hsm generate RSA keypair failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Inject a RSA public key */
    private void injectRsaPublicKey() {
        try {
            EditText edt = findViewById(R.id.edt_inject_pub_key_index);
            String keyIndexStr = edt.getText().toString();
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast("public key index should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_inject_pub_key_size);
            String keySizeStr = edt.getText().toString();
            if (TextUtils.isEmpty(keySizeStr)) {
                showToast("public key size should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_inject_pub_key_module);
            String keyModuleStr = edt.getText().toString();
            if (TextUtils.isEmpty(keyModuleStr)) {
                showToast("public key module should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_inject_pub_key_exp);
            String keyPubExpStr = edt.getText().toString();
            if (TextUtils.isEmpty(keyPubExpStr)) {
                showToast("public key exponent should not be empty");
                edt.requestFocus();
                return;
            }
            int keyIndex = Integer.parseInt(keyIndexStr);
            int keySize = Integer.parseInt(keySizeStr);
            addStartTimeWithClear("hsmInjectRSAKey()");
            int code = MyApplication.app.securityOptV2.hsmInjectRSAKey(keyIndex, keySize, keyModuleStr, keyPubExpStr);
            addEndTime("hsmInjectRSAKey()");
            LogUtil.e(TAG, "code:" + code);
            if (code == 0) {//success
                PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger(1, ByteUtil.hexStr2Bytes(keyModuleStr)), new BigInteger(ByteUtil.hexStr2Bytes(keyPubExpStr))));
                byte[] dataIn = ByteUtil.hexStr2Bytes("44444444444444444444444444444444");
                byte[] encrypt = encrypt(dataIn, publicKey);
                LogUtil.e(TAG, "encrypt = " + ByteUtil.bytes2HexStr(encrypt));
                showToast("hsm inject RSA public key success");
            } else {//failed
                showToast("hsm inject RSA public key failed,code:" + code);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Inject a RSA private key */
    private void injectRsaPrivateKey() {
        try {
            EditText edt = findViewById(R.id.edt_inject_pvt_key_index);
            String keyIndexStr = edt.getText().toString();
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast("private key index should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_inject_pvt_key_size);
            String keySizeStr = edt.getText().toString();
            if (TextUtils.isEmpty(keySizeStr)) {
                showToast("private key size should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_inject_pvt_key_module);
            String keyModuleStr = edt.getText().toString();
            if (TextUtils.isEmpty(keyModuleStr)) {
                showToast("private key module should not be empty");
                return;
            }
            edt = findViewById(R.id.edt_inject_pvt_key_exp);
            String keyPvtExpStr = edt.getText().toString();
            if (TextUtils.isEmpty(keyPvtExpStr)) {
                showToast("private key exponent should not be empty");
                return;
            }
            int keyIndex = Integer.parseInt(keyIndexStr);
            int keySize = Integer.parseInt(keySizeStr);
            addStartTimeWithClear("hsmInjectRSAKey()");
            int code = MyApplication.app.securityOptV2.hsmInjectRSAKey(keyIndex, keySize, keyModuleStr, keyPvtExpStr);
            addEndTime("hsmInjectRSAKey()");
            LogUtil.e(TAG, "code:" + code);
            if (code == 0) {//success
                PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new RSAPrivateKeySpec(new BigInteger(1, ByteUtil.hexStr2Bytes(keyModuleStr)), new BigInteger(ByteUtil.hexStr2Bytes(keyPvtExpStr))));
                byte[] dataIn = ByteUtil.hexStr2Bytes("857210779D3EC1AC1EE1B6A9A1BBD513D429F0D1CB9229308E625E3F31F630199C4DF34D42F289607C88E76173FECEDBB0F70B5D0E8A195F836BABF57BA1D8EAFAF4BA9EF4AE03188B943A0C0DF4F74599A4EC91CE3EF29E9F860D675B1033C729919961F781888045F465EB1F66E203A8BC909F766A4F18E7C938D20ED6F05E0E25053B32B96A2F254C4413B28119FE0CDE46CFE3DBD4ED03EA6A65429E7BC8E8A34E1CB6A06BABFA23B915B99A17190DB1355522C982E26F38109F653DBB21652990A4610740B4F7B59EB8DF9BC0D46D09EB4563642591A89BA3811B860AECD532B504D78A1307CF7CA2541328E6779B4EB62464F8FB88252756A93F9B0F03");
                byte[] decrypt = decrypt(dataIn, privateKey);
                LogUtil.e(TAG, "decrypt = " + ByteUtil.bytes2HexStr(decrypt));
                showToast("hsm inject RSA private key success");
            } else {//failed
                showToast("hsm inject RSA private key failed,code:" + code);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final int ENCRYPT_BLOCK_SIZE = 245;//加密时分组长度,245字节
    private static final int DECRYPT_BLOCK_SIZE = 256;//解密时分组长度,256字节

    /**
     * 用RSA算法加密数据，分组长度固定为245字节
     *
     * @param dataIn 待加密的数据
     * @param rsaKey 加密密钥
     * @return 加密后的数据
     */
    public static byte[] encrypt(byte[] dataIn, Key rsaKey) {
        if (dataIn == null || dataIn.length == 0) {
            return null;
        }
        int inputLen = dataIn.length;
        int blockSize = ENCRYPT_BLOCK_SIZE;
        int offset = 0;
        List<byte[]> list = new ArrayList<>();
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, rsaKey);
            while (offset < inputLen) { //对数据分段加密/解密
                if (offset + blockSize <= inputLen) {
                    list.add(cipher.doFinal(dataIn, offset, blockSize));
                } else {
                    list.add(cipher.doFinal(dataIn, offset, inputLen - offset));
                }
                offset += blockSize;
            }
            byte[] cipherData = ByteUtil.concatByteArrays(list);
            return cipherData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;//加密失败
    }

    /**
     * 用RSA算法解密数据,分组长度为256字节
     *
     * @param dataIn 待解密的数据
     * @param rsaKey 解密密钥
     * @return 解密后的数据
     */
    public static byte[] decrypt(byte[] dataIn, Key rsaKey) {
        if (dataIn == null || dataIn.length == 0 || dataIn.length % DECRYPT_BLOCK_SIZE != 0) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, rsaKey);
            int count = dataIn.length / DECRYPT_BLOCK_SIZE;
            for (int i = 0; i < count; i++) {
                byte[] block = Arrays.copyOfRange(dataIn, i * DECRYPT_BLOCK_SIZE, (i + 1) * DECRYPT_BLOCK_SIZE);
                cipher.update(block);
            }
            return cipher.doFinal();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;//解密失败
    }

    /** Read RSA key */
    private void readRsaKey() {
        try {
            String keyIndexStr = this.<EditText>findViewById(R.id.edt_read_key_index).getText().toString();
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast("key index should not be empty");
                return;
            }
            TextView tvKeyInfo = findViewById(R.id.tv_rsa_key_info);
            int keyIndex = Integer.parseInt(keyIndexStr);
            Bundle bundle = new Bundle();
            int code = MyApplication.app.securityOptV2.readRSAKey(keyIndex, bundle);
            if (code < 0) {
                showToast("Read RSA key failed, code:" + code);
                return;
            }
            byte[] modulus = bundle.getByteArray("modulus");
            byte[] exponent = bundle.getByteArray("exponent");
            String msg = Utility.formatStr("modulus:%s\nexponent:%s", ByteUtil.bytes2HexStr(modulus), ByteUtil.bytes2HexStr(exponent));
            tvKeyInfo.setText(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}