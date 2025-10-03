package com.sm.sdk.demo.emv;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.emv.wrapper.EMVListenerV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.SettingUtil;
import com.sm.sdk.demo.utils.ThreadPoolUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sm.sdk.demo.wrapper.PinPadListenerV2Wrapper;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidl.AidlConstants.EMV;
import com.sunmi.pay.hardware.aidl.SPErrorCode;
import com.sunmi.pay.hardware.aidl.bean.CardInfo;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.bean.EMVCandidateV2;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadConfigV2;
import com.sunmi.pay.hardware.aidlv2.emv.EMVListenerV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TossEmvActivity extends BaseAppCompatActivity {
    private EditText mEditAmount;
    private TextView mTvShowInfo;
    private Button mBtnOperate;
    private int mCardType;
    private int mAppSelect = 0;
    private String mCardNo; // card number
    private int mPinType;   // 0-online pin, 1-offline pin

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emv_toss);
        initView();
        initData();
    }

    private void initView() {
        initToolbarBringBack("Toss emv");
        mEditAmount = findViewById(R.id.edit_amount);
        mTvShowInfo = findViewById(R.id.tv_info);
        mBtnOperate = findViewById(R.id.mb_ok);
        mBtnOperate.setOnClickListener((v) -> onOkButtonClick());
    }

    private void initData() {
        // disable check card buzzer
        SettingUtil.setBuzzerEnable(false);
        // Client should config their own countryCode,capability,etc...
        ThreadPoolUtil.executeInCachePool(
                () -> {
                    Map<String, String> map = EmvUtil.getConfig(EmvUtil.COUNTRY_CHINA);
                    EmvUtil.initKey();
                    EmvUtil.initAidAndRid();
                    EmvUtil.setTerminalParam(map);
                    showToast("emv init process finished.");
                }
        );
    }

    private void onOkButtonClick() {
        LogUtil.e(Constant.TAG, "***************************************************************");
        LogUtil.e(Constant.TAG, "****************************Start Process**********************");
        LogUtil.e(Constant.TAG, "***************************************************************");
        mTvShowInfo.setText("");
        String amount = mEditAmount.getText().toString();
        try {
            // Before check card, initialize emv process(clear all TLV)
//            MyApplication.app.emvOptV2.initEmvProcess();
//            initEmvTlvData();
            long parseLong = Long.parseLong(amount);
            if (parseLong > 0) {
                int cardType = CardType.MAGNETIC.getValue() | CardType.NFC.getValue() | CardType.IC.getValue();
                checkCard(cardType, true);
            } else {
                showToast(R.string.card_cost_hint);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast(R.string.card_cost_hint);
        }
    }

    /** Set tlv essential tlv data */
    private void initEmvTlvData() {
        try {
            // set PayPass(MasterCard) tlv data
            String[] tagsPayPass = {"DF8117", "DF8118", "DF8119", "DF811F", "DF811E", "DF812C",
                    "DF8123", "DF8124", "DF8125", "DF8126",
                    "DF811B", "DF811D", "DF8122", "DF8120", "DF8121"};
            String[] valuesPayPass = {"E0", "F8", "F8", "E8", "00", "00",
                    "000000000000", "000000100000", "999999999999", "000000100000",
                    "30", "02", "0000000000", "000000000000", "000000000000"};
            MyApplication.app.emvOptV2.setTlvList(EMV.TLVOpCode.OP_PAYPASS, tagsPayPass, valuesPayPass);

            // set AMEX(AmericanExpress) tlv data
            String[] tagsAE = {"9F6D", "9F6E", "9F33", "9F35", "DF8168", "DF8167", "DF8169", "DF8170"};
            String[] valuesAE = {"C0", "D8E00000", "E0E888", "22", "00", "00", "00", "60"};
            MyApplication.app.emvOptV2.setTlvList(EMV.TLVOpCode.OP_AE, tagsAE, valuesAE);

            String[] tagsJCB = {"9F53", "DF8161"};
            String[] valuesJCB = {"708000", "7F00"};
            MyApplication.app.emvOptV2.setTlvList(EMV.TLVOpCode.OP_JCB, tagsJCB, valuesJCB);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Start check card */
    private void checkCard(int cardType, boolean clearMagCache) {
        try {
            showLoadingDialog(R.string.emv_swing_card_ic);
            Bundle bundle = new Bundle();
            bundle.putInt("cardType", cardType);
            bundle.putInt("ctrCode", 0);
            bundle.putInt("code", 1);
            bundle.putInt("type", 2);
            bundle.putInt("maskStart", 3);
            bundle.putInt("maskEnd", 4);
            bundle.putChar("maskChar", '*');
            bundle.putInt("stopOnError", 0);
            bundle.putInt("clearMagCache", clearMagCache ? 1 : 0);
            MyApplication.app.readCardOptV2.checkCardForToss(bundle, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Check card callback */
    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            LogUtil.e(Constant.TAG, "findMagCard:" + bundle);
            mCardType = CardType.MAGNETIC.getValue();
            String track1 = Utility.null2String(bundle.getString("TRACK1"));
            String track2 = Utility.null2String(bundle.getString("TRACK2"));
            String track3 = Utility.null2String(bundle.getString("TRACK3"));
            runOnUiThread(() -> {
                String value = "track1:" + track1 + "\ntrack2:" + track2 + "\ntrack3:" + track3;
                mTvShowInfo.setText(value);
            });
            dismissLoadingDialog();
//            mCardNo = magGetCardNo(track2);
//            if (TextUtils.isEmpty(mCardNo)) {
//                dismissLoadingDialog();
//                showToast(R.string.emv_card_no_error);
//            } else {
//                mPinType = 0;
//                initPinPad();
//            }
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            LogUtil.e(Constant.TAG, "findICCard:" + atr);
            mCardType = CardType.IC.getValue();
            //IC card Beep buzzer when check card success
            MyApplication.app.basicOptV2.buzzerOnDevice(1, 2750, 200, 0);
            transactProcess();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            LogUtil.e(Constant.TAG, "findRFCard:" + uuid);
            mCardType = CardType.NFC.getValue();
            checkNfcIsBankCardOrNot();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            addEndTime("checkCard()");
            String error = "onError:" + message + " -- " + code;
            LogUtil.e(Constant.TAG, error);
            showToast(error);
            dismissLoadingDialog();
        }
    };

    private String magGetCardNo(String track2) {
        if (!TextUtils.isEmpty(track2)) {
            int index = track2.indexOf("=");
            if (index != -1) {
                return track2.substring(0, index);
            }
        }
        return "";
    }

    private void checkNfcIsBankCardOrNot() {
        LogUtil.e(Constant.TAG, "transactProcess");
        try {
            Bundle bundle = new Bundle();
            bundle.putString("amount", mEditAmount.getText().toString());
            bundle.putString("transType", "00");
            //flowType:0x01-emv standard, 0x04：NFC-Speedup
            //Note:(1) flowType=0x04 only valid for QPBOC,PayPass,PayWave contactless transaction
            //     (2) set fowType=0x04, only EMVListenerV2.onRequestShowPinPad(),
            //         EMVListenerV2.onCardDataExchangeComplete() and EMVListenerV2.onTransResult() may will be called.
            bundle.putInt("flowType", EMV.FlowType.TYPE_EMV_BRIEF);
            bundle.putInt("cardType", mCardType);
            MyApplication.app.emvOptV2.transactProcessEx(bundle, new EMVListenerV2Wrapper() {
                @Override
                public void onWaitAppSelect(List<EMVCandidateV2> candList, boolean isFirstSelect) throws RemoteException {
                    LogUtil.e(TAG, "onWaitAppSelect(), candList:" + candList);
                    MyApplication.app.emvOptV2.importAppSelect(0);
                }

                @Override
                public void onAppFinalSelect(String tag9F06Value) throws RemoteException {
                    LogUtil.e(TAG, "onAppFinalSelect(), tag9F06Value:" + tag9F06Value);
                    MyApplication.app.emvOptV2.importAppFinalSelectStatus(0);
                }

                @Override
                public void onConfirmCardNo(String cardNo) throws RemoteException {
                    LogUtil.e(TAG, "onConfirmCardNo(), cardNo:" + cardNo);
                    MyApplication.app.emvOptV2.importCardNoStatus(0);
                }

                @Override
                public void onTransResult(int code, String desc) throws RemoteException {
                    LogUtil.e(TAG, "onTransResult(), code:" + code + ",desc:" + desc);
                    if (code == SPErrorCode.ENDAPPLICATION_TMAPP_EMPTY.getCode()) {//NFC card is not a bankcard, re start check card
                        dismissLoadingDialog();
                        int cardType = CardType.MAGNETIC.getValue() | CardType.IC.getValue() | CardType.NFC.getValue();
                        checkCard(cardType, false);
                    } else {//NFC card is a bankcard, start transact process
                        transactProcess();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Start emv transact process */
    private void transactProcess() {
        LogUtil.e(Constant.TAG, "transactProcess");
        try {
            MyApplication.app.emvOptV2.initEmvProcess();
            initEmvTlvData();

            Bundle bundle = new Bundle();
            bundle.putString("amount", mEditAmount.getText().toString());
            bundle.putString("transType", "00");
            //flowType:0x01-emv standard, 0x04：NFC-Speedup
            //Note:(1) flowType=0x04 only valid for QPBOC,PayPass,PayWave contactless transaction
            //     (2) set fowType=0x04, only EMVListenerV2.onRequestShowPinPad(),
            //         EMVListenerV2.onCardDataExchangeComplete() and EMVListenerV2.onTransResult() may will be called.
            if (mCardType == CardType.NFC.getValue()) {
                bundle.putInt("flowType", EMV.FlowType.TYPE_NFC_SPEEDUP);
            } else {
                bundle.putInt("flowType", EMV.FlowType.TYPE_EMV_STANDARD);
            }
            bundle.putInt("cardType", mCardType);
            MyApplication.app.emvOptV2.transactProcessEx(bundle, mEMVListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * EMV process callback
     */
    private final EMVListenerV2 mEMVListener = new EMVListenerV2.Stub() {
        /**
         * Notify client to do multi App selection, this method may called when card have more than one Application
         * <br/> For Contactless and flowType set as AidlConstants.FlowType.TYPE_NFC_SPEEDUP, this
         * method will not be called
         *
         * @param appNameList   The App list for selection
         * @param isFirstSelect is first time selection
         */
        @Override
        public void onWaitAppSelect(List<EMVCandidateV2> appNameList, boolean isFirstSelect) throws RemoteException {
            addEndTime("onWaitAppSelect()");
            LogUtil.e(Constant.TAG, "onWaitAppSelect isFirstSelect:" + isFirstSelect);
            importAppSelect(0);
        }

        /**
         * Notify client the final selected Application
         * <br/> For Contactless and flowType set as AidlConstants.FlowType.TYPE_NFC_SPEEDUP, this
         * method will not be called
         *
         * @param tag9F06Value The final selected Application id
         */
        @Override
        public void onAppFinalSelect(String tag9F06Value) throws RemoteException {
            addEndTime("onAppFinalSelect()");
            LogUtil.e(Constant.TAG, "onAppFinalSelect tag9F06Value:" + tag9F06Value);
            if (tag9F06Value != null && tag9F06Value.length() > 0) {
                boolean isUnionPay = tag9F06Value.startsWith("A000000333");
                boolean isVisa = tag9F06Value.startsWith("A000000003");
                boolean isMaster = tag9F06Value.startsWith("A000000004")
                        || tag9F06Value.startsWith("A000000005");
                boolean isAmericanExpress = tag9F06Value.startsWith("A000000025");
                boolean isJCB = tag9F06Value.startsWith("A000000065");
                boolean isRupay = tag9F06Value.startsWith("A000000524");
                boolean isPure = tag9F06Value.startsWith("D999999999")
                        || tag9F06Value.startsWith("D888888888")
                        || tag9F06Value.startsWith("D777777777")
                        || tag9F06Value.startsWith("D666666666")
                        || tag9F06Value.startsWith("A000000615");
                String paymentType = "unknown";
                if (isUnionPay) {
                    paymentType = "UnionPay";
                    mAppSelect = 0;
                } else if (isVisa) {
                    paymentType = "Visa";
                    mAppSelect = 1;
                } else if (isMaster) {
                    paymentType = "MasterCard";
                    mAppSelect = 2;
                } else if (isAmericanExpress) {
                    paymentType = "AmericanExpress";
                } else if (isJCB) {
                    paymentType = "JCB";
                } else if (isRupay) {
                    paymentType = "Rupay";
                } else if (isPure) {
                    paymentType = "Pure";
                }
                LogUtil.e(Constant.TAG, "detect " + paymentType + " card");
            }
            importFinalAppSelectStatus(0);
        }

        /**
         * Notify client to confirm card number
         * <br/> For Contactless and flowType set as AidlConstants.FlowType.TYPE_NFC_SPEEDUP, this
         * method will not be called
         *
         * @param cardNo The card number
         */
        @Override
        public void onConfirmCardNo(String cardNo) throws RemoteException {
            LogUtil.e(Constant.TAG, "onConfirmCardNo cardNo:" + cardNo);
            mCardNo = cardNo;
            runOnUiThread(() -> mTvShowInfo.setText(mCardNo));
            importCardNoStatus(0);
        }

        /**
         * Notify client to input PIN
         *
         * @param pinType    The PIN type, 0-online PIN，1-offline PIN
         * @param remainTime The the remain retry times of offline PIN, for online PIN, this param
         *                   value is always -1, and if this is the first time to input PIN, value
         *                   is -1 too.
         */
        @Override
        public void onRequestShowPinPad(int pinType, int remainTime) throws RemoteException {
            LogUtil.e(Constant.TAG, "onRequestShowPinPad pinType:" + pinType + " remainTime:" + remainTime);
            mPinType = pinType;
            if (mCardNo == null) {
                mCardNo = getCardNo();
            }
            initPinPad();
        }

        /**
         * Notify  client to do signature
         */
        @Override
        public void onRequestSignature() throws RemoteException {
            LogUtil.e(Constant.TAG, "onRequestSignature");
            importSignatureStatus(0);
        }

        /**
         * Notify client to do certificate verification
         *
         * @param certType The certificate type, refer to AidlConstants.CertType
         * @param certInfo The certificate info
         */
        @Override
        public void onCertVerify(int certType, String certInfo) throws RemoteException {
            LogUtil.e(Constant.TAG, "onCertVerify certType:" + certType + " certInfo:" + certInfo);
            runOnUiThread(() -> mTvShowInfo.setText(certInfo));
            importCertStatus(0);
        }

        /**
         * Notify client to do online process
         */
        @Override
        public void onOnlineProc() throws RemoteException {
            LogUtil.e(Constant.TAG, "onOnlineProcess");
            mockRequestToServer();
        }

        /**
         * Notify client EMV kernel and card data exchange finished, client can remove card
         */
        @Override
        public void onCardDataExchangeComplete() throws RemoteException {
            LogUtil.e(Constant.TAG, "onCardDataExchangeComplete");
            if (mCardType == CardType.NFC.getValue()) {
                //NFC card Beep buzzer to notify remove card
                MyApplication.app.basicOptV2.buzzerOnDevice(1, 2750, 200, 0);
            }
        }

        /**
         * Notify client EMV process ended
         *
         * @param code The transaction result code, 0-success, 1-offline approval, 2-offline denial,
         *             4-try again, other value-error code
         * @param desc The corresponding message of this code
         */
        @Override
        public void onTransResult(int code, String desc) throws RemoteException {
            if (mCardNo == null) {
                mCardNo = getCardNo();
            }
            LogUtil.e(Constant.TAG, "onTransResult code:" + code + " desc:" + desc);
            LogUtil.e(Constant.TAG, "***************************************************************");
            LogUtil.e(Constant.TAG, "****************************End Process************************");
            LogUtil.e(Constant.TAG, "***************************************************************");
            if (code == 0) {//成功
//                resetUI();
                showToast(R.string.success);
            } else if (code == 4) {//重试
//                tryAgain();
                int cardType = CardType.MAGNETIC.getValue() | CardType.IC.getValue() | CardType.NFC.getValue();
                checkCard(cardType, false);
            } else {//失败
                dismissLoadingDialog();
                showToast("error:" + code + " -- " + desc);
                int cardType = CardType.MAGNETIC.getValue() | CardType.IC.getValue() | CardType.NFC.getValue();
                checkCard(cardType, false);
            }
        }

        /**
         * Notify client the confirmation code verified(See phone)
         */
        @Override
        public void onConfirmationCodeVerified() throws RemoteException {
            addEndTime("onConfirmationCodeVerified()");
            showSpendTime();
            LogUtil.e(Constant.TAG, "onConfirmationCodeVerified");

            byte[] outData = new byte[512];
            int len = MyApplication.app.emvOptV2.getTlv(EMV.TLVOpCode.OP_PAYPASS, "DF8129", outData);
            if (len > 0) {
                byte[] data = new byte[len];
                System.arraycopy(outData, 0, data, 0, len);
                String hexStr = ByteUtil.bytes2HexStr(data);
                LogUtil.e(Constant.TAG, "DF8129: " + hexStr);
            }
            // card off
            MyApplication.app.readCardOptV2.cardOff(mCardType);
            runOnUiThread(() -> new AlertDialog.Builder(TossEmvActivity.this)
                    .setTitle("See Phone")
                    .setMessage("execute See Phone flow")
                    .setPositiveButton("OK", (dia, which) -> {
                                dia.dismiss();
                                // Restart transaction procedure.
                                try {
                                    MyApplication.app.emvOptV2.initEmvProcess();
                                    int cardType = CardType.MAGNETIC.getValue() | CardType.NFC.getValue() | CardType.IC.getValue();
                                    checkCard(cardType, false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                    ).show()
            );
        }

        /**
         * Notify client to exchange data
         * <br/> This method only used for Russia MIR
         *
         * @param cardNo The card number
         */
        @Override
        public void onRequestDataExchange(String cardNo) throws RemoteException {
            addEndTime("onRequestDataExchange()");
            LogUtil.e(Constant.TAG, "onRequestDataExchange,cardNo:" + cardNo);
            MyApplication.app.emvOptV2.importDataExchangeStatus(0);
        }

        @Override
        public void onTermRiskManagement() throws RemoteException {
            addEndTime("onTermRiskManagement()");
            LogUtil.e(Constant.TAG, "onTermRiskManagement");
            MyApplication.app.emvOptV2.importTermRiskManagementStatus(0);
        }

        @Override
        public void onPreFirstGenAC() throws RemoteException {
            addEndTime("onPreFirstGenAC()");
            LogUtil.e(Constant.TAG, "onPreFirstGenAC");
            MyApplication.app.emvOptV2.importPreFirstGenACStatus(0);
        }

        @Override
        public void onDataStorageProc(String[] containerID, String[] containerContent) throws RemoteException {
            addEndTime("onDataStorageProc()");
            LogUtil.e(Constant.TAG, "onDataStorageProc,");
            //此回调为Dpas2.0专用
            //根据需求配置tag及values
            String[] tags = new String[0];
            String[] values = new String[0];
            MyApplication.app.emvOptV2.importDataStorage(tags, values);
        }
    };

    /** getCard number */
    private String getCardNo() {
        LogUtil.e(Constant.TAG, "getCardNo");
        try {
            String[] tagList = {"57", "5A"};
            byte[] outData = new byte[256];
            addStartTime("getCardNo()");
            int len = MyApplication.app.emvOptV2.getTlvList(EMV.TLVOpCode.OP_NORMAL, tagList, outData);
            addEndTime("getCardNo()");
            if (len <= 0) {
                LogUtil.e(Constant.TAG, "getCardNo error,code:" + len);
                return "";
            }
            byte[] bytes = Arrays.copyOf(outData, len);
            Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(bytes);
            if (!TextUtils.isEmpty(Objects.requireNonNull(tlvMap.get("57")).getValue())) {
                TLV tlv57 = tlvMap.get("57");
                CardInfo cardInfo = parseTrack2(tlv57.getValue());
                return cardInfo.cardNo;
            }
            if (!TextUtils.isEmpty(Objects.requireNonNull(tlvMap.get("5A")).getValue())) {
                return Objects.requireNonNull(tlvMap.get("5A")).getValue();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return "";
    }

    /** Parse track2 data */
    public static CardInfo parseTrack2(String track2) {
        LogUtil.e(Constant.TAG, "track2:" + track2);
        String track_2 = stringFilter(track2);
        int index = track_2.indexOf("=");
        if (index == -1) {
            index = track_2.indexOf("D");
        }
        CardInfo cardInfo = new CardInfo();
        if (index == -1) {
            return cardInfo;
        }
        String cardNumber = "";
        if (track_2.length() > index) {
            cardNumber = track_2.substring(0, index);
        }
        String expiryDate = "";
        if (track_2.length() > index + 5) {
            expiryDate = track_2.substring(index + 1, index + 5);
        }
        String serviceCode = "";
        if (track_2.length() > index + 8) {
            serviceCode = track_2.substring(index + 5, index + 8);
        }
        LogUtil.e(Constant.TAG, "cardNumber:" + cardNumber + " expireDate:" + expiryDate + " serviceCode:" + serviceCode);
        cardInfo.cardNo = cardNumber;
        cardInfo.expireDate = expiryDate;
        cardInfo.serviceCode = serviceCode;
        return cardInfo;
    }

    /** remove characters not number,=,D */
    static String stringFilter(String str) {
        String regEx = "[^0-9=D]";
        Pattern p = Pattern.compile(regEx);
        Matcher matcher = p.matcher(str);
        return matcher.replaceAll("").trim();
    }


    private void initPinPad() {
        LogUtil.e(Constant.TAG, "initPinPad");
        try {
            PinPadConfigV2 pinPadConfig = new PinPadConfigV2();
            pinPadConfig.setPinPadType(0);
            pinPadConfig.setPinType(mPinType);
            pinPadConfig.setOrderNumKey(false);
            byte[] panBytes = mCardNo.substring(mCardNo.length() - 13, mCardNo.length() - 1).getBytes(StandardCharsets.US_ASCII);
            pinPadConfig.setPan(panBytes);
            pinPadConfig.setTimeout(60 * 1000); // input password timeout
            pinPadConfig.setPinKeyIndex(12);    // pik index
            pinPadConfig.setMaxInput(12);
            pinPadConfig.setMinInput(0);
            pinPadConfig.setKeySystem(0);
            pinPadConfig.setAlgorithmType(0);
            MyApplication.app.pinPadOptV2.initPinPad(pinPadConfig, mPinPadListener);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, "initPinPad() failure");
            if (mCardType != CardType.MAGNETIC.getValue()) {
                importPinInputStatus(3);
            }
        }
    }

    /** Input pin callback */
    private final PinPadListenerV2 mPinPadListener = new PinPadListenerV2Wrapper() {

        @Override
        public void onPinLength(int len) {
            LogUtil.e(TAG, "onPinLength:" + len);
        }

        @Override
        public void onConfirm(int i, byte[] pinBlock) {
            if (mCardType == CardType.MAGNETIC.getValue()) {
                mockRequestToServer();
                return;
            }
            if (pinBlock != null) {
                String hexStr = ByteUtil.bytes2HexStr(pinBlock);
                LogUtil.e(TAG, "onConfirm pin block:" + hexStr);
                importPinInputStatus(0);
            } else {
                importPinInputStatus(2);
            }
        }

        @Override
        public void onCancel() {
            LogUtil.e(TAG, "onCancel");
            if (mCardType != CardType.MAGNETIC.getValue()) {
                importPinInputStatus(1);
            }
        }

        @Override
        public void onError(int code) {
            String msg = AidlErrorCodeV2.valueOf(code).getMsg();
            LogUtil.e(TAG, "onError:" + code + ",msg:" + msg);
            if (mCardType != CardType.MAGNETIC.getValue()) {
                importPinInputStatus(3);
            }
        }
    };

    /**
     * Notify emv process the Application select result
     *
     * @param selectIndex the index of selected App, start from 0
     */
    private void importAppSelect(int selectIndex) {
        LogUtil.e(Constant.TAG, "importAppSelect selectIndex:" + selectIndex);
        try {
            MyApplication.app.emvOptV2.importAppSelect(selectIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Notify emv process the final Application select result
     *
     * @param status 0:success, other value:failed
     */
    private void importFinalAppSelectStatus(int status) {
        try {
            LogUtil.e(Constant.TAG, "importFinalAppSelectStatus status:" + status);
            MyApplication.app.emvOptV2.importAppFinalSelectStatus(status);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Notify emv process the card number confirm status
     *
     * @param status 0:success, other value:failed
     */
    private void importCardNoStatus(int status) {
        LogUtil.e(Constant.TAG, "importCardNoStatus status:" + status);
        try {
            MyApplication.app.emvOptV2.importCardNoStatus(status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Notify emv process the certification verify status
     *
     * @param status 0:success, other value:failed
     */
    private void importCertStatus(int status) {
        LogUtil.e(Constant.TAG, "importCertStatus status:" + status);
        try {
            MyApplication.app.emvOptV2.importCertStatus(status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Notify emv process the PIN input result
     *
     * @param inputResult 0:success,1:input PIN canceled,2:input PIN skipped,3:PINPAD problem,4:input PIN timeout
     */
    private void importPinInputStatus(int inputResult) {
        LogUtil.e(Constant.TAG, "importPinInputStatus:" + inputResult);
        try {
            MyApplication.app.emvOptV2.importPinInputStatus(mPinType, inputResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Import signature result to emv process
     *
     * @param status 0:success, other value:failed
     */
    private void importSignatureStatus(int status) {
        LogUtil.e(Constant.TAG, "importSignatureStatus status:" + status);
        try {
            MyApplication.app.emvOptV2.importSignatureStatus(status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Import online process result data(eg: field 55 ) to emv process.
     * if no date to import, set param tags and values as empty array
     *
     * @param status 0:online approval, 1:online denial, 2:online failed
     */
    private void importOnlineProcessStatus(int status) {
        LogUtil.e(Constant.TAG, "importOnlineProcessStatus status:" + status);
        try {
            String[] tags = {"71", "72", "91", "8A", "89"};
            String[] values = {"", "", "", "", ""};
            byte[] out = new byte[1024];
            int len = MyApplication.app.emvOptV2.importOnlineProcStatus(status, tags, values, out);
            if (len < 0) {
                LogUtil.e(Constant.TAG, "importOnlineProcessStatus error,code:" + len);
            } else {
                byte[] bytes = Arrays.copyOf(out, len);
                String hexStr = ByteUtil.bytes2HexStr(bytes);
                LogUtil.e(Constant.TAG, "importOnlineProcessStatus outData:" + hexStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Mock a POSP to do some data exchange(online process), we don't have a really POSP,
     * client should connect to a really POSP at this step.
     */
    private void mockRequestToServer() {
        ThreadPoolUtil.executeInCachePool(() -> {
            try {
                showLoadingDialog(R.string.requesting);
                if (mCardType == CardType.MAGNETIC.getValue()) {
                    showLoadingDialog(R.string.requesting);
                    Thread.sleep(1000);
                    showToast(R.string.success);
                } else {
                    getTlvData();
                    Thread.sleep(1000);
                    // notice  ==  import the online result to SDK and end the process.
                    importOnlineProcessStatus(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (mCardType != CardType.MAGNETIC.getValue()) {
                    importOnlineProcessStatus(-1);
                }
            } finally {
                dismissLoadingDialog();
            }
        });
    }

    /** Read we interested tlv data */
    private void getTlvData() {
        try {
            String[] tagList = {
                    "DF02", "5F34", "9F06", "FF30", "FF31", "95", "9B", "9F36", "9F26",
                    "9F27", "DF31", "5A", "57", "5F24", "9F1A", "9F33", "9F35", "9F40",
                    "9F03", "9F10", "9F37", "9C", "9A", "9F02", "5F2A", "82", "9F34", "9F1E",
                    "84", "4F", "9F66", "9F6C", "9F09", "9F41", "9F63", "5F20", "9F12", "50",
            };
            byte[] outData = new byte[2048];
            Map<String, TLV> map = new TreeMap<>();
            int tlvOpCode;
            if (CardType.NFC.getValue() == mCardType) {
                if (mAppSelect == 2) {
                    tlvOpCode = EMV.TLVOpCode.OP_PAYPASS;
                } else if (mAppSelect == 1) {
                    tlvOpCode = EMV.TLVOpCode.OP_PAYWAVE;
                } else {
                    tlvOpCode = EMV.TLVOpCode.OP_NORMAL;
                }
            } else {
                tlvOpCode = EMV.TLVOpCode.OP_NORMAL;
            }
            int len = MyApplication.app.emvOptV2.getTlvList(tlvOpCode, tagList, outData);
            if (len > 0) {
                byte[] bytes = Arrays.copyOf(outData, len);
                String hexStr = ByteUtil.bytes2HexStr(bytes);
                Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(hexStr);
                map.putAll(tlvMap);
            }
            // payPassTags
            String[] payPassTags = {
                    "DF811E", "DF812C", "DF8118", "DF8119", "DF811F", "DF8117", "DF8124",
                    "DF8125", "9F6D", "DF811B", "9F53", "DF810C", "9F1D", "DF8130", "DF812D",
                    "DF811C", "DF811D", "9F7C",
            };
            len = MyApplication.app.emvOptV2.getTlvList(EMV.TLVOpCode.OP_PAYPASS, payPassTags, outData);
            if (len > 0) {
                byte[] bytes = Arrays.copyOf(outData, len);
                String hexStr = ByteUtil.bytes2HexStr(bytes);
                Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(hexStr);
                map.putAll(tlvMap);
            }

            final StringBuilder sb = new StringBuilder();
            Set<String> keySet = map.keySet();
            for (String key : keySet) {
                TLV tlv = map.get(key);
                sb.append(key);
                sb.append(":");
                if (tlv != null) {
                    String value = tlv.getValue();
                    sb.append(value);
                }
                sb.append("\n");
            }
            runOnUiThread(() -> mTvShowInfo.setText(sb));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetUI() {
        runOnUiThread(() -> {
            mEditAmount.setText("");
            dismissLoadingDialog();
        });
    }

    private void tryAgain() {
        runOnUiThread(() -> new AlertDialog.Builder(this)
                .setTitle("Try again")
                .setMessage("Please read the card again")
                .setPositiveButton("OK", (dia, which) -> {
                    dia.dismiss();
                    int cardType = CardType.MAGNETIC.getValue() | CardType.IC.getValue() | CardType.NFC.getValue();
                    checkCard(cardType, false);
                }).show());
    }

}
