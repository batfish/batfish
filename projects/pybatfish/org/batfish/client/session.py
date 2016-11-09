# Author: Ratul Mahajan
# Copyright 2016 Intentionet

from options import Options
from coordconsts import CoordConsts

class Session:

    def __init__(self, logger):
        self.coordinatorHost = Options.coordinator_host
        self.coordinatorPort = Options.coordinator_work_port
        self.coordinatorBase = CoordConsts.SVC_BASE_WORK_MGR
        self.useSsl = Options.use_ssl
        self.verifySslCerts = Options.verify_ssl_certs

        self.apiKey = CoordConsts.DEFAULT_API_KEY
        self.container = None
        self.baseTestrig = None
        self.baseEnvironment = None
        self.deltaTestrig = None
        self.deltaEnvironment = None

        self.additionalArgs = {}
        
        self.logger = logger
        
    def get_base_url(self):
        if (self.useSsl): 
            protocol = "https"
        else:
            protocol = "http"
        
        return '{0}://{1}:{2}{3}'.format(protocol, self.coordinatorHost, self.coordinatorPort, self.coordinatorBase) 

    def get_url(self, resource):
        return '{0}/{1}'.format(self.get_base_url(), resource) 