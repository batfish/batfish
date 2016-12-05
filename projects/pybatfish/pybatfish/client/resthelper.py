import tempfile
import time
import requests
from requests_toolbelt.multipart.encoder import MultipartEncoder
from requests.exceptions import ConnectionError

from coordconsts import CoordConsts
from options import Options
from pybatfish.util.batfish_exception import BatfishException

def get_object(session, objectName):
    
    jsonData = {}
    jsonData[CoordConsts.SVC_API_KEY] = session.apiKey
    jsonData[CoordConsts.SVC_CONTAINER_NAME_KEY] = session.container
    jsonData[CoordConsts.SVC_TESTRIG_NAME_KEY] = session.baseTestrig
    jsonData[CoordConsts.SVC_OBJECT_NAME_KEY] = objectName
    
    response = _post_data(session, CoordConsts.SVC_GET_OBJECT_RSC, jsonData, stream=True)

    tempFile = tempfile.NamedTemporaryFile();
    for chunk in response.iter_content(1000):
        tempFile.write(chunk)
    tempFile.flush()
    tempFile.seek(0)
            
    return tempFile

def get_json_response(session, resource, jsonData):
    response = _post_data(session, resource, jsonData)    

    jsonResponse = response.json()
    if (jsonResponse[0] != CoordConsts.SVC_SUCCESS_KEY):
        raise BatfishException("Coordinator returned failure: " + jsonResponse[1])

    return jsonResponse[1]

def _post_data(session, resource, jsonData, stream=False):
    multipart_data = MultipartEncoder(jsonData)
    
    numTriesLeft = Options.max_tries_to_coonnect_to_coordinator
    numTries = 0
    while (numTriesLeft > 0):
        numTries = numTries + 1
        numTriesLeft = numTriesLeft - 1
        try:
            response = requests.post(
                session.get_url(resource), 
                data=multipart_data, 
                verify=session.verifySslCerts,
                stream=stream,
                headers={'Content-Type': multipart_data.content_type},
                #uncomment line below if you want http capture by fiddler
                #proxies = {'http': 'http://127.0.0.1:8888', 'https': 'http://127.0.0.1:8888'}
            )
            response.raise_for_status()
            return response
        except ConnectionError, e:
            if (numTries > Options.num_tries_warn_threshold):
                session.logger.info("Could not connect to coordinator at %s. NumTriesLeft = %d", session.get_url(resource), numTriesLeft)
            if (numTriesLeft <= 0):
                raise BatfishException("Failed to connect to Coordinator", cause=BatfishException(e))
            else:
                time.sleep(Options.seconds_to_sleep_between_tries_to_coordinator);

        

