from action.notification.types.NotifyType import NotifyType


class AbandonedType(NotifyType):
    def get_discord_header(self, locale: str) -> str:
        if locale.lower() == "fr":
            return "Nouveau média abandonné manuellement :"
        return "New media manually abandoned:"

    def get_discord_header_releasing(self, locale: str) -> str:
        return self.get_discord_header(locale)

    def get_subject(self, locale: str) -> str:
        if locale.lower() == "fr":
            return "Plex : Media abandonné manuellement"
        return "Plex: Media manually abandoned"
