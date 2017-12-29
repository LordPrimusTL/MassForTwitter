package com.mavericks.massfortwitter.Utility;

import android.util.Base64;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Formatter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class HmacShaEncrypt {
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();

        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        Log.e("TO String", "toHexString: " + formatter.toString());
        return formatter.toString();
    }

    public static String calculateRFC2104HMAC(String data, String key)
            throws SignatureException, NoSuchAlgorithmException, InvalidKeyException
    {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        byte[] result = mac.doFinal(data.getBytes());
        Log.e("HASH", "calculateRFC2104HMAC: "+ Arrays.toString(result));
        //return toHexString(mac.doFinal(data.getBytes()))
        return Base64.encodeToString(result,Base64.NO_WRAP);
    }
}