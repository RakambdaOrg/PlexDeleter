import logging

from flask import Response, request


class WebhookRadarr:
    def __init__(self):
        self.__logger = logging.getLogger(__name__)

    def on_call(self) -> Response:
        payload = request.json
        self.__logger.info(f"Received Radarr webhook call with payload {payload}")

        return Response(status=200)
