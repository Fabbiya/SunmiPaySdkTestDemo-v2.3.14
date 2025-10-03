package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ThreadPoolUtil;

public class BuzzerActivity extends BaseAppCompatActivity {
    private EditText mEditTimeDelay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_buzzer);
        initToolbarBringBack(R.string.basic_buzzer);
        initView();
    }

    private void initView() {
        mEditTimeDelay = findViewById(R.id.edit_time_delay);
        findViewById(R.id.rb_1).setOnClickListener(this);
        findViewById(R.id.rb_2).setOnClickListener(this);
        findViewById(R.id.rb_3).setOnClickListener(this);
        findViewById(R.id.rb_4).setOnClickListener(this);
        findViewById(R.id.rb_5).setOnClickListener(this);
        findViewById(R.id.rb_6).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_1:
                buzzer(1);
                break;
            case R.id.rb_2:
                buzzer(2);
                break;
            case R.id.rb_3:
                buzzer(3);
                break;
            case R.id.rb_4:
                buzzer(4);
                break;
            case R.id.rb_5:
                buzzer(5);
                break;
            case R.id.rb_6:
                buzzer(6);
                break;
        }
    }

    /**
     * 控制蜂鸣器
     * times: 设备上的蜂鸣器响的次数，1~10
     * SP默认的间隔时间和频率分别为200ms、2750Hz
     */
    private void buzzer(int time) {
        ThreadPoolUtil.executeInCachePool(() -> {
            try {
                String delayStr = mEditTimeDelay.getText().toString();
                if (TextUtils.isEmpty(delayStr)) {
                    showToast(R.string.basic_buzzer_time_delay_hint);
                    return;
                }
                int delay = Integer.parseInt(delayStr);
                if (delay < 200 || delay > 10000) {
                    showToast(R.string.basic_buzzer_time_delay_hint);
                    return;
                }
                addStartTime("buzzerOnDevice()");
                MyApplication.app.basicOptV2.buzzerOnDevice(time, 3000, 500, delay);
                addEndTime("buzzerOnDevice()");
                showSpendTime();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
