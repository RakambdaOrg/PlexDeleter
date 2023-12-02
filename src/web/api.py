import logging

from flask import Response

from api.overseerr.overseerr_helper import OverseerrHelper
from database.database import Database
from database.media_requirement_status import MediaRequirementStatus
from database.media_type import MediaType
from web.web_utils import WebUtils


class Api:
    def __init__(self, database: Database, web_utils: WebUtils, overseerr_helper: OverseerrHelper):
        self.__logger = logging.getLogger(__name__)
        self.__web_utils = web_utils
        self.__database = database
        self.__overseerr_helper = overseerr_helper

    def on_add_requirement(self, form: dict):
        overseerr_id = int(form.get('overseerrId'))
        season = form.get('seasonNumber') or None
        plex_user_id = int(form.get('plexUserId'))
        media_type = MediaType.from_overseerr(form.get('mediaType'))

        media = self.__overseerr_helper.get_media_details(overseerr_id, media_type)

        if not media.name:
            return Response(status=404, response='No such media')

        self.__web_utils.handle_season(overseerr_id, media.name, plex_user_id, int(season) if season else season, media_type)
        return Response(status=200)

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

        return Response(status=200)
