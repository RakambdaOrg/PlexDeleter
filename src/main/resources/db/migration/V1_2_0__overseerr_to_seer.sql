ALTER TABLE `media` RENAME COLUMN `overseerr_id` TO `seerr_id`;
ALTER TABLE `user_person` RENAME COLUMN `overseerr_id` TO `seerr_id`;

UPDATE `authorities`
SET `authority` = 'ROLE_SEERR'
WHERE `authority` = 'ROLE_OVERSEERR';