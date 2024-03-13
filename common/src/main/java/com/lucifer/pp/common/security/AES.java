package com.lucifer.pp.common.security;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class AES {

    /**AES密钥标识*/
    public static final String SIGN_AES = "AES";

    /**字符串编码*/
    public static final String UTF_8 = "UTF-8";

    /**密码器AES模式*/
    public static final String CIPHER_AES= "AES/ECB/PKCS5Padding";

    /**密钥长度128*/
    public static final int KEY_SIZE_128_LENGTH = 128;

    /**密钥长度192*/
    public static final int KEY_SIZE_192_LENGTH = 192;

    /**密钥长度256*/
    public static final int KEY_SIZE_256_LENGTH = 256;

    private static final String keyString = "MMsI3lBHnSJpMkM4XVP0zg==";

    /**
     * 生成密钥，请使用合适的长度128 192 256
     * @param keySize
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String createKeyString(int keySize) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(keySize);
        SecretKey secretKey = keyGenerator.generateKey();
        byte[] keyBytes = secretKey.getEncoded();
        return new String(Base64.encodeBase64(keyBytes), UTF_8);
    }

    /**
     * 生成Key对象
     * @param keyString
     * @return
     * @throws UnsupportedEncodingException
     */
    public static Key generateKey(String keyString) throws UnsupportedEncodingException {
        byte[] decodedKey = Base64.decodeBase64(keyString.getBytes(UTF_8));
        Key key = new SecretKeySpec(decodedKey, SIGN_AES);
        return key;
    }

    /**
     * AES加密
     * @param source
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static String encryptByAES(String source) throws NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(CIPHER_AES);
        cipher.init(Cipher.ENCRYPT_MODE, generateKey(keyString));
        byte[] encrypted = cipher.doFinal(source.getBytes(UTF_8));
        return Base64.encodeBase64String(encrypted);
    }

    /**
     * AES解密
     * @param encrypted
     * @return
     * @throws UnsupportedEncodingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     */
    public static String decryptByAES(String encrypted) throws UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(CIPHER_AES);
        cipher.init(Cipher.DECRYPT_MODE, generateKey(keyString));
        byte[] decrypted = cipher.doFinal(Base64.decodeBase64(encrypted));
        return new String(decrypted, UTF_8);
    }
}
