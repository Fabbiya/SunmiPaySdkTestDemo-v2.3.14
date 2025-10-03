package com.sm.sdk.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.sm.sdk.demo.basic.BasicActivity;
import com.sm.sdk.demo.biometric.BiometricActivity;
import com.sm.sdk.demo.card.CardActivity;
import com.sm.sdk.demo.emv.EMVActivity;
import com.sm.sdk.demo.etc.ETCActivity;
import com.sm.sdk.demo.m112.M112Activity;
import com.sm.sdk.demo.other.OtherActivity;
import com.sm.sdk.demo.pin.PinPadActivity;
import com.sm.sdk.demo.print.PrintActivity;
import com.sm.sdk.demo.scan.ScanActivity;
import com.sm.sdk.demo.security.SecurityActivity;
import com.sm.sdk.demo.tax.TaxTestActivity;
import com.sm.sdk.demo.utils.DeviceUtil;

public class MainActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("SunmiSDKTestDemo");

        findViewById(R.id.card_view_basic).setOnClickListener(this);
        findViewById(R.id.card_view_card).setOnClickListener(this);
        findViewById(R.id.card_view_pin_pad).setOnClickListener(this);
        findViewById(R.id.card_view_security).setOnClickListener(this);
        findViewById(R.id.card_view_emv).setOnClickListener(this);
        findViewById(R.id.card_view_scan).setOnClickListener(this);
        findViewById(R.id.card_view_print).setOnClickListener(this);
        findViewById(R.id.card_view_other).setOnClickListener(this);
        findViewById(R.id.card_view_etc).setOnClickListener(this);
        findViewById(R.id.card_view_comm).setOnClickListener(this);
        findViewById(R.id.card_view_tax).setOnClickListener(this);
        findViewById(R.id.card_view_biometric).setOnClickListener(this);
        View viewM112 = findViewById(R.id.card_view_m112);
        viewM112.setOnClickListener(this);
        if (DeviceUtil.isFinanceDevice()) {
            viewM112.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!MyApplication.app.isConnectPaySDK()) {
            MyApplication.app.bindPaySDKService();
        }
    }

    @Override
    public void onClick(View v) {
        if (!MyApplication.app.isConnectPaySDK()) {
            MyApplication.app.bindPaySDKService();
            showToast(R.string.connect_loading);
            return;
        }
        final int id = v.getId();
        switch (id) {
            case R.id.card_view_basic:
                openActivity(BasicActivity.class);
                break;
            case R.id.card_view_card:
                openActivity(CardActivity.class);
                break;
            case R.id.card_view_pin_pad:
                openActivity(PinPadActivity.class);
                break;
            case R.id.card_view_security:
                openActivity(SecurityActivity.class);
                break;
            case R.id.card_view_emv:
                openActivity(EMVActivity.class);
                break;
            case R.id.card_view_scan:
                openActivity(ScanActivity.class);
                break;
            case R.id.card_view_print:
                openActivity(PrintActivity.class);
                break;
            case R.id.card_view_etc:
                openActivity(ETCActivity.class);
                break;
            case R.id.card_view_other:
                openActivity(OtherActivity.class);
                break;
            case R.id.card_view_tax:
                openActivity(TaxTestActivity.class);
                break;
            case R.id.card_view_m112:
                openActivity(M112Activity.class);
                break;
            case R.id.card_view_biometric:
                openActivity(BiometricActivity.class);
                break;
        }
    }

    public static void reStart(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


}
