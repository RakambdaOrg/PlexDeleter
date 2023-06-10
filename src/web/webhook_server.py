import asyncio
import logging
from typing import Optional

from flask import Flask, request, Response
from waitress import serve

from action.deleter import Deleter
from action.notifier import Notifier
from action.status_updater import StatusUpdater
from action.watch_updater import WatchUpdater
from api.discord.discord_helper import DiscordHelper
from api.overseerr.overseerr_helper import OverseerrHelper
from database.database import Database
from database.media_action_status import MediaActionStatus
from database.media_status import MediaStatus
from database.media_type import MediaType


class WebhookServer:
    def __init__(self, overseerr: OverseerrHelper, database: Database, discord: DiscordHelper, status_updater: StatusUpdater, watch_updater: WatchUpdater, deleter: Deleter, notifier: Notifier):
        self.__overseerr = overseerr
        self.__database = database
        self.__discord = discord
        self.__status_updater = status_updater
        self.__watch_updater = watch_updater
        self.__deleter = deleter
        self.__notifier = notifier
        self.__logger = logging.getLogger(__name__)

        self.__app = Flask("PlexDeleter")
        self.__app.get('/maintenance')(self.run_maintenance)
        self.__app.post('/webhook')(self.webhook)

    def run(self):
        serve(self.__app, host="0.0.0.0", port=8080)

    def webhook(self) -> Response:
        payload = request.json
        self.__logger.info(f"Received webhook call with payload {payload}")

        notification_type = payload["notification_type"]
        if notification_type not in ["MEDIA_AUTO_APPROVED", "MEDIA_APPROVED"]:
            self.__logger.warning("Invalid notification type")
            return Response(status=400)

        plex_user_id = self.__overseerr.get_requester_plex_id(payload["request"]["request_id"])
        overseerr_id = int(payload["media"]["tmdbId"])
        media_type = MediaType.from_overseerr(payload["media"]["media_type"])
        name = payload["subject"]
        seasons = self.extract_seasons(payload)

        if len(seasons) > 0:
            for season in seasons:
                self.handle_season(overseerr_id, name, plex_user_id, season, media_type)
        else:
            self.handle_season(overseerr_id, name, plex_user_id, None, media_type)

        return Response(status=200)

    def maintenance(self) -> Response:
        self.__logger.info("Received maintenance request")
        asyncio.ensure_future(self.run_maintenance())
        return Response(status=200)

    async def run_maintenance(self):
        self.__status_updater.update()
        user_group_statuses = self.__watch_updater.update()
        self.__deleter.delete()
        self.__notifier.notify(user_group_statuses)

    @staticmethod
    def extract_seasons(payload) -> list[int]:
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

    def handle_season(self, overseerr_id: int, name: str, plex_user_id: int, season: Optional[int], media_type: MediaType):
        self.__logger.info(f"Handling requirement request for media with overseerr id {overseerr_id} (Season {season}) on plex id {plex_user_id}")
        medias = self.__database.media_get_by_overseerr_id(overseerr_id, season)
        user_groups = self.__database.user_group_get_with_plex_id(plex_user_id)

        if len(medias) == 0:
            self.__database.media_add(overseerr_id, name, season, media_type, MediaStatus.RELEASING, MediaActionStatus.TO_DELETE)
            medias = self.__database.media_get_by_overseerr_id(overseerr_id, season)
            for media in medias:
                self.__logger.info(f"Added media {media}")
                self.__discord.notify_media_added(media)

        for media in medias:
            for user_group in user_groups:
                self.__logger.info(f"Added media requirement for {user_group} on {media}")
                self.__database.media_requirement_add(media.id, user_group.id)
                self.__discord.notify_media_requirement_added(media, user_group)
