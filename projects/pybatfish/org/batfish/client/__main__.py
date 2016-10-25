#!/usr/bin/env python
'''
org.batfish.client.main -- shortdesc

org.batfish.client.main is a description

It defines classes_and_methods

@author:     Ratul Mahajan

@copyright:  2016 Intentionet. All rights reserved.

@license:    Apache 2.0

@contact:    info@intentionet.com
'''

import sys
import logging
import os

from argparse import ArgumentParser
from argparse import RawDescriptionHelpFormatter

from org.batfish.util.batfish_exception import BatfishException
from options import Options
from commands import *

__version__=1.0

SERVER="localhost"

def main(argv=None): # IGNORE:C0111
    '''Command line options.'''

    if argv is None:
        argv = sys.argv
    else:
        sys.argv.extend(argv)

    try:
        # Setup argument parser
        parser = ArgumentParser(description="Batfish client", formatter_class=RawDescriptionHelpFormatter)
        parser.add_argument('-c', '--cmdfile', dest="cmdFile", help="path to command file", default=None, type=file, metavar="CommandFile", required=True)
        parser.add_argument('-s', '--server', dest="server", help="location of the service", default=SERVER, metavar="HostName")
        parser.add_argument("-v", "--verbose", dest="verbose", action="count", help="set verbosity level [default: %(default)s]")
        parser.add_argument('-V', '--version', action='version', version="version " + str(__version__))

        # Process arguments
        args = parser.parse_args()

        options = Options()
        options.cmdFile = args.cmdFile
        options.server = args.server
        options.verbose = args.verbose

        print "verbose = " + str(options.verbose) + " server = " + options.server
        exit
        
        bf_session.coordinatorHost = options.server

        if (options.verbose is not None):
            bf_session.logger.addHandler(logging.StreamHandler())
            if (options.verbose == 1):
                bf_session.logger.setLevel(logging.INFO)
            else:
                bf_session.logger.setLevel(logging.DEBUG)
    
    except BatfishException as e:
        raise BatfishException("error running batfish client", e), None, sys.exc_info()[2]

    work(options)

if __name__ == "__main__":
    sys.exit(main())
    
def work(options):
    
    for line in options.cmdFile:
        if (line.startswith("#") or line.isspace()):
            continue
        
        words = line.split()    
        
        if (len(words) <= 1):
            raise "Illegal command line: " + line

        command = words[0]
        restOfLine = " ".join(words[1:])            
        
        if (command == "echo"):
            print restOfLine
        elif (command == "command"):
            print eval(restOfLine)
        else:
            raise "Unknown command " + command + " in line " + line    
            