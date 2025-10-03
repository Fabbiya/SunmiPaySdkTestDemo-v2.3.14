package com.sm.sdk.demo.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sm.sdk.demo.R;

public class LoadingDialog extends Dialog {
    private LinearLayout rootLayout;
    private ProgressBar mProgressbar;
    private TextView mTvMessage;

    public LoadingDialog(Context context, String text) {
        this(context, R.style.DefaultDialogStyle, text);
    }

    private LoadingDialog(Context context, int theme, String text) {
        super(context, theme);
        init(text);
    }

    private void init(String msg) {
        setContentView(R.layout.dialog_loading);
        Window window = getWindow();
        if (window != null) {
            window.getAttributes().gravity = Gravity.CENTER;
        }
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        rootLayout = findViewById(R.id.root_layout);
        mProgressbar = findViewById(R.id.progress_bar);
        mTvMessage = findViewById(R.id.tv_message);
        if (msg == null || msg.trim().length() == 0) {
            mTvMessage.setText(R.string.loading);
        } else {
            mTvMessage.setText(msg);
        }
    }

    public void setMessage(String msg) {
        mTvMessage.setText(msg);
    }

    public void setWidthHeight(int wPixels, int hPixels) {
        ViewGroup.LayoutParams lp = rootLayout.getLayoutParams();
        lp.width = wPixels;
        lp.height = hPixels;
        rootLayout.setLayoutParams(lp);
    }

    public void setTextSize(int sp) {
        mTvMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
    }

    public void setTextStyle(Typeface style) {
        mTvMessage.setTypeface(style);
    }

    public void setProgressbarVisibility(int visibility) {
        mProgressbar.setVisibility(visibility);
    }
}
