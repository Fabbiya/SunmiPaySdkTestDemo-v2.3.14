package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.security.SecurityOptV2;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class GetDeviceCertificateActivity extends BaseAppCompatActivity {

    private EditText mEditKeyIndex;
    private TextView mTvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_device_certificate);
        initToolbarBringBack(R.string.hsm_get_device_cert);
        initView();
    }

    private void initView() {
        mEditKeyIndex = findViewById(R.id.key_index);
        mTvInfo = findViewById(R.id.tv_info);
        findViewById(R.id.mb_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ok:
                getDeviceCert();
                break;
        }
    }

    private void getDeviceCert() {
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

        String devAuthCert = getDeviceCertInfo(keyIndex);
        LogUtil.e(TAG, "devAuthCert = " + devAuthCert);
        X509Certificate devAuthX509Certificate = getX509Certificate(devAuthCert);
        if (devAuthX509Certificate != null) {
            String x509CertificateStr = devAuthX509Certificate.toString();
            LogUtil.e(TAG, "x509Certificate = " + x509CertificateStr);
            mTvInfo.setText(x509CertificateStr);
        } else {
            mTvInfo.setText("");
        }
    }


    private String getDeviceCertInfo(int keyIndex) {
        StringBuilder sb = new StringBuilder();
        try {
            byte[] devAuth = new byte[2048];
            SecurityOptV2 securityOptV2 = MyApplication.app.securityOptV2;
            if (securityOptV2 != null) {
                addStartTimeWithClear("getDeviceCertificate()");
                int len = securityOptV2.getDeviceCertificate(keyIndex, devAuth);
                addEndTime("getDeviceCertificate()");
                if (len > 0) {
                    sb.append(new String((Arrays.copyOf(devAuth, len))));
                } else {
                    toastHint(len);
                }
                showSpendTime();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static X509Certificate getX509Certificate(String masterCertData) {
        if (TextUtils.isEmpty(masterCertData)) {
            LogUtil.e(TAG, "masterCertData 为空");
            return null;
        }
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(masterCertData.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtil.e(TAG, " getX509Certificate--->> failed:");
        return null;
    }
}