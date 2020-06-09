InCountry Storage SDK
===========
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=incountry_sdk-java&metric=alert_status)](https://sonarcloud.io/dashboard?id=incountry_sdk-java)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=incountry_sdk-java&metric=coverage)](https://sonarcloud.io/dashboard?id=incountry_sdk-java)
Installation
-----
Incountry Storage SDK requires Java Developer Kit 1.8 or higher, recommended language level 8.

For Maven users please add this section to your dependencies list
```xml
<dependency>
  <groupId>com.incountry</groupId>
  <artifactId>incountry-java-client</artifactId>
  <version>2.0.0</version>
</dependency>
```

For Gradle users please add this line to your dependencies list
```groovy
compile "com.incountry:incountry-java-client:2.0.0"
```

Countries List
----
For a full list of supported countries and their codes please [follow this link](countries.md).

Usage
-----
Use `StorageImpl` class to access your data in InCountry using Java SDK.
```java
public class StorageImpl implements Storage {
  /**
   * creating Storage instance
   *
   * @param environmentID     Required to be passed in, or as environment variable INC_API_KEY
   * @param apiKey            Required to be passed in, or as environment variable INC_ENVIRONMENT_ID
   * @param endpoint          Optional. Defines API URL.
   *                          Default endpoint will be used if this param is null
   * @param secretKeyAccessor Instance of SecretKeyAccessor class. Used to fetch encryption secret
   * @return instance of Storage
   * @throws StorageClientException if configuration validation finished with errors
   * @throws StorageServerException if server connection failed or server response error
   */
  public static Storage getInstance(String environmentID, String apiKey, String endpoint,
                            SecretKeyAccessor secretKeyAccessor) throws StorageServerException {...}
//...
}
```

Parameters `environmentID` and `apiKey` (or `clientId` and `clientSecret` instead of `apiKey`) can be fetched from your dashboard on `Incountry` site.

You can turn off encryption (not recommended) by providing `null` value for parameter `secretKeyAccessor`.

Below is an example how to create a storage instance:
```java
SecretKeyAccessor secretKeyAccessor = () -> SecretsDataGenerator.fromPassword("<password>");
String endPoint = "https://us.api.incountry.io";
String envId = "<env_id>";
String apiKey = "<api_key>";
Storage storage = StorageImpl.getInstance(envId, apiKey, endPoint, secretKeyAccessor);
```

#### oAuth Authentication

SDK also supports oAuth authentication credentials instead of plain API key authorization. oAuth authentication flow is mutually exclusive with API key authentication - you will need to provide either API key or oAuth credentials.

Below is the example how to create storage instance with oAuth credentials (and also provide custom oAuth endpoint):
```java
StorageConfig config = new StorageConfig()
   .setClientId(CLIENT_ID)  //can be also set via environment variable INC_CLIENT_ID
   .setClientSecret(SECRET) //can be also set via environment variable INC_CLIENT_SECRET
   .setAuthEndPoint(AUTH_URL) //can be also set via environment variable INC_AUTH_ENDPOINT
   .setEndpointMask(ENDPOINT_MASK)
   .setEnvId(ENV_ID);                                
Storage storage = StorageImpl.getInstance(config);
```

Note: parameter endpointMask is used for switching from default InCountry host family (api.incountry.io) to a different one. For example setting `endpointMask`==`private.incountry.io ` will make all further requests to be sent to `https://{COUNTRY_CODE}.private.incountry.io`
If your PoPAPI configuration relies on a custom PoPAPI server (rather than the default one) use `countriesEndpoint` option to specify the endpoint responsible for fetching supported countries list.  
```java
StorageConfig config = new StorageConfig()
   .setCountriesEndpoint(countriesEndpoint)  
   //...                                
Storage storage = StorageImpl.getInstance(config);
```

### Encryption key/secret

SDK provides `SecretKeyAccessor` interface which allows you to pass your own secrets/keys to the SDK.
```java
/**
 * Secrets accessor. Method {@link SecretKeyAccessor#getSecretsData()} is invoked on each encryption/decryption.
 */
public interface SecretKeyAccessor {

    /**
     * get your container with secrets
     *
     * @return SecretsData
     * @throws StorageClientException when something goes wrong during getting secrets
     */
    SecretsData getSecretsData() throws StorageClientException;
}


public class SecretsData {
    /**
     * creates a container with secrets
     *
     * @param secrets non-empty list of secrets. One of the secrets must have
     *        same version as currentVersion in SecretsData
     * @param currentVersion Should be a non-negative integer
     * @throws StorageClientException when parameter validation fails
     */
     public SecretsData(List<SecretKey> secrets, int currentVersion)
                throws StorageClientException {...}
    //...
}


public class SecretKey {
    /**
    * Creates a secret key
    *
    * @param secret  secret/key
    * @param version secret version, should be a non-negative integer
    * @param isKey   should be True only for user-defined encryption keys
    * @throws StorageClientException when parameter validation fails
    */
    public SecretKey(String secret, int version, boolean isKey)
              throws StorageClientException {...}
    //...
}
```

You can implement `SecretKeyAccessor` interface and pass secrets/keys in multiple ways:

1. As a constant SecretsData object
    ```java
    SecretsData secretsData = new SecretsData(secretsList, currentVersion);
    SecretKeyAccessor accessor = () -> secretsData;
    ```

2. As a function that dynamically fetches secrets
    ```java
    SecretKeyAccessor accessor = () -> loadSecretsData();

    private SecretsData loadSecretsData()  {
       String url = "<your_secret_url>";
       String responseJson = loadFromUrl(url).asJson();
       return SecretsDataGenerator.fromJson(responseJson);
    }
    ```

You can also use `SecretsDataGenerator` class for creating `SecretsData` instances:

1. from a String password
    ```java
    SecretsData secretsData = SecretsDataGenerator.fromPassword("<password>");
    ```

2. from a JSON string representing SecretsData object
    ```java
    SecretsData secretsData = SecretsDataGenerator.fromJson(jsonString);
    ```

    ```javascript
    {
    "secrets": [
        {
        "secret": "secret0",
        "version": 0,
        "isKey": false
        },
        {
        "secret": "secret1",
        "version": 1,
        "isKey": false
        }
    ],
    "currentVersion": 1
    }
    ```

`SecretsData` allows you to specify multiple keys/secrets which SDK will use for decryption based on the version of the key or secret used for encryption.

Meanwhile SDK will encrypt only using key/secret that matches `currentVersion` provided in `SecretsData` object. This enables the flexibility required to support Key Rotation policies when secrets/keys need to be changed with time.

SDK will encrypt data using current secret/key while maintaining the ability to decrypt records encrypted with old keys/secrets.
SDK also provides a method for data migration which allows to re-encrypt data with the newest key/secret. For details please see [migrate](#Data-Migration-and-Key-Rotation-support) method.

SDK allows you to use custom encryption keys, instead of secrets. Please note that user-defined encryption key should be a 32-characters 'utf8' encoded string as required by AES-256 cryptographic algorithm.

Note: even though SDK uses PBKDF2 to generate a cryptographically strong encryption key, you must make sure you provide a secret/password which follows modern security best practices and standards.

### Writing data to Storage

Use `write` method in order to create a record.
```java
public interface Storage {
    /**
     * Write data to remote storage
     *
     * @param country country identifier
     * @param record  object which encapsulate data which must be written in storage
     * @return recorded record
     * @throws StorageClientException if validation finished with errors
     * @throws StorageServerException if server connection failed or server response error
     * @throws StorageCryptoException if encryption failed
     */
    Record write(String country, Record record)
          throws StorageClientException, StorageServerException, StorageCryptoException;
    //...
}
```

Here is how you initialize a record object:
```java
public class Record {
    /**
     * Full constructor
     *
     * @param key        Required, record key
     * @param body       Optional, data to be stored and encrypted
     * @param profileKey Optional, profile key
     * @param rangeKey   Optional, range key
     * @param key2       Optional, key2
     * @param key3       Optional, key3
     */
    public Record(String key, String body, String profileKey, Integer rangeKey, String key2, String key3)
    //...
}
```

Below is the example of how you may use `write` method:
```java
key = "user_1";
body = "some PII data";
profileKey = "customer";
rangeKey = 10000;
key2 = "english";
key3 = "rolls-royce";
Record record = new Record(key, body, profileKey, batchWriteRangeKey, key2, key3);
storage.write("us", record);
```

#### Encryption
InCountry uses client-side encryption for your data. Note that only body is encrypted. Some of other fields are hashed.
Here is how data is transformed and stored in InCountry database:
```java
public class Record {
    private String key;          // hashed
    private String body;         // encrypted
    private String profileKey;   // hashed
    private Integer rangeKey;    // plain
    private String key2;         // hashed
    private String key3;         // hashed
    //...
}
```

### Batches

Use the `batchWrite` method to write multiple records to the storage in a single request.
```java
public interface Storage {
     /**
      * Write multiple records at once in remote storage
      *
      * @param country country identifier
      * @param records record list
      * @return BatchRecord object which contains list of recorded records
      * @throws StorageClientException if validation finished with errors
      * @throws StorageServerException if server connection failed or server response error
      * @throws StorageCryptoException if record encryption failed
      */
     BatchRecord batchWrite(String country, List<Record> records)
          throws StorageClientException, StorageServerException, StorageCryptoException;
     //...
}
```

 Below is the example of how you may use `batchWrite` method
```java
List<Record> list = new ArrayList<>();
list.add(new Record(firstKey, firstBody, firstProfileKey, firstRangeKey, firstKey2, firstKey3));
list.add(new Record(secondKey, secondBody, secondProfileKey, secondRangeKey, secondKey2, secondKey3));
storage.batchWrite("us", list);
```

### Reading stored data

Stored record can be read by `key` using `read` method.
```java
public interface Storage {
   /**
    * Read data from remote storage
    *
    * @param country   country identifier
    * @param key record unique identifier
    * @return Record object which contains required data
    * @throws StorageClientException if validation finished with errors
    * @throws StorageServerException if server connection failed or server response error
    * @throws StorageCryptoException if decryption failed
    */
    Record read(String country, String key)
        throws StorageClientException, StorageServerException, StorageCryptoException;
    //...
}
```

Below is the example of how you may use `read` method:
 ```java
String key = "user_1";
Record record = storage.read("us", key);
String decryptedBody = record.getBody();
 ```

`Record` contains the following properties: `key`, `body`, `key2`, `key3`, `profileKey`, `rangeKey`.

These properties can be accessed using getters, for example:

```java
String key2 = record.getKey2();
String body = record.getBody();
```

### Find records

It is possible to search by random keys using `find` method.
```java
public interface Storage {
   /**
    * Find records in remote storage according to filters
    *
    * @param country country identifier
    * @param builder object representing find filters and search options
    * @return BatchRecord object which contains required records
    * @throws StorageClientException if validation finished with errors
    * @throws StorageServerException if server connection failed or server response error
    * @throws StorageCryptoException if decryption failed
    */
    BatchRecord find(String country, FindFilterBuilder builder)
         throws StorageClientException, StorageServerException, StorageCryptoException;
    //...
}
```

Use `FindFilterBuilder` class to refine your find request.

Below is the example how to use `find` method along with `FindFilterBuilder`:
```java
FindFilterBuilder builder = FindFilterBuilder.create()
                  .key2Eq("kitty")
                  .key3Eq("firstValue","secondValue")
                  .rangeKeyBetween(123, 456);

BatchRecord findResult = storage.find("us", builder);
if (findResult.getCount() > 0) {
    Record record = findResult.getRecords().get(0);
    //...
}
```

The request will return records, filtered according to the following pseudo-sql
```sql
key2 = 'kitty' AND key3 in ('firstValue' , 'secondValue') AND (123 < = `rangeKey` < = 456)
```

All conditions added via `FindFilterBuilder` are joined using logical `AND`. You may not add multiple conditions for the same key - if you do only the last one will be used.

SDK returns 100 records at most. Use `limit` and `offset` to iterate through the records.
```java
FindFilterBuilder builder = FindFilterBuilder.create()
                  //...
                  .limitAndOffset(20, 80);
BatchRecord records = storage.find("us", builder);
```

Next predicate types are available for each string key field of class `Record` via individual methods of `FindFilterBuilder`:
```java
EQUALS         (FindFilterBuilder::keyEq)
               (FindFilterBuilder::key2Eq)
               (FindFilterBuilder::key3Eq)
               (FindFilterBuilder::profileKeyEq)
NOT_EQUALS     (FindFilterBuilder::keyNotEq)
               (FindFilterBuilder::key2NotEq)
               (FindFilterBuilder::key3NotEq)
               (FindFilterBuilder::profileKeyNotEq)
```

You can use the following builder methods for filtering by numerical `rangeKey` field:
```java
EQUALS              (FindFilterBuilder::rangeKeyEq)
IN                  (FindFilterBuilder::rangeKeyIn)
GREATER             (FindFilterBuilder::rangeKeyGT)
GREATER OR EQUALS   (FindFilterBuilder::rangeKeyGTE)
LESS                (FindFilterBuilder::rangeKeyLT)
LESS OR EQUALS      (FindFilterBuilder::rangeKeyLTE)
BETWEEN             (FindFilterBuilder::rangeKeyBetween)
```

Method `find` returns `BatchRecord` object which contains a list of `Record` and some metadata:
```java
class BatchRecord {
    private int count;
    private int limit;
    private int offset;
    private int total;
    private List<Record> records;
    //...
}
```

These fields can be accessed using getters, for example:

```java
int limit = records.getTotal();
```

`BatchRecord.getErrors()` allows you to get a List of `RecordException` objects which contains detailed information about records that failed to be processed correctly during `find` request.

### Find one record matching filter

If you need to find the first record matching filter, you can use `findOne` method:
```java
public interface Storage {
   /**
    * Find only one first record in remote storage according to filters
    *
    * @param country country identifier
    * @param builder object representing find filters
    * @return founded record or null
    * @throws StorageClientException if validation finished with errors
    * @throws StorageServerException if server connection failed or server response error
    * @throws StorageCryptoException if decryption failed
    */
    Record findOne(String country, FindFilterBuilder builder)
           throws StorageClientException, StorageServerException, StorageCryptoException;
    //...
}
```

It works the same way as `find` but returns the first record or `null` if no records found.

Here is the example of how `findOne` method can be used:
```java
FindFilterBuilder builder = FindFilterBuilder.create()
                .key2Eq("kitty")
                .key3Eq("firstValue", "secondValue")
                .rangeKeyBetween(123, 456);

Record record = storage.findOne("us", builder);
//...
```

### Delete records

Use `delete` method in order to delete a record from InCountry storage. It is only possible using `key` field.
```java
public interface Storage {
    /**
    * Delete record from remote storage
    *
    * @param country   country code of the record
    * @param key the record's key
    * @return true when record was deleted
    * @throws StorageClientException if validation finished with errors
    * @throws StorageServerException if server connection failed
    */
    boolean delete(String country, String key)
            throws StorageClientException, StorageServerException;
    //...
}
```

Below is the example of how you may use `delete` method:
 ```java
String key = "user_1";
storage.delete("us", key);
 ```

Data Migration and Key Rotation support
-----

Using `SecretKeyAccessor` that provides `SecretsData` object enables key rotation and data migration support.

SDK introduces method `migrate`
```java
public interface Storage {
   /**
    * Make batched key-rotation-migration of records
    *
    * @param country country identifier
    * @param limit   batch-limit parameter
    * @return MigrateResult object which contain total records
    *         left to migrate and total amount of migrated records
    * @throws StorageClientException if validation finished with errors
    * @throws StorageServerException if server connection failed or server response error
    * @throws StorageCryptoException if decryption failed
    */
    MigrateResult migrate(String country, int limit)
           throws StorageClientException, StorageServerException, StorageCryptoException;
    //...
}
```

It allows you to re-encrypt data encrypted with old versions of the secret. You should specify `country` you want to conduct migration in
and `limit` for precise amount of records to migrate. `migrate` returns a `MigrateResult` object which contains some information about the migration - the
amount of records migrated (`migrated`) and the amount of records left to migrate (`totalLeft`) (which basically means the amount of records with
version different from `currentVersion` provided by `SecretKeyAccessor`)

```java
public class MigrateResult {
    private int migrated;
    private int totalLeft;
    //...
}
```

For detailed example of a migration usage please [follow this link](/src/integration/java/com/incountry/residence/sdk/FullMigrationExample.java).

Error Handling
-----

InCountry Java SDK throws following Exceptions:
- **StorageClientException** - used for various input validation errors
- **StorageServerException** - thrown if SDK failed to communicate with InCountry servers or if server response validation failed.
- **StorageCryptoException** - thrown during encryption/decryption procedures (both default and custom). This may be a sign of malformed/corrupt data or a wrong encryption key provided to the SDK.
- **StorageException** - general exception. Inherited by all other exceptions

We suggest gracefully handling all the possible exceptions:

```java
public void test() {
    try {
        // use InCountry Storage instance here
    } catch (StorageClientException e) {
        // some input validation error
    } catch (StorageServerException e) {
        // some server error
    } catch (StorageCryptoException e) {
        // some encryption error
    } catch (StorageException e) {
        // general error
    } catch (Exception e) {
        // something else happened not related to InCountry SDK
    }
}
```

Custom Encryption Support
-----
SDK supports the ability to provide custom encryption/decryption methods if you decide to use your own algorithm instead of the default one.
One of the overloaded versions of the method `getInstance` in class `StorageImpl` allows you to pass a list of custom encryption implementations:

```java
/**
  * creating Storage instance
  *
  * @param config Configuration for Storage initialization
  * @return instance of Storage
  * @throws StorageClientException if configuration validation finished with errors
  * @throws StorageCryptoException if custom encryption fails during initialization
  * @throws StorageServerException if server connection failed or server response error
  */
public static Storage getInstance(StorageConfig config)
              throws StorageClientException, StorageServerException {...}
```

Class `StorageConfig` is a container with Storage configuration, using Builder pattern. Use method `setCustomEncryptionConfigsList` for passing a list of custom encryption implementations:

```java
public class StorageConfig {
   private String envId;
       private String apiKey;
       private String endPoint;
       private SecretKeyAccessor secretKeyAccessor;
       private List<Crypto> customEncryptionConfigsList;
       private boolean normalizeKeys;
       private String clientId;
       private String clientSecret;
       private String authEndPoint;
       private Integer httpTimeout;
    //...

    /**
     * for custom encryption
     *
     * @param customEncryptionConfigsList List with custom encryption functions
     * @return StorageConfig
     */
    public StorageConfig setCustomEncryptionConfigsList(List<Crypto> customEncryptionConfigsList) {
        this.customEncryptionConfigsList = customEncryptionConfigsList;
        return this;
    }

    /**
     * Set HTTP requests timeout. Parameter is optional. Should be greater than 0.
     * Default value is 30 seconds.
     *
     * @param httpTimeout timeout in seconds
     * @return StorageConfig
     */
    public StorageConfig setHttpTimeout(Integer httpTimeout) {
        this.httpTimeout = httpTimeout;
        return this;
    }


    //...
}
```

For using of custom encryption you need to implement the following interface:
```java
public interface Crypto {
    /**
     * encrypts data with secret
     *
     * @param text      data for encryption
     * @param secretKey secret
     * @return encrypted data as String
     * @throws StorageClientException when parameters validation fails
     * @throws StorageCryptoException when decryption fails
     */
    String encrypt(String text, SecretKey secretKey) 
            throws StorageClientException, StorageCryptoException;

    /**
     * decrypts data with Secret
     *
     * @param cipherText encrypted data
     * @param secretKey  secret
     * @return decrypted data as String
     * @throws StorageClientException when parameters validation fails
     * @throws StorageCryptoException when decryption fails
     */
    String decrypt(String cipherText, SecretKey secretKey) 
            throws StorageClientException, StorageCryptoException;

    /**
     * version of encryption algorithm as String
     *
     * @return version
     */
    String getVersion();

    /**
     * only one CustomCrypto can be current. This parameter 
     * used only during {@link com.incountry.residence.sdk.Storage}
     * initialisation. Changing this parameter will be ignored after initialization
     *
     * @return is current or not
     */
    boolean isCurrent();
}
```

---
**NOTE**

You should provide a specific `SecretKey` via `SecretsData` passed to `SecretKeyAccessor`. This secret should have flag `isForCustomEncryption` set to `true` and flag `isKey` set to `false`:
```java
public class SecretKey {
    /**
     * @param secret secret/key
     * @param version secret version, should be a non-negative integer
     * @param isKey should be True only for user-defined encryption keys
     * @param isForCustomEncryption should be True for using this key in custom encryption 
     *                              implementations. Either ({@link #isKey} or 
     *                              {@link #isForCustomEncryption}) can be True at the same 
     *                              moment, not both
     * @throws StorageClientException when parameter validation fails
     */
    public SecretKey(String secret, int version, boolean isKey, boolean isForCustomEncryption)
              throws StorageClientException {...}
    //...
}
```

You can set `isForCustomEncryption` using `SecretsData` JSON format as well:
```javascript
secrets_data = {
  "secrets": [{
       "secret": "<secret for custom encryption>",
       "version": 1,
       "isForCustomEncryption": true,
    }
  }],
  "currentVersion": 1,
}
```
---

`version` attribute is used to differ one custom encryption from another and from the default encryption as well.
This way SDK will be able to successfully decrypt any old data if encryption changes with time.

`isCurrent` attribute allows to specify one of the custom encryption implementations that will be used for encryption. Only one implementation can be set as `isCurrent() == true`.

If none of the configurations have `isCurrent() == true` then the SDK will use default encryption to encrypt stored data. At the same time it will keep the ability to decrypt old data, encrypted with custom encryption (if any).

Here's an example of how you can set up SDK to use custom encryption (using Fernet encryption from https://github.com/l0s/fernet-java8 )

```java
/**
 * Example of custom implementation of {@link Crypto} using Fernet algorithm
 */
public class FernetCrypto implements Crypto {
    private static final String VERSION = "fernet custom encryption";
    private boolean current;
    private Validator<String> validator;

    public FernetCrypto(boolean current) {
        this.current = current;
        this.validator = new StringValidator() {
        };
    }

    @Override
    public String encrypt(String text, SecretKey secretKey)
            throws StorageCryptoException {
        try {
            Key key = new Key(secretKey.getSecret());
            Token result = Token.generate(key, text);
            return result.serialise();
        } catch (IllegalStateException ex) {
            throw new StorageCryptoException("Encryption error", ex);
        }
    }

    @Override
    public String decrypt(String cipherText, SecretKey secretKey) 
            throws StorageCryptoException {
        try {
            Key key = new Key(secretKey.getSecret());
            Token result = Token.fromString(cipherText);
            return result.validateAndDecrypt(key, validator);
        } catch (PayloadValidationException ex) {
            throw new StorageCryptoException("Decryption error", ex);
        }
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public boolean isCurrent() {
        return current;
    }
}
```

Project dependencies
-----

The following is a list of compile dependencies for this project. These dependencies are required to compile and run the application:

| **GroupId**              | **ArtifactId**       | **Version** | **Type** |
| :---:                    | :---:                | :---:       | :---:    |
| javax.xml.bind           | jaxb-api             | 2.3.1       | jar      |
| javax.activation         | javax.activation-api | 1.14        | jar      |
| commons-codec            | commons-codec        | 1.14        | jar      |
| org.apache.logging.log4j | log4j-api            | 2.13.2      | jar      |
| org.apache.logging.log4j | log4j-core           | 2.13.2      | jar      |
| com.google.code.gson     | gson                 | 2.8.6       | jar      |

#### Dependency Tree
```
compileClasspath
+--- javax.xml.bind:jaxb-api:2.3.1
|    \--- javax.activation:javax.activation-api:1.2.0
+--- commons-codec:commons-codec:1.14
+--- org.apache.logging.log4j:log4j-api:2.13.2
+--- org.apache.logging.log4j:log4j-core:2.13.2
|    \--- org.apache.logging.log4j:log4j-api:2.13.2
\--- com.google.code.gson:gson:2.8.6
```

### Minimal JVM memory options
```
-Xms8m
-Xmx16m
-XX:MaxMetaspaceSize=32m
```
