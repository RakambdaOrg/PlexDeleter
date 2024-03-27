ALTER TABLE `user_person`
    ADD COLUMN `overseerr_id` int(3) after `plex_id`;

UPDATE `user_person`
SET `overseerr_id`= RAND() * 100;

ALTER TABLE `user_person`
    MODIFY COLUMN `overseerr_id` int(3) not null;
ALTER TABLE `user_person`
    ADD UNIQUE (`overseerr_id`);
