package org.batfish.client;

import java.io.File;

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

   private static final String ARG_COMMAND_FILE = "cmdfile";
   private static final String ARG_HELP = "help";
   private static final String ARG_API_KEY = "client.ApiKey";
   private static final String ARG_LOG_FILE = "client.LogFile";
   private static final String ARG_LOG_LEVEL = "client.LogLevel";
   private static final String ARG_PERIOD_CHECK_WORK = "client.PeriodCheckWorkMs";
   private static final String ARG_SERVICE_HOST = "coordinator.Host";
   private static final String ARG_SERVICE_POOL_PORT = "coordinator.PoolPort";
   private static final String ARG_SERVICE_WORK_PORT = "coordinator.WorkPort";

   private static final String EXECUTABLE_NAME = "batfish_client";

   private String _commandFile;
   private FileConfiguration _config;
   private String _logFile;
   private String _logLevel;
   private Options _options;
   private long _periodCheckWorkMs;
   private String _serviceHost;
   private int _servicePoolPort;
   private int _serviceWorkPort;

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
   }

   private void parseCommandLine(String[] args) throws ParseException {
      CommandLine line = null;
      CommandLineParser parser = new DefaultParser();

      // parse the command line arguments
      line = parser.parse(_options, args);

      if (line.hasOption(ARG_HELP)) {
         // automatically generate the help statement
         HelpFormatter formatter = new HelpFormatter();
         formatter.setLongOptPrefix("-");
         formatter.printHelp(EXECUTABLE_NAME, _options);
         System.exit(0);
      }

      _servicePoolPort = Integer.parseInt(line.getOptionValue(
            ARG_SERVICE_POOL_PORT, _config.getString(ARG_SERVICE_POOL_PORT)));
      _serviceWorkPort = Integer.parseInt(line.getOptionValue(
            ARG_SERVICE_WORK_PORT, _config.getString(ARG_SERVICE_WORK_PORT)));
      _serviceHost = line
            .getOptionValue(ARG_SERVICE_HOST, _config.getString(ARG_SERVICE_HOST));
      _periodCheckWorkMs = Long.parseLong(line.getOptionValue(
            ARG_PERIOD_CHECK_WORK, _config.getString(ARG_PERIOD_CHECK_WORK)));

      _logFile = line.getOptionValue(ARG_LOG_FILE, _config.getString(ARG_LOG_FILE));

      _logLevel = line.getOptionValue(ARG_LOG_LEVEL, _config.getString(ARG_LOG_LEVEL));

      _commandFile = line.getOptionValue(ARG_COMMAND_FILE);
   }
}
