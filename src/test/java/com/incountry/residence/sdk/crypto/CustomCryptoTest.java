package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.Storage;
import com.incountry.residence.sdk.StorageConfig;
import com.incountry.residence.sdk.StorageImpl;
import com.incountry.residence.sdk.crypto.testimpl.CryptoStub;
import com.incountry.residence.sdk.crypto.testimpl.DefaultCryptoWithCustomVersion;
import com.incountry.residence.sdk.crypto.testimpl.InvalidCrypto;
import com.incountry.residence.sdk.crypto.testimpl.PseudoCustomCrypto;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.crypto.DefaultCrypto;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomCryptoTest {

    private static final String CUSTOM_PASSWORD_1 = "123456789_123456789_123456789_12";
    private static final String CUSTOM_PASSWORD_2 = "123456789!123456789_123456789%Ab";
    private static final String ENV_ID = "envId";
    private static final String FAKE_ENDPOINT = "http://localhost";
    private static final String API_KEY = "apiKey";
    private static final String BODY_FOR_ENCRYPTION = "SomeSecretBody!234567=!@#$%^&**()_+|";

    @Test
    public void positiveStorageInitTest() throws StorageClientException, StorageServerException {
        List<Crypto> cryptoList = Arrays.asList(new CryptoStub(true), new PseudoCustomCrypto(false));
        SecretKey key1 = new SecretKey(CUSTOM_PASSWORD_1, 1, false, true);
        SecretKey key2 = new SecretKey(CUSTOM_PASSWORD_2, 2, false, true);
        SecretsData data = new SecretsData(Arrays.asList(key1, key2), 2);
        SecretKeyAccessor accessor = () -> data;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENV_ID)
                .setApiKey(API_KEY)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(accessor)
                .setCustomEncryptionConfigsList(cryptoList);
        Storage storage = StorageImpl.getInstance(config);
        assertNotNull(storage);
        storage = StorageImpl.getInstance(config.copy().setSecretKeyAccessor(null).setCustomEncryptionConfigsList(null));
        assertNotNull(storage);
        storage = StorageImpl.getInstance(config.copy().setSecretKeyAccessor(null).setCustomEncryptionConfigsList(new ArrayList<>()));
        assertNotNull(storage);
    }

    @Test
    public void customEncryptionTestPositive() throws StorageClientException {
        List<Crypto> cryptoList = Arrays.asList(new CryptoStub(true));
        int keyVersion = 1;
        SecretKey secretKey = new SecretKey(CUSTOM_PASSWORD_1, keyVersion, false, true);
        SecretsData secretsData = new SecretsData(Arrays.asList(secretKey), keyVersion);
        SecretKeyAccessor secretKeyAccessor = () -> secretsData;
        CryptoManager cryptoManager = new CryptoManager(secretKeyAccessor, ENV_ID, cryptoList, false);
        assertNotNull(cryptoManager);
    }

    @Test
    public void customEncryptionTestNegative() throws StorageClientException {
        List<Crypto> cryptoList = Arrays.asList(new InvalidCrypto(true));
        int keyVersion = 1;
        SecretKey secretKey = new SecretKey(CUSTOM_PASSWORD_1, keyVersion, false, true);
        SecretsData secretsData = new SecretsData(Arrays.asList(secretKey), keyVersion);
        SecretKeyAccessor secretKeyAccessor = () -> secretsData;
        assertThrows(StorageClientException.class, () -> new CryptoManager(secretKeyAccessor, ENV_ID, cryptoList, false));
    }

    @Test
    public void negativeNullCryptoVersionTest() throws StorageClientException {
        List<Crypto> cryptoList1 = Arrays.asList(new DefaultCryptoWithCustomVersion(null));
        SecretKey key1 = new SecretKey(CUSTOM_PASSWORD_1, 1, false, true);
        SecretsData data = new SecretsData(Arrays.asList(key1), 1);
        SecretKeyAccessor accessor = () -> data;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENV_ID)
                .setApiKey(API_KEY)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(accessor)
                .setCustomEncryptionConfigsList(cryptoList1);
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
        List<Crypto> cryptoList2 = Arrays.asList(new DefaultCryptoWithCustomVersion(""));
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config.copy().setCustomEncryptionConfigsList(cryptoList2)));
    }

    @Test
    public void negativeNullCryptoTest() throws StorageClientException {
        List<Crypto> cryptoList = Arrays.asList(null, new PseudoCustomCrypto(true));
        SecretKey key1 = new SecretKey(CUSTOM_PASSWORD_1, 1, false, true);
        SecretsData data = new SecretsData(Arrays.asList(key1), 1);
        SecretKeyAccessor accessor = () -> data;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENV_ID)
                .setApiKey(API_KEY)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(accessor)
                .setCustomEncryptionConfigsList(cryptoList);
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
    }

    @Test
    public void negativeTestWithoutEncWithCustomCrypto() {
        List<Crypto> cryptoList = Arrays.asList(new PseudoCustomCrypto(true));
        StorageConfig config = new StorageConfig()
                .setEnvId(ENV_ID)
                .setApiKey(API_KEY)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(null)
                .setCustomEncryptionConfigsList(cryptoList);
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
    }

    @Test
    public void negativeTestCustomCryptoSameVersions() throws StorageClientException {
        String cryptoVersion = "someVersion";
        Crypto crypto1 = new DefaultCryptoWithCustomVersion(cryptoVersion, true);
        Crypto crypto2 = new DefaultCryptoWithCustomVersion(cryptoVersion, false);
        List<Crypto> cryptoList = Arrays.asList(crypto1, crypto2);
        SecretKey key1 = new SecretKey(CUSTOM_PASSWORD_1, 1, false, true);
        SecretsData data = new SecretsData(Arrays.asList(key1), 1);
        SecretKeyAccessor accessor = () -> data;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENV_ID)
                .setApiKey(API_KEY)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(accessor)
                .setCustomEncryptionConfigsList(cryptoList);
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
    }

    @Test
    public void negativeTestCustomCryptoMultipleCurrent() throws StorageClientException {
        Crypto crypto1 = new DefaultCryptoWithCustomVersion("first", true);
        Crypto crypto2 = new CryptoStub(true);
        List<Crypto> cryptoList = Arrays.asList(crypto1, crypto2);
        SecretKey key1 = new SecretKey(CUSTOM_PASSWORD_1, 1, false, true);
        SecretsData data = new SecretsData(Arrays.asList(key1), 1);
        SecretKeyAccessor accessor = () -> data;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENV_ID)
                .setApiKey(API_KEY)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(accessor)
                .setCustomEncryptionConfigsList(cryptoList);
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
    }

    @Test
    public void negativeTestCustomCryptoWithoutKey() throws StorageClientException {
        Crypto crypto = new CryptoStub(true);
        List<Crypto> cryptoList = Arrays.asList(crypto);
        SecretKey key1 = new SecretKey(CUSTOM_PASSWORD_1, 1, false, false);
        SecretsData data = new SecretsData(Arrays.asList(key1), 1);
        SecretKeyAccessor accessor = () -> data;
        StorageConfig config = new StorageConfig()
                .setEnvId(ENV_ID)
                .setApiKey(API_KEY)
                .setEndPoint(FAKE_ENDPOINT)
                .setSecretKeyAccessor(accessor)
                .setCustomEncryptionConfigsList(cryptoList);
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
    }

    @Test
    public void positiveEncryptDecrypt() throws StorageClientException, StorageCryptoException {
        Crypto crypto = new CryptoStub(true);
        List<Crypto> cryptoList = Arrays.asList(crypto);
        int keyVersion = 1;
        SecretKey key1 = new SecretKey(CUSTOM_PASSWORD_1, keyVersion, false, true);
        SecretsData data = new SecretsData(Arrays.asList(key1), keyVersion);
        SecretKeyAccessor accessor = () -> data;
        CryptoManager manager = new CryptoManager(accessor, ENV_ID, cryptoList, false);
        String text = BODY_FOR_ENCRYPTION;
        Map.Entry<String, Integer> encryptionResult = manager.encrypt(text);
        assertEquals(keyVersion, encryptionResult.getValue());
        String text2 = manager.decrypt(encryptionResult.getKey(), encryptionResult.getValue());
        assertEquals(text, text2);
    }

    @Test
    public void negativeDecryptFormat() throws StorageClientException, StorageCryptoException {
        Crypto crypto = new CryptoStub(true);
        List<Crypto> cryptoList = Arrays.asList(crypto);
        int keyVersion = 1;
        SecretKey key1 = new SecretKey(CUSTOM_PASSWORD_1, keyVersion, false, true);
        SecretsData data = new SecretsData(Arrays.asList(key1), keyVersion);
        SecretKeyAccessor accessor = () -> data;
        CryptoManager manager = new CryptoManager(accessor, ENV_ID, cryptoList, false);

        String wrongCipherText1 = CryptoManager.PREFIX_CUSTOM_ENCRYPTION + UUID.randomUUID() + ":123";
        assertThrows(StorageCryptoException.class, () -> manager.decrypt(wrongCipherText1, keyVersion));

        String wrongCipherText2 = UUID.randomUUID() + ":123";
        assertThrows(StorageCryptoException.class, () -> manager.decrypt(wrongCipherText2, keyVersion));

        Crypto anotherCrypto = new PseudoCustomCrypto(true);
        cryptoList = Arrays.asList(anotherCrypto);
        CryptoManager anotherManager = new CryptoManager(accessor, ENV_ID, cryptoList, false);
        String encryptedAnother = anotherManager.encrypt(BODY_FOR_ENCRYPTION).getKey();
        assertThrows(StorageCryptoException.class, () -> manager.decrypt(encryptedAnother, keyVersion));
    }

    @Test
    public void negativeTestWithCryptoExceptions() throws StorageClientException, StorageCryptoException {
        Crypto crypto = new PseudoCustomCrypto(true, 2, 1, true);
        List<Crypto> cryptoList = Arrays.asList(crypto);
        int keyVersion = 1;
        SecretKey key1 = new SecretKey(CUSTOM_PASSWORD_1, keyVersion, false, true);
        SecretsData data = new SecretsData(Arrays.asList(key1), keyVersion);
        SecretKeyAccessor accessor = () -> data;
        CryptoManager manager = new CryptoManager(accessor, ENV_ID, cryptoList, false);
        String text = BODY_FOR_ENCRYPTION;
        Map.Entry<String, Integer> result = manager.encrypt(text);
        assertThrows(StorageCryptoException.class, () -> manager.encrypt(text));
        assertThrows(StorageCryptoException.class, () -> manager.decrypt(result.getKey(), keyVersion));
    }

    @Test
    public void negativeTestWithCryptoExceptionsInInit() throws StorageClientException {
        Crypto crypto = new PseudoCustomCrypto(true, 0, 0, true);
        List<Crypto> cryptoList = Arrays.asList(crypto);
        int keyVersion = 1;
        SecretKey key1 = new SecretKey(CUSTOM_PASSWORD_1, keyVersion, false, true);
        SecretsData data = new SecretsData(Arrays.asList(key1), keyVersion);
        SecretKeyAccessor accessor = () -> data;
        assertThrows(StorageClientException.class, () -> new CryptoManager(accessor, ENV_ID, cryptoList, false));
    }

    @Test
    public void negativeTestWithUnexpectedExceptions() throws StorageClientException, StorageCryptoException {
        Crypto crypto = new PseudoCustomCrypto(true, 2, 1, false);
        List<Crypto> cryptoList = Arrays.asList(crypto);
        int keyVersion = 1;
        SecretKey key1 = new SecretKey(CUSTOM_PASSWORD_1, keyVersion, false, true);
        SecretsData data = new SecretsData(Arrays.asList(key1), keyVersion);
        SecretKeyAccessor accessor = () -> data;
        CryptoManager manager = new CryptoManager(accessor, ENV_ID, cryptoList, false);
        String text = BODY_FOR_ENCRYPTION;
        Map.Entry<String, Integer> result = manager.encrypt(text);
        assertThrows(StorageClientException.class, () -> manager.encrypt(text));
        assertThrows(StorageClientException.class, () -> manager.decrypt(result.getKey(), keyVersion));
    }

    @Test
    public void negativeTestWithWrongCipher() throws StorageClientException {
        Crypto crypto = new PseudoCustomCrypto(true);
        List<Crypto> cryptoList = Arrays.asList(crypto);
        int keyVersion = 1;
        SecretKey key1 = new SecretKey(CUSTOM_PASSWORD_1, keyVersion, false, true);
        SecretsData data = new SecretsData(Arrays.asList(key1), keyVersion);
        SecretKeyAccessor accessor = () -> data;
        CryptoManager manager = new CryptoManager(accessor, ENV_ID, cryptoList, false);
        assertThrows(StorageClientException.class, () -> manager.decrypt(BODY_FOR_ENCRYPTION, keyVersion));
    }

    @Test
    public void negativeDecryptWrongVersion() throws StorageClientException, StorageCryptoException {
        Crypto crypto = new CryptoStub(true);
        List<Crypto> cryptoList = Arrays.asList(crypto);
        int keyVersion = 1;
        SecretKey key1 = new SecretKey(CUSTOM_PASSWORD_1, keyVersion, false, true);
        SecretsData data = new SecretsData(Arrays.asList(key1), keyVersion);
        SecretKeyAccessor accessor = () -> data;
        CryptoManager manager = new CryptoManager(accessor, ENV_ID, cryptoList, false);
        Map.Entry<String, Integer> result = manager.encrypt(BODY_FOR_ENCRYPTION);
        assertEquals(keyVersion, result.getValue());
        assertThrows(StorageClientException.class, () -> manager.decrypt(result.getKey(), keyVersion + 1));
    }

    @Test
    public void defaultCryptoTest() {
        DefaultCrypto crypto = new DefaultCrypto(StandardCharsets.UTF_8);
        assertTrue(crypto.isCurrent());
    }

    @Test
    public void negativeTestPteAndCustomEnc() {
        List<Crypto> cryptoList = Arrays.asList(new CryptoStub(true), new PseudoCustomCrypto(false));
        StorageConfig config = new StorageConfig()
                .setEnvId(ENV_ID)
                .setApiKey(API_KEY)
                .setEndPoint(FAKE_ENDPOINT)
                .setCustomEncryptionConfigsList(cryptoList);
        assertThrows(StorageClientException.class, () -> StorageImpl.getInstance(config));
    }
}