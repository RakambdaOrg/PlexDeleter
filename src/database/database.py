import array
import datetime
import logging
from typing import TypeVar, Callable, Optional

import pymysql
from dbutils.persistent_db import PersistentDB
from mariadb import Cursor

from database.auth import Auth
from database.media import Media
from database.media_action_status import MediaActionStatus
from database.media_requirement_status import MediaRequirementStatus
from database.media_status import MediaStatus
from database.media_type import MediaType
from database.notification_type import NotificationType
from database.user_group import UserGroup
from database.user_person import UserPerson

T = TypeVar('T')


class Database:
    def __init__(self, host: str, user: str, password: str, database: str):
        self.__logger = logging.getLogger(__name__)
        self.__persist_database = PersistentDB(creator=pymysql, host=host, port=3306, user=user, password=password, database=database, cursorclass=pymysql.cursors.DictCursor)

        self.__user_person_mapper = lambda row: UserPerson(self.__get_row_value(row, 'UP', 'Id'),
                                                           self.__get_row_value(row, 'UP', 'Name'),
                                                           self.__get_row_value(row, 'UP', 'PlexId'))
        self.__user_group_mapper = lambda row: UserGroup(self.__get_row_value(row, 'UG', 'Id'),
                                                         self.__get_row_value(row, 'UG', 'Name'),
                                                         NotificationType(self.__get_row_value(row, 'UG', 'NotificationType')),
                                                         self.__get_row_value(row, 'UG', 'NotificationValue'),
                                                         self.__get_row_value(row, 'UG', 'Locale'),
                                                         self.__get_row_value(row, 'UG', 'LastNotification'),
                                                         self.__get_row_value(row, 'UG', 'Display'),
                                                         self.__get_row_value(row, 'UG', 'ServarrTag'))
        self.__media_mapper = lambda row: Media(self.__get_row_value(row, 'M', 'Id'),
                                                self.__get_row_value(row, 'M', 'OverseerrId'),
                                                self.__get_row_value(row, 'M', 'TvdbId'),
                                                self.__get_row_value(row, 'M', 'Name'),
                                                self.__get_row_value(row, 'M', 'Season'),
                                                self.__get_row_value(row, 'M', 'ElementCount'),
                                                MediaType(self.__get_row_value(row, 'M', 'Type')),
                                                MediaStatus(self.__get_row_value(row, 'M', 'Status')),
                                                MediaActionStatus(self.__get_row_value(row, 'M', 'ActionStatus')))
        self.__auth_mapper = lambda row: Auth(self.__get_row_value(row, 'A', 'Type'),
                                              self.__get_row_value(row, 'A', 'Username'))

    def user_person_get_all_in_group(self, group_id: int) -> list[UserPerson]:
        return self.__select("SELECT UP.Id, UP.Name, UP.PlexId "
                             "FROM UserPerson UP "
                             "INNER JOIN UserMapping UM ON UP.Id = UM.PersonId "
                             "WHERE UM.GroupId=%(group_id)s",
                             self.__user_person_mapper,
                             {'group_id': group_id})

    def user_person_get_all(self) -> list[UserPerson]:
        return self.__select("SELECT UP.Id, UP.Name, UP.PlexId "
                             "FROM UserPerson UP "
                             "INNER JOIN UserMapping UM ON UP.Id = UM.PersonId",
                             self.__user_person_mapper)

    def user_group_get_all(self) -> list[UserGroup]:
        return self.__select("SELECT UG.Id, UG.Name, UG.NotificationType, UG.NotificationValue, UG.Locale, UG.LastNotification, UG.Display, UG.ServarrTag FROM UserGroup UG",
                             self.__user_group_mapper)

    def user_group_set_last_notified(self, group_id: int, date: datetime.datetime) -> None:
        self.__execute_and_commit("UPDATE UserGroup SET LastNotification=%(date)s WHERE Id=%(group_id)s",
                                  {'date': date, 'group_id': group_id})

    def user_group_get_with_plex_id(self, plex_user_id: int) -> list[UserGroup]:
        return self.__select("SELECT UG.Id, UG.Name, UG.NotificationType, UG.NotificationValue, UG.Locale, UG.LastNotification, UG.Display, UG.ServarrTag "
                             "FROM UserGroup UG "
                             "INNER JOIN UserMapping UM ON UG.Id = UM.GroupId "
                             "INNER JOIN UserPerson UP ON UM.PersonId = UP.Id "
                             "WHERE UP.PlexId=%(plex_user_id)s",
                             self.__user_group_mapper,
                             {'plex_user_id': plex_user_id})

    def user_group_get_watching(self, overseerr_id: int, season: int, media_type: MediaType) -> list[UserGroup]:
        return self.__select("SELECT UG.Id, UG.Name, UG.NotificationType, UG.NotificationValue, UG.Locale, UG.LastNotification, UG.Display, UG.ServarrTag "
                             "FROM UserGroup UG "
                             "INNER JOIN MediaRequirement MR ON UG.Id = MR.GroupId "
                             "INNER JOIN Media M ON MR.MediaId = M.Id "
                             "WHERE M.OverseerrId=%(overseerr_id)s AND M.Season=%(season)s AND M.Type=%(media_type)s AND MR.Status <> %(status)s",
                             self.__user_group_mapper,
                             {'overseerr_id': overseerr_id, 'season': season, 'media_type': media_type.value, 'status': MediaRequirementStatus.ABANDONED.value})

    def user_group_get_watching_media(self, media_id: int) -> list[UserGroup]:
        return self.__select("SELECT UG.Id, UG.Name, UG.NotificationType, UG.NotificationValue, UG.Locale, UG.LastNotification, UG.Display, UG.ServarrTag "
                             "FROM UserGroup UG "
                             "INNER JOIN MediaRequirement MR ON UG.Id = MR.GroupId "
                             "WHERE MR.MediaId=%(media_id)s AND MR.Status <> %(status)s",
                             self.__user_group_mapper,
                             {'media_id': media_id, 'status': MediaRequirementStatus.ABANDONED.value})

    def media_get_all_releasing(self) -> list[Media]:
        return self.__select("SELECT M.Id, M.OverseerrId, M.TvdbId, M.Name, M.Season, M.ElementCount, M.Type, M.Status, M.ActionStatus FROM Media M "
                             "WHERE Status=%(media_status)s",
                             self.__media_mapper,
                             {'media_status': MediaStatus.RELEASING.value})

    def media_get_fully_watched_to_delete(self) -> list[Media]:
        return self.__select("SELECT M.Id, M.OverseerrId, M.TvdbId, M.Name, M.Season, M.ElementCount, M.Type, M.Status, M.ActionStatus, MIN(IF(MR.Status IN ('WATCHED', 'ABANDONED'), 1, 0)) AS GroupWatched "
                             "FROM MediaRequirement MR "
                             "INNER JOIN Media M ON MR.MediaId = M.Id "
                             "WHERE M.ActionStatus=%(media_action_status)s AND M.Status=%(media_status)s "
                             "GROUP BY MediaId "
                             "HAVING GroupWatched > 0",
                             self.__media_mapper,
                             {'media_action_status': MediaActionStatus.TO_DELETE.value, 'media_status': MediaStatus.FINISHED.value})

    def media_get_waiting_for_user_group(self, group_id: int) -> list[Media]:
        return self.__select("SELECT M.Id, M.OverseerrId, M.TvdbId, M.Name, M.Season, M.ElementCount, M.Type, M.Status, M.ActionStatus FROM MediaRequirement MR "
                             "INNER JOIN Media M on MR.MediaId = M.Id "
                             "WHERE MR.GroupId=%(group_id)s AND MR.Status=%(media_requirement_status)s "
                             "ORDER BY M.Name, M.Season",
                             self.__media_mapper,
                             {'group_id': group_id, 'media_requirement_status': MediaRequirementStatus.WAITING.value})

    def media_get_waiting_with_groups(self) -> list[tuple[UserGroup, Media]]:
        return self.__select("SELECT M.Id, M.OverseerrId, M.TvdbId, M.Name, M.Season, M.Type, M.ElementCount, M.Status, M.ActionStatus, "
                             "UG.Id, UG.Name, UG.NotificationType, UG.NotificationValue, UG.Locale, UG.LastNotification, UG.Display, UG.ServarrTag "
                             "FROM MediaRequirement MR "
                             "INNER JOIN UserGroup UG on MR.GroupId = UG.Id "
                             "INNER JOIN Media M on MR.MediaId = M.Id "
                             "WHERE MR.Status=%(media_requirement_status)s "
                             "ORDER BY M.Name, M.Season",
                             lambda r: (self.__user_group_mapper(r), self.__media_mapper(r)),
                             {'media_requirement_status': MediaRequirementStatus.WAITING.value})

    def media_get_ready_to_delete(self) -> list[Media]:
        return self.__select("SELECT M.Id, M.OverseerrId, M.TvdbId, M.Name, M.Season, M.ElementCount, M.Type, M.Status, M.ActionStatus "
                             "FROM Media M "
                             "WHERE M.ActionStatus = 'TO_DELETE'"
                             "AND NOT EXISTS (SELECT MR.MediaId FROM MediaRequirement MR WHERE MR.MediaId = M.ID AND MR.Status=%(media_requirement_status)s)"
                             "ORDER BY M.Name, M.Season",
                             self.__media_mapper,
                             {'media_requirement_status': MediaRequirementStatus.WAITING.value})

    def media_set_status(self, media_id: int, status: MediaStatus) -> None:
        self.__execute_and_commit("UPDATE Media SET Status=%(status)s WHERE Id=%(id)s",
                                  {'status': status.value, 'id': media_id})

    def media_set_action_status(self, media_id: int, action_status: MediaActionStatus) -> None:
        self.__execute_and_commit("UPDATE Media SET ActionStatus=%(action_status)s WHERE Id=%(id)s",
                                  {'action_status': action_status.value, 'id': media_id})

    def media_set_element_count(self, media_id: int, element_count: int):
        self.__execute_and_commit("UPDATE Media SET ElementCount=%(element_count)s WHERE Id=%(id)s",
                                  {'element_count': element_count, 'id': media_id})

    def media_set_tvdb_id(self, media_id: int, tvdb_id: int):
        self.__execute_and_commit("UPDATE Media SET TvdbId=%(tvdb_id)s WHERE Id=%(id)s",
                                  {'tvdb_id': tvdb_id, 'id': media_id})

    def media_tvdb_id_set_episode(self, tvdb_id: int, season_number: int, episode_number: int):
        self.__execute_and_commit("UPDATE Media SET ElementCount=MAX(COALESCE(ElementCount, 0), %(episode)s) WHERE TvdbId=%(tvdb_id)s AND Season=%(season)s",
                                  {'tvdb_id': tvdb_id, 'season': season_number, 'episode': episode_number})

    def media_get_by_overseerr_id(self, overseerr_id: int, season: Optional[int], media_type: MediaType) -> list[Media]:
        if not season:
            return self.__select("SELECT M.Id, M.OverseerrId, M.TvdbId, M.Name, M.Season, M.ElementCount, M.Type, M.Status, M.ActionStatus FROM Media M "
                                 "WHERE OverseerrId=%(id)s AND M.Type=%(media_type)s AND Season IS NULL",
                                 self.__media_mapper,
                                 {'id': overseerr_id, 'media_type': media_type.value})
        return self.__select("SELECT M.Id, M.OverseerrId, M.TvdbId, M.Name, M.Season, M.ElementCount, M.Type, M.Status, M.ActionStatus FROM Media M "
                             "WHERE OverseerrId=%(id)s AND M.Type=%(media_type)s AND Season=%(season)s",
                             self.__media_mapper,
                             {'id': overseerr_id, 'media_type': media_type.value, 'season': season})

    def media_add(self, overseerr_id: int, name: str, season: Optional[int], media_type: MediaType, status: MediaStatus, action_status: MediaActionStatus) -> None:
        self.__execute_and_commit("INSERT INTO Media(OverseerrId, Name, Season, Type, Status, ActionStatus) VALUES (%(overseerr_id)s,%(name)s,%(season)s,%(type)s,%(status)s,%(action_status)s)",
                                  {'overseerr_id': overseerr_id, 'name': name, 'season': season, 'type': media_type.value, 'status': status.value, 'action_status': action_status.value})

    def media_requirement_set_status(self, media_id: int, group_id: int, media_requirement_status: MediaRequirementStatus) -> None:
        self.__execute_and_commit("UPDATE MediaRequirement SET Status=%(status)s "
                                  "WHERE MediaId=%(media_id)s AND GroupId=%(group_id)s",
                                  {'status': media_requirement_status.value, 'media_id': media_id, 'group_id': group_id})

    def media_requirement_add(self, media_id: int, user_group_id: int):
        self.__execute_and_commit("INSERT INTO MediaRequirement(MediaId, GroupId) VALUES(%(media_id)s,%(user_group_id)s) ON DUPLICATE KEY UPDATE MediaId=%(media_id)s",
                                  {'media_id': media_id, 'user_group_id': user_group_id})

    def get_auth(self, auth_type: str, username: Optional[str], password: str) -> Optional[Auth]:
        if auth_type == 'BEARER':
            values = self.__select("SELECT A.Type, A.Username FROM Auth A "
                                   "WHERE A.Type=%(auth_type)s AND A.Password=%(password)s",
                                   self.__auth_mapper,
                                   {'auth_type': auth_type, 'password': password})
        else:
            values = self.__select("SELECT A.Type, A.Username FROM Auth A "
                                   "WHERE A.Type=%(auth_type)s AND A.Username=%(username)s AND A.Password=%(password)s",
                                   self.__auth_mapper,
                                   {'auth_type': auth_type, 'username': username, 'password': password})

        if values and len(values) > 0:
            return values[0]
        return None

    def __execute_and_commit(self, query: str, args=None) -> None:
        cursor = self.__execute(query, args)
        cursor.connection.commit()

    def __execute(self, query: str, args=None) -> Cursor:
        if args is None:
            args = []

        conn = self.__persist_database.connection()
        cursor = conn.cursor()
        cursor.execute(query, args)
        return cursor

    def __select(self, query: str, parser: Callable[[array], T], args: dict = None) -> list[T]:
        values = []
        cursor = self.__execute(query, args)
        for row in cursor:
            values.append(parser(row))
        return values

    @staticmethod
    def __get_row_value(row: array, alias: str, field: str) -> any:
        aliased_field = f"{alias}.{field}"
        if aliased_field in row:
            return row[aliased_field]

        if field in row:
            return row[field]

        raise KeyError(f"Alias '{alias}', field '{field}'")
