CREATE SCHEMA `geo` ;

CREATE TABLE `geo`.`sessions` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '',
  `title` VARCHAR(128) NULL COMMENT '',
  PRIMARY KEY (`id`)  COMMENT '');
  
CREATE TABLE `geo`.`scan_types` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '',
  `title` VARCHAR(64) NULL COMMENT '',
  PRIMARY KEY (`id`)  COMMENT '');
  
CREATE TABLE `geo`.`scan_requests` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '',
  `cell_x` INT NULL COMMENT '',
  `cell_y` INT NULL COMMENT '',
  `scan_type_id` INT NOT NULL COMMENT '',
  PRIMARY KEY (`id`)  COMMENT '');

CREATE TABLE `geo`.`scan_data` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '',
  `cell_x` INT NULL COMMENT '',
  `cell_y` INT NULL COMMENT '',
  `scan_type_id` INT NOT NULL COMMENT '',
  `value` DECIMAL(10,6) NULL COMMENT '',
  PRIMARY KEY (`id`)  COMMENT '');session_data
  
CREATE TABLE `geo`.`map_data` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '',
  `cell_x` INT NULL COMMENT '',
  `cell_y` INT NULL COMMENT '',
  `scan_type_id` INT NOT NULL COMMENT '',
  `session_id` INT NULL COMMENT '',
  `value` DECIMAL(10,6) NULL DEFAULT 0 COMMENT '',
  PRIMARY KEY (`id`)  COMMENT '');
  