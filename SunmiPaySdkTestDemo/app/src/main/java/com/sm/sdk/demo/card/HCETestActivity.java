package com.sm.sdk.demo.card;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;

import java.util.Locale;

public class HCETestActivity extends BaseAppCompatActivity {
    private EditText edtNdefData;
    private RadioGroup rdgNfcType;
    private RadioGroup rdgNfcData;
    private final String NFC4_DATA_1 = "Explore the Android development landscape. Learn about the latest release and review details of earlier releases. Discover the device ecosystem, modern Android development, and training courses.\n" +
            "Welcome to the Android developer guides. These documents teach you how to build Android apps using APIs in the Android framework and other libraries.\n" +
            "If you're brand new to Android and want to jump into code, start with the Build your first Android app tutorial.\n" +
            "And check out these other resources to learn Android development:";
    private final String NFC4_DATA_2 = "Explore the Android development landscape. Learn about the latest release and rev\n" +
            "Great! You changed the text, but it introduces you as Android, which is probably not your name. Next, you will personalize it to introduce you with your name!\n" +
            "The GreetingPreview() function is a cool feature that lets you see what your composable looks like without having to build your entire app. To enable a preview of a composable, annotated with @Composable and @Preview. The @Preview annotation tells Android Studio that this composable should be shown in the design view of this file.\n" +
            "As you can see, the @Preview annotation takes in a parameter called showBackground. If showBackground is set to true, it will add a background to your composable preview.\n" +
            "Since Android Studio by default uses a light theme for the editor, it can be hard to see the difference between showBackground = true and showBackground = false. However, this is an example of what the difference looks like.";
    private final String NFC4_DATA_3 = "1.4 NFC通信模式\n" +
            "\n" +
            "读卡器模式（Reader/writer mode）、仿真卡模式(Card Emulation Mode)、点对点模式（P2P mode）。\n" +
            "\n" +
            "读卡器模式\n" +
            "数据在NFC芯片中，可以简单理解成“刷标签”。本质上就是通过支持NFC的手机或其它电子设备从带有NFC芯片的标签、贴纸、名片等媒介中读写信息。通常NFC标签是不需要外部供电的。当支持NFC的外设向NFC读写数据时，它会发送某种磁场，而这个磁场会自动的向NFC标签供电。";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_hce);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_hce);
        findViewById(R.id.btn_open_hce).setOnClickListener(this);
        findViewById(R.id.btn_write_ndef_data).setOnClickListener(this);
        findViewById(R.id.btn_close_hce).setOnClickListener(this);
        edtNdefData = findViewById(R.id.edt_ndef_text);
        rdgNfcType = findViewById(R.id.rdg_nfc_type);
        rdgNfcData = findViewById(R.id.rdg_nfc_data);
        rdgNfcData.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rdo_data_1:
                    edtNdefData.setText(NFC4_DATA_1);
                    break;
                case R.id.rdo_data_2:
                    edtNdefData.setText(NFC4_DATA_2);
                    break;
                case R.id.rdo_data_3:
                    edtNdefData.setText(NFC4_DATA_3);
                    break;
            }
        });
        rdgNfcType.check(R.id.rdo_type4);
        rdgNfcData.check(R.id.rdo_data_1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_hce:
                openHce();
                break;
            case R.id.btn_write_ndef_data:
                writeNdefData();
                break;
            case R.id.btn_close_hce:
                closeHce(true);
                break;
        }
    }

    /** open HCE */
    private void openHce() {
        showToast(R.string.invoking_not_support);
//        try {
//            int nfgType = CardType.NFC.getValue();
//            if (rdgNfcType.getCheckedRadioButtonId() == R.id.rdo_type2) {
//                nfgType = CardType.IC.getValue();
//            }
//            int code = MyApplication.app.hceManagerV2.hceOpen(nfgType, null);
//            String log = "open hce " + Utility.getStateString(code);
//            showToast(log);
//            LogUtil.e(TAG, log);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /** Write NDEF message */
    private void writeNdefData() {
        showToast(R.string.invoking_not_support);
//        try {
//            String dataStr = edtNdefData.getText().toString();
//            if (TextUtils.isEmpty(dataStr)) {
//                showToast("ndef data shouldn't be empty");
//                edtNdefData.requestFocus();
//                return;
//            }
//            String languageCode = Locale.CHINA.getLanguage();
//            NdefRecord record = NdefRecord.createTextRecord(languageCode, dataStr);
//            NdefMessage msg = new NdefMessage(record);
//            byte[] data = msg.toByteArray();
//            LogUtil.e(TAG, "writeNdefData() :" + ByteUtil.bytes2HexStr(data));
//            int code = MyApplication.app.hceManagerV2.hceNdefWrite(data);
//            String log = "hceNdefWrite " + Utility.getStateString(code);
//            showToast(log);
//            LogUtil.e(TAG, log);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /** close HCE */
    private void closeHce(boolean showToast) {
        showToast(R.string.invoking_not_support);
//        try {
//            int code = MyApplication.app.hceManagerV2.hceClose();
//            String log = "close hce " + Utility.getStateString(code);
//            if (showToast) {
//                showToast(log);
//            }
//            LogUtil.e(TAG, log);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeHce(false);
    }
}
