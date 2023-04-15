import datetime
import logging

from action.notification.notifier_discord import DiscordNotifier
from action.notification.notifier_mail import MailNotifier
from action.status.user_group_status import UserGroupStatus
from database.database import Database
from database.notification_type import NotificationType
from database.user_group import UserGroup


class Notifier:
    def __init__(self, database: Database, mail_notifier: MailNotifier, discord_notifier: DiscordNotifier):
        self.__database = database
        self.__mail_notifier = mail_notifier
        self.__discord_notifier = discord_notifier
        self.__logger = logging.getLogger(__name__)

    def notify(self, user_group_statuses: dict[UserGroup, UserGroupStatus] = None) -> None:
        if not user_group_statuses:
            user_group_statuses = {}

        self.__logger.info("Notifying all groups")
        user_groups = self.__database.user_group_get_all()
        for user_group in user_groups:
            user_group_status = user_group_statuses[user_group] if user_group in user_group_statuses else UserGroupStatus()
            self.__notify_group(user_group, user_group_status)

    def __notify_group(self, user_group: UserGroup, user_group_status: UserGroupStatus) -> None:
        self.__logger.info(f"Notifying {user_group}")
        if datetime.datetime.now() - user_group.last_notification < datetime.timedelta(days=6, hours=23):
            self.__logger.debug("Too early to notify group")
            return

        medias = self.__database.media_get_waiting_for_user_group(user_group.id)
        if len(medias) <= 0:
            self.__logger.debug("Nothing to notify")
        else:
            if user_group.notification_type == NotificationType.MAIL:
                self.__mail_notifier.notify(user_group, medias, user_group_status)
            elif user_group.notification_type == NotificationType.DISCORD:
                self.__discord_notifier.notify(user_group, medias, user_group_status)

        self.__database.user_group_set_last_notified(user_group.id, datetime.datetime.now())
