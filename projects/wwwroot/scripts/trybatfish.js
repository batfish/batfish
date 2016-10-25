/// <reference path="batfish-common.js" />
/// <reference path="workclient.js" />
$.ajaxSetup ({
//        Disable caching of AJAX responses
            cache: false
            });
var spinOpts = {
    lines: 13 // The number of lines to draw
, length: 28 // The length of each line
, width: 14 // The line thickness
, radius: 42 // The radius of the inner circle
, scale: 1 // Scales overall size of the spinner
, corners: 1 // Corner roundness (0..1)
, color: '#000' // #rgb or #rrggbb or array of colors
, opacity: 0.25 // Opacity of the lines
, rotate: 0 // The rotation offset
, direction: 1 // 1: clockwise, -1: counterclockwise
, speed: 1 // Rounds per second
, trail: 60 // Afterglow percentage
, fps: 20 // Frames per second when using setTimeout() as a fallback for CSS
, zIndex: 2e9 // The z-index (defaults to 2000000000)
, className: 'spinner' // The CSS class to assign to the spinner
, top: '50%' // Top position relative to parent
, left: '50%' // Left position relative to parent
, shadow: false // Whether to render a shadow
, hwaccel: false // Whether to use hardware acceleration
, position: 'absolute' // Element positioning
};

function login(entryPoint, remainingCalls)
{
	console.log(apiKey);
	$(elementTabs).tabs("option", "active", 1);
	$('#btnLogin').button("disable");
	$(elementUploadBaseTestrigBtn).button("enable");
	finishEntryPoint(entryPoint, remainingCalls);
}
						
var questions = [];

function questionInfo(_id, _questionText, _output, _highlights) {
	this.id = _id;
	this.questionText = _questionText;
	this.output = _output;
	this.highlights = _highlights;
}

function showOldQuestion(dropDownId) {
	console.log(jQuery(dropDownId).val());
	var index = jQuery(dropDownId).val();
	jQuery(elementQuestionText).val(questions[index].questionText);
	jQuery(elementOutputText).val(questions[index].output);
	if (fnShowHighlights != undefined) {
		fnShowHighlights(questions[index].highlights);
	}
}


// This function is called just before showing highlights and outout. This is a bad hack. 
// By the time we come here, there is no
// guarantee that question text has stayed intact (the user can play with it). 
// The right thing to do is to make a call to the server.
// Until we implement that, we assume that the user does not do all this.

function saveQuestion(entryPoint, output, highlights) {
	if (!( entryPoint in questions)) {
		qi = new questionInfo(questions.length, $(elementQuestionText).val(), 0, 0);
		questions[entryPoint] = qi;
		$('#prevQuestions').append($('<option/>', {
			value : entryPoint,
			text : entryPoint
		}));
	}
	if (output != null)
		questions[entryPoint].output = output;
	if (highlights != null)
		questions[entryPoint].highlights = JSON.parse(JSON.stringify(highlights));
}

//this function populates the config text box with the chosen file
function loadConfigText(event) {
 	console.log(event.data);
	file = jQuery(event.data.testrigFile).get(0).files[0];
    if (file.type && (file.type == 'application/x-zip-compressed' || file.type == 'application/zip')) {
        var reader = new FileReader();
        reader.onload = function (e) {
            try {
                testrigZip = new JSZip(e.target.result);
                jQuery(event.data.configText).val("Packaged testrig contents:\n" + zipToOverviewText(testrigZip, "    "));
            }
            catch (e) {
                errorCheck(true, "Looks like a bad zip file: " + e.message, "loadconfigtext");

                //empty the textbox and the file chooser
                jQuery(event.data.configText).val("");
                jQuery(event.data.testrigFile).val('');
            }

        };
        reader.readAsArrayBuffer(file);
    }
    else {
        //empty testrigZip
        testrigZip = "";

        var r = new FileReader();
        r.onload = function (e) {
            var contents = e.target.result;
            jQuery(event.data.configText).val(contents);
        };
        r.readAsText(file);
    }

    //if a config had been selected from the drop down menu, remove it
    //so that only one type of input is active
    jQuery(event.data.configSelect).prop('selectedIndex', 0);
}

//this function populates the question text box with the chosen file
function loadQuestionText() {

    //sanity check HTML configuration elements
    if (errorCheck(typeof elementQuestionFile === 'undefined' || bfIsInvalidElement(elementQuestionFile),
           "Questoin file element (elementQuestionFile) is not configured in the HTML header",
            "loadquestiontext") ||
        errorCheck(typeof elementQuestionText === 'undefined' || bfIsInvalidElement(elementQuestionText),
           "Question text element (elementQuestionText) is not configured in the HTML header",
            "loadquestiontext") ||
        errorCheck(typeof elementQuestionSelect === 'undefined' || bfIsInvalidElement(elementQuestionSelect),
            "Question select element (elementQuestionSelect) is not configured in te HTML header",
            "loadquestiontext"))
        return;

    var questionFile = jQuery(elementQuestionFile).get(0).files[0];

    var r = new FileReader();
    r.onload = function (e) {
        var contents = e.target.result;
        jQuery(elementQuestionText).val(contents);
    };
    r.readAsText(questionFile);

    //if a question had been selected from the drop down menu, remove it
    //so that only one type of input is active
    jQuery(elementQuestionSelect).prop('selectedIndex', 0);
    
    // also reset prev question menu.
    jQuery(elementPrevQuestionSelect).prop('selectedIndex', 0);
}

//loads the content behind selected value of dropdownId into dstTextBox
//   ... jquery's load function didn't work properly for this use. 
//   ... it wouldn't load once we've loaded content through loadConfigText or loadQuestionText 
function loadText(dropDownId, dstTextBox, elementLocalFile) {

    if (errorCheck(typeof dropDownId === 'undefined' || bfIsInvalidElement(dropDownId),
           "Dropdown selector Id is incorrect", "loadtext") ||
        errorCheck(typeof dstTextBox === 'undefined' || bfIsInvalidElement(dstTextBox),
           "Dst text box id is incorrect", "loadtext") ||
        errorCheck(typeof elementLocalFile === 'undefined' || bfIsInvalidElement(elementLocalFile),
           "Local file element is incorrect", "loadtext") ||
        errorCheck(typeof elementConfigSelect === 'undefined' || bfIsInvalidElement(elementConfigSelect),
            "Config select element (elementConfigSelect) is not configured in te HTML header",
            "loadtext"))
        return;

    var srcUrl = jQuery(dropDownId).val();

    var match = srcUrl.match(/^.+(\.[^\.]+)$/);

    //check if the source URL corresponds to a zip file, which can only happen for configs (not questions)
    if (match && match[1] == ".zip") {

        // loading a zip file
        JSZipUtils.getBinaryContent(srcUrl, function (err, data) {
            if (err) {
                errorCheck(true, "Failed to fetch config/question " + srcUrl, "loadtext");
            }
            try {
                testrigZip = new JSZip(data);
                jQuery(dstTextBox).val("Packaged testrig contents:\n" + zipToOverviewText(testrigZip, "    "));
            }
            catch (e) {
                errorCheck(true, "Looks like a bad zip file: " + e.message, "loadconfigtext");

                //empty the textbox and the drop down menu
                jQuery(dstTextBox).val("");
                jQuery(dropDownId).prop('selectedIndex', 0);
            }
        });
    }
    // check if the source URL corresponds to a JSON file
    else {
        var isJSON = match && match[1] == ".json";
        jQuery.ajax({
            url: srcUrl,
            success: function (data) {
                if (isJSON)
                    data = JSON.stringify(data, null, 2);
                jQuery(dstTextBox).val(data);
            }
        }).fail(function () {
            errorCheck(true, "Failed to fetch config/question " + srcUrl, "loadtext");

            //empty the textbox and the drop down menu
            jQuery(dstTextBox).val("");
            jQuery(dropDownId).prop('selectedIndex', 0);
        });

        //if we were the testrig, unset the testrigzip
        if (dropDownId == elementConfigSelect) {
            testrigZip = "";
        }
    }

    //if a local file had been chosen, cancel that 
    //so that only one type of input is active
    jQuery(elementLocalFile).val('');
    
    // also reset prev question menu.
    jQuery(elementPrevQuestionSelect).prop('selectedIndex', 0);
}

// this is a test function whose contents change based on what we want to test
function testMe() {
    //containerName = "js_41aceec6-018e-4434-bd97-e1c9430333a6";

    //var blob = 
    //bfPutObject(containerName, testrigName, "layout", blob, testMeSuccess_cb, testMeFailure_cb, "testme", []);

    //startCalls("testme", "drawtopology::drawanswer");
    AddHighlightMenu();
}

function testMeFailure_cb(message) {
    console.log(message);
}

function testMeSuccess_cb(response, entryPoint, remainingCalls) {
    console.log("testme success: ");
    //bfGetObject(containerName, testrigName, "co/layout", testMeSuccess_cb, testMeFailure_cb, "testme", []);
}


function zipToOverviewText(zip, strPrefix) {
    var overviewText = "";
    // that, or a good ol' for(var entryName in zip.files)
    $.each(zip.files, function (index, zipEntry) {
        overviewText += strPrefix + zipEntry.name + "\n";
    });
    return overviewText;
}
