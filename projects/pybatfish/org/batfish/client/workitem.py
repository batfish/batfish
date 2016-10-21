'''
This class mirrors WorkItem.java in batfish-common
'''

import json

import org.batfish.util.util as batfishutils

class WorkItem:
    
    def __init__(self, session):
        self.id = batfishutils.get_uuid()
        self.container = session.container
        self.testrig = session.baseTestrig
        self.requestParams = {}
        self.responseParams = {}
        
        for key, value in session.additionalArgs.iteritems():
            self.requestParams[key] = value
   
    def to_json(self):
        requestJson = json.dumps(self.requestParams)
        responseJson = json.dumps(self.responseParams)
        workList = [self.id, self.container, self.testrig, requestJson, responseJson]
        return json.dumps(workList)

