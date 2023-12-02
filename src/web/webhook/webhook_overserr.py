import logging
from threading import Thread

from flask import Response

from api.overseerr.overseerr_helper import OverseerrHelper
from database.media_type import MediaType
from web.web_utils import WebUtils


class WebhookOverseerr:
    def __init__(self, web_utils: WebUtils, overseerr: OverseerrHelper):
        self.__logger = logging.getLogger(__name__)
        self.__web_utils = web_utils
        self.__overseerr = overseerr

    def on_call(self, payload: dict) -> Response:
        notification_type = payload["notification_type"]
        if notification_type in ["MEDIA_AUTO_APPROVED", "MEDIA_APPROVED"]:
            return self.__on_media_approved(payload)
        if notification_type in ["MEDIA_AVAILABLE"]:
            return self.__on_media_added()

        return Response(status=400, response="Invalid notification type")

    def __on_media_added(self):
        thread = Thread(target=self.__web_utils.run_maintenance_updates, args=(True, False, None))
        thread.start()

        return Response(status=200)

    def __on_media_approved(self, payload: dict):
        request_id = payload["request"]["request_id"]
        request_details = self.__overseerr.get_request_details(request_id)

        media_type = MediaType.from_overseerr(payload["media"]["media_type"])
        plex_user_id = request_details.requester_id
        overseerr_id = int(payload["media"]["tmdbId"])
        name = payload["subject"]

        if self.__tag_excluded(request_details.tags, media_type):
            return Response(status=204, response="Skipped adding media because excluded tag is present")

        seasons = self.__extract_seasons(payload)

        if len(seasons) > 0:
            for season in seasons:
                self.__web_utils.handle_season(overseerr_id, name, plex_user_id, season, media_type)
        else:
            self.__web_utils.handle_season(overseerr_id, name, plex_user_id, None, media_type)

        return Response(status=200)

    def __tag_excluded(self, request_tags: list[int], media_type: MediaType) -> bool:
        labels = []

        tags = self.__overseerr.get_servarr_tags(media_type)
        for request_tag in request_tags:
            for tag in tags:
                if tag["id"] == request_tag:
                    labels.append(tag["label"])

        return "no-deleter" in labels

    @staticmethod
    def __extract_seasons(payload) -> list[int]:
        seasons = []
        if "extra" not in payload:
            return seasons

        extras = payload["extra"]
        for extra in extras:
            if extra["name"] == "Requested Seasons":
                values = extra["value"].split(",")
                for value in values:
                    seasons.append(int(value))
        return seasons
