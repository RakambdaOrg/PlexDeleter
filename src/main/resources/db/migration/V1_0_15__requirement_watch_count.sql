ALTER TABLE `media_requirement`
    ADD COLUMN `watched_count` int not null default 0 AFTER `status`;