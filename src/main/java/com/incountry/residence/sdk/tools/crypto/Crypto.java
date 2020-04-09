package com.incountry.residence.sdk.tools.crypto;

import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;

import java.util.Map;

public interface Crypto {
    Map.Entry<String, Integer> encrypt(String plainText) throws StorageCryptoException;

    String decrypt(String cipherText, Integer decryptKeyVersion) throws StorageCryptoException;

    String createKeyHash(String key);

    int getCurrentSecretVersion();
}
