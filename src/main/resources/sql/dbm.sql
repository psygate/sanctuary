-- SCRIPT INFORMATION --
-- Types: mysql mariadb
-- Version: 1
-- Upgrades: 0
-- SCRIPT INFORMATION --

START TRANSACTION;
SET foreign_key_checks = 0;

DROP TABLE IF EXISTS sanctuary_payed_items;
DROP TABLE IF EXISTS sanctuary_reinforced CASCADE;
DROP TABLE IF EXISTS sanctuary_anchors;
DROP TABLE IF EXISTS sanctuary_placement_times;
DROP TABLE IF EXISTS sanctuary_player_settings;

CREATE TABLE sanctuary_anchors (
  sanctuary_id   BIGINT      NOT NULL                        AUTO_INCREMENT,
  creation_time  TIMESTAMP   NOT NULL,
  creator        BINARY(16)  NOT NULL,
  group_id       BIGINT      NOT NULL,
  health         BIGINT      NOT NULL,
  max_health     BIGINT      NOT NULL,
  x              INTEGER     NOT NULL,
  y              INTEGER     NOT NULL,
  z              INTEGER     NOT NULL,
  world_uuid     BINARY(16)  NOT NULL,
  sanctuary_name VARCHAR(32) NOT NULL,
  PRIMARY KEY (sanctuary_id),
  UNIQUE KEY (x, y, z, world_uuid),
  FOREIGN KEY (creator) REFERENCES nucleus_usernames (puuid)
    ON UPDATE CASCADE,
  FOREIGN KEY (group_id) REFERENCES ivory_groups (group_id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
);

CREATE TABLE sanctuary_reinforced (
  sanctuary_id BIGINT     NOT NULL,
  x            INTEGER    NOT NULL,
  y            INTEGER    NOT NULL,
  z            INTEGER    NOT NULL,
  world_uuid   BINARY(16) NOT NULL,
  FOREIGN KEY (sanctuary_id) REFERENCES sanctuary_anchors (sanctuary_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  PRIMARY KEY (x, y, z, world_uuid)
);

CREATE TABLE sanctuary_payed_items (
  item_id       BIGINT  NOT NULL                        AUTO_INCREMENT,
  sanctuary_id  BIGINT  NOT NULL,
  material_type INTEGER NOT NULL,
  meta_data     INTEGER NOT NULL,
  amount        INTEGER NOT NULL,
  FOREIGN KEY (sanctuary_id) REFERENCES sanctuary_anchors (sanctuary_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  PRIMARY KEY (item_id)
);

CREATE TABLE sanctuary_placement_times (
  creator  BINARY(16) NOT NULL,
  creation TIMESTAMP  NOT NULL,
  PRIMARY KEY (creator, creation)
);

CREATE TABLE sanctuary_player_settings (
  puuid                  BINARY(16) NOT NULL,
  enter_exit_notify_bool BOOLEAN    NOT NULL,
  bossbar_bool           BOOLEAN    NOT NULL,
  scoreboard_bool        BOOLEAN    NOT NULL,
  PRIMARY KEY (puuid)
);

CREATE INDEX sanctuary_reinforced_idx ON sanctuary_reinforced (x, y, z, world_uuid);

SET foreign_key_checks = 1;
COMMIT;