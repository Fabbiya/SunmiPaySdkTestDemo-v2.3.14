package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.DeviceUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.security.SecurityOptV2;

import java.util.Arrays;

public class DukptKSNOperateActivity extends BaseAppCompatActivity {
    private TextView mTvInfo;
    private EditText mEditKeyIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_dukpt_ksn);
        initToolbarBringBack(R.string.security_duKpt_ksn_control);
        initView();
    }

    private void initView() {
        mEditKeyIndex = findViewById(R.id.key_index);
        mTvInfo = findViewById(R.id.tv_info);
        findViewById(R.id.mb_get_ksn).setOnClickListener(this);
        findViewById(R.id.mb_ksn_increased).setOnClickListener(this);
        findViewById(R.id.mb_get_init_ksn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ksn_increased:
                ksnIncreased();
                break;
            case R.id.mb_get_ksn:
                getKsn();
                break;
            case R.id.mb_get_init_ksn:
                getInitKSN();
                break;
        }
    }

    private void ksnIncreased() {
        try {
            SecurityOptV2 securityOptV2 = MyApplication.app.securityOptV2;
            String keyIndexStr = mEditKeyIndex.getText().toString();
            int keyIndex;
            try {
                keyIndex = Integer.parseInt(keyIndexStr);
                if (!KeyIndexUtil.checkDukptKeyIndex(keyIndex)) {
                    showKeyIndexToast();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showKeyIndexToast();
                return;
            }
            addStartTimeWithClear("dukptIncreaseKSN()");
            int result = securityOptV2.dukptIncreaseKSN(keyIndex);
            addEndTime("dukptIncreaseKSN()");
            toastHint(result);
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getKsn() {
        try {
            SecurityOptV2 securityOptV2 = MyApplication.app.securityOptV2;
            String keyIndexStr = mEditKeyIndex.getText().toString();
            //Nornmal dukpt key index is 0~9
            //Dukpt-AES key index is 10~19
            int keyIndex;
            try {
                keyIndex = Integer.parseInt(keyIndexStr);
                if (!KeyIndexUtil.checkDukptKeyIndex(keyIndex)) {
                    showKeyIndexToast();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showKeyIndexToast();
                return;
            }
            int len = 0;
            if (KeyIndexUtil.checkDukpt3DesKeyIndex(keyIndex)) {//Dukpt-3DES key
                len = 10;
            } else {//Dukpt-AES
                len = 12;
            }
            byte[] dataOut = new byte[len];
            addStartTimeWithClear("dukptCurrentKSN()");
            int result = securityOptV2.dukptCurrentKSN(keyIndex, dataOut);
            addEndTime("dukptCurrentKSN()");
            if (result == 0) {
                String hexStr = ByteUtil.bytes2HexStr(dataOut);
                mTvInfo.setText(hexStr);
            } else {
                toastHint(result);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getInitKSN() {
        try {
            byte[] buffer = new byte[12];
            int len = MyApplication.app.securityOptV2.dukptGetInitKSN(buffer);
            if (len < 0) {
                showToast("get init kSN failed, code:" + len);
                return;
            }
            String ksn = ByteUtil.bytes2HexStr(Arrays.copyOf(buffer, len));
            LogUtil.e(TAG, "dukptGetInitKSN() retValue:" + ksn);
            mTvInfo.setText(ksn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void showKeyIndexToast() {
        if (DeviceUtil.isBrazilCKD()) {
            showToast(R.string.security_duKpt_key_index_hint);
        } else {
            showToast(R.string.security_dukpt_key_index_hint);
        }
    }
}
