package com.sm.sdk.demo.other;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.SettingUtil;
import com.sm.sdk.demo.utils.Utility;

public class CardPollIntervalTimeActivity extends BaseAppCompatActivity {
    private TextView tvPollIntervalTime;
    private EditText tvTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_poll_interval_time);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.setting_card_poll_interval_time);
        tvPollIntervalTime = findViewById(R.id.tv_poll_interval_time);
        tvTime = findViewById(R.id.edt_time);
        findViewById(R.id.mb_get_interval_time).setOnClickListener((v) -> getCardPollIntervalTime());
        findViewById(R.id.mb_set_interval_time).setOnClickListener((v) -> setCardPollIntervalTime());
        tvPollIntervalTime.setText(Utility.formatStr("%s:", getString(R.string.setting_card_poll_interval_time)));
    }

    private void getCardPollIntervalTime() {
        int time = SettingUtil.getCardPollIntervalTime();
        tvPollIntervalTime.setText(Utility.formatStr("%s:%d", getString(R.string.setting_card_poll_interval_time), time));
    }

    private void setCardPollIntervalTime() {
        String timeStr = tvTime.getText().toString();
        if (TextUtils.isEmpty(timeStr)) {
            showToast("interval time shouldn't be empty");
            tvTime.requestFocus();
            return;
        }
        int intervalTime = Integer.parseInt(timeStr);
        SettingUtil.setCardPollIntervalTime(intervalTime);
    }

}
