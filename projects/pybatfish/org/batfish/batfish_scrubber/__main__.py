#!/usr/local/bin/python2.7
# encoding: utf-8
'''
org.batfish.batfish_scrubber.main -- shortdesc

org.batfish.batfish_scrubber.main is a description

It defines classes_and_methods

@author:     Ari Fogel

@copyright:  2016 Ari Fogel. All rights reserved.

@license:    Apache 2.0

@contact:    arifogel@ucla.edu
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
from scrub import scrub

__all__ = []
__version__ = 0.1
__date__ = '2016-01-06'
__updated__ = '2016-01-06'

DEBUG = 1
TESTRUN = 0
PROFILE = 0

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

  Created by Ari Fogel on %s.
  Copyright 2016 Ari Fogel. All rights reserved.

  Licensed under the Apache License 2.0
  http://www.apache.org/licenses/LICENSE-2.0

  Distributed on an "AS IS" basis without warranties
  or conditions of any kind, either express or implied.

USAGE
''' % (program_shortdesc, str(__date__))

    try:
        # Setup argument parser
        parser = ArgumentParser(description=program_license, formatter_class=RawDescriptionHelpFormatter)
        parser.add_argument('-o', '--outputdir', dest='outputDir', default=None, help="path to output directory", metavar="path")
        #parser.add_argument("-r", "--recursive", dest="recurse", action="store_true", help="recurse into subfolders [default: %(default)s]")
        parser.add_argument("-v", "--verbose", dest="verbose", action="count", help="set verbosity level [default: %(default)s]")
        #parser.add_argument("-i", "--include", dest="include", help="only include paths matching this regex pattern. Note: exclude is given preference over include. [default: %(default)s]", metavar="RE" )
        #parser.add_argument("-e", "--exclude", dest="exclude", help="exclude paths matching this regex pattern. [default: %(default)s]", metavar="RE" )
        parser.add_argument('-V', '--version', action='version', version=program_version_message)
        parser.add_argument('-i', '--inputdir', dest="inputDir", help="path to directory with source configuration file(s)", default=None, metavar="path", required=True)

        # Process arguments
        args = parser.parse_args()

        inputDir = args.inputDir
        options = Options()
        options.inputDir = inputDir
        options.verbose = args.verbose
        options.outputDir = args.outputDir

        if inputDir == None:
            raise BatfishException("No input directory specified"), None, sys.exc_info()[2]
        inputFiles = [join(inputDir, f) for f in listdir(inputDir) if isfile(join(inputDir, f))]
        for inputFile in inputFiles:
            ### do something with inpath ###
            scrub(inputFile, options)
        return 0
    except KeyboardInterrupt:
        ### handle keyboard interrupt ###
        return 0
    except BatfishException as e:
        raise BatfishException("error running batfish_scrubber", e), None, sys.exc_info()[2]
    except Exception, e:
        if DEBUG or TESTRUN:
            raise(e)
        indent = len(program_name) * " "
        sys.stderr.write(program_name + ": " + repr(e) + "\n")
        sys.stderr.write(indent + "  for help use --help")
        return 2

if __name__ == "__main__":
    if DEBUG:
#        sys.argv.append("-h")
        sys.argv.append("-v")
#        sys.argv.append("-r")
    if TESTRUN:
        import doctest
        doctest.testmod()
    if PROFILE:
        import cProfile
        import pstats
        profile_filename = 'org.batfish.batfish_scrubber.main_profile.txt'
        cProfile.run('main()', profile_filename)
        statsfile = open("profile_stats.txt", "wb")
        p = pstats.Stats(profile_filename, stream=statsfile)
        stats = p.strip_dirs().sort_stats('cumulative')
        stats.print_stats()
        statsfile.close()
        sys.exit(0)
    sys.exit(main())