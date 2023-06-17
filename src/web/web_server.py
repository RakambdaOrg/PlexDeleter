import logging
from threading import Thread
from typing import Optional

import flask
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
from database.user_group import UserGroup


class WebServer:
    def __init__(self, bearer_token: str, overseerr: OverseerrHelper, database: Database, discord: DiscordHelper, status_updater: StatusUpdater, watch_updater: WatchUpdater, deleter: Deleter, notifier: Notifier):
        self.__logger = logging.getLogger(__name__)
        self.__authorization = f"Bearer {bearer_token}"
        self.__overseerr = overseerr
        self.__database = database
        self.__discord = discord
        self.__status_updater = status_updater
        self.__watch_updater = watch_updater
        self.__deleter = deleter
        self.__notifier = notifier

        self.__app = Flask("PlexDeleter")
        self.__app.get('/')(self.home)
        self.__app.get('/maintenance')(self.maintenance)
        self.__app.post('/webhook')(self.webhook)
        self.__app.route('/favicon.svg')(self.favicon)

    def run(self):
        serve(self.__app, host="0.0.0.0", port=8080)

    def favicon(self):
        return flask.send_from_directory('static', 'favicon.svg', mimetype='image/svg+xml')

    def home(self) -> str:
        self.__logger.info(f"Received home call")
        all_overseerr_media = {}
        media_data = {}
        url_data = {}

        supported_languages = ["en", "fr"]
        locale = request.accept_languages.best_match(supported_languages)

        group_and_medias = self.__database.media_get_waiting_with_groups()

        soon_deleted_name = "Soon deleted"
        if locale == "fr":
            soon_deleted_name = "Bientôt supprimé"

        soon_deleted_group = UserGroup(-1, soon_deleted_name, None, None, None, None)
        soon_deleted_media = self.__database.media_get_ready_to_delete()
        media_data[soon_deleted_group] = soon_deleted_media

        for media in soon_deleted_media:
            all_overseerr_media[media.overseerr_id] = media

        for group_and_media in group_and_medias:
            user_group = group_and_media[0]
            media = group_and_media[1]
            if user_group not in media_data:
                media_data[user_group] = []

            media_data[user_group].append(media)
            all_overseerr_media[media.overseerr_id] = media

        for media in all_overseerr_media.values():
            url_data[media.overseerr_id] = self.__overseerr.get_plex_url(media.overseerr_id, media.type)

        return flask.render_template('index.html', media_data=media_data, url_data=url_data, locale=locale)

    def webhook(self) -> Response:
        if not self.__is_authorized():
            return Response(status=401)

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
        seasons = self.__extract_seasons(payload)

        if len(seasons) > 0:
            for season in seasons:
                self.__handle_season(overseerr_id, name, plex_user_id, season, media_type)
        else:
            self.__handle_season(overseerr_id, name, plex_user_id, None, media_type)

        return Response(status=200)

    def maintenance(self) -> Response:
        if not self.__is_authorized():
            return Response(status=401)

        self.__logger.info("Received maintenance request")
        thread = Thread(target=self.__run_maintenance)
        thread.start()
        return Response(status=200)

    def __run_maintenance(self):
        self.__status_updater.update()
        user_group_statuses = self.__watch_updater.update()
        self.__deleter.delete()
        self.__notifier.notify(user_group_statuses)
        self.__logger.info("Maintenance done")

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

    def __handle_season(self, overseerr_id: int, name: str, plex_user_id: int, season: Optional[int], media_type: MediaType):
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

    def __is_authorized(self):
        authorization = request.headers.get('Authorization')
        result = authorization == self.__authorization
        if not result:
            self.__logger.warning(f"Rejected authorization, received {authorization}")
        return result
