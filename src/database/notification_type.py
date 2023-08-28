from enum import Enum


class NotificationType(Enum):
    MAIL = 'MAIL'
    DISCORD = 'DISCORD'
    DISCORD_THREAD = 'DISCORD_THREAD'
    NONE = 'NONE'
