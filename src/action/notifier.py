import datetime
import logging

from src.api.overseerr.overseerr_helper import OverseerrHelper
from src.api.mail.mailer import Mailer
from src.database.database import Database
from src.database.media import Media
from src.database.media_type import MediaType
from src.database.user_group import UserGroup


class Notifier:
    def __init__(self, database: Database, overseerr: OverseerrHelper, mailer: Mailer, server_id: str):
        self.__database = database
        self.__overseerr = overseerr
        self.__mailer = mailer
        self.__server_id = server_id
        self.__logger = logging.getLogger(__name__)

    def notify(self) -> None:
        self.__logger.info("Notifying all groups")
        user_groups = self.__database.user_group_get_all()
        for user_group in user_groups:
            self.__notify_group(user_group)

    def __notify_group(self, user_group: UserGroup) -> None:
        self.__logger.info(f"Notifying {user_group}")
        if datetime.datetime.now() - user_group.last_notification < datetime.timedelta(days=6, hours=23):
            self.__logger.debug("Too early to notify group")
            return

        medias = self.__database.media_get_waiting_for_user_group(user_group.id)
        if len(medias) <= 0:
            self.__logger.debug("Nothing to notify")
        else:
            locale = user_group.locale
            mails = user_group.mail.split(',') if user_group.mail else []

            subject = self.__get_subject(locale)
            text_message = self.__get_text_body(locale, medias)
            html_message = self.__get_html_body(locale, medias)
            self.__mailer.send(mails, subject, text_message, html_message)

        self.__database.user_group_set_last_notified(user_group.id, datetime.datetime.now())

    def __get_text_body(self, locale: str, medias: list[Media]) -> str:
        media_list = "\n".join([f"* {self.__get_media_body(locale, media)}" for media in medias])
        return f'{self.__get_header(locale)}\n{media_list}'

    def __get_html_body(self, locale: str, medias: list[Media]) -> str:
        contents = []
        for media in medias:
            body = self.__get_media_body(locale, media)
            plex_urls = self.__overseerr.get_plex_url(media.overseerr_id, media.type)
            contents.append(f"<li>{body} | <a href='{plex_urls.web}'>Plex web</a><a href='{plex_urls.ios}'>Plex iOS</a></li>")

        header = self.__get_header(locale)
        merged_content = "\n".join(contents)
        return f'{header}\n<ul>\n{merged_content}\n</ul>'

    @staticmethod
    def __get_subject(locale: str) -> str:
        if locale.lower() == "fr":
            return "Plex: Media en attente"
        return "Plex: Pending media"

    @staticmethod
    def __get_header(locale: str) -> str:
        if locale.lower() == "fr":
            return "Liste des médias en attente de visionnage sur Plex:"
        return "Medias waiting to be watched on Plex:"

    @staticmethod
    def __get_media_body(locale: str, media: Media) -> str:
        if locale.lower() == 'fr':
            media_type = 'Film' if media.type == MediaType.MOVIE else 'Série'
            season = f' - Season {media.season_number}' if media.season_number else ''
        else:
            media_type = 'Movie' if media.type == MediaType.MOVIE else 'Series'
            season = f' - Saison {media.season_number}' if media.season_number else ''

        return f'{media_type}: {media.name}{season}'
