ALTER TABLE `media`
    ADD COLUMN `last_requested_time` timestamp after `status`;
