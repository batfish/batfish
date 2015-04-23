"use strict";
var SVC_WORK_MGR_ROOT = "http://localhost:9997/batfishworkmgr/";
var SVC_POOL_MGR_ROOT = "http://localhost:9998/batfishpoolmgr/";

//
//these constants come from CoordConsts.java
//make sure they are in sync with what is there
//
var SVC_SUCCESS_KEY = "success";
var SVC_FAILURE_KEY = "failure";

var SVC_POOL_GETSTATUS_RSC = "getstatus";
var SVC_POOL_UPDATE_RSC = "updatepool";

var SVC_WORK_GETSTATUS_RSC = "getstatus";
var SVC_WORK_UPLOAD_ENV_RSC = "uploadenvironment";
var SVC_WORK_UPLOAD_QUESTION_RSC = "uploadquestion";
var SVC_WORK_UPLOAD_TESTRIG_RSC = "uploadtestrig";
var SVC_WORK_QUEUE_WORK_RSC = "queuework";
var SVC_WORK_GET_WORKSTATUS_RSC = "getworkstatus";
var SVC_WORK_GET_OBJECT_RSC = "getobject";

var SVC_WORK_OBJECT_KEY = "object";
var SVC_WORKID_KEY = "workid";
var SVC_WORKSTATUS_KEY = "workstatus";
var SVC_WORKITEM_KEY = "workitem";
var SVC_ENV_NAME_KEY = "envname";
var SVC_QUESTION_NAME_KEY = "questionname";
var SVC_TESTRIG_NAME_KEY = "testrigname";
var SVC_FILE_KEY = "file";
var SVC_ZIPFILE_KEY = "zipfile";
var SVC_WORKSPACE_NAME_KEY = "workspace";

var SVC_WORK_OBJECT_KEY = "object";
var SVC_WORKID_KEY = "workid";
var SVC_WORKSTATUS_KEY = "workstatus";
var SVC_WORKITEM_KEY = "workitem";
var SVC_ENV_NAME_KEY = "envname";
var SVC_QUESTION_NAME_KEY = "questionname";
var SVC_TESTRIG_NAME_KEY = "testrigname";
var SVC_FILE_KEY = "file";
var SVC_ZIPFILE_KEY = "zipfile";
var SVC_WORKSPACE_NAME_KEY = "workspace";

//
//these constants come from BfConsts.java
//make sure they are in sync with what is there
//

var ARG_QUESTION_NAME = "questionname";
var ARG_PREDICATES = "predicates";

var COMMAND_ANSWER = "answer";
var COMMAND_COMPILE = "compile";
var COMMAND_DUMP_DP = "dp";
var COMMAND_ENV = "env";
var COMMAND_FACTS = "facts";
var COMMAND_GENERATE_FACT = "dumpcp";
var COMMAND_PARSE_VENDOR_INDEPENDENT = "si";
var COMMAND_PARSE_VENDOR_SPECIFIC = "sv";
var COMMAND_POST_FLOWS = "postflows";
var COMMAND_QUERY = "query";
var COMMAND_SYNTHESIZE_Z3_DATA_PLANE = "z3";

var PREDICATE_FLOW_PATH_HISTORY = "FlowPathHistory";

var RELPATH_DATA_PLANE_DIR = "dp";
var RELPATH_ENV_DIR = "env";
var RELPATH_ENV_NODE_SET = "env-node-set";
var RELPATH_ENVIRONMENTS_DIR = "environments";
var RELPATH_FACT_DUMP_DIR = "dump";
var RELPATH_FLOWS_DUMP_DIR = "flowdump";
var RELPATH_LB_HOSTNAME_PATH = "lb";
var RELPATH_MULTIPATH_QUERY_PREFIX = "multipath-query";
var RELPATH_QUERIES_DIR = "queries";
var RELPATH_QUERY_DUMP_DIR = "querydump";
var RELPATH_QUESTION_FILE = "question";
var RELPATH_QUESTIONS_DIR = "questions";
var RELPATH_TEST_RIG_DIR = "testrig";
var RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR = "indep";
var RELPATH_VENDOR_SPECIFIC_CONFIG_DIR = "vendor";
var RELPATH_Z3_DATA_PLANE_FILE = "dataplane.smt2";

function bfUploadData(taskname, url_parm, data) {
    console.log("bfUploadData: ", taskname, url_parm);
    jQuery.ajax({
        url: url_parm,
        type: "POST",
        contentType: false,
        processData: false,
        data: data,

        error: function (_, textStatus, errorThrown) {
            alert(taskname + " failed: ", textStatus, errorThrown);
        },
        success: function (response, textStatus) {
            if (response[0] === SVC_SUCCESS_KEY) {
                UpdateDebugInfo(taskname + " succeeded");
            }
            else {
                alert(taskname + " failed: " + response[1]);
            }
        }
    });
}

function bfGetJson(taskname, url_parm, callback) {
    console.log("bfGetJsonRequest: ", taskname, url_parm);
    $.ajax({
        type: "GET", //GET or POST or PUT or DELETE verb
        url: url_parm, // Location of the service
        dataType: "json", //Expected data format from server

        error: function (_, textStatus, errorThrown) {
            UpdateDebugInfo(taskname + " failed: ", textStatus, errorThrown);
        },
        success: function (response, textStatus) {
            console.log("bfGetJsonResponse: ", taskname, JSON.stringify(response));
            callback(response);
        }
    });
}
