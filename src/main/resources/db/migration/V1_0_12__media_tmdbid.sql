ALTER TABLE `media`
    ADD COLUMN IF NOT EXISTS `tmdb_id` INT AFTER `tvdb_id`;
ALTER TABLE `media`
    ADD COLUMN IF NOT EXISTS `sonarr_slug` VARCHAR(32) AFTER `tmdb_id`;
ALTER TABLE `media`
    ADD COLUMN IF NOT EXISTS `radarr_slug` VARCHAR(32) AFTER `sonarr_slug`;