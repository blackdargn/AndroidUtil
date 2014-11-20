package com.android.util.system;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class SumUtil {

    
    /**
     * @param filePath
     * @param md5OrSha1 true:MD5; false:SHA-1
     * @return
     */
    public static String sum(String filePath, boolean md5OrSha1) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance(md5OrSha1 ? "MD5" : "SHA-1");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            byte[] md5Bytes = digest.digest();
            return convertHashToString(md5Bytes);
        } catch (Exception e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private static String convertHashToString(byte[] md5Bytes) {
        StringBuilder returnVal = new StringBuilder();
        for (int i = 0; i < md5Bytes.length; i++) {
            returnVal .append( Integer.toString((md5Bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return returnVal.toString();
    }
}
