# Author: Todd Millstein
# Copyright 2016

# This script parses Javadoc comments in Batfish question files and produces a documentation page batfish-questions.html.

# If a command-line argument is given, it is used as the directory name in which to find Batfish questions;
# otherwise the default directory is the current directory.

# The script assumes that each question file has a javadoc comment of the following form:

# /**
#  * The description is here.
#  * <p>
#  * More of the description is 
#  * here.
#  * <p>
#  * And even more explanations to follow in consecutive
#  * paragraphs separated by HTML paragraph breaks.
#  *
#  * @param  var1 description
#  * @param name text text text
#  */

# So the comment has to start with /** on one line, then can have an arbitrary HTML description, then can have zero or more @param attributes,
# each with a name and description (which can take multiple lines), and then ends with */

# The first sentence of the description is treated as a summary.

# The leading * on each line is optional.


import string
import os
import sys
from os.path import join

from org.batfish.util.util import make_sure_path_exists
from org.batfish.util.batfish_exception import BatfishException

# check if this line starts with @param
def isParamAttr(s):
    return s.find("@param") == 0 and (len(s) == 6 or s[6] in string.whitespace)

def removeWhitespaceAndOptionalStar(s):
    s = s.lstrip(string.whitespace)
    sp = s.split()
    if (len(sp) > 0 and sp[0] == '*'):
        s = s[1:]
    return s.lstrip(string.whitespace)
    
# parse a param attribute
# we assume isParamAttr(s) is True    
def parseParam(s):    
    s = s[7:].lstrip(string.whitespace)
    pname = s.split()[0]
    param = {"name":pname, "desc":s[len(pname):]}
    return param
            
# parse the given file to grab the comment describing this property
# we assume the comment is in Javadoc syntax
def parseComment(fname, inputDir, options):
    fullfname = inputDir + "/" + fname
    f = open(fullfname)
    res = {"file":fullfname, "desc":"", "params":[]}
    state = 0
    param = None

    # the name of the file is used as the name of the property
    dot = fname.rindex('.')
    if (dot != -1):
        fname = fname[:dot]
    res["name"] = fname
    
    for line in f:
        if (options.debug):
            print line
            print state
            
        line = line.lstrip(string.whitespace)
        if (state == 0):
            # waiting for the /** that begins a javadoc comment
            # we expect it to be on its own line
            if (line.rstrip(string.whitespace) == "/**"):
                state = 1
        elif (line.rstrip(string.whitespace) == "*/"):
            # we reached the end
            if (state == 2):
                res["params"].append(param)
            return res
        elif (state == 1): 
            # now we are parsing the description
            # remove leading * characters, which are optional
            line = removeWhitespaceAndOptionalStar(line)
            if (isParamAttr(line)):
                # we've detected an attribute
                param = parseParam(line)
                state = 2
            else:
                res["desc"] += line
        elif (state == 2):
            # now we are parsing a parameter
            line = removeWhitespaceAndOptionalStar(line)
            if (isParamAttr(line)):
                # we've detected an attribute
                res["params"].append(param)
                param = parseParam(line)
            else:
                param["desc"] += line
        if (options.debug):
            print state
    if (state == 0):
        if (options.verbose):
            print("missing javadoc comment for question: " + fname)
        return res
    else:
        raise BatfishException("javadoc comment was never closed in question: " + fname), None, sys.exc_info()[2]

nl = "\n"


# Return the first sentence of the given string.  It is the portion of the string up to a period followed by some whitespace or the end of the string.
# If no such period exists then return the whole string.
def firstSentence(s):
    i = 0
    while True:
        dot = s.find(".", i)
        if (dot == -1):
            return s
        elif len(s) == (dot+1) or (s[dot+1] in string.whitespace):
            return s[:dot+1]
        else:
            i = dot + 1

def commentToSummaryHTML(comment, style):
    res = ""
    res += "<table><tr class=\"d" + str(style) + "\">" + nl
    res += "<td>" + nl
    commentParams = comment["params"]
    pnames = map(lambda p: p["name"], commentParams)
    res += "<a href=\"#" + comment["name"] + "\">" + comment["name"] + "</a>" + "(" + string.join(pnames, ", ") + ")" + nl
    res += "<div>" + firstSentence(comment["desc"]) + "<div>" + nl
    res += "</td>" + nl
    res += "</tr></table><br/>" + nl + nl

    return res

def commentToHTML(comment):
    res = ""
    res += "<hr />" + nl
    
    pnames = map(lambda p: p["name"], comment["params"])
    res += "<a name=\"" + comment["name"] + "\">" + nl + "<!--   -->" + nl + "</a>" + nl
    res += "<h4>" + comment["name"] + "(" + string.join(pnames, ", ") + ")" + "</h4>" + nl

    res += "<p>" + comment["desc"] + "</p>" + nl

    if (len(comment["params"]) > 0):
        res += "<dl>" + nl
        res += "<dt>Parameters:</dt>" + nl
        for p in comment["params"]:
            res += "<dd><code>" + p["name"] + "</code>" + " - " + p["desc"] + "</dd>" + nl
        res += "</dl>" + nl
        
    res += "<p>"
    divid = "def_" + comment["name"]
    res += "<input type=\"button\" value=\"Toggle Definition\""
    res += "onclick=\"jQuery('#" + divid + "').toggle()\" />" + nl
    res += "<div id=\"" + divid + "\" " + "style=\"display:none\">"
    f = open(comment["file"])
    defn = f.read()
    res += "<p><pre>" + defn + "</pre></p>"
    res += "</div>" + nl + nl
    return res


# parse a javadoc comment from each .q file in the given directory
# create questions.html as a result
def questionsToHTML(inputDir, options):
    try:
        html = "<HTML>" + nl
        html += "<HEAD>"
        html += "<TITLE>Batfish Questions</TITLE>" + nl
        html += "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js\"></script>" + nl
        html += "</HEAD>" + nl
        
        html += "<style type=\"text/css\">" + nl
        html += "tr.d0 td { background-color: #FFFFFF; color: black; }" + nl
        html += "tr.d1 td { background-color: #EEEEEF; color: black; }" + nl
        html += "</style>" + nl
        
        html += "<H3>Batfish Questions</H3>" + nl
    
        files = sorted(os.listdir(inputDir))
        # ignore inputFile names that don't end in .q
        comments = [parseComment(inputFile, inputDir, options) for inputFile in files if (len(inputFile) > 1 and len(inputFile) == inputFile.rfind(".q") + 2)]
    
        html += "<H4>Summary</H4>" + nl
        style = 0
        for c in comments:
            html += commentToSummaryHTML(c, style)
            style = abs(style - 1)
            
        html += "<H4>Detailed Descriptions</H4>" + nl
        for c in comments:
            html += commentToHTML(c)
            
        html += "</HTML>" + nl
        outputBasename = "batfish-questions.html"
        if (options.outputDir != None):
            make_sure_path_exists(options.outputDir)
            outputPath = join(options.outputDir, outputBasename)
        else:
            outputPath = outputBasename

        f = open(outputPath, "w")
        f.write(html)
        f.close()
    except Exception as e:
        raise BatfishException("questionsToHTML", e), None, sys.exc_info()[2]
