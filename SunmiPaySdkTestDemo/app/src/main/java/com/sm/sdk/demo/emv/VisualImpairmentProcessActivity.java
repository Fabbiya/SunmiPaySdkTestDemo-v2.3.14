package com.sm.sdk.demo.emv;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.DeviceUtil;
import com.sm.sdk.demo.utils.IOUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.SettingUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sm.sdk.demo.view.LoadingDialog;
import com.sm.sdk.demo.wrapper.PinPadListenerV2Wrapper;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidl.AidlConstants.PinBlockFormat;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadOptV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2;
import com.sunmi.pay.hardware.aidlv2.security.SecurityOptV2;

import java.nio.charset.StandardCharsets;

/**
 * 本页面测试支持普通EMV交易流程和RNIB认证键盘下的交易流程
 * <li>普通EMV交易流程走EMV</li>
 * <li>RNIB认证键盘下的交易流程不走EMV，输PIN后直接显示成功/失败</li>
 * <p>
 * 测试数据
 * PAN: 4336680005639972 (不用显示在界面上)
 * PIN: 318965
 * PINKEY: 3D9FEBC6CEE51A9B59F3E289BFA75460
 * PINBLOCK FORMAT: 0
 * PINBLOCK: 0F403A210DBB7061
 */
public class VisualImpairmentProcessActivity extends BaseAppCompatActivity {
    private final int PIK_INDEX = 12;
    private LoadingDialog loadDlg;
    private PinPadOptV2 mPinPadOptV2;
    private ReadCardOptV2 mReadCardOptV2;
    private SecurityOptV2 mSecurityOptV2;

    private TextView tvAmount;
    private EditText mEditAmount;
    private TextView mTvShowInfo;
    private Button mBtnOperate;
    private SwitchMaterial mSwitch;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emv_blind_pinpad);
        initData();
        initView();
    }

    /**
     * Do essential initialization, client App should
     * initialize their own data at this step.
     * Note: this method just show how to initialize data before
     * the emv process, the initialized data may not useful for your
     * App. Please init your own data.
     */
    private void initData() {
        mPinPadOptV2 = MyApplication.app.pinPadOptV2;
        mReadCardOptV2 = MyApplication.app.readCardOptV2;
        mSecurityOptV2 = MyApplication.app.securityOptV2;
        // disable check card buzzer
        SettingUtil.setBuzzerEnable(false);
        savePIK();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.emv_visual_impairment_process);
        toolbar.setNavigationIcon(R.drawable.ic_back_white);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setVisibility(View.GONE);
        mSwitch = findViewById(R.id.sw_switch);
        tvAmount = findViewById(R.id.tv_amount);
        mEditAmount = findViewById(R.id.edit_amount);
        mTvShowInfo = findViewById(R.id.tv_info);
        mBtnOperate = findViewById(R.id.mb_ok);
        mBtnOperate.setOnClickListener(this);
        //设置字体大小
        TextView tvEye = findViewById(R.id.tv_eye);
        if (DeviceUtil.isP2Se()) {
            tvEye.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
            tvEye.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            tvEye.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 27);
            tvEye.setTypeface(Typeface.DEFAULT_BOLD);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_ok:
                LogUtil.e(Constant.TAG, "***************************************************************");
                LogUtil.e(Constant.TAG, "****************************Start Process**********************");
                LogUtil.e(Constant.TAG, "***************************************************************");
                mTvShowInfo.setText("");
                String amount = mEditAmount.getText().toString();
                try {
                    // Before check card, initialize emv process(clear all TLV)
                    if (checkAmount(amount)) {
                        playTransInstruction(amount);
                        checkCard();
                    } else {
                        showLoadingDlg("Please input correct amount", 2000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /** check input amount */
    private boolean checkAmount(String amount) {
        if (TextUtils.isEmpty(amount)) {
            return false;
        }
        String[] values = amount.split("\\.");
        return values.length <= 2 && Long.parseLong(values[0]) > 0;
    }

    /** Save PIK */
    private void savePIK() {
        try {
            // save PIK
            byte[] dataByte = ByteUtil.hexStr2Bytes("3D9FEBC6CEE51A9B59F3E289BFA75460");
            int result = mSecurityOptV2.savePlaintextKey(Security.KEY_TYPE_PIK, dataByte, null, Security.KEY_ALG_TYPE_3DES, PIK_INDEX);
            LogUtil.e(Constant.TAG, "save PIK result:" + result);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Play keyboard instruction */
    private void playTransInstruction(String amount) {
        if (mSwitch.isChecked()) {
            String amountStr = null;
            String[] values = amount.split("\\.");
            int pounds = Integer.parseInt(values[0]);
            if (values.length == 1) {
                amountStr = Utility.formatStr("Total amount is %d pounds.", pounds);
            } else {
                int pence = Integer.parseInt(values[1]);
                amountStr = Utility.formatStr("Total amount is %d pounds, %d pence.", pounds, pence);
            }
            String cardPositionTip = "right of the device in the middle";
            if (DeviceUtil.isP2liteSE() || DeviceUtil.isP2Mini()) {
                cardPositionTip = "bottom of the device";
            }
            final String instructionFormat = "%s Please listen to these instructions or to interrupt and pay. "
                    + "Please swipe, present, or insert your card,"
                    + " at the %s"
                    + " PIN Pad is located on the bottom half of the screen. "
                    + "Layout is standard telephone layout with 1, 2, 3 at top and Cancel, Clear and Enter at the bottom. If you are too high on the screen the device will tell you Pin Pad below. "
                    + "The numbers are not spoken but the buttons at the bottom speak the words Cancel, Clear all digits and Enter. Slide your finger onto the screen and use the beeps to find the right digit."
                    + "Lift your finger and double tap anywhere on the screen to enter it. The device will say First digit entered. Once you have input all digits find the Enter button at the bottom right of the screen and double tap anywhere on the screen to confirm. "
                    + "The cancel button at the bottom left will cancel the transaction and the Clear button at the bottom in the middle will clear all digits."
                    + " Please swipe, present, or insert your card at the %s to start the transaction.";
            EmvTTS.getInstance().play(Utility.formatStr(instructionFormat, amountStr, cardPositionTip, cardPositionTip));
        }
    }

    /** Start check card */
    private void checkCard() {
        try {
            hideSoftInput();
            showLoadingDlg(R.string.emv_swing_card_ic);
            int cardType = CardType.NFC.getValue() | CardType.IC.getValue();
            addStartTime("checkCard()");
            mReadCardOptV2.checkCard(cardType, mCheckCardCallback, 120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Check card callback */
    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {
        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findMagCard:" + bundle);
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findICCard:" + atr);
            //IC card Beep buzzer when check card success
            MyApplication.app.basicOptV2.buzzerOnDevice(1, 2750, 200, 0);
            dismissLoadingDlg();
            if (mSwitch.isChecked()) {
                stopSpeaking();
            }
            initPinPad();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findRFCard:" + uuid);
            dismissLoadingDlg();
            if (mSwitch.isChecked()) {
                stopSpeaking();
            }
            initPinPad();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            addEndTime("checkCard()");
            String error = "onError:" + message + " -- " + code;
            LogUtil.e(Constant.TAG, error);
            showToast(error);
            stopSpeaking();
            dismissLoadingDlg();
        }
    };

    /** Start show PinPad */
    private void initPinPad() {
        LogUtil.e(Constant.TAG, "initPinPad");
        try {
            String cardNo = "4336680005639972";
            if (mSwitch.isChecked()) {//视障模式
                //设置键盘样式为RNIB认证的样式
                Bundle bundle = new Bundle();
                bundle.putInt("timeoutGap1", 5);
                bundle.putInt("timeoutGap2", 10);
                bundle.putInt("ttsLanguage", 0);
                mPinPadOptV2.setVisualImpairmentModeParam(bundle);
            }
            Bundle bundle = new Bundle();
            bundle.putInt("pinPadType", mSwitch.isChecked() ? 3 : 4);
            // PinType: 0-online PIN, 1-offline PIN
            bundle.putInt("pinType", 0);
            // isOrderNumberKey: true-order number PinPad, false-disorder number PinPad
            bundle.putInt("isOrderNumKey", 1);
            byte[] panBytes = cardNo.substring(cardNo.length() - 13, cardNo.length() - 1).getBytes(StandardCharsets.US_ASCII);
            bundle.putByteArray("pan", panBytes);
            // PIK(PIN key) index
            bundle.putInt("pinKeyIndex", PIK_INDEX);
            // Minimum input PIN number
            bundle.putInt("minInput", 0);
            // Maximum input number(Max value is 12)
            bundle.putInt("maxInput", 12);
            // The input step if input PIN, default 1
            bundle.putInt("inputStep", 1);
            // Input PIN timeout time
            bundle.putInt("timeout", mSwitch.isChecked() ? 5 * 60 * 1000 : 60 * 1000);
            // is support bypass PIN, 0-not support, 1-support
            bundle.putInt("isSupportbypass", 1);
            // PIN block format
            bundle.putInt("pinblockFormat", PinBlockFormat.SEC_PIN_BLK_ISO_FMT0);
            // PinAlgType: 0-3DES, 1-SM4, 2-AES
            bundle.putInt("algorithmType", 0);
            // PIK key system: 0-MKSK, 1-Dukpt
            bundle.putInt("keySystem", 0);
            //pinpad ui style, 0-normal-default, 1-normal-rnibAuth, 2-visualImpairment-default, 3-visualImpairment-rnibAuth
            bundle.putInt("uiStyle", mSwitch.isChecked() ? 3 : 1);
            MyApplication.app.pinPadOptV2.initPinPadEx(bundle, mPinPadListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Input pin callback */
    private final PinPadListenerV2 mPinPadListener = new PinPadListenerV2Wrapper() {

        @Override
        public void onPinLength(int len) {
            LogUtil.e(Constant.TAG, "onPinLength(), len:" + len);
        }

        @Override
        public void onConfirm(int pinType, byte[] pinBlock) {
            addEndTime("initPinPad()");
            String hexStr = ByteUtil.bytes2HexStr(pinBlock);
            LogUtil.e(Constant.TAG, "onConfirm(), pinType:" + pinType + ",pinBlock:" + hexStr);
            if ("0F403A210DBB7061".equals(hexStr)) {//PIN正确
                LogUtil.e(TAG, "PIN correct");
                importPinInputStatus(0);
            } else {//PIN错误
                LogUtil.e(TAG, "PIN incorrect");
                importPinInputStatus(3);
            }
        }

        @Override
        public void onCancel() {
            addEndTime("initPinPad()");
            LogUtil.e(Constant.TAG, "onCancel()");
            importPinInputStatus(1);
        }

        @Override
        public void onError(int code) {
            addEndTime("initPinPad()");
            LogUtil.e(Constant.TAG, "onError(), code:" + code);
            String msg = AidlErrorCodeV2.valueOf(code).getMsg();
        }
    };

    /**
     * Notify emv process the PIN input result
     *
     * @param inputResult 0:success,1:input PIN canceled,2:input PIN skipped,3:PINPAD problem,4:input PIN timeout
     */
    private void importPinInputStatus(int inputResult) {
        LogUtil.e(Constant.TAG, "importPinInputStatus:" + inputResult);
        final int delayTime = 3000;
        try {
            showLoadingDlg(R.string.requesting);
            IOUtil.delay(delayTime);
            String tip = null;
            if (inputResult == 0) {//成功
                tip = "transaction success";
            } else if (inputResult == 1) {//交易取消
                tip = "transaction Cancelled";
            } else if (inputResult == 2 || inputResult == 4) {//
                tip = "transaction refused";
            } else if (inputResult == 3) {//PIN错误
                tip = "PIN Error, please re-enter your PIN";
            }
            showLoadingDlg(tip, delayTime);
            if (mSwitch.isChecked()) {
                EmvTTS.getInstance().play(tip);
            }
            if (inputResult == 3) {
                handler.postDelayed(() -> initPinPad(), delayTime);
            } else {
                resetUI(delayTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetUI(int delay) {
        handler.postDelayed(() -> {
            mEditAmount.setText("");
            mBtnOperate.setText(R.string.ok);
            mSwitch.setChecked(false);
            dismissLoadingDlg();
        }, delay);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EmvTTS.getInstance().stop();
        cancelCheckCard();
        SettingUtil.setBuzzerEnable(true);
        SettingUtil.setEmvMaxInputPinTime(60);
    }

    private void stopSpeaking() {
        if (mSwitch.isChecked() && EmvTTS.getInstance().isSpeaking()) {
            EmvTTS.getInstance().stop();
        }
    }

    private void cancelCheckCard() {
        try {
            mReadCardOptV2.cardOff(CardType.NFC.getValue());
            mReadCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLoadingDlg(int resId) {
        showLoadingDlg(getString(resId), 0);
    }

    private void showLoadingDlg(int resId, int time) {
        showLoadingDlg(getString(resId), time);
    }

    private void showLoadingDlg(String msg, int time) {
        handler.post(() -> {
            if (loadDlg == null) {
                loadDlg = new LoadingDialog(this, msg);
            } else {
                loadDlg.setMessage(msg);
            }
            loadDlg.setWidthHeight(getScreenWidth(), Utility.dp2px(330));
            loadDlg.setTextSize(50);
            loadDlg.setTextStyle(Typeface.DEFAULT_BOLD);
            if (!loadDlg.isShowing()) {
                loadDlg.show();
            }
            if (time == 0) {//进度dialog
                loadDlg.setProgressbarVisibility(View.VISIBLE);
            } else {//结果dialog
                loadDlg.setProgressbarVisibility(View.GONE);
                handler.postDelayed(() -> loadDlg.dismiss(), time);
            }
        });
    }

    private void dismissLoadingDlg() {
        handler.post(() -> {
            if (loadDlg != null && loadDlg.isShowing()) {
                loadDlg.dismiss();
            }
        });
    }

    /** 隐藏输入法键盘，解决检卡后立即显示键盘，输入法键盘导致Pinpad按键坐标错位问题 */
    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditAmount.getWindowToken(), 0); //强制隐藏键盘
    }

    /** 获取屏幕宽度 */
    private int getScreenWidth() {
        DisplayMetrics dm = new DisplayMetrics();//屏幕度量
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;//宽度
    }
}
