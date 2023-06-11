import array
import datetime
import logging
from typing import TypeVar, Callable, Optional

import mariadb
from mariadb import Cursor, InterfaceError

from database.media_action_status import MediaActionStatus
from database.media_requirement_status import MediaRequirementStatus
from database.media_status import MediaStatus
from database.media import Media
from database.media_type import MediaType
from database.notification_type import NotificationType
from database.user_group import UserGroup
from database.user_person import UserPerson

T = TypeVar('T')


class Database:
    def __init__(self, host: str, user: str, password: str, database: str):
        self.__host = host,
        self.__port = 3306,
        self.__user = user,
        self.__password = password,
        self.__database = database

        self.__conn = None
        self.__logger = logging.getLogger(__name__)

    def __enter__(self):
        self.__connect()
        return self

    def __connect(self):
        self.__conn = mariadb.connect(
            host=self.__host,
            port=self.__port,
            user=self.__user,
            password=self.__password,
            database=self.__database
        )

    def __exit__(self, exc_type, exc_val, exc_tb):
        if self.__conn:
            self.__conn.close()

    def user_person_get_all_in_group(self, group_id: int) -> list[UserPerson]:
        return self.__select("SELECT UP.Id, UP.Name, UP.PlexId FROM UserPerson UP INNER JOIN UserMapping UM ON UP.Id = UM.PersonId WHERE UM.GroupId=?",
                             lambda row: UserPerson(row[0], row[1], row[2]),
                             [group_id])

    def user_group_get_all(self) -> list[UserGroup]:
        return self.__select("SELECT Id, Name, NotificationType, NotificationValue, Locale, LastNotification FROM UserGroup",
                             lambda row: UserGroup(row[0], row[1], NotificationType(row[2]), row[3], row[4], row[5]))

    def user_group_set_last_notified(self, group_id: int, date: datetime.datetime) -> None:
        self.__execute_and_commit("UPDATE UserGroup SET LastNotification=? WHERE Id=?",
                                  [date, group_id])

    def user_group_get_with_plex_id(self, plex_user_id: int) -> list[UserGroup]:
        return self.__select("SELECT UG.Id, UG.Name, UG.NotificationType, UG.NotificationValue, UG.Locale, UG.LastNotification FROM UserGroup UG INNER JOIN UserMapping UM ON UG.Id = UM.GroupId INNER JOIN UserPerson UP ON UM.PersonId = UP.Id WHERE UP.PlexId=?",
                             lambda row: UserGroup(row[0], row[1], NotificationType(row[2]), row[3], row[4], row[5]),
                             [plex_user_id])

    def media_get_all_releasing(self) -> list[Media]:
        return self.__select("SELECT Id, OverseerrId, Name, Season, Type, Status, ActionStatus FROM Media WHERE Status=?",
                             lambda row: Media(row[0], row[1], row[2], row[3], MediaType(row[4]), MediaStatus(row[5]), MediaActionStatus(row[6])),
                             [MediaStatus.RELEASING.value])

    def media_get_waiting_for_group(self, group_id: int) -> list[Media]:
        return self.__select("SELECT M.Id, M.OverseerrId, M.Name, M.Season, M.Type, M.Status, M.ActionStatus FROM MediaRequirement MR INNER JOIN Media M ON MR.MediaId = M.Id WHERE MR.GroupId=? AND MR.Status=?",
                             lambda row: Media(row[0], row[1], row[2], row[3], MediaType(row[4]), MediaStatus(row[5]), MediaActionStatus(row[6])),
                             [group_id, MediaRequirementStatus.WAITING.value])

    def media_get_fully_watched_to_delete(self) -> list[Media]:
        return self.__select("SELECT M.Id, M.OverseerrId, M.Name, M.Season, M.Type, M.Status, M.ActionStatus, MIN(IF(MR.Status IN ('WATCHED', 'ABANDONED'), 1, 0)) AS GroupWatched FROM MediaRequirement MR INNER JOIN Media M ON MR.MediaId = M.Id WHERE M.ActionStatus=? AND M.Status=? GROUP BY MediaId HAVING GroupWatched > 0",
                             lambda row: Media(row[0], row[1], row[2], row[3], MediaType(row[4]), MediaStatus(row[5]), MediaActionStatus(row[6])),
                             [MediaActionStatus.TO_DELETE.value, MediaStatus.FINISHED.value])

    def media_get_waiting_for_user_group(self, group_id: int) -> list[Media]:
        return self.__select("SELECT  M.Id, M.OverseerrId, M.Name, M.Season, M.Type, M.Status, M.ActionStatus FROM MediaRequirement MR INNER JOIN Media M on MR.MediaId = M.Id WHERE MR.GroupId=? AND MR.Status=?",
                             lambda row: Media(row[0], row[1], row[2], row[3], MediaType(row[4]), MediaStatus(row[5]), MediaActionStatus(row[6])),
                             [group_id, MediaRequirementStatus.WAITING.value])

    def media_set_finished(self, media_id: int) -> None:
        self.__execute_and_commit("UPDATE Media SET Status=? WHERE Id=?",
                                  [MediaStatus.FINISHED.value, media_id])

    def media_set_deleted(self, media_id: int) -> None:
        self.__execute_and_commit("UPDATE Media SET ActionStatus=? WHERE Id=?",
                                  [MediaActionStatus.DELETED.value, media_id])

    def media_get_by_overseerr_id(self, overseerr_id: int, season: Optional[int]) -> list[Media]:
        if not season:
            return self.__select("SELECT  Id, OverseerrId, Name, Season, Type, Status, ActionStatus FROM Media WHERE OverseerrId=? AND Season IS NULL",
                                 lambda row: Media(row[0], row[1], row[2], row[3], MediaType(row[4]), MediaStatus(row[5]), MediaActionStatus(row[6])),
                                 [overseerr_id])
        return self.__select("SELECT  Id, OverseerrId, Name, Season, Type, Status, ActionStatus FROM Media WHERE OverseerrId=? AND Season=?",
                             lambda row: Media(row[0], row[1], row[2], row[3], MediaType(row[4]), MediaStatus(row[5]), MediaActionStatus(row[6])),
                             [overseerr_id, season])

    def media_add(self, overseerr_id: int, name: str, season: Optional[int], type: MediaType, status: MediaStatus, action_status: MediaActionStatus) -> None:
        self.__execute_and_commit("INSERT INTO Media(OverseerrId, Name, Season, Type, Status, ActionStatus) VALUES (?,?,?,?,?,?)",
                                  [overseerr_id, name, season, type.value, status.value, action_status.value])

    def media_requirement_set_watched(self, media_id: int, group_id: int) -> None:
        self.__execute_and_commit("UPDATE MediaRequirement SET Status=? WHERE MediaId=? AND GroupId=?",
                                  [MediaRequirementStatus.WATCHED.value, media_id, group_id])

    def media_requirement_add(self, media_id: int, user_group_id: int):
        self.__execute_and_commit("INSERT INTO MediaRequirement(MediaId, GroupId) VALUES(?,?) ON DUPLICATE KEY UPDATE MediaId=?",
                                  [media_id, user_group_id, media_id])

    def __get_cursor(self) -> Cursor:
        return self.__conn.cursor()

    def __execute_and_commit(self, query: str, args=None) -> None:
        self.__execute(query, args)
        self.__conn.commit()

    def __execute(self, query: str, args=None, retry=True) -> Cursor:
        if args is None:
            args = []

        try:
            cursor = self.__get_cursor()
            cursor.execute(query, args)
        except InterfaceError as e:
            self.__connect()
            if retry:
                self.__logger.warning(f"Failed to execute request, will retry : {query}. Exception was {str(e)}")
                return self.__execute(query, args, False)
            raise e

        return cursor

    def __select(self, query: str, parser: Callable[[array], T], args=None) -> list[T]:
        values = []
        cursor = self.__execute(query, args)
        for row in cursor:
            values.append(parser(row))
        return values
