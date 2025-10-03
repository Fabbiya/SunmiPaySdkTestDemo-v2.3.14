package com.sm.sdk.demo.emv;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.utils.LogUtil;

import java.util.Locale;

public final class EmvTTS extends UtteranceProgressListener {
    private static final String TAG = "EmvTTS";
    private TextToSpeech textToSpeech;
    private boolean supportTTS;

    private EmvTTS() {

    }

    public static EmvTTS getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final class SingletonHolder {
        private static final EmvTTS INSTANCE = new EmvTTS();
    }

    public void init() {
        //初始化TTS对象
        destroy();
        textToSpeech = new TextToSpeech(MyApplication.app, this::onTTSInit);
        textToSpeech.setOnUtteranceProgressListener(this);
    }

    public void play(String text) {
        if (!supportTTS) {
            Log.e(TAG, "PinPadTTS: play TTS failed, TTS not support...");
            return;
        }
        if (textToSpeech == null) {
            Log.e(TAG, "PinPadTTS: play TTS slipped, textToSpeech not init..");
            return;
        }
        Log.e(TAG, "play() text: [" + text + "]");
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "0");
    }

    @Override
    public void onStart(String utteranceId) {
        Log.e(TAG, "播放开始,utteranceId:" + utteranceId);
    }

    @Override
    public void onDone(String utteranceId) {
        Log.e(TAG, "播放结束,utteranceId:" + utteranceId);
    }

    @Override
    public void onError(String utteranceId) {
        Log.e(TAG, "播放出错,utteranceId:" + utteranceId);
    }

    void stop() {
        if (textToSpeech != null) {
            int code = textToSpeech.stop();
            Log.e(TAG, "tts stop() code:" + code);
        }
    }

    boolean isSpeaking() {
        if (textToSpeech != null) {
            return textToSpeech.isSpeaking();
        }
        return false;
    }

    void destroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    /** TTS初始化回调 */
    private void onTTSInit(int status) {
        if (status != TextToSpeech.SUCCESS) {
            LogUtil.e(TAG, "PinPadTTS: init TTS failed, status:" + status);
            supportTTS = false;
            return;
        }
        updateTtsLanguage();
        if (supportTTS) {
            textToSpeech.setPitch(1.0f);
            textToSpeech.setSpeechRate(1.0f);
            LogUtil.e(TAG, "onTTSInit() success,locale:" + textToSpeech.getVoice().getLocale());
        }
    }

    /** 更新TTS语言 */
    private void updateTtsLanguage() {
        Locale locale = Locale.ENGLISH;
        int result = textToSpeech.setLanguage(locale);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            supportTTS = false; //系统不支持当前Locale对应的语音播报
            LogUtil.e(TAG, "updateTtsLanguage() failed, TTS not support in locale:" + locale);
        } else {
            supportTTS = true;
            LogUtil.e(TAG, "updateTtsLanguage() success, TTS locale:" + locale);
        }
    }
}


