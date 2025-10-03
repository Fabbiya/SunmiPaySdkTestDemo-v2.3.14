package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.DeviceUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.SettingUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class CompositeActivity extends BaseAppCompatActivity {
    private TextView tvDepictor;
    private Button tvTotal;
    private Button tvSuccess;
    private Button tvFail;
    private Button btnCheckCard;
    private int totalCount;
    private int successCount;
    private int failCount;
    private boolean checkingCard;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_composite);
        initView();
    }

    private void initView() {
        SettingUtil.setBuzzerEnable(true);
        initToolbarBringBack(R.string.card_test_mag_ic_nfc);
        tvTotal = findViewById(R.id.mb_total);
        tvSuccess = findViewById(R.id.mb_success);
        tvFail = findViewById(R.id.mb_fail);
        tvDepictor = findViewById(R.id.tv_depictor);
        btnCheckCard = findViewById(R.id.mb_check_card);
        btnCheckCard.setOnClickListener((v) -> switchCheckCard());
    }

    private void switchCheckCard() {
        try {
            if (checkingCard) {
                MyApplication.app.readCardOptV2.cancelCheckCard();
                btnCheckCard.setText(R.string.card_start_check_card);
                checkingCard = false;
            } else {
                checkCard();
                checkingCard = true;
                btnCheckCard.setText(R.string.card_stop_check_card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkCard() {
        try {
            int cardType = CardType.MAGNETIC.getValue() | CardType.IC.getValue() | CardType.NFC.getValue();
            addStartTimeWithClear("checkCard()");
            if (DeviceUtil.isTossTerminal()) {
                cardType &= ~CardType.NFC.getValue();
            }
            if (DeviceUtil.isTossTerminal() || DeviceUtil.isTossFront()) {
                Bundle bundle = new Bundle();
                bundle.putInt("cardType", cardType);
                bundle.putInt("ctrCode", 0);
                bundle.putInt("code", 1);
                bundle.putInt("type", 2);
                bundle.putInt("maskStart", 3);
                bundle.putInt("maskEnd", 4);
                bundle.putChar("maskChar", '*');
                bundle.putInt("stopOnError", 0);
                MyApplication.app.readCardOptV2.checkCardForToss(bundle, mCheckCardCallback, 60);
            } else {
                MyApplication.app.readCardOptV2.checkCard(cardType, mCheckCardCallback, 60);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle info) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findMagCard:" + Utility.bundle2String(info));
            handleResult(0, true, info);
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
            handleResult(1, true, info);
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
            handleResult(2, true, info);
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
            handleResult(-1, false, info);
            showSpendTime();
        }
    };

    /**
     * Show check card result
     *
     * @param type    类型，0-此条，1-IC，2-NFC
     * @param success true-成功，false-失败
     * @param info    The info returned by check card
     */
    private void handleResult(int type, boolean success, Bundle info) {
        if (isFinishing()) {
            return;
        }
        runOnUiThread(() -> {
            if (type == 0) {//磁卡
                if (DeviceUtil.isTossTerminal() || DeviceUtil.isTossFront()) {
                    handleTossMagCard(success, info);
                } else {
                    handleNormalMagCard(success, info);
                }
            } else if (type == 1) {
                handleICCard(success, info);
            } else if (type == 2) {
                handleNfcCard(success, info);
            }
            switchCheckCard();
        });
    }

    private void handleTossMagCard(boolean success, Bundle info) {
        totalCount++;
        if (success) {
            //US-ASCII encoding string, encrypted data
            String track2 = Utility.null2String(info.getString("TRACK2"));
            //convert track2 to hex string
            track2 = ByteUtil.bytes2HexStr(track2.getBytes(StandardCharsets.US_ASCII));
            String pan = Utility.null2String(info.getString("pan"));
            String serviceCode = Utility.null2String(info.getString("servicecode"));
            //磁道错误码：0-无错误，-1-磁道无数据，-2-奇偶校验错，-3-LRC校验错
            int code2 = info.getInt("track2ErrorCode");
            LogUtil.e(TAG, String.format(Locale.getDefault(),
                    "track2ErrorCode:%d,track2:%s\npan:%s\nserviceCode:%s",
                    code2, track2, pan, serviceCode));
            StringBuilder sb = new StringBuilder()
                    .append("find MagStripe card\n")
                    .append("track2:").append(track2).append("\n")
                    .append("serviceCode:").append(serviceCode).append("\n")
                    .append("pan:").append(pan);
            if (code2 == 0) {
                successCount++;
            } else {
                failCount++;
            }
            tvDepictor.setText(sb);
        } else {
            failCount++;
        }
        tvTotal.setText(Utility.formatStr("%s %d", getString(R.string.card_total), totalCount));
        tvSuccess.setText(Utility.formatStr("%s %d", getString(R.string.card_success), successCount));
        tvFail.setText(Utility.formatStr("%s %d", getString(R.string.card_fail), failCount));
    }

    private void handleNormalMagCard(boolean success, Bundle info) {
        totalCount++;
        if (success) {
            String track1 = Utility.null2String(info.getString("TRACK1"));
            String track2 = Utility.null2String(info.getString("TRACK2"));
            String track3 = Utility.null2String(info.getString("TRACK3"));
            //磁道错误码：0-无错误，-1-磁道无数据，-2-奇偶校验错，-3-LRC校验错
            int code1 = info.getInt("track1ErrorCode");
            int code2 = info.getInt("track2ErrorCode");
            int code3 = info.getInt("track3ErrorCode");
            LogUtil.e(TAG, String.format(Locale.getDefault(),
                    "track1ErrorCode:%d,track1:%s\ntrack2ErrorCode:%d,track2:%s\ntrack3ErrorCode:%d,track3:%s",
                    code1, track1, code2, track2, code3, track3));
            if ((code1 != 0 && code1 != -1) || (code2 != 0 && code2 != -1) || (code3 != 0 && code3 != -1)) {
                failCount++;
            } else {
                successCount++;
            }
            StringBuilder sb = new StringBuilder()
                    .append(getString(R.string.card_check_mag_card)).append("\n")
                    .append("track1:").append(track1).append("\n")
                    .append("track2:").append(track2).append("\n")
                    .append("track3:").append(track3).append("\n");
            tvDepictor.setText(sb);
        } else {
            failCount++;
        }
        tvTotal.setText(Utility.formatStr("%s %d", getString(R.string.card_total), totalCount));
        tvSuccess.setText(Utility.formatStr("%s %d", getString(R.string.card_success), successCount));
        tvFail.setText(Utility.formatStr("%s %d", getString(R.string.card_fail), failCount));
    }

    private void handleICCard(boolean success, Bundle info) {
        totalCount++;
        if (success) {
            successCount++;
            StringBuilder sb = new StringBuilder();
            sb.append(getString(R.string.card_check_ic_card)).append("\n")
                    .append("ATR:").append(info.getString("atr")).append("\n");
            tvDepictor.setText(sb);
        } else {
            failCount++;
        }
        tvTotal.setText(Utility.formatStr("%s %d", getString(R.string.card_total), totalCount));
        tvSuccess.setText(Utility.formatStr("%s %d", getString(R.string.card_success), successCount));
        tvFail.setText(Utility.formatStr("%s %d", getString(R.string.card_fail), failCount));
    }

    private void handleNfcCard(boolean success, Bundle info) {
        totalCount++;
        if (success) {
            successCount++;
            StringBuilder sb = new StringBuilder();
            sb.append(getString(R.string.card_check_rf_card)).append("\n")
                    .append("uuid:").append(info.getString("uuid")).append("\n")
                    .append("ats:").append(info.getString("ats")).append("\n");
            tvDepictor.setText(sb);
        } else {
            failCount++;
        }
        tvTotal.setText(Utility.formatStr("%s %d", getString(R.string.card_total), totalCount));
        tvSuccess.setText(Utility.formatStr("%s %d", getString(R.string.card_success), successCount));
        tvFail.setText(Utility.formatStr("%s %d", getString(R.string.card_fail), failCount));
    }

    @Override
    protected void onDestroy() {
        cancelCheckCard();
        super.onDestroy();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.app.readCardOptV2.cardOff(CardType.IC.getValue());
            MyApplication.app.readCardOptV2.cardOff(CardType.NFC.getValue());
            MyApplication.app.readCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
