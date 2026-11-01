-- Fix MySQL 1442: a trigger cannot UPDATE the same table as its invoking statement.
-- The application serializes address changes with the owning t_profiles row lock,
-- clears old defaults explicitly, then inserts/updates the selected address.
-- These validation-only triggers reject a conflicting default; they never edit another row.
-- No historical address or business balance is modified by this migration.
DROP TRIGGER IF EXISTS trg_t_addresses_default_ins;
DROP TRIGGER IF EXISTS trg_t_addresses_default_upd;
DELIMITER //
CREATE TRIGGER trg_t_addresses_default_ins
BEFORE INSERT ON t_addresses
FOR EACH ROW
BEGIN
 IF NEW.is_default=1 AND EXISTS(SELECT 1 FROM t_addresses a WHERE a.user_id=NEW.user_id AND a.is_default=1) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Default address already exists; use the serialized address service';
 END IF;
END//
CREATE TRIGGER trg_t_addresses_default_upd
BEFORE UPDATE ON t_addresses
FOR EACH ROW
BEGIN
 IF NEW.is_default=1 AND EXISTS(SELECT 1 FROM t_addresses a WHERE a.user_id=NEW.user_id AND a.is_default=1 AND a.id<>OLD.id) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Default address already exists; use the serialized address service';
 END IF;
END//
DELIMITER ;
