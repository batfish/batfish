package org.batfish.common;

// IMPORTANT:
// If you change the values of any of these constants,
// make sure that the javascript, python clients is updated too

public class CoordConsts {

   public enum WorkStatusCode {
      ASSIGNED,
      ASSIGNMENTERROR,
      CHECKINGSTATUS,
      TERMINATEDABNORMALLY,
      TERMINATEDNORMALLY,
      TRYINGTOASSIGN,
      UNASSIGNED;
   }

   public static final String DEFAULT_API_KEY = "00000000000000000000000000000000";
   /**
    * Various constants used as keys on multi-part form data
    */
   public static final String SVC_ADD_WORKER_KEY = "addworker";
   /**
    * Constants for endpoints of various service calls
    */

   public static final String SVC_ANALYSIS_LIST_KEY = "analysislist";
   public static final String SVC_ANALYSIS_NAME_KEY = "analysisname";
   public static final String SVC_ANSWER_KEY = "answer";
   public static final String SVC_ANSWERS_KEY = "answers";
   public static final String SVC_API_KEY = "apikey";

   /**
    * Constants for where and how services are hosted
    */
   public static final String SVC_BASE_POOL_MGR = "/batfishpoolmgr";
   public static final String SVC_BASE_WORK_MGR = "/batfishworkmgr";
   public static final String SVC_CHECK_API_KEY_RSC = "checkapikey";
   public static final String SVC_CONFIGURE_ANALYSIS_RSC = "configureanalysis";
   public static final String SVC_CONTAINER_LIST_KEY = "containerlist";
   public static final String SVC_CONTAINER_NAME_KEY = "container";
   public static final String SVC_CONTAINER_PREFIX_KEY = "containerprefix";
   public static final String SVC_DEL_ANALYSIS_QUESTIONS_KEY = "delanalysisquestions";
   public static final String SVC_DEL_ANALYSIS_RSC = "delanalysis";
   public static final String SVC_DEL_CONTAINER_RSC = "delcontainer";
   public static final String SVC_DEL_ENVIRONMENT_RSC = "delenvironment";
   public static final String SVC_DEL_QUESTION_RSC = "delquestion";
   public static final String SVC_DEL_TESTRIG_RSC = "deltestrig";
   public static final String SVC_DEL_WORKER_KEY = "delworker";
   public static final String SVC_DELTA_ENV_NAME_KEY = "deltaenvname";
   public static final String SVC_DELTA_TESTRIG_NAME_KEY = "deltatestrigname";
   public static final boolean SVC_DISABLE_SSL = false;
   public static final String SVC_ENV_NAME_KEY = "envname";
   public static final String SVC_ENVIRONMENT_LIST_KEY = "environmentlist";
   public static final String SVC_FAILURE_KEY = "failure";
   public static final String SVC_FILE_KEY = "file";
   public static final String SVC_FILE2_KEY = "file2";
   public static final String SVC_FILENAME_HDR = "FileName";
   public static final String SVC_GET_ANALYSIS_ANSWERS_RSC = "getanalysisanswers";
   public static final String SVC_GET_ANSWER_RSC = "getanswer";
   public static final String SVC_GET_OBJECT_RSC = "getobject";
   public static final String SVC_GET_WORKSTATUS_RSC = "getworkstatus";
   public static final String SVC_GETSTATUS_RSC = "getstatus";
   public static final String SVC_INIT_CONTAINER_RSC = "initcontainer";
   public static final String SVC_LIST_ANALYSES_RSC = "listanalyses";
   public static final String SVC_LIST_CONTAINERS_RSC = "listcontainers";
   public static final String SVC_LIST_ENVIRONMENTS_RSC = "listenvironments";
   public static final String SVC_LIST_QUESTIONS_RSC = "listquestions";
   public static final String SVC_LIST_TESTRIGS_RSC = "listtestrigs";
   public static final String SVC_NEW_ANALYSIS_KEY = "newanalysis";
   public static final String SVC_OBJECT_NAME_KEY = "objectname";
   public static final String SVC_POOL_GETSTATUS_RSC = "getstatus";
   public static final Integer SVC_POOL_PORT = 9998;
   public static final String SVC_POOL_UPDATE_RSC = "updatepool";
   public static final String SVC_PRETTY_ANSWER_KEY = "prettyanswer";
   public static final String SVC_PUT_OBJECT_RSC = "putobject";
   public static final String SVC_QUESTION_LIST_KEY = "questionlist";
   public static final String SVC_QUESTION_NAME_KEY = "questionname";
   public static final String SVC_QUEUE_WORK_RSC = "queuework";
   public static final String SVC_SUCCESS_KEY = "success";
   public static final String SVC_TASKSTATUS_KEY = "taskstatus";
   public static final String SVC_TESTRIG_INFO_KEY = "testriginfo";
   public static final String SVC_TESTRIG_LIST_KEY = "testriglist";
   public static final String SVC_TESTRIG_NAME_KEY = "testrigname";
   public static final String SVC_UPLOAD_ENV_RSC = "uploadenvironment";
   public static final String SVC_UPLOAD_QUESTION_RSC = "uploadquestion";
   public static final String SVC_UPLOAD_TESTRIG_RSC = "uploadtestrig";
   public static final String SVC_VERSION_KEY = "version";
   public static final Integer SVC_WORK_PORT = 9997;
   public static final String SVC_WORKID_KEY = "workid";
   public static final String SVC_WORKITEM_KEY = "workitem";
   public static final String SVC_WORKSPACE_NAME_KEY = "workspace";
   public static final String SVC_WORKSTATUS_KEY = "workstatus";
   public static final String SVC_ZIPFILE_KEY = "zipfile";

}
