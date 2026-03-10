package org.batfish.coordinator.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.batfish.common.BaseSettings;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.CoordConsts;
import org.batfish.coordinator.authorizer.Authorizer;
import org.batfish.datamodel.Ip;

public class Settings extends BaseSettings {

  private static final String ARG_AUTHORIZER_TYPE = "authorizertype";
  private static final String ARG_CONTAINERS_LOCATION = "containerslocation";

  /** (the arguments below are not wired to command line) */
  private static final String ARG_FILE_AUTHORIZER_PERMS_FILE = "fileauthpermsfile";

  private static final String ARG_FILE_AUTHORIZER_ROOT_DIR = "fileauthrootdir";
  private static final String ARG_FILE_AUTHORIZER_USERS_FILE = "fileauthusersfile";
  private static final String ARG_HELP = "help";
  private static final String ARG_LOG_LEVEL = "loglevel";
  private static final String ARG_PERIOD_ASSIGN_WORK_MS = "periodassignworkms";
  private static final String ARG_PERIOD_CHECK_WORK_MS = "periodcheckworkms";
  private static final String ARG_PERIOD_WORKER_STATUS_REFRESH_MS = "periodworkerrefreshms";
  private static final String ARG_QUESTION_TEMPLATE_DIRS = "templatedirs";

  public static final String ARG_SERVICE_NAME = "servicename";
  public static final String ARG_SERVICE_WORK_V2_PORT = "workv2port";

  private static final String ARG_WORK_BIND_HOST = "workbindhost";

  private static final String ARGNAME_PATHS = "path..";

  private static final String EXECUTABLE_NAME = "coordinator";

  private Authorizer.Type _authorizerType;
  private Path _containersLocation;
  private Path _fileAuthorizerPermsFile;
  private Path _fileAuthorizerRootDir;
  private Path _fileAuthorizerUsersFile;
  private long _periodAssignWorkMs;
  private List<Path> _questionTemplateDirs;
  private String _serviceName;
  private int _serviceWorkV2Port;
  private String _workBindHost;

  public Settings(String[] args) {
    super(
        getConfig(
            BfConsts.PROP_COORDINATOR_PROPERTIES_PATH,
            BfConsts.ABSPATH_CONFIG_FILE_NAME_COORDINATOR,
            Settings.class));

    initConfigDefaults();
    initOptions();
    parseCommandLine(args);
  }

  public Authorizer.Type getAuthorizationType() {
    return _authorizerType;
  }

  public Path getContainersLocation() {
    return _containersLocation;
  }

  public Path getFileAuthorizerPermsFile() {
    return _fileAuthorizerPermsFile;
  }

  public Path getFileAuthorizerRootDir() {
    return _fileAuthorizerRootDir;
  }

  public Path getFileAuthorizerUsersFile() {
    return _fileAuthorizerUsersFile;
  }

  public long getPeriodAssignWorkMs() {
    return _periodAssignWorkMs;
  }

  public String getServiceName() {
    return _serviceName;
  }

  public int getServiceWorkV2Port() {
    return _serviceWorkV2Port;
  }

  public List<Path> getQuestionTemplateDirs() {
    return _questionTemplateDirs;
  }

  public String getWorkBindHost() {
    return _workBindHost;
  }

  private void initConfigDefaults() {
    setDefaultProperty(ARG_AUTHORIZER_TYPE, Authorizer.Type.none.toString());
    setDefaultProperty(ARG_CONTAINERS_LOCATION, "containers");
    setDefaultProperty(ARG_FILE_AUTHORIZER_PERMS_FILE, "perms.json");
    setDefaultProperty(ARG_FILE_AUTHORIZER_ROOT_DIR, "fileauthorizer");
    setDefaultProperty(ARG_FILE_AUTHORIZER_USERS_FILE, "users.json");
    setDefaultProperty(ARG_HELP, false);
    setDefaultProperty(ARG_LOG_LEVEL, BatfishLogger.getLogLevelStr(BatfishLogger.LEVEL_OUTPUT));
    setDefaultProperty(ARG_PERIOD_ASSIGN_WORK_MS, 100);
    setDefaultProperty(ARG_PERIOD_CHECK_WORK_MS, 100);
    setDefaultProperty(ARG_PERIOD_WORKER_STATUS_REFRESH_MS, 10000);
    setDefaultProperty(ARG_QUESTION_TEMPLATE_DIRS, Collections.emptyList());
    setDefaultProperty(ARG_WORK_BIND_HOST, Ip.ZERO.toString());
    setDefaultProperty(ARG_SERVICE_NAME, "coordinator-service");
    setDefaultProperty(ARG_SERVICE_WORK_V2_PORT, CoordConsts.SVC_CFG_WORK_V2_PORT);
  }

  private void initOptions() {
    addOption(ARG_AUTHORIZER_TYPE, "type of authorizer to use", "authorizer type");

    addOption(ARG_CONTAINERS_LOCATION, "where to store containers", "containers_location");

    addBooleanOption(ARG_HELP, "print this message");

    addOption(ARG_LOG_LEVEL, "log level", "loglevel");

    addOption(
        ARG_PERIOD_WORKER_STATUS_REFRESH_MS,
        "period with which to check worker status (ms)",
        "period_worker_status_refresh_ms");

    addOption(
        ARG_PERIOD_ASSIGN_WORK_MS,
        "period with which to assign work (ms)",
        "period_assign_work_ms");

    addOption(
        ARG_PERIOD_CHECK_WORK_MS, "period with which to check work (ms)", "period_check_work_ms");

    addListOption(
        ARG_QUESTION_TEMPLATE_DIRS, "paths to question template directories", ARGNAME_PATHS);

    addOption(ARG_SERVICE_NAME, "service name", "service_name");

    addOption(
        ARG_WORK_BIND_HOST,
        "hostname for work management service",
        "base url for work management service");

    addOption(
        ARG_SERVICE_WORK_V2_PORT,
        "port for work management service v2",
        "port_number_work_v2_service");

    // deprecated and ignored
    for (String deprecatedStringArg :
        new String[] {
          "dbcacheexpiry",
          "dbconnection",
          "driverclass",
          "logfile",
          "poolbindhost",
          "poolport",
          "qcompletedwork",
          "qincompletework",
          "qtype",
          "sslpoolkeystorefile",
          "sslpoolkeystorepassword",
          "sslpooltruststorefile",
          "sslpooltruststorepassword",
          "sslworkkeystorefile",
          "sslworkkeystorepassword",
          "sslworktruststorefile",
          "sslworktruststorepassword",
          "tracingagenthost",
          "tracingagentport",
          "workport",
        }) {
      addOption(deprecatedStringArg, DEPRECATED_ARG_DESC, "ignored");
    }
    for (String deprecatedBooleanArg :
        new String[] {
          "allowdefaultkeylistings",
          "sslpooltrustallcerts",
          "sslworkdisable",
          "sslworktrustallcerts",
          "sslpooldisable",
          "tracingenable",
          "uselegacyworkmgrv1"
        }) {
      addBooleanOption(deprecatedBooleanArg, DEPRECATED_ARG_DESC);
    }
  }

  private void parseCommandLine(String[] args) {
    initCommandLine(args);

    if (getBooleanOptionValue(ARG_HELP)) {
      printHelp(EXECUTABLE_NAME);
      System.exit(0);
    }

    _authorizerType = Authorizer.Type.valueOf(getStringOptionValue(ARG_AUTHORIZER_TYPE));
    _fileAuthorizerRootDir = Paths.get(getStringOptionValue(ARG_FILE_AUTHORIZER_ROOT_DIR));
    _fileAuthorizerPermsFile = Paths.get(getStringOptionValue(ARG_FILE_AUTHORIZER_PERMS_FILE));
    _fileAuthorizerUsersFile = Paths.get(getStringOptionValue(ARG_FILE_AUTHORIZER_USERS_FILE));
    _questionTemplateDirs = getPathListOptionValue(ARG_QUESTION_TEMPLATE_DIRS);
    _serviceName = getStringOptionValue(ARG_SERVICE_NAME);
    _workBindHost = getStringOptionValue(ARG_WORK_BIND_HOST);
    _serviceWorkV2Port = getIntegerOptionValue(ARG_SERVICE_WORK_V2_PORT);
    _containersLocation = getPathOptionValue(ARG_CONTAINERS_LOCATION);
    _periodAssignWorkMs = getLongOptionValue(ARG_PERIOD_ASSIGN_WORK_MS);
  }

  public void setQuestionTemplateDirs(List<Path> questionTemplateDirs) {
    _questionTemplateDirs = questionTemplateDirs;
  }
}
