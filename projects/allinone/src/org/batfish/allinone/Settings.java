package org.batfish.allinone;

import java.nio.file.Paths;

import org.batfish.allinone.config.ConfigurationLocator;
import org.batfish.common.BaseSettings;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CoordConsts;
import org.batfish.common.Util;

public class Settings extends BaseSettings {

   private static final String ARG_API_KEY = "apikey";
   private static final String ARG_COMMAND_FILE = "cmdfile";
   private static final String ARG_HELP = "help";
   private static final String ARG_LOG_FILE = "logfile";
   private static final String ARG_LOG_LEVEL = "loglevel";
   private static final String ARG_BATFISH_ARGS = "batfishargs";
   private static final String ARG_CLIENT_ARGS = "clientargs";
   private static final String ARG_COORDINATOR_ARGS = "coordinatorargs";
   private static final String ARG_TESTRIG_DIR = "testrigdir";

   private static final String EXECUTABLE_NAME = "allinone";

   private String _apiKey;
   private String _batfishArgs;
   private String _clientArgs;
   private String _coordinatorArgs;
   private String _commandFile;
   private String _logFile;
   private String _logLevel;
   private String _testrigDir;

   public Settings(String[] args) throws Exception {
      super(Util.getConfigProperties(ConfigurationLocator.class));

      initConfigDefaults();

      initOptions();
      parseCommandLine(args);
   }

   public String getApiKey() {
      return _apiKey;
   }

   public String getBatfishArgs() {
	   return _batfishArgs;
   }
   
   public String getCommandFile() {
      return _commandFile;
   }

   public String getClientArgs() {
	   return _clientArgs;
   }
   
   public String getCoordinatorArgs() {
	   return _coordinatorArgs;
   }
   
   public String getLogFile() {
      return _logFile;
   }

   public String getLogLevel() {
      return _logLevel;
   }

   public String getTestrigDir() {
	      return _testrigDir;
   }


   private void initConfigDefaults() {
      setDefaultProperty(ARG_API_KEY, CoordConsts.DEFAULT_API_KEY);
      setDefaultProperty(ARG_COMMAND_FILE, 
            Paths.get(org.batfish.common.Util.getJarOrClassDir(
                  ConfigurationLocator.class).getAbsolutePath(), "default_commands")
                  .toAbsolutePath().toString());
      setDefaultProperty(ARG_HELP, false);
      setDefaultProperty(ARG_LOG_FILE, null);
      setDefaultProperty(ARG_LOG_LEVEL,
            BatfishLogger.getLogLevelStr(BatfishLogger.LEVEL_WARN));
      setDefaultProperty(ARG_BATFISH_ARGS, "-servicemode -register true");
      setDefaultProperty(ARG_CLIENT_ARGS, "-coordinatorhost localhost");
      setDefaultProperty(ARG_COORDINATOR_ARGS, null);
   }

   private void initOptions() {
      addOption(ARG_API_KEY,
            "API key for the coordinator", "apikey");

      addOption(ARG_COMMAND_FILE,
            "read commands from the specified command file", "cmdfile");

      addBooleanOption(ARG_HELP, "print this message");

      addOption(ARG_LOG_FILE, "send output to specified log file", "logfile");

      addOption(ARG_LOG_LEVEL, "log level", "loglevel");

      addOption(ARG_BATFISH_ARGS, "arguments for batfish process",
            "batfish_args");

      addOption(ARG_CLIENT_ARGS, "arguments for the client process",
              "client_args");

      addOption(ARG_COMMAND_FILE, "which command file to use",
            "command_file");

      addOption(ARG_COORDINATOR_ARGS, "arguments for coordinator process",
            "coordinator_args");

      addOption(ARG_TESTRIG_DIR, "where the testrig sits",
            "testrig_dir");
      
   }

   private void parseCommandLine(String[] args) {
      initCommandLine(args);

      if (getBooleanOptionValue(ARG_HELP)) {
         printHelp(EXECUTABLE_NAME);
         System.exit(0);
      }

      _apiKey = getStringOptionValue(ARG_API_KEY);
      _commandFile = getStringOptionValue(ARG_COMMAND_FILE);
      _logFile = getStringOptionValue(ARG_LOG_FILE);
      _logLevel = getStringOptionValue(ARG_LOG_LEVEL);
      _batfishArgs = getStringOptionValue(ARG_BATFISH_ARGS);
      _clientArgs = getStringOptionValue(ARG_CLIENT_ARGS);
      _coordinatorArgs = getStringOptionValue(ARG_COORDINATOR_ARGS);
      _testrigDir = getStringOptionValue(ARG_TESTRIG_DIR);
   }
}
