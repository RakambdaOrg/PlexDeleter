import logging
import threading
from threading import Thread
from typing import Optional

import flask
from flask import Flask, request, Response
from waitress import serve

from action.deleter import Deleter
from action.notifier import Notifier
from action.status.user_group_status import UserGroupStatus
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

        self.__lock = threading.RLock()

        self.__app = Flask("PlexDeleter")
        self.__app.get('/')(self.home)
        self.__app.get('/maintenance/full')(self.maintenance_full)
        self.__app.get('/maintenance/updates')(self.maintenance_updates)
        self.__app.post('/webhook/overseerr')(self.webhook_overseerr)
        self.__app.post('/webhook/tautulli')(self.webhook_tautulli)
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

        for group_and_media in sorted(group_and_medias, key=lambda x: x[0].name):
            user_group = group_and_media[0]
            media = group_and_media[1]
            if user_group not in media_data:
                media_data[user_group] = []

            media_data[user_group].append(media)
            all_overseerr_media[media.overseerr_id] = media

        for media in all_overseerr_media.values():
            url_data[media.overseerr_id] = self.__overseerr.get_media_details(media.overseerr_id, media.type)

        return flask.render_template('index.html', media_data=media_data, url_data=url_data, locale=locale)

    def webhook_overseerr(self) -> Response:
        if not self.__is_authorized():
            return Response(status=401)

        payload = request.json
        self.__logger.info(f"Received overseerr webhook call with payload {payload}")

        notification_type = payload["notification_type"]
        if notification_type not in ["MEDIA_AUTO_APPROVED", "MEDIA_APPROVED"]:
            self.__logger.warning("Invalid notification type")
            return Response(status=400)

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

    def webhook_tautulli(self) -> Response:
        if not self.__is_authorized():
            return Response(status=401)

        payload = request.json
        self.__logger.info(f"Received tautulli webhook call with payload {payload}")

        payload_type = payload["type"]
        refresh_status = True
        refresh_watch = True
        user_id = None
        if payload_type == "watched":
            refresh_status = False
            user_id = int(payload["user_id"]) if payload["user_id"] else None
            pass
        elif payload_type == "added":
            refresh_watch = False
            pass

        thread = Thread(target=self.__run_maintenance_updates, args=(refresh_status, refresh_watch, user_id))
        thread.start()

        return Response(status=200)

    def __tag_excluded(self, request_tags: list[int], media_type: MediaType) -> bool:
        labels = []

        tags = self.__overseerr.get_servarr_tags(media_type)
        for request_tag in request_tags:
            for tag in tags:
                if tag["id"] == request_tag:
                    labels.append(tag["label"])

        return "no-deleter" in labels

    def maintenance_full(self) -> Response:
        if not self.__is_authorized():
            return Response(status=401)

        self.__logger.info("Received full maintenance request")
        thread = Thread(target=self.__run_maintenance_full)
        thread.start()
        return Response(status=200)

    def maintenance_updates(self) -> Response:
        if not self.__is_authorized():
            return Response(status=401)

        self.__logger.info("Received update maintenance request")
        thread = Thread(target=self.__run_maintenance_updates)
        thread.start()
        return Response(status=200)

    def __run_maintenance_full(self):
        with self.__lock:
            user_group_statuses = self.__run_maintenance_updates()
            self.__deleter.delete()
            self.__notifier.notify_watchlist(user_group_statuses)
            self.__logger.info("Full maintenance done")

    def __run_maintenance_updates(self, refresh_status: bool = True, refresh_watch: bool = True, user_id: Optional[int] = None) -> dict[UserGroup, UserGroupStatus]:
        with self.__lock:
            if refresh_status:
                self.__status_updater.update()

            user_group_statuses = {}
            if refresh_watch:
                user_group_statuses = self.__watch_updater.update(user_id)

            self.__logger.info("Updates maintenance done")
            return user_group_statuses

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

    def __is_authorized(self):
        authorization = request.headers.get('Authorization')
        result = authorization == self.__authorization
        if not result:
            self.__logger.warning(f"Rejected authorization, received {authorization}")
        return result
