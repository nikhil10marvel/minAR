package io.minAR.util;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * A utility class, mainly helps in encryption of the archives.
 * Contains helper methods for <code>byte[]</code> encryption
 * @author nikhil
 * @see Cipher
 * @see SecretKey
 * @since 0.0.1
 */
public class Crypt {
    private static KeyGenerator keyGenerator;
    private static Cipher cipher;

    static {
        try {
            keyGenerator = KeyGenerator.getInstance("DES");
            cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    /**
     * A Helper class, to temporarily club the secret key and the encrypted bytes
     * @since 0.0.1
     */
    public static class ENC_OBJECT {
        final byte[] raw;
        final SecretKey secretKey;

        private ENC_OBJECT(byte[] raw, SecretKey secretKey){
            this.raw = raw;
            this.secretKey = secretKey;
        }

        /**
         * @return the bytes of data contained in this {@link ENC_OBJECT}
         */
        public byte[] getRaw() {
            return raw;
        }

        /**
         * @return the secret key of this {@link ENC_OBJECT}
         */
        public SecretKey secretKey() {
            return secretKey;
        }

        static ENC_OBJECT getInstance(byte[] data, SecretKey  key){
            return new ENC_OBJECT(data, key);
        }
    }

    /**
     * Encrypts the given data through a timely generated key.
     * @param data the <code>byte[]</code> to be encrypted
     * @return an {@link ENC_OBJECT} containing the encrypted data as {@code raw} and the secret key
     */
    public static ENC_OBJECT encrypt(byte[] data){
        byte[] ret = null; SecretKey secretKey = null;
        try {
            secretKey = keyGenerator.generateKey();
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            ret = cipher.doFinal(data);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return ENC_OBJECT.getInstance(ret, secretKey);
    }

    /**
     * Decrypt the given byte array using the secret key
     * @param encrypted the encrypted bytes
     * @param secretKey the secret key needed for decryption
     * @return the decrypted bytes
     */
    public static byte[] decrypt(byte[] encrypted, SecretKey secretKey){
        byte[] ret = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            ret = cipher.doFinal(encrypted);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Decrypts the data in the {@link ENC_OBJECT}
     * @param encrypted the {@code ENC_OBJECT} containing the data and kedy
     * @return the decrypted bytes
     */
    public static byte[] decrypt(ENC_OBJECT encrypted){
        byte[] ret = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, encrypted.secretKey);
            ret = cipher.doFinal(encrypted.raw);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Retrieves the secret key stored in the specified file
     * @param file the file containing the secret key
     * @return the secret key
     */
    public static SecretKey getKeyFromFile(String file){
        SecretKey secretKey = null;
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(file + ".key"));
            secretKey = new SecretKeySpec(encoded, 0, encoded.length, "DES");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return secretKey;
    }

    /**
     * Encodes the secret key into a string
     * @param secretKey the secret key to be converted to a string
     * @return the secret key as a string
     */
    public static String keyAsString(SecretKey secretKey){
        byte[] encoded = secretKey.getEncoded();
        return Base64.getEncoder().encodeToString(encoded);
    }

    /**
     * The inverse of {@code keyAsString(...)}, re-creates a secret key from the specified string
     * @param key the string form of the key
     * @return the secret key
     */
    public static SecretKey stringAsKey(String key){
        byte[] encoded = Base64.getDecoder().decode(key);
        return new SecretKeySpec(encoded, 0, encoded.length, "DES");
    }

}
