import datetime
import logging
from typing import Tuple

from database import Database
from notify.mailer import Mailer


class Notifier:
    def __init__(self, database: Database, mailer: Mailer, server_id: str):
        self.__database = database
        self.__mailer = mailer
        self.__server_id = server_id
        self.__logger = logging.getLogger(__name__)

    @staticmethod
    def __get_subject(locale: str) -> str:
        if locale.lower() == 'fr':
            return 'Plex: Media en attente'
        return 'Plex: Pending media'

    @staticmethod
    def __get_header(locale: str) -> str:
        if locale.lower() == 'fr':
            return 'Liste des médias en attente de visionnage sur Plex:'
        return 'Medias waiting to be watched on Plex:'

    def __get_media_link(self, media_id: int):
        return f'https://app.plex.tv/desktop/#!/server/{self.__server_id}/details?key=%2Flibrary%2Fmetadata%2F{media_id}'

    @staticmethod
    def __get_media_body(locale: str, media_info: Tuple[str, str, int]) -> str:
        if locale.lower() == 'fr':
            media_type = 'Film' if media_info[0] == 'MOVIE' else 'Série'
        else:
            media_type = 'Movie' if media_info[0] == 'MOVIE' else 'Series'
        return f'{media_type}: {media_info[1]}'

    def __get_text_body(self, locale: str, media_info: list[Tuple[str, str, int]]) -> str:
        media_list = '\n'.join([f'* {self.__get_media_body(locale, media_piece_info)}' for media_piece_info in media_info])
        return f'{self.__get_header(locale)}\n{media_list}'

    def __get_html_body(self, locale: str, media_info: list[Tuple[str, str, int]]) -> str:
        media_list = '\n'.join([f'<li><a href="{self.__get_media_link(media_piece_info[2])}">{self.__get_media_body(locale, media_piece_info)}</a></li>' for media_piece_info in media_info])
        return f'{self.__get_header(locale)}\n<ul>\n{media_list}\n</ul>'

    def __notify_group(self, group_id: int) -> None:
        self.__logger.info(f'Notifying group {group_id}')
        last_notified = self.__database.get_last_notification(group_id)
        if datetime.datetime.now() - last_notified < datetime.timedelta(days=6, hours=23):
            self.__logger.debug(f'Too early to notify {group_id}')
            return

        group_info = self.__database.get_group_info(group_id)
        media_info = self.__database.get_waiting_media_info_for_group(group_id)

        if len(media_info) <= 0:
            self.__logger.debug(f'Nothing to notify {group_id}')
        else:
            subject = self.__get_subject(group_info[0])
            text_message = self.__get_text_body(group_info[0], media_info)
            html_message = self.__get_html_body(group_info[0], media_info)
            self.__mailer.send_mail(group_info[1], subject, text_message, html_message)

        self.__database.set_last_notified(group_id, datetime.datetime.now())

    def notify_all_unwatched(self) -> None:
        self.__logger.info('Notifying all groups')
        group_ids = self.__database.get_all_group_ids()
        for group_id in group_ids:
            self.__notify_group(group_id)
