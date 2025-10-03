package com.sm.sdk.demo.card;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.DeviceUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * This page show how to transmit apdu to RFC card.
 * <br/> Both apdus which comply with or not comply with ISO-7816
 * can be transmitted, the ISO-7816 standard apdu format is:
 * <br/>commad(4B) + Lc(1B,value is len) + indata(len B) + Le(1B)
 */
public class TransmitApduActivity extends BaseAppCompatActivity {
    private EditText apdu;
    private EditText edtActiveCtr;
    private EditText edtApduCtr;
    private Button checkCard;
    private Button sendApdu;
    private TextView result;
    private int cardType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmit_apdu_layout);
        initView();
        initData();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_transmit_apdu);
        apdu = findViewById(R.id.apdu);
        edtActiveCtr = findViewById(R.id.active_ctr_code);
        edtApduCtr = findViewById(R.id.apdu_ctr_code);
        checkCard = findViewById(R.id.check_card);
        sendApdu = findViewById(R.id.send_apdu);
        result = findViewById(R.id.result);
        checkCard.setOnClickListener(this);
        sendApdu.setOnClickListener(this);
    }

    private void initData() {
        apdu.setText("00A404000E315041592E5359532E444446303100");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_card:
                handleClearData();
                checkCard();
                break;
            case R.id.send_apdu:
                if (checkInputData()) {
                    transmitApdu();
                }
                break;
        }
    }

    /** 清除结果 */
    private void handleClearData() {
        result.setText("");
    }

    private final CheckCardCallbackV2 mReadCardCallback = new CheckCardCallbackV2Wrapper() {
        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(TAG, "findMagCard,bundle:" + bundle);
            showSpendTime();
        }

        @Override
        public void findICCardEx(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            String atr = info.getString("atr");
            LogUtil.e(TAG, "findICCardEx, atr:" + atr);
            handleCheckCardSuccess("findICCardEx, atr:" + atr);
            cardType = info.getInt("cardType");
            showSpendTime();
        }

        @Override
        public void findRFCardEx(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            cardType = info.getInt("cardType");
            String uuid = info.getString("uuid");
            String ats = info.getString("ats");
            LogUtil.e(TAG, "findRFCardEx,cardType:" + cardType + ", uuid:" + uuid);
            StringBuilder sb = new StringBuilder();
            //ISO15693返回多条UID，多条UID以|分割
            if (cardType == CardType.ISO15693.getValue()) {
                String[] uids = uuid.split("\\|");
                for (int i = 0; i < uids.length; i++) {
                    sb.append("uid");
                    sb.append(i);
                    sb.append(":");
                    sb.append(uids[i]);
                    sb.append("\n");
                }
                if (sb.length() > 0) {
                    sb.deleteCharAt(sb.length() - 1);
                }
            } else {
                sb.append("cardType:");
                sb.append(cardType);
                sb.append("\nuuid:");
                sb.append(uuid);
                sb.append("\nats:");
                sb.append(ats);
            }
            handleCheckCardSuccess("findRFCardEx, " + sb);

            showSpendTime();
            //If want to transmit apdu to Mifare or Felica card,
            //change cardType to corresponding value, eg:
            //cardType = AidlConstantsV2.CardType.MIFARE.getValue()
        }

        @Override
        public void onErrorEx(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            int code = info.getInt("code");
            String msg = info.getString("message");
            LogUtil.e(TAG, "check card error,code:" + code + "message:" + msg);
            handleCheckCardFailed(code, msg);
            showSpendTime();
        }
    };

    /** 刷卡 */
    private void checkCard() {
        try {
            String ctrStr = edtActiveCtr.getText().toString();
            if (TextUtils.isEmpty(ctrStr)) {
                showToast("卡片激活控制参数不能为空");
                edtActiveCtr.requestFocus();
                return;
            }
            int activeCtr = Integer.parseInt(ctrStr, 16);
            //支持M1卡
            int allType = CardType.IC.getValue() | CardType.PSAM0.getValue() | CardType.SAM1.getValue()
                    | CardType.NFC.getValue() | CardType.MIFARE.getValue() | CardType.FELICA.getValue()
                    | CardType.ISO15693.getValue() | CardType.INNOVATRON.getValue();
            if (DeviceUtil.isTossTerminal()) {//TOSS terminal无NFC功能
                allType &= ~CardType.NFC.getValue();
                allType &= ~CardType.MIFARE.getValue();
                allType &= ~CardType.FELICA.getValue();
                allType &= ~CardType.ISO15693.getValue();
                allType &= ~CardType.INNOVATRON.getValue();
            }
            addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCardEx(allType, activeCtr, 0, mReadCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCheckCardSuccess(String msg) {
        runOnUiThread(() -> {
            StringBuilder sb = new StringBuilder("----------------------- check card success -----------------------\n");
            sb.append(msg);
            sb.append("\n");
            result.setText(sb);
            sendApdu.setEnabled(true);
        });
    }

    private void handleCheckCardFailed(int code, final String msg) {
        addText("check card error,code:" + code + ", message:" + msg + "\n");
    }

    private boolean checkInputData() {
        if (cardType != CardType.NFC.getValue()
                && cardType != CardType.MIFARE.getValue()
                && cardType != CardType.FELICA.getValue()
                && cardType != CardType.ISO15693.getValue()
                && cardType != CardType.IC.getValue()
                && cardType != CardType.PSAM0.getValue()
                && cardType != CardType.SAM1.getValue()
                && cardType != CardType.INNOVATRON.getValue()) {
            showToast("transmit apdu not support card type:" + cardType);
            return false;
        }
        String apduStr = apdu.getText().toString();
        if (TextUtils.isEmpty(apduStr)) {
            apdu.requestFocus();
            showToast("apdu should not be empty!");
            return false;
        }
        if (!Pattern.matches("[0-9a-fA-F]+", apduStr)) {
            apdu.requestFocus();
            showToast("apdu should hex characters!");
            return false;
        }
        String apduCtrStr = edtApduCtr.getText().toString();
        if (TextUtils.isEmpty(apduCtrStr)) {
            edtApduCtr.requestFocus();
            showToast("APDU exchange control code shouldn't be empty!");
            return false;
        }
        return true;
    }

    /**
     * 透传APDU到卡片
     */
    private void transmitApdu() {
        String apduCtrStr = edtApduCtr.getText().toString();
        int apduCtr = Integer.parseInt(apduCtrStr, 16);
        String apduStr = apdu.getText().toString();
        LogUtil.e(TAG, "transmitApdu:" + apduStr);
        byte[] send = ByteUtil.hexStr2Bytes(apduStr);
        byte[] recv = new byte[2048];
        try {
            addStartTimeWithClear("transmitApdu()");
            LogUtil.e(TAG, "transmitApdu,send:" + ByteUtil.bytes2HexStr(send));
            int len = MyApplication.app.readCardOptV2.transmitApduExx(cardType, apduCtr, send, recv);
            addEndTime("transmitApdu()");
            if (len < 0) {
                LogUtil.e(TAG, "transmitApdu failed,code:" + len);
                showToast(AidlErrorCodeV2.valueOf(len).getMsg());
            } else {
                LogUtil.e(TAG, "transmitApdu success,recv:" + ByteUtil.bytes2HexStr(recv));
                byte[] valid = Arrays.copyOf(recv, len);
                if (cardType == CardType.NFC.getValue() || cardType == CardType.IC.getValue()
                        || cardType == CardType.PSAM0.getValue() || cardType == CardType.SAM1.getValue()
                        || cardType == CardType.MIFARE_DESFIRE.getValue()) {
                    // (NFC)received data contains swa,swb
                    byte[] outData = Arrays.copyOf(valid, valid.length - 2);
                    byte swa = valid[valid.length - 2];//swa
                    byte swb = valid[valid.length - 1];//swb
                    showApduRecv(true, outData, swa, swb);
                } else {
                    // (Mifare/Felica)received data not contains swa,swb
                    showApduRecv(false, valid, Byte.MIN_VALUE, Byte.MIN_VALUE);
                }
            }
            showSpendTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示收到的APDU数据
     */
    private void showApduRecv(boolean hasSW, byte[] outData, byte swa, byte swb) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        SpannableString sb = new SpannableString("------------------- APDU Receive-------------------\n");
        sb.setSpan(new StyleSpan(Typeface.BOLD), 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.append(sb);

        ssb.append("outData:");
        ssb.append(ByteUtil.bytes2HexStr(outData));
        ssb.append("\n");
        if (hasSW) {
            ssb.append("SWA:");
            ssb.append(ByteUtil.bytes2HexStr(swa));
            ssb.append("\n");
            ssb.append("SWB:");
            ssb.append(ByteUtil.bytes2HexStr(swb));
            ssb.append("\n");
        }
        addText(ssb);
    }

    private void addText(CharSequence msg) {
        CharSequence preMsg = result.getText();
        runOnUiThread(() -> result.setText(TextUtils.concat(preMsg, msg)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.app.readCardOptV2.cardOff(AidlConstantsV2.CardType.NFC.getValue());
            MyApplication.app.readCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
