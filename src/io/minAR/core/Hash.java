package io.minAR.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Hash {

    private static MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static byte[] hash(String str){
        byte[] rdata = str.getBytes(StandardCharsets.UTF_8);
        return digest.digest(rdata);    // Always 64 bytes.
    }

    public static boolean checkHash(byte[] hash, byte[] data){
        byte[] ghash = hash(data);
        return Arrays.equals(hash, ghash);
    }

    public static byte[] hash(byte[] data){
        return digest.digest(data);     // Always 64 bytes.
    }

    public static String toHex(byte[] bytes){
        StringBuilder stringBuilder = new StringBuilder();
        for (byte byt: bytes) {
            stringBuilder.append(Integer.toHexString(byt));
        }
        return stringBuilder.toString();
    }

}
