package org.batfish.coordinator.authorizer;

import static org.batfish.coordinator.authorizer.DbAuthorizer.COLUMN_APIKEY;
import static org.batfish.coordinator.authorizer.DbAuthorizer.TABLE_CONTAINERS;
import static org.batfish.coordinator.authorizer.DbAuthorizer.TABLE_PERMISSIONS;
import static org.batfish.coordinator.authorizer.DbAuthorizer.TABLE_USERS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.batfish.common.BatfishLogger;
import org.batfish.coordinator.Main;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests of {@link DbAuthorizer} */
public class DbAuthorizerTest {
  /*
   *  Not using in-memory databases each new connection creates a brand new database.
   *  Instead, using slightly more persistent DB inside a temporary folder.
   */
  @Rule public TemporaryFolder tmpFolder = new TemporaryFolder();
  private static final String DB_NAME = "authorizer_test.sqlite";
  private Authorizer _authorizer;
  private BatfishLogger _logger = new BatfishLogger("fatal", false);

  private static final String KEY1 = "key1";
  private static final String KEY2 = "key2";
  private static final String KEY3 = "key3";

  @Before
  public void setupTestDB() throws SQLException {
    // Setup a test database using SQLite
    String connString =
        String.format("jdbc:sqlite:%s", Paths.get(tmpFolder.getRoot().getAbsolutePath(), DB_NAME));
    try (Connection conn = DriverManager.getConnection(connString)) {
      try (PreparedStatement ps =
          conn.prepareStatement(
              String.format(
                  "CREATE TABLE %s (`Memberid` int PRIMARY KEY,"
                      + "  `Username` varchar(255) NOT NULL DEFAULT '',"
                      + "  `ApiKey` varchar(64) DEFAULT '', "
                      + "  `Endpoint` varchar(255) DEFAULT 'test.service.intentionet.com')",
                  TABLE_USERS))) {
        ps.execute();
      }
      try (PreparedStatement ps =
          conn.prepareStatement(
              String.format(
                  "CREATE TABLE %s (`ContainerName` varchar(255) PRIMARY KEY,"
                      + "  `DateCreated` timestamp NULL DEFAULT NULL,"
                      + "  `DateLastAccessed` timestamp NULL DEFAULT NULL)",
                  TABLE_CONTAINERS))) {
        ps.execute();
      }
      try (PreparedStatement ps =
          conn.prepareStatement(
              String.format(
                  "CREATE TABLE %s ("
                      + "  `ComboId` int PRIMARY KEY,"
                      + "  `ContainerName` varchar(255) NOT NULL UNIQUE,"
                      + "  `ApiKey` varchar(64) DEFAULT '',"
                      + "  `DateCreated` timestamp NULL DEFAULT NULL,"
                      + "  `DateLastAccessed` timestamp NULL DEFAULT NULL)",
                  TABLE_PERMISSIONS))) {
        ps.execute();
      }

      try (PreparedStatement st =
          conn.prepareStatement(
              String.format(
                  "INSERT INTO %s ('Username', %s) VALUES (?, ?)", TABLE_USERS, COLUMN_APIKEY))) {
        st.setString(1, "test_user_1");
        st.setString(2, KEY1);
        st.execute();
        st.setString(1, "test_user_2");
        st.setString(2, KEY2);
        st.execute();
      }
    }

    // Set logger, otherwise exceptions are thrown
    Main.setLogger(_logger);

    // Effectively disable the cache
    _authorizer = new DbAuthorizer(connString, null, 0);
  }

  @Test
  public void testValidKey() {
    // Key 1 is valid
    assertThat(_authorizer.isValidWorkApiKey(KEY1), is(true));
    // Key 3 is not in the DB
    assertThat(_authorizer.isValidWorkApiKey(KEY3), is(false));
  }

  @Test
  public void testAuthorizeContainer() {
    String contName = "test_container";
    _authorizer.authorizeContainer(KEY1, contName);

    // Test only KEY1 can access container
    assertThat(_authorizer.isAccessibleNetwork(KEY1, contName, false), is(true));
    assertThat(_authorizer.isAccessibleNetwork(KEY2, contName, false), is(false));
    assertThat(_authorizer.isAccessibleNetwork(KEY3, contName, false), is(false));

    // Test only the right container accessible by key1
    assertThat(_authorizer.isAccessibleNetwork(KEY1, "gibberish", false), is(false));

    // Test for case sensitivity as well
    assertThat(_authorizer.isAccessibleNetwork(KEY1.toUpperCase(), contName, false), is(false));
    assertThat(_authorizer.isAccessibleNetwork(KEY1, contName.toUpperCase(), false), is(false));
  }
}
