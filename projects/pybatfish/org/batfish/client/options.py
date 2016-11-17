'''
Created on Jan 7, 2016

@author: arifogel
'''

class Options(object):
    '''
    classdocs
    '''

    coordinator_host = "localhost"
    coordinator_work_port = "9997"    
    use_ssl = True    
    # This should be true when coordinator is a public-facing service
    verify_ssl_certs = False

    default_container_prefix = "pcp"
    default_delta_env_prefix = "env_"
    default_question_prefix = "q"
    default_testrig_prefix = "tr_"

    max_tries_to_coonnect_to_coordinator = 10
    num_tries_warn_threshold = 5
    seconds_to_sleep_between_tries_to_coordinator = 3

    def __init__(self):
        '''
        Constructor
        '''
        