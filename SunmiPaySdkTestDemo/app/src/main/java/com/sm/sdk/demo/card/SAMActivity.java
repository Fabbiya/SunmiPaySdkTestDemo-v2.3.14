package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidlv2.bean.ApduRecvV2;
import com.sunmi.pay.hardware.aidlv2.bean.ApduSendV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SAMActivity extends BaseAppCompatActivity {
    private EditText apduCmd;
    private EditText apduLc;
    private EditText apduIndata;
    private EditText apduLe;
    private TextView tvATR;
    private TextView mTvApduResult;
    private int cardType = AidlConstants.CardType.PSAM0.getValue();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_sam);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_sam);
        RadioGroup group = findViewById(R.id.rdo_group_card_type);
        RadioButton rdoButton = findViewById(R.id.rdo_sam0);
        group.setOnCheckedChangeListener((grp, checkedId) -> {
            switch (checkedId) {
                case R.id.rdo_sam0:
                    cardType = AidlConstants.CardType.PSAM0.getValue();
                    checkCard();
                    break;
                case R.id.rdo_sam1:
                    cardType = AidlConstants.CardType.SAM1.getValue();
                    checkCard();
                    break;
                case R.id.rdo_sam2:
                    cardType = AidlConstants.CardType.SAM2.getValue();
                    checkCard();
                    break;
                case R.id.rdo_sam3:
                    cardType = AidlConstants.CardType.SAM3.getValue();
                    checkCard();
                    break;
                case R.id.rdo_sam4:
                    cardType = AidlConstants.CardType.SAM4.getValue();
                    checkCard();
                    break;
                case R.id.rdo_sam5:
                    cardType = AidlConstants.CardType.SAM5.getValue();
                    checkCard();
                    break;
            }
        });
        apduCmd = findViewById(R.id.edit_command);
        apduLc = findViewById(R.id.edit_lc_length);
        apduIndata = findViewById(R.id.edit_data);
        apduLe = findViewById(R.id.edit_le_length);
        tvATR = findViewById(R.id.tv_atr);
        mTvApduResult = findViewById(R.id.tv_apdu_result);
        findViewById(R.id.mb_ok).setOnClickListener(this);
        rdoButton.setChecked(true);

        apduCmd.setText("00840000");
        apduLc.setText("00");
        apduIndata.setText("");
        apduLe.setText("08");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_ok:
                if (checkInputData(0)) {
                    sendApduByTransmitApdu();
                }
//                if (checkInputData(1)) {
//                    sendApduBySmartCardExchange();
//                }
//                if (checkInputData(2)) {
//                    sendApduByApduCommand();
//                }
                break;
        }
    }

    /** Check card */
    private void checkCard() {
        try {
            addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCard(cardType, mCheckCardCallback, 5);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findMagCard:track1");
            showSpendTime();
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findICCard:" + atr);
            showSpendTime();
            updateATR(atr);
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findRFCard:" + uuid);
            showSpendTime();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            addEndTime("checkCard()");
            String error = "CheckCard error,code:" + code + ",msg:" + message;
            LogUtil.e(Constant.TAG, error);
            showToast(error);
            showSpendTime();
        }
    };

    /**
     * Check input data
     *
     * @param type 0-send by transmitApdu
     *             1-send by smartCardExchange
     *             2-send by ApduCommand
     */
    private boolean checkInputData(int type) {
        int limitLen = 0;
        if (type == 0) {//support long APDU
            limitLen = 6;
        } else if (type == 1) {
            limitLen = 2;
        } else if (type == 2) {
            limitLen = 4;
        }
        String command = apduCmd.getText().toString();
        String lc = apduLc.getText().toString();
        String indata = apduIndata.getText().toString();
        String le = apduLe.getText().toString();

        if (command.length() != 8 || !Utility.checkHexValue(command)) {
            apduCmd.requestFocus();
            showToast("command should be 8 hex characters!");
            return false;
        }
        if (type != 0 || !TextUtils.isEmpty(lc)) {//exist Lc
            if (lc.length() > limitLen || !Utility.checkHexValue(lc)) {
                apduLc.requestFocus();
                showToast(Utility.formatStr("Lc should less than %d hex characters!", limitLen));
                return false;
            }
            int lcValue = Integer.parseInt(lc, 16);
            if (lcValue < 0 || lcValue > 256) {
                apduLc.requestFocus();
                showToast("Lc value should in [0,0x0100]");
                return false;
            }
            if (indata.length() != lcValue * 2 || (indata.length() > 0 && !Utility.checkHexValue(indata))) {
                apduIndata.requestFocus();
                showToast("indata value should lc*2 hex characters!");
                return false;
            }
        }
        if (type != 0 || !TextUtils.isEmpty(le)) {//exist Le
            if (le.length() > limitLen || !Utility.checkHexValue(le)) {
                apduLe.requestFocus();
                showToast(Utility.formatStr("Le should less than %d hex characters!", limitLen));
                return false;
            }
            int leValue = Integer.parseInt(le, 16);
            if (leValue < 0 || leValue > 256) {
                apduLe.requestFocus();
                showToast("Le value should in [0,0x0100]");
                return false;
            }
        }
        return true;
    }

    /** 更新ATR */
    private void updateATR(String atr) {
        tvATR.post(() -> tvATR.setText("ATR:" + atr));
    }

    /** Send Apdu by transmitApdu */
    private void sendApduByTransmitApdu() {
        try {
            String command = apduCmd.getText().toString();
            String lc = apduLc.getText().toString();
            String indata = apduIndata.getText().toString();
            String le = apduLe.getText().toString();

            List<byte[]> sendList = new ArrayList<>();
            sendList.add(ByteUtil.hexStr2Bytes(command));
            if (!TextUtils.isEmpty(lc)) {//exist Lc
                int lcValue = Integer.parseInt(lc, 16);
                if (lcValue > 0) {//exist Lc and dataIn
                    sendList.add(ByteUtil.hexStr2Bytes(lc));
                    sendList.add(ByteUtil.hexStr2Bytes(indata));
                }
            }
            if (!TextUtils.isEmpty(le)) {//exist Le
                sendList.add(ByteUtil.hexStr2Bytes(le));
            }
            byte[] send = ByteUtil.concatByteArrays(sendList);
            byte[] recv = new byte[260];
            addStartTimeWithClear("transmitApdu()");
            int len = MyApplication.app.readCardOptV2.transmitApdu(cardType, send, recv);
            addEndTime("transmitApdu()");
            if (len < 0) {
                LogUtil.e(TAG, "transmitApdu failed,code:" + len);
                showToast(getString(R.string.fail) + ":" + len);
                showSpendTime();
                return;
            }
            byte[] valid = Arrays.copyOf(recv, len);
            byte[] outData = Arrays.copyOf(valid, valid.length - 2);
            byte swa = valid[valid.length - 2];//swa
            byte swb = valid[valid.length - 1];//swb
            showApduRecv(outData, swa, swb);
            String msg = String.format("outData:%s\nswa:%02X\nswb:%02X", ByteUtil.bytes2HexStr(outData), swa, swb);
            LogUtil.e(TAG, msg);
            showSpendTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 以字节数组方式发送ISO-7816标准的APDU */
    private void sendApduBySmartCardExchange() {
        String command = apduCmd.getText().toString();
        String lc = apduLc.getText().toString();
        String indata = apduIndata.getText().toString();
        String le = apduLe.getText().toString();
        byte[] send = ByteUtil.concatByteArrays(
                ByteUtil.hexStr2Bytes(command),
                ByteUtil.hexStr2Bytes(lc),
                ByteUtil.hexStr2Bytes(indata),
                ByteUtil.hexStr2Bytes(le)
        );
        try {
            byte[] recv = new byte[260];
            addStartTimeWithClear("smartCardExchange()");
            int code = MyApplication.app.readCardOptV2.smartCardExchange(cardType, send, recv);
            addEndTime("smartCardExchange()");
            if (code < 0) {
                LogUtil.e(TAG, "smartCardExchange failed,code:" + code);
                showToast(getString(R.string.fail) + ":" + code);
            } else {
                LogUtil.e(TAG, "smartCardExchange success,recv:" + ByteUtil.bytes2HexStr(recv));
                int outLen = ByteUtil.unsignedShort2IntBE(recv, 0);
                byte[] outData = {};
                if (outLen > 0) {
                    outData = Arrays.copyOfRange(recv, 2, 2 + outLen);
                }
                byte swa = recv[2 + outLen];
                byte swb = recv[2 + outLen + 1];
                showApduRecv(outData, swa, swb);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 以ApduRecvV2方式发送ISO-7816标准的APDU */
    private void sendApduByApduCommand() {
        String command = apduCmd.getText().toString();
        String lc = apduLc.getText().toString();
        String indata = apduIndata.getText().toString();
        String le = apduLe.getText().toString();
        ApduSendV2 send = new ApduSendV2();
        send.command = ByteUtil.hexStr2Bytes(command);
        send.lc = Short.parseShort(lc, 16);
        send.dataIn = ByteUtil.hexStr2Bytes(indata);
        send.le = Short.parseShort(le, 16);
        try {
            ApduRecvV2 recv = new ApduRecvV2();
            addStartTimeWithClear("apduCommand()");
            int code = MyApplication.app.readCardOptV2.apduCommand(cardType, send, recv);
            addEndTime("apduCommand()");
            if (code < 0) {
                LogUtil.e(TAG, "apduCommand failed,code:" + code);
                showToast(getString(R.string.fail) + ":" + code);
            } else {
                byte[] outData = Arrays.copyOf(recv.outData, recv.outlen);
                showApduRecv(outData, recv.swa, recv.swb);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 显示收到的APDU数据 */
    private void showApduRecv(byte[] outData, byte swa, byte swb) {
        String swaStr = ByteUtil.bytes2HexStr(swa);
        String swbStr = ByteUtil.bytes2HexStr(swb);
        String outDataStr = ByteUtil.bytes2HexStr(outData);
        String temp = String.format("SWA:%s\nSWB:%s\noutData:%s", swaStr, swbStr, outDataStr);
        mTvApduResult.setText(temp);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
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
