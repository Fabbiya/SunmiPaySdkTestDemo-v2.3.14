package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.IOUtil;
import com.sm.sdk.demo.utils.ThreadPoolUtil;
import com.sm.sdk.demo.utils.Utility;

import java.util.Arrays;
import java.util.Random;

public class TransmissionTestActivity extends BaseAppCompatActivity {
    private EditText edtInputDataLen;
    private EditText edtTestTime;
    private EditText edtDelay;
    private TextView tvTotalCount;
    private TextView tvSuccessCount;
    private TextView tvFailureCount;
    private TextView tvSuccessPercent;
    private TextView tvSuccessDataAmount;
    private TextView tvTotalTestTime;
    private TextView tvDataExchangeSpeed;
    private final DataTransmissionTask task = new DataTransmissionTask();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmission_stress_test);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.test_transmission_stress_test);
        edtInputDataLen = findViewById(R.id.edt_input_data_len);
        edtTestTime = findViewById(R.id.edt_test_time);
        edtDelay = findViewById(R.id.edt_delay_time);
        tvTotalCount = findViewById(R.id.tv_total_count);
        tvSuccessCount = findViewById(R.id.tv_success_count);
        tvFailureCount = findViewById(R.id.tv_failure_count);
        tvSuccessPercent = findViewById(R.id.tv_success_percent);
        tvSuccessDataAmount = findViewById(R.id.tv_success_data_amount);
        tvTotalTestTime = findViewById(R.id.tv_total_test_time);
        tvDataExchangeSpeed = findViewById(R.id.tv_data_exchange_speed);
        findViewById(R.id.btn_start).setOnClickListener((v) -> startDataTransmission());
        findViewById(R.id.btn_stop).setOnClickListener((v) -> stopDataTransmission());
    }

    /** 开始测试 */
    private void startDataTransmission() {
        if (task.running) {
            showToast("Testing is ongoing!");
            return;
        }
        //length of input data
        String dataInLenStr = edtInputDataLen.getText().toString();
        int dataInLen = -1;
        if (!TextUtils.isEmpty(dataInLenStr)) {
            dataInLen = Integer.parseInt(dataInLenStr);
        }
        //test duration, unit: min
        String testTimeStr = edtTestTime.getText().toString();
        int timeLimit = Integer.MAX_VALUE;
        if (!TextUtils.isEmpty(testTimeStr)) {
            timeLimit = Integer.parseInt(testTimeStr) * 60 * 1000;
        }
        //间隔时间
        String delayTimeStr = edtDelay.getText().toString();
        int delayTime = 0;
        if (!TextUtils.isEmpty(delayTimeStr)) {
            delayTime = Integer.parseInt(delayTimeStr);
        }
        if (delayTime < 0 || delayTime > 200) {
            showToast("delay time should in [0,200]");
            edtDelay.requestFocus();
            return;
        }
        task.setTimeLimit(timeLimit);
        task.setDataInLen(dataInLen);
        task.setDelayTime(delayTime);
        ThreadPoolUtil.executeInCachePool(task);
    }

    /** 停止测试 */
    private void stopDataTransmission() {
        task.running = false;
    }

    private void updateUI() {
        long totalCount = task.totalCount;
        long successCount = task.successCount;
        long failureCount = task.failedCount;
        long spentTime = task.spentTime / 1000;
        long successAmount = task.successAmount;
        runOnUiThread(() -> {
            tvTotalCount.setText(Utility.formatStr("%s%d", getString(R.string.test_total_count), totalCount));
            tvSuccessCount.setText(Utility.formatStr("%s%d", getString(R.string.test_success_count), successCount));
            tvFailureCount.setText(Utility.formatStr("%s%d", getString(R.string.test_failed_count), failureCount));
            float successPercent = (float) successCount / totalCount * 100;
            tvSuccessPercent.setText(Utility.formatStr("%s%.2f%%", getString(R.string.test_success_percent), successPercent));
            tvSuccessDataAmount.setText(Utility.formatStr("%s%d", getString(R.string.test_success_data_amount), successAmount));
            long hour = spentTime / 3600;
            long min = (spentTime - hour * 3600) / 60;
            long sec = spentTime - hour * 3600 - min * 60;
            tvTotalTestTime.setText(Utility.formatStr("%s%02d:%02d:%02d", getString(R.string.test_spent_time), hour, min, sec));
            double exSpeed = (spentTime == 0 ? successAmount : (double) successAmount / spentTime);
            tvDataExchangeSpeed.setText(Utility.formatStr("%s%.2fB/s", getString(R.string.test_data_exchange_speed), exSpeed));
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        task.quit();
    }

    private class DataTransmissionTask implements Runnable {
        private volatile boolean running = false;
        private long timeLimit;
        private int dataInLen;
        private int delayTime;

        private long totalCount;
        private long successCount;
        private long failedCount;
        private long spentTime;
        private long lastSpentTime;
        private long successAmount;


        private void setTimeLimit(long limit) {
            timeLimit = limit;
        }

        private void setDataInLen(int len) {
            dataInLen = len;
        }

        private void setDelayTime(int delay) {
            delayTime = delay;
        }

        @Override
        public void run() {
            try {
                running = true;
                long startTime = System.currentTimeMillis();
                long endTime = startTime + timeLimit;
                byte[] buffer = new byte[2048];
                Random random = new Random();
                byte[] dataIn = new byte[Math.max(dataInLen, 0)];
                while (running && System.currentTimeMillis() <= endTime) {
                    if (dataInLen < 0) {
                        dataIn = new byte[random.nextInt(536) + 1500];
                    }
                    for (int i = 0; i < dataIn.length; i++) {//填充随机数据
                        dataIn[i] = (byte) random.nextInt(256);
                    }
                    int len = MyApplication.app.testOptV2.testTransmission(dataIn, buffer);
                    totalCount++;
                    spentTime = System.currentTimeMillis() - startTime + lastSpentTime;
                    if (len >= 0) {//接口调用成功
                        byte[] valid = Arrays.copyOf(buffer, len);
                        if (Arrays.equals(valid, dataIn)) {//检查输出数据和输入数据是否相等
                            successCount++;
                            successAmount += len;
                        } else {//发送数据与应答数据不一致
                            failedCount++;
                        }
                    } else {//接口调用失败
                        failedCount++;
                    }
                    updateUI();
                    IOUtil.delay(delayTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                running = false;
                lastSpentTime = spentTime;
            }
        }

        private void quit() {
            running = false;
        }
    }

}
