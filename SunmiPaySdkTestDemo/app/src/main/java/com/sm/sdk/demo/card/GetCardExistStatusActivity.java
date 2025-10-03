package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardExistStatus;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidl.AidlErrorCode;

public class GetCardExistStatusActivity extends BaseAppCompatActivity {
    private CardType cardType = CardType.NFC;
    private TextView tvInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_card_exist_status);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_get_card_exist_status);
        TextView tv = findViewById(R.id.tv_card_type);
//        tv.setText(getString(R.string.card_card_type) + ":");
        RadioGroup rdg = findViewById(R.id.rdg_card_type);
        rdg.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rdo_nfc:
                    cardType = CardType.NFC;
                    break;
                case R.id.rdo_ic:
                    cardType = CardType.IC;
                    break;
                case R.id.rdo_sam0:
                    cardType = CardType.PSAM0;
                    break;
                case R.id.rdo_sam1:
                    cardType = CardType.SAM1;
                    break;
            }
        });
        rdg.check(R.id.rdo_nfc);
        tvInfo = findViewById(R.id.tv_info);
        findViewById(R.id.btn_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                getCardExistStatus();
                break;
        }
    }

    private void getCardExistStatus() {
        try {
            String msg = null;
            int result = MyApplication.app.readCardOptV2.getCardExistStatus(cardType.getValue());
            if (result < 0) {
                msg = getCardTypeMsg(cardType) + " exist status: error, code:" + result + ",msg:" + AidlErrorCode.valueOf(result).getMsg();
            } else if (result == CardExistStatus.CARD_ABSENT) {
                msg = getCardTypeMsg(cardType) + " exist status: ABSENT";
            } else if (result == CardExistStatus.CARD_PRESENT) {
                msg = getCardTypeMsg(cardType) + " exist status: PRESENT";
            }
            LogUtil.e(TAG, msg);
            tvInfo.setText(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCardTypeMsg(CardType type) {
        switch (type) {
            case NFC:
                return "NFC";
            case IC:
                return "IC";
            case PSAM0:
                return "SAM0";
            case SAM1:
                return "SAM1";
        }
        return "unknown";
    }
}
