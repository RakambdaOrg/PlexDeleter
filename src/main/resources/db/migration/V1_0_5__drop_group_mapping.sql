ALTER TABLE `UserPerson`
    ADD COLUMN IF NOT EXISTS `GroupId` INT AFTER `PlexId`;

ALTER TABLE `UserPerson`
    ADD CONSTRAINT FK_UG_GroupId foreign key (`GroupId`) references UserGroup (`Id`)
        on update cascade
        on delete cascade;

UPDATE `UserPerson` UP
SET UP.`GroupId` = (SELECT UM.`GroupId`
                    FROM `UserMapping` UM
                    WHERE UM.`PersonId` = UP.`Id`)
WHERE UP.`GroupId` IS NULL;

DROP TABLE `UserMapping`;