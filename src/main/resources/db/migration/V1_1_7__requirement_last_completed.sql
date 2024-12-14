ALTER TABLE `media_requirement`
    ADD COLUMN `last_completed_time` timestamp after `watched_count`;

UPDATE `media_requirement`
SET `last_completed_time` = NOW()
WHERE `status` IN ('WATCHED', 'ABANDONED');

ALTER TABLE `media`
    DROP COLUMN `last_added_time`;