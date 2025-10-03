package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
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
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;

public class SRIActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_sri_4k);
        initView();
        checkCard();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_sri_card_test);
        findViewById(R.id.mb_get_uid).setOnClickListener(this);
        findViewById(R.id.mb_read_block32).setOnClickListener(this);
        findViewById(R.id.mb_write_block32).setOnClickListener(this);
        findViewById(R.id.mb_protect_block).setOnClickListener(this);
        findViewById(R.id.mb_get_block_protection).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_get_uid:
                getUid();
                break;
            case R.id.mb_read_block32:
                readBlock32();
                break;
            case R.id.mb_write_block32:
                writeBlock32();
                break;
            case R.id.mb_protect_block:
                protectBlock();
                break;
            case R.id.mb_get_block_protection:
                getBlockProtection();
                break;

        }
    }

    private void checkCard() {
        try {
            showSwingCardHintDialog(0);
            addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCard(CardType.SRI.getValue(), mCheckCardCallback, 60);
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
        }

        @Override
        public void onErrorEx(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            showSpendTime();
            dismissSwingCardHintDialog();
            int code = info.getInt("code");
            String msg = info.getString("message");
            String tip = "check card failed, code:" + code + ",msg:" + msg;
            LogUtil.e(TAG, tip);
            showToast(tip);
        }
    };

    /** SRI4K Get uid */
    private void getUid() {
        try {
            //SRI Specific command format: SOF + GET_UID (cmd) + CRC_L + CRL_H + EOF
            //SDK command format: GET_UID (cmd)
            TextView tvResult = findViewById(R.id.tv_get_uid_result);
            byte[] send = {0x0B}; // cmd
            byte[] recv = new byte[256];
            addStartTimeWithClear("transmitApdu()");
            int len = MyApplication.app.readCardOptV2.transmitApduExx(CardType.SRI.getValue(), 0x00, send, recv);
            addEndTime("transmitApdu()");
            LogUtil.e(TAG, "transmitApdu code:" + len);
            if (len < 0) {
                tvResult.setText(null);
                showSpendTime();
                return;
            }
            byte[] valid = Arrays.copyOf(recv, len);
            LogUtil.e(TAG, "transmitApdu success,recv:" + ByteUtil.bytes2HexStr(valid));
            byte[] uid = Arrays.copyOf(valid, valid.length - 2);
            String uidStr = ByteUtil.bytes2HexStr(uid);
            StringBuilder sb = new StringBuilder("\nuid:");
            sb.append(uidStr);
            tvResult.setText(sb);
            LogUtil.e(TAG, "sriGetUid() result:" + sb);
            showSpendTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** SRI4K Read 4 bytes data */
    private void readBlock32() {
        try {
            //SRI Specific command format: SOF + READ_BLOCK (cmd)+ ADDRESS + CRC_L + CRL_H + EOF
            //SDK command format: READ_BLOCK (cmd)+ ADDRESS
            TextView tvResult = findViewById(R.id.tv_read_block32_result);
            EditText editText = findViewById(R.id.edt_read_block32_address);
            String addressStr = editText.getText().toString();
            if (TextUtils.isEmpty(addressStr) || !Utility.checkHexValue(addressStr)) {
                showToast("address should be hex characters");
                editText.requestFocus();
                return;
            }
            int address = Integer.parseInt(addressStr, 16);
            byte[] send = {0x08, (byte) address}; //cmd + address
            byte[] recv = new byte[256];
            addStartTimeWithClear("transmitApdu()");
            int len = MyApplication.app.readCardOptV2.transmitApduExx(CardType.SRI.getValue(), 0x00, send, recv);
            addEndTime("transmitApdu()");
            LogUtil.e(TAG, "transmitApdu code:" + len);
            if (len < 0) {
                tvResult.setText(null);
                showSpendTime();
                return;
            }
            byte[] valid = Arrays.copyOf(recv, len);
            LogUtil.e(TAG, "transmitApdu success,recv:" + ByteUtil.bytes2HexStr(valid));
            byte[] blockData = Arrays.copyOf(valid, valid.length - 2);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < blockData.length; i++) {
                sb.append("\nbyte");
                sb.append(i + 1);
                sb.append(":");
                sb.append(ByteUtil.bytes2HexStr(blockData[i]));
            }
            tvResult.setText(sb);
            LogUtil.e(TAG, "readBlock32() result:" + sb);
            showSpendTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * SRI4K Write 4 bytes data.
     * Due to write Block has no response，the transmitApduExx() will return -2520: Communication time out,
     * client App should check whether write operation success or not by read the written data.
     */
    private void writeBlock32() {
        try {
            //SRI Specific command format: SOF + WRITE_BLOCK (cmd)+ ADDRESS + DATA1 + DATA2 + DATA3 + DATA4 + CRC_L + CRL_H + EOF
            //SDK command format: WRITE_BLOCK (cmd)+ ADDRESS + DATA1 + DATA2 + DATA3 + DATA4
            EditText editText = findViewById(R.id.edt_write_block32_address);
            String addressStr = editText.getText().toString();
            if (TextUtils.isEmpty(addressStr) || !Utility.checkHexValue(addressStr)) {
                showToast("address should be hex characters");
                editText.requestFocus();
                return;
            }
            editText = findViewById(R.id.edt_write_block32_data);
            String dataInStr = editText.getText().toString();
            if (TextUtils.isEmpty(dataInStr) || !Utility.checkHexValue(dataInStr) || dataInStr.length() != 8) {
                showToast("Data to be written should be 8 hex characters");
                editText.requestFocus();
                return;
            }
            int address = Integer.parseInt(addressStr, 16);
            byte[] cmd = {0x09, (byte) address}; //cmd + address
            byte[] data = ByteUtil.hexStr2Bytes(dataInStr);
            byte[] send = ByteUtil.concatByteArrays(cmd, data);
            byte[] recv = new byte[256];
            addStartTimeWithClear("transmitApdu()");
            int code = MyApplication.app.readCardOptV2.transmitApduExx(CardType.SRI.getValue(), 0x00, send, recv);
            addEndTime("transmitApdu()");
            LogUtil.e(TAG, "transmitApdu code:" + code);
            //有些操作，卡片是不会返回信息对结果确认的。比如写块操作，此时接口会报-12520（接收超时），因为卡片本身就无响应。
            //写成功与否不能通过返回值判断，需重新读块数据对比
            showToast("writeBlock32 code:" + code);
            showSpendTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write block's protection bits
     * <br/>对于SRI4K卡，nLockReg值含义如下:
     * <br/>bit0对应blocks7和blocks8
     * <br/>bit1到b7依次对应block9到block15
     * <br/>bit为0表示对应的block配置了写保护，不能做写操作；bit为1表示未做写保护
     */
    private void protectBlock() {
//        try {
//            EditText editText = findViewById(R.id.edt_protect_block_nlockReg);
//            String nLockRegStr = editText.getText().toString();
//            if (TextUtils.isEmpty(nLockRegStr) || !Utility.checkHexValue(nLockRegStr)) {
//                showToast("nLockReg should be 2 hex characters");
//                editText.requestFocus();
//                return;
//            }
//            int nLockReg = Integer.parseInt(nLockRegStr, 16);
//            addStartTimeWithClear("protectBlock()");
//            int code = MyApplication.app.readCardOptV2.sriProtectBlock((byte) nLockReg);
//            addEndTime("protectBlock()");
//            Log.e(TAG, "protectBlock() code:" + code);
//            showToast("protectBlock " + (code == 0 ? "success" : "failed"));
//            showSpendTime();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Get block protection bits
     * <br/>对于SRI4K卡，nLockReg值含义如下:
     * <br/>bit0对应blocks7和blocks8
     * <br/>bit1到b7依次对应block9到block15
     * <br/>bit为0表示对应的block配置了写保护，不能做写操作；bit为1表示未做写保护
     */
    private void getBlockProtection() {
//        try {
//            byte[] dataOut = new byte[1];
//            addStartTimeWithClear("getBlockProtection()");
//            int code = MyApplication.app.readCardOptV2.sriGetBlockProtection(dataOut);
//            addEndTime("getBlockProtection()");
//            Log.e(TAG, "getBlockProtection() code:" + code);
//            TextView tvResult = findViewById(R.id.tv_block_protection_result);
//            if (code != 0) {
//                tvResult.setText(null);
//                showSpendTime();
//                return;
//            }
//            byte nLockReg = dataOut[0];
//            StringBuilder sb = new StringBuilder("nLockReg:\n");
//            sb.append(ByteUtil.bytes2HexStr(nLockReg));
//            tvResult.setText(sb);
//            LogUtil.e(TAG, "getBlockProtection() result:" + sb);
//            showSpendTime();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.app.readCardOptV2.cardOff(CardType.SRI.getValue());
            MyApplication.app.readCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
