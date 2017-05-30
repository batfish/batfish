package org.batfish.client;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.batfish.client.config.ConfigurationLocator;
import org.batfish.common.BaseSettings;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.CoordConsts;
import org.batfish.common.util.CommonUtil;

public class Settings extends BaseSettings {

   public enum RunMode {
      batch,
      gendatamodel,
      genquestions,
      interactive
   }

   private static final String ARG_API_KEY = "apikey";
   public static final String ARG_BATFISH_LOG_LEVEL = "batfishloglevel";
   public static final String ARG_COMMAND_FILE = "cmdfile";
   public static final String ARG_CONTAINER_ID = "containerid";
   public static final String ARG_COORDINATOR_HOST = "coordinatorhost";
   public static final String ARG_DATAMODEL_DIR = "datamodeldir";
   private static final String ARG_DISABLE_SSL = "disablessl";
   private static final String ARG_HELP = "help";
   public static final String ARG_LOG_FILE = "logfile";
   public static final String ARG_LOG_LEVEL = "loglevel";
   private static final String ARG_NO_SANITY_CHECK = "nosanitycheck";
   private static final String ARG_PERIOD_CHECK_WORK = "periodcheckworkms";
   private static final String ARG_PRETTY_PRINT_ANSWERS = "prettyanswers";
   public static final String ARG_QUESTIONS_DIR = "questionsdir";
   public static final String ARG_RUN_MODE = "runmode";
   private static final String ARG_SERVICE_POOL_PORT = "coordinatorpoolport";

   private static final String ARG_SERVICE_WORK_PORT = "coordinatorworkport";
   public static final String ARG_TESTRIG_DIR = "testrigdir";
   public static final String ARG_TESTRIG_ID = "testrigid";
   private static final String ARG_TRUST_ALL_SSL_CERTS = "trustallsslcerts";

   private static final String EXECUTABLE_NAME = "batfish_client";

   private String _apiKey;
   private String _batchCommandFile;
   private String _batfishLogLevel;
   private String _containerId;
   private String _coordinatorHost;
   private int _coordinatorPoolPort;
   private int _coordinatorWorkPort;
   private String _datamodelDir;
   private String _logFile;
   private String _logLevel;
   private long _periodCheckWorkMs;
   private List<Path> _pluginDirs;
   private boolean _prettyPrintAnswers;
   private String _questionsDir;

   private RunMode _runMode;
   private boolean _sanityCheck;
   private String _testrigDir;
   private String _testrigId;
   private boolean _trustAllSslCerts;
   private boolean _useSsl;

   public Settings(String[] args) throws Exception {
      super(CommonUtil.getConfigProperties(ConfigurationLocator.class,
            BfConsts.RELPATH_CONFIG_FILE_NAME_CLIENT,
            BfConsts.PROP_CLIENT_PROPERTIES_PATH));

      initConfigDefaults();

      initOptions();
      parseCommandLine(args);
   }

   public String getApiKey() {
      return _apiKey;
   }

   public String getBatchCommandFile() {
      return _batchCommandFile;
   }

   public String getBatfishLogLevel() {
      return _batfishLogLevel;
   }

   public String getContainerId() {
      return _containerId;
   }

   public String getCoordinatorHost() {
      return _coordinatorHost;
   }

   public int getCoordinatorPoolPort() {
      return _coordinatorPoolPort;
   }

   public int getCoordinatorWorkPort() {
      return _coordinatorWorkPort;
   }

   public String getDatamodelDir() {
      return _datamodelDir;
   }

   public String getLogFile() {
      return _logFile;
   }

   public String getLogLevel() {
      return _logLevel;
   }

   public long getPeriodCheckWorkMs() {
      return _periodCheckWorkMs;
   }

   public List<Path> getPluginDirs() {
      return _pluginDirs;
   }

   public boolean getPrettyPrintAnswers() {
      return _prettyPrintAnswers;
   }

   public String getQuestionsDir() {
      return _questionsDir;
   }

   public RunMode getRunMode() {
      return _runMode;
   }

   public boolean getSanityCheck() {
      return _sanityCheck;
   }

   public String getTestrigDir() {
      return _testrigDir;
   }

   public String getTestrigId() {
      return _testrigId;
   }

   public boolean getTrustAllSslCerts() {
      return _trustAllSslCerts;
   }

   public boolean getUseSsl() {
      return _useSsl;
   }

   private void initConfigDefaults() {
      setDefaultProperty(ARG_API_KEY, CoordConsts.DEFAULT_API_KEY);
      setDefaultProperty(ARG_BATFISH_LOG_LEVEL,
            BatfishLogger.getLogLevelStr(BatfishLogger.LEVEL_WARN));
      setDefaultProperty(ARG_COORDINATOR_HOST, "localhost");
      setDefaultProperty(ARG_DATAMODEL_DIR, "datamodel");
      setDefaultProperty(ARG_DISABLE_SSL, CoordConsts.SVC_DISABLE_SSL);
      setDefaultProperty(ARG_HELP, false);
      setDefaultProperty(ARG_LOG_FILE, null);
      setDefaultProperty(ARG_LOG_LEVEL,
            BatfishLogger.getLogLevelStr(BatfishLogger.LEVEL_OUTPUT));
      setDefaultProperty(ARG_NO_SANITY_CHECK, false);
      setDefaultProperty(ARG_PERIOD_CHECK_WORK, 1000);
      setDefaultProperty(BfConsts.ARG_PLUGIN_DIRS,
            Collections.<String> emptyList());
      setDefaultProperty(ARG_PRETTY_PRINT_ANSWERS, true);
      setDefaultProperty(ARG_RUN_MODE, RunMode.batch.toString());
      setDefaultProperty(ARG_SERVICE_POOL_PORT, CoordConsts.SVC_POOL_PORT);
      setDefaultProperty(ARG_SERVICE_WORK_PORT, CoordConsts.SVC_WORK_PORT);
      setDefaultProperty(ARG_TRUST_ALL_SSL_CERTS, true);
   }

   private void initOptions() {
      addOption(ARG_API_KEY, "API key for the coordinator", "apikey");

      addOption(ARG_COMMAND_FILE,
            "read commands from the specified command file", "cmdfile");

      addOption(ARG_COORDINATOR_HOST, "hostname for the service",
            "base url for coordinator service");

      addOption(ARG_BATFISH_LOG_LEVEL, "log level for batfish",
            "batfish_loglevel");

      addOption(ARG_CONTAINER_ID, "container to attach to", "container_id");

      addOption(ARG_DATAMODEL_DIR, "directory where datamodel should be dumped",
            "datamodel_dir");

      addBooleanOption(ARG_DISABLE_SSL, "disable coordinator ssl");

      addBooleanOption(ARG_HELP, "print this message");

      addOption(ARG_LOG_FILE, "send output to specified log file", "logfile");

      addOption(ARG_LOG_LEVEL, "log level", "loglevel");

      addBooleanOption(ARG_NO_SANITY_CHECK,
            "do not check if container, testrig etc. are set. (helps debugging.)");

      addOption(ARG_PERIOD_CHECK_WORK, "period with which to check work (ms)",
            "period_check_work_ms");

      addListOption(BfConsts.ARG_PLUGIN_DIRS,
            "directories containing plugin jars", "paths");

      addBooleanOption(ARG_PRETTY_PRINT_ANSWERS, "pretty print answers");

      addOption(ARG_QUESTIONS_DIR, "directory to output questions in",
            "questions_dir");

      addOption(ARG_RUN_MODE,
            "which mode to run in (batch|interactive|genquestions)",
            "run_mode");

      addOption(ARG_SERVICE_POOL_PORT, "port for pool management service",
            "port_number_pool_service");

      addOption(ARG_SERVICE_WORK_PORT, "port for work management service",
            "port_number_work_service");

      addOption(ARG_TESTRIG_DIR, "where the testrig sits", "testrig_dir");

      addOption(ARG_TESTRIG_ID, "testrig to attach to", "testrig_id");

      addBooleanOption(ARG_TRUST_ALL_SSL_CERTS,
            "whether we should trust any coordinator SSL certs (for testing locally)");
   }

   private void parseCommandLine(String[] args) {
      initCommandLine(args);

      if (getBooleanOptionValue(ARG_HELP)) {
         printHelp(EXECUTABLE_NAME);
         System.exit(0);
      }

      _apiKey = getStringOptionValue(ARG_API_KEY);
      _batchCommandFile = getStringOptionValue(ARG_COMMAND_FILE);
      _batfishLogLevel = getStringOptionValue(ARG_BATFISH_LOG_LEVEL);
      _containerId = getStringOptionValue(ARG_CONTAINER_ID);
      _datamodelDir = getStringOptionValue(ARG_DATAMODEL_DIR);
      _logFile = getStringOptionValue(ARG_LOG_FILE);
      _logLevel = getStringOptionValue(ARG_LOG_LEVEL);
      _periodCheckWorkMs = getLongOptionValue(ARG_PERIOD_CHECK_WORK);
      _pluginDirs = getPathListOptionValue(BfConsts.ARG_PLUGIN_DIRS);
      _prettyPrintAnswers = getBooleanOptionValue(ARG_PRETTY_PRINT_ANSWERS);
      _questionsDir = getStringOptionValue(ARG_QUESTIONS_DIR);
      _runMode = RunMode.valueOf(getStringOptionValue(ARG_RUN_MODE));
      _sanityCheck = !getBooleanOptionValue(ARG_NO_SANITY_CHECK);
      _testrigDir = getStringOptionValue(ARG_TESTRIG_DIR);
      _testrigId = getStringOptionValue(ARG_TESTRIG_ID);

      _coordinatorHost = getStringOptionValue(ARG_COORDINATOR_HOST);
      _coordinatorPoolPort = getIntegerOptionValue(ARG_SERVICE_POOL_PORT);
      _coordinatorWorkPort = getIntegerOptionValue(ARG_SERVICE_WORK_PORT);
      _trustAllSslCerts = getBooleanOptionValue(ARG_TRUST_ALL_SSL_CERTS);
      _useSsl = !getBooleanOptionValue(ARG_DISABLE_SSL);

   }

   public void setBatfishLogLevel(String logLevel) {
      _batfishLogLevel = logLevel;
   }

   public void setLogLevel(String logLevel) {
      _logLevel = logLevel;
   }

   public void setPrettyPrintAnswers(boolean prettyPrint) {
      _prettyPrintAnswers = prettyPrint;
   }
}
