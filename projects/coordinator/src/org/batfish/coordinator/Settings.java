package org.batfish.coordinator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.batfish.coordinator.authorizer.Authorizer;
import org.batfish.coordinator.config.ConfigurationLocator;
import org.batfish.coordinator.queues.WorkQueue;

public class Settings {

   private static final String ARG_AUTHORIZER_TYPE = "coordinator.AuthorizerType";
   private static final String ARG_LOG_FILE = "coordinator.LogFile";
   private static final String ARG_LOG_LEVEL = "coordinator.LogLevel";
   private static final String ARG_PERIOD_ASSIGN_WORK = "coordinator.PeriodAssignWorkMs";
   private static final String ARG_PERIOD_CHECK_WORK = "coordinator.PeriodCheckWorkMs";
   private static final String ARG_PERIOD_WORKER_STATUS_REFRESH = "coordinator.PeriodWorkerRefresh";
   private static final String ARG_QUEUE_COMPLETED_WORK = "coordinator.Q_CompletedWork";
   private static final String ARG_QUEUE_INCOMPLETE_WORK = "coordinator.Q_IncompleteWork";
   private static final String ARG_QUEUE_TYPE = "coordinator.Q_Type";
   private static final String ARG_SERVICE_HOST = "coordinator.ServiceHost";
   private static final String ARG_SERVICE_POOL_PORT = "coordinator.PoolPort";
   private static final String ARG_SERVICE_WORK_PORT = "coordinator.WorkPort";   
   private static final String ARG_STORAGE_ACCOUNT_KEY = "coordinator.StorageAccountKey";
   private static final String ARG_STORAGE_ACCOUNT_NAME = "coordinator.StorageAccountName";
   private static final String ARG_STORAGE_PROTOCOL = "coordinator.StorageProtocol";
   private static final String ARG_TESTRIG_STORAGE_LOCATION = "coordinator.TestrigStorageLocation";
   private static final String ARG_USE_SSL = "coordinator.UseSsl";

   //these arguments are not wired for options
   private static final String ARG_SSL_KEYSTORE_FILE = "coordinator.SslKeyStoreFile";
   private static final String ARG_SSL_KEYSTORE_PASSWORD = "coordinator.SslKeyStorePassword";

   private FileConfiguration _config;
   private Authorizer.Type _authorizerType;
   private String _logFile;
   private String _logLevel;
   private Options _options;
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

   public Settings() throws Exception {
      this(new String[] {});
   }

   public Settings(String[] args) throws Exception {
      
      _config = new PropertiesConfiguration();
      _config.setFile(org.batfish.common.Util.getConfigProperties(ConfigurationLocator.class));
      _config.load();

      initOptions();
      parseCommandLine(args);
   }

   public Authorizer.Type getAuthorizationType() {
      return _authorizerType;
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

   private void initOptions() {
      _options = new Options();
      _options.addOption(Option.builder().argName("port_number_pool_service")
            .hasArg().desc("port for pool management service")
            .longOpt(ARG_SERVICE_POOL_PORT).build());
      _options.addOption(Option.builder().argName("port_number_work_service")
            .hasArg().desc("port for work management service")
            .longOpt(ARG_SERVICE_WORK_PORT).build());
      _options.addOption(Option.builder().argName("hostname for the service")
            .hasArg().desc("base url for coordinator service")
            .longOpt(ARG_SERVICE_HOST).build());
      _options.addOption(Option.builder().argName("qtype").hasArg()
            .desc("queue type to use {azure, memory}").longOpt(ARG_QUEUE_TYPE)
            .build());
      _options.addOption(Option.builder().argName("testrig_storage_location")
            .hasArg().desc("where to store test rigs")
            .longOpt(ARG_TESTRIG_STORAGE_LOCATION).build());
      _options.addOption(Option.builder()
            .argName("period_worker_status_refresh_ms").hasArg()
            .desc("period with which to check worker status (ms)")
            .longOpt(ARG_PERIOD_WORKER_STATUS_REFRESH).build());
      _options.addOption(Option.builder().argName("period_assign_work_ms")
            .hasArg().desc("period with which to assign work (ms)")
            .longOpt(ARG_PERIOD_ASSIGN_WORK).build());
      _options.addOption(Option.builder().argName("period_check_work_ms")
            .hasArg().desc("period with which to check work (ms)")
            .longOpt(ARG_PERIOD_CHECK_WORK).build());
      _options.addOption(Option.builder().argName("log_file_path").hasArg()
            .desc("send output to specified log file").longOpt(ARG_LOG_FILE)
            .build());
      _options.addOption(Option.builder().argName("log_level").hasArg()
            .desc("log level").longOpt(ARG_LOG_LEVEL)
            .build());
      _options.addOption(Option.builder().argName("authorizer type").hasArg()
            .desc("type of authorizer to use").longOpt(ARG_AUTHORIZER_TYPE)
            .build());
      _options.addOption(Option.builder().argName("use_ssl").hasArg()
            .desc("whether to use SSL").longOpt(ARG_USE_SSL)
            .build());
   }

   private void parseCommandLine(String[] args) throws ParseException {
      CommandLine line = null;
      CommandLineParser parser = new DefaultParser();

      // parse the command line arguments
      line = parser.parse(_options, args);

      _queuIncompleteWork = line.getOptionValue(ARG_QUEUE_INCOMPLETE_WORK,
            _config.getString(ARG_QUEUE_INCOMPLETE_WORK));
      _queueCompletedWork = line.getOptionValue(ARG_QUEUE_COMPLETED_WORK,
            _config.getString(ARG_QUEUE_COMPLETED_WORK));

      _queueType = WorkQueue.Type.valueOf(line.getOptionValue(ARG_QUEUE_TYPE,
            _config.getString(ARG_QUEUE_TYPE)));

      _servicePoolPort = Integer.parseInt(line.getOptionValue(
            ARG_SERVICE_POOL_PORT, _config.getString(ARG_SERVICE_POOL_PORT)));
      _serviceWorkPort = Integer.parseInt(line.getOptionValue(
            ARG_SERVICE_WORK_PORT, _config.getString(ARG_SERVICE_WORK_PORT)));
      _serviceHost = line
            .getOptionValue(ARG_SERVICE_HOST, _config.getString(ARG_SERVICE_HOST));

      _storageAccountKey = line.getOptionValue(ARG_STORAGE_ACCOUNT_KEY,
            _config.getString(ARG_STORAGE_ACCOUNT_KEY));
      _storageAccountName = line.getOptionValue(ARG_STORAGE_ACCOUNT_NAME,
            _config.getString(ARG_STORAGE_ACCOUNT_NAME));
      _storageProtocol = line.getOptionValue(ARG_STORAGE_PROTOCOL,
            _config.getString(ARG_STORAGE_PROTOCOL));

      _testrigStorageLocation = line.getOptionValue(
            ARG_TESTRIG_STORAGE_LOCATION, _config.getString(ARG_TESTRIG_STORAGE_LOCATION));

      _periodWorkerStatusRefreshMs = Long.parseLong(line.getOptionValue(
            ARG_PERIOD_WORKER_STATUS_REFRESH,
            _config.getString(ARG_PERIOD_WORKER_STATUS_REFRESH)));
      _periodAssignWorkMs = Long.parseLong(line.getOptionValue(
            ARG_PERIOD_ASSIGN_WORK, _config.getString(ARG_PERIOD_ASSIGN_WORK)));
      _periodCheckWorkMs = Long.parseLong(line.getOptionValue(
            ARG_PERIOD_CHECK_WORK, _config.getString(ARG_PERIOD_CHECK_WORK)));

      _logFile = line.getOptionValue(ARG_LOG_FILE, _config.getString(ARG_LOG_FILE));
      _logLevel = line.getOptionValue(ARG_LOG_LEVEL, _config.getString(ARG_LOG_LEVEL));
      
      _authorizerType = Authorizer.Type.valueOf(line.getOptionValue(
            ARG_AUTHORIZER_TYPE, _config.getString(ARG_AUTHORIZER_TYPE)));
      
      _useSsl = Boolean.parseBoolean(line.getOptionValue(
            ARG_USE_SSL, _config.getString(ARG_USE_SSL)));
   }
}
