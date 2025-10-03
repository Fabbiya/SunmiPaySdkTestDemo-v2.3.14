package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.CMacUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MifarePlusTransmitApduActivity extends BaseAppCompatActivity {
    private byte[] Kenc;
    private byte[] Kmac;
    private byte[] TI;
    private short R_Ctr;
    private short W_Ctr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_mifareplus_transmit_apdu);
        initView();
        checkCard();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_mifare_plus_transmit_apdu);
        findViewById(R.id.mb_auth).setOnClickListener(this);
        findViewById(R.id.mb_read).setOnClickListener(this);
        findViewById(R.id.mb_write).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_auth:
                onAuthenticateClick();
                break;
            case R.id.mb_read:
                onReadBlockClick();
                break;
            case R.id.mb_write:
                onWriteBlockClick();
                break;
        }
    }

    private void checkCard() {
        try {
            showSwingCardHintDialog(0);
            addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCard(CardType.NFC.getValue(), mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findMagCard");
            dismissSwingCardHintDialog();
            showSpendTime();
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findICCard:" + atr);
            dismissSwingCardHintDialog();
            showSpendTime();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findRFCard:" + uuid);
            dismissSwingCardHintDialog();
            showSpendTime();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            addEndTime("checkCard()");
            showSpendTime();
            dismissSwingCardHintDialog();
            String tip = "check card failed, code:" + code + ",msg:" + message;
            LogUtil.e(TAG, tip);
            showToast(tip);
        }
    };

    private void onAuthenticateClick() {
        EditText edtBlock = findViewById(R.id.edt_auth_block_no);
        EditText edtKey = findViewById(R.id.edt_auth_key);
        String blockStr = edtBlock.getText().toString();
        String keyStr = edtKey.getText().toString();
        if (TextUtils.isEmpty(blockStr)) {
            showToast("block number shouldn't be empty");
            edtBlock.requestFocus();
            return;
        }
        if (!Utility.checkHexValue(blockStr)) {
            showToast("block number format error");
            edtBlock.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(keyStr)) {
            showToast("key shouldn't be empty");
            edtKey.requestFocus();
            return;
        }
        if (!Utility.checkHexValue(keyStr)) {
            showToast("key format error");
            edtKey.requestFocus();
            return;
        }
        int block = Integer.parseInt(blockStr, 16);
        byte[] key = ByteUtil.hexStr2Bytes(keyStr);
        mifarePlusFirstAuthentication(block, 0, key);
//        followingAuthentication(block, 0, key);
    }

    private void onReadBlockClick() {
        EditText edtBlock = findViewById(R.id.edt_read_block_no);
        EditText edtData = findViewById(R.id.edt_read_block_data);
        String blockStr = edtBlock.getText().toString();
        if (TextUtils.isEmpty(blockStr)) {
            showToast("block number shouldn't be empty");
            edtBlock.requestFocus();
            return;
        }
        if (!Utility.checkHexValue(blockStr)) {
            showToast("block number format error");
            edtBlock.requestFocus();
            return;
        }
        int block = Integer.parseInt(blockStr, 16);
        byte[] data = mifarePlusReadBlock(block);
        edtData.setText(ByteUtil.bytes2HexStr(data));
    }

    private void onWriteBlockClick() {
        EditText edtBlock = findViewById(R.id.edt_write_block_no);
        EditText edtData = findViewById(R.id.edt_write_block_data);
        String blockStr = edtBlock.getText().toString();
        String dataStr = edtData.getText().toString();
        if (TextUtils.isEmpty(blockStr)) {
            showToast("block number shouldn't be empty");
            edtBlock.requestFocus();
            return;
        }
        if (!Utility.checkHexValue(blockStr)) {
            showToast("block number format error");
            edtBlock.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(dataStr)) {
            showToast("data shouldn't be empty");
            edtData.requestFocus();
            return;
        }
        if (!Utility.checkHexValue(dataStr)) {
            showToast("data format error");
            edtData.requestFocus();
            return;
        }
        int block = Integer.parseInt(blockStr, 16);
        byte[] data = ByteUtil.hexStr2Bytes(dataStr);
        mifarePlusWriteBlock(block, data);
    }

    /**
     * Mifare plus first authentication
     *
     * @param block   block number
     * @param keyType key type, 0-KeyA, 1-KeyB
     * @param key     key value, default key is 12 bytes 0xff
     */
    private void mifarePlusFirstAuthentication(int block, int keyType, byte[] key) {
        String msg = null;
        //1.first authentication-1st
        int sector = block / 4;
        byte cmd = 0x70;//cmd
        int sectorKey = 0;
        if (keyType == 0) {//KeyA
            sectorKey = sector * 2;
        } else if (keyType == 1) {//KeyB
            sectorKey = sector * 2 + 1;
        }
        byte lenCap = 0x01;
        byte PCDCap2 = 0x00;
        byte[] send = {cmd, (byte) sectorKey, 0x40, lenCap, PCDCap2};
        byte[] recv = _transmitApdu(send);
        if (recv.length < 0x11) {//SC(1B)+E(Kx,RndB)(16B)
            msg = "\nfirst authentication-1st recv data length error";
            showToast(msg);
            Log.e(TAG, msg);
            return;
        }
        //generate 16 bytes random data
        Random ran = new Random();
        byte[] RndA = new byte[16];
        ran.nextBytes(RndA);
        //parse SC and RndB
        int sc = recv[0] & 0xff;
        if (sc != 0x90) {
            msg = "\nfirst authentication-1st  error, statusCode:" + hexStatusCode(sc);
            showToast(msg);
            Log.e(TAG, msg);
        }
        byte[] enc = Arrays.copyOfRange(recv, 1, recv.length);           //ciphertext data
        byte[] dec = aesEnDecrypt_CBC(key, new byte[16], enc, Cipher.DECRYPT_MODE);//plaintext data
        byte[] RndB = dec;//RndB
        msg = Utility.formatStr("first authentication-1st:\nSC=%02X\nRndB=%s", sc, ByteUtil.bytes2HexStr(RndB));
        showToast(msg);
        Log.e(TAG, msg);

        //2.first authentication-2nd
        cmd = 0x72;//cmd
        byte[] RndB_ = leftRotation(RndB);
        byte[] dataIn = ByteUtil.concatByteArrays(RndA, RndB_);
        byte[] temp = aesEnDecrypt_CBC(key, new byte[16], dataIn, Cipher.ENCRYPT_MODE);
        send = ByteUtil.concatByteArrays(new byte[]{cmd}, temp);
        recv = _transmitApdu(send);
        if (recv.length < 0x21) {//SC(1B)+E(Kx,TI||RndA'||PICCCap2||PCDCap2)
            msg = "\nfirst authentication-2nd recv data length error";
            showToast(msg);
            Log.e(TAG, msg);
            return;
        }
        sc = recv[0] & 0xff;
        if (sc != 0x90) {
            msg = "\nfirst authentication-2nd error, statusCode:" + hexStatusCode(sc);
            showToast(msg);
            Log.e(TAG, msg);
        }
        enc = Arrays.copyOfRange(recv, 1, recv.length);           //encrypted data
        dec = aesEnDecrypt_CBC(key, new byte[16], enc, Cipher.DECRYPT_MODE);//plaintext data
        byte[] TI = Arrays.copyOfRange(dec, 0, 4);//Transaction Identifier
        byte[] RdnA_ = Arrays.copyOfRange(dec, 4, 20);//RndA'
        byte[] PICCCap2 = Arrays.copyOfRange(recv, 20, 26);//PICCCap2
        byte[] pCDCap2 = Arrays.copyOfRange(recv, 26, 32);//PCDCap2
        msg = Utility.formatStr("first authentication-2nd:\nSC=%02X\nTI=%s\nRdnA_=%s\nPICCCap2=%s\nPCDCap2=%s",
                sc, ByteUtil.bytes2HexStr(TI), ByteUtil.bytes2HexStr(RdnA_), ByteUtil.bytes2HexStr(PICCCap2), ByteUtil.bytes2HexStr(pCDCap2));
        showToast(msg);
        Log.e(TAG, msg);
        byte[] tmpA = rightRotation(RdnA_);
        if (!Arrays.equals(tmpA, RndA)) {
            msg = "first authentication failed";
            showToast(msg);
            Log.e(TAG, msg);
            return;
        }

        //3.generate Kenc
        byte[] A = Arrays.copyOfRange(RndA, 11, 16);
        byte[] B = Arrays.copyOfRange(RndB, 11, 16);
        byte[] C = Arrays.copyOfRange(RndA, 4, 9);
        byte[] D = Arrays.copyOfRange(RndB, 4, 9);
        byte[] E = xor(C, D);
        // Session key base Kenc= A || B || E || 11h
        byte[] skbEnc = ByteUtil.concatByteArrays(A, B, E, new byte[]{0x11});
        Kenc = aesEnDecrypt_CBC(key, new byte[16], skbEnc, Cipher.ENCRYPT_MODE);

        //4.generate session key
        byte[] F = Arrays.copyOfRange(RndA, 7, 12);
        byte[] G = Arrays.copyOfRange(RndB, 7, 12);
        byte[] H = Arrays.copyOfRange(RndA, 0, 5);
        byte[] I = Arrays.copyOfRange(RndB, 0, 5);
        byte[] J = xor(H, I);
        //Session key base KMac = F || G || J || 22h
        byte[] skbMac = ByteUtil.concatByteArrays(F, G, J, new byte[]{0x22});
        Kmac = aesEnDecrypt_CBC(key, new byte[16], skbMac, Cipher.ENCRYPT_MODE);

        //5.store Transaction Identifier,R_Ctr,W_Ctr
        this.TI = TI;
        R_Ctr = 0;
        W_Ctr = 0;

        //6.show Kenc, Kmac value
        msg = Utility.formatStr("Kenc:%s\nKmac:%s", ByteUtil.bytes2HexStr(Kenc), ByteUtil.bytes2HexStr(Kmac));
        showToast(msg);
        Log.e(TAG, msg);

        //7.show success
        msg = "first authentication success";
        showToast(msg);
        Log.e(TAG, msg);
    }

    /**
     * Mifare plus following authentication
     *
     * @param block   block number
     * @param keyType key type, 0-KeyA, 1-KeyB
     * @param key     key value, default key is 12 bytes 0xff
     */
    private void followingAuthentication(int block, int keyType, byte[] key) {
        String msg = null;
        //1.following authentication-1st
        int sector = block / 4;
        byte cmd = 0x76;//cmd
        int sectorKey = 0;
        if (keyType == 0) {//KeyA
            sectorKey = sector * 2;
        } else if (keyType == 1) {//KeyB
            sectorKey = sector * 2 + 1;
        }
        byte[] send = {cmd, (byte) sectorKey, 0x40};
        byte[] recv = _transmitApdu(send);
        if (recv.length < 0x11) {//SC(1B)+E(Kx,RndB)(16B)
            msg = "\nfollowing authentication-1st recv data length error";
            showToast(msg);
            Log.e(TAG, msg);
            return;
        }
        //generate 16 bytes random data
        Random ran = new Random();
        byte[] RndA = new byte[16];
        ran.nextBytes(RndA);
        //parse SC and RndB
        int sc = recv[0] & 0xff;
        if (sc != 0x90) {
            msg = "\nfollowing authentication-1st  error, statusCode:" + hexStatusCode(sc);
            showToast(msg);
            Log.e(TAG, msg);
        }

        byte[] R_CTR = ByteUtil.short2BytesLE(R_Ctr);
        byte[] W_CTR = ByteUtil.short2BytesLE(W_Ctr);
        byte[] ti = TI;
        // iv-command: Transaction Identifier(4B) || R_Ctr(2B) || W_Ctr(2B) || R_Ctr(2B) || W_Ctr(2B) || R_Ctr(2B) || W_Ctr(2B)
        byte[] cmdIV = ByteUtil.concatByteArrays(ti, R_CTR, W_CTR, R_CTR, W_CTR, R_CTR, W_CTR);
        // iv-response: R_Ctr(2B) || W_Ctr(2B) || R_Ctr(2B) || W_Ctr(2B) || R_Ctr(2B) || W_Ctr(2B)|| Transaction Identifier(4B)
        byte[] rspIV = ByteUtil.concatByteArrays(R_CTR, W_CTR, R_CTR, W_CTR, R_CTR, W_CTR, ti);
        byte[] RndBEnc = Arrays.copyOfRange(recv, 1, recv.length);           //ciphertext data
        byte[] RndB = aesEnDecrypt_CBC(key, rspIV, RndBEnc, Cipher.DECRYPT_MODE);//RndB
        msg = Utility.formatStr("following authentication-1st:\nSC=%02X\nRndB=%s", sc, ByteUtil.bytes2HexStr(RndB));
        showToast(msg);
        Log.e(TAG, msg);

        //2.following authentication-2nd
        cmd = 0x72;//cmd
        byte[] RndB_ = leftRotation(RndB);
        byte[] dataIn = ByteUtil.concatByteArrays(RndA, RndB_);
        byte[] temp = aesEnDecrypt_CBC(key, cmdIV, dataIn, Cipher.ENCRYPT_MODE);
        send = ByteUtil.concatByteArrays(new byte[]{cmd}, temp);
        recv = _transmitApdu(send);
        if (recv.length < 0x11) {////SC(1B)+E(Kx,RndA')(16B)
            msg = "\nfollowing authentication-2nd recv data length error";
            showToast(msg);
            Log.e(TAG, msg);
            return;
        }
        sc = recv[0] & 0xff;
        if (sc != 0x90) {
            msg = "\nfollowing authentication-2nd error, statusCode:" + hexStatusCode(sc);
            showToast(msg);
            Log.e(TAG, msg);
        }
        byte[] RdnA_Enc = Arrays.copyOfRange(recv, 1, recv.length);               //encrypted data
        byte[] RdnA_ = aesEnDecrypt_CBC(key, rspIV, RdnA_Enc, Cipher.DECRYPT_MODE);//RndA'
        msg = Utility.formatStr("following authentication-2nd:\nSC=%02X\nRdnA_=%s", sc, ByteUtil.bytes2HexStr(RdnA_));
        showToast(msg);
        Log.e(TAG, msg);
        byte[] tmpA = rightRotation(RdnA_);
        if (!Arrays.equals(tmpA, RndA)) {
            msg = "following authentication failed";
            showToast(msg);
            Log.e(TAG, msg);
            return;
        }

        //3.generate Kenc
        byte[] A = Arrays.copyOfRange(RndA, 11, 16);
        byte[] B = Arrays.copyOfRange(RndB, 11, 16);
        byte[] C = Arrays.copyOfRange(RndA, 4, 9);
        byte[] D = Arrays.copyOfRange(RndB, 4, 9);
        byte[] E = xor(C, D);
        // Session key base = A || B || E || 11h
        byte[] skBase = ByteUtil.concatByteArrays(A, B, E, new byte[]{0x11});
        Kenc = aesEnDecrypt_CBC(key, new byte[16], skBase, Cipher.ENCRYPT_MODE);

        //4.generate session key
        byte[] F = Arrays.copyOfRange(RndA, 7, 12);
        byte[] G = Arrays.copyOfRange(RndB, 7, 12);
        byte[] H = Arrays.copyOfRange(RndA, 0, 5);
        byte[] I = Arrays.copyOfRange(RndB, 0, 5);
        byte[] J = xor(H, I);
        //Session key base KMAC = F || G || J || 22h
        byte[] skMac = ByteUtil.concatByteArrays(F, G, J, new byte[]{0x22});
        Kmac = aesEnDecrypt_CBC(key, new byte[16], skMac, Cipher.ENCRYPT_MODE);

        //5.show Kenc, Kmac value
        msg = Utility.formatStr("Kenc:%s\nKmac:%s", ByteUtil.bytes2HexStr(Kenc), ByteUtil.bytes2HexStr(Kmac));
        showToast(msg);
        Log.e(TAG, msg);

        //6.show success
        msg = "following authentication success";
        showToast(msg);
        Log.e(TAG, msg);
    }

    /**
     * Mifare plus read block
     *
     * @param block block number
     */
    private byte[] mifarePlusReadBlock(int block) {
        String msg = null;
        //1.send read command
        byte[] cmd = {0x33}; //Read encrypted, MAC on response, MAC on command
        byte[] BNr = ByteUtil.short2BytesLE((short) block);
        byte[] Ext = {0x01};
        byte[] R_CTR_CMD = ByteUtil.short2BytesLE(R_Ctr);
        byte[] ti = TI;
        //Mac-on command: Command Code(1B) || R_Ctr(2B) || Transaction Identifier(4B) || Block Number(2B) || Ext(1B) || Padding
        byte[] macPayloadCmd = ByteUtil.concatByteArrays(cmd, R_CTR_CMD, ti, BNr, Ext);
        Log.e(TAG, "macPayloadCmd:" + ByteUtil.bytes2HexStr(macPayloadCmd));
        byte[] macCmd = calMac(Kmac, macPayloadCmd);
        byte[] send = ByteUtil.concatByteArrays(cmd, BNr, Ext, macCmd);
        byte[] recv = _transmitApdu(send);

        R_Ctr++;

        if (recv.length < 0x19) {//SC(1B)+ dataEnc(16B) + Mac(8B)
            msg = "\nreadBlock() recv data length error";
            showToast(msg);
            Log.e(TAG, msg);
            return null;
        }
        //2.parse recv data
        int sc = recv[0] & 0xff;
        if (sc != 0x90) {
            msg = "\nreadBlock() error, statusCode:" + hexStatusCode(sc);
            showToast(msg);
            Log.e(TAG, msg);
            return null;
        }
        byte[] blockDataEnc = Arrays.copyOfRange(recv, 1, 17);
        byte[] macRsp = Arrays.copyOfRange(recv, 17, 25);

        //3.verify mac
        byte[] SC = {recv[0]};
        byte[] R_CTR_RSP = ByteUtil.short2BytesLE(R_Ctr);
        ///Mac-on response: Status Code || R_Ctr || Transaction Identifier || BNr of first command || Ext of first command || Data (encrypted or plain) || Padding
        byte[] macPayloadRsp = ByteUtil.concatByteArrays(SC, R_CTR_RSP, ti, BNr, Ext, blockDataEnc);
        byte[] macVerify = calMac(Kmac, macPayloadRsp);
        if (!Arrays.equals(macVerify, macRsp)) {
            msg = "\nreadBlock() verify mac failed";
            showToast(msg);
            Log.e(TAG, msg);
            return null;
        }
        //4.decrypt block data
        byte[] W_CTR = ByteUtil.short2BytesLE(W_Ctr);
        // iv: R_Ctr(2B) || W_Ctr(2B) || R_Ctr(2B) || W_Ctr(2B) || R_Ctr(2B) || W_Ctr(2B)|| Transaction Identifier(4B)
        byte[] iv = ByteUtil.concatByteArrays(R_CTR_RSP, W_CTR, R_CTR_RSP, W_CTR, R_CTR_RSP, W_CTR, ti);
        //todo If the data is in ciphertext, decrypt it according to your needs.
//        byte[] blockData = aesEnDecrypt_CBC(Kenc, iv, blockDataEnc, Cipher.DECRYPT_MODE);
        msg = "\nreadBlock() block:" + block + ",data:" + ByteUtil.bytes2HexStr(blockDataEnc);
        showToast(msg);
        Log.e(TAG, msg);
        return blockDataEnc;
    }

    /**
     * Mifare plus wirte block
     *
     * @param block  block number
     * @param dataIn block data(plaintext)
     */
    private void mifarePlusWriteBlock(int block, byte[] dataIn) {
        String msg = null;
        //1.send write command
        byte[] cmd = {(byte) 0xA1};// Write encrypted, MAC on response, MAC on command
        byte[] BNr = ByteUtil.short2BytesLE((short) block);
        byte[] ti = TI;
        byte[] R_CTR = ByteUtil.short2BytesLE(R_Ctr);
        byte[] W_CTR = ByteUtil.short2BytesLE(W_Ctr);
        //  iv: Transaction Identifier(4B) || R_Ctr(2B) || W_Ctr(2B) || R_Ctr(2B) || W_Ctr(2B) || R_Ctr(2B) || W_Ctr(2B)
        byte[] iv = ByteUtil.concatByteArrays(ti, R_CTR, W_CTR, R_CTR, W_CTR, R_CTR, W_CTR);
        byte[] dataInEnc = aesEnDecrypt_CBC(Kenc, iv, dataIn, Cipher.ENCRYPT_MODE);
        //Mac-on command:  Command Code || W_Ctr || Transaction Identifier|| Block Number || Data || Padding if required
        byte[] macPayloadCmd = ByteUtil.concatByteArrays(cmd, W_CTR, ti, BNr, dataInEnc);
        byte[] macCmd = calMac(Kmac, macPayloadCmd);
        byte[] send = ByteUtil.concatByteArrays(cmd, BNr, dataInEnc, macCmd);
        byte[] recv = _transmitApdu(send);

        W_Ctr++;

        if (recv.length < 0x09) {//SC(1B)+MAC(8B)
            msg = "\nwriteBlock() failed";
            showToast(msg);
            Log.e(TAG, msg);
            return;
        }
        //2.parse receive data
        int sc = recv[0] & 0xff;
        if (sc != 0x90) {
            msg = "\nwriteBlock() error, statusCode:" + hexStatusCode(sc);
            showToast(msg);
            Log.e(TAG, msg);
            return;
        }

        //3.verify mac
        byte[] macRsp = Arrays.copyOfRange(recv, 1, 9);
        ///Mac-on response: Status Code || W_Ctr || Transaction Identifier || Padding
        byte[] SC = {recv[0]};
        byte[] W_CTR_RSP = ByteUtil.short2BytesLE(W_Ctr);
        byte[] macPayloadRsp = ByteUtil.concatByteArrays(SC, W_CTR_RSP, ti);
        byte[] macVerify = calMac(Kmac, macPayloadRsp);
        if (!Arrays.equals(macVerify, macRsp)) {
            msg = "\nwriteBlock() verify mac failed";
            showToast(msg);
            Log.e(TAG, msg);
            return;
        }
        msg = "\nwriteBlock block:" + block + " success";
        showToast(msg);
        Log.e(TAG, msg);
    }

    private String hexStatusCode(int sc) {
        return Utility.formatStr("%02X", sc);
    }

    private byte[] calMac(byte[] key, byte[] dataIn) {
        byte[] iv = new byte[16];
        byte[] mac = CMacUtil.calcMac(key, iv, dataIn);
        byte[] result = new byte[8];
        for (int i = 1, j = 0; i < mac.length; i += 2) {
            result[j++] = mac[i];
        }
        return result;
    }

    /** Mifare desfire response APDU not contains SWA SWB */
    private byte[] _transmitApdu(byte[] send) {
        byte[] result = new byte[0];
        try {
            byte[] buffer = new byte[260];
            String msg = "\ntransmitApdu() send:" + ByteUtil.bytes2HexStr(send);
            LogUtil.e(TAG, msg);
            int len = MyApplication.app.readCardOptV2.transmitApdu(CardType.NFC.getValue(), send, buffer);
            if (len < 0) {
                msg = "\ntransmitApdu() failed,code:" + len;
            } else {
                result = Arrays.copyOf(buffer, len);
                msg = "\ntransmitApdu() recv:" + ByteUtil.bytes2HexStr(result);
            }
            LogUtil.e(TAG, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private byte[] leftRotation(byte[] src) {
        byte[] result = new byte[src.length];
        System.arraycopy(src, 1, result, 0, src.length - 1);
        result[result.length - 1] = src[0];
        return result;
    }

    private byte[] rightRotation(byte[] src) {
        byte[] result = new byte[src.length];
        System.arraycopy(src, 0, result, 1, src.length - 1);
        result[0] = src[src.length - 1];
        return result;
    }

    private byte[] xor(byte[] x, byte[] y) {
        byte[] result = new byte[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = (byte) (x[i] ^ y[i]);
        }
        return result;
    }

    /**
     * AES CBC加加解密算法
     *
     * @param key    密钥
     * @param dataIn 待加密数据
     * @return 加密返回数据
     */
    public static byte[] aesEnDecrypt_CBC(byte[] key, byte[] iv, byte[] dataIn, int mode) {
        if (key.length < 16) {
            throw new IllegalArgumentException("算法支持密钥是16、24或32字节的密钥加密算法");
        }
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            if (iv != null) {
                IvParameterSpec spec = new IvParameterSpec(iv);
                cipher.init(mode, secretKeySpec, spec);
            } else {
                cipher.init(mode, secretKeySpec);
            }
            return cipher.doFinal(dataIn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        cancelCheckCard();
        super.onDestroy();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.app.readCardOptV2.cardOff(CardType.NFC.getValue());
            MyApplication.app.readCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
