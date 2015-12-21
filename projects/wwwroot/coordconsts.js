
// These constants come from 'CoordConsts.java'.
// Make sure they are in sync with what is there.

/**
 * Constants for where and how services are hosted
 */

var SVC_BASE_POOL_MGR = "/batfishpoolmgr";
var SVC_BASE_WORK_MGR = "/batfishworkmgr";
var SVC_POOL_PORT = 9998;
var SVC_WORK_PORT = 9997;
var SVC_DISABLE_SSL = false;
   
/**
 * Various constants used as keys on multi-part form data
 */
   
var SVC_API_KEY = "apikey";
var SVC_CONTAINER_LIST_KEY = "containerlist";
var SVC_CONTAINER_NAME_KEY = "container";
var SVC_CONTAINER_PREFIX_KEY = "containerprefix";
var SVC_ENV_NAME_KEY = "envname";
var SVC_ENVIRONMENT_LIST_KEY = "environmentlist";
var SVC_FAILURE_KEY = "failure";
var SVC_FILE_KEY = "file";
var SVC_FILE2_KEY = "file2";
var SVC_QUESTION_LIST_KEY = "questionlist";
var SVC_QUESTION_NAME_KEY = "questionname";
var SVC_SUCCESS_KEY = "success";
var SVC_TESTRIG_LIST_KEY = "testriglist";
var SVC_TESTRIG_NAME_KEY = "testrigname";
var SVC_FILENAME_HDR = "FileName";
var SVC_OBJECT_KEY = "object";
var SVC_CUSTOM_OBJECT_NAME_KEY = "customobjectname";
var SVC_WORKID_KEY = "workid";
var SVC_WORKITEM_KEY = "workitem";
var SVC_WORKSPACE_NAME_KEY = "workspace";
var SVC_WORKSTATUS_KEY = "workstatus";
var SVC_ZIPFILE_KEY = "zipfile";

/**
 * Constants for endpoints of various service calls
 */   
   
var SVC_DEL_CONTAINER_RSC = "delcontainer";
var SVC_DEL_ENVIRONMENT_RSC = "delenvironment";
var SVC_DEL_QUESTION_RSC = "delquestion";
var SVC_DEL_TESTRIG_RSC = "deltestrig";
var SVC_INIT_CONTAINER_RSC = "initcontainer";
var SVC_LIST_CONTAINERS_RSC = "listcontainers";
var SVC_LIST_ENVIRONMENTS_RSC = "listenvironments";
var SVC_LIST_QUESTIONS_RSC = "listquestions";
var SVC_LIST_TESTRIGS_RSC = "listtestrigs";
var SVC_POOL_GETSTATUS_RSC = "getstatus";
var SVC_POOL_UPDATE_RSC = "updatepool";
var SVC_QUEUE_WORK_RSC = "queuework";
var SVC_UPLOAD_CUSTOM_OBJECT_RSC = "uploadcustomobject";
var SVC_UPLOAD_ENV_RSC = "uploadenvironment";
var SVC_UPLOAD_QUESTION_RSC = "uploadquestion";
var SVC_UPLOAD_TESTRIG_RSC = "uploadtestrig";
var SVC_GET_OBJECT_RSC = "getobject";
var SVC_GET_WORKSTATUS_RSC = "getworkstatus";
var SVC_GETSTATUS_RSC = "getstatus";
