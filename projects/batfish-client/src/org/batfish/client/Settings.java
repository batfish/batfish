package org.batfish.client;

import org.batfish.client.config.ConfigurationLocator;
import org.batfish.common.BaseSettings;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.CoordConsts;
import org.batfish.common.Util;

public class Settings extends BaseSettings {
   
   public enum RunMode {
      batch,
      genquestions,
      interactive
   }
   
   private static final String ARG_API_KEY = "apikey";
   public static final String ARG_BATCH_COMMAND_FILE = "batchcmdfile";
   private static final String ARG_DISABLE_SSL = "disablessl";
   private static final String ARG_HELP = "help";
   public static final String ARG_LOG_FILE = "logfile";
   public static final String ARG_LOG_LEVEL = "loglevel";
   private static final String ARG_NO_SANITY_CHECK = "nosanitycheck";
   private static final String ARG_PERIOD_CHECK_WORK = "periodcheckworkms";
   private static final String ARG_QUESTIONS_DIR = "questionsdir";   
   public static final String ARG_RUN_MODE = "runmode";
   
   public static final String ARG_COORDINATOR_HOST = "coordinatorhost";
   private static final String ARG_SERVICE_POOL_PORT = "coordinatorpoolport";
   private static final String ARG_SERVICE_WORK_PORT = "coordinatorworkport";
   private static final String ARG_TRUST_ALL_SSL_CERTS = "trustallsslcerts";

   private static final String EXECUTABLE_NAME = "batfish_client";

   private String _apiKey;
   private String _batchCommandFile;
   private String _coordinatorHost;
   private int _coordinatorPoolPort;
   private int _coordinatorWorkPort;
   private String _logFile;
   private String _logLevel;
   private boolean _sanityCheck;
   private long _periodCheckWorkMs;
   private String _questionsDir;
   private RunMode _runMode;
   private boolean _trustAllSslCerts;
   private boolean _useSsl;

   public Settings(String[] args) throws Exception {
      super(Util.getConfigProperties(ConfigurationLocator.class,
            BfConsts.RELPATH_CONFIG_FILE_NAME_CLIENT));

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

   public String getCoordinatorHost() {
      return _coordinatorHost;
   }

   public int getCoordinatorPoolPort() {
      return _coordinatorPoolPort;
   }

   public int getCoordinatorWorkPort() {
      return _coordinatorWorkPort;
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

   public String getQuestionsDir() {
      return _questionsDir;
   }

   public RunMode getRunMode() {
      return _runMode;
   }

   public boolean getSanityCheck() {
      return _sanityCheck;
   }
   
   public boolean getTrustAllSslCerts() {
      return _trustAllSslCerts;
   }

   public boolean getUseSsl() {
      return _useSsl;
   }

   private void initConfigDefaults() {
      setDefaultProperty(ARG_API_KEY, CoordConsts.DEFAULT_API_KEY);
      setDefaultProperty(ARG_DISABLE_SSL, CoordConsts.SVC_DISABLE_SSL);
      setDefaultProperty(ARG_HELP, false);
      setDefaultProperty(ARG_LOG_FILE, null);
      setDefaultProperty(ARG_LOG_LEVEL,
            BatfishLogger.getLogLevelStr(BatfishLogger.LEVEL_OUTPUT));
      setDefaultProperty(ARG_NO_SANITY_CHECK, false);
      setDefaultProperty(ARG_PERIOD_CHECK_WORK, 1000);
      setDefaultProperty(ARG_QUESTIONS_DIR, "questions_lib");
      setDefaultProperty(ARG_RUN_MODE, RunMode.batch.toString());
      setDefaultProperty(ARG_COORDINATOR_HOST, "localhost");
      setDefaultProperty(ARG_SERVICE_POOL_PORT, CoordConsts.SVC_POOL_PORT);
      setDefaultProperty(ARG_SERVICE_WORK_PORT, CoordConsts.SVC_WORK_PORT);
      setDefaultProperty(ARG_TRUST_ALL_SSL_CERTS, true);
   }

   private void initOptions() {
      addOption(ARG_API_KEY, "API key for the coordinator", "apikey");

      addOption(ARG_BATCH_COMMAND_FILE,
            "read commands from the specified command file", "cmdfile");

      addBooleanOption(ARG_DISABLE_SSL, "disable coordinator ssl");

      addBooleanOption(ARG_HELP, "print this message");

      addOption(ARG_LOG_FILE, "send output to specified log file", "logfile");

      addOption(ARG_LOG_LEVEL, "log level", "loglevel");

      addBooleanOption(ARG_NO_SANITY_CHECK, "do not check if container, testrig etc. are set. (helps debugging.)");

      addOption(ARG_PERIOD_CHECK_WORK, "period with which to check work (ms)",
            "period_check_work_ms");

      addOption(ARG_QUESTIONS_DIR, "directory to output questions in",
            "questions_dir");

      addOption(ARG_RUN_MODE, "which mode to run in (batch|interactive|genquestions)",
            "run_mode");

      addOption(ARG_COORDINATOR_HOST, "hostname for the service",
            "base url for coordinator service");

      addOption(ARG_SERVICE_POOL_PORT, "port for pool management service",
            "port_number_pool_service");

      addOption(ARG_SERVICE_WORK_PORT, "port for work management service",
            "port_number_work_service");

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
      _batchCommandFile = getStringOptionValue(ARG_BATCH_COMMAND_FILE);
      _logFile = getStringOptionValue(ARG_LOG_FILE);
      _logLevel = getStringOptionValue(ARG_LOG_LEVEL);
      _periodCheckWorkMs = getLongOptionValue(ARG_PERIOD_CHECK_WORK);
      _questionsDir = getStringOptionValue(ARG_QUESTIONS_DIR);
      _runMode = RunMode.valueOf(getStringOptionValue(ARG_RUN_MODE));
      _sanityCheck = !getBooleanOptionValue(ARG_NO_SANITY_CHECK);

      _coordinatorHost = getStringOptionValue(ARG_COORDINATOR_HOST);
      _coordinatorPoolPort = getIntegerOptionValue(ARG_SERVICE_POOL_PORT);
      _coordinatorWorkPort = getIntegerOptionValue(ARG_SERVICE_WORK_PORT);
      _trustAllSslCerts = getBooleanOptionValue(ARG_TRUST_ALL_SSL_CERTS);
      _useSsl = !getBooleanOptionValue(ARG_DISABLE_SSL);
   }

}
