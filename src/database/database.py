import array
import datetime
from typing import TypeVar, Callable

import mariadb

from media_action_status import MediaActionStatus
from media_requirement_status import MediaRequirementStatus
from media_status import MediaStatus
from media import Media
from user_group import UserGroup
from user_person import UserPerson

T = TypeVar('T')


class Database:
    def __init__(self, host: str, user: str, password: str, database: str):
        self.__conn = mariadb.connect(
            host=host,
            port=3306,
            user=user,
            password=password,
            database=database
        )

    def __enter__(self):
        self.__cursor = self.__conn.cursor()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        if self.__conn:
            self.__conn.close()

    def user_person_get_all_in_group(self, group_id: int) -> list[UserPerson]:
        return self.__select("SELECT UP.Id, UP.Name, UP.PlexId FROM UserPerson UP INNER JOIN UserMapping UM ON UP.Id = UM.PersonId WHERE UM.GroupId=?",
                             lambda row: UserPerson(row[0], row[1], row[2]),
                             [group_id])

    def user_group_get_all(self) -> list[UserGroup]:
        return self.__select("SELECT Id, Name, Mail, Locale, LastNotification FROM UserGroup",
                             lambda row: UserGroup(row[0], row[1], row[2], row[3], row[4]))

    def user_group_set_last_notified(self, group_id: int, date: datetime.datetime) -> None:
        self.__cursor.execute("UPDATE UserGroup SET LastNotification=? WHERE Id=?", [date, group_id])
        self.__conn.commit()

    def media_get_all_releasing(self) -> list[Media]:
        return self.__select("SELECT Id, OverseerrId, Name, Season, Type, Status, ActionStatus FROM Media WHERE Status=? AND OverseerrId IS NOT NULL",
                             lambda row: Media(row[0], row[1], row[2], row[3], row[4], row[5], row[6]),
                             [MediaStatus.RELEASING])

    def media_get_waiting_for_group(self, group_id: int) -> list[Media]:
        return self.__select("SELECT M.Id, M.OverseerrId, M.Name, M.Season, M.Type, M.Status, M.ActionStatus FROM MediaRequirement MR INNER JOIN Media M ON MR.MediaId = M.Id WHERE MR.GroupId=? AND MR.Status=?",
                             lambda row: Media(row[0], row[1], row[2], row[3], row[4], row[5], row[6]),
                             [group_id, MediaRequirementStatus.WAITING])

    def media_get_fully_watched_to_delete(self) -> list[Media]:
        return self.__select("SELECT M.Id, M.OverseerrId, M.Name, M.Season, M.Type, M.Status, M.ActionStatus, MIN(IF(MR.Status = 'WATCHED', 1, 0)) AS GroupWatched FROM MediaRequirement MR INNER JOIN Media M ON MR.MediaId = M.Id WHERE M.ActionStatus=? AND M.Status=? GROUP BY MediaId HAVING GroupWatched > 0",
                             lambda row: Media(row[0], row[1], row[2], row[3], row[4], row[5], row[6]),
                             [MediaActionStatus.TO_DELETE, MediaStatus.FINISHED])

    def media_get_waiting_for_user_group(self, group_id) -> list[Media]:
        return self.__select("SELECT  M.Id, M.OverseerrId, M.Name, M.Season, M.Type, M.Status, M.ActionStatus FROM MediaRequirement MR INNER JOIN Media M on MR.MediaId = M.Id WHERE MR.GroupId=? AND MR.Status=?",
                             lambda row: Media(row[0], row[1], row[2], row[3], row[4], row[5], row[6]),
                             [group_id, MediaRequirementStatus.WAITING])

    def media_set_finished(self, media_id: int) -> None:
        self.__cursor.execute("UPDATE Media SET Status=? WHERE Id=?", [MediaStatus.FINISHED, media_id])
        self.__conn.commit()

    def media_requirement_set_watched(self, media_id: int, group_id: int) -> None:
        self.__cursor.execute("UPDATE MediaRequirement SET Status=? WHERE MediaId=? AND GroupId=?", [MediaRequirementStatus.WATCHED, media_id, group_id])
        self.__conn.commit()

    def __select(self, query: str, parser: Callable[[array], T], args=None) -> list[T]:
        if args is None:
            args = []

        values = []
        self.__cursor.execute(query, args)
        for row in self.__cursor:
            values.append(parser(row))
        return values
