# Author: Ratul Mahajan
# Copyright 2016 Intentionet

import uuid

from org.batfish.util.batfish_exception import BatfishException
from coordconsts import CoordConsts
from options import Options
import resthelper 
from session import Session

bf_session = Session()

def bf_help():
    print "In the future, we'll list all commands here"
    
#              Client client = getClientBuilder().build();
#          WebTarget webTarget = getTarget(client,
#                CoordConsts.SVC_INIT_CONTAINER_RSC);
# 
#          MultiPart multiPart = new MultiPart();
#          multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
# 
#          addTextMultiPart(multiPart, CoordConsts.SVC_API_KEY,
#                _settings.getApiKey());
#          addTextMultiPart(multiPart, CoordConsts.SVC_CONTAINER_PREFIX_KEY,
#                containerPrefix);
# 
#          JSONObject jObj = postData(webTarget, multiPart);
#          if (jObj == null) {
#             return null;
#          }
# 
#          if (!jObj.has(CoordConsts.SVC_CONTAINER_NAME_KEY)) {
#             _logger.errorf("container name key not found in: %s\n",
#                   jObj.toString());
#             return null;
#          }
# 
#          return jObj.getString(CoordConsts.SVC_CONTAINER_NAME_KEY);

def bf_init_container(containerPrefix=Options.default_container_prefix):
    '''
    Initialize a new container
    '''
    
    jsonData = {}
    jsonData[CoordConsts.SVC_API_KEY] = bf_session.apiKey
    jsonData[CoordConsts.SVC_CONTAINER_PREFIX_KEY] = containerPrefix
    
    jsonResponse = resthelper.post_data(bf_session, CoordConsts.SVC_INIT_CONTAINER_RSC, jsonData)
    
    if (jsonResponse is None):
        bf_session.container = None
    else:
        bf_session.container = jsonResponse[CoordConsts.SVC_CONTAINER_NAME_KEY]
        
def bf_init_testrig(zipfileName, testrigName=None):
    '''
    Initialize a new testrig
    '''

    if (bf_session.container is None):
        bf_init_container()
    
    if (testrigName is None):
        testrigName = Options.default_testrig_prefix + str(uuid.uuid4())
    
    jsonData = {}
    jsonData[CoordConsts.SVC_API_KEY] = bf_session.apiKey
    jsonData[CoordConsts.SVC_CONTAINER_NAME_KEY] = bf_session.container
    jsonData[CoordConsts.SVC_TESTRIG_NAME_KEY] = testrigName
    jsonData[CoordConsts.SVC_ZIPFILE_KEY] = ('filename', open(zipfileName, 'rb'), 'application/octet-stream')

    jsonResponse = resthelper.post_data(bf_session, CoordConsts.SVC_UPLOAD_TESTRIG_RSC, jsonData)
    
    if (jsonResponse is None):
        bf_session.testrig = None
    else:
        bf_session.testrig = testrigName
        
    