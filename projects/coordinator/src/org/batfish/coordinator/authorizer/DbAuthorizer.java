package org.batfish.coordinator.authorizer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishLogger;
import org.batfish.coordinator.Main;

//An authorizer that is backed by a file
//Useful for testing
public class DbAuthorizer implements Authorizer {

   private static final String APIKEY_COLUMN = "apikey";
   private static final String CONTAINER_COLUMN = "containername";
   private static final int DB_VALID_CHECK_TIMEOUT_SECS = 3;
   private static final int MAX_DB_TRIES = 3;

   private static final String PERMS_TABLE = "Permissions";
   private static final String USERS_TABLE = "ApiKeys";

   private Map<String, Date> _cacheApiKeys = new HashMap<String, Date>();
   private Map<String, Date> _cachePermissions = new HashMap<String, Date>();

   private Connection _dbConn;
   private BatfishLogger _logger;

   public DbAuthorizer() throws SQLException {
      _logger = Main.getLogger();
      openDbConnection();
   }

   @Override
   public void authorizeContainer(String apiKey, String containerName)
         throws Exception {

      _logger.infof("Authorizing %s for %s\n", apiKey, containerName);

      if (isAccessibleContainer(apiKey, containerName)) {
         return;
      }

      String query = String.format(
            "INSERT INTO %s (%s, %s) VALUES ('%s', '%s')", PERMS_TABLE,
            APIKEY_COLUMN, CONTAINER_COLUMN, apiKey, containerName);

      int numRows = executeUpdate(query);

      if (numRows > 0) {
         String cacheKey = getPermsCacheKey(apiKey, containerName);
         insertInCache(_cachePermissions, cacheKey);
         _logger.infof("Authorization successful\n");
      }

   }

   private synchronized ResultSet executeQuery(String query) {
      int triesLeft = MAX_DB_TRIES;

      while (triesLeft > 0) {
         triesLeft--;
         try {
            Statement stmt = _dbConn.createStatement();
            return stmt.executeQuery(query);
         }
         catch (SQLException e) {
            _logger.errorf("SQLException while executing query '%s': %s",
                  query, e.getMessage());
            _logger.errorf("Tries left = %d\n", triesLeft);

            if (triesLeft > 0) {
               try {
                  if (!_dbConn.isValid(DB_VALID_CHECK_TIMEOUT_SECS)) {
                     openDbConnection();
                  }
               }
               catch (SQLException e1) {
                  return null;
               }
            }
         }
      }

      return null;
   }

   private synchronized int executeUpdate(String query) {
      int triesLeft = MAX_DB_TRIES;

      while (triesLeft > 0) {
         triesLeft--;
         try {
            Statement stmt = _dbConn.createStatement();
            return stmt.executeUpdate(query);
         }
         catch (SQLException e) {
            _logger.errorf("SQLException while executing query '%s': %s",
                  query, e.getMessage());
            _logger.errorf("Tries left = %d\n", triesLeft);

            if (triesLeft > 0) {
               try {
                  if (!_dbConn.isValid(DB_VALID_CHECK_TIMEOUT_SECS)) {
                     openDbConnection();
                  }
               }
               catch (SQLException e1) {
                  return 0;
               }
            }
         }
      }
      return 0;
   }

   private String getPermsCacheKey(String apiKey, String containerName) {
      return apiKey + "::" + containerName;
   }

   private synchronized void insertInCache(Map<String, Date> cache, String key) {
      cache.put(key, new Date());
   }

   @Override
   public boolean isAccessibleContainer(String apiKey, String containerName)
         throws Exception {

      String cacheKey = getPermsCacheKey(apiKey, containerName);

      boolean validInCache = isValidInCache(_cachePermissions, cacheKey);

      if (validInCache) {
         return true;
      }

      String query = String.format(
            "SELECT * FROM %s WHERE %s = '%s' AND %s = '%s'", PERMS_TABLE,
            APIKEY_COLUMN, apiKey, CONTAINER_COLUMN, containerName);

      ResultSet rs = executeQuery(query);

      if (rs != null && rs.first()) {
         insertInCache(_cachePermissions, cacheKey);
         return true;
      }

      _logger.infof("Authorizer: %s is NOT allowed to access %s\n", apiKey,
            containerName);
      return false;
   }

   private synchronized boolean isValidInCache(Map<String, Date> cache,
         String key) {
      if (cache.containsKey(key)) {
         // check if the entry is expired
         if (new Date().getTime() - cache.get(key).getTime() > Main
               .getSettings().getDbAuthorizerCacheExpiryMs()) {
            cache.remove(key);
            return false;
         }
         return true;
      }
      return false;
   }

   @Override
   public boolean isValidWorkApiKey(String apiKey) throws Exception {

      boolean validInCache = isValidInCache(_cacheApiKeys, apiKey);

      if (validInCache) {
         return true;
      }

      String query = String.format("SELECT * FROM %s WHERE %s = '%s'",
            USERS_TABLE, APIKEY_COLUMN, apiKey);

      ResultSet rs = executeQuery(query);

      if (rs != null && rs.first()) {
         insertInCache(_cacheApiKeys, apiKey);
         return true;
      }

      _logger.infof("Authorizer: %s is NOT a valid key\n", apiKey);
      return false;
   }

   private synchronized void openDbConnection() throws SQLException {
      int triesLeft = MAX_DB_TRIES;

      while (triesLeft > 0) {
         triesLeft--;
         try {

            if (_dbConn != null) {
               _dbConn.close();
            }

            _dbConn = DriverManager.getConnection(Main.getSettings()
                  .getDbAuthorizerConnString());
            return;
         }
         catch (SQLException e) {
            if (e.getMessage().contains("Access denied for user")
                  || e.getMessage().contains("No suitable driver found")
                  || triesLeft == 0) {
               throw e;
            }

            _logger.errorf("SQLException while opening Db connection: %s\n",
                  e.getMessage());
            _logger.errorf("Tries left = %d\n", triesLeft);

         }
      }
   }
}
