import logging

from flask import Response, request

from api.overseerr.overseerr_helper import OverseerrHelper
from web.web_utils import WebUtils


class WebhookOmbi:
    def __init__(self, web_utils: WebUtils, overseerr: OverseerrHelper):
        self.__logger = logging.getLogger(__name__)
        self.__web_utils = web_utils
        self.__overseerr = overseerr

    def on_call(self, payload: dict) -> Response:
        data = request.data
        self.__logger.info(f"Got Ombi request with payload {data}")
        return Response(status=400, response="Invalid notification type")
