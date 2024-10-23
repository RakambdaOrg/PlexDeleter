ALTER TABLE `media`
    DROP INDEX `IDX_UNIQUE_Show_id_season`,
    ADD UNIQUE `IDX_UNIQUE_Show_id_season` (`overseerr_id`, `type`, `media_index`, `sub_media_index`) USING BTREE;