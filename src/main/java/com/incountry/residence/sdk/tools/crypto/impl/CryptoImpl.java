package com.incountry.residence.sdk.tools.crypto.impl;

import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKeysData;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

public class CryptoImpl implements Crypto {
    private static final Logger LOG = LogManager.getLogger(CryptoImpl.class);

    private static final String MSG_ERR_NO_SECRET = "No secret provided. Cannot decrypt record: ";
    private static final String MSG_ERR_VERSION = "SecretKeyGenerator returns data in which there is no current version of the key";
    private static final String MSG_ERR_DECRYPTION = "Decryption error: Illegal decryption version";
    private static final String MSG_ERR_GEN_SECRET = "Secret generation exception";
    private static final String MSG_ERR_NO_ALGORITHM = "Unable to generate secret - cannot find PBKDF2WithHmacSHA512 algorithm. Please, check your JVM configuration";
    private static final String MSG_ERR_ENCRYPTION = "Data encryption error";
    private static final String MSG_ERR_ALG_EXCEPTION = "AES/GCM/NoPadding algorithm exception";

    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    private static final Charset CHARSET = Charset.defaultCharset();
    private static final int AUTH_TAG_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int KEY_LENGTH = 32;
    private static final int SALT_LENGTH = 64;
    private static final int PBKDF2_ITERATIONS_COUNT = 10000;
    private static final String VERSION = "2";

    public static final String PT_ENC_VERSION = "pt";

    private SecretKeysData secretKeysData;
    private String envId;
    private boolean isUsingPTEncryption = false;

    public CryptoImpl(SecretKeysData secret) {
        this.secretKeysData = secret;
    }

    public CryptoImpl(String envId) {
        this.envId = envId;
        this.isUsingPTEncryption = true;
    }

    public CryptoImpl(SecretKeysData secret, String envId) {
        this.secretKeysData = secret;
        this.envId = envId;
    }

    public Map.Entry<String, Integer> encrypt(String plainText) throws StorageCryptoException {
        if (isUsingPTEncryption) {
            byte[] ptEncoded = Base64.getEncoder().encode(plainText.getBytes(CHARSET));
            return new AbstractMap.SimpleEntry<>(PT_ENC_VERSION + ":" + new String(ptEncoded, CHARSET), null);
        }

        byte[] clean = plainText.getBytes(CHARSET);
        byte[] salt = generateRandomBytes(SALT_LENGTH);
        SecretKey secretKeyObj = getSecret(secretKeysData.getCurrentVersion());
        byte[] key = getKey(salt, secretKeyObj);
        byte[] iv = generateRandomBytes(IV_LENGTH);

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AUTH_TAG_LENGTH * 8, iv);

        byte[] encrypted = null;
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
            encrypted = cipher.doFinal(clean);
        } catch (GeneralSecurityException e) {
            throwStorageCryptoException(MSG_ERR_ALG_EXCEPTION, e);
        }

        byte[] resultByteArray = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(salt);
            outputStream.write(iv);
            outputStream.write(encrypted);
            resultByteArray = outputStream.toByteArray();
        } catch (IOException e) {
            throwStorageCryptoException(MSG_ERR_ENCRYPTION, e);
        }

        byte[] encoded = Base64.getEncoder().encode(resultByteArray);
        return new AbstractMap.SimpleEntry<>(VERSION + ":" + new String(encoded, CHARSET), secretKeyObj.getVersion());
    }

    private byte[] getKey(byte[] salt, SecretKey secretKeyObj) throws StorageCryptoException {
        if (secretKeyObj.getIsKey() != null && secretKeyObj.getIsKey()) {
            return secretKeyObj.getSecret().getBytes(StandardCharsets.UTF_8);
        }
        return generateStrongPasswordHash(secretKeyObj.getSecret(), salt, PBKDF2_ITERATIONS_COUNT, KEY_LENGTH);
    }

    private static String createHash(String stringToHash) {
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(stringToHash);
    }

    private String decryptUnpacked(byte[] parts, Integer decryptKeyVersion) throws StorageCryptoException {
        byte[] salt = Arrays.copyOfRange(parts, 0, 64);
        byte[] iv = Arrays.copyOfRange(parts, 64, 76);
        byte[] encrypted = Arrays.copyOfRange(parts, 76, parts.length);

        byte[] decryptedText = null;
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            byte[] key = getKey(salt, getSecret(decryptKeyVersion));

            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16 * 8, iv);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
            decryptedText = cipher.doFinal(encrypted);
        } catch (GeneralSecurityException e) {
            throwStorageCryptoException(MSG_ERR_ENCRYPTION, e);
        }
        return new String(decryptedText, CHARSET);
    }

    private SecretKey getSecret(Integer version) {
        SecretKey secret = null;
        for (SecretKey item : secretKeysData.getSecrets()) {
            if (item.getVersion() == version) {
                secret = item;
                break;
            }
        }
        if (secret == null) {
            LOG.error(MSG_ERR_VERSION);
            throw new IllegalArgumentException(MSG_ERR_VERSION);
        }
        return secret;
    }

    public int getCurrentSecretVersion() {
        return secretKeysData.getCurrentVersion();
    }

    public String createKeyHash(String key) {
        if (key == null) {
            return null;
        }
        String stringToHash = key + ":" + envId;
        return createHash(stringToHash);
    }

    public String decrypt(String cipherText, Integer decryptKeyVersion) throws StorageCryptoException {
        if (cipherText == null) {
            return null;
        }
        String[] parts = cipherText.split(":", -1);
        if (parts[0].equals(PT_ENC_VERSION)) {
            return decryptVPT(parts[1]);
        }
        if (isUsingPTEncryption) {
            String message = MSG_ERR_NO_SECRET + cipherText;
            throwStorageCryptoException(message, null);
        }
        switch (parts[0]) {
            case "1":
                return decryptV1(parts[1], decryptKeyVersion);
            case "2":
                return decryptV2(parts[1], decryptKeyVersion);
            default:
                throwStorageCryptoException(MSG_ERR_DECRYPTION, null);
        }
        return null;
    }

    private static String throwStorageCryptoException(String message, Exception ex) throws StorageCryptoException {
        if (ex == null) {
            LOG.error(message);
            throw new StorageCryptoException(message);
        } else {
            LOG.error(message, ex);
            throw new StorageCryptoException(message, ex);
        }

    }

    private String decryptV2(String cipherText, Integer decryptKeyVersion) throws StorageCryptoException {
        byte[] parts = Base64.getDecoder().decode(cipherText);
        return this.decryptUnpacked(parts, decryptKeyVersion);
    }

    private String decryptV1(String cipherText, Integer decryptKeyVersion) throws StorageCryptoException {
        byte[] parts = DatatypeConverter.parseHexBinary(cipherText);
        return this.decryptUnpacked(parts, decryptKeyVersion);
    }

    private String decryptVPT(String cipherText) {
        byte[] ptBytes = Base64.getDecoder().decode(cipherText);
        return new String(ptBytes, CHARSET);
    }

    private static byte[] generateStrongPasswordHash(String password, byte[] salt, int iterations, int length) throws StorageCryptoException {
        char[] chars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, length * 8);
        byte[] strongPasswordHash = null;
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            strongPasswordHash = skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throwStorageCryptoException(MSG_ERR_NO_ALGORITHM, e);
        } catch (InvalidKeySpecException e) {
            throwStorageCryptoException(MSG_ERR_GEN_SECRET, e);
        }
        return strongPasswordHash;
    }

    private static byte[] generateRandomBytes(int length) {
        SecureRandom randomSecureRandom = new SecureRandom();
        byte[] randomBytes = new byte[length];
        randomSecureRandom.nextBytes(randomBytes);
        return randomBytes;
    }
}