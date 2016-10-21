# Author: Ratul Mahajan
# Copyright 2016 Intentionet

import os
import tempfile

from org.batfish.util.batfish_exception import BatfishException
import org.batfish.util.util as batfishutils
from coordconsts import CoordConsts
from bfconsts import BfConsts
from options import Options
import resthelper 
from session import Session
import workhelper

bf_session = Session()

def bf_help():
    print "In the future, we'll list all commands here"

def bf_init_container(containerPrefix=Options.default_container_prefix):
    '''
    Initialize a new container
    '''
    
    jsonData = {}
    jsonData[CoordConsts.SVC_API_KEY] = bf_session.apiKey
    jsonData[CoordConsts.SVC_CONTAINER_PREFIX_KEY] = containerPrefix
    
    jsonResponse = resthelper.post_data(bf_session, CoordConsts.SVC_INIT_CONTAINER_RSC, jsonData)
    
    if (jsonResponse[CoordConsts.SVC_CONTAINER_NAME_KEY]):
        bf_session.container = jsonResponse[CoordConsts.SVC_CONTAINER_NAME_KEY]
    else:
        raise BatfishException("Bad json response in init_container; missing expected key: " + CoordConsts.SVC_CONTAINER_NAME_KEY, jsonResponse);
                       
def bf_init_testrig(dirOrZipfile, testrigName=None):
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
    
    jsonData = {}
    jsonData[CoordConsts.SVC_API_KEY] = bf_session.apiKey
    jsonData[CoordConsts.SVC_CONTAINER_NAME_KEY] = bf_session.container
    jsonData[CoordConsts.SVC_TESTRIG_NAME_KEY] = testrigName
    jsonData[CoordConsts.SVC_ZIPFILE_KEY] = ('filename', open(fileToSend, 'rb'), 'application/octet-stream')

    resthelper.post_data(bf_session, CoordConsts.SVC_UPLOAD_TESTRIG_RSC, jsonData)
    
    bf_session.baseTestrig = testrigName
    bf_session.baseEnvironment = BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME
    
    workItem = workhelper.get_workitem_parse(bf_session)

    answer = workhelper.execute(workItem, bf_session)

    return answer

def bf_answer(questionJson, parameters="", isDelta=False):
    '''
    Answer a question
    '''
    _check_container();
    _check_base_testrig();
    if (isDelta):
        _check_delta_testrig();

    questionName = Options.default_question_prefix + "_" + batfishutils.get_uuid()

    jsonData = {}
    jsonData[CoordConsts.SVC_API_KEY] = bf_session.apiKey
    jsonData[CoordConsts.SVC_CONTAINER_NAME_KEY] = bf_session.container
    jsonData[CoordConsts.SVC_TESTRIG_NAME_KEY] = bf_session.baseTestrig
    jsonData[CoordConsts.SVC_QUESTION_NAME_KEY] = questionName
    jsonData[CoordConsts.SVC_FILE_KEY] =  ('question', questionJson)
    jsonData[CoordConsts.SVC_FILE2_KEY] = ('parameters', parameters)

    resthelper.post_data(bf_session, CoordConsts.SVC_UPLOAD_QUESTION_RSC, jsonData)

    workItem = workhelper.get_workitem_answer_question(bf_session, questionName, isDelta)

    answer = workhelper.execute(workItem, bf_session)

    return answer
        
def _check_base_testrig():
    if (bf_session.baseTestrig is None):
        raise BatfishException("Base testrig is not set")

    if (bf_session.baseEnvironment is None):
        raise BatfishException("Base environment is not set")

def _check_delta_testrig():
    if (bf_session.deltaTestrig is None):
        raise BatfishException("Delta testrig is not set")

    if (bf_session.deltaEnvironment is None):
        raise BatfishException("Delta environment is not set")

def _check_container():
    if (bf_session.container is None):
        raise BatfishException("Container is not set")
    