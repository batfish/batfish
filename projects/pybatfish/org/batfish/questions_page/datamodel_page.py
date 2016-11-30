import os
import sys
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

def toMarkdown(options):   
    markdown = ""
    
    markdown += "Batfish converts vendors configuration files into a vendor-independent datamodel. The complete datamodel in [JSON Schema] (http://json-schema.org/) Draft v4 is below."
    markdown += "**An easier-to-browse version is [here] (http://intentionet.github.io/batfish/docson/#../datamodel.json).**" + nl + nl
        
    for line in open(options.inFile, "r"):
        markdown += "    " + line
    
    return markdown

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

    parser.add_argument(dest='inFile', help="input file with schema", metavar="<in file>")

    # Process arguments
    args = parser.parse_args()

    options = Options()
    options.debug = args.debug
    options.inFile = args.inFile
    options.outFile = args.outFile
    options.outputFormat = args.outputFormat

    outStr = ""
    
    if (options.outputFormat == "markdown"):
        outStr += toMarkdown(options)
    else:
        raise "Unknown output format: " + options.outputFormat

    if (options.outFile != None):
        f = open(options.outFile, "w")
        f.write(outStr)
        f.close()
    else:
        print outStr

    return 0

sys.exit(main())
