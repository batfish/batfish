package org.batfish.common;

public class BfConsts {
   
   public enum TaskStatus {Unscheduled, InProgress, TerminatedNormally, TerminatedAbnormally, Unknown, UnreachableOrBadResponse}

   public static final String SVC_BASE_RSC = "/batfishservice";
   public static final Integer SVC_PORT = 9999;

   public static final String SVC_SUCCESS_KEY = "success";
   public static final String SVC_FAILURE_KEY = "failure";

   public static final String SVC_GET_STATUS_RSC = "getstatus";
   public static final String SVC_RUN_TASK_RSC = "run";
   public static final String SVC_GET_TASKSTATUS_RSC = "gettaskstatus";
   
   public static final String SVC_TASKID_KEY = "taskid";
   public static final String SVC_TASK_KEY = "task";
}