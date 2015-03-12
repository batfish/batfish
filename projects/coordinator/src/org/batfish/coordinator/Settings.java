package org.batfish.coordinator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Settings {

   private static final String ARG_QUEUE_ASSIGNED_WORK = "q_assignedwork";
   private static final String ARG_QUEUE_COMPLETED_WORK = "q_completedwork";
   private static final String ARG_QUEUE_TYPE = "qtype";
   private static final String ARG_QUEUE_UNASSIGNED_WORK = "q_unassignedwork";
   private static final String ARG_SERVICE_PORT = "serviceport";
   private static final String ARG_SERVICE_URL = "serviceurl";
   private static final String ARG_STORAGE_ACCOUNT_KEY = "storageaccountkey";
   private static final String ARG_STORAGE_ACCOUNT_NAME = "storageaccountname";
   private static final String ARG_STORAGE_PROTOCOL = "storageprotocol";


   private static final String DEFAULT_QUEUE_ASSIGNED_WORK = "batfishassignedwork";
   private static final String DEFAULT_QUEUE_COMPLETED_WORK = "batfishcompletedwork";
   private static final String DEFAULT_QUEUE_TYPE = "memory";
   private static final String DEFAULT_QUEUE_UNASSIGNED_WORK = "batfishunassignedwork";
   private static final String DEFAULT_SERVICE_PORT = "9998";
   private static final String DEFAULT_SERVICE_URL = "http://localhost";
   private static final String DEFAULT_STORAGE_ACCOUNT_KEY = "zRTT++dVryOWXJyAM7NM0TuQcu0Y23BgCQfkt7xh2f/Mm+r6c8/XtPTY0xxaF6tPSACJiuACsjotDeNIVyXM8Q==";
   private static final String DEFAULT_STORAGE_ACCOUNT_NAME = "testdrive";
   private static final String DEFAULT_STORAGE_PROTOCOL = "http";

   private Options _options;
   private String _queueAssignedWork;
   private String _queueCompletedWork;
   private WorkQueue.Type _queueType;
   private String _queueUnassignedWork;
   private int _servicePort;
   private String _serviceUrl;
   private String _storageAccountName;
   private String _storageAccountKey;
   private String _storageProtocol;

   public Settings() throws ParseException {
      this(new String[] {});
   }

   public Settings(String[] args) throws ParseException {
      initOptions();
      parseCommandLine(args);
   }

   public String getQueueAssignedWork() {
      return _queueAssignedWork;
   }

   public String getQueueCompletedWork() {
      return _queueCompletedWork;
   }

   public WorkQueue.Type getQueueType() {
      return _queueType;
   }

   public String getQueueUnassignedWork() {
      return _queueUnassignedWork;
   }
   
   public int getServicePort() {
      return _servicePort;
   }
   
   public String getServiceUrl() {
      return _serviceUrl;
   }

   public String getStorageAccountName() {
      return _storageAccountName;
   }

   public String getStorageAccountKey() {
      return _storageAccountKey;
   }

   public String getStorageProtocol() {
      return _storageProtocol;
   }

   private void initOptions() {
      _options = new Options();
      _options.addOption(Option.builder().argName("port_number").hasArg()
            .desc("port for batfish service")
            .longOpt(ARG_SERVICE_PORT).build());
      _options.addOption(Option.builder().argName("base_url").hasArg()
            .desc("base url for coordinator service")
            .longOpt(ARG_SERVICE_URL).build());
      _options.addOption(Option.builder().argName("qtype").hasArg()
            .desc("queue type to use {azure, memory}")
            .longOpt(ARG_QUEUE_TYPE).build());
   }

   private void parseCommandLine(String[] args) throws ParseException {
      CommandLine line = null;
      CommandLineParser parser = new DefaultParser();

      // parse the command line arguments
      line = parser.parse(_options, args);

      _queueAssignedWork = line.getOptionValue(ARG_QUEUE_ASSIGNED_WORK, DEFAULT_QUEUE_ASSIGNED_WORK);
      _queueCompletedWork = line.getOptionValue(ARG_QUEUE_COMPLETED_WORK, DEFAULT_QUEUE_COMPLETED_WORK);
      _queueType = WorkQueue.Type.valueOf(line.getOptionValue(ARG_QUEUE_TYPE, DEFAULT_QUEUE_TYPE));
      _queueUnassignedWork = line.getOptionValue(ARG_QUEUE_UNASSIGNED_WORK, DEFAULT_QUEUE_UNASSIGNED_WORK);
      _servicePort = Integer.parseInt(line.getOptionValue(ARG_SERVICE_PORT, DEFAULT_SERVICE_PORT));
      _serviceUrl = line.getOptionValue(ARG_SERVICE_URL, DEFAULT_SERVICE_URL);
      _storageAccountKey = line.getOptionValue(ARG_STORAGE_ACCOUNT_KEY, DEFAULT_STORAGE_ACCOUNT_KEY);
      _storageAccountName = line.getOptionValue(ARG_STORAGE_ACCOUNT_NAME, DEFAULT_STORAGE_ACCOUNT_NAME);
      _storageProtocol = line.getOptionValue(ARG_STORAGE_PROTOCOL, DEFAULT_STORAGE_PROTOCOL);
   }
}
