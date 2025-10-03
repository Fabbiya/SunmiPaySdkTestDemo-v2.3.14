package com.sm.sdk.demo.pin;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.emv.EmvUtil;
import com.sm.sdk.demo.security.KeyIndexUtil;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sm.sdk.demo.wrapper.PinPadListenerV2Wrapper;
import com.sunmi.pay.hardware.aidl.AidlConstants.PinBlockFormat;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadConfigV2;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadTextConfigV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class PinPadActivity extends BaseAppCompatActivity {
    private EditText txtConfirm;
    private EditText txtInputPin;
    private EditText txtInputOfflinePin;
    private EditText txtReinputOfflinePinFormat;

    private String cardNo;
    private EditText mEditCardNo;
    private EditText mEditTimeout;
    private EditText mEditKeyIndex;

    private TextView mTvPinBlock;
    private TextView mTvKSN;

    private RadioGroup mRGKeyboard;
    private RadioGroup mRGIsOnline;
    private RadioGroup mRGKeyboardStyle;
    private RadioGroup mRGPikKeySystem;
    private RadioGroup mRGPinAlgorithmType;
    private SparseArray<CheckBox> chkModeList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_pad);
        initToolbarBringBack(R.string.pin_pad);
        initView();
        EmvUtil.setTerminalParam(EmvUtil.getConfig(EmvUtil.COUNTRY_CHINA));
    }

    private void initView() {
        RadioGroup rdoGroup = findViewById(R.id.rg_pin_style);
        rdoGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_pin_style_normal) {
                findViewById(R.id.input_step_lay).setVisibility(View.GONE);
                findViewById(R.id.edit_input_step).setVisibility(View.GONE);
                findViewById(R.id.input_diversify_lay).setVisibility(View.GONE);
                findViewById(R.id.edit_diversify).setVisibility(View.GONE);
            } else if (checkedId == R.id.rb_pin_style_normal_extend) {
                findViewById(R.id.input_step_lay).setVisibility(View.VISIBLE);
                findViewById(R.id.edit_input_step).setVisibility(View.VISIBLE);
                findViewById(R.id.input_diversify_lay).setVisibility(View.VISIBLE);
                findViewById(R.id.edit_diversify).setVisibility(View.GONE);
            }
        });
        rdoGroup.check(R.id.rb_pin_style_normal);
        chkModeList = new SparseArray<>();
        chkModeList.put(R.id.chk_mode_normal, findViewById(R.id.chk_mode_normal));
        chkModeList.put(R.id.rdo_mode_vi, findViewById(R.id.rdo_mode_vi));
        chkModeList.put(R.id.chk_mode_long_press_to_clear, findViewById(R.id.chk_mode_long_press_to_clear));
        chkModeList.put(R.id.chk_mode_silent, findViewById(R.id.chk_mode_silent));
        chkModeList.put(R.id.chk_mode_green_led, findViewById(R.id.chk_mode_green_led));
        chkModeList.put(R.id.chk_mode_monitor_clear_key, findViewById(R.id.chk_mode_monitor_clear_key));
        chkModeList.put(R.id.chk_mode_cancel_to_clear, findViewById(R.id.chk_mode_cancel_to_clear));
        chkModeList.put(R.id.chk_mode_long_timeout_time, findViewById(R.id.chk_mode_long_timeout_time));
        for (int i = 0, size = chkModeList.size(); i < size; i++) {
            chkModeList.valueAt(i).setOnClickListener(this);
        }
        txtConfirm = findViewById(R.id.edit_txt_confirm);
        txtInputPin = findViewById(R.id.edit_txt_input_pin);
        txtInputOfflinePin = findViewById(R.id.edit_txt_input_offline_pin);
        txtReinputOfflinePinFormat = findViewById(R.id.edit_txt_reinput_offline_pin_fmt);
        mEditCardNo = findViewById(R.id.edit_card_no);
        mEditTimeout = findViewById(R.id.edit_timeout);
        mEditKeyIndex = findViewById(R.id.edit_key_index);

        mTvPinBlock = findViewById(R.id.tv_pinblock);
        mTvKSN = findViewById(R.id.tv_dukpt_ksn);

        mRGKeyboard = findViewById(R.id.rg_keyboard);
        mRGIsOnline = findViewById(R.id.rg_is_online);
        mRGKeyboardStyle = findViewById(R.id.rg_keyboard_style);
        mRGPikKeySystem = findViewById(R.id.key_system);
        mRGPinAlgorithmType = findViewById(R.id.pin_type);

        chkModeList.get(R.id.chk_mode_normal).setChecked(true);
        findViewById(R.id.mb_set_mode).setOnClickListener(this);
        findViewById(R.id.mb_set_text).setOnClickListener(this);
        findViewById(R.id.mb_ok).setOnClickListener(this);
        findViewById(R.id.call_custom_keyboard).setOnClickListener(this);
        mEditCardNo.setText("123456789123456");
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.chk_mode_normal:
            case R.id.rdo_mode_vi:
            case R.id.chk_mode_long_press_to_clear:
            case R.id.chk_mode_silent:
            case R.id.chk_mode_green_led:
            case R.id.chk_mode_cancel_to_clear:
            case R.id.chk_mode_long_timeout_time:
                onModeButtonClick(id);
                break;
            case R.id.mb_set_mode:
                onSetPinPadModeClick();
                setVisualImpairmentParam();
                getVisualImpairmentModeParam();
                break;
            case R.id.mb_set_text:
                initPinPadText();
                break;
            case R.id.mb_ok:
                onOKButtonClick();
                break;
            case R.id.call_custom_keyboard:
                initCustomPinPad();
                break;
        }
    }

    private void onOKButtonClick() {
        RadioGroup rdoGroup = findViewById(R.id.rg_pin_style);
        int checkedId = rdoGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_pin_style_normal) {
            initPinPad();
        } else if (checkedId == R.id.rb_pin_style_normal_extend) {
            initPinPadEx();
        }
    }

    private void onModeButtonClick(int id) {
        if (id == R.id.chk_mode_normal) {
            for (int i = 0, size = chkModeList.size(); i < size; i++) {
                if (chkModeList.keyAt(i) != id) {
                    chkModeList.valueAt(i).setChecked(false);
                }
            }
        } else {
            chkModeList.get(R.id.chk_mode_normal).setChecked(false);
        }
    }

    /**
     * Set PinPad mode
     * the set value just valid for next time inputting PIN, after input PIN finished,
     * the set value is lost effect
     */
    private void onSetPinPadModeClick() {
        try {
            Bundle bundle = new Bundle();
            if (chkModeList.get(R.id.chk_mode_normal).isChecked()) {//Normal mode
                bundle.putInt("normal", 1);
            }
            if (chkModeList.get(R.id.rdo_mode_vi).isChecked()) {
                bundle.putInt("visualImpairment", 1);
            }
            if (chkModeList.get(R.id.chk_mode_long_press_to_clear).isChecked()) {
                bundle.putInt("longPressToClear", 1);
            }
            if (chkModeList.get(R.id.chk_mode_silent).isChecked()) {
                bundle.putInt("silent", 1);
            }
            if (chkModeList.get(R.id.chk_mode_green_led).isChecked()) {
                bundle.putInt("greenLed", 1);
            }
            if (chkModeList.get(R.id.chk_mode_monitor_clear_key).isChecked()) {
                bundle.putInt("monitorClearKey", 1);
            }
            if (chkModeList.get(R.id.chk_mode_cancel_to_clear).isChecked()) {
                bundle.putInt("cancelToClear", 1);
            }
            if (chkModeList.get(R.id.chk_mode_long_timeout_time).isChecked()) {
                bundle.putInt("longTimeoutTime", 1);
            }
            addStartTimeWithClear("setPinPadMode()");
            int code = MyApplication.app.pinPadOptV2.setPinPadMode(bundle);
            addEndTime("setPinPadMode()");
            String msg = "Set PinPad mode " + (code == 0 ? "success" : "failed");
            LogUtil.e(TAG, msg);
            showToast(msg);
            showSpendTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 设置视障模式参数 */
    private void setVisualImpairmentParam() {
        boolean visualImpairment = chkModeList.get(R.id.rdo_mode_vi).isChecked();
        if (visualImpairment) {
            int rnibSelectMode = 0;
            int rnibHoldTime = 30;
            String edtSelectModeStr = this.<EditText>findViewById(R.id.edit_rnib_select_mode).getText().toString();
            if (!TextUtils.isEmpty(edtSelectModeStr)) {
                rnibSelectMode = Integer.parseInt(edtSelectModeStr);
            }
            String edtHoldTimeStr = this.<EditText>findViewById(R.id.edit_rnib_hold_time).getText().toString();
            if (!TextUtils.isEmpty(edtHoldTimeStr)) {
                rnibHoldTime = Integer.parseInt(edtHoldTimeStr);
            }
            try {
                Bundle bundle = new Bundle();
                bundle.putInt("timeoutGap1", 5);
                bundle.putInt("timeoutGap2", 10);
                bundle.putInt("ttsLanguage", 0);
                bundle.putInt("rnibSelectMode", rnibSelectMode);
                bundle.putInt("rnibHoldTime", rnibHoldTime);
                int code = MyApplication.app.pinPadOptV2.setVisualImpairmentModeParam(bundle);
                String msg = "setVisualImpairmentModeParam() code:" + code;
                LogUtil.e(TAG, msg);
                showToast(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** 获取视障模式参数 */
    private void getVisualImpairmentModeParam() {
        try {
            Bundle bundle = new Bundle();
            int code = MyApplication.app.pinPadOptV2.getVisualImpairmentModeParam(bundle);
            LogUtil.e(TAG, "getVisualImpairmentModeParam() code:" + code);
            if (code < 0) {
                return;
            }
            int timeoutGap1 = bundle.getInt("timeoutGap1");
            int timeoutGap2 = bundle.getInt("timeoutGap2");
            int ttsLanguage = bundle.getInt("ttsLanguage");
            int rnibSelectMode = bundle.getInt("rnibSelectMode");
            int rnibHoldTime = bundle.getInt("rnibHoldTime");
            LogUtil.e(TAG, "getVisualImpairmentModeParam():\n" + Utility.bundle2String(bundle));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Get current PinPad mode */
    private void getPinPadMode() {
        try {
            Bundle bundle = new Bundle();
            addStartTimeWithClear("getPinPadMode()");
            int code = MyApplication.app.pinPadOptV2.getPinPadMode(bundle);
            addEndTime("getPinPadMode()");
            showSpendTime();
            if (code != 0) {
                LogUtil.e(TAG, "get PinPad mode failed.");
                return;
            }
            LogUtil.e(TAG, "get PinPad mode success.");
            int normal = bundle.getInt("normal");
            int longPressToClear = bundle.getInt("longPressToClear");
            int silent = bundle.getInt("silent");
            int greenLed = bundle.getInt("greenLed");
            int visualImpairment = bundle.getInt("visualImpairment");
            LogUtil.e(TAG, Utility.formatStr("PinPad mode:\nnormal:%d\nvisualImpairment:%d\nlongPressToClear:%d\nsilent:%d\ngreenLed:%d",
                    normal, visualImpairment, longPressToClear, silent, greenLed));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set PinPad text
     * <br/> Mostly, the SDK built-in text is appropriate, client don't need to change it.
     * But if client want to customize the showing text, this method is helpful.
     * the set value just valid for next time inputting PIN, after input PIN finished,
     * the set value lost effect
     */
    private void initPinPadText() {
        try {
            if (!checkInput(txtConfirm) || !checkInput(txtInputPin)
                    || !checkInput(txtInputOfflinePin) || !checkInput(txtReinputOfflinePinFormat)) {
                return;
            }
            String confirm = txtConfirm.getText().toString();
            String inputPin = txtInputPin.getText().toString();
            String inputOfflinePin = txtInputOfflinePin.getText().toString();
            String reInputOfflinePinFormat = txtReinputOfflinePinFormat.getText().toString();
            PinPadTextConfigV2 textConfigV2 = new PinPadTextConfigV2();
            textConfigV2.confirm = confirm;
            textConfigV2.inputPin = inputPin;
            textConfigV2.inputOfflinePin = inputOfflinePin;
            textConfigV2.reinputOfflinePinFormat = reInputOfflinePinFormat;
            addStartTimeWithClear("setPinPadText()");
            MyApplication.app.pinPadOptV2.setPinPadText(textConfigV2);
            addEndTime("setPinPadText()");
            showToast(R.string.success);
            showSpendTime();
        } catch (RemoteException e) {
            e.printStackTrace();
            showToast(R.string.fail);
        }
    }

    private boolean checkInput(EditText edt) {
        String text = edt.getText().toString();
        if (TextUtils.isEmpty(text)) {
            showToast("PinPad text can't be empty!");
            edt.requestFocus();
            return false;
        }
        return true;
    }

    /** start SDK built-in PinPad */
    private void initPinPad() {
        try {
            PinPadConfigV2 configV2 = initPinPadConfigV2();
            if (configV2 != null) {
                // start input PIN
                addStartTimeWithClear("initPinPad()");
                String result = MyApplication.app.pinPadOptV2.initPinPad(configV2, mPinPadListener);
                if (configV2.getPinPadType() == 1 && TextUtils.isEmpty(result)) {//自定义密码键盘
                    String msg = "initPinPad failed";
                    LogUtil.e(TAG, msg);
                    showToast(msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** start SDK built-in PinPad */
    private void initPinPadEx() {
        String pikIndexStr = mEditKeyIndex.getText().toString();
        if (TextUtils.isEmpty(pikIndexStr)) {
            showToast(R.string.pin_pad_key_index_hint);
            return;
        }
        int pinKeyIndex = Integer.parseInt(pikIndexStr);
        boolean isKeyDukpt = mRGPikKeySystem.getCheckedRadioButtonId() == R.id.rb_key_system_dukpt;
        if (!KeyIndexUtil.checkMkskOrDukptKeyIndex(pinKeyIndex, isKeyDukpt)) {
            showToast(R.string.pin_pad_key_index_hint);
            mEditKeyIndex.requestFocus();
            return;
        }
        String timeoutStr = mEditTimeout.getText().toString();
        if (TextUtils.isEmpty(timeoutStr)) {
            showToast(R.string.pin_pad_timeout_hint);
            return;
        }
        boolean visualImpairment = chkModeList.get(R.id.rdo_mode_vi).isChecked();
        int timeout = Integer.parseInt(timeoutStr) * 1000;
        if (timeout < 0 || (!visualImpairment && timeout > 60000)) {
            showToast(R.string.pin_pad_timeout_hint);
            return;
        }
        EditText edtInputStep = findViewById(R.id.edit_input_step);
        if (TextUtils.isEmpty(edtInputStep.getText())) {
            showToast("Please input correct inputStep");
            return;
        }
        int inputStep = Integer.parseInt(edtInputStep.getText().toString());
        if (inputStep < 0 || inputStep > 12) {
            showToast("Please input correct inputStep");
            return;
        }
        cardNo = mEditCardNo.getText().toString().trim();
        if (cardNo.length() < 13 || cardNo.length() > 19) {
            showToast(R.string.pin_pad_card_no_hint);
            return;
        }
        try {
            Bundle bundle = new Bundle();
            // PinAlgType: 0-3DES, 1-SM4, 2-AES
            int pinAlgType = 0;//3DES
            int pinBlockFormat = PinBlockFormat.SEC_PIN_BLK_ISO_FMT0;
            if (mRGPinAlgorithmType.getCheckedRadioButtonId() == R.id.rb_pin_type_sm4) {
                pinAlgType = 1;//SM4
            } else if (mRGPinAlgorithmType.getCheckedRadioButtonId() == R.id.rb_pin_type_aes) {
                pinAlgType = 2;//AES
                pinBlockFormat = PinBlockFormat.SEC_PIN_BLK_ISO_FMT4;
            }
            // PinPadType: 0-SDK built-in PinPad, 1-Client customized PinPad
            bundle.putInt("pinPadType", mRGKeyboardStyle.getCheckedRadioButtonId() == R.id.rb_preset_keyboard ? 0 : 1);
            // PinType: 0-online PIN, 1-offline PIN
            bundle.putInt("pinType", mRGIsOnline.getCheckedRadioButtonId() == R.id.rb_online_pin ? 0 : 1);
            // isOrderNumberKey: true-order number PinPad, false-disorder number PinPad
            bundle.putInt("isOrderNumKey", mRGKeyboard.getCheckedRadioButtonId() == R.id.rb_orderly_keyboard ? 1 : 0);
            byte[] panBytes = null;
            if (pinBlockFormat == PinBlockFormat.SEC_PIN_BLK_ISO_FMT4) {//for format 4,the whole card number(12-19 digits) is PAN
                panBytes = cardNo.getBytes(StandardCharsets.US_ASCII);
            } else { // PAN(Person Identify Number) convert ASCII characters to bytes, eg: “123456”.getBytes("US-ASCII")
                panBytes = cardNo.substring(cardNo.length() - 13, cardNo.length() - 1).getBytes(StandardCharsets.US_ASCII);
            }
            bundle.putByteArray("pan", panBytes);
            // PIK(PIN key) index
            bundle.putInt("pinKeyIndex", pinKeyIndex);
            // Minimum input PIN number
            bundle.putInt("minInput", 0);
            // Maximum input number(Max value is 12)
            bundle.putInt("maxInput", 12);
            // The input step if input PIN, default 1
            bundle.putInt("inputStep", inputStep);
            // Input PIN timeout time
            bundle.putInt("timeout", timeout);
            // is support bypass PIN, 0-not support, 1-support
            bundle.putInt("isSupportbypass", 1);
            // PIN block format
            bundle.putInt("pinblockFormat", pinBlockFormat);
            // PinAlgType: 0-3DES, 1-SM4, 2-AES
            bundle.putInt("algorithmType", pinAlgType);
            // PIK key system: 0-MKSK, 1-Dukpt
            bundle.putInt("keySystem", mRGPikKeySystem.getCheckedRadioButtonId() == R.id.rb_key_system_mksk ? 0 : 1);
            addStartTimeWithClear("initPinPadEx()");
            String result = MyApplication.app.pinPadOptV2.initPinPadEx(bundle, mPinPadListener);
            if (bundle.getInt("pinPadType") == 1 && TextUtils.isEmpty(result)) {//自定义密码键盘
                String msg = "initPinPad failed";
                LogUtil.e(TAG, msg);
                showToast(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Initialize customized PinPad */
    private void initCustomPinPad() {
        try {
            PinPadConfigV2 pinPadConfigV2 = initPinPadConfigV2();
            if (pinPadConfigV2 == null) {
                LogUtil.e(TAG, "init customize pinpad failed");
                return;
            }
            Intent intent = new Intent();
            intent.putExtra("PinPadConfigV2", (Serializable) pinPadConfigV2);
            intent.putExtra("cardNo", cardNo);
            if (chkModeList.get(R.id.rdo_mode_vi).isChecked()) {//视障模式
                intent.setClass(this, CustomizeVisualImpairmentPinActivity.class);
            } else {
                intent.setClass(this, CustomPinPadActivity.class);
            }
            startActivityForResult(intent, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 初始化PinPadConfigV2 */
    private PinPadConfigV2 initPinPadConfigV2() {
        String pikIndex = mEditKeyIndex.getText().toString();
        if (TextUtils.isEmpty(pikIndex)) {
            showToast(R.string.pin_pad_key_index_hint);
            return null;
        }
        int pinKeyIndex = Integer.parseInt(pikIndex);
        boolean isKeyDukpt = mRGPikKeySystem.getCheckedRadioButtonId() == R.id.rb_key_system_dukpt;
        if (!KeyIndexUtil.checkMkskOrDukptKeyIndex(pinKeyIndex, isKeyDukpt)) {
            showToast(R.string.pin_pad_key_index_hint);
            mEditKeyIndex.requestFocus();
            return null;
        }
        String timeoutStr = mEditTimeout.getText().toString();
        if (TextUtils.isEmpty(timeoutStr)) {
            showToast(R.string.pin_pad_timeout_hint);
            return null;
        }
        boolean visualImpairment = chkModeList.get(R.id.rdo_mode_vi).isChecked();
        boolean longTimeOutTime = chkModeList.get(R.id.chk_mode_long_timeout_time).isChecked();
        int timeout = Integer.parseInt(timeoutStr) * 1000;
        if (timeout < 0 || !visualImpairment && (longTimeOutTime && timeout > 600000 || !longTimeOutTime && timeout > 60000)) {
            showToast(R.string.pin_pad_timeout_hint);
            return null;
        }
        cardNo = mEditCardNo.getText().toString().trim();
        if (cardNo.length() < 13 || cardNo.length() > 19) {
            showToast(R.string.pin_pad_card_no_hint);
            return null;
        }
        try {
            PinPadConfigV2 pinPadConfig = new PinPadConfigV2();
            // PinPadType: 0-SDK built-in PinPad, 1-Client customized PinPad
            pinPadConfig.setPinPadType(mRGKeyboardStyle.getCheckedRadioButtonId() == R.id.rb_preset_keyboard ? 0 : 1);
            // PinType: 0-online PIN, 1-offline PIN
            pinPadConfig.setPinType(mRGIsOnline.getCheckedRadioButtonId() == R.id.rb_online_pin ? 0 : 1);
            // isOrderNumerKey: true:order number PinPad, false:disorder number PinPad
            pinPadConfig.setOrderNumKey(mRGKeyboard.getCheckedRadioButtonId() == R.id.rb_orderly_keyboard);
            // PinAlgType: 0-3DES, 1-SM4, 2-AES
            int pinAlgType = 0;//3DES
            int pinBlockFormat = PinBlockFormat.SEC_PIN_BLK_ISO_FMT0;
            if (mRGPinAlgorithmType.getCheckedRadioButtonId() == R.id.rb_pin_type_sm4) {
                pinAlgType = 1;//SM4
            } else if (mRGPinAlgorithmType.getCheckedRadioButtonId() == R.id.rb_pin_type_aes) {
                pinAlgType = 2;//AES
                pinBlockFormat = PinBlockFormat.SEC_PIN_BLK_ISO_FMT4;
            }
            pinPadConfig.setAlgorithmType(pinAlgType);
            // PIK key system: 0-MKSK, 1-Dukpt
            pinPadConfig.setKeySystem(mRGPikKeySystem.getCheckedRadioButtonId() == R.id.rb_key_system_mksk ? 0 : 1);
            byte[] panBytes = null;
            if (pinBlockFormat == PinBlockFormat.SEC_PIN_BLK_ISO_FMT4) {//for format 4,the whole card number(12-19 digits) is PAN
                panBytes = cardNo.getBytes(StandardCharsets.US_ASCII);
            } else { // PAN(Person Identify Number) convert ASCII characters to bytes, eg: “123456”.getBytes("US-ASCII")
                panBytes = cardNo.substring(cardNo.length() - 13, cardNo.length() - 1).getBytes(StandardCharsets.US_ASCII);
            }
            pinPadConfig.setPan(panBytes);
            // Input PIN timeout time
            pinPadConfig.setTimeout(timeout);
            // PIN block format
            pinPadConfig.setPinblockFormat(pinBlockFormat);
            // PIK(PIN key) index
            pinPadConfig.setPinKeyIndex(pinKeyIndex);
            // Minimum input PIN number
            pinPadConfig.setMinInput(0);
            // Maximum input number(Max value is 12)
            pinPadConfig.setMaxInput(12);
            return pinPadConfig;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private final PinPadListenerV2 mPinPadListener = new PinPadListenerV2Wrapper() {
        @Override
        public void onPinLength(int i) {
            LogUtil.e(Constant.TAG, "onPinLength:" + i);
        }

        @Override
        public void onConfirm(int pinType, byte[] bytes) {
            addEndTime("initPinPad()");
            addEndTime("initPinPadEx()");
            String hexStr = ByteUtil.bytes2HexStr(bytes);
            LogUtil.e(Constant.TAG, "onConfirm, pinType:" + pinType + ",PinBlock:" + hexStr);
            handleUpdateShowingPinBlockAndKsn(hexStr);
            showSpendTime();
        }

        @Override
        public void onCancel() {
            addEndTime("initPinPad()");
            addEndTime("initPinPadEx()");
            LogUtil.e(Constant.TAG, "onCancel");
            runOnUiThread(() -> {
                showToast("user cancel");
            });
            showSpendTime();
        }

        @Override
        public void onError(int code) {
            addEndTime("initPinPad()");
            addEndTime("initPinPadEx()");
            LogUtil.e(Constant.TAG, "onError:" + code);
            String msg = AidlErrorCodeV2.valueOf(code).getMsg();
            runOnUiThread(() -> {
                showToast("error:" + msg + " -- " + code);
            });
            showSpendTime();
        }
    };

    private void handleUpdateShowingPinBlockAndKsn(String pinBlock) {
        runOnUiThread(() -> {
            try {
                mTvPinBlock.setText("PinBlock:" + pinBlock);
                if (mRGPikKeySystem.getCheckedRadioButtonId() == R.id.rb_key_system_dukpt) {//dukpt key
                    int pikIndex = Integer.parseInt(mEditKeyIndex.getText().toString());
                    byte[] buffer = new byte[10];
                    if (pikIndex >= 10 && pikIndex <= 19 || pikIndex >= 2100 && pikIndex <= 2199) {//dukpt-aes
                        buffer = new byte[12];
                    }
                    int code = MyApplication.app.securityOptV2.dukptCurrentKSN(pikIndex, buffer);
                    if (code < 0) {
                        showToast("get Ksn failed, code:" + code);
                        return;
                    }
                    mTvKSN.setText("KSN:" + ByteUtil.bytes2HexStr(buffer));
                } else {
                    mTvKSN.setText("KSN:");
                }
                showToast("click ok");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String pinCipher = data.getStringExtra("pinCipher");
            if (!TextUtils.isEmpty(pinCipher)) {
                mTvPinBlock.setText("PinBlock:" + pinCipher);
            }
        }
    }
}
