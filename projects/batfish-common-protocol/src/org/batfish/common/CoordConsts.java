package org.batfish.common;

public class CoordConsts {

   // IMPORTANT:
   // If you change the values of any of these constants,
   // make sure that the javascript code is updated too

   public enum WorkStatusCode {
      ASSIGNED,
      ASSIGNMENTERROR,
      CHECKINGSTATUS,
      TERMINATEDABNORMALLY,
      TERMINATEDNORMALLY,
      TRYINGTOASSIGN,
      UNASSIGNED
   }

   public static final String SVC_BASE_POOL_MGR = "/batfishpoolmgr";

   public static final String SVC_BASE_WORK_MGR = "/batfishworkmgr";
   public static final String SVC_ENV_NAME_KEY = "envname";

   public static final String SVC_FAILURE_KEY = "failure";
   public static final String SVC_FILE_KEY = "file";
   public static final String SVC_FILE2_KEY = "file2";

   public static final String SVC_POOL_GETSTATUS_RSC = "getstatus";
   public static final Integer SVC_POOL_PORT = 9998;

   public static final String SVC_POOL_UPDATE_RSC = "updatepool";
   public static final String SVC_QUESTION_NAME_KEY = "questionname";
   public static final String SVC_SUCCESS_KEY = "success";
   public static final String SVC_TESTRIG_NAME_KEY = "testrigname";
   public static final String SVC_WORK_FILENAME_HDR = "FileName";
   public static final String SVC_WORK_GET_OBJECT_RSC = "getobject";
   public static final String SVC_WORK_GET_WORKSTATUS_RSC = "getworkstatus";

   public static final String SVC_WORK_GETSTATUS_RSC = "getstatus";
   public static final String SVC_WORK_OBJECT_KEY = "object";
   public static final Integer SVC_WORK_PORT = 9997;
   public static final String SVC_WORK_QUEUE_WORK_RSC = "queuework";
   public static final String SVC_WORK_UPLOAD_ENV_RSC = "uploadenvironment";
   public static final String SVC_WORK_UPLOAD_QUESTION_RSC = "uploadquestion";
   public static final String SVC_WORK_UPLOAD_TESTRIG_RSC = "uploadtestrig";
   public static final String SVC_WORKID_KEY = "workid";
   public static final String SVC_WORKITEM_KEY = "workitem";
   public static final String SVC_WORKSPACE_NAME_KEY = "workspace";

   public static final String SVC_WORKSTATUS_KEY = "workstatus";

   public static final String SVC_ZIPFILE_KEY = "zipfile";
}