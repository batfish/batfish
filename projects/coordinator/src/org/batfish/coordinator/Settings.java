package org.batfish.coordinator;

import org.batfish.common.BaseSettings;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CoordConsts;
import org.batfish.common.Util;
import org.batfish.coordinator.authorizer.Authorizer;
import org.batfish.coordinator.config.ConfigurationLocator;
import org.batfish.coordinator.queues.WorkQueue;

public class Settings extends BaseSettings {

   private static final String ARG_AUTHORIZER_TYPE = "authorizertype";
   /**
    * (not wired to command line)
    */
   private static final String ARG_DB_AUTHORIZER_CACHE_EXPIRY_MS = "dbcacheexpiry";
   private static final String ARG_DB_AUTHORIZER_CONN_STRING = "dbconnection";
   private static final String ARG_DISABLE_SSL = "disablessl";
   private static final String ARG_FILE_AUTHORIZER_PERMS_FILE = "coordinator.FileAuthPermsFile";
   private static final String ARG_FILE_AUTHORIZER_ROOT_DIR = "coordinator.FileAuthRootDir";
   private static final String ARG_FILE_AUTHORIZER_USERS_FILE = "coordinator.FileAuthUsersFile";
   private static final String ARG_HELP = "help";
   private static final String ARG_LOG_FILE = "logfile";
   private static final String ARG_LOG_LEVEL = "loglevel";
   private static final String ARG_PERIOD_ASSIGN_WORK_MS = "coordinator.PeriodAssignWorkMs";
   private static final String ARG_PERIOD_CHECK_WORK_MS = "coordinator.PeriodCheckWorkMs";
   private static final String ARG_PERIOD_WORKER_STATUS_REFRESH_MS = "coordinator.PeriodWorkerRefreshMs";
   private static final String ARG_QUEUE_COMPLETED_WORK = "coordinator.Q_CompletedWork";
   private static final String ARG_QUEUE_INCOMPLETE_WORK = "coordinator.Q_IncompleteWork";
   private static final String ARG_QUEUE_TYPE = "coordinator.Q_Type";
   private static final String ARG_SERVICE_HOST = "coordinator.ServiceHost";
   private static final String ARG_SERVICE_POOL_PORT = "coordinator.PoolPort";
   private static final String ARG_SERVICE_WORK_PORT = "coordinator.WorkPort";
   private static final String ARG_SSL_KEYSTORE_FILE = "coordinator.SslKeyStoreFile";
   private static final String ARG_SSL_KEYSTORE_PASSWORD = "coordinator.SslKeyStorePassword";
   private static final String ARG_STORAGE_ACCOUNT_KEY = "coordinator.StorageAccountKey";
   private static final String ARG_STORAGE_ACCOUNT_NAME = "coordinator.StorageAccountName";
   private static final String ARG_STORAGE_PROTOCOL = "coordinator.StorageProtocol";
   private static final String ARG_TESTRIG_STORAGE_LOCATION = "coordinator.TestrigStorageLocation";
   private static final String EXECUTABLE_NAME = "coordinator";

   private Authorizer.Type _authorizerType;
   private String _dbAuthorizerConnString;
   private long _dbCacheExpiryMs;
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
   private String _storageAccountKey;
   private String _storageAccountName;
   private String _storageProtocol;
   private String _testrigStorageLocation;
   private boolean _useSsl;

   public Settings(String[] args) throws Exception {
      super(Util.getConfigProperties(ConfigurationLocator.class));

      initConfigDefaults();

      initOptions();
      parseCommandLine(args);
   }

   public Authorizer.Type getAuthorizationType() {
      return _authorizerType;
   }

   public long getDbAuthorizerCacheExpiryMs() {
      return _dbCacheExpiryMs;
   }

   public String getDbAuthorizerConnString() {
      return _dbAuthorizerConnString;
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
      return _config.getString(ARG_SSL_KEYSTORE_FILE);
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

   public String getTestrigStorageLocation() {
      return _testrigStorageLocation;
   }

   public boolean getUseSsl() {
      return _useSsl;
   }

   private void initConfigDefaults() {
      setDefaultProperty(ARG_AUTHORIZER_TYPE, Authorizer.Type.none.toString());
      setDefaultProperty(ARG_DB_AUTHORIZER_CONN_STRING,
            "jdbc:mysql://localhost/batfish_test?user=batfish&password=batfish");
      setDefaultProperty(ARG_DB_AUTHORIZER_CACHE_EXPIRY_MS, 60 * 1000);
      setDefaultProperty(ARG_DISABLE_SSL, CoordConsts.SVC_DISABLE_SSL);
      setDefaultProperty(ARG_FILE_AUTHORIZER_PERMS_FILE, "perms.json");
      setDefaultProperty(ARG_FILE_AUTHORIZER_ROOT_DIR, "fileauthorizer");
      setDefaultProperty(ARG_FILE_AUTHORIZER_USERS_FILE, "users.json");
      setDefaultProperty(ARG_HELP, false);
      setDefaultProperty(ARG_LOG_FILE, null);
      setDefaultProperty(ARG_LOG_LEVEL,
            BatfishLogger.getLogLevelStr(BatfishLogger.LEVEL_OUTPUT));
      setDefaultProperty(ARG_PERIOD_ASSIGN_WORK_MS, 1000);
      setDefaultProperty(ARG_PERIOD_CHECK_WORK_MS, 5000);
      setDefaultProperty(ARG_PERIOD_WORKER_STATUS_REFRESH_MS, 10000);
      setDefaultProperty(ARG_QUEUE_COMPLETED_WORK, "batfishcompletedwork");
      setDefaultProperty(ARG_QUEUE_INCOMPLETE_WORK, "batfishincompletework");
      setDefaultProperty(ARG_QUEUE_TYPE, WorkQueue.Type.memory.toString());
      setDefaultProperty(ARG_SERVICE_HOST, "0.0.0.0");
      setDefaultProperty(ARG_SERVICE_POOL_PORT, CoordConsts.SVC_POOL_PORT);
      setDefaultProperty(ARG_SERVICE_WORK_PORT, CoordConsts.SVC_WORK_PORT);
      setDefaultProperty(ARG_SSL_KEYSTORE_FILE, "selfsigned.jks");
      setDefaultProperty(ARG_SSL_KEYSTORE_PASSWORD, "batfish");
      setDefaultProperty(
            ARG_STORAGE_ACCOUNT_KEY,
            "zRTT++dVryOWXJyAM7NM0TuQcu0Y23BgCQfkt7xh2f/Mm+r6c8/XtPTY0xxaF6tPSACJiuACsjotDeNIVyXM8Q==");
      setDefaultProperty(ARG_STORAGE_ACCOUNT_NAME, "testdrive");
      setDefaultProperty(ARG_STORAGE_PROTOCOL, "http");
      setDefaultProperty(ARG_TESTRIG_STORAGE_LOCATION, "containers");
   }

   private void initOptions() {
      addOption(ARG_AUTHORIZER_TYPE, "type of authorizer to use",
            "authorizer type");

      addOption(ARG_DB_AUTHORIZER_CONN_STRING,
            "connection string for authorizer db", "connection string");

      addOption(ARG_DB_AUTHORIZER_CACHE_EXPIRY_MS,
            "when to expire information from authorizer database",
            "expiration time (ms)");

      addBooleanOption(ARG_DISABLE_SSL, "disable coordinator ssl");

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

      addOption(ARG_TESTRIG_STORAGE_LOCATION, "where to store test rigs",
            "testrig_storage_location");

   }

   private void parseCommandLine(String[] args) {
      initCommandLine(args);

      if (getBooleanOptionValue(ARG_HELP)) {
         printHelp(EXECUTABLE_NAME);
         System.exit(0);
      }

      _authorizerType = Authorizer.Type
            .valueOf(getStringOptionValue(ARG_AUTHORIZER_TYPE));
      _dbAuthorizerConnString = getStringOptionValue(ARG_DB_AUTHORIZER_CONN_STRING);
      _dbCacheExpiryMs = getLongOptionValue(ARG_DB_AUTHORIZER_CACHE_EXPIRY_MS);
      _queuIncompleteWork = getStringOptionValue(ARG_QUEUE_INCOMPLETE_WORK);
      _queueCompletedWork = getStringOptionValue(ARG_QUEUE_COMPLETED_WORK);
      _queueType = WorkQueue.Type.valueOf(getStringOptionValue(ARG_QUEUE_TYPE));
      _servicePoolPort = getIntegerOptionValue(ARG_SERVICE_POOL_PORT);
      _serviceWorkPort = getIntegerOptionValue(ARG_SERVICE_WORK_PORT);
      _serviceHost = getStringOptionValue(ARG_SERVICE_HOST);
      _storageAccountKey = getStringOptionValue(ARG_STORAGE_ACCOUNT_KEY);
      _storageAccountName = getStringOptionValue(ARG_STORAGE_ACCOUNT_NAME);
      _storageProtocol = getStringOptionValue(ARG_STORAGE_PROTOCOL);
      _testrigStorageLocation = getStringOptionValue(ARG_TESTRIG_STORAGE_LOCATION);
      _periodWorkerStatusRefreshMs = getLongOptionValue(ARG_PERIOD_WORKER_STATUS_REFRESH_MS);
      _periodAssignWorkMs = getLongOptionValue(ARG_PERIOD_ASSIGN_WORK_MS);
      _periodCheckWorkMs = getLongOptionValue(ARG_PERIOD_CHECK_WORK_MS);
      _logFile = getStringOptionValue(ARG_LOG_FILE);
      _logLevel = getStringOptionValue(ARG_LOG_LEVEL);
      _useSsl = !getBooleanOptionValue(ARG_DISABLE_SSL);
   }
}
