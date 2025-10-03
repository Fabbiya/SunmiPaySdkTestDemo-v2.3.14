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
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;

public class PassApduActivity extends BaseAppCompatActivity {
    private EditText edtActiveCtr;
    private EditText edtApdu;
    private TextView tvResult;
    private Button btnCheckCard;
    private Button btnSendApdu;
    private int cardType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_apdu_layout);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_pass_apdu);
        edtActiveCtr = findViewById(R.id.active_ctr_code);
        edtApdu = findViewById(R.id.apdu);
        tvResult = findViewById(R.id.result);
        btnCheckCard = findViewById(R.id.check_card);
        btnSendApdu = findViewById(R.id.send_apdu);
        btnCheckCard.setOnClickListener(this);
        btnSendApdu.setOnClickListener(this);
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
                    smartCardExChangePASS();
                }
                break;
        }
    }

    /** 清除结果 */
    private void handleClearData() {
        tvResult.setText("");
    }

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
            int contact = CardType.IC.getValue() | CardType.PSAM0.getValue() | CardType.SAM1.getValue();
            int contactless = CardType.NFC.getValue() | CardType.SRI.getValue() | CardType.ISO15693.getValue() | CardType.INNOVATRON.getValue();
            int allType = contact | contactless;
            if (DeviceUtil.isTossTerminal()) {//TOSS terminal无NFC功能
                allType = contact;
            }
            addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCardEx(allType, activeCtr, 0, mReadCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            LogUtil.e(TAG, "findRFCardEx, uuid:" + uuid);
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
            } else if (cardType == CardType.INNOVATRON.getValue()) {
                byte[] activeData = info.getByteArray("activeData");
                sb.append("activeData:");
                sb.append(ByteUtil.bytes2HexStr(activeData));
            } else {
                sb.append("uuid:");
                sb.append(uuid);
            }
            handleCheckCardSuccess("findRFCardEx, " + sb);
            showSpendTime();
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

    private void handleCheckCardSuccess(String msg) {
        runOnUiThread(() -> {
            StringBuilder sb = new StringBuilder("----------------------- check card success -----------------------\n");
            sb.append(msg);
            sb.append("\n");
            tvResult.setText(sb);
            btnSendApdu.setEnabled(true);
        });
    }

    private void handleCheckCardFailed(int code, final String msg) {
        addText("check card error,code:" + code + ", message:" + msg + "\n");
    }

    private boolean checkInputData() {
        String apduStr = edtApdu.getText().toString();
        if (TextUtils.isEmpty(apduStr)) {
            edtApdu.requestFocus();
            showToast("apdu should not be empty!");
            return false;
        }
        if (!Utility.checkHexValue(apduStr)) {
            edtApdu.requestFocus();
            showToast("apdu should hex characters!");
            return false;
        }
        return true;
    }

    /**
     * 透传APDU到卡片
     */
    private void smartCardExChangePASS() {
        String apduStr = edtApdu.getText().toString();
        LogUtil.e(TAG, "smartCardExChangePASS:" + apduStr);
        byte[] send = ByteUtil.hexStr2Bytes(apduStr);
        byte[] recv = new byte[2048];
        try {
            addStartTimeWithClear("smartCardExChangePASS()");
            LogUtil.e(TAG, "smartCardExChangePASS,send:" + ByteUtil.bytes2HexStr(send));
            int code = MyApplication.app.readCardOptV2.smartCardExChangePASS(cardType, send, recv);
            addEndTime("smartCardExChangePASS()");
            if (code < 0) {
                LogUtil.e(TAG, "smartCardExChangePASS failed,code:" + code);
                showToast(AidlErrorCodeV2.valueOf(code).getMsg());
                showSpendTime();
                return;
            }
            LogUtil.e(TAG, "smartCardExChangePASS success,recv:" + ByteUtil.bytes2HexStr(recv));
            int outLen = ByteUtil.unsignedShort2IntBE(recv, 0);
            byte[] outData = Arrays.copyOfRange(recv, 2, 2 + outLen);
            if (cardType == CardType.NFC.getValue() || cardType == CardType.IC.getValue()
                    || cardType == CardType.PSAM0.getValue() || cardType == CardType.SAM1.getValue()) {
                // (NFC)received data contains swa,swb
                byte swa = recv[2 + outLen];//swa
                byte swb = recv[3 + outLen];//swb
                showApduRecv(true, outData, swa, swb);
            } else {
                // (Mifare/Felica)received data not contains swa,swb
                showApduRecv(false, outData, Byte.MIN_VALUE, Byte.MIN_VALUE);
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
        CharSequence preMsg = tvResult.getText();
        runOnUiThread(() -> tvResult.setText(TextUtils.concat(preMsg, msg)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.app.readCardOptV2.cardOff(CardType.NFC.getValue());
            MyApplication.app.readCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
