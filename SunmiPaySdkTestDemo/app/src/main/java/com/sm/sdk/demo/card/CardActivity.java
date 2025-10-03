package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;

public class CardActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((R.layout.activity_card));
        initToolbarBringBack(R.string.read_card);
        initView();
    }

    private void initView() {
        View view = findViewById(R.id.card_mag);
        TextView leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_mag);

        view = findViewById(R.id.card_mag_enc);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_mag_enc);

        view = findViewById(R.id.card_mag_enc_pan);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_mag_track2_enc);

        view = findViewById(R.id.card_ic);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_ic);

        view = findViewById(R.id.card_nfc);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_nfc);

        view = findViewById(R.id.card_composite);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_mag_ic_nfc);

        view = findViewById(R.id.card_sam);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_sam);

        view = findViewById(R.id.card_m1);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_m1);

        view = findViewById(R.id.card_m1_psam);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_m1_psame);

        view = findViewById(R.id.card_mifare_ultralight);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_mifare_ultralight);

        view = findViewById(R.id.card_mifare_ultralight_ev1);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_mifare_ultralight_ev1);

        view = findViewById(R.id.card_mifare_desfire_ev2);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_mifare_desfire_ev2);

        view = findViewById(R.id.card_mifare_plus);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_mifare_plus);

        view = findViewById(R.id.card_mifare_plus_transmit_apdu);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_mifare_plus_transmit_apdu);

        view = findViewById(R.id.card_FELICA);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_FELICA);

        view = findViewById(R.id.card_apdu);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_apdu);

        view = findViewById(R.id.card_transmit_apdu);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_transmit_apdu);

        view = findViewById(R.id.card_extended_apdu);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_extended_apdu);

        view = findViewById(R.id.card_pass_apdu);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_pass_apdu);

        view = findViewById(R.id.card_sle4442);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_sle4442_4428);

        view = findViewById(R.id.card_at24c);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_at24c);

        view = findViewById(R.id.card_at88sc);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_at88scxx);

        view = findViewById(R.id.card_ctx512);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_ctx512);

        view = findViewById(R.id.card_sri);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_sri_card_test);

        view = findViewById(R.id.card_ctr_code_test);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_ctr_code_multi_apdu_test);

        view = findViewById(R.id.card_exist_status);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_get_card_exist_status);

        view = findViewById(R.id.card_hce_test);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_hce);

        view = findViewById(R.id.card_ping_test);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_ping);

        view = findViewById(R.id.card_innovatron_test);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_innovatron_test);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.card_mag:
                openActivity(MagActivity.class);
                break;
            case R.id.card_mag_enc:
                openActivity(MagEncActivity.class);
                break;
            case R.id.card_mag_enc_pan:
                openActivity(MagTrack2EncActivity.class);
                break;
            case R.id.card_ic:
                openActivity(ICActivity.class);
                break;
            case R.id.card_nfc:
                openActivity(NFCActivity.class);
                break;
            case R.id.card_composite:
                openActivity(CompositeActivity.class);
                break;
            case R.id.card_m1:
                openActivity(M1Activity.class);
                break;
            case R.id.card_m1_psam:
                openActivity(MifareAuthedByPSAMActivity.class);
                break;
            case R.id.card_sam:
                openActivity(SAMActivity.class);
                break;
            case R.id.card_mifare_ultralight:
                openActivity(MifareUltralightCActivity.class);
                break;
            case R.id.card_mifare_ultralight_ev1:
                openActivity(MifareUtralightEv1Activity.class);
                break;
            case R.id.card_mifare_desfire_ev2:
                openActivity(MifareDesfireEv2Activity.class);
                break;
            case R.id.card_mifare_plus:
                openActivity(MifarePlusActivity.class);
                break;
            case R.id.card_mifare_plus_transmit_apdu:
                openActivity(MifarePlusTransmitApduActivity.class);
                break;
            case R.id.card_FELICA:
                openActivity(FelicaActivity.class);
                break;
            case R.id.card_apdu:
                openActivity(NormalApduActivity.class);
                break;
            case R.id.card_transmit_apdu:
                openActivity(TransmitApduActivity.class);
                break;
            case R.id.card_extended_apdu:
                openActivity(ExtendedApduActivity.class);
                break;
            case R.id.card_pass_apdu:
                openActivity(PassApduActivity.class);
                break;
            case R.id.card_sle4442:
                openActivity(SLE4442_4428Actviity.class);
                break;
            case R.id.card_at24c:
                openActivity(AT24CActivity.class);
                break;
            case R.id.card_at88sc:
                openActivity(AT88SCActivity.class);
                break;
            case R.id.card_ctx512:
                openActivity(CTX512Activity.class);
                break;
            case R.id.card_sri:
                openActivity(SRIActivity.class);
                break;
            case R.id.card_ctr_code_test:
                openActivity(CtrCodeAndMultiApduActivity.class);
                break;
            case R.id.card_exist_status:
                openActivity(GetCardExistStatusActivity.class);
                break;
            case R.id.card_hce_test:
                openActivity(HCETestActivity.class);
                break;
            case R.id.card_ping_test:
                openActivity(PingActivity.class);
                break;
            case R.id.card_innovatron_test:
                openActivity(InnovatronActivity.class);
                break;
        }
    }

}
