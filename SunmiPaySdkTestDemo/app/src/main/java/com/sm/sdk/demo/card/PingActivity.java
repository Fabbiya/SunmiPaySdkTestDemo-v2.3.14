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
import com.sm.sdk.demo.utils.IOUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.ThreadPoolUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;

public class PingActivity extends BaseAppCompatActivity {
    private EditText edtApdu;
    private EditText edtInterval;
    private Button btnCheckCard;
    private Button btnStart;
    private TextView tvResult;
    private int cardType;
    private PingTask task;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_ping_layout);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_ping);
        edtApdu = findViewById(R.id.edt_apdu);
        edtInterval = findViewById(R.id.edt_interval);
        btnCheckCard = findViewById(R.id.check_card);
        btnStart = findViewById(R.id.btn_start);
        btnCheckCard.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        tvResult = findViewById(R.id.result);
        edtApdu.setText("00A404000E315041592E5359532E444446303100");
        task = new PingTask();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_card:
                handleClearData();
                checkCard();
                break;
            case R.id.btn_start:
                handleStartClick();
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
            int activeCtr = 0;
            //支持M1卡
            int cardType = CardType.IC.getValue() | CardType.NFC.getValue();
            addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCardEx(cardType, activeCtr, 0, mReadCardCallback, 60);
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
            String msg = Utility.bundle2String(info);
            LogUtil.e(TAG, "findRFCardEx, uuid:" + msg);
            handleCheckCardSuccess("findRFCardEx, " + msg);
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
            StringBuilder sb = new StringBuilder("------------------ check card success ------------------\n");
            sb.append(msg);
            sb.append("\n");
            tvResult.setText(sb);
            btnStart.setEnabled(true);
        });
    }

    private void handleCheckCardFailed(int code, final String msg) {
        addText("check card error,code:" + code + ", message:" + msg + "\n");
    }

    private void handleStartClick() {
        if (!checkInputData()) {
            return;
        }
        String apduStr = edtApdu.getText().toString();
        String interval = edtInterval.getText().toString();
        task.apdu = ByteUtil.hexStr2Bytes(apduStr);
        task.delay = Integer.parseInt(interval);
        if (!task.running) {//未启动
            task.start();
            btnStart.setText("Stop");
        } else {//已启动
            task.stop();
            btnStart.setText("Start");
        }
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
        String intervalStr = edtApdu.getText().toString();
        if (TextUtils.isEmpty(intervalStr)) {
            edtApdu.requestFocus();
            showToast("interval should not be empty!");
            return false;
        }
        return true;
    }

    /** 以字节数组方式发送ISO-7816标准的APDU */
    private void smartCardExchange(byte[] send) {
        String sendStr = ByteUtil.bytes2HexStr(send);
        byte[] buffer = new byte[1024];
        try {
            LogUtil.e(TAG, "smartCardExchange send:" + sendStr);
            int code = MyApplication.app.readCardOptV2.smartCardExchange(cardType, send, buffer);
            if (code < 0) {
                LogUtil.e(TAG, "smartCardExchange failed,code:" + code);
                showToast(AidlErrorCodeV2.valueOf(code).getMsg());
            } else {
                int outLen = ByteUtil.unsignedShort2IntBE(buffer, 0);
                String recvStr = ByteUtil.bytes2HexStr(Arrays.copyOf(buffer, outLen + 4));
                LogUtil.e(TAG, "smartCardExchange success,recv:" + recvStr);
                byte[] outdata = {};
                if (outLen > 0) {
                    outdata = Arrays.copyOfRange(buffer, 2, 2 + outLen);
                }
                byte swa = buffer[2 + outLen];
                byte swb = buffer[2 + outLen + 1];
                showApduRecv(sendStr, recvStr, true, outdata, swa, swb);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Pass模式透传APDU到卡片 */
    private void sendPing() {
        String pingStr = "B2";
        byte[] buffer = new byte[2048];
        try {
            LogUtil.e(TAG, "smartCardExChangePASS send:" + pingStr);
            byte[] send = ByteUtil.hexStr2Bytes(pingStr);
            int code = MyApplication.app.readCardOptV2.smartCardExChangePASS(cardType, send, buffer);
            if (code < 0) {
                LogUtil.e(TAG, "smartCardExChangePASS failed,code:" + code);
                showToast(AidlErrorCodeV2.valueOf(code).getMsg());
                return;
            }
            int outLen = ByteUtil.unsignedShort2IntBE(buffer, 0);
            String recvStr = ByteUtil.bytes2HexStr(Arrays.copyOf(buffer, outLen + 4));
            LogUtil.e(TAG, "smartCardExChangePASS success,recv:" + recvStr);
            byte[] outData = Arrays.copyOfRange(buffer, 2, 2 + outLen);
            // (NFC)received data contains swa,swb
            byte swa = buffer[2 + outLen];//swa
            byte swb = buffer[3 + outLen];//swb
            showApduRecv(pingStr, recvStr, true, outData, swa, swb);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 显示收到的APDU数据 */
    private void showApduRecv(String send, String recv, boolean hasSW, byte[] outData, byte swa, byte swb) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        SpannableString sb = new SpannableString("------------------------------------------------\n");
        sb.setSpan(new StyleSpan(Typeface.BOLD), 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.append(sb);
        ssb.append("send:").append(send);
        ssb.append("\n");
        ssb.append("recv:").append(recv);
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
        task.stop();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.app.readCardOptV2.cardOff(AidlConstantsV2.CardType.NFC.getValue());
            MyApplication.app.readCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class PingTask implements Runnable {
        private volatile boolean running;
        private volatile Thread runner;
        private byte[] apdu;
        private int delay;

        private void start() {
            ThreadPoolUtil.executeInCachePool(this);
        }

        private void stop() {
            if (runner != null) {
                runner.interrupt();
            }
            running = false;
        }

        @Override
        public void run() {
            try {
                runner = Thread.currentThread();
                running = true;
                while (running) {
                    smartCardExchange(apdu);
                    sendPing();
                    IOUtil.delay(delay);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                running = false;
                runner = null;
            }
        }
    }
}
