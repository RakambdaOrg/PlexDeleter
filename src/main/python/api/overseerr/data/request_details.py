from dataclasses import dataclass, field
from typing import Optional


@dataclass
class RequestDetails:
    requester_id: Optional[int] = None
    tags: list[int] = field(default_factory=lambda: [])
