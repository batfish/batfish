/// <reference path="batfish-common.js" />

//these structures are indexed on entrypoint
var epCurrWorkChecker = new Object();
var epWorkGuid = new Object();
var epOutput = new Object();
var epQuestionName = new Object();

function checkWork(entryPoint, remainingCalls) {
   // delete any old work checker
   window.clearTimeout(epCurrWorkChecker[entryPoint]);

   if  (!(entryPoint in epWorkGuid)) {
      alert("Work GUID is empty. Cannot check status");
      return;
   }

   var data = new FormData();
   data.append(SVC_API_KEY, apiKey);
   data.append(SVC_WORKID_KEY, epWorkGuid[entryPoint]);

   bfPostData(SVC_GET_WORKSTATUS_RSC, data, checkWork_cb, genericFailure_cb, entryPoint, remainingCalls);
}

function checkWork_cb(response, entryPoint, remainingCalls) {
    var status = response[SVC_WORKSTATUS_KEY];

    bfUpdateDebugInfo("Workcheck returned with response " + status);

    switch (status) {
        case "TERMINATEDNORMALLY":
            getLog(entryPoint, remainingCalls);
            delete epCurrWorkChecker[entryPoint];
            delete epWorkGuid[entryPoint];
            break;
        case "TERMINATEDABNORMALLY":
        case "ASSIGNMENTERROR":
            //we ignore the remaining calls if work terminated abnormally
            getLog(entryPoint, []);
            delete epCurrWorkChecker[entryPoint];
            delete epWorkGuid[entryPoint];
            break;
        case "UNASSIGNED":
        case "TRYINGTOASSIGN":
        case "ASSIGNED":
        case "CHECKINGSTATUS":
            // fire again
            epCurrWorkChecker[entryPoint] = window.setTimeout(function () {
                checkWork(entryPoint, remainingCalls)
            }, 2 * 1000);
            break;
        default:
            bfUpdateDebugInfo("Got unknown work status: ", status);
    }
}

function errorCheck(isError, message, entryPoint) {
    if (isError) {
        finishEntryPoint(entryPoint);
        alert(message);
        return true;
    }
    return false;
}

// we are done with this entrypoint
// display any output and delete everything
function finishEntryPoint(entryPoint, remainingCalls) {

    //jQuery(elementOutputText).val(epOutput[entryPoint]);
    bfUpdateOutput(epOutput[entryPoint]);

    delete epCurrWorkChecker[entryPoint];
    delete epOutput[entryPoint];
    delete epWorkGuid[entryPoint];
    delete epQuestionName[entryPoint];
}

function getLog(entryPoint, remainingCalls) {
    if (errorCheck(bfIsInvalidStr(containerName), "Container name is empty", entryPoint) ||
        errorCheck(bfIsInvalidStr(testrigName), "Testrig name is empty.", entryPoint) ||
        errorCheck(!(entryPoint in epWorkGuid), "epWorkGuid is not set", entryPoint))
       return;

   var objectName = epWorkGuid[entryPoint] + ".log";

   bfGetObject(containerName, testrigName, objectName, getLog_cb, genericFailure_cb, entryPoint, remainingCalls);
}

function getLog_cb(responseObject, entryPoint, remainingCalls) {
    epOutput[entryPoint] += responseObject;
    makeNextCall(entryPoint, remainingCalls);
}

// we encountered a failure
// add the failure message to output and then finish
function genericFailure_cb(message, entryPoint, remainingCalls) {
    epOutput[entryPoint] += message;
    finishEntryPoint(entryPoint);
}

function initContainer(entryPoint, remainingCalls) {
    if (errorCheck(bfIsInvalidStr(containerPrefix), "Container name is not set", entryPoint))
        return;

    bfUpdateDebugInfo("Init'ing container");

    var data = new FormData();
    data.append(SVC_API_KEY, apiKey);
    data.append(SVC_CONTAINER_PREFIX_KEY, containerPrefix);

    bfPostData(SVC_INIT_CONTAINER_RSC, data, initContainer_cb, genericFailure_cb, entryPoint, remainingCalls);
}

function initContainer_cb(response, entryPoint, remainingCalls) {
    containerName = response[SVC_CONTAINER_NAME_KEY];
    bfUpdateDebugInfo("Init'd container " + containerName);
    makeNextCall(entryPoint, remainingCalls);
}

function makeNextCall(entryPoint, callList) {

    if (callList.length == 0) {
        bfUpdateDebugInfo("Done with all the calls for " + entryPoint)

        finishEntryPoint(entryPoint);
    }
    else {
        var nextCall = callList[0];
        callList.shift();

        switch (nextCall) {
            case "initcontainer":
                initContainer(entryPoint, callList);
                break;
            case "uploadtestrigfile":
                uploadTestrigFile(entryPoint, callList);
                break;
            case "uploadtestrigsmart":
                uploadTestrigSmart(entryPoint, callList);
                break;
            case "uploadtestrigtext":
                uploadTestrigText(entryPoint, callList);
                break;
            case "parsevendorspecific":
            case "parsevendorindependent":
            case "generatedataplane":
            case "getdataplane":
            case "generatediffdataplane":
            case "getdiffdataplane":
            case "answerquestion":
            case "answerdiffquestion":
                queueWork(nextCall, entryPoint, callList);
                break;
            case "uploaddiffenvfile":
                uploadDiffEnvFile(entryPoint, callList);
                break;
            case "uploaddiffenvsmart":
                uploadDiffEnvSmart(entryPoint, callList);
                break;
            case "uploadquestionfile":
                uploadQuestionFile(entryPoint, callList);
                break;
            case "uploadquestiontext":
                uploadQuestionText(entryPoint, callList);
                break;
            case "postdiffenvinit":
                postDiffEnvInit(entryPoint, callList);
                break;
            case "posttestriginit":
                postTestrigInit(entryPoint, callList);
                break;
            default:
                alert("Unsupported call", nextCall);
        }
    }
}

function postDiffEnvInit(entryPoint, remainingCalls) {
    if (errorCheck((typeof elementAnswerDiffQuestionBtn === 'undefined' || bfIsInvalidElement(elementAnswerDiffQuestionBtn)),
                "Answer diff question button element (elementAnswerDiffQuestionBtn) is not configured in the HTML header",
                entryPoint))
        return;

    jQuery(elementAnswerDiffQuestionBtn).prop('disabled', false);

    makeNextCall(entryPoint, remainingCalls);
}

function postTestrigInit(entryPoint, remainingCalls) {
    if (errorCheck((typeof elementUploadBaseTestrigBtn === 'undefined' || bfIsInvalidElement(elementUploadBaseTestrigBtn)),
                "Upload base config button element (elementUploadBaseTestrigBtn) is not configured in the HTML header",
                entryPoint) ||
        errorCheck((typeof elementUploadDiffEnvBtn === 'undefined' || bfIsInvalidElement(elementUploadDiffEnvBtn)),
                "Upload differential config button element (elementUploadDiffEnvBtn) is not configured in the HTML header",
                entryPoint) ||
        errorCheck((typeof elementAnswerQuestionBtn === 'undefined' || bfIsInvalidElement(elementAnswerQuestionBtn)),
                "Answer question button element (elementAnswerQuestionBtn) is not configured in the HTML header",
                entryPoint))
        return;

    jQuery(elementUploadBaseTestrigBtn).prop('disabled', true);
    jQuery(elementUploadDiffEnvBtn).prop('disabled', false);
    jQuery(elementAnswerQuestionBtn).prop('disabled', false);

    makeNextCall(entryPoint, remainingCalls);
}


function queueWork(worktype, entryPoint, remainingCalls) {

    if (errorCheck(bfIsInvalidStr(containerName), "Container name is empty", entryPoint) ||
        errorCheck(bfIsInvalidStr(testrigName), "Testrig name is empty", entryPoint))
        return;

    // real work begins
    bfUpdateDebugInfo("Queueing work of type " + worktype);

    epWorkGuid[entryPoint] = bfGetGuid();

    var reqParams = {};
    reqParams[ARG_LOG_LEVEL] = LOG_LEVEL;

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
        case "answerdiffquestion":
            //many checks and tasks are common to both answer commands; do those first, and then special case for diff
            if (errorCheck(!(entryPoint in epQuestionName), "Question name is not set in epQuestionName", entryPoint))
                return;

            reqParams[COMMAND_ANSWER] = "";
            reqParams[ARG_QUESTION_NAME] = epQuestionName[entryPoint];
            reqParams[ARG_ENVIRONMENT_NAME] = envName;
            if (! bfIsInvalidStr(diffEnvName)) {
                reqParams[ARG_DIFF_ENVIRONMENT_NAME] = diffEnvName;
            }
            reqParams[COMMAND_NXTNET_TRAFFIC] = "";
            reqParams[COMMAND_GET_HISTORY] = "";

            if (worktype == "answerdiffquestion") {
                if (errorCheck(bfIsInvalidStr(diffEnvName), "Diff environment is not set", entryPoint))
                    return;

                reqParams[ARG_DIFF_ENVIRONMENT_NAME] = diffEnvName;
                reqParams[ARG_DIFF_ACTIVE] = "";
            }

            break;
        default:
            alert("Unsupported work command", worktype);
    }

    var workItem = JSON.stringify([epWorkGuid[entryPoint], containerName, testrigName, reqParams, {}]);

    var data = new FormData();
    data.append(SVC_API_KEY, apiKey);
    data.append(SVC_WORKITEM_KEY, workItem);

    bfPostData(SVC_QUEUE_WORK_RSC, data, queueWork_cb, genericFailure_cb, entryPoint, remainingCalls);
}

function queueWork_cb(response, entryPoint, remainingCalls) {
    bfUpdateDebugInfo("Work queued. Beginning to poll for status");
    checkWork(entryPoint, remainingCalls);
}


function startCalls(entryPoint, calls) {

    entryPoint += "__" + bfGetGuid();

    //initialize the epOutput to empty
    epOutput[entryPoint] = "";

    var callList = calls.split("::");
    makeNextCall(entryPoint, callList);
}

// this is a test function whose contents change based on what we want to test
function testMe() {
    containerName = "js_9b23b69d-e0f7-4034-8d4c-954a1c9eaa86";
    queueWork("answerquestion", "abc", []);
}

function txtToConfigBlob(entryPoint, elementText) {

    var configText = jQuery(elementText).val();
    if (errorCheck(bfIsInvalidStr(configText), "Enter text", entryPoint))
        return;

    var zip = new JSZip();

    var topLevelFolder = zip.folder("testrig");
    var configFolder = topLevelFolder.folder("configs");
    configFolder.file("device.cfg", configText);

    var content = zip.generate({ type: "blob" });

    return content;
}

function uploadDifEnvFile(entryPoint, remainingCalls) {

    if (errorCheck((typeof elementDiffEnvFile === 'undefined' || bfIsInvalidElement(elementDiffEnvFile)),
                    "Diff environment file selector (elementDiffEnvFile) is not configured in the HTML header",
                    entryPoint))
        return;

    var envFile = jQuery(elementDiffEnvFile).get(0).files[0];
    if (errorCheck(typeof envFile === 'undefined', "Select a differential environment file", entryPoint))
        return;

    uploadDiffEnvFinal(entryPoint, remainingCalls, envFile);
}

function uploadDiffEnvFinal(entryPoint, remainingCalls, envBlobOrFile) {

    if (errorCheck(bfIsInvalidStr(containerName), "Container name is not set", entryPoint) ||
        errorCheck(bfIsInvalidStr(testrigName), "Testrig name is not set", entryPoint))
        return;

    diffEnvName = "de_" + bfGetGuid();

    bfUpdateDebugInfo("Uploading differential environment");

    var data = new FormData();
    data.append(SVC_API_KEY, apiKey);
    data.append(SVC_CONTAINER_NAME_KEY, containerName);
    data.append(SVC_TESTRIG_NAME_KEY, testrigName);
    data.append(SVC_ENV_NAME_KEY, diffEnvName);
    data.append(SVC_ZIPFILE_KEY, envBlobOrFile);

    bfPostData(SVC_UPLOAD_ENV_RSC, data, uploadDifEnv_cb, genericFailure_cb, entryPoint, remainingCalls);
}

function uploadDiffEnvSmart(entryPoint, remainingCalls) {
    // sanity check inputs
    if (errorCheck(typeof elementConfigText === 'undefined' || bfIsInvalidElement(elementConfigText),
                    "Config text element (elementConfigText) is not configured in the HTML header",
                    entryPoint))
        return;

    if (testrigZip == "") {
        if (errorCheck(typeof elementConfigText === 'undefined' || bfIsInvalidElement(elementConfigText),
                        "Config text element (elementConfigText) is not configured in the HTML header",
                        entryPoint))
            return;

        var content = txtToConfigBlob(entryPoint, elementConfigText);
        uploadDiffEnvFinal(entryPoint, remainingCalls, content);
    }
    else {
        var content = testrigZip.generate({ type: "blob" });
        uploadDiffEnvFinal(entryPoint, remainingCalls, content);
    }
}


function uploadDifEnv_cb(response, entryPoint, remainingCalls) {
    bfUpdateDebugInfo("Uploaded diff environment.");
    makeNextCall(entryPoint, remainingCalls);
}

function uploadQuestionFile(entryPoint, remainingCalls) {
    // sanity check inputs
    if (errorCheck(bfIsInvalidStr(containerName), "Container name is not set", entryPoint) ||
        errorCheck(bfIsInvalidStr(testrigName), "Testrig name is not set", entryPoint) ||
        errorCheck(typeof elementQuestionFile === 'undefined' || bfIsInvalidElement(elementQuestionFile),
                   "Questoin file element (elementQuestionFile) is not configured in the HTML header",
                    entryPoint) ||
        errorCheck(typeof elementQuestionParams === 'undefined' || bfIsInvalidElement(elementQuestionParams),
                   "Question parameters element (elementQuestionParams) is not configured in the HTML header",
                    entryPoint))
        return;
 
    var qFile = jQuery(elementQuestionFile).get(0).files[0];
    if (errorCheck(typeof qFile === 'undefined'), "Select a question file", entryPoint)
      return;

    epQuestionName[entryPoint] = bfGetGuid();

   var paramsString = jQuery(elementQuestionParams).val();
   var paramBlob = new Blob([paramsString]);

   var data = new FormData();
   data.append(SVC_API_KEY, apiKey);
   data.append(SVC_CONTAINER_NAME_KEY, containerName);
   data.append(SVC_TESTRIG_NAME_KEY, testrigName);
   data.append(SVC_QUESTION_NAME_KEY, epQuestionName[entryPoint]);
   data.append(SVC_FILE_KEY, qFile);
   data.append(SVC_FILE2_KEY, paramBlob);

   bfPostData(SVC_UPLOAD_QUESTION_RSC, data, uploadQuestion_cb, genericFailure_cb, entryPoint, remainingCalls);
}

function uploadQuestionText(entryPoint, remainingCalls) {
    // sanity check inputs
    if (errorCheck(bfIsInvalidStr(containerName), "Container name is not set", entryPoint) ||
        errorCheck(bfIsInvalidStr(testrigName), "Testrig name is not set", entryPoint) ||
        errorCheck(typeof elementQuestionText === 'undefined' || bfIsInvalidElement(elementQuestionText),
                   "Question text element (elementQuestionText) is not configured in the HTML header",
                    entryPoint))
        return;

    var qText = jQuery(elementQuestionText).val();
    if (errorCheck(bfIsInvalidStr(qText), "Enter question text", entryPoint))
        return;

    var qBlob = new Blob([qText]);

    //parameters will be empty for this
    var paramBlob = new Blob([""]);

    epQuestionName[entryPoint] = bfGetGuid();

    var data = new FormData();
    data.append(SVC_API_KEY, apiKey);
    data.append(SVC_CONTAINER_NAME_KEY, containerName);
    data.append(SVC_TESTRIG_NAME_KEY, testrigName);
    data.append(SVC_QUESTION_NAME_KEY, epQuestionName[entryPoint]);
    data.append(SVC_FILE_KEY, qBlob);
    data.append(SVC_FILE2_KEY, paramBlob);

    bfPostData(SVC_UPLOAD_QUESTION_RSC, data, uploadQuestion_cb, genericFailure_cb, entryPoint, remainingCalls);
}

function uploadQuestion_cb(response, entryPoint, remainingCalls) {
    bfUpdateDebugInfo("Uploaded question.");
    makeNextCall(entryPoint, remainingCalls);
}

function uploadTestrigFile(entryPoint, remainingCalls) {
    if (errorCheck(typeof elementTestrigFile === 'undefined' || bfIsInvalidElement(elementTestrigFile),
                   "Testrig file element (elementTestrigFile) is not configured in the HTML header",
                    entryPoint))
        return;

    var testrigFile = jQuery(elementTestrigFile).get(0).files[0];
    if (errorCheck(typeof testrigFile === 'undefined', "Select a testrig file", entryPoint))
        return;

    uploadTestrigFinal(entryPoint, remainingCalls, testrigFile);
}

function uploadTestrigFinal(entryPoint, remainingCalls, testrigBlobOrFile) {
    // sanity check inputs
    if (errorCheck(bfIsInvalidStr(containerName), "Container name is not set", entryPoint) ||
        errorCheck(bfIsInvalidStr(testrigName), "Testrig name is not set", entryPoint))
        return;

    bfUpdateDebugInfo("Uploading testrig");

    // begin the work now
    var data = new FormData();
    data.append(SVC_API_KEY, apiKey);
    data.append(SVC_CONTAINER_NAME_KEY, containerName);
    data.append(SVC_TESTRIG_NAME_KEY, testrigName);
    data.append(SVC_ZIPFILE_KEY, testrigBlobOrFile);

    bfPostData(SVC_UPLOAD_TESTRIG_RSC, data, uploadTestrig_cb, genericFailure_cb, entryPoint, remainingCalls);
}

function uploadTestrigSmart(entryPoint, remainingCalls) {
    // sanity check inputs
    if (errorCheck(typeof elementConfigText === 'undefined' || bfIsInvalidElement(elementConfigText),
                    "Config text element (elementConfigText) is not configured in the HTML header",
                    entryPoint))
        return;

    if (testrigZip == "") {
        if (errorCheck(typeof elementConfigText === 'undefined' || bfIsInvalidElement(elementConfigText),
                        "Config text element (elementConfigText) is not configured in the HTML header",
                        entryPoint))
            return;

        var content = txtToConfigBlob(entryPoint, elementConfigText);
        uploadTestrigFinal(entryPoint, remainingCalls, content);
    }
    else {
        var content = testrigZip.generate({ type: "blob" });
        uploadTestrigFinal(entryPoint, remainingCalls, content);
    }
}

//function uploadTestrigText(entryPoint, remainingCalls) {
//    if (errorCheck(typeof elementConfigText === 'undefined' || bfIsInvalidElement(elementConfigText),
//                    "Config text element (elementConfigText) is not configured in the HTML header",
//                    entryPoint))
//      return;

//    var configText = jQuery(elementConfigText).val();
//    if (errorCheck(bfIsInvalidStr(configText), "Enter configuration", entryPoint))
//        return;

//    bfUpdateDebugInfo("Uploading config text");

//    var zip = new JSZip();

//    var topLevelFolder = zip.folder("testrig");
//    var configFolder = topLevelFolder.folder("configs");
//    configFolder.file("device.cfg", configText);

//    var content = zip.generate({ type: "blob" });

//    uploadTestrigFinal(entryPoint, remainingCalls, content);
//}

function uploadTestrig_cb(response, entryPoint, remainingCalls) {
    bfUpdateDebugInfo("Uploaded testrig.");
    makeNextCall(entryPoint, remainingCalls);
}
