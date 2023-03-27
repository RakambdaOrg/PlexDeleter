from dataclasses import dataclass

from database.media_action_status import MediaActionStatus
from database.media_status import MediaStatus
from database.media_type import MediaType


@dataclass
class Media:
    id: int
    overseerr_id: int
    name: str
    season_number: int
    type: MediaType
    status: MediaStatus
    action_status: MediaActionStatus

    def __str__(self):
        return f"{self.type} '{self.name}' (Season {self.season_number}) [overseerr: {self.overseerr_id}]"
