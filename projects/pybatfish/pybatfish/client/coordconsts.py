#
# IMPORTANT:
# This class was derived from CoordConsts.java in batfish-common
# make sure that the two (and javascript client) are in sync
#
class CoordConsts:
    
    class WorkStatusCode:
        ASSIGNED="ASSIGNED"
        ASSIGNMENTERROR="ASSIGNMENTERROR"
        CHECKINGSTATUS="CHECKINGSTATUS"
        TERMINATEDABNORMALLY="TERMINATEDABNORMALLY"
        TERMINATEDNORMALLY="TERMINATEDNORMALLY"
        TRYINGTOASSIGN="TRYINGTOASSIGN"
        UNASSIGNED="UNASSIGNED"
      
    DEFAULT_API_KEY = "00000000000000000000000000000000";
    '''
     * Various constants used as keys on multi-part form data
    '''
    SVC_API_KEY = "apikey";
    '''
    * Constants for where and how services are hosted
    '''
    SVC_BASE_POOL_MGR = "/batfishpoolmgr";
    SVC_BASE_WORK_MGR = "/batfishworkmgr";

    '''
    * Constants for endpoints of various service calls
    '''

    SVC_CHECK_API_KEY_RSC = "checkapikey";

    SVC_CONTAINER_LIST_KEY = "containerlist";

    SVC_CONTAINER_NAME_KEY = "container";
    SVC_CONTAINER_PREFIX_KEY = "containerprefix";
    SVC_DEL_CONTAINER_RSC = "delcontainer";
    SVC_DEL_ENVIRONMENT_RSC = "delenvironment";
    SVC_DEL_QUESTION_RSC = "delquestion";
    SVC_DEL_TESTRIG_RSC = "deltestrig";
    SVC_DISABLE_SSL = False;
    SVC_ENV_NAME_KEY = "envname";
    SVC_ENVIRONMENT_LIST_KEY = "environmentlist";
    SVC_FAILURE_KEY = "failure";
    SVC_FILE_KEY = "file";
    SVC_FILE2_KEY = "file2";
    SVC_FILENAME_HDR = "FileName";
    SVC_GET_OBJECT_RSC = "getobject";
    SVC_GET_WORKSTATUS_RSC = "getworkstatus";
    SVC_GETSTATUS_RSC = "getstatus";
    SVC_INIT_CONTAINER_RSC = "initcontainer";
    SVC_LIST_CONTAINERS_RSC = "listcontainers";
    SVC_LIST_ENVIRONMENTS_RSC = "listenvironments";
    SVC_LIST_QUESTIONS_RSC = "listquestions";
    SVC_LIST_TESTRIGS_RSC = "listtestrigs";

    SVC_OBJECT_NAME_KEY = "objectname";

    SVC_POOL_GETSTATUS_RSC = "getstatus";
    SVC_POOL_PORT = 9998;
    SVC_POOL_UPDATE_RSC = "updatepool";
    SVC_PUT_OBJECT_RSC = "putobject";
    SVC_QUESTION_LIST_KEY = "questionlist";
    SVC_QUESTION_NAME_KEY = "questionname";
    SVC_QUEUE_WORK_RSC = "queuework";
    SVC_SUCCESS_KEY = "success";
    SVC_TESTRIG_INFO_KEY = "testriginfo";
    SVC_TESTRIG_LIST_KEY = "testriglist";
    SVC_TESTRIG_NAME_KEY = "testrigname";
    SVC_UPLOAD_ENV_RSC = "uploadenvironment";
    SVC_UPLOAD_QUESTION_RSC = "uploadquestion";
    SVC_UPLOAD_TESTRIG_RSC = "uploadtestrig";
    SVC_VERSION_KEY = "version";
    SVC_WORK_PORT = 9997;
    SVC_WORKID_KEY = "workid";
    SVC_WORKITEM_KEY = "workitem";
    SVC_WORKSPACE_NAME_KEY = "workspace";
    SVC_WORKSTATUS_KEY = "workstatus";
    SVC_ZIPFILE_KEY = "zipfile";

