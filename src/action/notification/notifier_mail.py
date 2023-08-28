import logging
from typing import Optional, Callable

from action.notification.common_notifier import CommonNotifier
from action.status.user_group_status import UserGroupStatus
from api.mail.mailer import Mailer
from api.overseerr.overseerr_helper import OverseerrHelper
from database.media import Media
from database.user_group import UserGroup


class MailNotifier(CommonNotifier):
    def __init__(self, mailer: Mailer, overseerr: OverseerrHelper):
        self.__mailer = mailer
        self.__overseerr = overseerr
        self.__logger = logging.getLogger(__name__)

    def notify_watchlist(self, user_group: UserGroup, medias: list[Media], user_group_status: UserGroupStatus):
        return self.__notify(user_group, medias, user_group_status, self._get_subject_watchlist)

    def notify_requirement_added(self, user_group: UserGroup, medias: list[Media]):
        return self.__notify(user_group, medias, None, self._get_subject_requirement_added)

    def __notify(self, user_group: UserGroup, medias: list[Media], user_group_status: Optional[UserGroupStatus], subject_function: Callable[[str], str]):
        mails = user_group.notification_value.split(',') if user_group.notification_value else []

        locale = user_group.locale
        subject = subject_function(locale)
        text_message = self.__get_text_body(locale, medias, user_group_status)
        html_message = self.__get_html_body(locale, medias, user_group_status)
        self.__mailer.send(mails, subject, text_message, html_message)
        self.__logger.info("Mail sent")

    def __get_text_body(self, locale: str, medias: list[Media], user_group_status: Optional[UserGroupStatus]) -> str:
        media_list = "\n".join([f"* {self._get_media_body(locale, media, user_group_status.get(media) if user_group_status else None)}" for media in medias])
        return f'{self._get_header_watchlist(locale)}\n{media_list}'

    def __get_html_body(self, locale: str, medias: list[Media], user_group_status: Optional[UserGroupStatus]) -> str:
        contents = []
        for media in medias:
            user_media_status = user_group_status.get(media) if user_group_status else None
            content_parts = [self._get_media_body(locale, media, user_media_status)]

            media_details = self.__overseerr.get_media_details(media.overseerr_id, media.type)
            if media_details.overseerr_url:
                content_parts.append(f"<a href='{media_details.overseerr_url}'><img style='max-height: 15px;' src='https://plexdeleter.ds920.rakambda.fr/static/overseerr.png'/></a>")
            if media_details.plex_web_url:
                content_parts.append(f"<a href='{media_details.plex_web_url}'><img style='max-height: 15px;' src='https://plexdeleter.ds920.rakambda.fr/static/plex.png'/></a>")

            contents.append(f"<li>{' | '.join(content_parts)}</li>")

        header = self._get_header_watchlist(locale)
        merged_content = "\n".join(contents)
        return f'{header}\n<ul>\n{merged_content}\n</ul>'
