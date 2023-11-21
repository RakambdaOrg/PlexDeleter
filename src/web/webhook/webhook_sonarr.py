import logging

from flask import Response, request

from database.database import Database


class WebhookSonarr:
    def __init__(self, database: Database):
        self.__logger = logging.getLogger(__name__)
        self.__database = database

    def on_call(self) -> Response:
        payload = request.json
        self.__logger.info(f"Received Sonarr webhook call with payload {payload}")

        payload_type = payload["eventType"]
        if payload_type != 'Grab':
            return Response(status=204)

        if 'series' not in payload:
            return Response(status=204)
        series = payload["series"]

        if 'episodes' in series:
            return Response(status=204)
        episodes = series["episodes"]
        title = series["title"]
        tvdb_id = series["tvdbId"]

        for episode in episodes:
            season_number = episode["seasonNumber"]
            episode_number = episode["episodeNumber"]
            if tvdb_id and season_number and episode_number:
                self.__logger.info(f"Setting episode count to {episode_number} for show {title} (Season {season_number}) of tvdb {tvdb_id}")
                self.__database.media_tvdb_id_set_episode(tvdb_id, season_number, episode_number)

        return Response(status=200)
