# Author: Ratul Mahajan
# Copyright 2016 Intentionet

import os
import json
import logging
import tempfile

from org.batfish.util.batfish_exception import BatfishException
import org.batfish.util.util as batfishutils
from coordconsts import CoordConsts
from bfconsts import BfConsts
from options import Options
import resthelper 
from session import Session
from questionhelper import bf_get_question_json
import workhelper

#suppress the urllib3 warnings due to old version of urllib3 (inside requests)
import requests.packages.urllib3
requests.packages.urllib3.disable_warnings()

_bfDebug = True;

bf_logger = logging.getLogger("org.batfish.client")
bf_session = Session(bf_logger)

if (_bfDebug):
    bf_logger.setLevel(logging.INFO)
    bf_logger.addHandler(logging.StreamHandler())
else:
    bf_logger.addHandler(logging.NullHandler())



def bf_answer(questionStr, parametersStr="{}", doDelta=False):
    '''
    Answer a question based on json strings for question and parametersJsonStr
    '''
    _check_base_testrig();
    if (doDelta):
        _check_delta_testrig();

    #these conversions verify that the strings are proper json
    _questionJson = json.loads(questionStr)
    _parametersJson = json.loads(parametersStr)
 
    questionName = Options.default_question_prefix + "_" + batfishutils.get_uuid()

    jsonData = workhelper.get_data_answer(bf_session, questionName, questionStr, parametersStr)
    resthelper.post_data(bf_session, CoordConsts.SVC_UPLOAD_QUESTION_RSC, jsonData)

    workItem = workhelper.get_workitem_answer(bf_session, questionName, doDelta)
    answer = workhelper.execute(workItem, bf_session)

    return answer

def bf_answer_type(questionType, doDelta=False, **parameters):
    '''
    Answer a question based on question type
    '''
    questionJson = bf_get_question_json(questionType)    
    for param in parameters.keys():
        questionJson[param] = parameters[param]

    bf_logger.info("Question: %s", json.dumps(questionJson))
     
    return bf_answer(json.dumps(questionJson), doDelta=doDelta)

def bf_generate_dataplane(doDelta=False):
    '''
    Generate the data plane for base or delta testrig
    '''
    _check_base_testrig();
    if (doDelta):
        _check_delta_testrig();

    workItem = workhelper.get_workitem_generate_dataplane(bf_session, doDelta)
    answer = workhelper.execute(workItem, bf_session)

    return answer
    
def bf_help():
    '''
    Lists basic functions
    '''
    print """
    Basic function calls
        bf_answer           Answer a question based on question json
        bf_answer_file      Answer a question based on question file
        bf_init_container   Initializes a new container
        bf_init_testrig     Initializes a new testrig
        bf_list_testrigs    Lists all the testrigs

    Type 'help(func_name)' to get help on a specific function.
    Type 'bf_help_advanced()' to list advanced commands
    """

def bf_help_advanced():
    '''
    Lists advanced functions
    '''
    print """
    Advanced function calls
        bf_generate_dataplane Generate the dataplane
        bf_reinit_testrig     Re-initializes an existing testrig

    Type 'help(func_name)' to get help on a specific function.
    Type 'bf_help()' to list basic commands
    """

def bf_init_container(containerPrefix=Options.default_container_prefix):
    '''
    Initialize a new container
    '''
    
    jsonData = workhelper.get_data_init_container(bf_session, containerPrefix)
    jsonResponse = resthelper.post_data(bf_session, CoordConsts.SVC_INIT_CONTAINER_RSC, jsonData)
    
    if (jsonResponse[CoordConsts.SVC_CONTAINER_NAME_KEY]):
        bf_session.container = jsonResponse[CoordConsts.SVC_CONTAINER_NAME_KEY]
        bf_logger.info("Container is now set to " + bf_session.container)
    else:
        raise BatfishException("Bad json response in init_container; missing expected key: " + CoordConsts.SVC_CONTAINER_NAME_KEY, jsonResponse);

def bf_init_environment(environmentName=None, interfaceBlacklist=None, nodeBlacklist=None):
    
    _check_base_testrig()
    
    if (environmentName is None):
        environmentName = Options.default_delta_env_prefix + batfishutils.get_uuid()
        
    bf_session.deltaTestrig = bf_session.baseTestrig
    bf_session.deltaEnvironment = environmentName
    bf_logger.info("Delta testrig/environment is now set to %s/%s", bf_session.deltaTestrig, bf_session.deltaEnvironment)
    
    return bf_answer_type("environmentcreation", environmentName=environmentName, interfaceBlacklist=interfaceBlacklist, nodeBlacklist=nodeBlacklist)
          
def bf_init_testrig(dirOrZipfile, doDelta=False, testrigName=None):
    '''
    Initialize a new testrig
    '''

    fileToSend = dirOrZipfile
    
    if (os.path.isdir(dirOrZipfile)):
        tempFile = tempfile.NamedTemporaryFile()
        batfishutils.zip_dir(dirOrZipfile, tempFile)
        fileToSend = tempFile.name

    if (bf_session.container is None):
        bf_init_container()

    if (testrigName is None):
        testrigName = Options.default_testrig_prefix + batfishutils.get_uuid()
    
    jsonData = workhelper.get_data_upload_testrig(bf_session, testrigName, fileToSend)
    resthelper.post_data(bf_session, CoordConsts.SVC_UPLOAD_TESTRIG_RSC, jsonData)
    
    if (not doDelta):
        bf_session.baseTestrig = testrigName
        bf_session.baseEnvironment = BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME
        bf_logger.info("Base testrig/environment is now set to %s/%s", bf_session.baseTestrig, bf_session.baseEnvironment)
    else:
        bf_session.deltaTestrig = testrigName
        bf_session.deltaEnvironment = BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME        
        bf_logger.info("Delta testrig/environment is now set to %s/%s", bf_session.deltaTestrig, bf_session.deltaEnvironment)
    
    workItem = workhelper.get_workitem_parse(bf_session, doDelta)
    answer = workhelper.execute(workItem, bf_session)

    return answer

def bf_list_testrigs(currentContainerOnly=False):
    
    containerName = None
    
    if (currentContainerOnly):
        _check_container()
        containerName = bf_session.container
        
    jsonData = workhelper.get_data_list_testrigs(bf_session, containerName)
    jsonResponse = resthelper.post_data(bf_session, CoordConsts.SVC_LIST_TESTRIGS_RSC, jsonData)

    return jsonResponse

def bf_reinit_testrig(doDelta=False):
    '''
    Re-initialize the current base or delta testrig
    '''

    _check_base_testrig();
    if (doDelta):
        _check_delta_testrig();

    workItem = workhelper.get_workitem_parse(bf_session, doDelta)
    answer = workhelper.execute(workItem, bf_session)

    return answer

def bf_set_container(containerName):
    bf_session.container = containerName
    bf_logger.info("Container is now set to " + bf_session.container)

def bf_set_testrig(testrigName):
    bf_session.baseTestrig = testrigName
    bf_session.baseEnvironment = BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME
    bf_logger.info("Base testrig/environment is now set to %s/%s", bf_session.baseTestrig, bf_session.baseEnvironment)

def _check_base_testrig():
    _check_container()
    if (bf_session.baseTestrig is None):
        raise BatfishException("Base testrig is not set")

    if (bf_session.baseEnvironment is None):
        raise BatfishException("Base environment is not set")

def _check_delta_testrig():
    _check_container()
    if (bf_session.deltaTestrig is None):
        raise BatfishException("Delta testrig is not set")

    if (bf_session.deltaEnvironment is None):
        raise BatfishException("Delta environment is not set")

def _check_container():
    if (bf_session.container is None):
        raise BatfishException("Container is not set")
    