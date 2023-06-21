from dataclasses import dataclass, field
from typing import Optional, Self

from api.tautulli.data.watch_status import WatchStatus


@dataclass
class RequestDetails:
    requester_id: Optional[int] = None
    tags: list[int] = field(default_factory=lambda: [])
