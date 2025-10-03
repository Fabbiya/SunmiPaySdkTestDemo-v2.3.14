package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;

/**
 * This page show how to send extended APDU with interface apduCommand() or
 * smartCardExchange() or transmitApd()
 */
public class ExtendedApduActivity extends BaseAppCompatActivity {
    private TextView tvCheckCard;
    private TextView tvCardExistStatus;
    private TextView tvPSAMApdu;
    private int cardType = CardType.PSAM0.getValue();
    private int optType = -1;// 0-croatia, 1-serbia，2-IRCC_4231

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_extended_apdu);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_extended_apdu);
        RadioGroup group = findViewById(R.id.rdo_group_card_type);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            switch (checkedId) {
                case R.id.rdo_sam0:
                    cardType = CardType.PSAM0.getValue();
                    break;
                case R.id.rdo_sam1:
                    cardType = CardType.SAM1.getValue();
                    break;
                case R.id.rdo_sam2:
                    cardType = CardType.SAM2.getValue();
                    break;
                case R.id.rdo_sam3:
                    cardType = CardType.SAM3.getValue();
                    break;
            }
        });
        group.check(R.id.rdo_sam0);
        findViewById(R.id.mb_card_exist_status).setOnClickListener((v) -> testCardExistStatus());
        findViewById(R.id.mb_croatia).setOnClickListener((v) -> {
            optType = 0;
            checkCard();
        });
        findViewById(R.id.mb_serbia).setOnClickListener((v) -> {
            optType = 1;
            checkCard();
        });
        findViewById(R.id.mb_ircc_4231).setOnClickListener((v) -> {
            optType = 2;
            checkCard();
        });
        tvCheckCard = findViewById(R.id.tv_check_card);
        tvCardExistStatus = findViewById(R.id.tv_exist_status);
        tvPSAMApdu = findViewById(R.id.tv_apdu_result);
    }

    private void checkCard() {
        try {
            MyApplication.app.readCardOptV2.checkCardEx(cardType, 0x28, 0, checkCardCallbackV2, 5);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CheckCardCallbackV2 checkCardCallbackV2 = new CheckCardCallbackV2.Stub() {
        @Override
        public void findMagCard(Bundle info) throws RemoteException {
            Log.e(TAG, "findMagCard..");
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            Log.e(TAG, "findICCard, atr:" + atr);
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            Log.e(TAG, "findRFCard, uuid:" + uuid);
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            Log.e(TAG, "onError, code:" + code + ",message:" + message);
        }

        @Override
        public void findICCardEx(Bundle info) throws RemoteException {
            String atr = info.getString("atr");
            String msg = "findICCardEx, atr:" + atr;
            Log.e(TAG, msg);
            showCheckCardResult(msg);
            if (optType == 0) {
                testCroatiaPSAM();
            } else if (optType == 1) {
                testSerbiaPSAM();
            } else if (optType == 2) {
                testIRCC4231Param();
            }
        }

        @Override
        public void findRFCardEx(Bundle info) throws RemoteException {
            String uuid = info.getString("uuid");
            String ats = info.getString("ats");
            String msg = "findRFCardEx, uuid:" + uuid + ", ats:" + ats;
            Log.e(TAG, msg);
            showCheckCardResult(msg);
        }

        @Override
        public void onErrorEx(Bundle info) throws RemoteException {
            int code = info.getInt("code");
            String message = info.getString("message");
            String msg = "onErrorEx, code:" + code + ", msg:" + message;
            Log.e(TAG, msg);
            showCheckCardResult(msg);
        }
    };

    /** Test card exist status */
    private void testCardExistStatus() {
        try {
            int value = MyApplication.app.readCardOptV2.getCardExistStatus(cardType);
            if (value == AidlConstants.CardExistStatus.CARD_PRESENT) {
                String msg = "card present..";
                Log.e(TAG, msg);
                showCardExistStatus(msg);
            } else if (value == AidlConstants.CardExistStatus.CARD_ABSENT) {
                String msg = "card absent..";
                Log.e(TAG, msg);
                showCardExistStatus(msg);
            } else {
                String msg = "getCardExistStatus() error, code: " + value;
                Log.e(TAG, msg);
                showCardExistStatus(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //克罗地亚PSAM卡测试
    private void testCroatiaPSAM() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("start Croatia PSAM test...");
            //step 1, send:00A4040010A000000748464A492D546178436F726500 recv:9000
            byte[] send = ByteUtil.hexStr2Bytes("00A4040010A000000748464A492D546178436F726500");
            byte[] recv = new byte[2048];
            sb.append("\nsend: 00A4040010A000000748464A492D546178436F726500");
            int len = apduExchange(cardType, send, recv);
            if (len < 0) {
                sb.append("\nError:").append(len);
                return;
            }
            byte[] valid = Arrays.copyOf(recv, len);
            sb.append("\nrecv: ").append(ByteUtil.bytes2HexStr(valid));

            //step 2, send:881104000406040006 recv:9000
            send = ByteUtil.hexStr2Bytes("881104000406040006");
            sb.append("\nsend: 881104000406040006");
            len = apduExchange(cardType, send, recv);
            if (len < 0) {
                sb.append("\nError:").append(len);
                return;
            }
            valid = Arrays.copyOf(recv, len);
            sb.append("\nrecv: ").append(ByteUtil.bytes2HexStr(valid));

            //step 3, send:88040400000000 recv:1400以上字节+ 9000
            send = ByteUtil.hexStr2Bytes("8804040000");
            sb.append("\nsend: 8804040000");
            len = apduExchange(cardType, send, recv);
            if (len < 0) {
                sb.append("\nError:").append(len);
                return;
            }
            valid = Arrays.copyOf(recv, len);
            sb.append("\nrecv: ").append(ByteUtil.bytes2HexStr(valid));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            showAPDUResult(sb);
        }
    }

    //塞尔维亚PSAM卡测试
    private void testSerbiaPSAM() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("start Serbia PSAM test...");
            //step 1, send:00A4040010A000000748464A492D546178436F726500
            byte[] recv = new byte[2048];
            byte[] send = ByteUtil.hexStr2Bytes("00A4040010A000000748464A492D546178436F726500");
            sb.append("\nsend: 00A4040010A000000748464A492D546178436F726500");
            int len = apduExchange(cardType, send, recv);
            if (len < 0) {
                sb.append("\nError:").append(len);
                return;
            }
            byte[] valid = Arrays.copyOf(recv, len);
            sb.append("\nrecv: ").append(ByteUtil.bytes2HexStr(valid));

            //step 2, send:881100000400010304 recv:9000
            send = ByteUtil.hexStr2Bytes("881100000400010304");
            sb.append("\nsend: 881100000400010304");
            len = apduExchange(cardType, send, recv);
            if (len < 0) {
                sb.append("\nError:").append(len);
                return;
            }
            valid = Arrays.copyOf(recv, len);
            sb.append("\nrecv: ").append(ByteUtil.bytes2HexStr(valid));

            //step 3, send:881300000000420000017CC744EAB5000000000000000000000031303136343734363400000000000000000000000000000000000000000000000000000F42400101000000000F42400000
            send = ByteUtil.hexStr2Bytes("881300000000420000017CC744EAB5000000000000000000000031303136343734363400000000000000000000000000000000000000000000000000000F42400101000000000F42400000");
            sb.append("\nsend: 881300000000420000017CC744EAB5000000000000000000000031303136343734363400000000000000000000000000000000000000000000000000000F42400101000000000F42400000");
            len = apduExchange(cardType, send, recv);
            if (len < 0) {
                sb.append("\nError:").append(len);
                return;
            }
            valid = Arrays.copyOf(recv, len);
            sb.append("\nrecv: ").append(ByteUtil.bytes2HexStr(valid));

            //step 4, send:00A4040010A000000748464A492D546178436F726500
            send = ByteUtil.hexStr2Bytes("00A4040010A000000748464A492D546178436F726500");
            sb.append("\nsend: 00A4040010A000000748464A492D546178436F726500");
            len = apduExchange(cardType, send, recv);
            if (len < 0) {
                sb.append("\nError:").append(len);
                return;
            }
            valid = Arrays.copyOf(recv, len);
            sb.append("\nrecv: ").append(ByteUtil.bytes2HexStr(valid));

            //step 5, send:88040000000000
            send = ByteUtil.hexStr2Bytes("8804000000");
            sb.append("\nsend: 8804000000");
            len = apduExchange(cardType, send, recv);
            if (len < 0) {
                sb.append("\nError:").append(len);
                return;
            }
            valid = Arrays.copyOf(recv, len);
            sb.append("\nrecv: ").append(ByteUtil.bytes2HexStr(valid));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            showAPDUResult(sb);
        }
    }

    //TB问题IRCC-4231 PSAM卡测试
    private void testIRCC4231Param() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("start IRCC-4231 PSAM test...");
            //step 1-- Select TaxCore Applet on card
            //send:00A4040010A000000748464A492D546178436F7265 recv:9000
            byte[] recv = new byte[2048];
            byte[] send = ByteUtil.hexStr2Bytes("00A4040010A000000748464A492D546178436F7265");
            sb.append("\nsend: 00A4040010A000000748464A492D546178436F7265");
            int len = apduExchange(cardType, send, recv);
            if (len < 0) {
                sb.append("\nError:").append(len);
                return;
            }
            byte[] valid = Arrays.copyOf(recv, len);
            sb.append("\nrecv: ").append(ByteUtil.bytes2HexStr(valid));

            //step 2-- Get version
            //send:8809040000 recv:0000000100000001000000009000
            send = ByteUtil.hexStr2Bytes("8809040000");
            sb.append("\nsend: 8809040000");
            len = apduExchange(cardType, send, recv);
            if (len < 0) {
                sb.append("\nError:").append(len);
                return;
            }
            valid = Arrays.copyOf(recv, len);
            sb.append("\nrecv: ").append(ByteUtil.bytes2HexStr(valid));

            //step 3-- Export public key
            //send:88070400000000 recv:D3562AE5A802D4DBAF0CB1A8DB11A940515C336D3FA0C85597437901A9E3BC47CA0A17DA88E6E6051D36714B8B92B1E232756F093DBF009EB20BEC3527C7EC9778A8110CED304B81DA40A9BAAA1CC6238E4B0A56DB63BFD77F2E2ADF76B3334529483AF61B1036F1D14C3DEA72538CA3F922AF29BB2C299CDACA8100D33B80D4DD6E66613E6C14CA5160C23AAB30043219D303ACD56B331CF771CA264AD0706A566E3B62624ED638736960DF80B16040D4DC91C9192355A44B6D8CE65262E1B264B4FE149682AC7B65BD802FF75B02CEA77F75C26A3249218057F1CF0835A1365D452737D7CE4A9BF0B87C34CCF62C4718A9F6F143326807D73EB5B3D572EF150100019000
            send = ByteUtil.hexStr2Bytes("88070400000000");
            sb.append("\nsend: 88070400000000");
            len = apduExchange(cardType, send, recv);
            if (len < 0) {
                sb.append("\nError:").append(len);
                return;
            }
            valid = Arrays.copyOf(recv, len);
            sb.append("\nrecv: ").append(ByteUtil.bytes2HexStr(valid));

            //step 4-- Export certificate
            //send:88040400000000 recv:308205B23082049AA003020102020E415CE881DDD6E8AD0000000025EE300D06092A864886F70D01010D05003049310B300906035504061302525331173015060355040A130E506F7265736B61205570726176613121301F0603550403131853616E64626F78205355462049737375696E672043412031301E170D3232303432303038343934355A170D3235303432303038353934355A3081A5310B30090603550406130252533117301506035504080C0ED091D0B5D0BED0B3D180D0B0D0B43110300E0603550407130742656F677261643117301506035504090C0E557374616E69C48D6B612031393631123010060355040A13095465726F6E20646F6F31123010060355040B13095465726F6E20646F6F3111300F060355040513083441395341453843311730150603550403130E34413953205465726F6E20646F6F30820122300D06092A864886F70D01010105000382010F003082010A0282010100AEEEDDDB730F98A46D5A060BB9F7DB642E87E5F23A9CC69117C4CBF24786310FC519805A16C2C7E16636FF4126ECAF48EF3FCB4C86B4CB75DA5C2D334F23C4A4BED109DCF2D5ACE3DF94B055D5E6EDDCA774A35E61CBE41D65962476B7B3C443CB96211069E8D21E8A22597C67EA2C0CAA71E88121AD2A96466AA40AC8FA522C9441E1BED6FC97B14082A69D69B3E79EC26EE06514406E1B8406A01A547E6CA8EB4EEEA0E77C3834CBE58F700A77E11B1AE9B7D4BCD4B68A8CE8A95C70F216F5EF1D3DB0F2E1873D564064AD19F478DA32FD639164B8CBD4CD69112589EC4E4E847D1BBAC158B2EF7F2D33BB0EA3DE954A4343546F2B467394DBE7A9152B02350203010001A382023930820235300F0603551D130101FF04053003020100300E0603551D0F0101FF04040302049030170603551D250410300E060C2B06010401838620050803033081C50603551D200481BD3081BA3081B7060C2B06010401838620050804023081A6303206082B060105050702011626687474703A2F2F706B692E73616E64626F782E7375662E707572732E676F762E72732F706B69307006082B0601050507020230641E6200730061006E00640062006F007800550073006500200070004B00490053007200620069006A006100530061006E00640062006F0078002000690073007300750061006E006300650050006F006C00690063007900200063006C00610073007300323032060B2B06010401838620050805042368747470733A2F2F6170692E73616E64626F782E7375662E707572732E676F762E7273301A060B2B06010401838620050806040B5253313131373538313935301D0603551D0E041604140FB7C97D3333772E512EAD6FBE62B03F19D2C22E301F0603551D23041830168014332482ABBB7BF155698118B8DE971B0BDFE4A1E1304A0603551D1F04433041303FA03DA03B8639687474703A2F2F706B692E73616E64626F782E7375662E707572732E676F762E72732F706B692F5355464943413153616E64626F782E63726C305506082B0601050507010104493047304506082B060105050730028639687474703A2F2F706B692E73616E64626F782E7375662E707572732E676F762E72732F706B692F5355464943413153616E64626F782E636572300D06092A864886F70D01010D0500038201010062F73B6C34D3F9E34F22123E0BCA51E82DE376E927BBFB333B6C5B1E433CC90495C649D287B217E3E7C5AA3B23F8E77B44DE020F641BAF16937469688A31A4FED4451704863A7BD5BC20CADDD09E088A4EBA8757D9A2221B6B8FF98F14F6709AE935430952EB98F7E153C1CB52EABEF87BB57062CD1E96879000
            send = ByteUtil.hexStr2Bytes("88040400000000");
            sb.append("\nsend: 88040400000000");
            len = apduExchange(cardType, send, recv);
            if (len < 0) {
                sb.append("\nError:").append(len);
                return;
            }
            valid = Arrays.copyOf(recv, len);
            sb.append("\nrecv: ").append(ByteUtil.bytes2HexStr(valid));

            //step 5-- Get amount status on card
            //send:8814040000 recv:000000000000000000174876E8009000
            send = ByteUtil.hexStr2Bytes("8814040000");
            sb.append("\nsend: 8814040000");
            len = apduExchange(cardType, send, recv);
            if (len < 0) {
                sb.append("\nError:").append(len);
                return;
            }
            valid = Arrays.copyOf(recv, len);
            sb.append("\nrecv: ").append(ByteUtil.bytes2HexStr(valid));

            //step 6-- Authenticate with PIN code
            //send:881104000406060405 recv:9000
            send = ByteUtil.hexStr2Bytes("881104000406060405");
            sb.append("\nsend: 881104000406060405");
            len = apduExchange(cardType, send, recv);
            if (len < 0) {
                sb.append("\nError:").append(len);
                return;
            }
            valid = Arrays.copyOf(recv, len);
            sb.append("\nrecv: ").append(ByteUtil.bytes2HexStr(valid));

            //step 7-- Start audit
            //send:88210400000000 recv:01000000B900077920B25C5851DA1979613BAD8B0BEA226CE60DD9529218908A4206BE356A4BEC4D786F5F81C1E0EC534EB191C852361C39CE9EA125441AD815A60C3F7D210365459D70A0BF73D134B96A81292D8617AE7F774F9B404D1D5B31C2B96CDCE740DA9B6162826E6C2E8BDD7DD3BBBD453F9437F5B8C2612510246957A3A70C6193D08F2C8D258ACB503503E9A0E875A441B28809F71C6F1FF889265D0479A4B15CDF04F4960FA987A11D68DE737D92698E1E1786631289BBD80CE297212398023E3DB01B9E804C7C828F10A2A272F731D630D55FFC4654CFEE909DF95290C79ABBCC2557F6EBBEAA349B2F50D8A846D38BB2C429E1607478FB551CCB09ABFB9000
            send = ByteUtil.hexStr2Bytes("88210400000000");
            sb.append("\nsend: 88210400000000");
            len = apduExchange(cardType, send, recv);
            if (len < 0) {
                sb.append("\nError:").append(len);
                return;
            }
            valid = Arrays.copyOf(recv, len);
            sb.append("\nrecv: ").append(ByteUtil.bytes2HexStr(valid));

            //step 8-- End audit
            //send:882004000001002366C8C3C1B284AD81BFA2D3A191F74100F0EE4607FCF092A5756C1D1B9C927CB46966A9DAE58FCF206D842CB8E3CCF569FB589D57F72D00EC05095787679AA0EBF3822221D47016FA5BFE0C6943180CC128567B942D86A07920FAD916F3483A30D1A6B72B68A35865377C01C7E3FC39DB5EC60D6C08187642C43BE499E593EA243CC846A8B5049B4646B6EC63C6E52DB64588EE8A4D9BD560918A37FE4E4105A5908B8FD3BD549FC926FDE5D63FA075587CEECC9CF6586D9AEAA6A007324ABC4E11FB0998352D8036B82CC21529515CEE5B68221EAF82386CC14C11C380D7B817F092C68331C45C6DAC1C825D4AF9C680A46BF0636E5C1B8BB04D8636CC943D recv:9000
            send = ByteUtil.hexStr2Bytes("882004000001002366C8C3C1B284AD81BFA2D3A191F74100F0EE4607FCF092A5756C1D1B9C927CB46966A9DAE58FCF206D842CB8E3CCF569FB589D57F72D00EC05095787679AA0EBF3822221D47016FA5BFE0C6943180CC128567B942D86A07920FAD916F3483A30D1A6B72B68A35865377C01C7E3FC39DB5EC60D6C08187642C43BE499E593EA243CC846A8B5049B4646B6EC63C6E52DB64588EE8A4D9BD560918A37FE4E4105A5908B8FD3BD549FC926FDE5D63FA075587CEECC9CF6586D9AEAA6A007324ABC4E11FB0998352D8036B82CC21529515CEE5B68221EAF82386CC14C11C380D7B817F092C68331C45C6DAC1C825D4AF9C680A46BF0636E5C1B8BB04D8636CC943D");
            sb.append("\nsend: 882004000001002366C8C3C1B284AD81BFA2D3A191F74100F0EE4607FCF092A5756C1D1B9C927CB46966A9DAE58FCF206D842CB8E3CCF569FB589D57F72D00EC05095787679AA0EBF3822221D47016FA5BFE0C6943180CC128567B942D86A07920FAD916F3483A30D1A6B72B68A35865377C01C7E3FC39DB5EC60D6C08187642C43BE499E593EA243CC846A8B5049B4646B6EC63C6E52DB64588EE8A4D9BD560918A37FE4E4105A5908B8FD3BD549FC926FDE5D63FA075587CEECC9CF6586D9AEAA6A007324ABC4E11FB0998352D8036B82CC21529515CEE5B68221EAF82386CC14C11C380D7B817F092C68331C45C6DAC1C825D4AF9C680A46BF0636E5C1B8BB04D8636CC943D");
            len = apduExchange(cardType, send, recv);
            if (len < 0) {
                sb.append("\nError:").append(len);
                return;
            }
            valid = Arrays.copyOf(recv, len);
            sb.append("\nrecv: ").append(ByteUtil.bytes2HexStr(valid));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            showAPDUResult(sb);
        }
    }

    /** We can test extended APDU with  apduCommand() or smartCardExchange() or transmitApdu() in this method */
    private int apduExchange(int cardType, byte[] sendBuff, byte[] recvBuff) throws Exception {
        return transmitApdu(cardType, sendBuff, recvBuff);
    }

    private int transmitApdu(int cardType, byte[] send, byte[] recv) throws RemoteException {
        return MyApplication.app.readCardOptV2.transmitApduEx(cardType, send, recv);
    }

    private void showCheckCardResult(String msg) {
        runOnUiThread(() -> tvCheckCard.setText(msg));
    }

    private void showCardExistStatus(String msg) {
        runOnUiThread(() -> tvCardExistStatus.setText(msg));
    }

    private void showAPDUResult(CharSequence msg) {
        runOnUiThread(() -> tvPSAMApdu.setText(msg));
    }

    @Override
    protected void onDestroy() {
        cancelCheckCard();
        super.onDestroy();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.app.readCardOptV2.cardOff(cardType);
            MyApplication.app.readCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
