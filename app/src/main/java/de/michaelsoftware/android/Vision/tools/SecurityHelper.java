package de.michaelsoftware.android.Vision.tools;

import android.util.Base64;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Michael on 06.12.2015.
 * Partly copied from stackoverflow (Thanks)
 * Doesn't recommended to use this class in secure applications. It isn't ready yet
 */
public class SecurityHelper {
    private static final String KEY_STR = "1234567891234567";
    private static final String IV_STR = "1234567891234567";

    public static String generateKey() {
        return generateKey(16);
    }

    public static String generateKey(int outputKeyLength) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // for example
            SecretKey secretKey = keyGen.generateKey();

            String encodedKey = Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
            encodedKey = encodedKey.substring(0,outputKeyLength);

            return encodedKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return Math.round(Math.random()* Math.pow(10, outputKeyLength)) + "";
        }
    }

    @SuppressWarnings("unused") /* Will maybe be used in later development */
    public static String base64Encode(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static String encrypt(String data) {
        return encrypt(data, KEY_STR, IV_STR);
    }

    public static String encrypt(String data, String key) {
        return encrypt(data, key, IV_STR);
    }

    public static String encrypt(String data, String key, String iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();

            // We need to pad with zeros to a multiple of the cipher block size,
            // so first figure out what the size of the plaintext needs to be.
            byte[] dataBytes = data.getBytes();
            int plaintextLength = dataBytes.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }

            // In java, primitive arrays of integer types have all elements
            // initialized to zero, so no need to explicitly zero any part of
            // the array.
            byte[] plaintext = new byte[plaintextLength];

            // Copy our actual data into the beginning of the array.  The
            // rest of the array is implicitly zero-filled, as desired.
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);

            return Base64.encodeToString(encrypted, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String data) {
        return decrypt(data, KEY_STR, IV_STR);
    }

    @SuppressWarnings("unused") /* Maybe used later or by an other Developer (doesn't recommended to use this class in secure applications. It isn't ready yet) */
    public static String decrypt(String data, String key) {
        return decrypt(data, key, IV_STR);
    }

    public static String decrypt(String data, String key, String iv) {
        try {

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();

            byte[] crypted2 = Base64.decode(data, Base64.DEFAULT);
            // We need to pad with zeros to a multiple of the cipher block size,
            // so first figure out what the size of the plaintext needs to be.
            int plaintextLength = crypted2.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }

            // In java, primitive arrays of integer types have all elements
            // initialized to zero, so no need to explicitly zero any part of
            // the array.
            byte[] plaintext = new byte[plaintextLength];

            // Copy our actual data into the beginning of the array.  The
            // rest of the array is implicitly zero-filled, as desired.
            System.arraycopy(crypted2, 0, plaintext, 0, crypted2.length);

            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);

            return new String(encrypted).trim();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String base64Decode(String s) {
        return new String(Base64.decode(s, Base64.DEFAULT));
    }
}
