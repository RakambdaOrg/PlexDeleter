from action.notification.types.NotifyType import NotifyType


class WatchlistType(NotifyType):
    def get_discord_header(self, locale: str) -> str:
        if locale.lower() == "fr":
            return "Liste des médias en attente de visionnage sur Plex :"
        return "Medias waiting to be watched on Plex:"

    def get_discord_header_releasing(self, locale: str) -> str:
        if locale.lower() == "fr":
            return "Medias en cours de téléchargement :"
        return "Medias waiting to be downloaded:"

    def get_subject(self, locale: str) -> str:
        if locale.lower() == "fr":
            return "Plex : Media en attente"
        return "Plex: Pending media"
