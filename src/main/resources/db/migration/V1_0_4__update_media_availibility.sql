UPDATE `Media`
SET `Availability` = 'DOWNLOADING'
WHERE `Availability` = 'DOWNLOADED'
  AND `ActionStatus` = 'TO_DELETE';