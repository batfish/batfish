'''
Created on Jan 7, 2016

@author: arifogel
'''

class Options(object):
    '''
    classdocs
    '''

    use_ssl = True
    coordinator_host = "localhost"
    coordinator_work_port = "9997"    

    default_container_prefix = "pcp"
    default_delta_env_prefix = "env_";
    default_question_prefix = "q";
    default_testrig_prefix = "tr_";

    def __init__(self):
        '''
        Constructor
        '''
        