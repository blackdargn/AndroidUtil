package com.android.util.system;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import android.util.Base64;

/**
 * DES安全编码组件
 * 
 * <pre>
 * 支持 DES、DESede(TripleDES,就是3DES)、AES、Blowfish、RC2、RC4(ARCFOUR) 
 * DES                  key size must be equal to 56 
 * DESede(TripleDES)    key size must be equal to 112 or 168 
 * AES                  key size must be equal to 128, 192 or 256,but 192 and 256 bits may not be available 
 * Blowfish             key size must be multiple of 8, and can only range from 32 to 448 (inclusive) 
 * RC2                  key size must be between 40 and 1024 bits 
 * RC4(ARCFOUR)         key size must be between 40 and 1024 bits 
 * 具体内容 需要关注 JDK Document http://.../docs/technotes/guides/security/SunProviders.html
 * </pre>
 */
public class DESCoder {
    public static final String cipherCode = "lolaage";
    // SfvNWAFz79M=
    private static final String sercretKey = "lolaage0";
    private static final String Android_ALGORITHM = "DES/ECB/PKCS5Padding";
    private static final String ALGORITHM = "DES";

    /**
     * 转换密钥<br>
     */
    protected static Key toKey(byte[] key) throws Exception {

        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
        SecretKey secretKey = keyFactory.generateSecret(dks);

        // 当使用其他对称加密算法时，如AES、Blowfish等算法时，用下述代码替换上述三行代码
        // SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);

        return secretKey;
    }

    /**
     * 解密
     */
    protected static byte[] decrypt(byte[] data, String key) throws Exception {
        byte[] keyBytes = key.getBytes(); //Base64.decode(key, Base64.DEFAULT);
        Key k = toKey(keyBytes);

        Cipher cipher = Cipher.getInstance(Android_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, k);

        return cipher.doFinal(data);
    }

    /**
     * 加密
     */
    protected static byte[] encrypt(byte[] data, String key) throws Exception {
        byte[] keyBytes = key.getBytes(); //Base64.decode(key, Base64.DEFAULT);
        Key k = toKey(keyBytes);

        Cipher cipher = Cipher.getInstance(Android_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, k);

        return cipher.doFinal(data);
    }

    public static String encode(String data) throws Exception {
        byte[] inputData = data.getBytes();
        inputData = DESCoder.encrypt(inputData, sercretKey);
        return Base64.encodeToString(inputData, Base64.DEFAULT);
    }

    public static String decode(String data) throws Exception {
        byte[] inputData = Base64.decode(data, Base64.DEFAULT);
        byte[] outputData = DESCoder.decrypt(inputData, sercretKey);
        String outputStr = new String(outputData);
        return outputStr;
    }

    public static String buildUrl(String path, String params) {
        try {
            return path + DESCoder.encode(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        String params = DESCoder.encode(18708 + "_" + 0 + "_" + 0);
        String url = "http://files.2bulu.com/f/d1?downParams=" + params;
        System.out.print(url);
        System.out.println(decode(params));
    }
}