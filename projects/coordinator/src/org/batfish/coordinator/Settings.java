package org.batfish.coordinator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.batfish.common.CoordConsts;

public class Settings {

   private static final String ARG_INITIAL_WORKER = "initialworker";

   private static final String ARG_LOG_FILE = "logfile";
   private static final String ARG_PERIOD_ASSIGN_WORK = "periodassignwork";
   private static final String ARG_PERIOD_CHECK_WORK = "periodcheckwork";
   private static final String ARG_PERIOD_WORKER_STATUS_REFRESH = "periodworkerrefresh";
   private static final String ARG_QUEUE_COMPLETED_WORK = "q_completedwork";
   private static final String ARG_QUEUE_INCOMPLETE_WORK = "q_incompletework";
   private static final String ARG_QUEUE_TYPE = "qtype";
   private static final String ARG_SERVICE_HOST = "servicehost";
   private static final String ARG_SERVICE_POOL_PORT = "servicepoolport";
   private static final String ARG_SERVICE_WORK_PORT = "serviceworkport";
   private static final String ARG_STORAGE_ACCOUNT_KEY = "storageaccountkey";

   private static final String ARG_STORAGE_ACCOUNT_NAME = "storageaccountname";
   private static final String ARG_STORAGE_PROTOCOL = "storageprotocol";
   private static final String ARG_TESTRIG_STORAGE_LOCATION = "testrigstorage";

   private static final String DEFAULT_INITIAL_WORKER = "";
   private static final String DEFAULT_PERIOD_ASSIGN_WORK = "1000"; // 1 seconds
   private static final String DEFAULT_PERIOD_CHECK_WORK = "5000"; // 5 seconds
   private static final String DEFAULT_PERIOD_WORKER_STATUS_REFRESH = "10000"; // 10
                                                                               // seconds
   private static final String DEFAULT_QUEUE_COMPLETED_WORK = "batfishcompletedwork";
   private static final String DEFAULT_QUEUE_INCOMPLETE_WORK = "batfishincompletework";
   private static final String DEFAULT_QUEUE_TYPE = "memory";
   private static final String DEFAULT_SERVICE_HOST = "0.0.0.0";
   private static final String DEFAULT_SERVICE_POOL_PORT = CoordConsts.SVC_POOL_PORT
         .toString();
   private static final String DEFAULT_SERVICE_WORK_PORT = CoordConsts.SVC_WORK_PORT
         .toString();
   private static final String DEFAULT_STORAGE_ACCOUNT_KEY = "zRTT++dVryOWXJyAM7NM0TuQcu0Y23BgCQfkt7xh2f/Mm+r6c8/XtPTY0xxaF6tPSACJiuACsjotDeNIVyXM8Q==";

   private static final String DEFAULT_STORAGE_ACCOUNT_NAME = "testdrive";
   private static final String DEFAULT_STORAGE_PROTOCOL = "http";
   private static final String DEFAULT_TESTRIG_STORAGE_LOCATION = ".";

   private String _initialWorker;
   private String _logFile;
   private Options _options;
   private long _periodAssignWorkMs;
   private long _periodCheckWorkMs;
   private long _periodWorkerStatusRefreshMs;
   private String _queueCompletedWork;
   private WorkQueue.Type _queueType;
   private String _queuIncompleteWork;
   private String _serviceHost;
   private int _servicePoolPort;
   private int _serviceWorkPort;

   private String _storageAccountKey;
   private String _storageAccountName;
   private String _storageProtocol;
   private String _testrigStorageLocation;

   public Settings() throws ParseException {
      this(new String[] {});
   }

   public Settings(String[] args) throws ParseException {
      initOptions();
      parseCommandLine(args);
   }

   public String getInitialWorker() {
      return _initialWorker;
   }

   public String getLogFile() {
      return _logFile;
   }

   public long getPeriodAssignWorkMs() {
      return _periodAssignWorkMs;
   }

   public long getPeriodCheckWorkMs() {
      return _periodCheckWorkMs;
   }

   public long getPeriodWorkerStatusRefreshMs() {
      return _periodWorkerStatusRefreshMs;
   }

   public String getQueueCompletedWork() {
      return _queueCompletedWork;
   }

   public String getQueueIncompleteWork() {
      return _queuIncompleteWork;
   }

   public WorkQueue.Type getQueueType() {
      return _queueType;
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

   public String getStorageAccountKey() {
      return _storageAccountKey;
   }

   public String getStorageAccountName() {
      return _storageAccountName;
   }

   public String getStorageProtocol() {
      return _storageProtocol;
   }

   public String getTestrigStorageLocation() {
      return _testrigStorageLocation;
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
      _options.addOption(Option.builder().argName("qtype").hasArg()
            .desc("queue type to use {azure, memory}").longOpt(ARG_QUEUE_TYPE)
            .build());
      _options.addOption(Option.builder().argName("testrig_storage_location")
            .hasArg().desc("where to store test rigs")
            .longOpt(ARG_TESTRIG_STORAGE_LOCATION).build());
      _options.addOption(Option.builder()
            .argName("period_worker_status_refresh_ms").hasArg()
            .desc("period with which to check worker status (ms)")
            .longOpt(ARG_PERIOD_WORKER_STATUS_REFRESH).build());
      _options.addOption(Option.builder().argName("period_assign_work_ms")
            .hasArg().desc("period with which to assign work (ms)")
            .longOpt(ARG_PERIOD_ASSIGN_WORK).build());
      _options.addOption(Option.builder().argName("period_check_work_ms")
            .hasArg().desc("period with which to check work (ms)")
            .longOpt(ARG_PERIOD_CHECK_WORK).build());
      _options.addOption(Option.builder().argName("path").hasArg()
            .desc("send output to specified log file").longOpt(ARG_LOG_FILE)
            .build());
      _options.addOption(Option.builder().argName("location_initial_worker")
            .hasArg().desc("location of initial worker (host:port)")
            .longOpt(ARG_INITIAL_WORKER).build());
   }

   private void parseCommandLine(String[] args) throws ParseException {
      CommandLine line = null;
      CommandLineParser parser = new DefaultParser();

      // parse the command line arguments
      line = parser.parse(_options, args);

      _queuIncompleteWork = line.getOptionValue(ARG_QUEUE_INCOMPLETE_WORK,
            DEFAULT_QUEUE_INCOMPLETE_WORK);
      _queueCompletedWork = line.getOptionValue(ARG_QUEUE_COMPLETED_WORK,
            DEFAULT_QUEUE_COMPLETED_WORK);

      _queueType = WorkQueue.Type.valueOf(line.getOptionValue(ARG_QUEUE_TYPE,
            DEFAULT_QUEUE_TYPE));

      _servicePoolPort = Integer.parseInt(line.getOptionValue(
            ARG_SERVICE_POOL_PORT, DEFAULT_SERVICE_POOL_PORT));
      _serviceWorkPort = Integer.parseInt(line.getOptionValue(
            ARG_SERVICE_WORK_PORT, DEFAULT_SERVICE_WORK_PORT));
      _serviceHost = line
            .getOptionValue(ARG_SERVICE_HOST, DEFAULT_SERVICE_HOST);

      _storageAccountKey = line.getOptionValue(ARG_STORAGE_ACCOUNT_KEY,
            DEFAULT_STORAGE_ACCOUNT_KEY);
      _storageAccountName = line.getOptionValue(ARG_STORAGE_ACCOUNT_NAME,
            DEFAULT_STORAGE_ACCOUNT_NAME);
      _storageProtocol = line.getOptionValue(ARG_STORAGE_PROTOCOL,
            DEFAULT_STORAGE_PROTOCOL);

      _testrigStorageLocation = line.getOptionValue(
            ARG_TESTRIG_STORAGE_LOCATION, DEFAULT_TESTRIG_STORAGE_LOCATION);

      _periodWorkerStatusRefreshMs = Long.parseLong(line.getOptionValue(
            ARG_PERIOD_WORKER_STATUS_REFRESH,
            DEFAULT_PERIOD_WORKER_STATUS_REFRESH));
      _periodAssignWorkMs = Long.parseLong(line.getOptionValue(
            ARG_PERIOD_ASSIGN_WORK, DEFAULT_PERIOD_ASSIGN_WORK));
      _periodCheckWorkMs = Long.parseLong(line.getOptionValue(
            ARG_PERIOD_CHECK_WORK, DEFAULT_PERIOD_CHECK_WORK));

      _initialWorker = line.getOptionValue(ARG_INITIAL_WORKER,
            DEFAULT_INITIAL_WORKER);

      _logFile = line.getOptionValue(ARG_LOG_FILE);
   }
}
