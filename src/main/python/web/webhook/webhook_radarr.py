import logging

from flask import Response


class WebhookRadarr:
    def __init__(self):
        self.__logger = logging.getLogger(__name__)

    def on_call(self) -> Response:
        return Response(status=200)
