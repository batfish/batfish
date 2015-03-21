package org.batfish.common;

public class CoordinatorConstants {
   
   public static final String SERVICE_BASE_POOL_MGR = "/batfishpoolmgr";
   public static final String SERVICE_BASE_WORK_MGR = "/batfishworkmgr";
   
   public static final String SERVICE_POOL_GETSTATUS_RESOURCE = "getstatus";
   public static final String SERVICE_POOL_UPDATE_RESOURCE = "updatepool";  

   public static final String SERVICE_WORK_GET_WORK_QUEUE_STATUS_RESOURCE = "getworkqueuestatus";  
   public static final String SERVICE_WORK_UPLOAD_TESTRIG_RESOURCE = "uploadtestrig";
   public static final String SERVICE_WORK_QUEUE_WORK_RESOURCE = "queuework";
   public static final String SERVICE_WORK_GET_WORK_STATUS_RESOURCE = "getworkstatus";
   public static final String SERVICE_WORK_GET_OBJECT_RESOURCE = "getobject";

   public static final String SERVICE_WORK_QUEUE_WORK_PATH = "workitem";
   
   public static final String SERVICE_WORK_GET_OBJECT_KEY = "object";
   public static final String SERVICE_WORKID_KEY = "workid";
   public static final String SERVICE_TESTRIG_NAME_KEY = "testrigname";
   public static final String SERVICE_TESTRIG_ZIPFILE_KEY = "testrigzipfile"; 
   public static final String SERVICE_COMMAND_KEY = "command";
   public static final String SERVICE_COMMAND_PARSE_KEY = "parse";
   public static final String SERVICE_WORKSPACE_NAME_KEY = "workspace";
   
   public enum WorkStatusCode {
      UNASSIGNED,
      TRYINGTOASSIGN,
      ASSIGNED,
      CHECKINGSTATUS,
      TERMINATEDNORMALLY,
      TERMINATEDABNORMALLY
   }
}