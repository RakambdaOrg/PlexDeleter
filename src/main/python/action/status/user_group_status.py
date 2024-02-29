from action.status.user_media_status import UserMediaStatus
from database.media import Media
from dataclasses import dataclass, field
from typing import Optional


@dataclass
class UserGroupStatus:
    medias: dict[Media, UserMediaStatus] = field(default_factory=lambda: {})

    def add(self, media: Media, status: UserMediaStatus) -> None:
        self.medias[media] = status

    def get(self, media: Media) -> Optional[UserMediaStatus]:
        return self.medias[media] if media in self.medias else None
