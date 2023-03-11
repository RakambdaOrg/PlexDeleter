import datetime
from typing import Optional, Tuple

import mariadb


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

    def get_all_group_ids(self) -> list[int]:
        ids = []
        self.__cursor.execute("SELECT Id FROM UserGroup")
        for row in self.__cursor:
            ids.append(int(row[0]))
        return ids

    def get_plex_ids_in_group(self, group_id: int) -> list[int]:
        ids = []
        self.__cursor.execute("SELECT PlexId FROM UserPerson UP INNER JOIN UserMapping UM ON UP.Id = UM.PersonId WHERE UM.GroupId=?", [group_id])
        for row in self.__cursor:
            ids.append(int(row[0]))
        return ids

    def get_waiting_media_ids_for_group(self, group_id: int) -> list[int]:
        ids = []
        self.__cursor.execute("SELECT MediaId FROM MediaRequirement WHERE GroupId=? AND Status='WAITING'", [group_id])
        for row in self.__cursor:
            ids.append(int(row[0]))
        return ids

    def get_waiting_media_info_for_group(self, group_id) -> list[Tuple[str, str, int]]:
        info = []
        self.__cursor.execute("SELECT M.Type, M.Name, M.PlexId FROM MediaRequirement MR INNER JOIN Media M on MR.MediaId = M.Id WHERE MR.GroupId=? AND MR.Status='WAITING'", [group_id])
        for row in self.__cursor:
            info.append((row[0], row[1], row[2]))
        return info

    def get_plex_id_for_finished_media(self, media_id: int) -> Optional[int]:
        plex_id = None
        self.__cursor.execute("SELECT PlexId FROM Media WHERE Id=? AND Status = 'FINISHED'", [media_id])
        for row in self.__cursor:
            plex_id = int(row[0])
        return plex_id

    def mark_watched(self, media_id, group_id) -> None:
        self.__cursor.execute("UPDATE MediaRequirement SET Status='WATCHED' WHERE MediaId=? AND GroupId=?", [media_id, group_id])
        self.__conn.commit()

    def get_fully_watched(self) -> list[int]:
        ids = []
        self.__cursor.execute("SELECT MediaId, MIN(IF(Status = 'WATCHED', 1, 0)) AS GroupWatched FROM MediaRequirement GROUP BY MediaId HAVING GroupWatched > 0")
        for row in self.__cursor:
            ids.append(int(row[0]))
        return ids

    def get_group_info(self, group_id) -> Tuple[str, list[str]]:
        info = None
        self.__cursor.execute("SELECT Locale, Mail FROM UserGroup WHERE Id=?", [group_id])
        for row in self.__cursor:
            mails = str(row[1])
            mail_list = mails.split(',') if mails else []
            info = (str(row[0]), mail_list)
        return info

    def get_last_notification(self, group_id: int) -> datetime.datetime:
        notification = datetime.datetime(1970, 1, 1)
        self.__cursor.execute("SELECT LastNotification FROM UserGroup WHERE Id=?", [group_id])
        for row in self.__cursor:
            notification = row[0]
        return notification

    def set_last_notified(self, group_id: int, date: datetime.datetime) -> None:
        self.__cursor.execute("UPDATE UserGroup SET LastNotification=? WHERE Id=?", [date, group_id])
        self.__conn.commit()

    def get_all_releasing_show(self) -> list[Tuple[int, int, int]]:
        info = []
        self.__cursor.execute("SELECT Id, PlexId, OverseerrId FROM Media WHERE Status='RELEASING' AND Type = 'SHOW' AND OverseerrId IS NOT NULL", [])
        for row in self.__cursor:
            info.append((int(row[0]), int(row[1]), int(row[2])))
        return info

    def set_finished(self, media_id: int) -> None:
        self.__cursor.execute("UPDATE Media SET Status='FINISHED' WHERE Id=?", [media_id])
        self.__conn.commit()
