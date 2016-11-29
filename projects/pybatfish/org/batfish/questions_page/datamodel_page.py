import os
import sys
import string
from argparse import ArgumentParser
from argparse import RawDescriptionHelpFormatter

class Options(object):
    '''
    classdocs
    '''

    def __init__(self):
        '''
        Constructor
        '''

def main(argv=None): # IGNORE:C0111
    '''Command line options.'''

    if argv is None:
        argv = sys.argv
    else:
        sys.argv.extend(argv)

    # Setup argument parser
    parser = ArgumentParser(description="datamodel_page", formatter_class=RawDescriptionHelpFormatter)
    parser.add_argument('-o', '--outfile', dest='outFile', default=None, help="output file", metavar="<out file>")
    parser.add_argument("-d", "--debug", dest="debug", help="print debug messages", action='store_true')
    parser.add_argument('-f', '--outputformat', dest='outputFormat', default="markdown", help="format to print the output in: {html, markdown}", metavar="<output format>")
    parser.add_argument('-i', '--infile', dest='inFile', default=None, help="input file with schema", metavar="<in file>")

    # Process arguments
    args = parser.parse_args()

    options = Options()
    options.debug = args.debug
    options.inputDirs = args.inputDirs
    options.outputFile = args.outFile
    options.outputFormat = args.outputFormat

    if (options.outputFormat == "markdown"):
        toMarkdown(options)
    else:
        raise "Unknown output format: " + options.outputFormat

    return 0

sys.exit(main())
