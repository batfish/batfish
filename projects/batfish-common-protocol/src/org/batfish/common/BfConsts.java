package org.batfish.common;

public class BfConsts {

   public enum TaskStatus {
      InProgress,
      TerminatedAbnormally,
      TerminatedNormally,
      Unknown,
      UnreachableOrBadResponse,
      Unscheduled
   }

   public static final String RELPATH_FACT_DUMP_DIR = "dump";
   public static final String RELPATH_TEST_RIG_DIR = "testrig";
   public static final String RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR = "indep";
   public static final String RELPATH_VENDOR_SPECIFIC_CONFIG_DIR = "vendor";

   public static final String SVC_BASE_RSC = "/batfishservice";
   public static final String SVC_FAILURE_KEY = "failure";

   public static final String SVC_GET_STATUS_RSC = "getstatus";
   public static final String SVC_GET_TASKSTATUS_RSC = "gettaskstatus";

   public static final Integer SVC_PORT = 9999;
   public static final String SVC_RUN_TASK_RSC = "run";
   public static final String SVC_SUCCESS_KEY = "success";

   public static final String SVC_TASK_KEY = "task";
   public static final String SVC_TASKID_KEY = "taskid";
   
   public static final String COMMAND_PARSE_VENDOR_SPECIFIC = "sv";
   public static final String COMMAND_PARSE_VENDOR_INDEPENDENT = "si";
   public static final String COMMAND_GENERATE_FACT = "dumpcp";

}
