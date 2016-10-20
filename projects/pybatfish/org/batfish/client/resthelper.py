import requests
from requests_toolbelt.multipart.encoder import MultipartEncoder
from coordconsts import CoordConsts

def post_data(session, resource, jsonData):
    multipart_data = MultipartEncoder(jsonData)
    response = requests.post(
        session.get_url(resource), 
        data=multipart_data, 
        verify=False, 
        headers={'Content-Type': multipart_data.content_type}
    )

    if (response.status_code != requests.codes.OK):
        return None
    
    jsonResponse = response.json()

    if (jsonResponse[0] != CoordConsts.SVC_SUCCESS_KEY):
        return None

    return jsonResponse[1]
