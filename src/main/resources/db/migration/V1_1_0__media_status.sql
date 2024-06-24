ALTER TABLE `media`
    MODIFY COLUMN `availability` ENUM ('MANUAL', 'WAITING', 'DOWNLOADED', 'DOWNLOADING', 'DOWNLOADED_NEED_METADATA', 'PENDING_DELETION', 'DELETED', 'KEEP') NOT NULL;

UPDATE `media`
SET `availability` = 'KEEP'
WHERE `action_status` = 'KEEP';

UPDATE `media`
SET `availability` = 'DELETED'
WHERE `action_status` = 'DELETED';

ALTER TABLE `media`
    RENAME COLUMN `availability` TO `status`;

