from enum import Enum


class MediaStatus(Enum):
    FINISHED = 'FINISHED'
    RELEASING = 'RELEASING'
    MANUAL = 'MANUAL'

    def get_for_display(self, locale: str) -> str:
        if self == MediaStatus.RELEASING:
            if locale == "fr":
                return "Diffusion"
            return "Releasing"
        if self == MediaStatus.MANUAL:
            if locale == "fr":
                return "Manuel"
            return "Manual"
