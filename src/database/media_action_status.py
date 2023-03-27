from enum import Enum


class MediaActionStatus(Enum):
    TO_DELETE = 'TO_DELETE'
    DELETED = 'DELETED'
    KEEP = 'KEEP'
