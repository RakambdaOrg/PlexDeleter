ALTER TABLE `media`
    ADD COLUMN `last_added_time` timestamp after `status`;

UPDATE `media`
SET `last_added_time` = STR_TO_DATE('2000-01-01 00:00:00', '%Y-%m-%d %H:%i:%s')
WHERE `last_added_time` IS NULL;

ALTER TABLE `media`
    MODIFY COLUMN `last_added_time` timestamp after `status`;