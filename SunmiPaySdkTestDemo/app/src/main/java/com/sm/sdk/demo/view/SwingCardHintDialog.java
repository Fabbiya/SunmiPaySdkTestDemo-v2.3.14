package com.sm.sdk.demo.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.sm.sdk.demo.R;

public class SwingCardHintDialog extends Dialog {
    /** 窗口类型，0-NFC,1-IC,2-NFC和IC */
    private final int dlgType;

    public SwingCardHintDialog(@NonNull Context context, int dlgType) {
        super(context, R.style.DefaultDialogStyle);
        this.dlgType = dlgType;
        init();
    }

    private void init() {
        setContentView(R.layout.dialog_swing_card_hint);
        ImageView imgView = findViewById(R.id.src_img);
        if (dlgType == 0) {//nfc
            imgView.setImageDrawable(getContext().getDrawable(R.drawable.card_nfc));
        } else if (dlgType == 1) {//ic
            imgView.setImageDrawable(getContext().getDrawable(R.drawable.card_ic));
        } else if (dlgType == 2) {//nfc+ic
            imgView.setImageDrawable(getContext().getDrawable(R.drawable.card_nfc_ic));
        }
        if (getWindow() != null) {
            getWindow().getAttributes().gravity = Gravity.CENTER;
        }

        // 点击空白不取消
        setCanceledOnTouchOutside(false);
        // 点击返回按钮不取消
        setCancelable(true);
    }


}
