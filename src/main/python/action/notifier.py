import datetime
import logging
from action.notification.common_notifier import CommonNotifier
from action.notification.none_mail import NoneNotifier
from action.notification.notifier_discord import DiscordNotifier
from action.notification.notifier_discord_thread import DiscordNotifierThread
from action.notification.notifier_mail import MailNotifier
from action.notification.types.AbandonedType import AbandonedType
from action.notification.types.CompletedType import CompletedType
from action.notification.types.DeletedType import DeletedType
from action.notification.types.WatchlistType import WatchlistType
from action.status.user_group_status import UserGroupStatus
from database.database import Database
from database.media import Media
from database.media_status import MediaStatus
from database.notification_type import NotificationType
from database.user_group import UserGroup
from functools import cmp_to_key


class Notifier:
    def __init__(self, database: Database, mail_notifier: MailNotifier, discord_notifier: DiscordNotifier, discord_notifier_thread: DiscordNotifierThread):
        self.__database = database
        self.__none_notifier = NoneNotifier()
        self.__mail_notifier = mail_notifier
        self.__discord_notifier = discord_notifier
        self.__discord_notifier_thread = discord_notifier_thread
        self.__logger = logging.getLogger(__name__)

    def notify_abandoned(self, media: Media, user_group_id: int) -> None:
        self.__logger.info(f"Notifying media {media} is abandoned by {user_group_id}")
        user_groups = self.__database.user_group_get_by_id(user_group_id)

        for user_group in user_groups:
            self.__get_notifier(user_group.notification_type).notify(user_group, [media], None, AbandonedType())

    def notify_completed(self, media: Media, user_group_id: int) -> None:
        self.__logger.info(f"Notifying media {media} is completed by {user_group_id}")
        user_groups = self.__database.user_group_get_by_id(user_group_id)

        for user_group in user_groups:
            self.__get_notifier(user_group.notification_type).notify(user_group, [media], None, CompletedType())

    def notify_deleted(self, medias: [Media], user_groups: [UserGroup]) -> None:
        self.__logger.info(f"Notifying medias {medias} are deleted")

        for user_group in user_groups:
            self.__get_notifier(user_group.notification_type).notify(user_group, medias, None, DeletedType())

    @staticmethod
    def __media_sorter_name_season(x: Media, y: Media) -> int:
        if x.overseerr_id == y.overseerr_id:
            if x.season_number <= y.season_number:
                return -1
            return 1

        if x.name <= y.name:
            return -1
        return 1

    def __media_sorter(self, x: Media, y: Media) -> int:
        if x.status == y.status:
            return self.__media_sorter_name_season(x, y)
        if x.status != MediaStatus.RELEASING:
            return -1
        return 1

    def __notify_group_watchlist(self, user_group: UserGroup, user_group_status: UserGroupStatus) -> None:
        self.__logger.info(f"Notifying {user_group} watchlist")
        if datetime.datetime.now() - user_group.last_notification < datetime.timedelta(days=6, hours=23):
            self.__logger.debug("Too early to notify group watchlist")
            return

        medias = self.__database.media_get_waiting_for_user_group(user_group.id)
        medias = sorted(medias, key=cmp_to_key(self.__media_sorter))
        if len(medias) <= 0:
            self.__logger.debug("No media in watchlist to notify")
        elif all(media.status == MediaStatus.RELEASING for media in medias):
            self.__logger.info("Not notifying watchlist, only got releasing")
        else:
            self.__get_notifier(user_group.notification_type).notify(user_group, medias, user_group_status, WatchlistType())

        self.__database.user_group_set_last_notified(user_group.id, datetime.datetime.now())

    def __get_notifier(self, notification_type: NotificationType) -> CommonNotifier:
        if notification_type == NotificationType.MAIL:
            return self.__mail_notifier
        elif notification_type == NotificationType.DISCORD:
            return self.__discord_notifier
        elif notification_type == NotificationType.DISCORD_THREAD:
            return self.__discord_notifier_thread
        elif notification_type == NotificationType.NONE:
            return self.__none_notifier
