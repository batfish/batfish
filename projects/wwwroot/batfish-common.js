"use strict";

// These constants come from 'BfConsts.java'.
// Make sure they are in sync with what is there.
var ARG_DIFF_ACTIVE = "diffactive";
var ARG_DIFF_ENVIRONMENT_NAME = "diffenv";
var ARG_ENVIRONMENT_NAME = "env";
var ARG_LOG_LEVEL = "loglevel";
var ARG_OUTPUT_ENV = "outputenv";
var ARG_PEDANTIC_AS_ERROR = "pedanticerror";
var ARG_PEDANTIC_SUPPRESS = "pedanticsuppress";
var ARG_PREDICATES = "predicates";
var ARG_QUESTION_NAME = "questionname";
var ARG_RED_FLAG_AS_ERROR = "redflagerror";
var ARG_RED_FLAG_SUPPRESS = "redflagsuppress";
var ARG_UNIMPLEMENTED_AS_ERROR = "unimplementederror";
var ARG_UNIMPLEMENTED_SUPPRESS = "unimplementedsuppress";

var COMMAND_ANSWER = "answer";
var COMMAND_CREATE_WORKSPACE = "createworkspace";
var COMMAND_DUMP_DP = "dp";
var COMMAND_FACTS = "facts";
var COMMAND_GENERATE_FACT = "dumpcp";
var COMMAND_GET_DIFFERENTIAL_HISTORY = "getdiffhistory";
var COMMAND_GET_HISTORY = "gethistory";
var COMMAND_PARSE_VENDOR_INDEPENDENT = "si";
var COMMAND_PARSE_VENDOR_SPECIFIC = "sv";
var COMMAND_POST_DIFFERENTIAL_FLOWS = "postdiffflows";
var COMMAND_POST_FLOWS = "postflows";
var COMMAND_QUERY = "query";
var COMMAND_SYNTHESIZE_Z3_DATA_PLANE = "z3";
var COMMAND_WRITE_ROUTES = "writeroutes";

var LOG_LEVEL_OUTPUT = "output";
var LOG_LEVEL_WARN = "warn";

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

// These constants come from 'CoordConsts.java'.
// Make sure they are in sync with what is there.
var SVC_ENV_NAME_KEY = "envname";
var SVC_FAILURE_KEY = "failure";
var SVC_FILE_KEY = "file";
var SVC_POOL_GETSTATUS_RSC = "getstatus";
var SVC_POOL_MGR_ROOT = "http://localhost:9998/batfishpoolmgr/";
var SVC_POOL_UPDATE_RSC = "updatepool";
var SVC_QUESTION_NAME_KEY = "questionname";
var SVC_SUCCESS_KEY = "success";
var SVC_TESTRIG_NAME_KEY = "testrigname";
var SVC_WORK_GET_OBJECT_RSC = "getobject";
var SVC_WORK_GET_WORKSTATUS_RSC = "getworkstatus";
var SVC_WORK_GETSTATUS_RSC = "getstatus";
var SVC_WORK_MGR_ROOT = "http://localhost:9997/batfishworkmgr/";
var SVC_WORK_OBJECT_KEY = "object";
var SVC_WORK_QUEUE_WORK_RSC = "queuework";
var SVC_WORK_UPLOAD_ENV_RSC = "uploadenvironment";
var SVC_WORK_UPLOAD_QUESTION_RSC = "uploadquestion";
var SVC_WORK_UPLOAD_TESTRIG_RSC = "uploadtestrig";
var SVC_WORKID_KEY = "workid";
var SVC_WORKITEM_KEY = "workitem";
var SVC_WORKSPACE_NAME_KEY = "workspace";
var SVC_WORKSTATUS_KEY = "workstatus";
var SVC_ZIPFILE_KEY = "zipfile";

$(document).ajaxError(function(event, request, settings, thrownError) {
   bfUpdateDebugInfo(settings.url + " " + thrownError + " " + request);
});

var debugLog = [];

var maxLogEntries = 10000;

function bfDownloadObject(testrigName, objectName) {
   var uri = encodeURI(SVC_WORK_MGR_ROOT + SVC_WORK_GET_OBJECT_RSC + "?"
         + SVC_TESTRIG_NAME_KEY + "=" + testrigName + "&" + SVC_WORK_OBJECT_KEY
         + "=" + objectName);

   bfUpdateDebugInfo("Fetching " + uri);

   window.location.assign(uri);
}

function bfGenericCallback(taskname, result) {
   if (result[0] === SVC_SUCCESS_KEY) {
      bfUpdateDebugInfo(taskname + " succeeded");
   }
   else {
      alert(taskname + "failed: " + result[1]);
   }
}

function bfGetJson(taskname, url_parm, callback, worktype) {
   console.log("bfGetJsonRequest: ", taskname, url_parm);
   $
         .ajax({
            type : "GET", // GET or POST or PUT or DELETE verb
            url : url_parm, // Location of the service
            dataType : "json", // Expected data format from server

            error : function(_, textStatus, errorThrown) {
               alert(taskname + " failed: ", textStatus, errorThrown);
            },
            success : function(response, textStatus) {
               console.log("bfGetJsonResponse: ", taskname, JSON
                     .stringify(response));
               callback(taskname, response, worktype);
            }
         });
}

function bfGetObject(testrigName, objectName) {
   var uri = encodeURI(SVC_WORK_MGR_ROOT + SVC_WORK_GET_OBJECT_RSC + "?"
         + SVC_TESTRIG_NAME_KEY + "=" + testrigName + "&" + SVC_WORK_OBJECT_KEY
         + "=" + objectName);

   bfUpdateDebugInfo("Fetching " + uri);

   $.get(uri, function(data) {
      var op = document.getElementById("divOutputInfo");
      op.textContent = data;
   }).fail(function() {
      bfUpdateDebugInfo("Failed to fetch " + uri);
   });

   // window.location.assign(uri);
}

function bfGetTimestamp() {
   var now = new Date();
   return now.toLocaleTimeString();
}

function bfUpdateCoordinatorLocation() {

   var coordinatorHost = jQuery("#txtCoordinatorHost").val();

   if (coordinatorHost == "") {
      alert("Specify a coordinator host");
      return;
   }

   SVC_WORK_MGR_ROOT = "http://" + coordinatorHost + ":9997/batfishworkmgr/";
   SVC_POOL_MGR_ROOT = "http://" + coordinatorHost + ":9998/batfishpoolmgr/";

   bfUpdateDebugInfo("Coordinator host is updated to " + coordinatorHost);
}

function bfUpdateDebugInfo(string) {
   debugLog.splice(0, 0, bfGetTimestamp() + " " + string);
   while (debugLog.length > maxLogEntries) {
      debugLog.shift();
   }
   $("#divDebugInfo").html(debugLog.join("\n"));
}

function bfUploadData(taskname, url_parm, data, callback, worktype) {
   console.log("bfUploadData: ", taskname, url_parm);
   jQuery.ajax({
      url : url_parm,
      type : "POST",
      contentType : false,
      processData : false,
      data : data,

      error : function(_, textStatus, errorThrown) {
         alert(taskname + " failed: ", textStatus, errorThrown);
      },
      success : function(response, textStatus) {
         if (response[0] === SVC_SUCCESS_KEY) {
            bfUpdateDebugInfo(taskname + " succeeded");
            if (callback != undefined)
               callback(worktype);
         }
         else {
            alert(taskname + " failed: " + response[1]);
         }
      }
   });
}
