import logging
from threading import Thread
from typing import Optional

from flask import Response, request

from action.notifier import Notifier
from action.status_updater import StatusUpdater
from api.discord.discord_helper import DiscordHelper
from api.overseerr.overseerr_helper import OverseerrHelper
from database.database import Database
from database.media_action_status import MediaActionStatus
from database.media_status import MediaStatus
from database.media_type import MediaType
from web.web_utils import WebUtils


class WebhookOverseerr:
    def __init__(self, web_utils: WebUtils, overseerr: OverseerrHelper, database: Database, discord: DiscordHelper, status_updater: StatusUpdater, notifier: Notifier):
        self.__logger = logging.getLogger(__name__)
        self.__web_utils = web_utils
        self.__overseerr = overseerr
        self.__database = database
        self.__discord = discord
        self.__status_updater = status_updater
        self.__notifier = notifier

    def on_webhook_overseerr(self) -> Response:
        if not self.__web_utils.is_authorized():
            return Response(status=401)

        payload = request.json
        self.__logger.info(f"Received Overseerr webhook call with payload {payload}")

        notification_type = payload["notification_type"]
        if notification_type in ["MEDIA_AUTO_APPROVED", "MEDIA_APPROVED"]:
            return self.__on_media_approved(payload)
        if notification_type in ["MEDIA_AVAILABLE"]:
            return self.__on_media_added()

        self.__logger.warning("Invalid notification type")
        return Response(status=400)

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
            self.__logger.info("Skipped adding media because excluded tag is present")
            return Response(status=204)

        seasons = self.__extract_seasons(payload)

        if len(seasons) > 0:
            for season in seasons:
                self.__handle_season(overseerr_id, name, plex_user_id, season, media_type)
        else:
            self.__handle_season(overseerr_id, name, plex_user_id, None, media_type)

        return Response(status=200)

    def __handle_season(self, overseerr_id: int, name: str, plex_user_id: int, season: Optional[int], media_type: MediaType):
        self.__logger.info(f"Handling requirement request for media with overseerr id {overseerr_id} (Season {season}) on plex id {plex_user_id}")
        medias = self.__database.media_get_by_overseerr_id(overseerr_id, season)
        user_groups = set(self.__database.user_group_get_with_plex_id(plex_user_id))

        if len(medias) == 0:
            self.__database.media_add(overseerr_id, name, season, media_type, MediaStatus.RELEASING, MediaActionStatus.TO_DELETE)
            medias = self.__database.media_get_by_overseerr_id(overseerr_id, season)
            for media in medias:
                self.__logger.info(f"Added media {media}")
                self.__discord.notify_media_added(media)
        else:
            for media in medias:
                self.__database.media_set_status(media.id, MediaStatus.RELEASING)
                self.__database.media_set_action_status(media.id, MediaActionStatus.TO_DELETE)

        self.__status_updater.update_medias(medias)

        if season:
            user_groups.update(self.__database.user_group_get_watching(overseerr_id, season - 1))

        for media in medias:
            for user_group in user_groups:
                self.__logger.info(f"Added media requirement for {user_group} on {media}")
                self.__database.media_requirement_add(media.id, user_group.id)
                self.__discord.notify_media_requirement_added(media, user_group)
                self.__notifier.notify_requirement_added(user_group, media)

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
