import logging

import flask
from flask import request

from api.overseerr.overseerr_helper import OverseerrHelper
from database.database import Database
from database.user_group import UserGroup


class Homepage:
    def __init__(self, database: Database, overseerr: OverseerrHelper):
        self.__logger = logging.getLogger(__name__)
        self.__database = database
        self.__overseerr = overseerr

    def on_call(self) -> str:
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

        soon_deleted_group = UserGroup(-1, soon_deleted_name, None, None, None, None, True, None)
        soon_deleted_media = self.__database.media_get_ready_to_delete()
        media_data[soon_deleted_group] = soon_deleted_media

        for media in soon_deleted_media:
            all_overseerr_media[media.overseerr_id] = media

        for group_and_media in sorted(group_and_medias, key=lambda x: x[0].name):
            user_group = group_and_media[0]
            media = group_and_media[1]

            if not user_group.display:
                continue
            if user_group not in media_data:
                media_data[user_group] = []

            media_data[user_group].append(media)
            all_overseerr_media[media.overseerr_id] = media

        for media in all_overseerr_media.values():
            url_data[media.overseerr_id] = self.__overseerr.get_media_details(media.overseerr_id, media.type)

        return flask.render_template('index.html', media_data=media_data, url_data=url_data, locale=locale)
