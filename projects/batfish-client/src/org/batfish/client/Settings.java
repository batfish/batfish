package org.batfish.client;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.batfish.client.config.ConfigurationLocator;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CoordConsts;
import org.apache.commons.configuration.*;

public class Settings {

   private static final String ARG_LOG_FILE = "client.LogFile";
   private static final String ARG_LOG_LEVEL = "client.LogLevel";
   private static final String ARG_PERIOD_CHECK_WORK = "client.PeriodCheckWorkMs";
   private static final String ARG_SERVICE_HOST = "coordinator.ServiceHost";
   private static final String ARG_SERVICE_POOL_PORT = "coordinator.PoolPort";
   private static final String ARG_SERVICE_WORK_PORT = "coordinator.WorkPort";

   private static final String ARG_TRUST_ALL_SSL_CERTS = "client.TrustAllSslCerts";
   private static final String ARG_USE_SSL = "coordinator.UseSsl";

   /*
    *  not wired to command line
    */   
   private static final String ARG_API_KEY = "client.ApiKey";
   private static final String ARG_COMMAND_FILE = "cmdfile";
   private static final String ARG_HELP = "help";

   private static final String EXECUTABLE_NAME = "batfish_client";

   private String _commandFile;
   private FileConfiguration _config;
   private CommandLine _line;
   private String _logFile;
   private String _logLevel;
   private Options _options;
   private long _periodCheckWorkMs;
   private String _serviceHost;
   private int _servicePoolPort;
   private int _serviceWorkPort;
   private boolean _useSsl;

   public Settings() throws Exception {
      this(new String[] {});
   }

   public Settings(String[] args) throws Exception {

      _config = new PropertiesConfiguration();
      _config.setFile(org.batfish.common.Util
            .getConfigProperties(ConfigurationLocator.class));
      _config.load();
      initConfigDefaults();
      
      initOptions();
      parseCommandLine(args);
   }

   public String getApiKey() {
      return _config.getString(ARG_API_KEY);
   }

   private boolean getBooleanOptionValue(String key) {
      boolean value = _line.hasOption(key);
      if (!value) {
         value = _config.getBoolean(key);
      }
      return value;
   }

   public String getCommandFile() {
      return _commandFile;
   }

   private Integer getIntegerOptionValue(String key) {
      String valueStr = _line.getOptionValue(key);
      if (valueStr == null) {
         return _config.getInteger(key, null);
      }
      else {
         return Integer.parseInt(valueStr);
      }
   }

   public String getLogFile() {
      return _logFile;
   }

   public String getLogLevel() {
      return _logLevel;
   }

   private Long getLongOptionValue(String key) {
      String valueStr = _line.getOptionValue(key);
      if (valueStr == null) {
         return _config.getLong(key, null);
      }
      else {
         return Long.parseLong(valueStr);
      }
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

   private String getStringOptionValue(String key) {
      String value = _line.getOptionValue(key, _config.getString(key));
      return value;
   }

   public boolean getTrustAllSslCerts() {
      return _config.getBoolean(ARG_TRUST_ALL_SSL_CERTS);
   }

   public boolean getUseSsl() {
      return _useSsl;
   }
   
   private void initConfigDefaults() {
      setDefaultProperty(ARG_LOG_FILE, null);
      setDefaultProperty(ARG_LOG_LEVEL, BatfishLogger.getLogLevelStr(BatfishLogger.LEVEL_OUTPUT));
      setDefaultProperty(ARG_PERIOD_CHECK_WORK, 1000);
      setDefaultProperty(ARG_SERVICE_HOST, "localhost");
      setDefaultProperty(ARG_SERVICE_POOL_PORT, CoordConsts.SVC_POOL_PORT);
      setDefaultProperty(ARG_SERVICE_WORK_PORT, CoordConsts.SVC_WORK_PORT);
      setDefaultProperty(ARG_TRUST_ALL_SSL_CERTS, false);
      setDefaultProperty(ARG_USE_SSL, CoordConsts.SVC_USE_SSL);
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
      _options.addOption(Option.builder().argName("period_check_work_ms")
            .hasArg().desc("period with which to check work (ms)")
            .longOpt(ARG_PERIOD_CHECK_WORK).build());
      _options.addOption(Option.builder().argName("logfile").hasArg()
            .desc("send output to specified log file").longOpt(ARG_LOG_FILE)
            .build());
      _options.addOption(Option.builder().argName("loglevel").hasArg()
            .desc("log level").longOpt(ARG_LOG_LEVEL).build());
      _options.addOption(Option.builder().argName("cmdfile").hasArg()
            .desc("read commands from the specified command file")
            .longOpt(ARG_COMMAND_FILE).build());
      _options.addOption(Option.builder().desc("print this message")
            .longOpt(ARG_HELP).build());
      _options.addOption(Option.builder().argName("use_ssl").hasArg()
            .desc("whether to use ssl with coordinator").longOpt(ARG_USE_SSL)
            .build());
   }

   private void parseCommandLine(String[] args) throws ParseException {
      CommandLineParser parser = new DefaultParser();

      // parse the command line arguments
      _line = parser.parse(_options, args);

      if (_line.hasOption(ARG_HELP)) {
         // automatically generate the help statement
         HelpFormatter formatter = new HelpFormatter();
         formatter.setLongOptPrefix("-");
         formatter.printHelp(EXECUTABLE_NAME, _options);
         System.exit(0);
      }

      _commandFile = getStringOptionValue(ARG_COMMAND_FILE);
      _logFile = getStringOptionValue(ARG_LOG_FILE);
      _logLevel = getStringOptionValue(ARG_LOG_LEVEL);
      _periodCheckWorkMs = getLongOptionValue(ARG_PERIOD_CHECK_WORK);
      _serviceHost = getStringOptionValue(ARG_SERVICE_HOST);
      _servicePoolPort = getIntegerOptionValue(ARG_SERVICE_POOL_PORT);
      _serviceWorkPort = getIntegerOptionValue(ARG_SERVICE_WORK_PORT);
      _useSsl = getBooleanOptionValue(ARG_USE_SSL);
   }
   
   private void setDefaultProperty(String key, Object value) {
      if (_config.getProperty(key) == null) {
         _config.setProperty(key, value);
      }
   }
}
