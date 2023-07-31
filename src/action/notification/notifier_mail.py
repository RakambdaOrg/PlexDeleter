import logging

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

    def notify(self, user_group: UserGroup, medias: list[Media], user_group_status: UserGroupStatus):
        mails = user_group.notification_value.split(',') if user_group.notification_value else []

        locale = user_group.locale
        subject = self.get_subject(locale)
        text_message = self.__get_text_body(locale, medias, user_group_status)
        html_message = self.__get_html_body(locale, medias, user_group_status)
        self.__mailer.send(mails, subject, text_message, html_message)
        self.__logger.info("Mail sent")

    def __get_text_body(self, locale: str, medias: list[Media], user_group_status: UserGroupStatus) -> str:
        media_list = "\n".join([f"* {self._get_media_body(locale, media, user_group_status.get(media))}" for media in medias])
        return f'{self._get_header(locale)}\n{media_list}'

    def __get_html_body(self, locale: str, medias: list[Media], user_group_status: UserGroupStatus) -> str:
        contents = []
        for media in medias:
            content_parts = [self._get_media_body(locale, media, user_group_status.get(media))]

            plex_urls = self.__overseerr.get_media_details(media.overseerr_id, media.type)
            if plex_urls.overseerr_url:
                content_parts.append(f"<a href='{plex_urls.overseerr_url}'><img style='max-height: 15px;' src='https://plexdeleter.ds920.rakambda.fr/static/overseerr.png'/></a>")
            if plex_urls.plex_web_url:
                content_parts.append(f"<a href='{plex_urls.plex_web_url}'><img style='max-height: 15px;' src='https://plexdeleter.ds920.rakambda.fr/static/plex.png'/></a>")

            contents.append(f"<li>{' | '.join(content_parts)}</li>")

        header = self._get_header(locale)
        merged_content = "\n".join(contents)
        return f'{header}\n<ul>\n{merged_content}\n</ul>'
