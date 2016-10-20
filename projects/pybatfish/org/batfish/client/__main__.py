#!/usr/local/bin/python2.7
# encoding: utf-8
'''
org.batfish.client.main -- shortdesc

org.batfish.client.main is a description

It defines classes_and_methods

@author:     Ratul Mahajan

@copyright:  2016 Intentionet. All rights reserved.

@license:    Apache 2.0

@contact:    info@intentionet.com
@deffield    updated: Updated
'''

import sys
import os

from argparse import ArgumentParser
from argparse import RawDescriptionHelpFormatter

from os import listdir
from os.path import isfile, join

from org.batfish.util.batfish_exception import BatfishException
from options import Options

__all__ = []
__version__ = 0.1
__date__ = '2016-10-18'
__updated__ = '2016-10-18'

class CLIError(Exception):
    '''Generic exception to raise and log different fatal errors.'''
    def __init__(self, msg):
        super(CLIError).__init__(type(self))
        self.msg = "E: %s" % msg
    def __str__(self):
        return self.msg
    def __unicode__(self):
        return self.msg

def main(argv=None): # IGNORE:C0111
    '''Command line options.'''

    if argv is None:
        argv = sys.argv
    else:
        sys.argv.extend(argv)

    program_name = os.path.basename(sys.argv[0])
    program_version = "v%s" % __version__
    program_build_date = str(__updated__)
    program_version_message = '%%(prog)s %s (%s)' % (program_version, program_build_date)
    if __name__ == '__main__':
        program_shortdesc = __import__('__main__').__doc__.split("\n")[1]
    else:
        program_shortdesc = __doc__.split("\n")[1]
    program_license = '''%s

  Created by Ratul Mahajan on %s.
  Copyright 2016 Intentionet. All rights reserved.

  Licensed under the Apache License 2.0
  http://www.apache.org/licenses/LICENSE-2.0

  Distributed on an "AS IS" basis without warranties
  or conditions of any kind, either express or implied.

USAGE
''' % (program_shortdesc, str(__date__))

    try:
        # Setup argument parser
        parser = ArgumentParser(description=program_license, formatter_class=RawDescriptionHelpFormatter)
        parser.add_argument('-c', '--cmdfile', dest="cmdFile", help="path to command file", default=None, type=file, metavar="CommandFile", required=True)
        parser.add_argument("-v", "--verbose", dest="verbose", action="count", help="set verbosity level [default: %(default)s]")
        parser.add_argument('-V', '--version', action='version', version=program_version_message)

        # Process arguments
        args = parser.parse_args()

        options = Options()
        options.cmdFile = args.cmdFile
        options.verbose = args.verbose


    except BatfishException as e:
        raise BatfishException("error running", program_name, e), None, sys.exc_info()[2]

if __name__ == "__main__":
    sys.exit(main())