from action.notification.common_notifier import CommonNotifier
from action.notification.types.NotifyType import NotifyType
from action.status.user_group_status import UserGroupStatus
from database.media import Media
from database.user_group import UserGroup
from typing import Optional


class NoneNotifier(CommonNotifier):
    def notify(self, user_group: UserGroup, medias: list[Media], user_group_status: Optional[UserGroupStatus], notify_type: NotifyType):
        pass
