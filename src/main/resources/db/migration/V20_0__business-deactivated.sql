ALTER TABLE T_BUSINESS ADD COLUMN deactivated DATETIME;
UPDATE T_BUSINESS SET deactivated=suspended;

ALTER TABLE T_BUSINESS DROP COLUMN suspended;
