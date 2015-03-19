package org.batfish.common;

public class BatfishConstants {
   
   public enum WorkStatus {Unscheduled, InProgress, TerminatedNormally, TerminatedAbnormally, Unknown}

   public static final String SERVICE_BASE_RESOURCE = "/batfishservice";
   public static final String SERVICE_GETSTATUS_RESOURCE = "getstatus";
   public static final String SERVICE_RUN_RESOURCE = "run";
   public static final String SERVICE_GETWORKSTATUS_RESOURCE = "getworkstatus";
   
   public static final String SERVICE_WORKID_KEY = "workid";
}