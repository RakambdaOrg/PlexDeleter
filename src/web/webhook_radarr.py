import logging

from flask import Response, request

from web.web_utils import WebUtils


class WebhookRadarr:
    def __init__(self, web_utils: WebUtils):
        self.__logger = logging.getLogger(__name__)
        self.__web_utils = web_utils

    def on_webhook_radarr(self) -> Response:
        if not self.__web_utils.is_authorized():
            return Response(status=401)

        payload = request.json
        self.__logger.info(f"Received Radarr webhook call with payload {payload}")

        return Response(status=200)
