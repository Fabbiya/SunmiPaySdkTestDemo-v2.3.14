package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sm.sdk.demo.view.SwingCardHintDialog;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;
import java.util.Locale;

/**
 * This page show how to Read/Write MifareUtralightC/MifareUtraLightEv1/MifareUtralightNano card.
 * These Utralight cards has 4 bytes blocks,and blocks can be read/written directly without authentication.
 * For each card,the available blocks range is:
 * MifareUtraLightC: 4~39(04h-27h)
 * MifareUtralightEv1-MF0UL11: 4~19(04h~13h)
 * MifareUtraligthEv1-MF0UL21: 4~40(04h~28h)
 * MifareUtralightNano: 4~13(04h~0Dh)
 * other blocks which not in the listed range are control blocks, write control blocks
 * has many risks(eg, locked the card), if write, please refer to NXP MifareUtralight specifications.
 */
public class MifareUltralightCActivity extends BaseAppCompatActivity {
    private EditText edtKey;
    private EditText edtBlockNo;
    private EditText edtBlockData;
    private SwingCardHintDialog mHintDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_mifare_ultralight_c);
        initView();
        checkCard();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_mifare_ultralight);
        edtKey = findViewById(R.id.edit_key);
        edtBlockNo = findViewById(R.id.edit_block_no);
        edtBlockData = findViewById(R.id.edit_block_data);
        findViewById(R.id.mb_read).setOnClickListener(this);
        findViewById(R.id.mb_write).setOnClickListener(this);
        mHintDialog = new SwingCardHintDialog(this, 0);
        mHintDialog.setOwnerActivity(this);
        edtKey.setText("0000000000000000");
        edtBlockNo.setText("4");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_read:
                onReadClick();
                break;
            case R.id.mb_write:
                onWriteClick();
                break;
        }
    }

    /** 刷卡 */
    private void checkCard() {
        try {
            showHintDialog();
            addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCard(AidlConstants.CardType.MIFARE.getValue(), mReadCardCallback, 60);
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
            dismissHintDialog();
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(TAG, "findICCard, atr:" + atr);
            showSpendTime();
            dismissHintDialog();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(TAG, "findRFCard, uuid:" + uuid);
            dismissHintDialog();
            showSpendTime();
        }

        @Override
        public void onError(final int code, final String message) throws RemoteException {
            addEndTime("checkCard()");
            showSpendTime();
            dismissHintDialog();
            String tip = "check card failed, code:" + code + ",msg:" + message;
            LogUtil.e(TAG, tip);
            showToast(tip);
        }
    };

    /** 读块数据 */
    private void onReadClick() {
        if (!checkInputData(false)) {
            return;
        }
        try {
//            byte[] rawKey = edtKey.getText().toString().getBytes();
//            // origin key：BREAKMEIFYOUCAN!  converted key：IEMKAERB!NACUOYF
//            byte[] key = convertKey(rawKey);
//            int code = MyApplication.mReadCardOptV2.mifareUltralightCAuth(key);
//            if (code != 0) {
//                showToast("Authentication failed!");
//                return;
//            }
            int blockNo = Integer.parseInt(edtBlockNo.getText().toString());
            byte[] out = new byte[128];
            addStartTimeWithClear("mifareUltralightCReadData()");
            int len = MyApplication.app.readCardOptV2.mifareUltralightCReadData(blockNo, out);
            addEndTime("mifareUltralightCReadData()");
            if (len < 0) {
                showToast("Read block failed, code:" + len);
                showSpendTime();
                return;
            }
            byte[] validOut = Arrays.copyOf(out, len);
            String dataOut = ByteUtil.bytes2HexStr(validOut);
            LogUtil.e(TAG, formatStr("Read block success,block:%d,data:%s", blockNo, dataOut));
            edtBlockData.setText(dataOut);
            showSpendTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 写块数据 */
    private void onWriteClick() {
        if (!checkInputData(true)) {
            return;
        }
        try {
//            byte[] rawKey = edtKey.getText().toString().getBytes();
//            // origin key：BREAKMEIFYOUCAN!  converted key：IEMKAERB!NACUOYF
//            byte[] key = convertKey(rawKey);
//            int code = MyApplication.mReadCardOptV2.mifareUltralightCAuth(key);
//            if (code != 0) {
//                showToast("Authentication failed!");
//                return;
//            }
            int blockNo = Integer.parseInt(edtBlockNo.getText().toString());
            byte[] data = ByteUtil.hexStr2Bytes(edtBlockData.getText().toString());
            addStartTimeWithClear("mifareUltralightCWriteData()");
            int code = MyApplication.app.readCardOptV2.mifareUltralightCWriteData(blockNo, data);
            addEndTime("mifareUltralightCWriteData()");
            if (code < 0) {
                showToast("Write block data failed, code:" + code);
            } else {
                showToast("Write block data success!");
            }
            showSpendTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 检查数据合法性 */
    private boolean checkInputData(boolean write) {
//        String keyStr = edtKey.getText().toString();
//        int keyLen = keyStr.length();
//        //密钥长度8/16/24字节
//        if (!Utility.checkHexValue(keyStr) || (keyLen != 8 && keyLen != 16 && keyLen != 24)) {
//            showToast("Key should be 8 or 16 or 24 characters!");
//            edtKey.requestFocus();
//            return false;
//        }
        String blockNoStr = edtBlockNo.getText().toString();
        if (TextUtils.isEmpty(blockNoStr)) {
            showToast("Block number should not be empty!");
            edtBlockNo.requestFocus();
            return false;
        }
        int blockNo = Integer.parseInt(edtBlockNo.getText().toString());
        if (blockNo < 0 || blockNo > 40) {
            showToast("Block number should in [4,40]");
            edtBlockNo.requestFocus();
            return false;
        }
        if (write) {
            String data = edtBlockData.getText().toString();
            //数据块长度4字节
            if (!Utility.checkHexValue(data)) {
                showToast("Block data should be 8 hex characters!");
                edtBlockData.requestFocus();
                return false;
            }
        }
        return true;
    }

    /** 转换认证密钥 */
    private byte[] convertKey(byte[] inputKey) {
        int length = inputKey.length;
        int mid = length / 2;
        if (length == 8 || length == 16 || length == 24) {
            byte[] head = new byte[mid];
            byte[] tail = new byte[mid];
            for (int i = 0; i < mid; i++) {
                head[mid - i - 1] = inputKey[i];
            }
            for (int i = mid; i < length; i++) {
                tail[length - i - 1] = inputKey[i];
            }
            return ByteUtil.concatByteArrays(head, tail);
        }
        return inputKey;
    }

    private String formatStr(String format, Object... params) {
        return String.format(Locale.getDefault(), format, params);
    }

    private void showHintDialog() {
        runOnUiThread(() -> {
            if (!mHintDialog.isShowing() && !isDestroyed()) {
                mHintDialog.show();
            }
        });
    }

    private void dismissHintDialog() {
        runOnUiThread(() -> {
            if (mHintDialog != null) {
                mHintDialog.dismiss();
            }
        });
    }

    @Override
    protected void onDestroy() {
        cancelCheckCard();
        super.onDestroy();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.app.readCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
