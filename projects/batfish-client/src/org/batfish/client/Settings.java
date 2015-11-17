package org.batfish.client;

import org.batfish.client.config.ConfigurationLocator;
import org.batfish.common.BaseSettings;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CoordConsts;
import org.batfish.common.Util;

public class Settings extends BaseSettings {

   /*
    * not wired to command line
    */
   private static final String ARG_API_KEY = "client.ApiKey";
   private static final String ARG_COMMAND_FILE = "cmdfile";
   private static final String ARG_DISABLE_SSL = "coordinator.DisableSsl";
   private static final String ARG_HELP = "help";
   private static final String ARG_LOG_FILE = "client.LogFile";
   private static final String ARG_LOG_LEVEL = "client.LogLevel";

   private static final String ARG_PERIOD_CHECK_WORK = "client.PeriodCheckWorkMs";
   private static final String ARG_SERVICE_HOST = "coordinator.ServiceHost";

   private static final String ARG_SERVICE_POOL_PORT = "coordinator.PoolPort";
   private static final String ARG_SERVICE_WORK_PORT = "coordinator.WorkPort";
   private static final String ARG_TRUST_ALL_SSL_CERTS = "client.TrustAllSslCerts";

   private static final String EXECUTABLE_NAME = "batfish_client";

   private String _commandFile;
   private String _logFile;
   private String _logLevel;
   private long _periodCheckWorkMs;
   private String _serviceHost;
   private int _servicePoolPort;
   private int _serviceWorkPort;
   private boolean _useSsl;

   public Settings(String[] args) throws Exception {
      super(Util.getConfigProperties(ConfigurationLocator.class));

      initConfigDefaults();

      initOptions();
      parseCommandLine(args);
   }

   public String getApiKey() {
      return _config.getString(ARG_API_KEY);
   }

   public String getCommandFile() {
      return _commandFile;
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

   public String getServiceHost() {
      return _serviceHost;
   }

   public int getServicePoolPort() {
      return _servicePoolPort;
   }

   public int getServiceWorkPort() {
      return _serviceWorkPort;
   }

   public boolean getTrustAllSslCerts() {
      return _config.getBoolean(ARG_TRUST_ALL_SSL_CERTS);
   }

   public boolean getUseSsl() {
      return _useSsl;
   }

   private void initConfigDefaults() {
      setDefaultProperty(ARG_DISABLE_SSL, CoordConsts.SVC_DISABLE_SSL);
      setDefaultProperty(ARG_HELP, false);
      setDefaultProperty(ARG_LOG_FILE, null);
      setDefaultProperty(ARG_LOG_LEVEL,
            BatfishLogger.getLogLevelStr(BatfishLogger.LEVEL_WARN));
      setDefaultProperty(ARG_PERIOD_CHECK_WORK, 1000);
      setDefaultProperty(ARG_SERVICE_HOST, "localhost");
      setDefaultProperty(ARG_SERVICE_POOL_PORT, CoordConsts.SVC_POOL_PORT);
      setDefaultProperty(ARG_SERVICE_WORK_PORT, CoordConsts.SVC_WORK_PORT);
      setDefaultProperty(ARG_TRUST_ALL_SSL_CERTS, false);
   }

   private void initOptions() {
      addOption(ARG_COMMAND_FILE,
            "read commands from the specified command file", "cmdfile");

      addBooleanOption(ARG_DISABLE_SSL, "disable coordinator ssl");

      addBooleanOption(ARG_HELP, "print this message");

      addOption(ARG_LOG_FILE, "send output to specified log file", "logfile");

      addOption(ARG_LOG_LEVEL, "log level", "loglevel");

      addOption(ARG_PERIOD_CHECK_WORK, "period with which to check work (ms)",
            "period_check_work_ms");

      addOption(ARG_SERVICE_HOST, "hostname for the service",
            "base url for coordinator service");

      addOption(ARG_SERVICE_POOL_PORT, "port for pool management service",
            "port_number_pool_service");

      addOption(ARG_SERVICE_WORK_PORT, "port for work management service",
            "port_number_work_service");

   }

   private void parseCommandLine(String[] args) {
      initCommandLine(args);

      if (getBooleanOptionValue(ARG_HELP)) {
         printHelp(EXECUTABLE_NAME);
         System.exit(0);
      }

      _commandFile = getStringOptionValue(ARG_COMMAND_FILE);
      _logFile = getStringOptionValue(ARG_LOG_FILE);
      _logLevel = getStringOptionValue(ARG_LOG_LEVEL);
      _periodCheckWorkMs = getLongOptionValue(ARG_PERIOD_CHECK_WORK);
      _serviceHost = getStringOptionValue(ARG_SERVICE_HOST);
      _servicePoolPort = getIntegerOptionValue(ARG_SERVICE_POOL_PORT);
      _serviceWorkPort = getIntegerOptionValue(ARG_SERVICE_WORK_PORT);
      _useSsl = !getBooleanOptionValue(ARG_DISABLE_SSL);
   }

}
