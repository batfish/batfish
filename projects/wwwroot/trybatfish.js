
//this function populates the config text box with the chosen file
function loadConfigText() {
    var configFile = jQuery("#fileConfig").get(0).files[0];

    var r = new FileReader();
    r.onload = function (e) {
        var contents = e.target.result;
        jQuery('#txtConfig').val(contents);
    }
    r.readAsText(configFile);
}

//this function populates the question text box with the chosen file
function loadQuestionText() {
    var questionFile = jQuery("#fileQuestion").get(0).files[0];

    var r = new FileReader();
    r.onload = function (e) {
        var contents = e.target.result;
        jQuery('#txtQuestion').val(contents);
    }
    r.readAsText(questionFile);
}

function loadText(srcUrl, dstTextBox) {
    jQuery(dstTextBox).load(srcUrl);
}

// this is a test function whose contents change based on what we want to test
function testMe() {
    alert("Nothing to test");
}
