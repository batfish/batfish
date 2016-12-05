'''
Created on Oct 20, 2016

@author: ratul
'''

import time

from bfconsts import BfConsts
from coordconsts import CoordConsts
import resthelper
from workitem import WorkItem
from pybatfish.util.batfish_exception import BatfishException

def execute(wItem, session):
    
    jsonData = {}
    jsonData[CoordConsts.SVC_WORKITEM_KEY] =  wItem.to_json()
    jsonData[CoordConsts.SVC_API_KEY] = session.apiKey
      
    resthelper.get_json_response(session, CoordConsts.SVC_QUEUE_WORK_RSC, jsonData)
    
    status = get_work_status(wItem, session)

    while (status != CoordConsts.WorkStatusCode.TERMINATEDABNORMALLY and 
           status != CoordConsts.WorkStatusCode.TERMINATEDNORMALLY and 
           status != CoordConsts.WorkStatusCode.ASSIGNMENTERROR):
        time.sleep(1);
        status = get_work_status(wItem, session)

    if (status == CoordConsts.WorkStatusCode.ASSIGNMENTERROR):
        raise BatfishException("Work finished with status " + status, wItem.to_json())

    # get the answer
    ansFileName = wItem.id + BfConsts.SUFFIX_ANSWER_JSON_FILE
    downloadedAnsFile = resthelper.get_object(session, ansFileName)

    answerString = downloadedAnsFile.read()

    return answerString

def get_data_answer(session, questionName, questionJson, parametersJson):
    jsonData = {}
    jsonData[CoordConsts.SVC_API_KEY] = session.apiKey
    jsonData[CoordConsts.SVC_CONTAINER_NAME_KEY] = session.container
    jsonData[CoordConsts.SVC_TESTRIG_NAME_KEY] = session.baseTestrig
    jsonData[CoordConsts.SVC_QUESTION_NAME_KEY] = questionName
    jsonData[CoordConsts.SVC_FILE_KEY] =  ('question', questionJson)
    jsonData[CoordConsts.SVC_FILE2_KEY] = ('parameters', parametersJson)
    return jsonData

def get_data_init_container(session, containerPrefix):
    jsonData = {}
    jsonData[CoordConsts.SVC_API_KEY] = session.apiKey
    jsonData[CoordConsts.SVC_CONTAINER_PREFIX_KEY] = containerPrefix
    return jsonData

def get_data_list_testrigs(session, containerName):
    jsonData = {}
    jsonData[CoordConsts.SVC_API_KEY] = session.apiKey
    if (containerName is not None):
        jsonData[CoordConsts.SVC_CONTAINER_NAME_KEY] = containerName
    return jsonData

def get_data_upload_testrig(session, testrigName, fileToSend):
    jsonData = {}
    jsonData[CoordConsts.SVC_API_KEY] = session.apiKey
    jsonData[CoordConsts.SVC_CONTAINER_NAME_KEY] = session.container
    jsonData[CoordConsts.SVC_TESTRIG_NAME_KEY] = testrigName
    jsonData[CoordConsts.SVC_ZIPFILE_KEY] = ('filename', open(fileToSend, 'rb'), 'application/octet-stream')
    return jsonData

def get_workitem_answer(session, questionName, doDelta):
    wItem = WorkItem(session)
    wItem.requestParams[BfConsts.COMMAND_ANSWER] = ""
    wItem.requestParams[BfConsts.ARG_QUESTION_NAME] = questionName
    wItem.requestParams[BfConsts.ARG_ENVIRONMENT_NAME] = session.baseEnvironment
    if (session.deltaEnvironment is not None): 
        wItem.requestParams[BfConsts.ARG_DELTA_ENVIRONMENT_NAME] = session.deltaEnvironment
    if (session.deltaTestrig is not None):
        wItem.requestParams[BfConsts.ARG_DELTA_TESTRIG] = session.deltaTestrig;
    if (doDelta):
        wItem.requestParams[BfConsts.ARG_DIFF_ACTIVE] = ""
    return wItem

def get_workitem_generate_dataplane(session, doDelta):
    wItem = WorkItem(session)
    wItem.requestParams[BfConsts.COMMAND_DUMP_DP] = ""
    wItem.requestParams[BfConsts.ARG_ENVIRONMENT_NAME] = session.baseEnvironment
    if (doDelta):
        wItem.requestParams[BfConsts.ARG_DELTA_TESTRIG] = session.deltaTestrig
        wItem.requestParams[BfConsts.ARG_DELTA_ENVIRONMENT_NAME] = session.deltaEnvironment
        wItem.requestParams[BfConsts.ARG_DIFF_ACTIVE] = ""
    return wItem;

def get_workitem_parse(session, doDelta):
    wItem = WorkItem(session)
    if (doDelta):
        wItem.requestParams[BfConsts.ARG_DELTA_TESTRIG] = session.deltaTestrig
        wItem.requestParams[BfConsts.ARG_DELTA_ENVIRONMENT_NAME] = session.deltaEnvironment
        wItem.requestParams[BfConsts.ARG_DIFF_ACTIVE] = ""
    wItem.requestParams[BfConsts.COMMAND_PARSE_VENDOR_INDEPENDENT] = ""
    wItem.requestParams[BfConsts.COMMAND_PARSE_VENDOR_SPECIFIC] = ""
    wItem.requestParams[BfConsts.ARG_UNIMPLEMENTED_SUPPRESS] = ""
    return wItem;

def get_work_status(wItem, session):
    
    jsonData = {}
    jsonData[CoordConsts.SVC_API_KEY] = session.apiKey
    jsonData[CoordConsts.SVC_WORKID_KEY] = wItem.id

    answer = resthelper.get_json_response(session, CoordConsts.SVC_GET_WORKSTATUS_RSC, jsonData)
    
    if CoordConsts.SVC_WORKSTATUS_KEY in answer:
        return answer[CoordConsts.SVC_WORKSTATUS_KEY]
    else:
        raise BatfishException("Expected key not found in status check response: " + CoordConsts.SVC_WORKSTATUS_KEY), answer
