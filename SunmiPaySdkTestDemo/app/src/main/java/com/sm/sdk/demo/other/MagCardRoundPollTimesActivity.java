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

public class MagCardRoundPollTimesActivity extends BaseAppCompatActivity {
    private TextView tvRoundPollTimes;
    private EditText edtRoundPollTimes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mag_card_round_poll_times);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.setting_mag_card_round_poll_times);
        tvRoundPollTimes = findViewById(R.id.tv_round_poll_times);
        edtRoundPollTimes = findViewById(R.id.edt_round_poll_times);
        findViewById(R.id.mb_get_round_poll_times).setOnClickListener((v) -> getMagCardRoundPollTimes());
        findViewById(R.id.mb_set_round_poll_times).setOnClickListener((v) -> setMagCardRoundPollTimes());
        tvRoundPollTimes.setText(Utility.formatStr("%s:", getString(R.string.setting_mag_card_round_poll_times)));
    }

    private void getMagCardRoundPollTimes() {
        int times = SettingUtil.getMagCardRoundPollTimes();
        tvRoundPollTimes.setText(Utility.formatStr("%s:%d", getString(R.string.setting_mag_card_round_poll_times), times));
    }

    private void setMagCardRoundPollTimes() {
        String timesStr = edtRoundPollTimes.getText().toString();
        if (TextUtils.isEmpty(timesStr)) {
            showToast("Round poll times shouldn't be empty");
            edtRoundPollTimes.requestFocus();
            return;
        }
        int times = Integer.parseInt(timesStr);
        SettingUtil.setMagCardRoundPollTimes(times);
    }

}
