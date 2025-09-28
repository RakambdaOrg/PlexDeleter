ALTER TABLE `media`
    ADD COLUMN `plex_guid` varchar(64) after `root_plex_id`;