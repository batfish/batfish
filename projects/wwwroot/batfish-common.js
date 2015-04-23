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


function ServiceHelper() {

    this.ClearFields = function () {
        this.Type = null;
        this.Url = null;
        this.Data = null;
        this.ContentType = null;
        this.DataType = null;
        this.ProcessData = null;
        this.Callback = null;
    }


    this.Get = function (url_parm, callback) {
        this.Call("GET", url_parm, "", "application/json; charset=utf-8", "json", true, callback);
    }

    this.Upload = function (url_parm, data_parm, callback) {
        this.Call("POST", url_parm, data_parm, false, "json", false, callback);
    }

    this.Call = function (type, url_parm, data_parm, content_type, data_type, process_data, callback) {
        this.Type = type;
        this.Url = url_parm;
        this.Data = data_parm;
        this.ContentType = content_type;
        this.DataType = data_type;
        this.ProcessData = process_data;
        this.Callback = callback;
        this.CallService();
    }


    // Function to call WCF  Service       
    this.CallService = function () {
        var Type = this.Type;
        var Url = this.Url;
        var Data = this.Data;
        var ContentType = this.ContentType;
        var DataType = this.DataType;
        var ProcessData = this.ProcessData;
        var Callback = this.Callback;

        var SucceededServiceCallback = this.SucceededServiceCallback;
        var FailedServiceCallback = this.FailedServiceCallback;
        var Context = this;

        UpdateDebugInfo(this, 'calling: ' + this.DataType + " URL: " + this.Url + " Data: " + this.Data);


        //var data = new FormData();
        //data.append(SVC_TESTRIG_NAME_KEY, jQuery("#txtUploadTestrig").val());
        //data.append(SVC_ZIPFILE_KEY, jQuery("#fileUploadTestrig").get(0).files[0]);

        $.ajax({
            type: Type, //GET or POST or PUT or DELETE verb
            url: Url, // Location of the service
            data: Data, //Data sent to server
            contentType: ContentType, // content type sent to server
            dataType: DataType, //Expected data format from server
            processdata: ProcessData, //True or False

            success: function (msg) {//On Successfull service call
                SucceededServiceCallback(this, msg);
            },
            error: function (msg) {
                this.FailedServiceCallback(this, msg);
            },
            context: Context
        });
    }

    this.FailedServiceCallback = function (context, result) {
        ShowDebugInfo();
        UpdateDebugInfo(this, 'failed: ' + context.DataType + " URL: " + context.Url + " Data: " + context.Data + "result: " + result.status + ' ' + result.statusText);
    }

    this.SucceededServiceCallback = function (context, result) {
        //if (null != context) {
        //    UpdateDebugInfo(context, "succeeded: " + context.DataType + " URL: " + context.Url + " Data: " + context.Data + " Result: " + result);
        //}
        //if (context != null && context.DataType == "json" && result != null && context.Callback != null) {
        if (context != null && context.Callback != null) {
                context.Callback(context, result);
        }
    }
}