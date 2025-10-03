package com.sm.sdk.demo.biometric;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;

import java.util.Arrays;
import java.util.regex.Pattern;

public class BiometricActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometric);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.biometric_manager);
        findViewById(R.id.btn_register).setOnClickListener(v -> faceFeatureRegister());
        findViewById(R.id.btn_delete).setOnClickListener((v) -> deleteFaceFeature());
        findViewById(R.id.btn_search).setOnClickListener(v -> searchFaceFeature());
        findViewById(R.id.btn_gen_register_feature_data).setOnClickListener(v -> genTestData());
        findViewById(R.id.btn_copy_search_feature_data).setOnClickListener(v -> copyTestData());

    }

    private void genTestData() {
        EditText etFeature = findViewById(R.id.edit_face_feature);
        float[] srcTest = new float[]{0.0035723823F, 0.16852678F, -0.015985109F, 0.0061068526F, -0.04351306F, 0.09484165F, -0.026529485F, 0.026392635F, 0.0872919F, 0.018768018F, 0.014578089F, 0.060745098F, -0.059039377F, -0.01801939F, -0.010961723F, -0.10087655F, 0.051175456F, 0.065237984F, -0.02976189F, -0.049013216F, 0.045014028F, 0.06850955F, 0.055180103F, 0.032126557F, 0.04597977F, -0.02545828F, -0.014685949F, -0.04441495F, 0.041876044F, -0.018782008F, 0.012808201F, 0.034017343F, 0.010780668F, -0.0263211F, -0.011166506F, 0.0029245496F, -0.065084115F, -0.01574281F, 0.034215968F, -0.03303322F, 0.040885195F, 0.005797105F, -0.010317375F, -0.018303797F, 0.0037841527F, -0.08088664F, 0.03513265F, -0.056480583F, -0.014677539F, -0.061026327F, -0.026469976F, 0.047686074F, 0.051599476F, -0.031877305F, 0.1475494F, 0.07086707F, -0.062719814F, 0.015175585F, 0.018720113F, -0.0122526605F, -0.016546872F, -0.01113858F, 0.001773304F, -0.0049082367F, -0.009331692F, -0.031120053F, -0.051572923F, 0.0034321472F, 0.0050693057F, -0.0053273938F, -0.015090572F, 0.04984996F, -0.01641243F, 0.049663227F, 0.064742096F, 0.011845147F, -0.0311404F, -0.050058495F, -0.032054957F, -0.06917009F, -0.0725311F, -0.049936365F, 0.04205391F, 0.001136112F, 0.1331373F, 0.009888825F, 0.044511635F, -0.009339631F, 0.06634322F, -0.055507313F, 0.0014744273F, -0.016486723F, 0.03602717F, -0.0030992678F, -0.03971523F, -0.04856872F, 0.07838553F, 0.0052611963F, -0.006926513F, 0.039681897F, -0.03772184F, -0.0727479F, 0.03629888F, 0.0139427325F, -0.044364464F, -0.07800992F, 0.044459917F, -0.016528748F, 0.0047740787F, 0.110052615F, -0.0079606F, -0.05036672F, 0.047012594F, -0.10070386F, 0.02292763F, -0.0026014282F, 0.042296562F, -0.013595114F, -0.0017750505F, 0.024663303F, 0.016277675F, 0.07038324F, -0.0034478682F, 0.06842181F, -0.0210301F, 0.032905653F, 0.058620647F, -0.022356123F, 0.028345635F, 0.019073335F, -0.019155711F, 0.06961684F, -0.0033471445F, -0.058934942F, -0.008567831F, 0.034049455F, -0.055086557F, 0.011376159F, 0.033898167F, -0.021830069F, -0.03241905F, 0.0107072145F, 0.10280076F, -0.013541408F, 0.0025138326F, 0.04178333F, -0.050949175F, 0.06546783F, 0.034512512F, -0.0010644861F, 0.007639094F, 0.0078540575F, 0.020247715F, -0.0035360374F, -0.018620756F, -0.01217756F, -0.049012657F, -0.010289857F, -0.10119606F, -0.042860135F, -0.0314624F, -0.031236855F, -0.029472107F, 0.03988063F, 0.014211534F, -0.019994276F, -0.050005686F, 0.004884572F, -0.004112818F, -0.052118663F, 0.016554177F, 0.050966293F, -0.027079348F, -0.007941429F, -0.015420984F, 0.053280335F, 0.022683062F, 0.038305048F, -0.061791368F, -0.04233661F, -0.004392047F, -0.078372695F, 0.05202521F, -0.03573093F, 0.038631875F, 0.0032739206F, 0.0421501F, 0.018938232F, 0.011512216F, 0.07489826F, -0.023465736F, -0.014647933F, -0.07502032F, -0.043293983F, -0.022731747F, -0.030852435F, 0.0018714837F, 0.008898947F, 0.017971514F, -0.02305019F, -0.019989073F, 0.019682964F, 0.05290714F, 0.019989977F, -0.012653935F, 0.013469891F, -0.006049265F, -0.0034602864F, 0.0029530057F, 0.024130365F, 0.07525914F, -0.010790729F, -0.014307244F, 0.07590471F, -0.05776048F, 0.07278116F, 0.05946355F, 0.051515684F, -0.0056447224F, -0.03315278F, 0.04433097F, -0.0024044588F, 0.07817023F, -0.016311266F, -0.058061946F, 0.1077435F, 0.021231467F, -0.033193324F, 0.008205095F, -0.061704308F, 0.05688566F, -0.004326769F, 0.016506571F, 0.063930556F, -0.04017807F, 0.0018154129F, 0.07238171F, 0.0038360478F, 0.01669393F, -0.020869812F, 0.03314598F, 0.049873404F, 0.05979519F, -0.007939085F, -0.057868823F, -0.016035728F, 0.06522948F, -0.02975291F, -0.055069733F, -0.018395044F, -0.0669611F, -0.0021420014F, 0.062119536F, 0.08296572F, -0.03123741F, -0.0262533F, 0.07059816F, -0.020635448F, -0.0045735105F, 0.016418817F, 0.018416945F, 0.059843477F, 0.029361833F, -0.05535628F, 0.0026969174F, 0.067679904F, -0.0311164F, -0.031453516F, -0.006250534F, -0.023557855F, 0.06847417F, -0.06951611F, 0.044064306F, 0.009984458F, 0.0555717F, 0.025747875F, -0.0669254F, -0.0050921356F, 0.013827761F, -0.07767992F, -0.029155616F, -0.0026177624F, 0.12010121F, -0.0064288173F, 0.014157524F, -0.062289678F, 0.038096502F, -0.04727409F, 0.012370778F, -0.0712896F, 0.01594664F, 0.015017189F, -0.070590384F, 0.0037692816F, 0.021095399F, 0.020593556F, -0.017832907F, -0.014580753F, 0.06765293F, -0.035022F, -0.036232747F, 0.022533704F, -0.034326453F, 0.037217766F, -0.023458183F, -0.024285039F, -0.05826331F, -0.008056934F, -0.039045088F, 0.010214354F, 0.061874237F, -0.095117524F, -9.500765e-05F, -0.022226837F, 0.02137997F, 0.024665987F, 0.088035434F, -0.09062526F, -0.06670338F, 0.0012850107F, 0.008521717F, 0.03687179F, 0.03122268F, -0.060038067F, -0.03886467F, 0.010517714F, -0.006455168F, 0.004059275F, -0.004132391F, -0.021415383F, -0.032444768F, -0.008891782F, 0.047163446F, 0.014276534F, 0.017163664F, 0.017855478F, 0.006993332F, 0.056704655F, -0.013051955F, -0.024315562F, -0.03435958F, -0.0028253112F, 0.024229204F, -0.022298839F, -0.106773764F, -0.0072847917F, -0.010214993F, -0.038951844F, -0.03193261F, 0.082084484F, 0.023005309F, -0.014682922F, -0.04840538F, 0.005104446F, -0.05487589F, -0.043938514F, -0.02517763F, -0.03604898F, 0.08920385F, -0.048680644F, 0.023830326F, 0.028598744F, -0.07869849F, 0.098472364F, 0.03715024F, -0.021378666F, -0.0077850316F, -0.014703305F, 0.042910434F, -0.01387259F, -0.015374145F, -0.045943204F, -0.022664262F, -0.044044897F, -0.053678177F, 0.00734305F, -0.057537954F, 0.013222705F, -0.01852263F, -0.019584203F, 0.018547256F, 0.057002544F, -0.028350446F, -0.007532884F, -0.0151902F, -0.014934936F, -0.0025421185F, -0.026977373F, 7.3173076e-05F, -0.005116835F, -0.018195665F, 0.06875886F, 0.03268557F, -0.040663425F, -0.055970136F, 0.088830546F, 0.0055561797F, 0.060146097F, 0.04809344F, -0.0058406168F, -0.020658974F, -0.03415475F, 0.041430246F, 0.0139134675F, 0.0038537153F, 0.024912613F, 0.0026242388F, -0.06649773F, 0.043940395F, -0.02958598F, -0.0015828722F, 0.04293269F, 0.003822808F, 0.016893491F, 0.067768715F, -0.026497388F, 0.051179107F, -0.032669608F, 0.06417192F, 0.056281846F, -0.04526612F, 0.0058402675F, -0.032904353F, 0.028865686F, -0.0077273967F, 0.032709934F, 0.081888F, 0.04290857F, 0.021398708F, -0.05334359F, 0.09501293F, 0.058689933F, -0.01563374F, -0.016246503F, 0.011479127F, -0.074585386F, 0.016396716F, 0.041488867F, -0.031483267F, 0.0147108305F, 0.010821127F, -0.015329186F, -0.028483752F, 0.05844903F, 0.08246804F, -0.031092254F, 0.03330206F, -0.046759296F, 0.042631734F, 0.006002458F, -0.025510686F, 0.017006561F, -0.023494048F, -0.011812544F, -0.026801655F, 0.08774766F, 0.0011453548F, 0.02019028F, 0.027511215F, 0.015051818F, 0.054630112F, 0.045231F, -0.039819516F, 0.0877658F, -0.0022095032F, 0.028351717F, -0.0573522F, 0.09226976F, 0.00054443826F, 0.007897559F, 0.107306845F, -0.06692497F, -0.048415743F, -0.020963727F, 0.051818002F, 0.10041266F, -0.017149404F, -0.0077085127F, 0.008368827F, -0.024733635F, -0.0008555115F, 0.05958225F, 0.03246009F, -0.011167974F, 0.0094385985F, 0.097160675F, 0.00956533F, -0.07846253F, 0.017662277F, -0.060322523F, 0.009053055F, -0.052242048F, -0.0015003353F, -0.030971125F, 0.0762678F, 0.05082127F, 0.034945402F, -0.055993848F, -0.08674468F, 0.00107988F, 0.013082701F, 0.01577973F, 0.073253006F, 0.020587305F, -0.049464438F, -0.010179514F, 0.060751434F, -0.029774839F, -0.016470939F, 0.064288825F, -0.04515154F, -0.0049955016F};
        String data = Arrays.toString(srcTest);
        data = data.substring(1, data.length() - 1);
        etFeature.setText(data);
    }

    private void copyTestData() {
        EditText etSearchFeature = findViewById(R.id.edit_search_feature);
        EditText etRegisterFeature = findViewById(R.id.edit_face_feature);
        etSearchFeature.setText(etRegisterFeature.getText());
    }

    private void faceFeatureRegister() {
        showToast(R.string.invoking_not_support);
//        TextView textView = findViewById(R.id.txt_biometric_register);
//        try {
//            EditText etId = findViewById(R.id.edit_user_id);
//            EditText etFeature = findViewById(R.id.edit_face_feature);
//            String hexUserId = etId.getText().toString();
//            if (!checkHexValue(hexUserId)) {
//                etId.requestFocus();
//                showToast("user id should be hex characters!");
//                return;
//            }
//            if (hexUserId.length() != 64) {
//                etId.requestFocus();
//                showToast("user id length should be 64 characters!");
//                return;
//            }
//
//            String strFeatureData = etFeature.getText().toString();
//            if (TextUtils.isEmpty(strFeatureData)) {
//                etFeature.requestFocus();
//                showToast("face feature data is not allowed to be empty!");
//                return;
//            }
//            String[] arrayFeatureData = strFeatureData.split(",");
//            if (arrayFeatureData.length != 512) {
//                etFeature.requestFocus();
//                showToast("face feature data data is illegal!");
//                return;
//            }
//            float[] featureData = new float[arrayFeatureData.length];
//            int i = 0;
//            for (String s : arrayFeatureData) {
//                featureData[i] = Float.parseFloat(s);
//                i++;
//            }
//            int code = MyApplication.app.mBiometricManagerV2.sysFaceRegisterFeature(ByteUtil.hexStr2Bytes(hexUserId), featureData);
//            StringBuilder info = new StringBuilder();
//            info.append("sysFaceRegisterFeature result:").append(code);
//            textView.setText(info.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//            textView.setText("exception:" + e.getMessage());
//        }
    }

    private void deleteFaceFeature() {
        showToast(R.string.invoking_not_support);
//        try {
//            TextView textView = findViewById(R.id.txt_biometric_delete);
//            try {
//                EditText etId = findViewById(R.id.edit_deleted_user_id);
//                String hexUserId = etId.getText().toString();
//                if (!checkHexValue(hexUserId)) {
//                    etId.requestFocus();
//                    showToast("user id should be hex characters!");
//                    return;
//                }
//                if (hexUserId.length() != 64) {
//                    etId.requestFocus();
//                    showToast("user id length should be 64 characters!");
//                    return;
//                }
//                int code = MyApplication.app.mBiometricManagerV2.sysDeleterFeature(ByteUtil.hexStr2Bytes(hexUserId));
//                StringBuilder info = new StringBuilder();
//                info.append("sysDeleterFeature result:").append(code);
//                textView.setText(info);
//            } catch (Exception e) {
//                e.printStackTrace();
//                textView.setText("exception:" + e.getMessage());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void searchFaceFeature() {
        showToast(R.string.invoking_not_support);
//        TextView textView = findViewById(R.id.txt_biometric_search);
//        try {
//            EditText editThreshold = findViewById(R.id.edit_search_threshold);
//            EditText etFeature = findViewById(R.id.edit_search_feature);
//            float threshold;
//            try {
//                threshold = Float.parseFloat(editThreshold.getText().toString());
//            } catch (Exception e) {
//                e.printStackTrace();
//                textView.setText("threshold must be float");
//                return;
//            }
//            String strFeatureData = etFeature.getText().toString();
//            if (TextUtils.isEmpty(strFeatureData)) {
//                etFeature.requestFocus();
//                showToast("face feature data is not allowed to be empty!");
//                return;
//            }
//            String[] arrayFeatureData = strFeatureData.split(",");
//            if (arrayFeatureData.length != 512) {
//                etFeature.requestFocus();
//                showToast("face feature data data is illegal!");
//                return;
//            }
//            float[] featureData = new float[arrayFeatureData.length];
//            int i = 0;
//            for (String s : arrayFeatureData) {
//                featureData[i] = Float.parseFloat(s);
//                i++;
//            }
//            byte[] outData = new byte[1024];
//            int len = MyApplication.app.mBiometricManagerV2.sysSearchFeature(featureData, threshold, outData);
//            StringBuilder info = new StringBuilder();
//            if (len > 0) {
//                info = info.append("sysSearchFeature result:").append(ByteUtil.bytes2HexStr(Arrays.copyOf(outData, len)));
//            } else {
//                info = info.append("sysSearchFeature result:").append(len);
//            }
//            textView.setText(info);
//        } catch (Exception e) {
//            e.printStackTrace();
//            textView.setText("exception:" + e.getMessage());
//        }
    }

    private boolean checkHexValue(String src) {
        if (TextUtils.isEmpty(src)) {
            return false;
        }
        return Pattern.matches("[0-9a-fA-F]+", src);
    }

}
