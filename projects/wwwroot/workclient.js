
$(document).ready(
    function () {
        fnGetCoordinatorWorkQueueStatus();
    }
);

// ------------------------------------------------

var WorkGuids = {};

// -------------------------------------------------

function fnGetCoordinatorWorkQueueStatus() {
    bfGetJson("GetCoordinatorWorkQueueStatus", SVC_WORK_MGR_ROOT + SVC_WORK_GETSTATUS_RSC, cbGetCoordinatorWorkQueueStatus, "");
}

function cbGetCoordinatorWorkQueueStatus(taskname, result) {

    if (result[0] === SVC_SUCCESS_KEY) {
        var cWorks = result[1]["completed-works"];
        var iWorks = result[1]["incomplete-works"];

        jQuery("#txtCompletedWorks").val(cWorks);
        jQuery("#txtIncompleteWorks").val(iWorks);

        bfUpdateDebugInfo("Coordinator work queue status refreshed");
    }
    else {
        alert(taskname + "failed: " + result[1]);
    }
}

// -------------------------------------------------

function fnAddWorker() {
    var worker = jQuery("#txtAddWorker").val();

    if (worker == "") {
        alert("Specify a worker first");
        return;
    }

    bfGetJson("AddWorker-" + worker, SVC_POOL_MGR_ROOT + SVC_POOL_UPDATE_RSC + "?add=" + worker, bfGenericCallback, "");
}

// -----------------------------------------------

function fnUploadTestrig() {

    var testrigName = jQuery("#txtTestrigName").val();

    if (testrigName == "") {
        alert("Specify a testrig name");
        return;
    }

    var testrigFile = jQuery("#fileUploadTestrig").get(0).files[0];

    if (typeof testrigFile === 'undefined') {
        alert("Select a testrig file");
        return;
    }

    var data = new FormData();
    data.append(SVC_TESTRIG_NAME_KEY, testrigName);
    data.append(SVC_ZIPFILE_KEY, testrigFile);

    bfUploadData("UploadTestrig " + testrigName, SVC_WORK_MGR_ROOT + SVC_WORK_UPLOAD_TESTRIG_RSC, data, cbUploadData, "testrig");
}

// -----------------------------------doWork-----------------------

function fnDoWork(worktype) {

    var uuidCurrWork = guid();

    WorkGuids[worktype] = uuidCurrWork;

    //set the guid of the text field
    jQuery("#txtWorkGuid").val(uuidCurrWork);

    var testrigName = jQuery("#txtTestrigName").val();
    if (testrigName == "") {
        alert("Testrig name is empty");
        return;
    }

    var envName = jQuery("#txtEnvironmentName").val();
    if (envName == "" && worktype.substring(0, 6) == "vendor") { //vendor* worktype does not need an environment
        alert("Environment name is empty");
        return;
    }

    var questionName = jQuery("#txtQuestionName").val();
    if (questionName == "" && (worktype == "answerquestion" || worktype == "postflows")) { 
        alert("Question name is empty");
        return;
    }

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
            reqParams[COMMAND_ENV] = envName
            break;
        case "generatedataplane":
            reqParams[COMMAND_COMPILE] = "";
            reqParams[COMMAND_FACTS] = "";
            reqParams[COMMAND_ENV] = envName;
            break;
        case "getdataplane":
            reqParams[COMMAND_DUMP_DP] = "";
            reqParams[COMMAND_ENV] = envName;
            break;
        case "getz3encoding":
            reqParams[COMMAND_SYNTHESIZE_Z3_DATA_PLANE] = "";
            reqParams[COMMAND_ENV] = envName;
            break;
        case "answerquestion":
            reqParams[COMMAND_ANSWER] = "";
            reqParams[ARG_QUESTION_NAME] = questionName;
            reqParams[ARG_LOGLEVEL] = "output";
            break;
        case "postflows":
            reqParams[COMMAND_POST_FLOWS] = "";
            reqParams[ARG_QUESTION_NAME] = questionName;
            reqParams[COMMAND_ENV] = envName;
            break;
        case "getflowtraces":
            reqParams[COMMAND_QUERY] = "";
            reqParams[ARG_PREDICATES] = PREDICATE_FLOW_PATH_HISTORY;
            reqParams[COMMAND_ENV] = envName;
            break;
        default:
            alert("Unsupported work command", worktype);
    }

    var workItem = JSON.stringify([uuidCurrWork, testrigName, reqParams, {}]);

    bfGetJson("DoWork:" + worktype, SVC_WORK_MGR_ROOT + SVC_WORK_QUEUE_WORK_RSC + "?" + SVC_WORKITEM_KEY + "=" + workItem, cbDoWork, worktype);
}

function cbDoWork(taskname, result, worktype) {
    if (result[0] === SVC_SUCCESS_KEY) {
        bfUpdateDebugInfo(taskname + " succeeded. Will start polling for status");
        fnCheckWork(worktype);
    }
    else {
        alert("Work queuing failed: " + result[1]);
    }
}

function doFollowOnWork(worktype) {

    if (DEMO_MODE == 0)
        return;

    switch (worktype) {
        case "vendorspecific":
            fnDoWork("vendorindependent");
            break;
        case "vendorindependent":
            fnDoWork("generatefacts");
            break;
        case "generatefacts":
            //no follow on work to be done here
            bfUpdateDebugInfo("Done generating control plane");
            break;
        case "generatedataplane":
            fnDoWork("getdataplane");
            break;
        case "getdataplane":
            fnDoWork("getz3encoding");
            break;
        case "getz3encoding":
            //no follow on work to be done here
            bfUpdateDebugInfo("Done generating data plane");
            break;
        case "answerquestion":
            var qName = jQuery("#txtQuestionName").val();
            if (qName.toLowerCase().indexOf("multi") >= 0) {
                fnDoWork("postflows");
            }
            else {
                bfUpdateDebugInfo("Done answering query");
            }
            break;
        case "postflows":
            fnDoWork("getflowtraces");
            break;
        case "getflowtraces":
            //no follow on work to be done here
            bfUpdateDebugInfo("Done answering query");
            break;
        default:
            alert("Unsupported work command", worktype);
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

// --------------------------------

var currWorkChecker;

function fnCheckWork(worktype) {

    //delete any old work checker
    window.clearTimeout(currWorkChecker);

    var uuid = jQuery("#txtWorkGuid").val();
    if (uuid == "") {
        alert("Work GUID is empty. Cannot check status");
        return;
    }

    bfGetJson("Checkwork-" + uuid, SVC_WORK_MGR_ROOT + SVC_WORK_GET_WORKSTATUS_RSC + "?" + SVC_WORKID_KEY + "=" + uuid, cbCheckWork, worktype);
}

function cbCheckWork(taskname, result, worktype) {
    if (result[0] === SVC_SUCCESS_KEY) {

        var status = result[1][SVC_WORKSTATUS_KEY];

        bfUpdateDebugInfo(taskname + " returned with response " + status);

        jQuery("#txtCheckWorkStatus").val(status);

        switch (status) {
            case "TERMINATEDNORMALLY":
                doFollowOnWork(worktype);
                break;
            case "TERMINATEDABNORMALLY":
            case "ASSIGNMENTERROR":
                break;
            case "UNASSIGNED":
            case "TRYINGTOASSIGN":
            case "ASSIGNED":
            case "CHECKINGSTATUS":
                //fire again
                currWorkChecker = window.setTimeout(function () { fnCheckWork(worktype) }, 10 * 1000);
                break;
            default:
                bfUpdateDebugInfo("Got unknown work status: ", status);
        }        
    }
    else {
        bfUpdateDebugInfo("Work status check failed: " + result[1]);
    }
}

// ----------------------------------------------------------------

function fnGetLog(worktype) {
    var testrigName = jQuery("#txtTestrigName").val();
    if (testrigName == "") {
        alert("Testrig name is empty.");
        return;
    }

    var uuidWork = "";

    if (worktype == "") {
        uuidWork = jQuery("#txtWorkGuid").val();
    }
    else {
        uuidWork = WorkGuids[worktype];
    }

    if (uuidWork == "") {
        alert("Work GUID is empty");
        return;
    }

    bfGetObject(testrigName, uuidWork + ".log");
}

function fnGetObject(worktype) {

    var testrigName = jQuery("#txtTestrigName").val();

    if (testrigName == "") {
        alert("Testrig name is empty");
        return;
    }

    var envName = jQuery("#txtEnvironmentName").val();
    if (envName == "" && worktype.substring(0, 6) == "vendor") { //vendor* worktype does not need an environment
        alert("Environment name is empty");
        return;
    }

    var objectName = ""; 

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
            alert("Unsupported worktype for get result", worktype);
    }

    if (objectName == "") { 
        alert("Could not determine the right object name to fetch");
        return;
    }

    bfGetObject(testrigName, objectName);
}

// ------------------------------------

function fnUploadEnvironment() {

    var testrigName = jQuery("#txtTestrigName").val();
    if (testrigName == "") {
        alert("Specify a testrig name");
        return;
    }

    var envName = jQuery("#txtEnvironmentName").val();
    if (envName == "") {
        alert("Specify an environment name");
        return;
    }

    var envFile = jQuery("#fileUploadEnvironment").get(0).files[0];
    if (typeof envFile === 'undefined') {
        alert("Select an environment file");
        return;
    }

    var data = new FormData();
    data.append(SVC_TESTRIG_NAME_KEY, testrigName);
    data.append(SVC_ENV_NAME_KEY, envName);
    data.append(SVC_ZIPFILE_KEY, envFile);

    bfUploadData("UploadEnvironment-" + envName, SVC_WORK_MGR_ROOT + SVC_WORK_UPLOAD_ENV_RSC, data, cbUploadData, "environment");
}

// ------------------------------

function fnUploadQuestion() {

    var testrigName = jQuery("#txtTestrigName").val();
    if (testrigName == "") {
        alert("Specify a testrig name");
        return;
    }

    var qFile = jQuery("#fileUploadQuestion").get(0).files[0];
    if (typeof qFile === 'undefined') {
        alert("Select a question file");
        return;
    }

    var qName = jQuery("#txtQuestionName").val();

    if (qName == "") {
        if (DEMO_MODE == 0) {
            alert("Specify a question name");
            return;
        }
        else {
            jQuery("#txtQuestionName").val(qFile.name);
            qName = jQuery("#txtQuestionName").val();
        }
    }


    var data = new FormData();
    data.append(SVC_TESTRIG_NAME_KEY, testrigName);
    data.append(SVC_QUESTION_NAME_KEY, qName);
    data.append(SVC_FILE_KEY, qFile);

    bfUploadData("UploadQuestion-" + qName, SVC_WORK_MGR_ROOT + SVC_WORK_UPLOAD_QUESTION_RSC, data, cbUploadData, "question");
}

function cbUploadData(uploadtype) {

    switch (uploadtype) {
        case "question":
            fnDoWork("answerquestion");
            break;
        case "testrig":
        case "environment":
            break;
        default:
            alert("Unknown upload type " + uploadtype);
    }
}