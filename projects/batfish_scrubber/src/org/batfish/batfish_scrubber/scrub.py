'''
Created on Jan 7, 2016

@author: Ari Fogel
'''

import re
import sys
import os
import errno
from os.path import basename, join

from batfish_exception import BatfishException

def make_sure_path_exists(path):
    try:
        os.makedirs(path)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise

sensitive_line_regex =  (
                         r'(!?\s*)('
                         'enable secret|'
                         'aaa|'
                         'ip ospf message-digest-key [0-9]* md5|'
                         'neighbor\s*[^\s]*\s*password|'
                         'password|'
                         'server-private|'
                         'snmp-server|'
                         'tacacs-server'
                         ')(\s*)(.*)'
                        ) 

def scrub(inputPath, options):
    if (options.outputDir != None):
        make_sure_path_exists(options.outputDir)
        outputPath = join(options.outputDir, basename(inputPath))
    else:
        outputPath = inputPath + ".scrubbed"

    if (options.verbose):
        print("Scrubbing: \"" + inputPath +"\" => \"" + outputPath + "\"")
    try:
        with open(inputPath) as inputFile:
            try:
                inputLines = inputFile.read().splitlines()
            except IOError as e:
                raise BatfishException("cannot read line from input file", e), None, sys.exc_info()[2]
    except IOError as e:
        raise BatfishException("cannot open input file: \"" + inputPath + "\"", e), None, sys.exc_info()[2]
    
    scrubbedLines = []
    for inputLine in inputLines:
        scrubbedLine = re.sub(sensitive_line_regex, r'\1\2\3<BATFISH_SCRUBBED_SECRET>', inputLine)
        scrubbedLines.append(scrubbedLine)
    output = "\n".join(scrubbedLines)
    try:
        with open(outputPath, "w") as outputFile:
            outputFile.write(output)
    except IOError as e:
        raise BatfishException("cannot open output file", e), None, sys.exc_info()[2] 