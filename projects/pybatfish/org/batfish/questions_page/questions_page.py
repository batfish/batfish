# Author: Todd Millstein
# Copyright 2016

# This script parses Javadoc comments in Batfish question files and produces a documentation page batfish-questions.html.

# If a command-line argument is given, it is used as the directory name in which to find Batfish questions;
# otherwise the default directory is the current directory.

# The script assumes that each question file has a javadoc comment of the following form:

# // <question_page_comment>
# /**
#  * @type QuestionType
#  * The description is here.
#  * <p>
#  * More of the description is 
#  * here.
#  * <p>
#  * And even more explanations to follow in consecutive
#  * paragraphs separated by HTML paragraph breaks.
#  *
#  * @param var1 description
#  * @param var1 text 
#  *             text text
#  *
#  * @example bf_answer("QuestionType", aclNameRegex="OUTSIDE_TO_INSIDE.*")
#  *          Analyzes only ACLs whose names start with 'OUTSIDE_TO_INSIDE'.
#  */

# So, the comment has to start with '// <question_page_comment>' , then 
# /** by itself on the line
# @typename oneword: optional; if not specified, filename will be used
# Arbitrary HTML description; the first sentence is treated as summary
# Zero or more @param attributes, each with a name and description (which can take multiple lines)
# Zero of more example attributes, the code snippet should follow the attribute and the description (possibly multi-line)  on following lines
# ends with */

# The leading * on each line is optional.

import sys
import os
import string
from argparse import ArgumentParser
from argparse import RawDescriptionHelpFormatter

nl = "\n"

class Options(object):
    '''
    classdocs
    '''

    def __init__(self):
        '''
        Constructor
        '''

#returns all files in the list of directories
def allFilesWithComment(dirlist):
    retFiles = []
    
    for dir in dirlist:
        for root, subdirs, files in os.walk(dir):
            retFiles += [os.path.join(root, file) 
                         for file in files 
                         if "<question_page_comment>" in open(os.path.join(root, file)).read()]
    
    return retFiles

# check if this line starts with @example
def isExampleAttr(s):
    return s.find("@example") == 0 and (len(s) == 8 or s[8] in string.whitespace)

# check if this line starts with @param
def isParamAttr(s):
    return s.find("@param") == 0 and (len(s) == 6 or s[6] in string.whitespace)

# check if this line starts with @type
def isTypeAttr(s):    
    return s.find("@type") == 0 and s[5] in string.whitespace

def removeWhitespaceAndOptionalStar(s):
    s = s.lstrip(string.whitespace)
    sp = s.split()
    if (len(sp) > 0 and sp[0] == '*'):
        s = s[1:]
    return s.lstrip(string.whitespace)
    
# parse an example attribute
# we assume isExampleAttr(s) is True    
def parseExample(s):    
    s = s[9:].lstrip(string.whitespace).rstrip(string.whitespace)
    example = {"name":s, "desc": ""}
    return example
            
# parse a param attribute
# we assume isParamAttr(s) is True    
def parseParam(s):    
    s = s[7:].lstrip(string.whitespace)
    pname = s.split()[0]
    param = {"name":pname, "desc":s[len(pname):]}
    return param
            
# parse the given file to grab the comment describing this property
# we assume the comment is in the syntax above
def parseComment(fullfname, options):
    f = open(fullfname)
    res = {"file":fullfname, "desc":"", "params":[], "examples":[], "commentFound": False}
    param = None
    example = None

    # states are 0:uninitialized, 1:foundstart, 2:foundparam, 3:foundexample
    state = "0:uninitialized"

    # the name of the file is used as the default name of the property
    res["name"] = os.path.basename(fullfname)
    
    for line in f:
        if (options.debug):
            sys.stderr.write(line + nl)
            sys.stderr.write(state + nl)
            
        line = line.lstrip(string.whitespace)
        
        #ignore this line which will come right after the opening
        if (line.rstrip(string.whitespace) == "/**"):
            continue
                
        if (state == "0:uninitialized"):
            # waiting for the '// <question_page_comment>' that begins the comment
            # we expect it to be on its own line, and compare after remove all white space
            if ("".join(line.split()) == "//<question_page_comment>"):
                state = "1:foundstart"
                res["commentFound"] = True
        elif (line.rstrip(string.whitespace) == "*/"):
            # we reached the end
            if (state == "2:foundparam"):
                res["params"].append(param)
            if (state == "3:foundexample"):
                res["examples"].append(example)
            return res
        elif (state == "1:foundstart"): 
            # now we are parsing the description
            # remove leading * characters, which are optional
            line = removeWhitespaceAndOptionalStar(line)
                        
            if (isTypeAttr(line)):
                words = line.split()
                res["name"] = words[1]  
            elif (isParamAttr(line)):
                param = parseParam(line)
                state = "2:foundparam"
            elif (isExampleAttr(line)):
                example = parseExample(line)
                state = "3:foundexample"
            else:
                res["desc"] += line
        elif (state == "2:foundparam"):
            # now we are parsing a parameter
            line = removeWhitespaceAndOptionalStar(line)            

            if (isParamAttr(line)):
                res["params"].append(param)
                param = parseParam(line)
            elif (isExampleAttr(line)):
                res["params"].append(param)
                example = parseExample(line)
                state = "3:foundexample"                
            else:
                param["desc"] += line
        elif (state == "3:foundexample"):
            # now we are parsing an example
            line = removeWhitespaceAndOptionalStar(line)            

            if (isExampleAttr(line)):
                res["examples"].append(example)
                example = parseExample(line)
            else:
                example["desc"] += line            
        if (options.debug):
            sys.stderr.write(state + nl)
    if (state == "0:uninitialized"):
        if (options.debug):
            sys.stderr.write("missing javadoc comment for question: " + fullfname + nl)
        return res
    else:
        raise Exception("javadoc comment was never closed in question: " + fullfname), None, sys.exc_info()[2]


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
#    res += "<table><tr class=\"d" + str(style) + "\">" + nl
    res += "<tr class=\"d" + str(style) + "\">" + nl
    res += "<td>" + nl
    commentParams = comment["params"]
    pnames = map(lambda p: p["name"], commentParams)
    res += "<a href=\"#" + comment["name"] + "\">" + comment["name"] + "</a>" + "(" + string.join(pnames, ", ") + ")" + nl
    res += "</td>" + nl
    res += "<td> <div>" + firstSentence(comment["desc"]) + "</div>" + nl
    res += "</td>" + nl
#    res += "</tr></table><br/>" + nl + nl
    res += "</tr>" + nl + nl

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
        
#     res += "<p>"
#     divid = "def_" + comment["name"]
#     res += "<input type=\"button\" value=\"Toggle Definition\""
#     res += "onclick=\"jQuery('#" + divid + "').toggle()\" />" + nl
#     res += "<div id=\"" + divid + "\" " + "style=\"display:none\">"
#     f = open(comment["file"])
#     defn = f.read()
#     res += "<p><pre>" + defn + "</pre></p>"
#     res += "</div>" + nl + nl
    return res


def questionsToHtml(comments, options):
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


    html += "<H4>Summary</H4>" + nl
    html += "<table border=1 cellpadding=10>" + nl
    style = 0
    for c in comments:
        html += commentToSummaryHTML(c, style)
        style = abs(style - 1)
    html += "</table>" + nl
        
    html += "<H4>Detailed Descriptions</H4>" + nl
    for c in comments:
        html += commentToHTML(c)
        
    html += "</HTML>" + nl

    if (options.outputFile != None):
        f = open(options.outputFile, "w")
        f.write(html)
        f.close()
    else:
        print html


def questionsToMarkdown(comments, options):
    markdown = ""
    
    markdown += "## Batfish Questions" + nl + nl
    markdown += "### Summary" + nl

    for comment in comments:        
        commentParams = comment["params"]
        pnames = map(lambda p: p["name"], commentParams)
        markdown += "[" + comment["name"] + "(" + string.join(pnames, ", ") + ")] (#" + comment["name"].lower() + ")" +  nl
        markdown += " * " + firstSentence(comment["desc"]) + nl + nl

    markdown += nl + nl + "### Detailed Descriptions" + nl
    
    for comment in comments:
        pnames = map(lambda p: p["name"], comment["params"])
        markdown += "#### " + comment["name"] + nl
    
        markdown += "\n\n" + comment["desc"] + nl
    
        if (len(comment["params"]) > 0):
            markdown += nl + "*Parameter(s):*" + nl
            
            for p in comment["params"]:
                markdown += "* *" + p["name"] + "*: " + p["desc"]
            
        if (len(comment["examples"]) > 0):
            markdown += nl + "*Example(s):*" + nl
            
            for p in comment["examples"]:
                markdown += "* " + p["name"] + nl + nl + "  " + p["desc"]
            
        markdown += nl + nl + "***" + nl + nl
        
    if (options.outputFile != None):
        f = open(options.outputFile, "w")
        f.write(markdown)
        f.close()
    else:
        print markdown


def main(argv=None): # IGNORE:C0111
    '''Command line options.'''

    if argv is None:
        argv = sys.argv
    else:
        sys.argv.extend(argv)

    # Setup argument parser
    parser = ArgumentParser(description="questions_page", formatter_class=RawDescriptionHelpFormatter)
    parser.add_argument('-o', '--outfile', dest='outFile', default=None, help="output file", metavar="<out file>")
    parser.add_argument("-d", "--debug", dest="debug", help="print debug messages", action='store_true')
    #parser.add_argument('-i', '--inputdir', dest="inputDir", help="path to directory with source question file(s)", default=None, metavar="path", required=True)
    parser.add_argument('-f', '--outputformat', dest='outputFormat', default="markdown", help="format to print the output in: {html, markdown}", metavar="<output format>")

    parser.add_argument(dest='inputDirs', metavar='inputdir', nargs='+',
                    help='input directory for question files')
    # Process arguments
    args = parser.parse_args()

    options = Options()
    options.debug = args.debug
    options.inputDirs = args.inputDirs
    options.outputFile = args.outFile
    options.outputFormat = args.outputFormat

    files = allFilesWithComment(options.inputDirs)
    comments = [parseComment(file, options) for file in files]
    comments = sorted( (comment for comment in comments), key=lambda x: x["name"])
    # ignore inputFile names that don't end in .q
    # comments = [parseComment(inputFile, inputDir, options) for inputFile in files if (len(inputFile) > 1 and len(inputFile) == inputFile.rfind(".q") + 2)]

    if (options.outputFormat == "html"):
        questionsToHtml(comments, options)
    elif (options.outputFormat == "markdown"):
        questionsToMarkdown(comments, options)
    else:
        raise "Unknown output format: " + options.outputFormat

    return 0

sys.exit(main())
