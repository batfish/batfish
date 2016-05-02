package org.batfish.client;

import java.nio.file.Paths;

import org.batfish.client.config.ConfigurationLocator;
import org.batfish.common.BaseSettings;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CoordConsts;
import org.batfish.common.Util;

public class Settings extends BaseSettings {

   private static final String ARG_API_KEY = "apikey";
   private static final String ARG_BATCH_COMMAND_FILE = "batchcmdfile";
   private static final String ARG_DISABLE_SSL = "disablessl";
   private static final String ARG_HELP = "help";
   private static final String ARG_LOG_FILE = "logfile";
   private static final String ARG_LOG_LEVEL = "loglevel";
   private static final String ARG_PERIOD_CHECK_WORK = "periodcheckworkms";
   private static final String ARG_RUN_MODE = "runmode";
   private static final String ARG_TRIAL_CMDS_FILE = "trialcmds";  
   private static final String ARG_COORDINATOR_HOST = "coordinatorhost";
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
   private long _periodCheckWorkMs;
   private String _runMode;
   private String _trialCmdsFile;
   private boolean _trustAllSslCerts;
   private boolean _useSsl;

   public Settings(String[] args) throws Exception {
      super(Util.getConfigProperties(ConfigurationLocator.class));

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

   public String getRunMode() {
      return _runMode;
   }

   public String getTrialCmdsFile() {
      return _trialCmdsFile;
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
            BatfishLogger.getLogLevelStr(BatfishLogger.LEVEL_WARN));
      setDefaultProperty(ARG_PERIOD_CHECK_WORK, 1000);
      setDefaultProperty(ARG_RUN_MODE, "interactive");
      setDefaultProperty(ARG_TRIAL_CMDS_FILE, 
            Paths.get(org.batfish.common.Util.getJarOrClassDir(
                  ConfigurationLocator.class).getAbsolutePath(), "trial.cmds")
                  .toAbsolutePath().toString());
      setDefaultProperty(ARG_COORDINATOR_HOST, "localhost");
      setDefaultProperty(ARG_SERVICE_POOL_PORT, CoordConsts.SVC_POOL_PORT);
      setDefaultProperty(ARG_SERVICE_WORK_PORT, CoordConsts.SVC_WORK_PORT);
      setDefaultProperty(ARG_TRUST_ALL_SSL_CERTS, true);
   }

   private void initOptions() {
      addOption(ARG_API_KEY,
            "API key for the coordinator", "apikey");

      addOption(ARG_BATCH_COMMAND_FILE,
            "read commands from the specified command file", "cmdfile");

      addBooleanOption(ARG_DISABLE_SSL, "disable coordinator ssl");

      addBooleanOption(ARG_HELP, "print this message");

      addOption(ARG_LOG_FILE, "send output to specified log file", "logfile");

      addOption(ARG_LOG_LEVEL, "log level", "loglevel");

      addOption(ARG_PERIOD_CHECK_WORK, "period with which to check work (ms)",
            "period_check_work_ms");

      addOption(ARG_RUN_MODE, "which mode to run in (auto|batch|interactive)",
            "run_mode");
      
      addOption(ARG_TRIAL_CMDS_FILE, "the trial cmds file",
            "trial_cmds_file");
      
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
      _runMode = getStringOptionValue(ARG_RUN_MODE);
      _trialCmdsFile = getStringOptionValue(ARG_TRIAL_CMDS_FILE);
      _coordinatorHost = getStringOptionValue(ARG_COORDINATOR_HOST);
      _coordinatorPoolPort = getIntegerOptionValue(ARG_SERVICE_POOL_PORT);
      _coordinatorWorkPort = getIntegerOptionValue(ARG_SERVICE_WORK_PORT);
      _trustAllSslCerts = getBooleanOptionValue(ARG_TRUST_ALL_SSL_CERTS);
      _useSsl = !getBooleanOptionValue(ARG_DISABLE_SSL);
   }

}
