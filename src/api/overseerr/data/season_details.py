import datetime
from dataclasses import dataclass
from typing import Optional


@dataclass
class SeasonDetails:
    episode_count: Optional[int] = None
    last_air_date: Optional[datetime.datetime] = None
    tvdb_id: Optional[int] = None
