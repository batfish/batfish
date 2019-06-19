package org.batfish.coordinator.authorizer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.util.CommonUtil;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.config.Settings;

/** An {@link Authorizer} backed by an SQL database */
@ParametersAreNonnullByDefault
public class DbAuthorizer implements Authorizer {

  @VisibleForTesting static final String COLUMN_APIKEY = "ApiKey";
  private static final String COLUMN_CONTAINER_NAME = "ContainerName";
  private static final String COLUMN_DATE_CREATED = "DateCreated";
  private static final String COLUMN_DATE_LAST_ACCESSED = "DateLastAccessed";

  private static final int DB_VALID_CHECK_TIMEOUT_SECS = 3;
  private static final int MAX_DB_TRIES = 3;

  @VisibleForTesting static final String TABLE_CONTAINERS = "batfish_containers";
  @VisibleForTesting static final String TABLE_PERMISSIONS = "batfish_containerpermissions";
  @VisibleForTesting static final String TABLE_USERS = "batfish_ui_server_userprofile";

  private final Cache<String, Boolean> _cacheApiKeys;
  private final Cache<String, Boolean> _cachePermissions;

  private Connection _dbConn;
  private BatfishLogger _logger;
  private String _connString;
  private String _driverClassName;

  /**
   * Create a new database authorizer.
   *
   * @param connectionString JDBC string to use for connection to the database
   * @param driverClass specific class name to load as the JDBC driver
   * @param cacheTimeout time after which cache entries should be invalidated, in milliseconds
   */
  DbAuthorizer(String connectionString, @Nullable String driverClass, long cacheTimeout) {
    _logger = Main.getLogger();
    _cacheApiKeys =
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterWrite(cacheTimeout, TimeUnit.MILLISECONDS)
            .build();
    _cachePermissions =
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterWrite(cacheTimeout, TimeUnit.MILLISECONDS)
            .build();
    _connString = connectionString;
    _driverClassName = driverClass;
    openDbConnection();
  }

  /**
   * Create a new database authorizer from coordinator settings
   *
   * @param settings coordinator settings
   * @return a new authorizer
   */
  public static DbAuthorizer createFromSettings(Settings settings) {
    return new DbAuthorizer(
        settings.getDbAuthorizerConnString(),
        settings.getDriverClass(),
        settings.getDbAuthorizerCacheExpiryMs());
  }

  @Override
  public void authorizeContainer(String apiKey, String containerName) {
    _logger.infof("Authorizing %s for %s\n", apiKey, containerName);

    // add the container if it does not exist; update datelastaccessed if it
    // does
    java.sql.Date now = new java.sql.Date(new Date().getTime());
    String insertContainersString =
        String.format(
            "REPLACE INTO %s (%s, %s, %s) VALUES (?, ?, ?)",
            TABLE_CONTAINERS,
            COLUMN_CONTAINER_NAME,
            COLUMN_DATE_CREATED,
            COLUMN_DATE_LAST_ACCESSED);
    int numInsertRows;
    try (PreparedStatement insertContainers = _dbConn.prepareStatement(insertContainersString)) {
      insertContainers.setString(1, containerName);
      insertContainers.setDate(2, now);
      insertContainers.setDate(3, now);
      numInsertRows = executeUpdate(insertContainers);
    } catch (SQLException e) {
      throw new BatfishException("Could not update containers table", e);
    }

    // MySQL says 2 rows impacted when an old row is updated; otherwise it
    // says 1
    if (numInsertRows == 1) {
      _logger.infof("New container added\n");
    }

    // return if already accessible
    if (isAccessibleNetwork(apiKey, containerName, false)) {
      return;
    }
    int numRows;
    String insertPermissionsString =
        String.format(
            "INSERT INTO %s (%s, %s) VALUES (?, ?)",
            TABLE_PERMISSIONS, COLUMN_APIKEY, COLUMN_CONTAINER_NAME);
    try (PreparedStatement insertPermissions = _dbConn.prepareStatement(insertPermissionsString)) {
      insertPermissions.setString(1, apiKey);
      insertPermissions.setString(2, containerName);
      numRows = executeUpdate(insertPermissions);
    } catch (SQLException e) {
      throw new BatfishException("Could not update permissions table", e);
    }
    if (numRows > 0) {
      String cacheKey = getPermsCacheKey(apiKey, containerName);
      _cachePermissions.put(cacheKey, Boolean.TRUE);
      _logger.infof("Authorization successful\n");
    }
  }

  @Nullable
  private synchronized ResultSet executeQuery(PreparedStatement query) {
    int triesLeft = MAX_DB_TRIES;
    while (triesLeft > 0) {
      triesLeft--;
      try {
        _logger.debugf("Executing SQL query: %s\n", query);
        return query.executeQuery();
      } catch (SQLException e) {
        _logger.errorf("SQLException while executing query '%s': %s", query, e.getMessage());
        _logger.errorf("Tries left = %d\n", triesLeft);

        if (triesLeft > 0) {
          try {
            if (!_dbConn.isValid(DB_VALID_CHECK_TIMEOUT_SECS)) {
              openDbConnection();
            }
          } catch (SQLException e1) {
            return null;
          }
        }
      } catch (Exception e) {
        throw new BatfishException("Non-SQL-related exception occurred when executing query");
      }
    }

    return null;
  }

  private synchronized int executeUpdate(PreparedStatement update) {
    int triesLeft = MAX_DB_TRIES;
    String updateString = update.toString();
    while (triesLeft > 0) {
      triesLeft--;
      try {
        _logger.debugf("Executing SQL update: %s\n", updateString);
        return update.executeUpdate();
      } catch (SQLException e) {
        _logger.errorf("SQLException while executing query '%s': %s", updateString, e.getMessage());
        _logger.errorf("Tries left = %d\n", triesLeft);

        if (triesLeft > 0) {
          try {
            if (!_dbConn.isValid(DB_VALID_CHECK_TIMEOUT_SECS)) {
              openDbConnection();
            }
          } catch (SQLException e1) {
            return 0;
          }
        }
      }
    }
    return 0;
  }

  private static String getPermsCacheKey(String apiKey, String containerName) {
    return apiKey + "::" + containerName;
  }

  @Override
  public boolean isAccessibleNetwork(String apiKey, String containerName, boolean logError) {

    String cacheKey = getPermsCacheKey(apiKey, containerName);
    if (_cachePermissions.getIfPresent(cacheKey) != null) {
      return true;
    }

    String selectPermittedContainerString =
        String.format(
            "SELECT COUNT(*) FROM %s WHERE %s = ? AND %s = ?",
            TABLE_PERMISSIONS, COLUMN_APIKEY, COLUMN_CONTAINER_NAME);
    boolean authorized = false;
    try (PreparedStatement ps = _dbConn.prepareStatement(selectPermittedContainerString)) {
      ps.setString(1, apiKey);
      ps.setString(2, containerName);
      try (ResultSet rs = executeQuery(ps)) {
        if (rs != null) {
          rs.next();
          authorized = rs.getInt(1) == 1;
        }
      }
    } catch (SQLException e) {
      throw new BatfishException("Could not query permissions table successfully", e);
    }

    if (authorized) {
      _cachePermissions.put(cacheKey, Boolean.TRUE);

      // a valid access was made; update datelastaccessed
      java.sql.Date now = new java.sql.Date(new Date().getTime());
      String updatePsString =
          String.format(
              "UPDATE %s SET %s=? WHERE %s=?",
              TABLE_CONTAINERS, COLUMN_DATE_LAST_ACCESSED, COLUMN_CONTAINER_NAME);
      try (PreparedStatement updatePs = _dbConn.prepareStatement(updatePsString)) {

        updatePs.setDate(1, now);
        updatePs.setString(2, containerName);
        executeUpdate(updatePs);
      } catch (SQLException e) {
        throw new BatfishException("Could not update containers table", e);
      }
      return true;
    }

    if (logError) {
      _logger.infof("Authorizer: %s is NOT allowed to access %s\n", apiKey, containerName);
    }

    return false;
  }

  @Override
  public boolean isValidWorkApiKey(String apiKey) {
    if (_cacheApiKeys.getIfPresent(apiKey) != null) {
      return true;
    }
    String selectApiKeyRowString =
        String.format("SELECT COUNT(*) FROM %s WHERE %s = ?", TABLE_USERS, COLUMN_APIKEY);
    boolean authorized;
    try (PreparedStatement ps = _dbConn.prepareStatement(selectApiKeyRowString)) {
      ps.setString(1, apiKey);
      try (ResultSet rs = executeQuery(ps)) {
        if (rs == null) {
          return false;
        }
        rs.next();
        authorized = rs.getInt(1) == 1;
      }
    } catch (SQLException e) {
      throw new BatfishException("Could not query users table", e);
    }
    if (authorized) {
      _cacheApiKeys.put(apiKey, Boolean.TRUE);
      return true;
    }
    _logger.infof("Authorizer: %s is NOT a valid key\n", apiKey);
    return false;
  }

  private synchronized void openDbConnection() {
    int triesLeft = MAX_DB_TRIES;

    while (triesLeft > 0) {
      triesLeft--;
      try {
        // Gracefully close previous connection if it exists (but could be invalid)
        if (_dbConn != null) {
          _dbConn.close();
          _dbConn = null;
        }

        // Load a specific driver class, if one was specified
        if (_driverClassName != null) {
          ClassLoader cl = Thread.currentThread().getContextClassLoader();
          cl.loadClass(_driverClassName);
          Class.forName(_driverClassName, true, cl);
        }

        // Open a new connection
        _dbConn = DriverManager.getConnection(_connString);
        return;
      } catch (SQLException e) {
        if (CommonUtil.causedByMessage(e, "Access denied for user")
            || CommonUtil.causedByMessage(e, "No suitable driver found")
            || CommonUtil.causedByMessage(e, "Unknown database")
            || CommonUtil.causedBy(e, UnknownHostException.class)
            || CommonUtil.causedBy(e, ConnectException.class)) {
          throw new BatfishException(
              "Unrecoverable SQLException loading JDBC driver: " + _driverClassName, e);
        }
        if (triesLeft == 0) {
          throw new BatfishException("No tries left loading JDBC driver: " + _driverClassName);
        }
        _logger.errorf(
            "SQLException while opening Db connection: %s\n", Throwables.getStackTraceAsString(e));
        _logger.errorf("Tries left = %d\n", triesLeft);

      } catch (ClassNotFoundException e) {
        throw new BatfishException("JDBC driver class not found: " + _driverClassName, e);
      }
    }
  }

  @Override
  public String toString() {
    return DbAuthorizer.class.getSimpleName();
  }
}
