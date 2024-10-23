ALTER TABLE `media`
    ADD COLUMN `sub_media_index` int(11) after `media_index`;
ALTER TABLE `media`
    MODIFY COLUMN `type` ENUM ('MOVIE', 'SEASON', 'EPISODE') NOT NULL AFTER `id`;