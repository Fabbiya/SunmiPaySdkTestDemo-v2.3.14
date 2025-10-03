package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;

/** TOSS机型上使用 */
public class GetSetDeviceCertificateActivity extends BaseAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_certificate);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_get_set_device_cert);
        findViewById(R.id.mb_save_cert).setOnClickListener((v) -> saveDeviceCertificate());
        findViewById(R.id.mb_get_cert).setOnClickListener((v) -> getDeviceCertificate());
        EditText edtCertData = findViewById(R.id.save_cert_data);
        edtCertData.setText(getTestCertificate(1));
    }

    /** 保存设备证书(TOSS使用) */
    private void saveDeviceCertificate() {
        try {
            EditText edtCertIndex = findViewById(R.id.save_cert_index);
            EditText edtCertData = findViewById(R.id.save_cert_data);
            String certIndexStr = edtCertIndex.getText().toString();
            String certDataStr = edtCertData.getText().toString();
            if (TextUtils.isEmpty(certIndexStr)) {
                showToast("certificate index shouldn't be empty");
                edtCertIndex.requestFocus();
                return;
            }
            int certIndex = Integer.parseInt(certIndexStr);
            if (certIndex < 9001 || certIndex > 9008) {
                showToast("certificate index should in [9001,9008]");
                return;
            }
            if (TextUtils.isEmpty(certDataStr)) {
                showToast("certificate data shouldn't be empty");
                edtCertData.requestFocus();
                return;
            }
            byte[] certData = certDataStr.getBytes();
            addStartTimeWithClear("setDeviceCertificate()");
            int code = MyApplication.app.securityOptV2.setDeviceCertificate(certIndex, certData);
            addEndTime("setDeviceCertificate()");
            showToast("save certificate " + (code == 0 ? "success" : "failed"));
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 获取保存的设备证书(TOSS使用) */
    private void getDeviceCertificate() {
        try {
            EditText edtCertIndex = findViewById(R.id.get_cert_index);
            String certIndexStr = edtCertIndex.getText().toString();
            if (TextUtils.isEmpty(certIndexStr)) {
                showToast("certificate index shouldn't be empty");
                edtCertIndex.requestFocus();
                return;
            }
            int certIndex = Integer.parseInt(certIndexStr);
            if (certIndex < 9001 || certIndex > 9008) {
                showToast("certificate index should in [9001,9008]");
                return;
            }
            byte[] buffer = new byte[2048];
            addStartTimeWithClear("getDeviceCertificate()");
            int len = MyApplication.app.securityOptV2.getDeviceCertificate(certIndex, buffer);
            addEndTime("getDeviceCertificate()");
            if (len < 0) {
                showToast("save certificate failed,code:" + len);
                return;
            }
            String certStr = new String(buffer, 0, len);
            TextView tvCertData = findViewById(R.id.tv_get_cert_data);
            tvCertData.setText(certStr);
            showToast("save certificate success");
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 获取测试用的证书数据 */
    private String getTestCertificate(int index) {
        final String testPerm1 = "-----BEGIN CERTIFICATE-----\r\n"
                + "MIIDRzCCAi+gAwIBAgIQRfijvoKalMeYP3ObMg/NojANBgkqhkiG9w0BAQsFADAs\r\n"
                + "MQswCQYDVQQGEwJDTjEdMBsGA1UEAwwUU3VubWkgR2xvYmFsIFJvb3QgQ0EwIBcN\r\n"
                + "MjAwOTAxMDMzODI1WhgPMjA1MDEwMTQwMzM4MjVaMCwxCzAJBgNVBAYTAkNOMR0w\r\n"
                + "GwYDVQQDDBRTdW5taSBHbG9iYWwgUm9vdCBDQTCCASIwDQYJKoZIhvcNAQEBBQAD\r\n"
                + "ggEPADCCAQoCggEBAK9NvCkSjGYtKeN2d+TOgrIOEY4ONMpNc1ArwqVK+4ysH4GO\r\n"
                + "gXTb3q8ccXFiF6sbwAP0NN+GAb7CcaOpD3bChgLenhVEvEXEgPrWskZiWAgpUwjs\r\n"
                + "40QFZZI/dDzRCOESyGceg404EIe3bTYH52R9JEluoy1bizfm5wj5wUK66gZkFqQ4\r\n"
                + "DIociv4Xe660W70QRNx02R3lBd5MCtaIhPFBc91+jnniGDbgNK2HschowVKXiWmm\r\n"
                + "hMgnGdCLsV0PWTdxNP7HEWYjpNMplGnfxEbORAw2VQvBbK3cLn1bHg68nRiRU/9P\r\n"
                + "e7WuHGqmYJpKgB7HxyDqqymNSZZSaW1IllYYK4kCAwEAAaNjMGEwHwYDVR0jBBgw\r\n"
                + "FoAUR0fZ+lgm9S/EGK0gB8Ka2+Lz96YwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8B\r\n"
                + "Af8EBAMCAf4wHQYDVR0OBBYEFEdH2fpYJvUvxBitIAfCmtvi8/emMA0GCSqGSIb3\r\n"
                + "DQEBCwUAA4IBAQAinBqnZ/KVXU4hmYJZpgzXEZgLraJ8hALZsV672I9HKOr2TSbj\r\n"
                + "bAkU1NiTIO3bltX7ZtermrSS5OOPjmwba8TnuBvp3pD/9l/RnL9F/5sSYaOGQZus\r\n"
                + "i4Y017DIZhw4BLFIoG5dld2EXopCs9cCWrhJUaJQedOD8f7rP+h1gHibY2Vu9pJh\r\n"
                + "4z6H1S7W/8q/U+DysAwDSNg56B1VrO8nIDhmj4vgJw66g18k35y/HvyfibJrq3ys\r\n"
                + "awuVL/1BBqF9b+uxmewj7UbvaxcbpqFbZQFsJJduQQx8F+AciR8Jmw5C43C1hWsb\r\n"
                + "L0gr0MHtV29k4vKkxdQvqvERRcdX1Mi5PcG9\r\n"
                + "-----END CERTIFICATE-----\r\n";

        final String testPem2 = "-----BEGIN CERTIFICATE-----\r\n"
                + "MIIDsTCCApmgAwIBAgIGEAAAAAARMA0GCSqGSIb3DQEBCwUAMCwxCzAJBgNVBAYT\r\n"
                + "AkNOMR0wGwYDVQQDDBRTdW5taSBHbG9iYWwgUm9vdCBDQTAeFw0yMDA5MTEwNzE5\r\n"
                + "MjNaFw0zMDA5MDkwNzE5MjNaMGkxHzAdBgNVBAMMFlN1bm1pIEFwcCBTaWduIENB\r\n"
                + "IDIwMjAxCzAJBgNVBAYTAkNOMRQwEgYDVQQKDAtTdW5taSBHcm91cDEjMCEGA1UE\r\n"
                + "CwwaU3VubWkgRmluYW5jaWFsIFRlY2hub2xvZ3kwggEiMA0GCSqGSIb3DQEBAQUA\r\n"
                + "A4IBDwAwggEKAoIBAQDYue9h+tFS1hQclxx67tSQFC27VtZYWpUOyTwEPlOQMM2f\r\n"
                + "ZxO0uIAro4k2DkeH9ugaj2o8VFcsHxIE3gMzyCiKTO2b7xG5S8IKgvWibqogZ7LW\r\n"
                + "5mlDVCJ5EoxAOoWKYiMlDctloNZs+4GUrkI2dYS/K5nnkDMA7IMvZgf+vWIpcjMa\r\n"
                + "zH4QD54Egx8nY4MbW3p3S2PCZA+7GOUvySJZWeS/JJESGqpaIkoY94OqiaIifQbJ\r\n"
                + "cGLqmerWkX5F/Gicswrz9+7jCdbL7beAlrDFZNGzkFJRFIc/hwzWlQg2fglIrkyF\r\n"
                + "Mqek2PMzvt7rdmLNrrVuevXLNQSaWmVUPSNpAKW9AgMBAAGjgZswgZgwHwYDVR0j\r\n"
                + "BBgwFoAUR0fZ+lgm9S/EGK0gB8Ka2+Lz96YwDwYDVR0TAQH/BAUwAwEB/zA1BgNV\r\n"
                + "HR8ELjAsMCqgKKAmhiRodHRwOi8vMTkyLjE2OC4zLjEzL2NybC9SU0EvY3JsMS5j\r\n"
                + "cmwwDgYDVR0PAQH/BAQDAgEGMB0GA1UdDgQWBBQBytRHjv6+sfvFp/U2CVILlLqM\r\n"
                + "BjANBgkqhkiG9w0BAQsFAAOCAQEARkLrmstsX1DFxsRUamj8I6ViQ3YfLHC1/wny\r\n"
                + "Kb+ESJn2bAJ5T+7Lu6nF3nDkIwHTBKR5qK5FyvZxZMTIJuXq0GyscPqEYhzxVcCQ\r\n"
                + "U9FqcJG1aTHitOYaNB32gjc7tFbRJmouPhDaK+xz7dzvOiWsx5X1TQSgb2aVr/qr\r\n"
                + "hTK9FFOgHCjyLRJXDAvcwjT8MJIQYoW5tJqiSdLIPqsBCJQL3uj8YhhMUqgoVuAu\r\n"
                + "l1ub9MVhSwGiNIgaeMFB8dGfuASC8CwFjNEzcRHfdUPgSrS63Wfl+OG88z4vzwFW\r\n"
                + "F4XpGTBd2h15yZkoz1knVIp4jzau20g61StvIIiD930vWIITAw==\r\n"
                + "-----END CERTIFICATE-----";
        return index == 1 ? testPerm1 : testPem2;
    }
}