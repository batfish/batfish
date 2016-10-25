import requests
import tempfile
from requests_toolbelt.multipart.encoder import MultipartEncoder
from coordconsts import CoordConsts
from org.batfish.util.batfish_exception import BatfishException

def post_data(session, resource, jsonData):
    multipart_data = MultipartEncoder(jsonData)
    response = requests.post(
        session.get_url(resource), 
        data=multipart_data, 
        verify=False, 
        headers={'Content-Type': multipart_data.content_type},
        #uncomment line below if you want http capture by fiddler
        #proxies = {'http': 'http://127.0.0.1:8888', 'https': 'http://127.0.0.1:8888'}
    )

    response.raise_for_status();
    
    jsonResponse = response.json()

    if (jsonResponse[0] != CoordConsts.SVC_SUCCESS_KEY):
        raise BatfishException("Coordinator returned failure: " + jsonResponse[1])

    return jsonResponse[1]

def get_object(session, objectName):
    
    jsonData = {}
    jsonData[CoordConsts.SVC_API_KEY] = session.apiKey
    jsonData[CoordConsts.SVC_CONTAINER_NAME_KEY] = session.container
    jsonData[CoordConsts.SVC_TESTRIG_NAME_KEY] = session.baseTestrig
    jsonData[CoordConsts.SVC_OBJECT_NAME_KEY] = objectName
    
    multipart_data = MultipartEncoder(jsonData)
    response = requests.post(
        session.get_url(CoordConsts.SVC_GET_OBJECT_RSC), 
        data=multipart_data, 
        verify=False, 
        stream=True,
        headers={'Content-Type': multipart_data.content_type},
        #uncomment line below if you want http capture by fiddler
        #proxies = {'http': 'http://127.0.0.1:8888', 'https': 'http://127.0.0.1:8888'}
    )

    response.raise_for_status();

    tempFile = tempfile.NamedTemporaryFile();
    
    with open(tempFile.name, 'wb') as fd:
        for chunk in response.iter_content(1000):
            fd.write(chunk)
            
    return tempFile