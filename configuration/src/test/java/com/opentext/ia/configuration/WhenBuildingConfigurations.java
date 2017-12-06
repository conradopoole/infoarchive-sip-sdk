/*
 * Copyright (c) 2016-2017 by OpenText Corporation. All Rights Reserved.
 */
package com.opentext.ia.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.opentext.ia.configuration.JsonConfigurationProducer.JsonConfiguration;


public class WhenBuildingConfigurations {

  private static final Pattern NAME_PATTERN = Pattern.compile("[a-z]{1,3}(?<uuid>.*)");
  private static final String NAME = "name";
  private static final String TYPE = "type";
  private static final String DESCRIPTION = "description";
  private static final String DEFAULT_TENANT_NAME = "INFOARCHIVE";
  private static final String TENANT_NAME = "myTenant";
  private static final String APPLICATION = "application";
  private static final String APPLICATION_NAME = "myApplication";
  private static final String SEARCH_NAME = "mySearch";
  private static final String DESCRIPTIVE_TEXT = "myDescription";
  private static final String CATEGORY = "myCategory";
  private static final String STATE = "state";
  private static final String DRAFT = "DRAFT";
  private static final String FILE_SYSTEM_ROOT_NAME = "myFileSystemRoot";
  private static final String PATH = "path";
  private static final String SOME_PATH = "/path/to/some/place";
  private static final String SPACE_NAME = "mySpace";
  private static final String SPACE_ROOT_XDB_LIBRARY_NAME = "mySpaceRootXdbLibrary";
  private static final String XDB_LIBRARY_NAME = "myXdbLibrary";
  private static final String SUB_PATH = "my/sub/path";
  private static final String PDI_SCHEMA_NAME = "myPdiSchema";
  private static final String PDI_SCHEMA_FORMAT = "rnc";
  private static final String PDI_SCHEMA_TEXT
      = "element addressBook {\n"
      + "  element card {\n"
      + "    element name { text },\n"
      + "    element email { text }\n"
      + "  }*\n"
      + "}";
  private static final String SPACE_ROOT_FOLDER_NAME = "mySpaceRootFolder";
  private static final String HOLDING_NAME = "myHolding";
  private static final String XDB_MODE = "xdbMode";
  private static final String DEFAULT_XDB_MODE = "PRIVATE";
  private static final String CRYPTO_OBJECT_NAME = "myCryptoObject";
  private static final String XDB_FEDERATION_NAME = "myXdbFederation";

  @Rule
  public ExpectedException thrown = ExpectedException.none();
  private final ConfigurationProducer<JsonConfiguration> producer = new JsonConfigurationProducer();
  private final ConfigurationBuilder<JsonConfiguration> builder = new ConfigurationBuilder<>(producer);
  private Configuration<ConfigurationObject> configuration;

  @Test
  public void shouldUseDefaultPropertiesForTenant() {
    configuration = builder.withTenant().build();

    assertEquals(NAME, DEFAULT_TENANT_NAME, configuration.getTenant().getProperties().getString(NAME));
  }

  @Test
  public void shouldSetPropertiesOfTenant() {
    configuration = builder.withTenant()
        .named(TENANT_NAME)
    .build();

    assertEquals(NAME, TENANT_NAME, nameOf(configuration.getTenant()));
  }

  private String nameOf(ConfigurationObject object) {
    return object.getProperties().getString(NAME);
  }

  @Test
  public void shouldUseDefaultPropertiesForApplication() {
    configuration = builder.withApplication().build();
    ConfigurationObject application = configuration.getApplication();

    assertRandomName(application);
    assertProperties(application,
        "tenant", DEFAULT_TENANT_NAME,
        TYPE, "ACTIVE_ARCHIVING",
        "archiveType", "SIP",
        STATE, "IN_TEST",
        "retentionEnabled", "true");
  }

  private void assertRandomName(ConfigurationObject actual) {
    Matcher matcher = NAME_PATTERN.matcher(nameOf(actual));
    if (matcher.matches()) {
      assertUuid(NAME, matcher.group("uuid"));
    } else {
      fail("Not a random name");
    }
  }

  private void assertUuid(String message, String actual) {
    try {
      UUID.fromString(actual);
    } catch (IllegalArgumentException e) {
      fail(message + " is not a UUID");
    }
  }

  private void assertProperties(ConfigurationObject actual, String... expectedPropertyValues) {
    JSONObject properties = actual.getProperties();
    for (int i = 0; i < expectedPropertyValues.length; i += 2) {
      String property = expectedPropertyValues[i];
      assertEquals(property, expectedPropertyValues[i + 1], properties.optString(property));
    }
  }

  @Test
  public void shouldSetPropertiesOfApplication() {
    configuration = builder.withTenant()
        .named(TENANT_NAME)
        .withApplication()
            .named(APPLICATION_NAME)
            .forAppDecom()
            .forTables()
            .activated()
            .withDescription(DESCRIPTIVE_TEXT)
            .withCategory(CATEGORY)
    .build();

    ConfigurationObject application = configuration.getApplication();
    assertProperties(application,
        "tenant", TENANT_NAME,
        NAME, APPLICATION_NAME,
        TYPE, "APP_DECOMM",
        "archiveType", "TABLE",
        STATE, "ACTIVE",
        DESCRIPTION, DESCRIPTIVE_TEXT,
        "category", CATEGORY);
  }

  @Test
  public void shouldThrowExceptionWhenAskedForMissingItem() {
    thrown.expect(IllegalArgumentException.class);

    builder.withTenant().build().getApplication();
  }

  @Test
  public void shouldUseDefaultPropertiesForSearch() {
    configuration = builder.withSearch().build();
    ConfigurationObject search = configuration.getSearch();

    assertRandomName(search);
    assertProperties(search,
        STATE, DRAFT);
  }

  @Test
  public void shouldSetPropertiesForSearch() {
    configuration = builder.withApplication()
        .named(APPLICATION_NAME)
        .withSearch()
            .named(SEARCH_NAME)
            .withDescription(DESCRIPTIVE_TEXT)
            .published()
    .build();
    ConfigurationObject search = configuration.getSearch();

    assertProperties(search,
        APPLICATION, APPLICATION_NAME,
        NAME, SEARCH_NAME,
        STATE, "PUBLISHED",
        DESCRIPTION, DESCRIPTIVE_TEXT);
  }

  @Test
  public void shouldUseDefaultPropertiesForFileSystemRoot() {
    configuration = builder.withFileSystemRoot().build();
    ConfigurationObject fileSystemRoot = configuration.getFileSystemRoot();

    assertRandomName(fileSystemRoot);
    assertProperties(fileSystemRoot,
        PATH, "/data/root",
        TYPE, "FILESYSTEM");
  }

  @Test
  public void shouldSetPropertiesForFileSystemRoot() {
    configuration = builder.withFileSystemRoot()
        .named(FILE_SYSTEM_ROOT_NAME)
        .withDescription(DESCRIPTIVE_TEXT)
        .at(SOME_PATH)
        .onIsilon()
    .build();
    ConfigurationObject fileSystemRoot = configuration.getFileSystemRoot();

    assertProperties(fileSystemRoot,
        NAME, FILE_SYSTEM_ROOT_NAME,
        DESCRIPTION, DESCRIPTIVE_TEXT,
        PATH, SOME_PATH,
        TYPE, "ISILON");
  }

  @Test
  public void shouldUseDefaultPropertiesForSpace() {
    configuration = builder.withSpace().build();
    ConfigurationObject space = configuration.getSpace();

    assertRandomName(space);
  }

  @Test
  public void shouldSetPropertiesForSpace() {
    configuration = builder.withApplication()
        .named(APPLICATION_NAME)
        .withSpace()
            .named(SPACE_NAME)
    .build();
    ConfigurationObject space = configuration.getSpace();

    assertProperties(space,
        APPLICATION, APPLICATION_NAME,
        NAME, SPACE_NAME);
  }

  @Test
  public void shouldUseDefaultPropertiesForSpaceRootFolder() {
    configuration = builder
        .withFileSystemRoot()
            .named(FILE_SYSTEM_ROOT_NAME)
        .end()
        .withSpace()
            .withSpaceRootFolder(FILE_SYSTEM_ROOT_NAME)
    .build();
    ConfigurationObject spaceRootFolder = configuration.getSpaceRootFolder(configuration.getSpace());

    assertRandomName(spaceRootFolder);
    assertProperties(spaceRootFolder, "fileSystemRoot", FILE_SYSTEM_ROOT_NAME);
  }

  @Test
  public void shouldSetPropertiesForSpaceRootFolder() {
    configuration = builder
        .withFileSystemRoot()
            .named(FILE_SYSTEM_ROOT_NAME)
        .end()
        .withSpace()
            .named(SPACE_NAME)
            .withSpaceRootFolder(FILE_SYSTEM_ROOT_NAME)
                .named(SPACE_ROOT_FOLDER_NAME)
    .build();
    ConfigurationObject spaceRootFolder = configuration.getSpaceRootFolder(configuration.getSpace());

    assertProperties(spaceRootFolder,
        "space", SPACE_NAME,
        NAME, SPACE_ROOT_FOLDER_NAME);
  }

  @Test
  public void shouldUseDefaultPropertiesForSpaceRootXdbLibrary() {
    configuration = builder.withSpace()
        .withSpaceRootXdbLibrary()
    .build();
    ConfigurationObject spaceRootXdbLibrary = configuration.getSpaceRootXdbLibrary(configuration.getSpace());

    assertRandomName(spaceRootXdbLibrary);
  }

  @Test
  public void shouldSetPropertiesForSpaceRootXdbLibrary() {
    configuration = builder.withSpace()
        .named(SPACE_NAME)
        .withSpaceRootXdbLibrary()
            .named(SPACE_ROOT_XDB_LIBRARY_NAME)
    .build();
    ConfigurationObject spaceRootXdbLibrary = configuration.getSpaceRootXdbLibrary(configuration.getSpace());

    assertProperties(spaceRootXdbLibrary,
        "space", SPACE_NAME,
        NAME, SPACE_ROOT_XDB_LIBRARY_NAME);
  }

  @Test
  public void shouldUseDefaultPropertiesForXdbLibrary() {
    configuration = builder.withSpace()
        .withSpaceRootXdbLibrary()
            .withXdbLibrary()
    .build();
    ConfigurationObject xdbLibrary = configuration.getXdbLibrary(configuration.getSpaceRootXdbLibrary(
        configuration.getSpace()));

    assertRandomName(xdbLibrary);
    assertProperties(xdbLibrary,
        TYPE, "DATA",
        XDB_MODE, DEFAULT_XDB_MODE);
  }

  @Test
  public void shouldSetPropertiesForXdbLibrary() {
    configuration = builder.withSpace()
        .named(SPACE_NAME)
        .withSpaceRootXdbLibrary()
            .named(SPACE_ROOT_XDB_LIBRARY_NAME)
            .withXdbLibrary()
                .named(XDB_LIBRARY_NAME)
                .storingSearchResults()
                .at(SUB_PATH)
                .inAggregate()
    .build();
    ConfigurationObject xdbLibrary = configuration.getXdbLibrary(configuration.getSpaceRootXdbLibrary(
        configuration.getSpace()));

    assertProperties(xdbLibrary,
        "parentSpaceRootXdbLibrary", SPACE_ROOT_XDB_LIBRARY_NAME,
        NAME, XDB_LIBRARY_NAME,
        TYPE, "SEARCH_RESULTS",
        "subPath", SUB_PATH,
        XDB_MODE, "AGGREGATE");
  }

  @Test
  public void shouldUseDefaultPropertiesForPdiSchema() {
    configuration = builder.withApplication()
        .withPdiSchema()
    .build();
    ConfigurationObject pdiSchema = configuration.getPdiSchema(configuration.getApplication());

    assertRandomName(pdiSchema);
    assertProperties(pdiSchema,
        "format", "xsd");
  }

  @Test
  public void shouldSetPropertiesForPdiSchema() {
    configuration = builder.withApplication()
        .named(APPLICATION_NAME)
        .withPdiSchema()
            .named(PDI_SCHEMA_NAME)
            .ofType(PDI_SCHEMA_FORMAT)
            .as(PDI_SCHEMA_TEXT)
    .build();
    ConfigurationObject pdiSchema = configuration.getPdiSchema(configuration.getApplication());

    assertProperties(pdiSchema,
        APPLICATION, APPLICATION_NAME,
        NAME, PDI_SCHEMA_NAME,
        "format", PDI_SCHEMA_FORMAT,
        "content", PDI_SCHEMA_TEXT);
  }

  @Test
  public void shouldUseDefaultPropertiesForHolding() {
    configuration = builder.withHolding().build();
    ConfigurationObject holding = configuration.getHolding();

    assertRandomName(holding);
    assertProperties(holding, XDB_MODE, DEFAULT_XDB_MODE);
  }

  @Test
  public void shouldSetPropertiesForHolding() {
    configuration = builder.withApplication()
        .named(APPLICATION_NAME)
        .withHolding()
            .named(HOLDING_NAME)
            .inPool()
    .build();
    ConfigurationObject holding = configuration.getHolding();

    assertProperties(holding,
        APPLICATION, APPLICATION_NAME,
        NAME, HOLDING_NAME,
        XDB_MODE, "POOLED");
  }

  @Test
  public void shouldUseDefaultPropertiesForCryptoObject() {
    configuration = builder.withCryptoObject().build();
    ConfigurationObject cryptoObject = configuration.getCryptoObject();

    assertRandomName(cryptoObject);
    assertProperties(cryptoObject,
        "securityProvider", "Bouncy Castle",
        "keySize", "256",
        "encryptionMode", "CBC",
        "paddingScheme", "PKCS5PADDING",
        "encryptionAlgorithm", "AES");
  }

  @Test
  public void shouldSetPropertiesForCryptoObject() {
    configuration = builder.withCryptoObject()
        .named(CRYPTO_OBJECT_NAME)
        .providedBy("SunJCE")
        .withKeysOfSize(192)
        .combiningBlocksUsing("OFB")
        .paddedBy("RSA/ECB/PKCS1Padding")

    .build();
    ConfigurationObject cryptoObject = configuration.getCryptoObject();

    assertProperties(cryptoObject,
        NAME, CRYPTO_OBJECT_NAME,
        "securityProvider", "SunJCE",
        "keySize", "192",
        "encryptionMode", "OFB",
        "paddingScheme", "RSA/ECB/PKCS1Padding");
  }

  @Test
  public void shouldUseDefaultPropertiesForXdbFederation() {
    configuration = builder.withXdbFederation().build();
    ConfigurationObject xdbFederation = configuration.getXdbFederation();

    assertRandomName(xdbFederation);
    assertProperties(xdbFederation,
        "bootstrap", "xhive://127.0.0.1:2910",
        "superUserPassword", "test");
  }

  @Test
  public void shouldSetPropertiesForXdbFederation() {
    configuration = builder.withXdbFederation()
        .named(XDB_FEDERATION_NAME)
        .runningAt("xhives://xdb.com:2345")
        .protectedWithPassword("secret")
    .build();
    ConfigurationObject xdbFederation = configuration.getXdbFederation();

    assertProperties(xdbFederation,
        NAME, XDB_FEDERATION_NAME,
        "bootstrap", "xhives://xdb.com:2345",
        "superUserPassword", "secret");
  }

  @Test
  public void shouldSetCryptoObject() {
    configuration = builder
        .withCryptoObject()
            .named(CRYPTO_OBJECT_NAME)
        .end()
        .withXdbFederation()
            .encryptedBy(CRYPTO_OBJECT_NAME)
        .end()
    .build();
    ConfigurationObject xdbFederation = configuration.getXdbFederation();

    assertProperties(xdbFederation, "cryptoObject", CRYPTO_OBJECT_NAME);
  }

  @Test
  public void shouldUseDefaultPropertiesForXdbDatabase() {
    configuration = builder.withXdbDatabase().build();
    ConfigurationObject xdbDatabase = configuration.getXdbDatabase();

    assertRandomName(xdbDatabase);
    assertProperties(xdbDatabase,
        "adminPassword", "secret");
  }

  @Test
  public void shouldSetPropertiesForXdbDatabase() {
    configuration = builder.withXdbDatabase()
        .named("myXdbDatabase")
        .protectedWithPassword("test")
    .build();
    ConfigurationObject xdbDatabase = configuration.getXdbDatabase();

    assertProperties(xdbDatabase,
        NAME, "myXdbDatabase",
        "adminPassword", "test");
  }

}
