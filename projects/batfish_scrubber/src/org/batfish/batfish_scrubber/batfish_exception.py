'''
Created on Jan 7, 2016

@author: arifogel
'''

class BatfishException(Exception):
    '''
    classdocs
    '''


    def __init__(self, message, cause = None):
        super(BatfishException, self).__init__(message + ",\n\tcaused by " + repr(cause).decode('string_escape'))
        self.cause = cause
        '''
        Constructor
        '''
        self.message = message
