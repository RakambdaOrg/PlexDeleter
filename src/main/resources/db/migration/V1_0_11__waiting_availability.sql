ALTER TABLE `media`
    MODIFY COLUMN `availability` ENUM ('MANUAL', 'WAITING', 'DOWNLOADED', 'DOWNLOADING') NOT NULL;

UPDATE `media`
SET `availability` = 'WAITING'
WHERE `availability` = 'DOWNLOADING';