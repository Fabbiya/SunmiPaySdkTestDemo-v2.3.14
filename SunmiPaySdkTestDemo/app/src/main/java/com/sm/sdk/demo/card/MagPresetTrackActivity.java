package com.sm.sdk.demo.card;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.widget.EditText;

import androidx.annotation.Nullable;

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

public class MagPresetTrackActivity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;
    private EditText edtTrack1;
    private EditText edtTrack2;
    private EditText edtTrack3;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_mag_preset_track);
        initView();
        checkCard();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_preset_track_data);
        edtTrack1 = findViewById(R.id.edt_preset_track1);
        edtTrack2 = findViewById(R.id.edt_preset_track2);
        edtTrack3 = findViewById(R.id.edt_preset_track3);
        findViewById(R.id.mb_ok).setOnClickListener(v -> {
            cancelCheckCard();
            Intent intent = new Intent();
            intent.putExtra("extra_track1", edtTrack1.getText().toString());
            intent.putExtra("extra_track2", edtTrack2.getText().toString());
            intent.putExtra("extra_track3", edtTrack3.getText().toString());
            setResult(1000, intent);
            finish();
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
        if (success) {
            edtTrack1.setText(track1);
            edtTrack2.setText(track2);
            edtTrack3.setText(track3);
        } else {
            showToast("check card failed");
        }
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
