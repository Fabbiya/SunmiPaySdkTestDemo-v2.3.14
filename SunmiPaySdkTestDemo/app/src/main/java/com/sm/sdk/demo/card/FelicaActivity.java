package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.bean.ApduRecvV2;
import com.sunmi.pay.hardware.aidlv2.bean.ApduSendV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;

public class FelicaActivity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;

    private EditText apduCmd;
    private EditText apduLc;
    private EditText apduIndata;
    private EditText apduLe;
    private TextView mTvResultInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_felica);
        initView();
//        setFelicaParamter();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_FELICA);
        apduCmd = findViewById(R.id.edit_command);
        apduLc = findViewById(R.id.edit_lc_length);
        apduIndata = findViewById(R.id.edit_data);
        apduLe = findViewById(R.id.edit_le_length);
        mTvResultInfo = findViewById(R.id.tv_info);
        findViewById(R.id.mb_check_card).setOnClickListener(this);
        findViewById(R.id.mb_send_apdu).setOnClickListener(this);
        apduCmd.setText("00000000");
        apduLe.setText("0100");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_check_card:
                mTvResultInfo.setText(null);
                checkCard();
                break;
            case R.id.mb_send_apdu:
                if (checkInputData()) {
                    sendApduByApduCommand();
                }
                break;
        }
    }

    private void checkCard() {
        try {
            int cardType = CardType.FELICA.getValue();
            addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCard(cardType, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {
        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findMagCard:" + Utility.bundle2String(bundle));
            showSpendTime();
        }

        @Override
        public void findICCardEx(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findICCard:" + Utility.bundle2String(info));
            showSpendTime();
        }

        /**
         * Find RF card
         *
         * @param info return data，contain the following keys:
         *             <br/>cardType: card type (int)
         *             <br/>uuid: card's UUID (String)
         *             <br/>ats: card's ATS (String)
         *             <br/>sak: card's SAK, if exist (int) (M1 S50:0x08, M1 S70:0x18, CPU:0x28)
         *             <br/>cardCategory: card's category,'A' or 'B', if exist (int)
         *             <br/>atqa: card's ATQA, if exist (byte[])
         *             <br/>IDm: Manufacture ID
         *             <br/>PMm: Manufacture Parameter
         */
        @Override
        public void findRFCardEx(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            String msg = Utility.formatStr("findRFCard:\nuuid:%s\nIDm:%s\nPMm:%s",
                    info.getString("uuid"), info.getString("IDm"), info.getString("PMm"));
            LogUtil.e(Constant.TAG, msg);
            setInfoText(msg);
            setDefaultIndata(info.getString("IDm"));
            showSpendTime();
        }

        @Override
        public void onErrorEx(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            int code = info.getInt("code");
            String msg = info.getString("message");
            String error = "onError:" + msg + " -- " + code;
            LogUtil.e(Constant.TAG, error);
            showToast(error);
            showSpendTime();
        }
    };

    /** 设置默认数据 */
    private void setDefaultIndata(String IDm) {
        IDm = Utility.null2String(IDm);
        final String uuidStr = "1006" + IDm + "010009018000";
        runOnUiThread(() -> {
            String hexLc = Integer.toHexString(uuidStr.length() / 2);
            if (hexLc.length() % 2 != 0) {
                hexLc = "0" + hexLc;
            }
            apduLc.setText(hexLc);
            apduIndata.setText(uuidStr);
        });
    }

    /** Check input data */
    private boolean checkInputData() {
        int limitLen = 4;
        String command = apduCmd.getText().toString();
        String lc = apduLc.getText().toString();
        String indata = apduIndata.getText().toString();
        String le = apduLe.getText().toString();

        if (command.length() != 8 || !Utility.checkHexValue(command)) {
            apduCmd.requestFocus();
            showToast("command should be 8 hex characters!");
            return false;
        }
        if (lc.length() > limitLen || !Utility.checkHexValue(lc)) {
            apduLc.requestFocus();
            showToast(formatStr("Lc should less than %d hex characters!", limitLen));
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
        if (le.length() > limitLen || !Utility.checkHexValue(le)) {
            apduLe.requestFocus();
            showToast(formatStr("Le should less than %d hex characters!", limitLen));
            return false;
        }
        int leValue = Integer.parseInt(le, 16);
        if (leValue < 0 || leValue > 256) {
            apduLe.requestFocus();
            showToast("Le value should in [0,0x0100]");
            return false;
        }
        return true;
    }

    private String formatStr(String format, Object... params) {
        return String.format(format, params);
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
            int code = MyApplication.app.readCardOptV2.apduCommand(AidlConstantsV2.CardType.FELICA.getValue(), send, recv);
            addEndTime("apduCommand()");
            if (code < 0) {
                LogUtil.e(TAG, "apduCommand failed,code:" + code);
                showToast(getString(R.string.fail) + ":" + code);
            } else {
                showApduRecv(recv.outlen, recv.outData, recv.swa, recv.swb);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 显示收到的APDU数据 */
    private void showApduRecv(int outLen, byte[] outData, byte swa, byte swb) {
        String swaStr = ByteUtil.bytes2HexStr(swa);
        String swbStr = ByteUtil.bytes2HexStr(swb);
        String outDataStr = ByteUtil.bytes2HexStr(Arrays.copyOf(outData, outLen));
        String temp = String.format("SWA:%s\nSWB:%s\noutData:%s", swaStr, swbStr, outDataStr);
        setInfoText(temp);
    }

    private void setInfoText(CharSequence msg) {
        runOnUiThread(() -> mTvResultInfo.setText(msg));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.app.readCardOptV2.cardOff(CardType.FELICA.getValue());
            MyApplication.app.readCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Set Felica related parameters */
    private void setFelicaParamter() {
        try {
            //set Felica polling system code as 0x8008
            byte[] dataIn = ByteUtil.short2BytesBE((short) 0x8008);
            byte[] dataOut = new byte[0];
            addStartTimeWithClear("smartCardIoControl()");
            int code = MyApplication.app.readCardOptV2.smartCardIoControl(AidlConstants.CardType.FELICA.getValue(), 0, dataIn, dataOut);
            addEndTime("smartCardIoControl()");
            Log.e(Constant.TAG, "set Felica polling system code, code:" + code);
            //set Felica apdu timeout time as 200ms
            dataIn = ByteUtil.int2BytesBE(200);
            code = MyApplication.app.readCardOptV2.smartCardIoControl(AidlConstants.CardType.FELICA.getValue(), 1, dataIn, dataOut);
            Log.e(Constant.TAG, "set Felica apdu timeout time, code:" + code);
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
