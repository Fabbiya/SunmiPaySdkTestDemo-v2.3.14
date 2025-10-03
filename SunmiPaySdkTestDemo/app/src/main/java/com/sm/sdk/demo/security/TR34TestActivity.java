package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;
import com.sunmi.pay.hardware.aidl.AidlConstants;

/**
 * @Desc
 * @Author blanks
 * @Date 2023/11/8 2:05 PM
 */
public class TR34TestActivity extends BaseAppCompatActivity {
    private static final String TAG = "TR34Activity";

    private final String kdhCredToken = "MIIFqAYJKoZIhvcNAQcCoIIFmTCCBZUCAQExADAPBgkqhkiG9w0BBwGgAgQAoIIDnTCCA5kwggKBoAMCAQICBRjDNF/JMA0GCSqGSIb3DQEBCwUAMGkxHzAdBgNVBAMMFlN1bm1pIEFwcCBTaWduIENBIDIwMjAxCzAJBgNVBAYTAkNOMRQwEgYDVQQKDAtTdW5taSBHcm91cDEjMCEGA1UECwwaU3VubWkgRmluYW5jaWFsIFRlY2hub2xvZ3kwHhcNMjIwMjE4MDgzNzU4WhcNMjQwMjE4MDgzNzU4WjBOMRcwFQYDVQQDDA5zdW5taXRlc3RfMDIxODELMAkGA1UEBhMCQ04xEjAQBgNVBAoMCXN1bm1pdGVzdDESMBAGA1UECwwJc3VubWl0ZXN0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArkJGAKoTQ4XG4GFifFwte2fjFBINTDHFrFECvCa6egL9lYNcNpAJW5QH3czjKrM6NaOo8Wju2JZzacFR+g6BFjuigIabY3oKHQq6bU92WuTICkut3A++UksDLHI1xN50TPrXuDDG6sIaUWSvdd24CoYXJdnae3IB99khhVyZc/JfkXfpIVTqx6pb8MVIuB6TKNqOhLhNId2+6c2PrcljTfCIXrvDgw16QXiHsdCryDykfFTiMrA0fT0NtnnTgfy5Md+B9rpJFulp/zxosK98zWJA3N089qBAtq3zc/Z7w2oZ8LWbPQymrY76AAprAboPMuKqNUg+cjb5+7tTENlDTQIDAQABo2MwYTAfBgNVHSMEGDAWgBQBytRHjv6+sfvFp/U2CVILlLqMBjAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBBjAdBgNVHQ4EFgQUSwfNVTJONUuRny2C5dO3QLwkxR4wDQYJKoZIhvcNAQELBQADggEBAKFwrXH51tL2vUr0n2lVkE9kfYHCgCNZvNyAvnKsUbKFY89VhB82Kibgr+KG9rxjz4WondFoAsvSg5dPaqVy9KyLCxHJiXPbPqnDTk7YkZxNfMXYQR0DP70lMYPBVwysCFxx3MsH04ZTSCxSECynkv2ePZFeDnAIelZCnFSteLlfaNdFzQy/KXMRdM8Iw1W+SLONzQQz6H6M13kz4ZdUlfiiAl6yTDFfh5vZpRMzaS5b/2XUjCtbk93NMcReImggGUHA/2O1fMyC1outaFP7E10FwVsgYf3crXhztiB9xqri2SdVPz1MpMPav9SFt4Wwswq2ofB4dVDUUrGmp11C1KYxAKGCAdgwggHUMIG9AgEBMA0GCSqGSIb3DQEBCwUAMEExCzAJBgNVBAYTAlVTMRUwEwYDVQQKEwxUUjM0IFNhbXBsZXMxGzAZBgNVBAMTElRSMzQgU2FtcGxlIENBIEtESBcNMTAxMTAyMTczMzMwWhcNMTAxMjAyMTczMzMwWjBIMBYCBTQAAAAIFw0xMDExMDIxNzI4MTNaMBYCBTQAAAAKFw0xMDExMDIxNzMxNDZaMBYCBTQAAAALFw0xMDExMDIxNzMzMjVaMA0GCSqGSIb3DQEBCwUAA4IBAQA28Go8iKy8+p+OOwhQ7uCanPe1spGIblENNJ/l8Fwb232fOeTqZQiCjy7T7nljyFh0UnlcOUYrBg1blyyae174kKGTxXIl0ciNVu7R0D9+mnWHDWBNFeAS/9e4UG6kuhTntRAYwW2NnvtqxcUVaGsTThax1n8ei/Q8j8mmC5YUrczpr2nCepRT1CY0T2sURQ7lg4IUdB6r9/h/FSqTGfrZ7TtIGhE8AzY9Ka3QLjAxOik6/Bh7clKY+xTF2ZeWeFomzBnPB8K8miO49vbuGaAHkE9L9jLc/efygl8xL+Yo49DHHNULsI7kBOKl8BGgtatPKmEnXiUAna4AZg7/MtbD\0";
    private final String keyToken = "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\0";
    //    String unbindToken = "MIIElQYJKoZIhvcNAQcCoIIEhjCCBIICAQExDTALBglghkgBZQMEAgEwgYMGCSqGSIb3DQEHAaB2BHQwcjBpMR8wHQYDVQQDDBZTdW5taSBBcHAgU2lnbiBDQSAyMDIwMQswCQYDVQQGEwJDTjEUMBIGA1UECgwLU3VubWkgR3JvdXAxIzAhBgNVBAsMGlN1bm1pIEZpbmFuY2lhbCBUZWNobm9sb2d5AgUTmYAEHKGCAdgwggHUMIG9AgEBMA0GCSqGSIb3DQEBCwUAMEExCzAJBgNVBAYTAlVTMRUwEwYDVQQKEwxUUjM0IFNhbXBsZXMxGzAZBgNVBAMTElRSMzQgU2FtcGxlIENBIEtESBcNMTAxMTAyMTczMzMwWhcNMTAxMjAyMTczMzMwWjBIMBYCBTQAAAAIFw0xMDExMDIxNzI4MTNaMBYCBTQAAAAKFw0xMDExMDIxNzMxNDZaMBYCBTQAAAALFw0xMDExMDIxNzMzMjVaMA0GCSqGSIb3DQEBCwUAA4IBAQA28Go8iKy8+p+OOwhQ7uCanPe1spGIblENNJ/l8Fwb232fOeTqZQiCjy7T7nljyFh0UnlcOUYrBg1blyyae174kKGTxXIl0ciNVu7R0D9+mnWHDWBNFeAS/9e4UG6kuhTntRAYwW2NnvtqxcUVaGsTThax1n8ei/Q8j8mmC5YUrczpr2nCepRT1CY0T2sURQ7lg4IUdB6r9/h/FSqTGfrZ7TtIGhE8AzY9Ka3QLjAxOik6/Bh7clKY+xTF2ZeWeFomzBnPB8K8miO49vbuGaAHkE9L9jLc/efygl8xL+Yo49DHHNULsI7kBOKl8BGgtatPKmEnXiUAna4AZg7/MtbDMYICCjCCAgYCAQEwcjBpMR8wHQYDVQQDDBZTdW5taSBBcHAgU2lnbiBDQSAyMDIwMQswCQYDVQQGEwJDTjEUMBIGA1UECgwLU3VubWkgR3JvdXAxIzAhBgNVBAsMGlN1bm1pIEZpbmFuY2lhbCBUZWNobm9sb2d5AgUYwzRfyTALBglghkgBZQMEAgGgbTAYBgkqhkiG9w0BCQMxCwYJKoZIhvcNAQcBMCAGCiqGSIb3DQEJGQMxCgQQFn6w5yeB5JQBEiM0RVZneDAvBgkqhkiG9w0BCQQxIgQgNd8/OSFFdetX9vlsK8zi1HDnuktNuuWvGXUFagoXTNswDQYJKoZIhvcNAQEBBQAEggEAEHj6EWQ6QDnB7p9YTvv+Cs4e/QyJUC79a2f4ChI22NUHazqQFgFsYNZUC+oSmct/vgbaBDZ6Dp2PYapww0AEBMd4perhayZb1nvETu2BDlAvLaQE2vFUDR99RbY9A+OGaMbYAmWNEaulH3VvGChNGyLxTnmpeUndmnfiqZpkTDhv1xtnifhFAQDuKZ17lvzZzU2lVFdPdRFm/B4RIQruhdlL/mIeU8bcZAtYT8XB4A0VYcnzq0TfrbEmzrWbWhXQQ0p8SLFbPPJNCVPChCQIITNaoDPDv7lY+PggoXphO1NmvagKuIoPSX9PS5FWagivoQi2MS2WORjiUopj7uUMaw==";
    private final String keyToken1 = "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";
    private final String unbindToken = "MIIEMQYJKoZIhvcNAQcCoIIEIjCCBB4CAQExDTALBglghkgBZQMEAgEwgYMGCSqGSIb3DQEHAaB2BHQwcjBpMR8wHQYDVQQDDBZTdW5taSBBcHAgU2lnbiBDQSAyMDIwMQswCQYDVQQGEwJDTjEUMBIGA1UECgwLU3VubWkgR3JvdXAxIzAhBgNVBAsMGlN1bm1pIEZpbmFuY2lhbCBUZWNobm9sb2d5AgUTmYAEHKGCAa0wggGpMIGUAgEBMAsGCSqGSIb3DQEBCzAwMQswCQYDVQQGEwJDTjEhMB8GA1UEAwwYU3VubWkgRGV2aWNlIEF1dGggUlNBIENBFw0yMjExMDgxNjAwMDBaFw0yNzExMDcxNjAwMDBaMACgMDAuMAsGA1UdFAQEAgICNzAfBgNVHSMEGDAWgBSMszc7X1ie24tz9AJmKCUqpc8mijALBgkqhkiG9w0BAQsDggEBALP1eO1OF3mvxZ5HiiozffU39YD62H7No+3S5KpbVYS7KAD0JsZpliL0TPy4Rk7Iu0nDVIk2gkutipaEMqwsTGw+9eLo4LWI8/U2yc40oncS+J9US74r4bo/ftZ+e6XnooTbLSupJ3XX6OCSGj5lpEQrGTO+Ofko0gR7Xt/VkptCdiqm/6B5Bqc52ajygTkExiC+IeZsEPSb4ZaoODiBr7QEReI3rBGZjFOS4BbkFDk1W5tPYIVb0H5oOFbfcW/1VZz4OSdR5VsbsZ2V2iFN2prDB+BulqLMjMuUOmvsmnqCpSp42489LPpxRMxjVDr8aoPYUnB/0hhAdk0HRCXyhd0xggHRMIIBzQIBATA5MDAxCzAJBgNVBAYTAkNOMSEwHwYDVQQDDBhTdW5taSBEZXZpY2UgQXV0aCBSU0EgQ0ECBR/uHQYHMAsGCWCGSAFlAwQCAaBtMBgGCSqGSIb3DQEJAzELBgkqhkiG9w0BBwEwIAYKKoZIhvcNAQkZAzESBBD7LwoFPk1Dnu25NkcS+k51MC8GCSqGSIb3DQEJBDEiBCA13z85IUV161f2+WwrzOLUcOe6S0265a8ZdQVqChdM2zANBgkqhkiG9w0BAQEFAASCAQAtzTAGNfuo1Hd2SNaDI0z1LIUw3/qSNdeGz9NJ3qO/JsCmU9a7Cph/5hDUipVhqYAVCnZ8lM5zfxovKR4Kfg4zTrZ1iZnTZNkY6X0+GR+6sLzbZP7Hf5ZXYRvEPUkJb4g/P5TeOtRqJvB/z9l8cqD4QoZF69CH7esaPYQS6TuvlP+PxDv79EIC7l92jN+ru643wcUFmRjYZ4rb7KBguXWYOUBgUN9CUhyk+7wLZvGhdUL98ECJ+sUcu0IOxJlrQvxPnSlLkeiBsoYSvO7rFkJHPGYQLBYrg4OXFQOqhkjmrV3Z+8qk2nep5aOkXoSf9pcOWG1YrDpYE2Zh0Ith2lmU";

    private int keySystem = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_tr34_test);
        initView();
        initData();
    }

    private void initData() {
        this.<EditText>findViewById(R.id.edt_validate_input_key_token).setText(keyToken1);
        this.<EditText>findViewById(R.id.edt_validate_unbind_token).setText(unbindToken);
        this.<EditText>findViewById(R.id.edt_validate_cred_token).setText(kdhCredToken);
    }

    private void initView() {
        initToolbarBringBack(R.string.security_tr34_test);
        findViewById(R.id.btn_gen_tr34_cred_token).setOnClickListener(this);
        findViewById(R.id.btn_gen_tr34_random_token).setOnClickListener(this);
        findViewById(R.id.btn_validate_cred_token).setOnClickListener(this);
        findViewById(R.id.btn_validate_key_token).setOnClickListener(this);
        findViewById(R.id.btn_validate_unbind_token).setOnClickListener(this);
        this.<RadioGroup>findViewById(R.id.key_system).setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_key_system_type1) {
                keySystem = AidlConstants.Security.SEC_MKSK;
            } else {
                keySystem = AidlConstants.Security.SEC_DUKPT;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gen_tr34_cred_token:
                genTR34CredToken();
                break;
            case R.id.btn_gen_tr34_random_token:
                genTR34RandomToken();
                break;
            case R.id.btn_validate_cred_token:
                validateTR34CredToken();
                break;
            case R.id.btn_validate_key_token:
                validateTR34KeyToken();
                break;
            case R.id.btn_validate_unbind_token:
                validateTR34UNBToken();
                break;
        }
    }

    private void genTR34CredToken() {
        showToast(R.string.invoking_not_support);
//        try {
//            String indexStr = this.<EditText>findViewById(R.id.edt_gen_cert_index).getText().toString();
//            if (TextUtils.isEmpty(indexStr)) {
//                showToast("Cert index should not be empty");
//                return;
//            }
//            int certIndex = Integer.parseInt(indexStr);
//            if (certIndex < 9001 || certIndex > 9008) {
//                showToast("index should in [9001,9008]");
//                return;
//            }
//            Bundle bundle = new Bundle();
//            bundle.putInt("certIndex", certIndex);
//            byte[] outData = new byte[2048];
//            int len = MyApplication.app.securityOptV2.genTR34CredTokenWL(bundle, outData);
//            LogUtil.e(TAG, "genTR34CredToken code:" + len);
//            if (len > 0) {
//                outData = Arrays.copyOf(outData, len);
//                LogUtil.e(TAG, "genTR34CredToken Data:" + ByteUtil.bytes2HexStr(outData));
//                showToast("genTR34CredToken success");
//            } else {
//                showToast("genTR34CredToken failed,code = " + len);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void genTR34RandomToken() {
        showToast(R.string.invoking_not_support);
//        try {
//            byte[] outData = new byte[2048];
//            int len = MyApplication.app.securityOptV2.genTR34RandomTokenWL(32, outData);
//            LogUtil.e(TAG, "genTR34RandomToken code:" + len);
//            if (len > 0) {
//                outData = Arrays.copyOf(outData, len);
//                LogUtil.e(TAG, "genTR34RandomToken Data:" + ByteUtil.bytes2HexStr(outData));
//                showToast("genTR34RandomToken success");
//            } else {
//                showToast("genTR34RandomToken failed,code = " + len);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void validateTR34CredToken() {
        showToast(R.string.invoking_not_support);
//        try {
//            String inputData = this.<EditText>findViewById(R.id.edt_validate_cred_token).getText().toString();
//            if (TextUtils.isEmpty(inputData)) {
//                showToast("Cred token should not be empty");
//                return;
//            }
//            int code = MyApplication.app.securityOptV2.validateTR34CredTokenWL(inputData.getBytes());
//            LogUtil.e(TAG, "validateTR34CredToken code:" + code);
//            if (code == 0) {
//                showToast("validateTR34CredToken success");
//            } else {
//                showToast("validateTR34CredToken failed,code = " + code);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void validateTR34KeyToken() {
        showToast(R.string.invoking_not_support);
//        try {
//            String inputData = this.<EditText>findViewById(R.id.edt_validate_input_key_token).getText().toString();
//            if (TextUtils.isEmpty(inputData)) {
//                showToast("Key token should not be empty");
//                return;
//            }
//            String depSKIndexStr = this.<EditText>findViewById(R.id.edt_validate_depSKIndex).getText().toString();
//            if (TextUtils.isEmpty(depSKIndexStr)) {
//                showToast("depSKIndex should not be empty");
//                return;
//            }
//            String keyIndexStr = this.<EditText>findViewById(R.id.edt_validate_key_index).getText().toString();
//            if (TextUtils.isEmpty(keyIndexStr)) {
//                showToast("keyIndex should not be empty");
//                return;
//            }
//            int certIndex = Integer.parseInt(depSKIndexStr);
//            if (certIndex < 9001 || certIndex > 9008) {
//                showToast("depSKIndexStr should in [9001,9008]");
//                return;
//            }
//            int keyIndex = Integer.parseInt(keyIndexStr);
//            Bundle bundle = new Bundle();
//            bundle.putInt("depSKIndex", certIndex);
//            bundle.putInt("keyIndex", keyIndex);
//            bundle.putInt("keySystem", keySystem);
//            bundle.putByteArray("dataIn", inputData.getBytes());
//            int code = MyApplication.app.securityOptV2.validateTR34KeyTokenWL(bundle);
//            LogUtil.e(TAG, "validateTR34KeyToken code:" + code);
//            if (code == 0) {
//                showToast("validateTR34KeyToken success");
//            } else {
//                showToast("validateTR34KeyToken failed,code = " + code);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void validateTR34UNBToken() {
        showToast(R.string.invoking_not_support);
//        try {
//            String inputData = this.<EditText>findViewById(R.id.edt_validate_unbind_token).getText().toString();
//            if (TextUtils.isEmpty(inputData)) {
//                showToast("Unbind token should not be empty");
//                return;
//            }
//            String certIndexStr = this.<EditText>findViewById(R.id.edt_validate_unbind_cert_index).getText().toString();
//            if (TextUtils.isEmpty(certIndexStr)) {
//                showToast("Cert Index should not be empty");
//                return;
//            }
//            int certIndex = Integer.parseInt(certIndexStr);
//            if (certIndex < 9001 || certIndex > 9008) {
//                showToast("Cert Index should in [9001,9008]");
//                return;
//            }
//            Bundle bundle = new Bundle();
//            bundle.putInt("certIndex", certIndex);
//            bundle.putByteArray("dataIn", inputData.getBytes());
//            int code = MyApplication.app.securityOptV2.validateTR34UNBTokenWL(bundle);
//            LogUtil.e(TAG, "validateTR34UNBToken code:" + code);
//            if (code == 0) {
//                showToast("validateTR34UNBToken success");
//            } else {
//                showToast("validateTR34UNBToken failed,code = " + code);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

}
