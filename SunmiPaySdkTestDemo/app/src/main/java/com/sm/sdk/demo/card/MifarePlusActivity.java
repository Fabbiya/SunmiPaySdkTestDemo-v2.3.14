package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;

public class MifarePlusActivity extends BaseAppCompatActivity {
    private EditText edtReadBlockNo;
    private EditText edtReadKey;
    private EditText edtReadBlockData;
    private EditText edtWriteBlockNo;
    private EditText edtWriteKey;
    private EditText edtWriteBlockData;
    private EditText edtAlterBlockNo;
    private EditText edtAlterOldKey;
    private EditText edtAlterNewKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_mifareplus);
        initView();
        checkCard();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_mifare_plus);
        edtReadBlockNo = findViewById(R.id.edt_read_block_no);
        edtReadKey = findViewById(R.id.edt_read_key);
        edtReadBlockData = findViewById(R.id.edt_read_block_data);
        edtWriteBlockNo = findViewById(R.id.edt_write_block_no);
        edtWriteKey = findViewById(R.id.edt_write_key);
        edtWriteBlockData = findViewById(R.id.edt_write_block_data);
        edtAlterBlockNo = findViewById(R.id.edt_alter_block_no);
        edtAlterOldKey = findViewById(R.id.edt_alter_old_key);
        edtAlterNewKey = findViewById(R.id.edt_alter_new_key);
        findViewById(R.id.mb_read).setOnClickListener(this);
        findViewById(R.id.mb_write).setOnClickListener(this);
        findViewById(R.id.mb_change_password).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_read:
                mifarePlusReadBlock();
                break;
            case R.id.mb_write:
                mifarePlusWriteBlockData();
                break;
            case R.id.mb_change_password:
                mifarePlusChangeBlockKey();
                break;
        }
    }

    private void checkCard() {
        try {
            showSwingCardHintDialog(0);
            addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCard(CardType.MIFARE_PLUS.getValue(), mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findMagCard");
            dismissSwingCardHintDialog();
            showSpendTime();
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findICCard:" + atr);
            dismissSwingCardHintDialog();
            showSpendTime();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findRFCard:" + uuid);
            dismissSwingCardHintDialog();
            showSpendTime();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            addEndTime("checkCard()");
            showSpendTime();
            dismissSwingCardHintDialog();
            String tip = "check card failed, code:" + code + ",msg:" + message;
            LogUtil.e(TAG, tip);
            showToast(tip);
        }
    };

    /** MifarePlus read block data */
    private void mifarePlusReadBlock() {
        try {
            if (!checkInput(edtReadBlockNo, edtReadKey, null, null)) {
                return;
            }
            String blockNo = edtReadBlockNo.getText().toString();
            String blockKey = edtReadKey.getText().toString();
            int blkNo = Integer.parseInt(blockNo, 16);
            byte[] key = ByteUtil.hexStr2Bytes(blockKey);
            byte[] out = new byte[260];
            addStartTimeWithClear("mifarePlusReadBlock()");
            int len = MyApplication.app.readCardOptV2.mifarePlusReadBlock(blkNo, key, out);
            addEndTime("mifarePlusReadBlock()");
            if (len < 0) {
                showToast("mifarePlusReadBlock failed");
                LogUtil.e(Constant.TAG, "mifarePlusReadBlock error,code:" + len);
                showSpendTime();
                return;
            }
            byte[] valid = Arrays.copyOf(out, len);
            edtReadBlockData.setText(ByteUtil.bytes2HexStr(valid));
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** MifarePlus write block data */
    private void mifarePlusWriteBlockData() {
        try {
            if (!checkInput(edtWriteBlockNo, edtWriteKey, edtWriteBlockData, null)) {
                return;
            }
            String blockNo = edtWriteBlockNo.getText().toString();
            String blockKey = edtWriteKey.getText().toString();
            String blockData = edtWriteBlockData.getText().toString();
            int blkNo = Integer.parseInt(blockNo, 16);
            byte[] key = ByteUtil.hexStr2Bytes(blockKey);
            byte[] data = ByteUtil.hexStr2Bytes(blockData);
            addStartTimeWithClear("mifarePlusWriteBlock()");
            int code = MyApplication.app.readCardOptV2.mifarePlusWriteBlock(blkNo, key, data);
            addEndTime("mifarePlusWriteBlock()");
            if (code < 0) {
                showToast("mifarePlusWriteBlockData failed");
                LogUtil.e(Constant.TAG, "mifarePlusWriteBlockData error,code:" + code);
            } else {
                showToast("mifarePlusWriteBlockData success");
                LogUtil.e(Constant.TAG, "mifarePlusWriteBlockData success");
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** MifarePlus change block key */
    private void mifarePlusChangeBlockKey() {
        try {
            if (!checkInput(edtAlterBlockNo, edtAlterOldKey, null, edtAlterNewKey)) {
                return;
            }
            String blockNo = edtAlterBlockNo.getText().toString();
            String blockOldKey = edtAlterOldKey.getText().toString();
            String blockNewkey = edtAlterNewKey.getText().toString();
            int blkNo = Integer.parseInt(blockNo, 16);
            byte[] oldKey = ByteUtil.hexStr2Bytes(blockOldKey);
            byte[] newKey = ByteUtil.hexStr2Bytes(blockNewkey);
            addStartTimeWithClear("mifarePlusChangeBlockKey()");
            int code = MyApplication.app.readCardOptV2.mifarePlusChangeBlockKey(blkNo, oldKey, newKey);
            addEndTime("mifarePlusChangeBlockKey()");
            if (code < 0) {
                showToast("mifarePlusChangeBlockKey failed");
                LogUtil.e(Constant.TAG, "mifarePlusChangeBlockKey error,code:" + code);
            } else {
                showToast("mifarePlusChangeBlockKey success");
                LogUtil.e(Constant.TAG, "mifarePlusChangeBlockKey success");
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Check input data */
    private boolean checkInput(EditText block, EditText key1, EditText data, EditText key2) {
        if (block != null) {
            String blockNo = block.getText().toString();
            if (TextUtils.isEmpty(blockNo) || !Utility.checkHexValue(blockNo)) {
                showToast("blockNo should be 2 hex characters!");
                block.requestFocus();
                return false;
            }
            int blkNo = Integer.parseInt(blockNo, 16);
            if (blkNo < 0 || blkNo >= 0x100) {
                showToast("blockNo should in [00~FF]");
                block.requestFocus();
                return false;
            }
        }
        if (key1 != null) {
            String blockKey = key1.getText().toString();
            if (TextUtils.isEmpty(blockKey) || !Utility.checkHexValue(blockKey) || blockKey.length() != 32) {
                showToast("blockKey should be 32 hex characters!");
                key1.requestFocus();
                return false;
            }
        }
        if (data != null) {
            String blockData = data.getText().toString();
            if (TextUtils.isEmpty(blockData) || !Utility.checkHexValue(blockData) || blockData.length() != 32) {
                showToast("blockData should be 32 hex characters!");
                data.requestFocus();
                return false;
            }
        }
        if (key2 != null) {
            String blockKey = key2.getText().toString();
            if (TextUtils.isEmpty(blockKey) || !Utility.checkHexValue(blockKey) || blockKey.length() != 32) {
                showToast("blockKey should be 32 hex characters!");
                key2.requestFocus();
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.app.readCardOptV2.cardOff(CardType.MIFARE_PLUS.getValue());
            MyApplication.app.readCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
