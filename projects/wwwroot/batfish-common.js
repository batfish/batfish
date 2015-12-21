"use strict";

var SVC_WORK_MGR_ROOT;
var LOG_LEVEL = "warn";  //output, debug, ...

$(document).ajaxError(function(event, request, settings, thrownError) {
   bfUpdateDebugInfo(settings.url + " " + thrownError + " " + request);
});

var debugLog = [];
var maxLogEntries = 10000;

function bfGetGuid() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000).toString(16)
              .substring(1);
    }
    return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4()
          + s4() + s4();
}

function bfGetObject(containerName, testrigName, objectName, cbSuccess, cbFailure, entryPoint, remainingCalls) {
    var url_parm = SVC_WORK_MGR_ROOT + SVC_GET_OBJECT_RSC;
    console.log("bfGetObject: ", entryPoint, objectName);

    var data = new FormData();
    data.append(SVC_API_KEY, API_KEY);
    data.append(SVC_CONTAINER_NAME_KEY, containerName);
    data.append(SVC_TESTRIG_NAME_KEY, testrigName);
    data.append(SVC_OBJECT_KEY, objectName);

    jQuery.ajax({
        url: url_parm,
        type: "POST",
        contentType: false,
        processData: false,
        data: data,

        success: function (responseObject) {
            bfUpdateDebugInfo("Fetched " + objectName);
            if (cbSuccess != undefined)
                cbSuccess(responseObject, entryPoint, remainingCalls);
        }
    }).fail(function () {
        if (cbFailure != undefined)
            cbFailure("Failed to fetch " + objectName, entryPoint, remainingCalls);
   });
}

function bfGetTimestamp() {
   var now = new Date();
   return now.toLocaleTimeString();
}

function bfPostData(rscEndPoint, data, cbSuccess, cbFailure, entryPoint, remainingCalls) {

    var url_parm = SVC_WORK_MGR_ROOT + rscEndPoint;
    console.log("bfPostData: ", entryPoint, url_parm);

    jQuery.ajax({
        url: url_parm,
        type: "POST",
        contentType: false,
        processData: false,
        data: data,

        error: function (_, textStatus, errorThrown) {
            if (cbFailure != undefined)
                cbFailure("PostData failed: " + textStatus + " " + errorThrown, entryPoint, remainingCalls);
        },
        success: function (response, textStatus) {
            if (response[0] === SVC_SUCCESS_KEY) {
                bfUpdateDebugInfo(entryPoint + " succeeded");
                if (cbSuccess != undefined)
                    cbSuccess(response[1], entryPoint, remainingCalls);
            }
            else {
                if (cbFailure != undefined)
                    cbFailure("API call fail: " + response[1], entryPoint, remainingCalls);
            }
        }
    });
}

function bfInitialize() {
    var hostname = location.hostname;
    if (hostname == "")
        hostname = "localhost";
    var protocol = (SVC_DISABLE_SSL) ? "http" : "https";
    SVC_WORK_MGR_ROOT = protocol + "://" + hostname + ":" + SVC_WORK_PORT + SVC_BASE_WORK_MGR + "/";

   bfUpdateDebugInfo("Coordinator location is set to " + SVC_WORK_MGR_ROOT);
}

function bfUpdateDebugInfo(string) {
   debugLog.splice(0, 0, bfGetTimestamp() + " " + string);
   while (debugLog.length > maxLogEntries) {
      debugLog.shift();
   }
   $("#divDebugInfo").html(debugLog.join("\n"));
}

