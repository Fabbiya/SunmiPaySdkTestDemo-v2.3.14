package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.SettingUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

public class ICActivity extends BaseAppCompatActivity {
    private TextView tvDepictor;
    private TextView tvAtr;
    private Button tvTotal;
    private Button tvSuccess;
    private Button tvFail;
    private int totalCount;
    private int successCount;
    private int failCount;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_ic);
        initView();
        checkCard();
    }

    private void initView() {
        SettingUtil.setBuzzerEnable(true);
        initToolbarBringBack(R.string.card_test_ic);
        tvTotal = findViewById(R.id.mb_total);
        tvSuccess = findViewById(R.id.mb_success);
        tvFail = findViewById(R.id.mb_fail);
        tvDepictor = findViewById(R.id.tv_depictor);
        tvAtr = findViewById(R.id.tv_atr);
    }

    private void checkCard() {
        try {
            int cardType = CardType.IC.getValue();
            addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCard(cardType, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findMagCard:" + Utility.bundle2String(info));
            showSpendTime();
        }

        /**
         * Find IC card
         *
         * @param info return data，contain the following keys:
         *             <br/>cardType: card type (int)
         *             <br/>atr: card's ATR (String)
         */
        @Override
        public void findICCardEx(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findICCard:" + Utility.bundle2String(info));
            handleResult(true, info);
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
         */
        @Override
        public void findRFCardEx(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findRFCard:" + Utility.bundle2String(info));
            showSpendTime();
        }

        /**
         * Check card error
         *
         * @param info return data，contain the following keys:
         *             <br/>cardType: card type (int)
         *             <br/>code: the error code (String)
         *             <br/>message: the error message (String)
         */
        @Override
        public void onErrorEx(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            int code = info.getInt("code");
            String msg = info.getString("message");
            String error = "onError:" + msg + " -- " + code;
            LogUtil.e(Constant.TAG, error);
            showToast(error);
            handleResult(false, info);
            showSpendTime();
        }
    };

    /**
     * Show check card result
     *
     * @param success true-成功，false-失败
     * @param info    The info returned by check card
     */
    private void handleResult(boolean success, Bundle info) {
        if (isFinishing()) {
            return;
        }
        handler.post(() -> {
            totalCount++;
            if (success) {// find IC
                successCount++;
                tvDepictor.setText(getString(R.string.card_check_ic_card));
                tvAtr.setText(info.getString("atr"));
            } else {//on Error
                failCount++;
                tvDepictor.setText(getString(R.string.card_check_card_error));
            }
            tvTotal.setText(Utility.formatStr("%s %d", getString(R.string.card_total), totalCount));
            tvSuccess.setText(Utility.formatStr("%s %d", getString(R.string.card_success), successCount));
            tvFail.setText(Utility.formatStr("%s %d", getString(R.string.card_fail), failCount));
            // 继续检卡
            if (!isFinishing()) {
                handler.postDelayed(this::checkCard, 500);
            }
        });
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        cancelCheckCard();
        super.onDestroy();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.app.readCardOptV2.cardOff(CardType.IC.getValue());
            MyApplication.app.readCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
