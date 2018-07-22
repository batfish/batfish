package org.batfish.allinone.config;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.batfish.common.BaseSettings;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.Version;
import org.batfish.common.util.CommonUtil;
import org.batfish.main.Driver;
import org.batfish.main.Driver.RunMode;

public class Settings extends BaseSettings {

  private static final String ARG_BATFISH_ARGS = "batfishargs";
  private static final String ARG_BATFISH_RUN_MODE = "batfishmode";
  private static final String ARG_CLIENT_ARGS = "clientargs";
  private static final String ARG_COMMAND_FILE =
      org.batfish.client.config.Settings.ARG_COMMAND_FILE;
  private static final String ARG_COORDINATOR_ARGS = "coordinatorargs";
  private static final String ARG_HELP = "help";
  private static final String ARG_LOG_FILE = "logfile";
  private static final String ARG_LOG_LEVEL = "loglevel";
  private static final String ARG_RUN_CLIENT = "runclient";
  private static final String ARG_RUN_MODE = org.batfish.client.config.Settings.ARG_RUN_MODE;
  public static final String ARG_SERVICE_NAME = "servicename";
  private static final String ARG_TESTRIG_DIR = org.batfish.client.config.Settings.ARG_TESTRIG_DIR;
  private static final String ARG_TRACING_AGENT_HOST = "tracingagenthost";
  private static final String ARG_TRACING_AGENT_PORT = "tracingagentport";
  private static final String ARG_TRACING_ENABLE = "tracingenable";
  private static final String ARG_VERSION = "version";

  private static final String EXECUTABLE_NAME = "allinone";

  private String _batfishArgs;
  private Driver.RunMode _batfishRunMode;
  private String _clientArgs;
  private String _commandFile;
  private String _coordinatorArgs;
  private String _logFile;
  private String _logLevel;
  private boolean _runClient;
  private String _runMode;
  private String _serviceName;
  private String _testrigDir;
  private String _tracingAgentHost;
  private Integer _tracingAgentPort;
  private boolean _tracingEnable;

  public Settings(String[] args) {
    super(
        CommonUtil.getConfig(
            BfConsts.PROP_ALLINONE_PROPERTIES_PATH,
            BfConsts.ABSPATH_CONFIG_FILE_NAME_ALLINONE,
            ConfigurationLocator.class));

    initConfigDefaults();

    initOptions();
    parseCommandLine(args);
  }

  public String getBatfishArgs() {
    return _batfishArgs;
  }

  public RunMode getBatfishRunMode() {
    return _batfishRunMode;
  }

  public String getClientArgs() {
    return _clientArgs;
  }

  public String getCommandFile() {
    return _commandFile;
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

  public boolean getRunClient() {
    return _runClient;
  }

  public String getRunMode() {
    return _runMode;
  }

  public String getServiceName() {
    return _serviceName;
  }

  public String getTestrigDir() {
    return _testrigDir;
  }

  public Integer getTracingAgentPort() {
    return _tracingAgentPort;
  }

  public String getTracingAgentHost() {
    return _tracingAgentHost;
  }

  public boolean getTracingEnable() {
    return _tracingEnable;
  }

  private void initConfigDefaults() {
    // setDefaultProperty(ARG_COMMAND_FILE,
    // Paths.get(org.batfish.common.Util.getJarOrClassDir(
    // ConfigurationLocator.class).getAbsolutePath(), "default_commands")
    // .toAbsolutePath().toString());
    setDefaultProperty(ARG_HELP, false);
    setDefaultProperty(ARG_LOG_FILE, null);
    setDefaultProperty(ARG_LOG_LEVEL, BatfishLogger.getLogLevelStr(BatfishLogger.LEVEL_OUTPUT));
    setDefaultProperty(ARG_BATFISH_ARGS, "");
    setDefaultProperty(ARG_BATFISH_RUN_MODE, RunMode.WORKSERVICE.toString());
    setDefaultProperty(ARG_CLIENT_ARGS, "");
    setDefaultProperty(ARG_COORDINATOR_ARGS, "");
    setDefaultProperty(ARG_RUN_CLIENT, true);
    setDefaultProperty(ARG_RUN_MODE, "batch");
    setDefaultProperty(ARG_SERVICE_NAME, "allinone-service");
    setDefaultProperty(ARG_TRACING_AGENT_HOST, "localhost");
    setDefaultProperty(ARG_TRACING_AGENT_PORT, 5775);
    setDefaultProperty(ARG_TRACING_ENABLE, false);
    setDefaultProperty(ARG_VERSION, false);
  }

  private void initOptions() {
    addOption(ARG_COMMAND_FILE, "read commands from the specified command file", "cmdfile");

    addBooleanOption(ARG_HELP, "print help message and exit");

    addOption(ARG_LOG_FILE, "send output to specified log file", "logfile");

    addOption(ARG_LOG_LEVEL, "log level", "loglevel");

    addOption(ARG_BATFISH_ARGS, "arguments for batfish process", "batfish_args");

    addOption(
        ARG_BATFISH_RUN_MODE,
        "mode in which to start batfish",
        Arrays.stream(RunMode.values())
            .filter(v -> v != RunMode.WORKER)
            .map(Object::toString)
            .collect(Collectors.joining("|")));

    addOption(ARG_CLIENT_ARGS, "arguments for the client process", "client_args");

    addOption(ARG_COMMAND_FILE, "which command file to use", "command_file");

    addOption(ARG_COORDINATOR_ARGS, "arguments for coordinator process", "coordinator_args");

    addBooleanOption(ARG_RUN_CLIENT, "whether to run the client");

    addOption(ARG_RUN_MODE, "which mode to run in (batch|interactive)", "run_mode");

    addOption(ARG_SERVICE_NAME, "service name", "service_name");

    addOption(ARG_TESTRIG_DIR, "where the testrig sits", "testrig_dir");

    addOption(ARG_TRACING_AGENT_HOST, "jaeger agent host", "jaeger_agent_host");

    addOption(ARG_TRACING_AGENT_PORT, "jaeger agent port", "jaeger_agent_port");

    addBooleanOption(ARG_TRACING_ENABLE, "enable tracing");

    addBooleanOption(ARG_VERSION, "print the version number of the code and exit");
  }

  private void parseCommandLine(String[] args) {
    initCommandLine(args);

    if (getBooleanOptionValue(ARG_HELP)) {
      printHelp(EXECUTABLE_NAME);
      System.exit(0);
    }

    if (getBooleanOptionValue(ARG_VERSION)) {
      System.out.print(Version.getCompleteVersionString());
      System.exit(0);
    }

    _commandFile = getStringOptionValue(ARG_COMMAND_FILE);
    _logFile = getStringOptionValue(ARG_LOG_FILE);
    _logLevel = getStringOptionValue(ARG_LOG_LEVEL);
    _batfishArgs = getStringOptionValue(ARG_BATFISH_ARGS);
    _batfishRunMode = RunMode.valueOf(getStringOptionValue(ARG_BATFISH_RUN_MODE).toUpperCase());
    if (_batfishRunMode == RunMode.WORKER) {
      throw new IllegalArgumentException("Cannot start batfish in worker mode");
    }
    _clientArgs = getStringOptionValue(ARG_CLIENT_ARGS);
    _coordinatorArgs = getStringOptionValue(ARG_COORDINATOR_ARGS);
    _runClient = getBooleanOptionValue(ARG_RUN_CLIENT);
    _runMode = getStringOptionValue(ARG_RUN_MODE);
    _serviceName = getStringOptionValue(ARG_SERVICE_NAME);
    _testrigDir = getStringOptionValue(ARG_TESTRIG_DIR);
    _tracingAgentHost = getStringOptionValue(ARG_TRACING_AGENT_HOST);
    _tracingAgentPort = getIntegerOptionValue(ARG_TRACING_AGENT_PORT);
    _tracingEnable = getBooleanOptionValue(ARG_TRACING_ENABLE);
  }
}
