$(document).ready(function() {
   fnGetCoordinatorWorkQueueStatus();
});

var currWorkChecker;

var WorkGuids = {};

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
         // fire again
         currWorkChecker = window.setTimeout(function() {
            fnCheckWork(worktype)
         }, 2 * 1000);
         break;
      default:
         bfUpdateDebugInfo("Got unknown work status: ", status);
      }
   }
   else {
      bfUpdateDebugInfo("Work status check failed: " + result[1]);
   }
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

function doFollowOnWork(worktype) {

   if (DEMO_MODE == 0)
      return;

   var dataPlaneQuery = document.getElementById("chkDataPlaneQuery").checked;
   var differentialQuery = document.getElementById("chkDifferentialQuery").checked;
   var qName = jQuery("#txtQuestionName").val();
   switch (worktype) {
   case "vendorspecific":
      fnDoWork("vendorindependent");
      break;
   case "vendorindependent":
      // no follow on work to be done here
      bfUpdateDebugInfo("Done generating common control plane");
      break;
   case "generatefacts":
      // no follow on work to be done here
      bfUpdateDebugInfo("Done dumping control plane for base environment");
      break;
   case "generatedifffacts":
      // no follow on work to be done here
      bfUpdateDebugInfo("Done dumping control plane for differential environment");
      break;
   case "generatedataplane":
      fnDoWork("getdataplane");
      break;
   case "generatediffdataplane":
      fnDoWork("getdiffdataplane");
      break;
   case "getdataplane":
      // no follow on work to be done here
      bfUpdateDebugInfo("Done generating base data plane");
      break;
   case "getdiffdataplane":
      // no follow on work to be done here
      bfUpdateDebugInfo("Done generating differential data plane");
      break;
   case "gethistory":
      // no follow on work to be done here
      bfUpdateDebugInfo("Done answering query");
      break;
   case "getdiffhistory":
      // no follow on work to be done here
      bfUpdateDebugInfo("Done answering differential query");
      break;
   case "getz3encoding":
      // no follow on work to be done here
      bfUpdateDebugInfo("Done dumping z3 encoding");
      break;
   case "answerquestion":
      if (dataPlaneQuery) {
         if (differentialQuery) {
            fnDoWork("postdiffflows");
         }
         else {
            fnDoWork("postflows");
         }
      }
      else {
         bfUpdateDebugInfo("Done answering query");
      }
      break;
   case "postflows":
      fnDoWork("gethistory");
      break;
   case "postdiffflows":
      fnDoWork("getdiffhistory");
      break;
   case "getflowtraces":
      // no follow on work to be done here
      bfUpdateDebugInfo("Done answering query");
      break;
   case "writeroutes":
      // no follow on work to be done here
      bfUpdateDebugInfo("Done generating precomputed routes");
      break;
   default:
      alert("Unsupported work command", worktype);
   }
}

function fnAddWorker() {
   var worker = jQuery("#txtAddWorker").val();

   if (worker == "") {
      alert("Specify a worker first");
      return;
   }

   bfGetJson("AddWorker-" + worker, SVC_POOL_MGR_ROOT + SVC_POOL_UPDATE_RSC
         + "?add=" + worker, bfGenericCallback, "");
}

function fnAnswerQuestion() {
   var testrigName = jQuery("#txtTestrigName").val();
   if (testrigName == "") {
      alert("Specify a testrig name");
      return;
   }
   var qFile = jQuery("#fileUploadQuestion").get(0).files[0];
   var fileSpecified = (typeof qFile === 'undefined');
   var qName = jQuery("#txtQuestionName").val();
   var nameSpecified = (qName != "");
   if (!nameSpecified) {
      if (fileSpecified) {
         jQuery("#txtQuestionName").val(qFile.name);
         qName = jQuery("#txtQuestionName").val();
      }
      else {
         alert("Specify a question name");
         return;
      }
   }
   fnDoWork("answerquestion");
}

function fnCheckWork(worktype) {

   // delete any old work checker
   window.clearTimeout(currWorkChecker);

   var uuid = jQuery("#txtWorkGuid").val();
   if (uuid == "") {
      alert("Work GUID is empty. Cannot check status");
      return;
   }

   bfGetJson("Checkwork-" + uuid, SVC_WORK_MGR_ROOT
         + SVC_WORK_GET_WORKSTATUS_RSC + "?" + SVC_WORKID_KEY + "=" + uuid,
         cbCheckWork, worktype);
}

function fnDoWork(worktype) {
   var uuidCurrWork = guid();

   WorkGuids[worktype] = uuidCurrWork;

   // set the guid of the text field
   jQuery("#txtWorkGuid").val(uuidCurrWork);

   var testrigName = jQuery("#txtTestrigName").val();
   if (testrigName == "") {
      alert("Testrig name is empty");
      return;
   }

   var envName = jQuery("#txtEnvironmentName").val();
   if (envName == "" && worktype.substring(0, 6) == "vendor") { // vendor*
      // worktype
      // does not
      // need an
      // environment
      alert("Environment name is empty");
      return;
   }

   var diffEnvName = jQuery("#txtDiffEnvironmentName").val();

   var outputEnvName = jQuery("#txtOutputEnvironmentName").val();
   if (outputEnvName == "" && worktype == "writeroutes") {
      alert("Output environment name is empty");
      return;
   }

   var questionName = jQuery("#txtQuestionName").val();
   if (questionName == ""
         && (worktype == "answerquestion" || worktype == "postflows")) {
      alert("Question name is empty");
      return;
   }

   var dataPlaneQuery = document.getElementById("chkDataPlaneQuery").checked;
   var differentialQuery = document.getElementById("chkDifferentialQuery").checked;

   var reqParams = {};

   switch (worktype) {
   case "gethistory":
      reqParams[COMMAND_GET_HISTORY] = "";
      reqParams[ARG_ENVIRONMENT_NAME] = envName;
      reqParams[ARG_QUESTION_NAME] = questionName;
      reqParams[ARG_LOG_LEVEL] = LOG_LEVEL_OUTPUT;
      break;
   case "getdiffhistory":
      reqParams[COMMAND_GET_DIFFERENTIAL_HISTORY] = "";
      reqParams[ARG_ENVIRONMENT_NAME] = envName;
      reqParams[ARG_DIFF_ENVIRONMENT_NAME] = diffEnvName;
      reqParams[ARG_QUESTION_NAME] = questionName;
      reqParams[ARG_LOG_LEVEL] = LOG_LEVEL_OUTPUT;
      break;
   case "vendorspecific":
      reqParams[COMMAND_PARSE_VENDOR_SPECIFIC] = "";
      reqParams[ARG_UNIMPLEMENTED_SUPPRESS] = "";
      reqParams[ARG_LOG_LEVEL] = LOG_LEVEL_WARN;
      break;
   case "vendorindependent":
      reqParams[COMMAND_PARSE_VENDOR_INDEPENDENT] = "";
      reqParams[ARG_LOG_LEVEL] = LOG_LEVEL_WARN;
      break;
   case "generatefacts":
      reqParams[COMMAND_GENERATE_FACT] = "";
      reqParams[ARG_ENVIRONMENT_NAME] = envName;
      reqParams[ARG_LOG_LEVEL] = LOG_LEVEL_WARN;
      break;
   case "generatedifffacts":
      reqParams[COMMAND_GENERATE_FACT] = "";
      reqParams[ARG_ENVIRONMENT_NAME] = envName;
      reqParams[ARG_DIFF_ENVIRONMENT_NAME] = diffEnvName;
      reqParams[ARG_LOG_LEVEL] = LOG_LEVEL_WARN;
      break;
   case "generatedataplane":
      reqParams[COMMAND_CREATE_WORKSPACE] = "";
      reqParams[COMMAND_FACTS] = "";
      reqParams[ARG_ENVIRONMENT_NAME] = envName;
      break;
   case "generatediffdataplane":
      reqParams[COMMAND_CREATE_WORKSPACE] = "";
      reqParams[COMMAND_FACTS] = "";
      reqParams[ARG_DIFF_ENVIRONMENT_NAME] = diffEnvName;
      reqParams[ARG_ENVIRONMENT_NAME] = envName;
      break;
   case "getdataplane":
      reqParams[COMMAND_DUMP_DP] = "";
      reqParams[ARG_ENVIRONMENT_NAME] = envName;
      break;
   case "getdiffdataplane":
      reqParams[COMMAND_DUMP_DP] = "";
      reqParams[ARG_DIFF_ENVIRONMENT_NAME] = diffEnvName;
      reqParams[ARG_ENVIRONMENT_NAME] = envName;
      break;
   case "getz3encoding":
      reqParams[COMMAND_SYNTHESIZE_Z3_DATA_PLANE] = "";
      reqParams[ARG_ENVIRONMENT_NAME] = envName;
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
   case "postflows":
      reqParams[COMMAND_POST_FLOWS] = "";
      reqParams[ARG_QUESTION_NAME] = questionName;
      reqParams[ARG_ENVIRONMENT_NAME] = envName;
      break;
   case "postdiffflows":
      reqParams[COMMAND_POST_DIFFERENTIAL_FLOWS] = "";
      reqParams[ARG_QUESTION_NAME] = questionName;
      reqParams[ARG_ENVIRONMENT_NAME] = envName;
      reqParams[ARG_DIFF_ENVIRONMENT_NAME] = diffEnvName;
      break;
   case "getflowtraces":
      reqParams[COMMAND_QUERY] = "";
      reqParams[ARG_PREDICATES] = PREDICATE_FLOW_PATH_HISTORY;
      reqParams[ARG_ENVIRONMENT_NAME] = envName;
      break;
   case "writeroutes":
      reqParams[COMMAND_WRITE_ROUTES] = "";
      reqParams[ARG_OUTPUT_ENV] = outputEnvName;
      reqParams[ARG_ENVIRONMENT_NAME] = envName;
      break;
   default:
      alert("Unsupported work command", worktype);
   }

   var workItem = JSON.stringify([ uuidCurrWork, testrigName, reqParams, {} ]);
   var rawURL = SVC_WORK_MGR_ROOT + SVC_WORK_QUEUE_WORK_RSC + "?"
         + SVC_WORKITEM_KEY + "=" + workItem;
   var encodedURL = encodeURI(rawURL);

   bfGetJson("DoWork:" + worktype, encodedURL, cbDoWork, worktype);
}

function fnGetCoordinatorWorkQueueStatus() {
   bfGetJson("GetCoordinatorWorkQueueStatus", SVC_WORK_MGR_ROOT
         + SVC_WORK_GETSTATUS_RSC, cbGetCoordinatorWorkQueueStatus, "");
}

function fnGetLog(worktype) {
   var testrigName = jQuery("#txtTestrigName").val();
   if (testrigName == "") {
      alert("Testrig name is empty.");
      return;
   }

   var uuidWork = "";

   if (typeof worktype === 'undefined') {
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
   if (envName == "" && worktype.substring(0, 6) == "vendor") { // vendor*
      // worktype
      // does not
      // need an
      // environment
      alert("Environment name is empty");
      return;
   }

   var objectName = "";

   switch (worktype) {
   case "vendorspecific":
      objectName = RELPATH_VENDOR_SPECIFIC_CONFIG_DIR;
      bfGetObject(testrigName, objectName);
      break;
   case "vendorindependent":
      objectName = RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR;
      bfGetObject(testrigName, objectName);
      break;
   case "generatefacts":
      objectName = [ RELPATH_ENVIRONMENTS_DIR, envName, RELPATH_FACT_DUMP_DIR ]
            .join("/");
      bfGetObject(testrigName, objectName);
      break;
   case "getdataplane":
      objectName = [ RELPATH_ENVIRONMENTS_DIR, envName, RELPATH_DATA_PLANE_DIR ]
            .join("/");
      bfGetObject(testrigName, objectName);
      break;
   case "getz3encoding":
      objectName = [ RELPATH_ENVIRONMENTS_DIR, envName,
            RELPATH_Z3_DATA_PLANE_FILE ].join("/");
      bfGetObject(testrigName, objectName);
      break;
   case "getflowtraces":
      objectName = [ RELPATH_ENVIRONMENTS_DIR, envName, RELPATH_QUERY_DUMP_DIR ]
            .join("/");
      bfDownloadObject(testrigName, objectName);
      break;
   default:
      alert("Unsupported worktype for get result", worktype);
   }

   if (objectName == "") {
      alert("Could not determine the right object name to fetch");
      return;
   }

}

function fnUploadDiffEnvironment() {

   var testrigName = jQuery("#txtTestrigName").val();
   if (testrigName == "") {
      alert("Specify a testrig name");
      return;
   }

   var envName = jQuery("#txtDiffEnvironmentName").val();
   if (envName == "") {
      alert("Specify a differential environment name");
      return;
   }

   var envFile = jQuery("#fileUploadDiffEnvironment").get(0).files[0];
   if (typeof envFile === 'undefined') {
      alert("Select a differential environment file");
      return;
   }

   var data = new FormData();
   data.append(SVC_TESTRIG_NAME_KEY, testrigName);
   data.append(SVC_ENV_NAME_KEY, envName);
   data.append(SVC_ZIPFILE_KEY, envFile);

   bfUploadData("UploadEnvironment-" + envName, SVC_WORK_MGR_ROOT
         + SVC_WORK_UPLOAD_ENV_RSC, data, cbUploadData, "environment");
}

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

   bfUploadData("UploadEnvironment-" + envName, SVC_WORK_MGR_ROOT
         + SVC_WORK_UPLOAD_ENV_RSC, data, cbUploadData, "environment");
}

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

   bfUploadData("UploadQuestion-" + qName, SVC_WORK_MGR_ROOT
         + SVC_WORK_UPLOAD_QUESTION_RSC, data, undefined, undefined);
}

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

   bfUploadData("UploadTestrig " + testrigName, SVC_WORK_MGR_ROOT
         + SVC_WORK_UPLOAD_TESTRIG_RSC, data, cbUploadData, "testrig");
}

function fnWriteRoutes() {
   var testrigName = jQuery("#txtTestrigName").val();
   if (testrigName == "") {
      alert("Specify a testrig name");
      return;
   }

   var iName = jQuery("#txtEnvironmentName").val();
   var iNameSpecified = (iName != "");
   if (!iNameSpecified) {
      alert("Specify an input environment name");
      return;
   }

   var oName = jQuery("#txtOutputEnvironmentName").val();
   var oNameSpecified = (oName != "");
   if (!oNameSpecified) {
      alert("Specify an output environment name");
      return;
   }

   fnDoWork("writeroutes");
}

function guid() {
   function s4() {
      return Math.floor((1 + Math.random()) * 0x10000).toString(16)
            .substring(1);
   }
   return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4()
         + s4() + s4();
}
