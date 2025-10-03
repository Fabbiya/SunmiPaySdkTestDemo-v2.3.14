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

public class HsmSaveKeyUnderKEKActivity extends BaseAppCompatActivity {
    private int keyType = Security.KEY_TYPE_KEK;
    private int keyAlgType = Security.KEY_ALG_TYPE_3DES;
    private int paddingMode = Security.NOTHING_PADDING;
    private int keySystem = Security.SEC_MKSK;
    private int encryptKeySystem = Security.SEC_MKSK;
    private int encryptionMode = Security.DATA_MODE_ECB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hsm_save_key_under_kek);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.hsm_save_key_under_kek);
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
        group = findViewById(R.id.rg_padding_mode);
        group.setOnCheckedChangeListener((g, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_mode_none:
                    paddingMode = Security.NOTHING_PADDING;
                    break;
                case R.id.rb_mode_pkcs1:
                    paddingMode = Security.PKCS1_PADDING;
                    break;
                case R.id.rb_mode_pkcs7:
                    paddingMode = Security.PKCS7_PADDING;
                    break;
                case R.id.rb_mode_pkcs5:
                    paddingMode = Security.PKCS5_PADDING;
                    break;
                case R.id.rb_mode_pkcs1_oaep:
                    paddingMode = Security.PKCS1_OAEP_PADDING;
                    break;
            }
        });
        group = findViewById(R.id.rg_key_system);
        group.setOnCheckedChangeListener((g, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_mksk:
                    keySystem = Security.SEC_MKSK;
                    break;
                case R.id.rb_rsa:
                    keySystem = Security.SEC_RSA_KEY;
                    break;
            }
        });
        group = findViewById(R.id.rg_enc_key_system);
        group.setOnCheckedChangeListener((g, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_enc_mksk:
                    encryptKeySystem = Security.SEC_MKSK;
                    break;
                case R.id.rb_enc_rsa:
                    encryptKeySystem = Security.SEC_RSA_KEY;
                    break;
            }
        });
        group = findViewById(R.id.rg_enc_mode);
        group.setOnCheckedChangeListener((g, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_enc_mode_ecb:
                            encryptionMode = Security.DATA_MODE_ECB;
                            break;
                        case R.id.rb_enc_mode_cbc:
                            encryptionMode = Security.DATA_MODE_CBC;
                            break;
                        case R.id.rb_enc_mode_ofb:
                            encryptionMode = Security.DATA_MODE_OFB;
                            break;
                        case R.id.rb_enc_mode_cfb:
                            encryptionMode = Security.DATA_MODE_CFB;
                            break;
                    }
                }
        );
        findViewById(R.id.btn_ok).setOnClickListener((v) -> saveKeyUnderKEK());
        String keyValueData = "2520A21C6F9EB0C20B350F39212B9A61B21BE02EFB26D605525F89E8EEBB074500B41FDC9248FFFB2BD1B78D5A2E4F4918650145A0D6F54F3E5D81C1C9FFEF1A602209099DC95449739B552DE44BEA77935AA683DB22FA3BB1C6C8F672D530DAA5C92FE183FA15E25B7E8F9592CF1EB1E1C88C2DC4F7E1627B615D32DF629FEBD1C1B854684E3C551A5943CAAEFC5B59CAEBB2C0B450DD2C236EF71D3BEEBF308D6C605B4B426119746EBB58FD8706F26847399E30788D007B1011016E10098EF607CC61474721E18A508B77CACBFF3084D0F987B98C244EDC760B53FEF48E70227FFB03A88B93B400DCB02420DA01663BF6A23BC0908021ADB97C74E986EA3EA87873003EEF0C7C55BF71ED1815185F2C0B151F6E3AAB2E63BDBA039B733FFCB3C01431D8A4E9F17BD311E4C0105C90805551FE14AB4311F615FCD1D5D6828FF576557AC261124BF7EAD6768CA2B78589A91F525548E710DDCC91488586E0787207A0F99459EFD353D891DA75B8F9DBD4F72292107752092425626D927200109C519B86C4E6B85832FDCB9F5E1A4C3B89A8CC64FE44F3942EEF3FDB9F75DA784FAF5EB8CCFA8604675E3C74799B1C525FF403A9063462F8B3727EDF2B055E2E1A7D7B95FC4A619072B4E1A7D183D65C3C895DFD38239AE451114A3F9D8C259DF5092803611DD7AED61E0748A92E4A31B2909DA7C41ED0948EC853FE2B95B17FEAB2DFF89F96C0422726D4213AB71072FDA52CB605A15697A743EE43F7E347F292E1C01FB29ED768668DCD7E3366B4235CF88853781CF4F8ECE4524A1053F5D9A17D9D21184F2334527CE8F5C9069E3A726471181364ECCD454404F6B4E7A90FC6276AB214913351FCAA104B344811568DD86EE20F7131D851A46381AC0412AE68B0D58D5F56B067127423742487B2ABC33E2FC4B43841203243AD84F7B2082E74268519AE13687DB8C1B30FD41C6395B00E472EE00AB9241A7B828822F8168390FFBA5F672CEA1B0693607DADE5F2307CD28DC48371920357ACD6F9A9F7D50EAEFE9D33BAF5A6A0143E96A3542FB984EB59559E8699D62A0C523C1BACF7DDDA8E55DEB8CC0BCD4D3AA85033388FB695B6934AD4FE6104DA7E6EF363F9E1F17F864337C8CFBD3B0FA13F066050758608430D3DC7DA25E74070601E91E431ED999849A4334DA53CDEBD28A9CC6D82F824BA65D991B07A6E8DC3078285D650BD7121EB58889FA29A3929912BA3E0E6688E316C1ABC2E830505E25505E63EE50DEA447640CCE5D1337CD389AE33F6FF796EC3F8417BAF8AD8668807A3C4FA521A2A5646E05BC7799DDF4B1B2E7981992B9EDC5E342F222CCF6ED6D1F9870A8C1F72C2B31F20B8652B70B55663C191031E92C46D0A49D6E53AF2307959E9AF29973AB036F13B466BA1E212F00FAAD66C1DE2FEA02DE2D82C38EF721081F4683C0FB65FABD3501209BC92DF00DE848AC458B67B69E399BAFE35EA9DA291B5D59486C74259E5B7BC542A7E71E2A1226E4D8BFEEAC12D655B6CD74A6AAE95DECEC2563AE76BE112DCE8F16DA629CF67F699EFFC25216FDD7C8E67D119C0DC61FA5B22D1B91A5EB600EA50BF96C5A34918153A04D30E7A8BF124BB5E9149342D561F402DE7ACA017EF6077C40808080808080808";
        EditText edtKeyValue = findViewById(R.id.edt_key_value);
        edtKeyValue.setText(keyValueData);
    }

    /** save key under KEK */
    private void saveKeyUnderKEK() {
        try {
            EditText edtKeyValue = findViewById(R.id.edt_key_value);
            EditText edtKeyIndex = findViewById(R.id.edt_key_index);
            EditText edtEncryptIndex = findViewById(R.id.edt_encrypt_index);
            String keyValueStr = edtKeyValue.getText().toString();
            String keyIndexStr = edtKeyIndex.getText().toString();
            String encryptKeyIndexStr = edtEncryptIndex.getText().toString();
            if (TextUtils.isEmpty(keyValueStr) || keyValueStr.length() % 8 != 0 || !Utility.checkHexValue(keyValueStr)) {
                showToast(R.string.security_key_value_hint);
                edtKeyValue.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast(R.string.security_mksk_key_index_hint);
                edtKeyIndex.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(encryptKeyIndexStr)) {
                showToast(R.string.security_mksk_key_index_hint);
                edtEncryptIndex.requestFocus();
                return;
            }
            byte[] keyValue = ByteUtil.hexStr2Bytes(keyValueStr);
            int keyIndex = Integer.parseInt(keyIndexStr);
            int encryptIndex = Integer.parseInt(encryptKeyIndexStr);

            Bundle bundle = new Bundle();
            bundle.putInt("keyIndex", keyIndex);
            bundle.putInt("paddingMode", paddingMode);
            bundle.putByteArray("keyValue", keyValue);
            bundle.putInt("keyType", keyType);
            bundle.putInt("keyAlgType", keyAlgType);
            bundle.putInt("keySystem", keySystem);
            bundle.putInt("encryptKeySystem", encryptKeySystem);
            bundle.putInt("encryptIndex", encryptIndex);
            bundle.putInt("encryptionMode", encryptionMode);
            int code = MyApplication.app.securityOptV2.hsmSaveKeyUnderKEKEx(bundle);
            String msg = null;
            if (code == 0) {
                msg = "hsmSaveKeyUnderKEKEx success";
            } else {
                msg = "hsmSaveKeyUnderKEKEx failed,code:" + code;
            }
            LogUtil.e(TAG, msg);
            showToast(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
