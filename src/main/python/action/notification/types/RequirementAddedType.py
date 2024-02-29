from action.notification.types.NotifyType import NotifyType


class RequirementAddedType(NotifyType):
    def get_discord_header(self, locale: str) -> str:
        if locale.lower() == "fr":
            return "Nouveau média à regarder ajouté car vous l'avez demandé ou avez regardé la saison précédente :"
        return "New media to watch added because you requested it or watched previous season:"

    def get_discord_header_releasing(self, locale: str) -> str:
        return self.get_discord_header(locale)

    def get_subject(self, locale: str) -> str:
        if locale.lower() == "fr":
            return "Plex : Media ajouté a votre liste de lecture"
        return "Plex: Media added to your watch list"
