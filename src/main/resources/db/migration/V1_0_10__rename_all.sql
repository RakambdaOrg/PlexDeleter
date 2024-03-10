ALTER TABLE `authorities` RENAME COLUMN `Username` TO `username`;
ALTER TABLE `authorities` RENAME COLUMN `Authority` TO `authority`;

ALTER TABLE `Media` RENAME COLUMN `Id` TO `id`;
ALTER TABLE `Media` RENAME COLUMN `Type` TO `type`;
ALTER TABLE `Media` RENAME COLUMN `PlexId` TO `plex_id`;
ALTER TABLE `Media` RENAME COLUMN `OverseerrId` TO `overseerr_id`;
ALTER TABLE `Media` RENAME COLUMN `ServarrId` TO `servarr_id`;
ALTER TABLE `Media` RENAME COLUMN `TvdbId` TO `tvdb_id`;
ALTER TABLE `Media` RENAME COLUMN `Name` TO `name`;
ALTER TABLE `Media` RENAME COLUMN `MediaIndex` TO `media_index`;
ALTER TABLE `Media` RENAME COLUMN `PartsCount` TO `parts_count`;
ALTER TABLE `Media` RENAME COLUMN `AvailablePartsCount` TO `available_parts_count`;
ALTER TABLE `Media` RENAME COLUMN `Availability` TO `availability`;
ALTER TABLE `Media` RENAME COLUMN `ActionStatus` TO `action_status`;
RENAME TABLE `Media` TO `media`;

ALTER TABLE `MediaRequirement` RENAME COLUMN `MediaId` TO `media_id`;
ALTER TABLE `MediaRequirement` RENAME COLUMN `GroupId` TO `group_id`;
ALTER TABLE `MediaRequirement` RENAME COLUMN `Status` TO `status`;
RENAME TABLE `MediaRequirement` TO `media_requirement`;

ALTER TABLE `UserGroup` RENAME COLUMN `Id` TO `id`;
ALTER TABLE `UserGroup` RENAME COLUMN `Name` TO `name`;
ALTER TABLE `UserGroup` RENAME COLUMN `NotificationType` TO `notification_type`;
ALTER TABLE `UserGroup` RENAME COLUMN `NotificationValue` TO `notification_value`;
ALTER TABLE `UserGroup` RENAME COLUMN `Locale` TO `locale`;
ALTER TABLE `UserGroup` RENAME COLUMN `LastNotification` TO `last_notification`;
ALTER TABLE `UserGroup` RENAME COLUMN `Display` TO `display`;
ALTER TABLE `UserGroup` RENAME COLUMN `ServarrTag` TO `servarr_tag`;
RENAME TABLE `UserGroup` TO `user_group`;

ALTER TABLE `UserPerson` RENAME COLUMN `Id` TO `id`;
ALTER TABLE `UserPerson` RENAME COLUMN `Name` TO `name`;
ALTER TABLE `UserPerson` RENAME COLUMN `PlexId` TO `plex_id`;
ALTER TABLE `UserPerson` RENAME COLUMN `GroupId` TO `group_id`;
RENAME TABLE `UserPerson` TO `user_person`;

ALTER TABLE `users` RENAME COLUMN `Username` TO `username`;
ALTER TABLE `users` RENAME COLUMN `Password` TO `password`;
ALTER TABLE `users` RENAME COLUMN `Enabled` TO `enabled`;