package org.batfish.client;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CoordConsts;

public class Settings {

   private static final String ARG_COMMAND_FILE = "cmdfile";
   private static final String ARG_HELP = "help";
   private static final String ARG_LOG_FILE = "logfile";
   private static final String ARG_LOG_LEVEL = "loglevel";
   private static final String ARG_PERIOD_CHECK_WORK = "periodcheckwork";
   private static final String ARG_SERVICE_HOST = "coordhost";
   private static final String ARG_SERVICE_POOL_PORT = "poolmgrport";
   private static final String ARG_SERVICE_WORK_PORT = "workmgrport";

   private static final String DEFAULT_LOG_LEVEL = BatfishLogger
         .getLogLevelStr(BatfishLogger.LEVEL_WARN);
   private static final String DEFAULT_PERIOD_CHECK_WORK = "5000"; // 5 seconds
   private static final String DEFAULT_SERVICE_HOST = "localhost";
   private static final String DEFAULT_SERVICE_POOL_PORT = CoordConsts.SVC_POOL_PORT
         .toString();
   private static final String DEFAULT_SERVICE_WORK_PORT = CoordConsts.SVC_WORK_PORT
         .toString();

   private static final String EXECUTABLE_NAME = "batfish_client";

   private String _commandFile;
   private String _logFile;
   private String _logLevel;
   private Options _options;
   private long _periodCheckWorkMs;
   private String _serviceHost;
   private int _servicePoolPort;
   private int _serviceWorkPort;

   public Settings() throws ParseException {
      this(new String[] {});
   }

   public Settings(String[] args) throws ParseException {
      initOptions();
      parseCommandLine(args);
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
            ARG_SERVICE_POOL_PORT, DEFAULT_SERVICE_POOL_PORT));
      _serviceWorkPort = Integer.parseInt(line.getOptionValue(
            ARG_SERVICE_WORK_PORT, DEFAULT_SERVICE_WORK_PORT));
      _serviceHost = line
            .getOptionValue(ARG_SERVICE_HOST, DEFAULT_SERVICE_HOST);

      _periodCheckWorkMs = Long.parseLong(line.getOptionValue(
            ARG_PERIOD_CHECK_WORK, DEFAULT_PERIOD_CHECK_WORK));

      _logFile = line.getOptionValue(ARG_LOG_FILE);

      _logLevel = line.getOptionValue(ARG_LOG_LEVEL, DEFAULT_LOG_LEVEL);

      _commandFile = line.getOptionValue(ARG_COMMAND_FILE);
   }
}
