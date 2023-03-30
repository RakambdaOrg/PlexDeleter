from dataclasses import dataclass, field

from database.media_action_status import MediaActionStatus
from database.media_status import MediaStatus
from database.media_type import MediaType


@dataclass(unsafe_hash=True)
class Media:
    id: int = field(hash=True, compare=True)
    overseerr_id: int = field(hash=False, compare=False)
    name: str = field(hash=False, compare=False)
    season_number: int = field(hash=False, compare=False)
    type: MediaType = field(hash=False, compare=False)
    status: MediaStatus = field(hash=False, compare=False)
    action_status: MediaActionStatus = field(hash=False, compare=False)

    def __str__(self):
        return f"{self.type} '{self.name}' (Season {self.season_number}) [overseerr: {self.overseerr_id}]"
