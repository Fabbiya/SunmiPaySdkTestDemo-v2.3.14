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
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;
import java.util.regex.Pattern;

public class InnovatronActivity extends BaseAppCompatActivity {
    private EditText edtActiveCtr;
    private EditText edtApduCtr;
    private EditText edtApdu;
    private TextView tvResult;
    private Button btnCheckCard;
    private Button btnSendApdu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_innovatron);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_innovatron_test);
        edtActiveCtr = findViewById(R.id.edt_active_ctr_code);
        edtApduCtr = findViewById(R.id.edt_apdu_ctr_code);
        edtApdu = findViewById(R.id.edt_apdu);
        btnCheckCard = findViewById(R.id.check_card);
        btnSendApdu = findViewById(R.id.send_apdu);
        btnCheckCard.setOnClickListener(this);
        btnSendApdu.setOnClickListener(this);
        tvResult = findViewById(R.id.tv_result);
        edtApdu.setText("94A40800022F10");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_card:
                checkCard();
                break;
            case R.id.send_apdu:
                if (checkInputData()) {
                    transmitApduExx();
                }
                break;
        }
    }

    private void checkCard() {
        try {
            String ctrStr = edtActiveCtr.getText().toString();
            if (TextUtils.isEmpty(ctrStr)) {
                showToast("卡片激活控制参数不能为空");
                edtActiveCtr.requestFocus();
                return;
            }
            int activeCtr = Integer.parseInt(ctrStr, 16);
            showSwingCardHintDialog(0);
            addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCardEx(CardType.INNOVATRON.getValue(), activeCtr, 0, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(TAG, "findMagCard");
            dismissSwingCardHintDialog();
            showSpendTime();
        }

        @Override
        public void findICCardEx(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(TAG, "findICCard(), info:" + Utility.bundle2String(info));
            dismissSwingCardHintDialog();
            showSpendTime();
        }

        @Override
        public void findRFCardEx(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(TAG, "findRFCard(), info:" + Utility.bundle2String(info));
            dismissSwingCardHintDialog();
            showSpendTime();
            StringBuilder sb = new StringBuilder();
            byte[] activeData = info.getByteArray("activeData");
            sb.append("activeData:");
            sb.append(ByteUtil.bytes2HexStr(activeData));
            handleCheckCardSuccess("findRFCardEx, " + sb);
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
        String apduCtrStr = edtApduCtr.getText().toString();
        if (TextUtils.isEmpty(apduCtrStr)) {
            edtApduCtr.requestFocus();
            showToast("APDU exchange control code shouldn't be empty!");
            return false;
        }
        String apduStr = edtApdu.getText().toString();
        if (TextUtils.isEmpty(apduStr)) {
            edtApdu.requestFocus();
            showToast("apdu should not be empty!");
            return false;
        }
        if (!Pattern.matches("[0-9a-fA-F]+", apduStr)) {
            edtApdu.requestFocus();
            showToast("apdu should hex characters!");
            return false;
        }
        return true;
    }

    /** transmit APDU to Innovatron card */
    private void transmitApduExx() {
        String apduCtrStr = edtApduCtr.getText().toString();
        int apduCtr = Integer.parseInt(apduCtrStr, 16);
        String apduStr = edtApdu.getText().toString();
        byte[] send = ByteUtil.hexStr2Bytes(apduStr);
        byte[] recv = new byte[2048];
        try {
            addStartTimeWithClear("transmitApdu()");
            LogUtil.e(TAG, "transmitApdu,send:" + ByteUtil.bytes2HexStr(send));
            int len = MyApplication.app.readCardOptV2.transmitApduExx(CardType.INNOVATRON.getValue(), apduCtr, send, recv);
            addEndTime("transmitApdu()");
            if (len < 0) {
                LogUtil.e(TAG, "transmitApdu failed,code:" + len);
                showToast(AidlErrorCodeV2.valueOf(len).getMsg());
            } else {
                LogUtil.e(TAG, "transmitApdu success,recv:" + ByteUtil.bytes2HexStr(recv));
                byte[] valid = Arrays.copyOf(recv, len);
                // (NFC)received data contains swa,swb
                byte[] outData = Arrays.copyOf(valid, valid.length - 2);
                byte swa = valid[valid.length - 2];//swa
                byte swb = valid[valid.length - 1];//swb
                showApduRecv(true, outData, swa, swb);
            }
            showSpendTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** show received APDU data */
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

    /** Innovatron card apdu list */
    private String[] getInnovatronApdu() {
        return new String[]{
                "0001000000",
                "94A40000022000",
                "00A4040000",
                "00A4040005334D54522E",
                "00A4040005315449432E",
                "00A4040005A000000404",
                "00A4040005A000000291",
                "00A40400050404000001",
                "00A4040007A0000004040125",
                "94A40900023F00",
                "94A40200023F00",
                "94A40900020001",
                "94A40800020001",
                "94A40000020001",
                "94A40900020002",
                "94A40900020003",
                "94A40900022F10",
                "94A40800022F10",
                "94A40000022F10",
                "94A40900022000",
                "94A40200022000",
                "94A40900022001",
                "94A40900022010",
                "94A40900022020",
                "94A40900022030",
                "94A40900022040",
                "94A40900022050",
                "94A40800022050",
                "94A40000022050",
                "94A40900022069",
                "94A4090002202A",
                "94A4090002202B",
                "94A4090002202C",
                "94A4090002202D",
                "94A4090002202E",
                "94A4090002202F",
                "0084000008",
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.app.readCardOptV2.cardOff(CardType.INNOVATRON.getValue());
            MyApplication.app.readCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
