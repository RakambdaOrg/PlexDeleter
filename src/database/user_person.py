from dataclasses import dataclass


@dataclass
class UserPerson:
    id: int
    name: str
    plex_id: int

    def __str__(self):
        return f"User person '{self.name}' [id: {self.id}]"
