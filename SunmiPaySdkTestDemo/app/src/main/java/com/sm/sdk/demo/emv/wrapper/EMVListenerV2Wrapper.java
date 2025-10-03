package com.sm.sdk.demo.emv.wrapper;

import android.os.RemoteException;
import android.util.Log;

import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.bean.EMVCandidateV2;
import com.sunmi.pay.hardware.aidlv2.emv.EMVListenerV2;

import java.util.List;

public class EMVListenerV2Wrapper extends EMVListenerV2.Stub {
    private static final String TAG = "EMVListenerV2Wrapper";
    @Override
    public void onWaitAppSelect(List<EMVCandidateV2> candList, boolean isFirstSelect) throws RemoteException {
        LogUtil.e(TAG,"onWaitAppSelect()");
    }

    @Override
    public void onAppFinalSelect(String tag9F06Value) throws RemoteException {
        LogUtil.e(TAG,"onAppFinalSelect()");
    }

    @Override
    public void onConfirmCardNo(String cardNo) throws RemoteException {
        LogUtil.e(TAG,"onConfirmCardNo()");
    }

    @Override
    public void onRequestShowPinPad(int pinType, int remainTimes) throws RemoteException {
        LogUtil.e(TAG,"onRequestShowPinPad()");
    }

    @Override
    public void onRequestSignature() throws RemoteException {
        LogUtil.e(TAG,"onRequestSignature()");
    }

    @Override
    public void onCertVerify(int certType, String certInfo) throws RemoteException {
        LogUtil.e(TAG,"onCertVerify()");
    }

    @Override
    public void onOnlineProc() throws RemoteException {
        LogUtil.e(TAG,"onOnlineProc()");
    }

    @Override
    public void onCardDataExchangeComplete() throws RemoteException {
        LogUtil.e(TAG,"onCardDataExchangeComplete()");
    }

    @Override
    public void onTransResult(int code, String desc) throws RemoteException {
        LogUtil.e(TAG,"onTransResult()");
    }

    @Override
    public void onConfirmationCodeVerified() throws RemoteException {
        LogUtil.e(TAG,"onConfirmationCodeVerified()");
    }

    @Override
    public void onRequestDataExchange(String cardNo) throws RemoteException {
        LogUtil.e(TAG,"onRequestDataExchange()");
    }

    @Override
    public void onTermRiskManagement() throws RemoteException {
        LogUtil.e(TAG,"onTermRiskManagement()");
    }

    @Override
    public void onPreFirstGenAC() throws RemoteException {
        LogUtil.e(TAG,"onPreFirstGenAC()");
    }

    @Override
    public void onDataStorageProc(String[] containerID, String[] containerContent) throws RemoteException {
        LogUtil.e(TAG,"onDataStorageProc()");
    }
}
