import logging
from threading import Thread

from flask import Response, request

from web.web_utils import WebUtils


class WebhookTautulli:
    def __init__(self, web_utils: WebUtils):
        self.__logger = logging.getLogger(__name__)
        self.__web_utils = web_utils

    def on_call(self) -> Response:
        payload = request.json

        payload_type = payload["type"]
        if "media_type" not in payload or payload["media_type"] == 'track':
            self.__logger.info("Skipping update as media is a track")
            return Response(status=200)

        refresh_status = True
        refresh_watch = True
        user_id = None
        if payload_type == "watched":
            refresh_status = False
            user_id = int(payload["user_id"]) if payload["user_id"] else None
        elif payload_type == "added":
            refresh_watch = False

        thread = Thread(target=self.__web_utils.run_maintenance_updates, args=(refresh_status, refresh_watch, user_id))
        thread.start()

        return Response(status=200)
