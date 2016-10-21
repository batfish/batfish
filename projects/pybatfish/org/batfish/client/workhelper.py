'''
Created on Oct 20, 2016

@author: ratul
'''

import time

from bfconsts import BfConsts
from coordconsts import CoordConsts
import resthelper
from workitem import WorkItem
from org.batfish.util.batfish_exception import BatfishException

def execute(wItem, session):
    
    jsonData = {}
    jsonData[CoordConsts.SVC_WORKITEM_KEY] =  wItem.to_json()
    jsonData[CoordConsts.SVC_API_KEY] = session.apiKey
      
    resthelper.post_data(session, CoordConsts.SVC_QUEUE_WORK_RSC, jsonData)
    
    status = get_work_status(wItem, session)

    while (status != CoordConsts.WorkStatusCode.TERMINATEDABNORMALLY and 
           status != CoordConsts.WorkStatusCode.TERMINATEDNORMALLY and 
           status != CoordConsts.WorkStatusCode.ASSIGNMENTERROR):
        time.sleep(1);
        status = get_work_status(wItem, session)

    if (status != CoordConsts.WorkStatusCode.TERMINATEDNORMALLY):
        raise BatfishException("Work finished with status " + status, wItem.to_json())

    # get the answer
    ansFileName = wItem.id + BfConsts.SUFFIX_ANSWER_JSON_FILE
    downloadedAnsFile = resthelper.get_object(session, ansFileName)

    answerString = downloadedAnsFile.read()

    return answerString

def get_workitem_answer_question(session, questionName, isDelta):
    wItem = WorkItem(session)
    wItem.requestParams[BfConsts.COMMAND_ANSWER] = ""
    wItem.requestParams[BfConsts.ARG_QUESTION_NAME] = questionName
    wItem.requestParams[BfConsts.ARG_ENVIRONMENT_NAME] = session.baseEnvironment
    if (session.deltaEnvironment is not None): 
        wItem.requestParams[BfConsts.ARG_DELTA_ENVIRONMENT_NAME] = session.deltaEnvironment
    if (session.deltaTestrig is not None):
        wItem.requestParams[BfConsts.ARG_DELTA_TESTRIG] = session.deltaTestrig;
    if (isDelta):
        wItem.requestParams[BfConsts.ARG_DIFF_ACTIVE] = ""
    return wItem

def get_workitem_parse(session):
    wItem = WorkItem(session)
    wItem.requestParams[BfConsts.COMMAND_PARSE_VENDOR_INDEPENDENT] = ""
    wItem.requestParams[BfConsts.COMMAND_PARSE_VENDOR_SPECIFIC] = ""
    wItem.requestParams[BfConsts.ARG_UNIMPLEMENTED_SUPPRESS] = ""
    return wItem;

def get_work_status(wItem, session):
    
    jsonData = {}
    jsonData[CoordConsts.SVC_API_KEY] = session.apiKey
    jsonData[CoordConsts.SVC_WORKID_KEY] = wItem.id

    answer = resthelper.post_data(session, CoordConsts.SVC_GET_WORKSTATUS_RSC, jsonData)
    
    if CoordConsts.SVC_WORKSTATUS_KEY in answer:
        return answer[CoordConsts.SVC_WORKSTATUS_KEY]
    else:
        raise BatfishException("Expected key not found in status check response: " + CoordConsts.SVC_WORKSTATUS_KEY), answer
