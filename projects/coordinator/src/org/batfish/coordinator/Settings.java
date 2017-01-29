package org.batfish.coordinator;

import org.batfish.common.BaseSettings;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.CoordConsts;
import org.batfish.common.util.CommonUtil;
import org.batfish.coordinator.authorizer.Authorizer;
import org.batfish.coordinator.config.ConfigurationLocator;
import org.batfish.coordinator.queues.WorkQueue;

public class Settings extends BaseSettings {

   private static final String ARG_ALLOW_DEFAULT_KEY_LISTINGS = "allowdefaultkeylistings";
   private static final String ARG_AUTHORIZER_TYPE = "authorizertype";
   private static final String ARG_CONTAINERS_LOCATION = "containerslocation";
   private static final String ARG_DB_AUTHORIZER_CACHE_EXPIRY_MS = "dbcacheexpiry";
   private static final String ARG_DB_AUTHORIZER_CONN_STRING = "dbconnection";
   private static final String ARG_DISABLE_SSL = "disablessl";
   private static final String ARG_DRIVER_CLASS = "driverclass";
   /**
    * (the arguments below are not wired to command line)
    */
   private static final String ARG_FILE_AUTHORIZER_PERMS_FILE = "fileauthpermsfile";
   private static final String ARG_FILE_AUTHORIZER_ROOT_DIR = "fileauthrootdir";
   private static final String ARG_FILE_AUTHORIZER_USERS_FILE = "fileauthusersfile";
   private static final String ARG_HELP = "help";
   private static final String ARG_LOG_FILE = "logfile";
   private static final String ARG_LOG_LEVEL = "loglevel";
   private static final String ARG_PERIOD_ASSIGN_WORK_MS = "periodassignworkms";
   private static final String ARG_PERIOD_CHECK_WORK_MS = "periodcheckworkms";
   private static final String ARG_PERIOD_WORKER_STATUS_REFRESH_MS = "periodworkerrefreshms";
   private static final String ARG_QUEUE_COMPLETED_WORK = "qcompletedwork";
   private static final String ARG_QUEUE_INCOMPLETE_WORK = "qincompletework";
   private static final String ARG_QUEUE_TYPE = "qtype";
   private static final String ARG_SERVICE_HOST = "servicehost";

   private static final String ARG_SERVICE_POOL_PORT = "poolport";
   private static final String ARG_SERVICE_WORK_PORT = "workport";
   private static final String ARG_SSL_KEYSTORE_FILE = "sslkeystorefile";

   private static final String ARG_SSL_KEYSTORE_PASSWORD = "sslkeystorepassword";
   /**
    * Need when using Azure queues for storing work items
    */
   private static final String ARG_STORAGE_ACCOUNT_KEY = "storageaccountkey";
   private static final String ARG_STORAGE_ACCOUNT_NAME = "storageaccountname";

   private static final String ARG_STORAGE_PROTOCOL = "storageprotocol";

   private static final String EXECUTABLE_NAME = "coordinator";

   private Authorizer.Type _authorizerType;
   private String _containersLocation;
   private String _dbAuthorizerConnString;
   private long _dbCacheExpiryMs;
   private boolean _defaultKeyListings;
   private String _driverClass;
   private String _logFile;
   private String _logLevel;
   private long _periodAssignWorkMs;
   private long _periodCheckWorkMs;
   private long _periodWorkerStatusRefreshMs;
   private String _queueCompletedWork;
   private WorkQueue.Type _queueType;
   private String _queuIncompleteWork;
   private String _serviceHost;
   private int _servicePoolPort;
   private int _serviceWorkPort;
   private String _sslKeystoreFilename;
   private String _storageAccountKey;
   private String _storageAccountName;
   private String _storageProtocol;
   private boolean _useSsl;

   public Settings(String[] args) throws Exception {
      super(CommonUtil.getConfigProperties(ConfigurationLocator.class,
            BfConsts.RELPATH_CONFIG_FILE_NAME_COORDINATOR));

      initConfigDefaults();

      initOptions();
      parseCommandLine(args);
   }

   public Authorizer.Type getAuthorizationType() {
      return _authorizerType;
   }

   public String getContainersLocation() {
      return _containersLocation;
   }

   public long getDbAuthorizerCacheExpiryMs() {
      return _dbCacheExpiryMs;
   }

   public String getDbAuthorizerConnString() {
      return _dbAuthorizerConnString;
   }

   public boolean getDefaultKeyListings() {
      return _defaultKeyListings;
   }

   public String getDriverClass() {
      return _driverClass;
   }

   public String getFileAuthorizerPermsFile() {
      return _config.getString(ARG_FILE_AUTHORIZER_PERMS_FILE);
   }

   public String getFileAuthorizerRootDir() {
      return _config.getString(ARG_FILE_AUTHORIZER_ROOT_DIR);
   }

   public String getFileAuthorizerUsersFile() {
      return _config.getString(ARG_FILE_AUTHORIZER_USERS_FILE);
   }

   public String getLogFile() {
      return _logFile;
   }

   public String getLogLevel() {
      return _logLevel;
   }

   public long getPeriodAssignWorkMs() {
      return _periodAssignWorkMs;
   }

   public long getPeriodCheckWorkMs() {
      return _periodCheckWorkMs;
   }

   public long getPeriodWorkerStatusRefreshMs() {
      return _periodWorkerStatusRefreshMs;
   }

   public String getQueueCompletedWork() {
      return _queueCompletedWork;
   }

   public String getQueueIncompleteWork() {
      return _queuIncompleteWork;
   }

   public WorkQueue.Type getQueueType() {
      return _queueType;
   }

   public String getServiceHost() {
      return _serviceHost;
   }

   public int getServicePoolPort() {
      return _servicePoolPort;
   }

   public int getServiceWorkPort() {
      return _serviceWorkPort;
   }

   public String getSslKeystoreFilename() {
      return _sslKeystoreFilename;
   }

   public String getSslKeystorePassword() {
      return _config.getString(ARG_SSL_KEYSTORE_PASSWORD);
   }

   public String getStorageAccountKey() {
      return _storageAccountKey;
   }

   public String getStorageAccountName() {
      return _storageAccountName;
   }

   public String getStorageProtocol() {
      return _storageProtocol;
   }

   public boolean getUseSsl() {
      return _useSsl;
   }

   private void initConfigDefaults() {
      setDefaultProperty(ARG_AUTHORIZER_TYPE, Authorizer.Type.none.toString());
      setDefaultProperty(ARG_ALLOW_DEFAULT_KEY_LISTINGS, false);
      setDefaultProperty(ARG_DB_AUTHORIZER_CONN_STRING,
            "jdbc:mysql://localhost/batfish?user=batfish&password=batfish");
      setDefaultProperty(ARG_DB_AUTHORIZER_CACHE_EXPIRY_MS, 15 * 60 * 1000); // 15
                                                                             // minutes
      setDefaultProperty(ARG_DISABLE_SSL, CoordConsts.SVC_DISABLE_SSL);
      setDefaultProperty(ARG_DRIVER_CLASS, null);
      setDefaultProperty(ARG_FILE_AUTHORIZER_PERMS_FILE, "perms.json");
      setDefaultProperty(ARG_FILE_AUTHORIZER_ROOT_DIR, "fileauthorizer");
      setDefaultProperty(ARG_FILE_AUTHORIZER_USERS_FILE, "users.json");
      setDefaultProperty(ARG_HELP, false);
      setDefaultProperty(ARG_LOG_FILE, null);
      setDefaultProperty(ARG_LOG_LEVEL,
            BatfishLogger.getLogLevelStr(BatfishLogger.LEVEL_OUTPUT));
      setDefaultProperty(ARG_PERIOD_ASSIGN_WORK_MS, 1000);
      setDefaultProperty(ARG_PERIOD_CHECK_WORK_MS, 1000);
      setDefaultProperty(ARG_PERIOD_WORKER_STATUS_REFRESH_MS, 10000);
      setDefaultProperty(ARG_QUEUE_COMPLETED_WORK, "batfishcompletedwork");
      setDefaultProperty(ARG_QUEUE_INCOMPLETE_WORK, "batfishincompletework");
      setDefaultProperty(ARG_QUEUE_TYPE, WorkQueue.Type.memory.toString());
      setDefaultProperty(ARG_SERVICE_HOST, "0.0.0.0");
      setDefaultProperty(ARG_SERVICE_POOL_PORT, CoordConsts.SVC_POOL_PORT);
      setDefaultProperty(ARG_SERVICE_WORK_PORT, CoordConsts.SVC_WORK_PORT);
      setDefaultProperty(ARG_SSL_KEYSTORE_FILE, "selfsigned.jks");
      setDefaultProperty(ARG_SSL_KEYSTORE_PASSWORD, "batfish");
      setDefaultProperty(ARG_STORAGE_ACCOUNT_KEY,
            "zRTT++dVryOWXJyAM7NM0TuQcu0Y23BgCQfkt7xh2f/Mm+r6c8/XtPTY0xxaF6tPSACJiuACsjotDeNIVyXM8Q==");
      setDefaultProperty(ARG_STORAGE_ACCOUNT_NAME, "testdrive");
      setDefaultProperty(ARG_STORAGE_PROTOCOL, "http");
      setDefaultProperty(ARG_CONTAINERS_LOCATION, "containers");
   }

   private void initOptions() {
      addOption(ARG_AUTHORIZER_TYPE, "type of authorizer to use",
            "authorizer type");

      addBooleanOption(ARG_ALLOW_DEFAULT_KEY_LISTINGS,
            "allow default API key to list containers and testrigs");

      addOption(ARG_DB_AUTHORIZER_CONN_STRING,
            "connection string for authorizer db", "connection string");

      addOption(ARG_DB_AUTHORIZER_CACHE_EXPIRY_MS,
            "when to expire information from authorizer database",
            "expiration time (ms)");

      addBooleanOption(ARG_DISABLE_SSL, "disable coordinator ssl");

      addBooleanOption(ARG_DRIVER_CLASS,
            "jdbc driver class to load explicitly");

      addBooleanOption(ARG_HELP, "print this message");

      addOption(ARG_LOG_FILE, "send output to specified log file", "logfile");

      addOption(ARG_LOG_LEVEL, "log level", "loglevel");

      addOption(ARG_PERIOD_WORKER_STATUS_REFRESH_MS,
            "period with which to check worker status (ms)",
            "period_worker_status_refresh_ms");

      addOption(ARG_PERIOD_ASSIGN_WORK_MS,
            "period with which to assign work (ms)", "period_assign_work_ms");

      addOption(ARG_PERIOD_CHECK_WORK_MS,
            "period with which to check work (ms)", "period_check_work_ms");

      addOption(ARG_QUEUE_TYPE, "queue type to use {azure, memory}", "qtype");

      addOption(ARG_SERVICE_HOST, "hostname for the service",
            "base url for coordinator service");

      addOption(ARG_SERVICE_POOL_PORT, "port for pool management service",
            "port_number_pool_service");

      addOption(ARG_SERVICE_WORK_PORT, "port for work management service",
            "port_number_work_service");

      addOption(ARG_SSL_KEYSTORE_FILE, "which keystore file to use for ssl",
            "keystore (.jks) file");

      addOption(ARG_CONTAINERS_LOCATION, "where to store containers",
            "containers_location");

   }

   private void parseCommandLine(String[] args) {
      initCommandLine(args);

      if (getBooleanOptionValue(ARG_HELP)) {
         printHelp(EXECUTABLE_NAME);
         System.exit(0);
      }

      _authorizerType = Authorizer.Type
            .valueOf(getStringOptionValue(ARG_AUTHORIZER_TYPE));
      _dbAuthorizerConnString = getStringOptionValue(
            ARG_DB_AUTHORIZER_CONN_STRING);
      _dbCacheExpiryMs = getLongOptionValue(ARG_DB_AUTHORIZER_CACHE_EXPIRY_MS);
      _defaultKeyListings = getBooleanOptionValue(
            ARG_ALLOW_DEFAULT_KEY_LISTINGS);
      _driverClass = getStringOptionValue(ARG_DRIVER_CLASS);
      _queuIncompleteWork = getStringOptionValue(ARG_QUEUE_INCOMPLETE_WORK);
      _queueCompletedWork = getStringOptionValue(ARG_QUEUE_COMPLETED_WORK);
      _queueType = WorkQueue.Type.valueOf(getStringOptionValue(ARG_QUEUE_TYPE));
      _servicePoolPort = getIntegerOptionValue(ARG_SERVICE_POOL_PORT);
      _serviceWorkPort = getIntegerOptionValue(ARG_SERVICE_WORK_PORT);
      _serviceHost = getStringOptionValue(ARG_SERVICE_HOST);
      _sslKeystoreFilename = getStringOptionValue(ARG_SSL_KEYSTORE_FILE);
      _storageAccountKey = getStringOptionValue(ARG_STORAGE_ACCOUNT_KEY);
      _storageAccountName = getStringOptionValue(ARG_STORAGE_ACCOUNT_NAME);
      _storageProtocol = getStringOptionValue(ARG_STORAGE_PROTOCOL);
      _containersLocation = getStringOptionValue(ARG_CONTAINERS_LOCATION);
      _periodWorkerStatusRefreshMs = getLongOptionValue(
            ARG_PERIOD_WORKER_STATUS_REFRESH_MS);
      _periodAssignWorkMs = getLongOptionValue(ARG_PERIOD_ASSIGN_WORK_MS);
      _periodCheckWorkMs = getLongOptionValue(ARG_PERIOD_CHECK_WORK_MS);
      _logFile = getStringOptionValue(ARG_LOG_FILE);
      _logLevel = getStringOptionValue(ARG_LOG_LEVEL);
      _useSsl = !getBooleanOptionValue(ARG_DISABLE_SSL);
   }
}
