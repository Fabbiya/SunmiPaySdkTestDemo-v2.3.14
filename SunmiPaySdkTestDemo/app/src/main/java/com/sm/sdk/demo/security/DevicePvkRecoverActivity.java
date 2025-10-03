package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class DevicePvkRecoverActivity extends BaseAppCompatActivity {
    private EditText mEditData;
    private EditText mEditKeyIndex;
    private TextView mTvInfo;
    private String hashAlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_pvk_recover);
        initToolbarBringBack(R.string.hsm_device_pvk_recover);
        initView();
    }

    private void initView() {
        RadioGroup rgHashType = findViewById(R.id.hash_type_group);
        rgHashType.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_hash_type0:
                            hashAlg = "";
                            break;
                        case R.id.rb_hash_type1:
                            hashAlg = "sha-1";
                            break;
                        case R.id.rb_hash_type2:
                            hashAlg = "sha-224";
                            break;
                        case R.id.rb_hash_type3:
                            hashAlg = "sha-256";
                            break;
                        case R.id.rb_hash_type4:
                            hashAlg = "sha-384";
                            break;
                        case R.id.rb_hash_type5:
                            hashAlg = "sha-512";
                            break;
                    }
                }
        );
        rgHashType.check(R.id.rb_hash_type0);
        mEditData = findViewById(R.id.source_data);
        mEditKeyIndex = findViewById(R.id.key_index);
        mTvInfo = findViewById(R.id.tv_info);
        findViewById(R.id.mb_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_ok:
                rsaRecover();
                break;
        }
    }

    private void rsaRecover() {
        String dataStr = mEditData.getText().toString();
        String keyIndexStr = mEditKeyIndex.getText().toString();

        int keyIndex;
        try {
            keyIndex = Integer.parseInt(keyIndexStr);
            if (keyIndex < 9001 || keyIndex > 9008) {
                showToast(R.string.security_device_pvk_index_hint);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast(R.string.security_mksk_key_index_hint);
            return;
        }
        if (dataStr.trim().length() == 0) {
            showToast(R.string.security_source_data_hint);
            return;
        }
        if (dataStr.length() % 2 != 0) {
            showToast(R.string.security_source_data_hint);
            return;
        }
        byte[] dataIn = ByteUtil.hexStr2Bytes(dataStr);
        byte[] dataOut = signBySP(keyIndex, dataIn, hashAlg);
        String hexStr = ByteUtil.bytes2HexStr(dataOut);
        LogUtil.e(TAG, "rsaRecover() output:" + hexStr);
        mTvInfo.setText(hexStr);
    }

    public byte[] signBySP(int keyIndex, byte[] dataIn, String hashAlg) {
        if (dataIn == null || dataIn.length == 0) {
            return null;
        }
        try {
            byte[] buffer = new byte[2048];
            int len = algASN1Sha(hashAlg, dataIn, buffer); //hash运算
            if (len < 0) { // len ：长度（包括asn1格式数据）
                return null;
            }
            byte[] dataOut = new byte[2048];
            byte[] hashIn = Arrays.copyOf(buffer, len);
            addStartTimeWithClear("devicePrivateKeyRecover()");
            len = MyApplication.app.securityOptV2.devicePrivateKeyRecover(keyIndex, 0, 1, hashIn, dataOut); //sp签名
            addEndTime("devicePrivateKeyRecover()");
            showSpendTime();
            if (len < 0) {
                toastHint(len);
                return null;
            }
            return Arrays.copyOf(dataOut, len);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 哈希运算
     *
     * @param hashAlg 哈希算法类型
     * @param dataIn  待运算数据
     * @param buffer  运算结果数据缓冲区
     * @return >=0-buffer中有效数据的长度，<0-出错
     */
    private int algASN1Sha(String hashAlg, byte[] dataIn, byte[] buffer) {
        if (dataIn == null || dataIn.length == 0 || buffer == null) {
            return -1;
        }
        byte[] asn1Byte = new byte[0];
        switch (hashAlg) {
            case "sha-1":
                asn1Byte = ByteUtil.hexStr2Bytes("3021300906052B0E03021A05000414"); // ASN1: 1.3.14.3.2.26 sha1 (OIW)
                break;
            case "sha-224":
                asn1Byte = ByteUtil.hexStr2Bytes("3031300D06096086480165030402040500041C"); // ASN1: 1.3.14.3.2.26 sha1 (OIW)
                break;
            case "sha-256":
                asn1Byte = ByteUtil.hexStr2Bytes("3031300D060960864801650304020105000420"); // ASN1: 2.16.840.1.101.3.4.2.1 sha-256 (NIST Algorithm)
                break;
            case "sha-384":
                asn1Byte = ByteUtil.hexStr2Bytes("3031300D060960864801650304020205000430"); // ASN1: 2.16.840.1.101.3.4.2.2 sha-384 (NIST Algorithm)
                break;
            case "sha-512":
                asn1Byte = ByteUtil.hexStr2Bytes("3031300D060960864801650304020305000440"); // ASN1: 2.16.840.1.101.3.4.2.3 sha-512 (NIST Algorithm)
                break;
        }
        byte[] hashOut = hash(dataIn, hashAlg);
        System.arraycopy(asn1Byte, 0, buffer, 0, asn1Byte.length);
        System.arraycopy(hashOut, 0, buffer, asn1Byte.length, hashOut.length);
        return asn1Byte.length + hashOut.length;
    }

    public byte[] hash(byte[] dataIn, String algorithm) {
        try {
            if (dataIn == null || dataIn.length == 0 || TextUtils.isEmpty(algorithm)) {//no hash
                return dataIn;
            }
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return md.digest(dataIn);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}