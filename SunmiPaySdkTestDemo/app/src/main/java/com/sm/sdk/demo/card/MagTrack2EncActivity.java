package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.widget.Button;
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
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

public class MagTrack2EncActivity extends BaseAppCompatActivity {
    private EditText mEdtCode;
    private EditText mEdtType;
    private EditText mEdtMaskStart;
    private EditText mEdtMaskEnd;
    private EditText mEdtMaskChar;
    private Button mBtnTotal;
    private Button mBtnSuccess;
    private Button mBtnFail;
    private TextView mTvTrack2;
    private TextView mTvPan;
    private TextView mTvServiceCode;
    private Button mBtnCheckCard;
    private int mTotalTime;
    private int mSuccessTime;
    private int mFailTime;
    private final Handler handler = new Handler();
    private boolean checkingCard = false;
    private int vanCode;
    private int vanCardType;
    private int maskStart;
    private int maskEnd;
    private char maskChar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_mag_encrypt_pan);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_mag_track2_enc);
        mEdtCode = findViewById(R.id.edit_code);
        mEdtType = findViewById(R.id.edit_type);
        mEdtMaskStart = findViewById(R.id.edit_mask_start);
        mEdtMaskEnd = findViewById(R.id.edit_mask_end);
        mEdtMaskChar = findViewById(R.id.edit_mask_char);
        mBtnTotal = findViewById(R.id.mb_total);
        mBtnSuccess = findViewById(R.id.mb_success);
        mBtnFail = findViewById(R.id.mb_fail);
        mTvTrack2 = findViewById(R.id.tv_track2);
        mTvServiceCode = findViewById(R.id.tv_service_code);
        mTvPan = findViewById(R.id.tv_pan);
        mBtnCheckCard = findViewById(R.id.mb_start_check_card);
        mBtnCheckCard.setOnClickListener((v) -> switchCheckCard());
    }

    /** switch check card */
    private void switchCheckCard() {
        try {
            if (checkingCard) {
                handler.removeCallbacksAndMessages(null);
                MyApplication.app.readCardOptV2.cancelCheckCard();
                mBtnCheckCard.setText(R.string.card_start_check_card);
                checkingCard = false;
            } else {
                String codeStr = mEdtCode.getText().toString();
                String typeStr = mEdtType.getText().toString();
                String maskStartStr = mEdtMaskStart.getText().toString();
                String maskEndStr = mEdtMaskEnd.getText().toString();
                String maskCharStr = mEdtMaskChar.getText().toString();
                if (TextUtils.isEmpty(codeStr) || !checkHexValue(codeStr)) {
                    showToast("code should be Hex value");
                    mEdtCode.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(typeStr) || !checkHexValue(typeStr)) {
                    showToast("code should be Hex value");
                    mEdtType.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(maskStartStr)) {
                    showToast("MaskStart shouldn't be empty");
                    mEdtMaskStart.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(maskEndStr)) {
                    showToast("MaskEnd shouldn't be empty");
                    mEdtMaskEnd.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(maskCharStr)) {
                    showToast("MaskChar shouldn't be empty");
                    mEdtMaskChar.requestFocus();
                    return;
                }
                vanCode = Integer.parseInt(codeStr, 16);
                vanCardType = Integer.parseInt(typeStr, 16);
                maskStart = Integer.parseInt(maskStartStr);
                maskEnd = Integer.parseInt(maskEndStr);
                maskChar = maskCharStr.charAt(0);
                checkCard();
                checkingCard = true;
                mBtnCheckCard.setText(R.string.card_stop_check_card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Check card */
    private void checkCard() {
        try {
            Bundle bundle = new Bundle();
            bundle.putInt("cardType", AidlConstants.CardType.MAGNETIC.getValue());
            bundle.putInt("ctrCode", 0);//ctrCode only useful for IC/NFC card
            bundle.putInt("code", vanCode);
            bundle.putInt("type", vanCardType);
            bundle.putInt("maskStart", maskStart);
            bundle.putInt("maskEnd", maskEnd);
            bundle.putChar("maskChar", maskChar);
            bundle.putInt("stopOnError", 0);//Whether stop check card or not when checkCard occurred error，0-not stop，1-stop
            addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCardForToss(bundle, mCheckCardCallback, 60);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {
        /**
         * Find magnetic card
         *
         * @param info return data，contain the following keys:
         *             <br/>cardType: card type (int)
         *             <br/>TRACK1: track 1 data (String)
         *             <br/>TRACK2: track 2 data (String)
         *             <br/>TRACK3: track 3 data (String)
         *             <br/>track1ErrorCode: track 1 error code (int)
         *             <br/>track2ErrorCode: track 2 error code (int)
         *             <br/>track3ErrorCode: track 3 error code (int)
         *             <br/> track error code is one of the following values:
         *             <ul>
         *             <li>0 - No error</li>
         *             <li>-1 - Track has no data</li>
         *             <li>-2 - Track parity check error</li>
         *             <li>-3 - Track LRC check error</li>
         *             </ul>
         */
        @Override
        public void findMagCard(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findMagCard,bundle:" + Utility.bundle2String(info));
            handleResult(info);
            showSpendTime();
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findICCard,atr:" + atr);
            showSpendTime();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findRFCard,uuid:" + uuid);
            showSpendTime();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            addEndTime("checkCard()");
            String error = "onError:" + message + " -- " + code;
            LogUtil.e(Constant.TAG, error);
            showToast(error);
            handleResult(null);
            showSpendTime();
        }
    };

    private void handleResult(Bundle bundle) {
        if (isFinishing()) {
            return;
        }
        handler.post(() -> {
            if (bundle == null) {
                showResult(false, "", "", "");
                return;
            }
            //US-ASCII encoding string, encrypted data
            String track2 = Utility.null2String(bundle.getString("TRACK2"));
            //convert track2 to hex string
            track2 = ByteUtil.bytes2HexStr(track2.getBytes(StandardCharsets.US_ASCII));
            String pan = Utility.null2String(bundle.getString("pan"));
            String serviceCode = Utility.null2String(bundle.getString("servicecode"));
            //磁道错误码：0-无错误，-1-磁道无数据，-2-奇偶校验错，-3-LRC校验错
            int code2 = bundle.getInt("track2ErrorCode");
            LogUtil.e(TAG, String.format(Locale.getDefault(),
                    "track2ErrorCode:%d,track2:%s\npan:%s\nserviceCode:%s",
                    code2, track2, pan, serviceCode));
            showResult(code2 == 0, track2, pan, serviceCode);
            // 继续检卡
            if (!isFinishing()) {
                handler.postDelayed(this::checkCard, 500);
            }
        });
    }

    private void showResult(boolean success, String track2, String pan, String serviceCode) {
        mTotalTime += 1;
        if (success) {
            mSuccessTime += 1;
        } else {
            mFailTime += 1;
        }
        mTvTrack2.setText(track2);
        mTvServiceCode.setText(serviceCode);
        mTvPan.setText(pan);

        String temp = getString(R.string.card_total) + " " + mTotalTime;
        mBtnTotal.setText(temp);
        temp = getString(R.string.card_success) + " " + mSuccessTime;
        mBtnSuccess.setText(temp);
        temp = getString(R.string.card_fail) + " " + mFailTime;
        mBtnFail.setText(temp);
    }

    private boolean checkHexValue(String src) {
        return Pattern.matches("[0-9a-fA-F]+", src);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
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
