import logging
from action.notifier import Notifier
from api.overseerr.overseerr_helper import OverseerrHelper
from database.database import Database
from database.media_requirement_status import MediaRequirementStatus
from database.media_type import MediaType
from flask import Response
from web.web_utils import WebUtils


class Api:
    def __init__(self, database: Database, web_utils: WebUtils, overseerr_helper: OverseerrHelper, notifier: Notifier):
        self.__logger = logging.getLogger(__name__)
        self.__web_utils = web_utils
        self.__database = database
        self.__overseerr_helper = overseerr_helper
        self.__notifier = notifier

    def on_add_requirement(self, form: dict):
        overseerr_id = int(form.get('overseerrId'))
        season = form.get('seasonNumber') or None
        plex_user_id = int(form.get('plexUserId'))
        media_type = MediaType.from_overseerr(form.get('mediaType'))

        media = self.__overseerr_helper.get_media_details(overseerr_id, media_type)

        if not media.name:
            return Response(status=404, response='No such media')

        self.__web_utils.handle_season(overseerr_id, media.name, plex_user_id, int(season) if season else season, media_type)
        return Response(status=200, response='Ok')

    def on_abandon_requirement(self, form: dict):
        overseerr_id = int(form.get('overseerrId'))
        season = form.get('seasonNumber') or None
        user_group_id = int(form.get('userGroupId'))
        media_type = MediaType.from_overseerr(form.get('mediaType'))

        medias = self.__database.media_get_by_overseerr_id(overseerr_id, int(season) if season else season, media_type)
        if len(medias) == 0:
            return Response(status=404, response='No such media')

        for media in medias:
            self.__database.media_requirement_set_status(media.id, user_group_id, MediaRequirementStatus.ABANDONED)
            self.__notifier.notify_abandoned(media, user_group_id)

        return Response(status=200, response='Ok')

    def on_complete_requirement(self, form: dict):
        overseerr_id = int(form.get('overseerrId'))
        season = form.get('seasonNumber') or None
        user_group_id = int(form.get('userGroupId'))
        media_type = MediaType.from_overseerr(form.get('mediaType'))

        medias = self.__database.media_get_by_overseerr_id(overseerr_id, int(season) if season else season, media_type)
        if len(medias) == 0:
            return Response(status=404, response='No such media')

        for media in medias:
            self.__database.media_requirement_set_status(media.id, user_group_id, MediaRequirementStatus.WATCHED)
            self.__notifier.notify_completed(media, user_group_id)

        return Response(status=200, response='Ok')

    def on_delete_media(self, form: dict):
        media_id = int(form.get('mediaId'))

        medias = self.__database.media_get_by_id(media_id)
        user_groups = self.__database.user_group_get_watching_media(media_id)

        self.__database.media_delete(media_id)
        self.__notifier.notify_deleted(medias, user_groups)

        return Response(status=200, response='Ok')
