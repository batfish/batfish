package org.batfish.common;

public class BatfishConstants {
   
   public enum TaskkStatus {Unscheduled, InProgress, TerminatedNormally, TerminatedAbnormally, Unknown}

   public static final String SERVICE_BASE_RESOURCE = "/batfishservice";
   public static final String SERVICE_GETSTATUS_RESOURCE = "getstatus";
   public static final String SERVICE_RUN_RESOURCE = "run";
   public static final String SERVICE_GETTASKSTATUS_RESOURCE = "gettaskstatus";
   
   public static final String SERVICE_TASKID_KEY = "taskid";
}