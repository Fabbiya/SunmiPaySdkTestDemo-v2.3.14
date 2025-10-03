package com.sm.sdk.demo.print;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.SystemDateTime;
import com.sunmi.peripheral.printer.InnerResultCallback;
import com.sunmi.peripheral.printer.SunmiPrinterService;

public class PrintTextActivity extends BaseAppCompatActivity {

    private SunmiPrinterService sunmiPrinterService;
    private EditText etText, etTextSize;
    private EditText edtRepeatCount;
    private EditText edtWaitTime;
    private EditText edtIntervalTime;
    private ScreenOnOffReceiver receiver;
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_text);
        initToolbarBringBack(R.string.print_text);
        sunmiPrinterService = MyApplication.app.sunmiPrinterService;
        wakeLock = createWakeLock("tag");
        initView();
    }

    private void initView() {
        findViewById(R.id.btn_print).setOnClickListener(this);
        etText = findViewById(R.id.et_text);
        etTextSize = findViewById(R.id.et_text_size);
        etTextSize.setText("30");
        etText.setVisibility(View.GONE);
        edtRepeatCount = findViewById(R.id.edt_repeat_count);
        edtWaitTime = findViewById(R.id.edt_wait_time);
        edtIntervalTime = findViewById(R.id.edt_interval_time);
//        etText.setText(R.string.text_mock);
        findViewById(R.id.btn_screen_off_print).setOnClickListener(this);
        checkPrint();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_print:
                onPrintClick();
                break;
            case R.id.btn_screen_off_print:
                onScreenOffPrintClick();
                break;
        }
    }

    private boolean checkPrint() {
        if (MyApplication.app.sunmiPrinterService == null) {
            showToast("Print not supported");
            return false;
        }
        return true;
    }

    private void onPrintClick() {
        try {
            if (!checkPrint()) {
                return;
            }
            String repeatStr = edtRepeatCount.getText().toString();
            if (TextUtils.isEmpty(repeatStr)) {
                showToast("Repeat count shouldn't be empty");
                edtRepeatCount.requestFocus();
                return;
            }
            int repeatCount = Integer.parseInt(repeatStr);
            int textSize = Integer.parseInt(etTextSize.getText().toString());
            String text = etText.getText().toString();
            setHeight(0x12);
            String hHmmss = SystemDateTime.getHHmmss();
            addStartTimeWithClear("print total");
            sunmiPrinterService.enterPrinterBuffer(true);
            sunmiPrinterService.printTextWithFont(hHmmss + "\n", "", textSize, innerResultCallbcak);
            for (int i = 0; i < repeatCount; i++) {
                sunmiPrinterService.printTextWithFont(getString(R.string.text_mock1) + "\n", "", textSize, innerResultCallbcak);
            }
            sunmiPrinterService.exitPrinterBufferWithCallback(true, innerResultCallbcak);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onScreenOffPrintClick() {
        if (!checkPrint()) {
            return;
        }
        String waitTimeStr = edtWaitTime.getText().toString();
        if (TextUtils.isEmpty(waitTimeStr)) {
            showToast("Screen off wait time shouldn't be empty");
            edtWaitTime.requestFocus();
            return;
        }
        String intervalTimeStr = edtIntervalTime.getText().toString();
        if (TextUtils.isEmpty(intervalTimeStr)) {
            showToast("Screen off print interval time shouldn't be empty");
            edtIntervalTime.requestFocus();
            return;
        }
        if (receiver == null) {
            receiver = new ScreenOnOffReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(receiver, filter);
        }
        receiver.waitTime = Integer.parseInt(waitTimeStr);
        receiver.intervalTime = Integer.parseInt(intervalTimeStr);
        showToast("set screen off print success");
    }

    private boolean is = true;

    private final InnerResultCallback innerResultCallbcak = new InnerResultCallback() {
        @Override
        public void onRunResult(boolean isSuccess) {
            LogUtil.e("lxy", "isSuccess:" + isSuccess);
            if (is) {
                try {
                    sunmiPrinterService.printTextWithFont(SystemDateTime.getHHmmss() + "\n", "", 30, innerResultCallbcak);
                    sunmiPrinterService.lineWrap(6, innerResultCallbcak);
                    is = false;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onReturnString(String result) {
            LogUtil.e("lxy", "result:" + result);
        }

        @Override
        public void onRaiseException(int code, String msg) {
            addEndTime("print total");
            LogUtil.e("lxy", "code:" + code + ",msg:" + msg);
            showSpendTime();
        }

        @Override
        public void onPrintResult(int code, String msg) {
            addEndTime("print total");
            LogUtil.e("lxy", "code:" + code + ",msg:" + msg);
            showSpendTime();
        }
    };

    public void setHeight(int height) throws RemoteException {
        byte[] returnText = new byte[3];
        returnText[0] = 0x1B;
        returnText[1] = 0x33;
        returnText[2] = (byte) height;
        sunmiPrinterService.sendRAWData(returnText, null);
    }

    /** 创建wakelock对象 */
    private static PowerManager.WakeLock createWakeLock(String tag) {
        PowerManager pm = (PowerManager) MyApplication.app.getSystemService(Context.POWER_SERVICE);
        //获取电源管理器对象
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.PARTIAL_WAKE_LOCK, tag);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        wakeLock.setReferenceCounted(false);
        return wakeLock;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wakeLock.release();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    /** 息屏广播 */
    private class ScreenOnOffReceiver extends BroadcastReceiver {
        private int waitTime;
        private int intervalTime;

        private final Handler handler = new Handler(new Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                LogUtil.e(Constant.TAG, "PrintTextActivity test screen off print...");
                onPrintClick();
                handler.sendEmptyMessageDelayed(0, intervalTime * 1000L);
                return true;
            }
        });

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_ON.equalsIgnoreCase(intent.getAction())) {
                wakeLock.release();
                handler.removeCallbacksAndMessages(null);
            } else if (Intent.ACTION_SCREEN_OFF.equalsIgnoreCase(intent.getAction())) {
                wakeLock.acquire();
                handler.sendEmptyMessageDelayed(0, waitTime * 1000L);
            }
        }
    }

}
