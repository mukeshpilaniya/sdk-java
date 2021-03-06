package com.incountry.residence.sdk;

import com.incountry.residence.sdk.crypto.testimpl.FernetCrypto;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.dto.search.NumberField;
import com.incountry.residence.sdk.dto.search.StringField;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.incountry.residence.sdk.dto.search.StringField.SERVICE_KEY1;
import static com.incountry.residence.sdk.dto.search.StringField.SERVICE_KEY2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StorageIntegrationTest {

    public static final String INT_INC_COUNTRY = "INT_INC_COUNTRY";
    public static final String INT_INC_COUNTRY_2 = "INT_INC_COUNTRY_2";
    public static final String INT_INC_ENDPOINT = "INT_INC_ENDPOINT";
    private static final String INT_INC_ENVIRONMENT_ID = "INT_INC_ENVIRONMENT_ID";
    private static final String INT_INC_API_KEY = "INT_INC_API_KEY";
    public static final String INT_INC_ENVIRONMENT_ID_OAUTH = "INT_INC_ENVIRONMENT_ID_OAUTH";
    public static final String INT_INC_CLIENT_ID = "INT_INC_CLIENT_ID";
    public static final String INT_INC_CLIENT_SECRET = "INT_INC_CLIENT_SECRET";
    public static final String INT_INC_DEFAULT_AUTH_ENDPOINT = "INT_INC_DEFAULT_AUTH_ENDPOINT";
    public static final String INT_INC_HTTP_POOL_SIZE = "INT_INC_HTTP_POOL_SIZE";
    public static final String INT_INC_EMEA_AUTH_ENDPOINT = "INT_INC_EMEA_AUTH_ENDPOINT";
    public static final String INT_INC_APAC_AUTH_ENDPOINT = "INT_INC_APAC_AUTH_ENDPOINT";
    public static final String INT_INC_ENPOINT_MASK = "INT_INC_ENPOINT_MASK";
    public static final String INT_COUNTRIES_LIST_ENDPOINT = "INT_COUNTRIES_LIST_ENDPOINT";

    private static final Logger LOG = LogManager.getLogger(StorageIntegrationTest.class);

    private static final String TEMP = "-javasdk-" +
            new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +
            "-" +
            UUID.randomUUID().toString().replace("-", "");


    private final Storage storage;
    private final Storage storageIgnoreCase;
    private final SecretKeyAccessor secretKeyAccessor;

    private static final String EMEA = "emea";
    private static final String APAC = "apac";
    private static final String BATCH_RECORD_KEY_1 = "BatchRecordKey" + TEMP;
    private static final String RECORD_KEY = "RecordKey" + TEMP;
    private static final String RECORD_KEY_IGNORE_CASE = RECORD_KEY + "_IgnorE_CasE";
    private static final String PROFILE_KEY = "ProfileKey" + TEMP;
    private static final String KEY_1 = "Key1" + TEMP;
    private static final String KEY_2 = "Key2" + TEMP;
    private static final String KEY_3 = "Key3" + TEMP;
    private static final String KEY_4 = "Key4" + TEMP;
    private static final String KEY_5 = "Key5" + TEMP;
    private static final String KEY_6 = "Key6" + TEMP;
    private static final String KEY_7 = "Key7" + TEMP;
    private static final String KEY_8 = "Key8" + TEMP;
    private static final String KEY_9 = "Key9" + TEMP;
    private static final String KEY_10 = "Key10" + TEMP;
    private static final String SERVICE_KEY_1 = "ServiceKey1" + TEMP;
    private static final String SERVICE_KEY_2 = "ServiceKey2" + TEMP;
    private static final String PRECOMMIT_BODY = "PreсommitBody" + TEMP;
    private static final Long BATCH_WRITE_RANGE_KEY_1 = 2L;
    private static final Long WRITE_RANGE_KEY_1 = 1L;
    private static final Long RANGE_KEY_2 = 2L;
    private static final Long RANGE_KEY_3 = 3L;
    private static final Long RANGE_KEY_4 = 4L;
    private static final Long RANGE_KEY_5 = 5L;
    private static final Long RANGE_KEY_6 = 6L;
    private static final Long RANGE_KEY_7 = 7L;
    private static final Long RANGE_KEY_8 = 8L;
    private static final Long RANGE_KEY_9 = 9L;
    private static final Long RANGE_KEY_10 = 10L;
    private static final String RECORD_BODY = "test";
    private static final Integer HTTP_POOL_SIZE = Integer.valueOf(loadFromEnv(INT_INC_HTTP_POOL_SIZE, "4"));

    private static final String MIDIPOP_COUNTRY = loadFromEnv(INT_INC_COUNTRY);
    private static final String MIDIPOP_COUNTRY_2 = loadFromEnv(INT_INC_COUNTRY_2);
    private static final byte[] ENCRYPTION_SECRET = "123456789_123456789_1234567890Ab".getBytes(StandardCharsets.UTF_8);
    private static final String DEFAULT_AUTH_ENDPOINT = loadFromEnv(INT_INC_DEFAULT_AUTH_ENDPOINT);
    private static final String EMEA_AUTH_ENDPOINT = loadFromEnv(INT_INC_EMEA_AUTH_ENDPOINT);
    private static final String APAC_AUTH_ENDPOINT = loadFromEnv(INT_INC_APAC_AUTH_ENDPOINT);
    private static final String CLIENT_ID = loadFromEnv(INT_INC_CLIENT_ID);
    private static final String SECRET = loadFromEnv(INT_INC_CLIENT_SECRET);
    private static final String ENDPOINT_MASK = loadFromEnv(INT_INC_ENPOINT_MASK);
    private static final String ENV_ID = loadFromEnv(INT_INC_ENVIRONMENT_ID_OAUTH);
    private static final String COUNTRIES_LIST_ENDPOINT = loadFromEnv(INT_COUNTRIES_LIST_ENDPOINT);

    private static final int VERSION = 0;

    public static String loadFromEnv(String key) {
        return System.getenv(key);
    }

    public static String loadFromEnv(String key, String defaultValue) {
        String value = loadFromEnv(key);
        return value == null ? defaultValue : value;
    }


    public StorageIntegrationTest() throws StorageServerException, StorageClientException {
        SecretKey secretKey = new SecretKey(ENCRYPTION_SECRET, VERSION, false);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        SecretsData secretsData = new SecretsData(secretKeyList, VERSION);
        secretKeyAccessor = () -> secretsData;
        storage = StorageImpl.getInstance(loadFromEnv(INT_INC_ENVIRONMENT_ID),
                loadFromEnv(INT_INC_API_KEY),
                loadFromEnv(INT_INC_ENDPOINT),
                secretKeyAccessor);

        StorageConfig config = new StorageConfig()
                .setEnvId(loadFromEnv(INT_INC_ENVIRONMENT_ID))
                .setApiKey(loadFromEnv(INT_INC_API_KEY))
                .setEndPoint(loadFromEnv(INT_INC_ENDPOINT))
                .setSecretKeyAccessor(secretKeyAccessor)
                .setNormalizeKeys(true);
        storageIgnoreCase = StorageImpl.getInstance(config);
    }

    @Test
    @Order(100)
    public void batchWriteTest() throws StorageException {
        List<Record> records = new ArrayList<>();
        Record record = new Record(BATCH_RECORD_KEY_1)
                .setBody(RECORD_BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(BATCH_WRITE_RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        records.add(record);
        storage.batchWrite(MIDIPOP_COUNTRY, records);
    }

    @Test
    @Order(200)
    public void writeTest() throws StorageException {
        Record record = new Record(RECORD_KEY)
                .setBody(RECORD_BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(WRITE_RANGE_KEY_1)
                .setRangeKey2(RANGE_KEY_2)
                .setRangeKey3(RANGE_KEY_3)
                .setRangeKey4(RANGE_KEY_4)
                .setRangeKey5(RANGE_KEY_5)
                .setRangeKey6(RANGE_KEY_6)
                .setRangeKey7(RANGE_KEY_7)
                .setRangeKey8(RANGE_KEY_8)
                .setRangeKey9(RANGE_KEY_9)
                .setRangeKey10(RANGE_KEY_10)
                .setKey1(KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3)
                .setKey4(KEY_4)
                .setKey5(KEY_5)
                .setKey6(KEY_6)
                .setKey7(KEY_7)
                .setKey8(KEY_8)
                .setKey9(KEY_9)
                .setKey10(KEY_10)
                .setPrecommitBody(PRECOMMIT_BODY)
                .setServiceKey1(SERVICE_KEY_1)
                .setServiceKey2(SERVICE_KEY_2);
        storage.write(MIDIPOP_COUNTRY, record);
    }

    @Test
    @Order(300)
    public void readTest() throws StorageException {
        Record incomingRecord = storage.read(MIDIPOP_COUNTRY, RECORD_KEY);
        assertEquals(RECORD_KEY, incomingRecord.getRecordKey());
        assertEquals(RECORD_BODY, incomingRecord.getBody());
        assertEquals(PROFILE_KEY, incomingRecord.getProfileKey());
        assertEquals(KEY_1, incomingRecord.getKey1());
        assertEquals(KEY_2, incomingRecord.getKey2());
        assertEquals(KEY_3, incomingRecord.getKey3());
        assertEquals(KEY_4, incomingRecord.getKey4());
        assertEquals(KEY_5, incomingRecord.getKey5());
        assertEquals(KEY_6, incomingRecord.getKey6());
        assertEquals(KEY_7, incomingRecord.getKey7());
        assertEquals(KEY_8, incomingRecord.getKey8());
        assertEquals(KEY_9, incomingRecord.getKey9());
        assertEquals(KEY_10, incomingRecord.getKey10());
        assertEquals(PRECOMMIT_BODY, incomingRecord.getPrecommitBody());
        assertEquals(SERVICE_KEY_1, incomingRecord.getServiceKey1());
        assertEquals(SERVICE_KEY_2, incomingRecord.getServiceKey2());
        assertEquals(WRITE_RANGE_KEY_1, incomingRecord.getRangeKey1());
        assertEquals(RANGE_KEY_2, incomingRecord.getRangeKey2());
        assertEquals(RANGE_KEY_3, incomingRecord.getRangeKey3());
        assertEquals(RANGE_KEY_4, incomingRecord.getRangeKey4());
        assertEquals(RANGE_KEY_5, incomingRecord.getRangeKey5());
        assertEquals(RANGE_KEY_6, incomingRecord.getRangeKey6());
        assertEquals(RANGE_KEY_7, incomingRecord.getRangeKey7());
        assertEquals(RANGE_KEY_8, incomingRecord.getRangeKey8());
        assertEquals(RANGE_KEY_9, incomingRecord.getRangeKey9());
        assertEquals(RANGE_KEY_10, incomingRecord.getRangeKey10());
        assertNotNull(incomingRecord.getCreatedAt());
        assertNotNull(incomingRecord.getUpdatedAt());
    }

    @Test
    @Order(301)
    public void readIgnoreCaseTest() throws StorageException {
        Record record = new Record(RECORD_KEY_IGNORE_CASE)
                .setBody(RECORD_BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(WRITE_RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        storageIgnoreCase.write(MIDIPOP_COUNTRY, record);

        Record incomingRecord = storageIgnoreCase.read(MIDIPOP_COUNTRY, RECORD_KEY_IGNORE_CASE.toLowerCase());
        assertEquals(RECORD_KEY_IGNORE_CASE, incomingRecord.getRecordKey());
        assertEquals(RECORD_BODY, incomingRecord.getBody());
        assertEquals(PROFILE_KEY, incomingRecord.getProfileKey());
        assertEquals(KEY_2, incomingRecord.getKey2());
        assertEquals(KEY_3, incomingRecord.getKey3());

        incomingRecord = storageIgnoreCase.read(MIDIPOP_COUNTRY, RECORD_KEY_IGNORE_CASE.toUpperCase());
        assertEquals(RECORD_KEY_IGNORE_CASE, incomingRecord.getRecordKey());
        assertEquals(RECORD_BODY, incomingRecord.getBody());
        assertEquals(PROFILE_KEY, incomingRecord.getProfileKey());
        assertEquals(KEY_2, incomingRecord.getKey2());
        assertEquals(KEY_3, incomingRecord.getKey3());
    }

    @Test
    @Order(400)
    public void findTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(StringField.RECORD_KEY, RECORD_KEY)
                .keyEq(StringField.KEY2, KEY_2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY)
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1);
        BatchRecord batchRecord = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(RECORD_KEY, batchRecord.getRecords().get(0).getRecordKey());
        assertNotNull(batchRecord.getRecords().get(0).getCreatedAt());
        assertNotNull(batchRecord.getRecords().get(0).getUpdatedAt());

        builder.clear()
                .keyEq(StringField.RECORD_KEY, BATCH_RECORD_KEY_1)
                .keyEq(StringField.KEY2, KEY_2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY)
                .keyEq(NumberField.RANGE_KEY1, BATCH_WRITE_RANGE_KEY_1);
        batchRecord = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(BATCH_RECORD_KEY_1, batchRecord.getRecords().get(0).getRecordKey());

        builder.clear()
                .keyEq(StringField.KEY2, KEY_2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY);
        batchRecord = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(2, batchRecord.getCount());
        assertEquals(2, batchRecord.getRecords().size());
        assertTrue(batchRecord.getRecords().stream().anyMatch(record
                -> record.getRecordKey().equals(BATCH_RECORD_KEY_1)));
        assertTrue(batchRecord.getRecords().stream().anyMatch(record
                -> record.getRecordKey().equals(RECORD_KEY)));

        builder.clear()
                .keyNotEq(StringField.RECORD_KEY, RECORD_KEY)
                .keyEq(StringField.KEY2, KEY_2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY);
        batchRecord = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(BATCH_RECORD_KEY_1, batchRecord.getRecords().get(0).getRecordKey());
    }

    @Test
    @Order(401)
    public void findAdvancedTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(StringField.KEY2, KEY_2)
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1, BATCH_WRITE_RANGE_KEY_1, WRITE_RANGE_KEY_1 + BATCH_WRITE_RANGE_KEY_1 + 1);
        BatchRecord batchRecord = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(2, batchRecord.getCount());
        assertEquals(2, batchRecord.getRecords().size());
        List<String> resultIdList = new ArrayList<>();
        resultIdList.add(batchRecord.getRecords().get(0).getRecordKey());
        resultIdList.add(batchRecord.getRecords().get(1).getRecordKey());
        assertTrue(resultIdList.contains(RECORD_KEY));
        assertTrue(resultIdList.contains(BATCH_RECORD_KEY_1));
    }

    @Test
    @Order(402)
    public void findIgnoreCaseTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(StringField.RECORD_KEY, RECORD_KEY_IGNORE_CASE)
                .keyEq(StringField.KEY2, KEY_2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY)
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1);
        BatchRecord batchRecord = storageIgnoreCase.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(RECORD_KEY_IGNORE_CASE, batchRecord.getRecords().get(0).getRecordKey());

        builder = builder.clear()
                .keyEq(StringField.RECORD_KEY, RECORD_KEY_IGNORE_CASE.toLowerCase())
                .keyEq(StringField.KEY2, KEY_2.toLowerCase())
                .keyEq(StringField.KEY3, KEY_3.toLowerCase())
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY.toLowerCase())
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1);
        batchRecord = storageIgnoreCase.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(RECORD_KEY_IGNORE_CASE, batchRecord.getRecords().get(0).getRecordKey());

        builder = builder.clear()
                .keyEq(StringField.RECORD_KEY, RECORD_KEY_IGNORE_CASE.toUpperCase())
                .keyEq(StringField.KEY2, KEY_2.toUpperCase())
                .keyEq(StringField.KEY3, KEY_3.toUpperCase())
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY.toUpperCase())
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1);
        batchRecord = storageIgnoreCase.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        assertEquals(RECORD_KEY_IGNORE_CASE, batchRecord.getRecords().get(0).getRecordKey());
    }

    @Test
    @Order(403)
    public void findByVersionTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(StringField.KEY2, KEY_2)
                .keyEq(StringField.VERSION, String.valueOf(VERSION));
        BatchRecord batchRecord1 = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(2, batchRecord1.getCount());
        assertEquals(2, batchRecord1.getRecords().size());

        builder.keyEq(StringField.VERSION, String.valueOf(VERSION + 10));
        BatchRecord batchRecord2 = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(0, batchRecord2.getCount());
        assertEquals(0, batchRecord2.getRecords().size());

        builder.keyNotEq(StringField.VERSION, String.valueOf(VERSION));
        BatchRecord batchRecord3 = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(0, batchRecord3.getCount());
        assertEquals(0, batchRecord3.getRecords().size());

        builder.keyNotEq(StringField.VERSION, String.valueOf(VERSION + 10));
        BatchRecord batchRecord4 = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(2, batchRecord4.getCount());
        assertEquals(2, batchRecord4.getRecords().size());
    }

    @Test
    @Order(404)
    public void findByAllFieldsTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(StringField.RECORD_KEY, RECORD_KEY)
                .keyEq(StringField.KEY1, KEY_1)
                .keyEq(StringField.KEY2, KEY_2)
                .keyEq(StringField.KEY3, KEY_3)
                .keyEq(StringField.KEY4, KEY_4)
                .keyEq(StringField.KEY5, KEY_5)
                .keyEq(StringField.KEY6, KEY_6)
                .keyEq(StringField.KEY7, KEY_7)
                .keyEq(StringField.KEY8, KEY_8)
                .keyEq(StringField.KEY9, KEY_9)
                .keyEq(StringField.KEY10, KEY_10)
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1)
                .keyEq(NumberField.RANGE_KEY2, RANGE_KEY_2)
                .keyEq(NumberField.RANGE_KEY3, RANGE_KEY_3)
                .keyEq(NumberField.RANGE_KEY4, RANGE_KEY_4)
                .keyEq(NumberField.RANGE_KEY5, RANGE_KEY_5)
                .keyEq(NumberField.RANGE_KEY6, RANGE_KEY_6)
                .keyEq(NumberField.RANGE_KEY7, RANGE_KEY_7)
                .keyEq(NumberField.RANGE_KEY8, RANGE_KEY_8)
                .keyEq(NumberField.RANGE_KEY9, RANGE_KEY_9)
                .keyEq(NumberField.RANGE_KEY10, RANGE_KEY_10)
                .keyEq(StringField.PROFILE_KEY, PROFILE_KEY)
                .keyEq(SERVICE_KEY1, SERVICE_KEY_1)
                .keyEq(SERVICE_KEY2, SERVICE_KEY_2);

        BatchRecord batchRecord = storage.find(MIDIPOP_COUNTRY, builder);
        assertEquals(1, batchRecord.getCount());
        assertEquals(1, batchRecord.getRecords().size());
        Record record = batchRecord.getRecords().get(0);
        assertEquals(RECORD_KEY, record.getRecordKey());
        assertEquals(KEY_1, record.getKey1());
        assertEquals(KEY_2, record.getKey2());
        assertEquals(KEY_3, record.getKey3());
        assertEquals(KEY_4, record.getKey4());
        assertEquals(KEY_5, record.getKey5());
        assertEquals(KEY_6, record.getKey6());
        assertEquals(KEY_7, record.getKey7());
        assertEquals(KEY_8, record.getKey8());
        assertEquals(KEY_9, record.getKey9());
        assertEquals(KEY_10, record.getKey10());
        assertEquals(WRITE_RANGE_KEY_1, record.getRangeKey1());
        assertEquals(RANGE_KEY_2, record.getRangeKey2());
        assertEquals(RANGE_KEY_3, record.getRangeKey3());
        assertEquals(RANGE_KEY_4, record.getRangeKey4());
        assertEquals(RANGE_KEY_5, record.getRangeKey5());
        assertEquals(RANGE_KEY_6, record.getRangeKey6());
        assertEquals(RANGE_KEY_7, record.getRangeKey7());
        assertEquals(RANGE_KEY_8, record.getRangeKey8());
        assertEquals(RANGE_KEY_9, record.getRangeKey9());
        assertEquals(RANGE_KEY_10, record.getRangeKey10());
        assertEquals(PROFILE_KEY, record.getProfileKey());
        assertEquals(SERVICE_KEY_1, record.getServiceKey1());
        assertEquals(SERVICE_KEY_2, record.getServiceKey2());
    }

    @Test
    @Order(500)
    public void findOneTest() throws StorageException {
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(StringField.KEY2, KEY_2)
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1);
        Record record = storage.findOne(MIDIPOP_COUNTRY, builder);
        assertEquals(RECORD_KEY, record.getRecordKey());
        assertEquals(RECORD_BODY, record.getBody());
    }

    @Test
    @Order(600)
    public void customEncryptionTest() throws StorageException {
        SecretKey customSecretKey = new SecretKey(ENCRYPTION_SECRET, VERSION + 1, false, true);
        List<SecretKey> secretKeyList = new ArrayList<>(secretKeyAccessor.getSecretsData().getSecrets());
        secretKeyList.add(customSecretKey);
        SecretsData anotherSecretsData = new SecretsData(secretKeyList, customSecretKey.getVersion());
        SecretKeyAccessor customAccessor = () -> anotherSecretsData;
        List<Crypto> cryptoList = new ArrayList<>();
        cryptoList.add(new FernetCrypto(true));

        StorageConfig config = new StorageConfig()
                .setEnvId(loadFromEnv(INT_INC_ENVIRONMENT_ID))
                .setApiKey(loadFromEnv(INT_INC_API_KEY))
                .setEndPoint(loadFromEnv(INT_INC_ENDPOINT))
                .setSecretKeyAccessor(customAccessor)
                .setCustomEncryptionConfigsList(cryptoList);

        Storage storage2 = StorageImpl.getInstance(config);
        //write record with custom enc
        String customRecordKey = RECORD_KEY + "_custom";
        Record record = new Record(customRecordKey)
                .setBody(RECORD_BODY)
                .setProfileKey(PROFILE_KEY)
                .setRangeKey1(WRITE_RANGE_KEY_1)
                .setKey2(KEY_2)
                .setKey3(KEY_3);
        storage2.write(MIDIPOP_COUNTRY, record);
        //read record with custom enc
        Record record1 = storage2.read(MIDIPOP_COUNTRY, customRecordKey);
        assertEquals(record.getBody(), record1.getBody());
        assertEquals(record.getRecordKey(), record1.getRecordKey());
        assertEquals(record.getProfileKey(), record1.getProfileKey());
        assertEquals(record.getRangeKey1(), record1.getRangeKey1());
        assertEquals(record.getKey2(), record1.getKey2());
        assertEquals(record.getKey3(), record1.getKey3());
        //read recorded record with default encryption
        Record record2 = storage2.read(MIDIPOP_COUNTRY, RECORD_KEY);
        assertEquals(RECORD_BODY, record2.getBody());
        //find record with custom enc
        FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq(StringField.RECORD_KEY, customRecordKey)
                .keyEq(NumberField.RANGE_KEY1, WRITE_RANGE_KEY_1);
        Record record3 = storage2.findOne(MIDIPOP_COUNTRY, builder);
        assertEquals(record.getRecordKey(), record3.getRecordKey());
        assertEquals(record.getRangeKey1(), record3.getRangeKey1());
        //delete record with custom enc
        storage2.delete(MIDIPOP_COUNTRY, customRecordKey);
        Record record4 = storage2.read(MIDIPOP_COUNTRY, customRecordKey);
        assertNull(record4);
    }

    @Test
    @Order(700)
    public void deleteTest() throws StorageException {
        storage.delete(MIDIPOP_COUNTRY, RECORD_KEY);
        storage.delete(MIDIPOP_COUNTRY, BATCH_RECORD_KEY_1);
        // Cannot read deleted record
        Record writeMethodRecord = storage.read(MIDIPOP_COUNTRY, RECORD_KEY);
        Record batchWriteMethodRecord = storage.read(MIDIPOP_COUNTRY, BATCH_RECORD_KEY_1);
        assertNull(writeMethodRecord);
        assertNull(batchWriteMethodRecord);
    }

    @Test
    @Order(701)
    public void deleteIgnoreCaseTest() throws StorageException {
        storageIgnoreCase.delete(MIDIPOP_COUNTRY, RECORD_KEY_IGNORE_CASE.toUpperCase());
        // Cannot read deleted record
        Record record = storageIgnoreCase.read(MIDIPOP_COUNTRY, RECORD_KEY_IGNORE_CASE);
        assertNull(record);
        record = storageIgnoreCase.read(MIDIPOP_COUNTRY, RECORD_KEY_IGNORE_CASE.toUpperCase());
        assertNull(record);
        record = storageIgnoreCase.read(MIDIPOP_COUNTRY, RECORD_KEY_IGNORE_CASE.toLowerCase());
        assertNull(record);
    }

    @Test
    @Order(800)
    public void connectionPoolTest() throws StorageException, InterruptedException, ExecutionException {
        SecretKey secretKey = new SecretKey(ENCRYPTION_SECRET, VERSION, false);
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);
        SecretsData secretsData = new SecretsData(secretKeyList, VERSION);
        SecretKeyAccessor mySecretKeyAccessor = () -> secretsData;

        Map<String, String> authMap = new HashMap<>();
        if (EMEA_AUTH_ENDPOINT != null && !EMEA_AUTH_ENDPOINT.isEmpty()) {
            authMap.put(EMEA, EMEA_AUTH_ENDPOINT);
        }
        if (APAC_AUTH_ENDPOINT != null && !APAC_AUTH_ENDPOINT.isEmpty()) {
            authMap.put(APAC, APAC_AUTH_ENDPOINT);
        }
        StorageConfig config = new StorageConfig()
                .setClientId(CLIENT_ID)
                .setClientSecret(SECRET)
                .setDefaultAuthEndpoint(DEFAULT_AUTH_ENDPOINT)
                .setEndpointMask(ENDPOINT_MASK)
                .setEnvId(ENV_ID)
                .setSecretKeyAccessor(mySecretKeyAccessor)
                .setCountriesEndpoint(COUNTRIES_LIST_ENDPOINT)
                .setMaxHttpPoolSize(HTTP_POOL_SIZE)
                .setMaxHttpConnectionsPerRoute(HTTP_POOL_SIZE / 2);
        if (!authMap.isEmpty()) {
            config.setAuthEndpoints(authMap);
        }
        Storage customStorage = StorageImpl.getInstance(config);
        //http pool size < concurrent threads < count of threads
        ExecutorService executorService = Executors.newFixedThreadPool(HTTP_POOL_SIZE / 2);
        List<Future<StorageException>> futureList = new ArrayList<>();
        Long startTime = System.currentTimeMillis();
        int taskCount = HTTP_POOL_SIZE * 2;
        for (int i = 0; i < taskCount; i++) {
            futureList.add(executorService.submit(createCallableTask(customStorage, i)));
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.MINUTES);
        int successfulTaskCount = 0;
        for (Future<StorageException> one : futureList) {
            assertTrue(one.isDone());
            if (one.get() == null) {
                successfulTaskCount += 1;
            }
        }
        Long finishTime = System.currentTimeMillis();
        LOG.debug("connectionPoolTest duration time = {} ms, average speed = {} ms per 1 task", finishTime - startTime, (finishTime - startTime) / taskCount);
        assertEquals(taskCount, successfulTaskCount);
    }

    private Callable<StorageException> createCallableTask(final Storage storage, final int numb) {
        return () -> {
            try {
                String randomKey = RECORD_KEY + UUID.randomUUID().toString();
                Thread currentThread = Thread.currentThread();
                currentThread.setName("connectionPoolTest #" + numb);
                Record record = new Record(randomKey)
                        .setBody(RECORD_BODY)
                        .setProfileKey(PROFILE_KEY)
                        .setRangeKey1(WRITE_RANGE_KEY_1)
                        .setKey2(KEY_2)
                        .setKey3(KEY_3);
                String country = (numb % 2 == 0 ? MIDIPOP_COUNTRY : MIDIPOP_COUNTRY_2);
                storage.write(country, record);
                Record incomingRecord = storage.read(country, randomKey);
                assertEquals(randomKey, incomingRecord.getRecordKey());
                assertEquals(RECORD_BODY, incomingRecord.getBody());
                assertEquals(PROFILE_KEY, incomingRecord.getProfileKey());
                assertEquals(KEY_2, incomingRecord.getKey2());
                assertEquals(KEY_3, incomingRecord.getKey3());
                storage.delete(country, randomKey);
            } catch (StorageException exception) {
                LOG.error("Exception in connectionPoolTest", exception);
                return exception;
            }
            return null;
        };
    }
}
