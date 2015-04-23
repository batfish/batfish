
$(document).ready(
    function () {
        UpdateDebugInfo(this, "document loaded");
        new ServiceHelper().Get(SVC_WORK_MGR_ROOT + SVC_WORK_GETSTATUS_RSC, cbGetWorkStatus);
    }
);

function cbGetWorkStatus(context, result) {

    if (result[0] === SVC_SUCCESS_KEY) {
        UpdateDebugInfo(context, "WorkStatus: " + result[1]["completed-works"] + " / " + result[1]["incomplete-works"]);
    }
    else {
        UpdateDebugInfo(this, "GetWorkStatusCallback: " + result[0] + " " + result[1]);
    }
}

function fnAddWorker() {
    var worker = jQuery("#txtAddWorker").val();
    new ServiceHelper().Get(SVC_POOL_MGR_ROOT + SVC_POOL_UPDATE_RSC + "?add=" + worker, cbAddWorker);
}

function cbAddWorker(context, result) {
    if (result[0] === SVC_SUCCESS_KEY) {
        UpdateDebugInfo(context, "Worked added");
    }
    else {
        UpdateDebugInfo(this, "Worker addition failed: " + result[1]);
    }
}


//function fnUploadTestrig() {

////    var dataParam = function () {
//        var data = new FormData();
//        data.append(SVC_TESTRIG_NAME_KEY, jQuery("#txtUploadTestrig").val());
//        data.append(SVC_ZIPFILE_KEY, jQuery("#fileUploadTestrig").get(0).files[0]);
////        return data;
//        // Or simply return new FormData(jQuery("form")[0]);
////    }();
 
//    new ServiceHelper().Upload(SVC_WORK_MGR_ROOT + SVC_WORK_UPLOAD_TESTRIG_RSC, data, cbUploadTestrig);
//}

// ------------------ uploadEnvironment------------------

//code roughly based on http://www.thefourtheye.in/2013/10/file-upload-with-jquery-and-ajax.html

function fnUploadTestrig() {
    var data = new FormData();
    data.append(SVC_TESTRIG_NAME_KEY, jQuery("#txtTestrigName").val());
    data.append(SVC_ZIPFILE_KEY, jQuery("#fileUploadTestrig").get(0).files[0]);

    jQuery.ajax({
        url: SVC_WORK_MGR_ROOT + SVC_WORK_UPLOAD_TESTRIG_RSC,
        type: "POST",
        contentType: false,
        processData: false,
        data: data,

        error: function (_, textStatus, errorThrown) {
            UpdateDebugInfo("Testrig upload failed:", textStatus, errorThrown);
            console.log(textStatus, errorThrown);
        },
        success: function (response, textStatus) {
            if (response[0] === SVC_SUCCESS_KEY) {
                UpdateDebugInfo(this, "Testrig uploaded");
            }
            else {
                UpdateDebugInfo(this, "Testrig upload failed: " + response[1]);
            }
        }
    });
}

// -----------------------------------doWork-----------------------

var uuidCurrWork;
var currWorkChecker;

function fnDoWork(worktype) {

    uuidCurrWork = guid();

    //set the guid of the text field
    jQuery("#txtDoWorkGuid").val(uuidCurrWork);

    var testrigName = jQuery("#txtTestrigName").val();

    var reqParams = {};

    switch (worktype) {
        case "vendorspecific":
            reqParams[COMMAND_PARSE_VENDOR_SPECIFIC] = "";
            break;
        case "vendorindependent":
            reqParams[COMMAND_PARSE_VENDOR_INDEPENDENT] = "";
            break;
        case "generatefacts":
            reqParams[COMMAND_GENERATE_FACT] = "";
            reqParams[COMMAND_ENV] = jQuery("#txtEnvironmentName").val();
            break;
        case "generatedataplane":
            reqParams[COMMAND_COMPILE] = "";
            reqParams[COMMAND_FACTS] = "";
            reqParams[COMMAND_ENV] = jQuery("#txtEnvironmentName").val();
            break;
        case "getdataplane":
            reqParams[COMMAND_DUMP_DP] = "";
            reqParams[COMMAND_ENV] = jQuery("#txtEnvironmentName").val();
            break;
        case "getz3encoding":
            reqParams[COMMAND_SYNTHESIZE_Z3_DATA_PLANE] = "";
            reqParams[COMMAND_ENV] = jQuery("#txtEnvironmentName").val();
            break;
        case "answerquestion":
            reqParams[COMMAND_ANSWER] = "";
            reqParams[ARG_QUESTION_NAME] = jQuery("#txtQuestionName").val();
            break;
        case "postflows":
            reqParams[COMMAND_POST_FLOWS] = "";
            reqParams[ARG_QUESTION_NAME] = jQuery("#txtQuestionName").val();
            reqParams[COMMAND_ENV] = jQuery("#txtEnvironmentName").val();
            break;
        case "getflowtraces":
            reqParams[COMMAND_POST_FLOWS] = "";
            reqParams[ARG_PREDICATES] = PREDICATE_FLOW_PATH_HISTORY;
            reqParams[COMMAND_ENV] = jQuery("#txtEnvironmentName").val();
            break;
        default:
            UpdateDebugInfo("failed: unsupported work command", worktype);
    }

    var workItem = JSON.stringify([uuidCurrWork, testrigName, reqParams, {}]);

    //if we had an old work checker, kill it before queuing this work
    window.clearTimeout(currWorkChecker);

    new ServiceHelper().Get(SVC_WORK_MGR_ROOT + SVC_WORK_QUEUE_WORK_RSC + "?" + SVC_WORKITEM_KEY + "=" + workItem, cbDoWork);
}

function cbDoWork(context, result) {
    if (result[0] === SVC_SUCCESS_KEY) {
        UpdateDebugInfo(context, "Work queued. Will continue checking.");
        currWorkChecker = window.setTimeout(fnCheckWork, 10 * 1000);
    }
    else {
        UpdateDebugInfo(this, "Work queuing failed: " + result[1]);
    }
}

function fnCheckWork() {
    new ServiceHelper().Get(SVC_WORK_MGR_ROOT + SVC_WORK_GET_WORKSTATUS_RSC + "?" + SVC_WORKID_KEY + "=" + uuidCurrWork, cbCheckWork);
}

function cbCheckWork(context, result) {
    if (result[0] === SVC_SUCCESS_KEY) {
        UpdateDebugInfo(context, "Work checking succeeded");

        var status = result[1][SVC_WORKSTATUS_KEY];
        jQuery("#txtCheckWorkStatus").val(status);

        switch (status) {
            case "TERMINATEDNORMALLY":
            case "TERMINATEDABNORMALLY":
                break;
            case "UNASSIGNED":
            case "TRYINGTOASSIGN":
            case "ASSIGNED":
            case "ASSIGNMENTERROR":
            case "CHECKINGSTATUS":
                //fire again
                currWorkChecker = window.setTimeout(fnCheckWork, 10 * 1000);
                break;
            default:
                UpdateDebugInfo("Got unknown status: ", status);
        }        
    }
    else {
        UpdateDebugInfo(this, "Work queuing failed: " + result[1]);
    }
}

function guid() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
          .toString(16)
          .substring(1);
    }
    return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
      s4() + '-' + s4() + s4() + s4();
}

function fnGetLog() {
    var uuidWork = jQuery("#txtDoWorkGuid").val();
    helperGetObject(testrigName, uuidWork + ".log");
}

function fnGetObject(worktype) {

    var testrigName = jQuery("#txtTestrigName").val();

    //this need not be properly populated for all worktypes
    var envName = jQuery("#txtEnvironmentName").val();

    var objectName = "unknown";

    switch (worktype) {
        case "vendorspecific":
            objectName = RELPATH_VENDOR_SPECIFIC_CONFIG_DIR;
            break;
        case "vendorindependent":
            objectName = RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR;
            break;
        case "generatefacts":
            objectName = [RELPATH_ENVIRONMENTS_DIR, envName, RELPATH_FACT_DUMP_DIR].join("/");
            break;
        case "getdataplane":
            objectName = [RELPATH_ENVIRONMENTS_DIR, envName, RELPATH_DATA_PLANE_DIR].join("/");
            break;
        case "getz3encoding":
            objectName = [RELPATH_ENVIRONMENTS_DIR, envName, RELPATH_Z3_DATA_PLANE_FILE].join("/");
            break;
        case "getflowtraces":
            objectName = [RELPATH_ENVIRONMENTS_DIR, envName, RELPATH_QUERY_DUMP_DIR].join("/");
            break;
        default:
            UpdateDebugInfo("failed: unsupported worktype for get result", worktype);
    }

    helperGetObject(testrigName, objectName);
}

function helperGetObject(testrigName, objectName) {
    //    new ServiceHelper().Get(SVC_WORK_MGR_ROOT + SVC_WORK_GET_OBJECT_RSC + "?" + SVC_TESTRIG_NAME_KEY + "=" + testrigName + "&" + SVC_WORK_OBJECT_KEY + "=" + objectName, cbGetObject);
    //jQuery.ajax({
    //    url: SVC_WORK_MGR_ROOT + SVC_WORK_GET_OBJECT_RSC + "?" + SVC_TESTRIG_NAME_KEY + "=" + testrigName + "&" + SVC_WORK_OBJECT_KEY + "=" + objectName,
    //    type: "GET",

    //    error: function (_, textStatus, errorThrown) {
    //        UpdateDebugInfo("Get object failed:", textStatus, errorThrown);
    //    },
    //    success: function (response, textStatus) {
    //        cbGetObject(response, textStatus);
    //    }
    //});

    var uri = encodeURI(SVC_WORK_MGR_ROOT + SVC_WORK_GET_OBJECT_RSC + "?" + SVC_TESTRIG_NAME_KEY + "=" + testrigName + "&" + SVC_WORK_OBJECT_KEY + "=" + objectName);
    window.location.assign(uri);
}

function cbGetObject(context, result) {
    if (result[0] === SVC_SUCCESS_KEY) {
        UpdateDebugInfo(context, "Got object");
    }
    else {
        UpdateDebugInfo(this, "Getting object failed: " + result[1]);
    }
}


function fnUploadEnvironment() {
    var data = new FormData();
    data.append(SVC_TESTRIG_NAME_KEY, jQuery("#txtTestrigName").val());
    data.append(SVC_ENV_NAME_KEY, jQuery("#txtEnvironmentName").val());
    data.append(SVC_ZIPFILE_KEY, jQuery("#fileUploadEnvironment").get(0).files[0]);

    jQuery.ajax({
        url: SVC_WORK_MGR_ROOT + SVC_WORK_UPLOAD_ENV_RSC,
        type: "POST",
        contentType: false,
        processData: false,
        data: data,

        error: function (_, textStatus, errorThrown) {
            UpdateDebugInfo("Environment upload failed:", textStatus, errorThrown);
            console.log(textStatus, errorThrown);
        },
        success: function (response, textStatus) {
            if (response[0] === SVC_SUCCESS_KEY) {
                UpdateDebugInfo(this, "Environment uploaded");
            }
            else {
                UpdateDebugInfo(this, "Environment upload failed: " + response[1]);
            }
        }
    });
}

function fnUploadQuestion() {
    var data = new FormData();
    data.append(SVC_TESTRIG_NAME_KEY, jQuery("#txtTestrigName").val());
    data.append(SVC_QUESTION_NAME_KEY, jQuery("#txtQuestionName").val());
    data.append(SVC_FILE_KEY, jQuery("#fileUploadQuestion").get(0).files[0]);

    jQuery.ajax({
        url: SVC_WORK_MGR_ROOT + SVC_WORK_UPLOAD_QUESTION_RSC,
        type: "POST",
        contentType: false,
        processData: false,
        data: data,

        error: function (_, textStatus, errorThrown) {
            UpdateDebugInfo("Question upload failed:", textStatus, errorThrown);
            console.log(textStatus, errorThrown);
        },
        success: function (response, textStatus) {
            if (response[0] === SVC_SUCCESS_KEY) {
                UpdateDebugInfo(this, "Question uploaded");
            }
            else {
                UpdateDebugInfo(this, "Question upload failed: " + response[1]);
            }
        }
    });
}


    //By convention we add div with this id: divDebugInfo to the bottom of pages as place to display debugging info
    function UpdateDebugInfo(object, string) {
        if ($("#divDebugInfo").is(':hidden'))
            return;
        $("#divDebugInfo").html(string);
    }

    function ShowDebugInfo() {
        $("#divDebugInfo").show();
    }

