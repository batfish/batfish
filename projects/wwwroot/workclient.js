//$(document).ready(function() {
//   fnGetCoordinatorWorkQueueStatus();
//});

var currWorkChecker;
var WorkGuid;

function checkWork(entryPoint, remainingCalls) {
   // delete any old work checker
   window.clearTimeout(currWorkChecker);

   if (WorkGuid == "") {
      alert("Work GUID is empty. Cannot check status");
      return;
   }

   var data = new FormData();
   data.append(SVC_API_KEY, API_KEY);
   data.append(SVC_WORKID_KEY, WorkGuid);

   bfPostData(SVC_GET_WORKSTATUS_RSC, data, checkWork_cb, entryPoint, remainingCalls);
}

function checkWork_cb(response, entryPoint, remainingCalls) {
    var status = response[SVC_WORKSTATUS_KEY];

    bfUpdateDebugInfo("Workcheck returned with response " + status);

    switch (status) {
        case "TERMINATEDNORMALLY":
            getLog(entryPoint, remainingCalls);
            break;
        case "TERMINATEDABNORMALLY":
        case "ASSIGNMENTERROR":
            //we ignore the remaining calls if work terminated abnormally
            getLog(entryPoint, []);
            break;
        case "UNASSIGNED":
        case "TRYINGTOASSIGN":
        case "ASSIGNED":
        case "CHECKINGSTATUS":
            // fire again
            currWorkChecker = window.setTimeout(function () {
                checkWork(entryPoint, remainingCalls)
            }, 2 * 1000);
            break;
        default:
            bfUpdateDebugInfo("Got unknown work status: ", status);
    }
}

function queueWork(worktype, entryPoint, remainingCalls) {

    if (containerName == "") {
        alert("Container name is empty");
        return;
    }
    if (testrigName == "") {
        alert("Testrig name is empty");
        return;
    }

    //var questionName = jQuery("#txtQuestionName").val();
    //if (questionName == ""
    //      && (worktype == "answerquestion" || worktype == "postflows")) {
    //   alert("Question name is empty");
    //   return;
    //}

    // real work begins
    bfUpdateDebugInfo("Doing work " + worktype);

    WorkGuid = bfGetGuid();

    var reqParams = {};
    reqParams[ARG_LOG_LEVEL] = logLevel;

    switch (worktype) {
        case "parsevendorspecific":
            reqParams[COMMAND_PARSE_VENDOR_SPECIFIC] = "";
            reqParams[ARG_UNIMPLEMENTED_SUPPRESS] = "";
            break;
        case "parsevendorindependent":
            reqParams[COMMAND_PARSE_VENDOR_INDEPENDENT] = "";
            break;
        case "generatedataplane":
            reqParams[COMMAND_WRITE_CP_FACTS] = "";
            reqParams[COMMAND_DUMP_DP] = "";
            reqParams[COMMAND_NXTNET_DATA_PLANE] = "";
            reqParams[ARG_ENVIRONMENT_NAME] = envName;
            break;
        case "generatediffdataplane":
            reqParams[COMMAND_WRITE_CP_FACTS] = "";
            reqParams[COMMAND_DUMP_DP] = "";
            reqParams[COMMAND_NXTNET_DATA_PLANE] = "";
            reqParams[ARG_ENVIRONMENT_NAME] = envName;
            reqParams[ARG_DIFF_ENVIRONMENT_NAME] = diffEnvName;
            reqParams[ARG_DIFF_ACTIVE] = "";
        case "getdataplane":
            reqParams[COMMAND_DUMP_DP] = "";
            reqParams[ARG_ENVIRONMENT_NAME] = envName;
            break;
        case "getdiffdataplane":
            reqParams[COMMAND_DUMP_DP] = "";
            reqParams[ARG_DIFF_ENVIRONMENT_NAME] = diffEnvName;
            reqParams[ARG_ENVIRONMENT_NAME] = envName;
            reqParams[ARG_DIFF_ACTIVE] = "";
            break;
        case "answerquestion":
            reqParams[COMMAND_ANSWER] = "";
            reqParams[ARG_QUESTION_NAME] = questionName;
            reqParams[ARG_ENVIRONMENT_NAME] = envName;
            reqParams[ARG_LOG_LEVEL] = LOG_LEVEL_OUTPUT;
            if (differentialQuery) {
                reqParams[ARG_DIFF_ENVIRONMENT_NAME] = diffEnvName;
            }
            break;
        default:
            alert("Unsupported work command", worktype);
    }

    var workItem = JSON.stringify([WorkGuid, containerName, testrigName, reqParams, {}]);

    var data = new FormData();
    data.append(SVC_API_KEY, API_KEY);
    data.append(SVC_WORKITEM_KEY, workItem);

    bfPostData(SVC_QUEUE_WORK_RSC, data, queueWork_cb, entryPoint, remainingCalls);
}

function queueWork_cb(response, entryPoint, remainingCalls) {
    bfUpdateDebugInfo("Work queued. Beginning to poll for status");
    checkWork(entryPoint, remainingCalls);
}


function getLog(entryPoint, remainingCalls) {
    if (containerName == "") {
        alert("Container name is empty");
        return;
    }
   if (testrigName == "") {
      alert("Testrig name is empty.");
      return;
   }
   if (WorkGuid == "") {
       alert("Workguid is not set");
       return;
   }

   var objectName = WorkGuid + ".log";

   bfGetObject(containerName, testrigName, objectName, getLog_cb, entryPoint, remainingCalls);
}

function getLog_cb(responseObject, entryPoint, remainingCalls) {
    var op = document.getElementById("divOutputInfo");
    op.textContent += responseObject;
    makeNextCall(entryPoint, remainingCalls);
}

function initContainer(entryPoint, remainingCalls) {

    //we want to make sure that the testrig file supplied before we creaate the container
    var testrigFile = jQuery("#fileUploadTestrig").get(0).files[0];

    if (typeof testrigFile === 'undefined') {
        alert("Select a testrig file");
        return;
    }

    if (containerPrefix == "") {
        alert("Container name is not set");
        return;
    }

    bfUpdateDebugInfo("Init'ing container");

    var data = new FormData();
    data.append(SVC_API_KEY, API_KEY);
    data.append(SVC_CONTAINER_PREFIX_KEY, containerPrefix);

    bfPostData(SVC_INIT_CONTAINER_RSC, data, initContainer_cb, entryPoint, remainingCalls);
}

function initContainer_cb(response, entryPoint, remainingCalls) {
    containerName = response[SVC_CONTAINER_NAME_KEY];
    bfUpdateDebugInfo("Init'd container " + containerName);
    makeNextCall(entryPoint, remainingCalls);
}

function makeNextCall(entryPoint, callList) {

    if (callList.length == 0) {
        bfUpdateDebugInfo("Done with all the calls for " + entryPoint)
    }
    else {
        var nextCall = callList[0];
        callList.shift();

        switch (nextCall) {
            case "initcontainer":
                initContainer(entryPoint, callList);
                break;
            case "inittestrig":
                uploadTestrig(entryPoint, callList);
                break;
            case "parsevendorspecific":
            case "parsevendorindependent":
            case "generatedataplane":
            case "getdataplane":
            case "generatediffdataplane":
            case "getdiffdataplane":
            case "answerquestion":
                queueWork(nextCall, entryPoint, callList);
                break;
            case "uploaddiffenvironment":
                uploadDiffEnvironment(entryPoint, callList);
                break;
            case "uploadquestion":
                uploadQuestion(entryPoint, callList);
                break;
            default:
                alert("Unsupported call", nextCall);
        }
    }
}

function startCalls(entryPoint, calls) {
    //clear the output box
    var op = document.getElementById("divOutputInfo");
    op.textContent = "";

    var callList = calls.split("::");
    makeNextCall(entryPoint, callList);
}

function uploadDiffEnvironment(entryPoint, remainingCalls) {

    // sanity check inputs
    if (containerName == "") {
        alert("Container name is not set");
        return;
    }
    if (testrigName == "") {
        alert("Testrig name is not set");
        return;
    }
    diffEnvName = jQuery("#txtDiffEnvironmentName").val();
    if (diffEnvName == "") {
        alert("Specify a differential environment name");
        return;
    }
    var envFile = jQuery("#fileUploadDiffEnvironment").get(0).files[0];
    if (typeof envFile === 'undefined') {
        alert("Select a differential environment file");
        return;
    }

    bfUpdateDebugInfo("Uploading differential environment");

    // begin the work now

   var data = new FormData();
   data.append(SVC_API_KEY, API_KEY);
   data.append(SVC_CONTAINER_NAME_KEY, containerName);
   data.append(SVC_TESTRIG_NAME_KEY, testrigName);
   data.append(SVC_ENV_NAME_KEY, diffEnvName);
   data.append(SVC_ZIPFILE_KEY, envFile);

   bfPostData(SVC_UPLOAD_ENV_RSC, data, uploadDiffEnvironment_cb, entryPoint, remainingCalls);
}

function uploadDiffEnvironment_cb(response, entryPoint, remainingCalls) {
    bfUpdateDebugInfo("Uploaded diff environment.");
    makeNextCall(entryPoint, remainingCalls);
}


function uploadQuestion(entryPoint, remainingCalls) {
    // sanity check inputs
    if (containerName == "") {
        alert("Container name is not set");
        return;
    }
    if (testrigName == "") {
        alert("Testrig name is not set");
        return;
    }
   var qFile = jQuery("#fileUploadQuestion").get(0).files[0];
   if (typeof qFile === 'undefined') {
      alert("Select a question file");
      return;
   }
   var qName = jQuery("#txtQuestionName").val();
   if (qName == "") {
       alert("Specify a question name");
       return;
   }

   var data = new FormData();
   data.append(SVC_API_KEY, API_KEY);
   data.append(SVC_CONTAINER_NAME_KEY, containerName);
   data.append(SVC_TESTRIG_NAME_KEY, testrigName);
   data.append(SVC_QUESTION_NAME_KEY, qName);
   data.append(SVC_FILE_KEY, qFile);

   bfPostData(SVC_UPLOAD_QUESTION_RSC, data, uploadQuestion_cb, entryPoint, remainingCalls);
}

function uploadQuestion_cb(response, entryPoint, remainingCalls) {
    bfUpdateDebugInfo("Uploaded question.");
    makeNextCall(entryPoint, remainingCalls);
}

function uploadTestrig(entryPoint, remainingCalls) {

    // sanity check inputs
    if (containerName == "") {
        alert("Container name is not set");
        return;
    }
    if (testrigName == "") {
        alert("Testrig name is not set");
        return;
    }
    var testrigFile = jQuery("#fileUploadTestrig").get(0).files[0];
    if (typeof testrigFile === 'undefined') {
        alert("Select a testrig file");
        return;
    }

    bfUpdateDebugInfo("Uploading testrig");

    // begin the work now
   var data = new FormData();
   data.append(SVC_API_KEY, API_KEY);
   data.append(SVC_CONTAINER_NAME_KEY, containerName);
   data.append(SVC_TESTRIG_NAME_KEY, testrigName);
   data.append(SVC_ZIPFILE_KEY, testrigFile);

   bfPostData(SVC_UPLOAD_TESTRIG_RSC, data, uploadTestrig_cb, entryPoint, remainingCalls);
}

function uploadTestrig_cb(response, entryPoint, remainingCalls) {
    bfUpdateDebugInfo("Uploaded testrig.");
    makeNextCall(entryPoint, remainingCalls);
}
