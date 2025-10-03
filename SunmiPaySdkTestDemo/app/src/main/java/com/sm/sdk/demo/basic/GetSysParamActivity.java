package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.DeviceUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;

import java.util.ArrayList;
import java.util.List;

public class GetSysParamActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_get_sys_param);
        initToolbarBringBack(R.string.basic_get_sys_param);
        initView();
    }

    private void initView() {
        TextView tvInfo = findViewById(R.id.tv_info);
        List<String> keys = new ArrayList<>();
        keys.add(AidlConstantsV2.SysParam.BASE_VERSION);
        keys.add(AidlConstantsV2.SysParam.MSR2_FW_VER);
        keys.add(AidlConstantsV2.SysParam.TERM_STATUS);
        keys.add(AidlConstantsV2.SysParam.DEBUG_MODE);
        keys.add(AidlConstantsV2.SysParam.HARDWARE_VERSION);
        keys.add(AidlConstantsV2.SysParam.FIRMWARE_VERSION);
        keys.add(AidlConstantsV2.SysParam.SM_VERSION);
        keys.add(AidlConstantsV2.SysParam.SUPPORT_ETC);
        keys.add(AidlConstantsV2.SysParam.ETC_FIRM_VERSION);
        keys.add(AidlConstantsV2.SysParam.BootVersion);
        keys.add(AidlConstantsV2.SysParam.CFG_FILE_VERSION);
        keys.add(AidlConstantsV2.SysParam.FW_VERSION);
        keys.add(AidlConstantsV2.SysParam.SN);
        keys.add(AidlConstantsV2.SysParam.PN);
        keys.add(AidlConstantsV2.SysParam.TUSN);
        keys.add(AidlConstantsV2.SysParam.DEVICE_CODE);
        keys.add(AidlConstantsV2.SysParam.DEVICE_MODEL);
        keys.add(AidlConstantsV2.SysParam.RESERVED);
        keys.add(AidlConstantsV2.SysParam.PCD_PARAM_A);
        keys.add(AidlConstantsV2.SysParam.PCD_PARAM_B);
        keys.add(AidlConstantsV2.SysParam.PCD_PARAM_C);
        keys.add(AidlConstantsV2.SysParam.TUSN_KEY_KCV);
        keys.add(AidlConstantsV2.SysParam.PCD_IFM_VERSION);
        keys.add(AidlConstantsV2.SysParam.SAM_COUNT);
        keys.add(AidlConstantsV2.SysParam.SEC_MODE);
        keys.add(AidlConstantsV2.SysParam.SM_TYPE);
        keys.add(AidlConstantsV2.SysParam.PUSH_CFG_FILE);
        keys.add(AidlConstantsV2.SysParam.EMV_VERSION);
        keys.add(AidlConstantsV2.SysParam.PAYPASS_VERSION);
        keys.add(AidlConstantsV2.SysParam.PAYWAVE_VERSION);
        keys.add(AidlConstantsV2.SysParam.QPBOC_VERSION);
        keys.add(AidlConstantsV2.SysParam.ENTRY_VERSION);
        keys.add(AidlConstantsV2.SysParam.MIR_VERSION);
        keys.add(AidlConstantsV2.SysParam.JCB_VERSION);
        keys.add(AidlConstantsV2.SysParam.PAGO_VERSION);
        keys.add(AidlConstantsV2.SysParam.PURE_VERSION);
        keys.add(AidlConstantsV2.SysParam.AE_VERSION);
        keys.add(AidlConstantsV2.SysParam.FLASH_VERSION);
        keys.add(AidlConstantsV2.SysParam.DPAS_VERSION);
        keys.add(AidlConstantsV2.SysParam.APEMV_VERSION);
        keys.add(AidlConstantsV2.SysParam.EMVBASE_VERSION);
        keys.add(AidlConstantsV2.SysParam.KD_VERSION);
        keys.add(AidlConstantsV2.SysParam.EFTPOS_VERSION);
        keys.add(AidlConstantsV2.SysParam.RUPAY_VERSION);
        keys.add(AidlConstantsV2.SysParam.SAMSUNGPAY_VERSION);
        keys.add(AidlConstantsV2.SysParam.EMV_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.PAYPASS_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.PAYWAVE_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.QPBOC_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.ENTRY_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.MIR_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.JCB_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.PAGO_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.PURE_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.AE_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.FLASH_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.DPAS_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.EFTPOS_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.EMVBASE_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.KD_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.RUPAY_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.SAMSUNGPAY_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.FLASH_SIZE);
        keys.add(AidlConstantsV2.SysParam.CARD_HW);
        keys.add(AidlConstants.SysParam.IFM_LIB_VERSION);
        keys.add(AidlConstants.SysParam.MSR_VERSION);
        keys.add(AidlConstants.SysParam.POSAPI_VERSION);
        keys.add(AidlConstants.SysParam.RTC_BAT_VOL_DET);
        StringBuilder sb = new StringBuilder();
        appendSecStatus(sb);//获取安装状态
        addStartTime("getSysParam() total");
        for (String key : keys) {
            String value = null;
            try {
                addStartTime("getSysParam() key=" + key);
                value = MyApplication.app.basicOptV2.getSysParam(key);
                addEndTime("getSysParam() key=" + key);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!key.contains("ReleaseDate")) {
                sb.append(getDisplayKey(key));
                sb.append(":");
                sb.append(value);
                sb.append("\n");
            } else if (!TextUtils.isEmpty(value)) {
                sb.append(value);
                sb.append("\n");
            }
        }
        addEndTime("getSysParam() total");
        getDeviceCardStatus(sb);
        getDeviceSAMCount(sb);
        getDeviceNFCConfig(sb);
        tvInfo.setText(sb);
        showSpendTime();
    }

    /** 获取触发状态 */
    private void appendSecStatus(StringBuilder sb) {
        sb.append("SecStatus:");
        try {
            if (DeviceUtil.isFinanceDevice() || DeviceUtil.isNPDevice()) {
                addStartTime("getSecStatus()");
                int status = MyApplication.app.securityOptV2.getSecStatus();
                addEndTime("getSecStatus()");
                sb.append(Utility.formatStr("%08X", status));
            } else {
                sb.append("null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sb.append("\n");
    }

    /** 获取各种卡状态（功能是否正常） */
    private void getDeviceCardStatus(StringBuilder sb) {
        try {
            //Bit 0：IC卡功能   0：功能正常 1：功能异常
            //Bit 1：SAM卡功能  0：功能正常 1：功能异常
            //Bit 2：NFC卡功能  0：功能正常 1：功能异常
            //Bit 3：磁卡功能   0：功能正常 1：功能异常
            //bit4-7 ：值为0，保留
            //(暂时只支持 toss项目)
            String value = MyApplication.app.basicOptV2.getSysParam(AidlConstantsV2.SysParam.CARD_HW);
            int intValue = Integer.parseInt(value);
            //Bit0：IC card status, 0-normal, 1-error
            sb.append(getString(R.string.basic_ic_status));
            sb.append((intValue & 0x01) == 0 ? getString(R.string.basic_card_status_normal) : getString(R.string.basic_card_status_error));
            sb.append("\n");
            //Bit1：SAM card status, 0-normal, 1-error
            sb.append(getString(R.string.basic_sam_status));
            sb.append((intValue & 0x02) == 0 ? getString(R.string.basic_card_status_normal) : getString(R.string.basic_card_status_error));
            sb.append("\n");
            //Bit2：NFC card status, 0-normal, 1-error
            sb.append(getString(R.string.basic_nfc_status));
            sb.append((intValue & 0x04) == 0 ? getString(R.string.basic_card_status_normal) : getString(R.string.basic_card_status_error));
            sb.append("\n");
            //Bit3：MagStripe card status, 0-normal, 1-error
            sb.append(getString(R.string.basic_mag_status));
            sb.append((intValue & 0x08) == 0 ? getString(R.string.basic_card_status_normal) : getString(R.string.basic_card_status_error));
            sb.append("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 获取显示的key */
    private String getDisplayKey(String key) {
        if (AidlConstants.SysParam.SAM_COUNT.equals(key)) {
            return "SAM count";
        }
        return key;
    }

    /** 获取显示的value */
    private String getDisplayValue(String key, String value) {
        if (key.contains("ReleaseDate")) {
            return Utility.null2String(value);
        }
        return value;
    }

    /** 获取设备SAM卡槽个数 */
    private void getDeviceSAMCount(StringBuilder sb) {
        try {
            //AM卡配置，并返回SAM个数
            //“00”: 表示不支持SAM卡
            //“01”: 表示支持一个SAM卡
            //“02”: 表示支持两个SAM卡
            //“03”: 表示支持三个SAM卡
            //(使用1902安全芯片的机型不支持)
            String value = MyApplication.app.basicOptV2.getSysParam(AidlConstantsV2.SysParam.SAM_COUNT);
            int intValue = Integer.parseInt(value);
            sb.append(getString(R.string.basic_sam_count));
            sb.append(intValue);
            sb.append("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 获取设备NFC配置 */
    private void getDeviceNFCConfig(StringBuilder sb) {
        try {
            //非接卡配置
            //“00”: 表示没有非接模块
            //“01”: 表示有非接模块为RC531
            //“02”: 表示非接模块为PN512
            //“03”: 表示有非接模块为RC663
            //“04”: 表示非接模块为AS3911
            //“06”: 表示非接模块为MH1608C
            //“07”:表示非接模块为PN5190
            //“08”:表示非接模块为ST3916
            //“09”:表示非接模块为ST3917
            //“10”:表示非接模块为FM17660
            //(支持所有机型)
            String value = MyApplication.app.basicOptV2.getSysParam(AidlConstantsV2.SysParam.NFC_CONFIG);
            int intValue = Integer.parseInt(value);
            sb.append(getString(R.string.basic_nfc_config));
            switch (intValue) {
                case 0:
                    sb.append("--");
                    break;
                case 1:
                    sb.append("RC531");
                    break;
                case 2:
                    sb.append("PN512");
                    break;
                case 3:
                    sb.append("RC663");
                    break;
                case 4:
                    sb.append("AS3911");
                    break;
                case 6:
                    sb.append("MH1608C");
                    break;
                case 7:
                    sb.append("PN5190");
                    break;
                case 8:
                    sb.append("ST3916");
                    break;
                case 9:
                    sb.append("ST3917");
                    break;
                case 10:
                    sb.append("FM17660");
                    break;
            }
            sb.append("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
