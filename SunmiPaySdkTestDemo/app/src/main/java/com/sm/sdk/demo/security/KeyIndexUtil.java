package com.sm.sdk.demo.security;

import com.sm.sdk.demo.utils.DeviceUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;

public final class KeyIndexUtil {
    private KeyIndexUtil() {
        throw new AssertionError("create instance of KeyIndexUtil is prohibited");
    }

    /**
     * 检查密钥索引
     *
     * @param keySystem 0-mksk，1-dukpt，2-RSA，3-SM2
     * @return true-成功，false-失败
     */
    public static boolean checkKeyIndex(int keySystem, int keyIndex) {
        switch (keySystem) {
            case Security.SEC_MKSK:
                return checkMkskKeyIndex(keyIndex);
            case Security.SEC_DUKPT:
                return checkDukptKeyIndex(keyIndex);
            case Security.SEC_RSA_KEY:
                return checkRsaKeyIndex(keyIndex);
            case Security.SEC_SM2_KEY:
                return checkSm2KeyIndex(keyIndex);
        }
        return false;
    }

    public static boolean checkMkskOrDukptKeyIndex(int keyIndex, boolean isDukpt) {
        return isDukpt ? checkDukptKeyIndex(keyIndex) : checkMkskKeyIndex(keyIndex);
    }

    public static boolean checkMkskKeyIndex(int keyIndex) {
        return keyIndex >= 0 && keyIndex <= 199;
    }

    public static boolean checkDukptKeyIndex(int keyIndex) {
        if (DeviceUtil.isBrazilCKD()) {
            return keyIndex >= 0 && keyIndex <= 199;
        }
        return keyIndex >= 0 && keyIndex <= 19
                || keyIndex >= 1100 && keyIndex <= 1199
                || keyIndex >= 2100 && keyIndex <= 2199;
    }

    public static boolean checkDukpt3DesKeyIndex(int keyIndex) {
        if (DeviceUtil.isBrazilCKD()) {
            return keyIndex >= 0 && keyIndex <= 99;
        }
        return keyIndex >= 0 && keyIndex <= 9
                || keyIndex >= 1100 && keyIndex <= 1199;
    }

    public static boolean checkDukptAesKeyIndex(int keyIndex) {
        if (DeviceUtil.isBrazilCKD()) {
            return keyIndex >= 100 && keyIndex <= 199;
        }
        return keyIndex >= 10 && keyIndex <= 19
                || keyIndex >= 2100 && keyIndex <= 2199;
    }

    public static boolean checkRsaKeyIndex(int keyIndex) {
        return keyIndex >= 0 && keyIndex <= 19;
    }

    public static boolean checkSm2KeyIndex(int keyIndex) {
        return keyIndex >= 0 && keyIndex <= 9;
    }
}
