package com.sm.sdk.demo.card;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Locale;

public class MagActivity extends BaseAppCompatActivity {
    private MaterialButton mBtnTotal;
    private MaterialButton mBtnSuccess;
    private MaterialButton mBtnFail;
    private TextView mTvTrack1;
    private TextView mTvTrack2;
    private TextView mTvTrack3;
    private TextView mTrack1Preset;
    private TextView mTrack2Preset;
    private TextView mTrack3Preset;
    private int mTotalTime;
    private int mSuccessTime;
    private int mFailTime;
    private String presetTrack1;
    private String presetTrack2;
    private String presetTrack3;

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_mag);
        initView();
        checkCard();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_mag);

        mBtnTotal = findViewById(R.id.mb_total);
        mBtnSuccess = findViewById(R.id.mb_success);
        mBtnFail = findViewById(R.id.mb_fail);
        mTvTrack1 = findViewById(R.id.tv_track1);
        mTvTrack2 = findViewById(R.id.tv_track2);
        mTvTrack3 = findViewById(R.id.tv_track3);
        mTrack1Preset = findViewById(R.id.tv_track1_preset);
        mTrack2Preset = findViewById(R.id.tv_track2_preset);
        mTrack3Preset = findViewById(R.id.tv_track3_preset);
        findViewById(R.id.mb_preset).setOnClickListener(v -> {
            cancelCheckCard();
            openActivityForResult(MagPresetTrackActivity.class, 1000);
        });
    }

    /** start check card */
    private void checkCard() {
        try {
            addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCard(AidlConstantsV2.CardType.MAGNETIC.getValue(), mCheckCardCallback, 60);
        } catch (Exception e) {
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
            String track1 = Utility.null2String(bundle.getString("TRACK1"));
            String track2 = Utility.null2String(bundle.getString("TRACK2"));
            String track3 = Utility.null2String(bundle.getString("TRACK3"));
            //磁道错误码：0-无错误，-1-磁道无数据，-2-奇偶校验错，-3-LRC校验错
            int code1 = bundle.getInt("track1ErrorCode");
            int code2 = bundle.getInt("track2ErrorCode");
            int code3 = bundle.getInt("track3ErrorCode");
            LogUtil.e(TAG, String.format(Locale.getDefault(),
                    "track1ErrorCode:%d,track1:%s\ntrack2ErrorCode:%d,track2:%s\ntrack3ErrorCode:%d,track3:%s",
                    code1, track1, code2, track2, code3, track3));
            if ((code1 != 0 && code1 != -1) || (code2 != 0 && code2 != -1) || (code3 != 0 && code3 != -1)) {
                showResult(false, track1, track2, track3);
            } else if ((!TextUtils.isEmpty(presetTrack1) && !presetTrack1.equals(track1))
                    || (!TextUtils.isEmpty(presetTrack2) && !presetTrack2.equals(track2))
                    || (!TextUtils.isEmpty(presetTrack3) && !presetTrack3.equals(track3))) {
                showResult(false, track1, track2, track3);
            } else {
                showResult(true, track1, track2, track3);
            }
            // 继续检卡
            if (!isFinishing()) {
                handler.postDelayed(this::checkCard, 500);
            }
        });
    }

    private void showResult(boolean success, String track1, String track2, String track3) {
        mTotalTime += 1;
        if (success) {
            mSuccessTime += 1;
        } else {
            mFailTime += 1;
        }
        mTvTrack1.setText(track1);
        mTvTrack2.setText(track2);
        mTvTrack3.setText(track3);

        String temp = getString(R.string.card_total) + " " + mTotalTime;
        mBtnTotal.setText(temp);
        temp = getString(R.string.card_success) + " " + mSuccessTime;
        mBtnSuccess.setText(temp);
        temp = getString(R.string.card_fail) + " " + mFailTime;
        mBtnFail.setText(temp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && data != null && data.getExtras() != null) {
            presetTrack1 = data.getStringExtra("extra_track1");
            presetTrack2 = data.getStringExtra("extra_track2");
            presetTrack3 = data.getStringExtra("extra_track3");
            mTrack1Preset.setText(presetTrack1);
            mTrack2Preset.setText(presetTrack2);
            mTrack3Preset.setText(presetTrack3);
        }
        handler.postDelayed(this::checkCard, 500);
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
