import json
import requests


class IMS_HTTP_request_client():

    def __init__(self, base_url):
        self.base_url = base_url

    def send_post_request(self, endpoint, data, headers=None, json_data=True):
        url = f"{self.base_url}{endpoint}"
        if json_data:
            data = json.dumps(data)
            if headers is None:
                headers = {}
            headers['Content-Type'] = 'application/json; charset=utf-8'
            headers['Content-Length'] = str(len(data.encode('utf-8')))
        response = requests.post(url, data=data, headers=headers)

        return response