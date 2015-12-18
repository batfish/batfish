"use strict";

var LOG_LEVEL_OUTPUT = "output";
var LOG_LEVEL_WARN = "warn";

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

function bfGetObject(containerName, testrigName, objectName, callback, entryPoint, remainingCalls) {
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
            if (callback != undefined)
                callback(responseObject, entryPoint, remainingCalls);
        }
    }).fail(function () {
      bfUpdateDebugInfo("Failed to fetch " + objectName);
   });
}

function bfGetTimestamp() {
   var now = new Date();
   return now.toLocaleTimeString();
}

function bfGetGuid() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000).toString(16)
              .substring(1);
    }
    return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4()
          + s4() + s4();
}

function bfPostData(rscEndPoint, data, callback, entryPoint, remainingCalls) {

    var url_parm = SVC_WORK_MGR_ROOT + rscEndPoint;
    console.log("bfPostData: ", entryPoint, url_parm);

    jQuery.ajax({
        url: url_parm,
        type: "POST",
        contentType: false,
        processData: false,
        data: data,

        error: function (_, textStatus, errorThrown) {
            alert(entryPoint + " failed: ", textStatus, errorThrown);
        },
        success: function (response, textStatus) {
            if (response[0] === SVC_SUCCESS_KEY) {
                bfUpdateDebugInfo(entryPoint + " succeeded");
                if (callback != undefined)
                    callback(response[1], entryPoint, remainingCalls);
            }
            else {
                alert(taskname + " failed: " + response[1]);
            }
        }
    });
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

