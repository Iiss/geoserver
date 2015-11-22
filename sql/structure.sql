-- MySQL dump 10.13  Distrib 5.6.24, for Win64 (x86_64)
--
-- Host: localhost    Database: geo
-- ------------------------------------------------------
-- Server version	5.6.26-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `layers`
--

DROP TABLE IF EXISTS `layers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `layers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(45) DEFAULT NULL,
  `scan_type_id` int(11) NOT NULL,
  PRIMARY KEY (`id`,`scan_type_id`),
  KEY `fk_layers_scan_types1_idx` (`scan_type_id`),
  CONSTRAINT `fk_layers_scan_types1` FOREIGN KEY (`scan_type_id`) REFERENCES `scan_types` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `map_data`
--

DROP TABLE IF EXISTS `map_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `map_data` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cell_x` int(11) DEFAULT NULL,
  `cell_y` int(11) DEFAULT NULL,
  `map_id` int(11) NOT NULL,
  `layer_id` int(11) NOT NULL,
  `value_id` int(11) NOT NULL,
  PRIMARY KEY (`id`,`map_id`,`layer_id`,`value_id`),
  KEY `fk_map_data_maps_idx` (`map_id`),
  KEY `fk_map_data_layers1_idx` (`layer_id`),
  KEY `fk_map_data_values1_idx` (`value_id`),
  CONSTRAINT `fk_map_data_layers1` FOREIGN KEY (`layer_id`) REFERENCES `layers` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_map_data_maps` FOREIGN KEY (`map_id`) REFERENCES `maps` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=9208 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `maps`
--

DROP TABLE IF EXISTS `maps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `maps` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `width` int(11) DEFAULT '32',
  `height` int(11) DEFAULT '18',
  `is_epic` tinyint(1) DEFAULT '0',
  `title` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `scan_data`
--

DROP TABLE IF EXISTS `scan_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `scan_data` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cell_x` int(11) DEFAULT NULL,
  `cell_y` int(11) DEFAULT NULL,
  `value` int(11) DEFAULT NULL,
  `layer_id` int(11) NOT NULL,
  PRIMARY KEY (`id`,`layer_id`),
  KEY `fk_scan_data_layers1_idx` (`layer_id`),
  CONSTRAINT `fk_scan_data_layers1` FOREIGN KEY (`layer_id`) REFERENCES `layers` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `scan_limits`
--

DROP TABLE IF EXISTS `scan_limits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `scan_limits` (
  `map_id` int(11) NOT NULL,
  `scan_type_id` int(11) NOT NULL,
  PRIMARY KEY (`map_id`,`scan_type_id`),
  KEY `fk_scan_limits_scan_types1_idx` (`scan_type_id`),
  CONSTRAINT `fk_scan_limits_maps1` FOREIGN KEY (`map_id`) REFERENCES `maps` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_scan_limits_scan_types1` FOREIGN KEY (`scan_type_id`) REFERENCES `scan_types` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `scan_requests`
--

DROP TABLE IF EXISTS `scan_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `scan_requests` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cell_x` int(11) DEFAULT NULL,
  `cell_y` int(11) DEFAULT NULL,
  `layer_id` int(11) NOT NULL,
  PRIMARY KEY (`id`,`layer_id`),
  KEY `fk_scan_requests_layers1_idx` (`layer_id`),
  CONSTRAINT `fk_scan_requests_layers1` FOREIGN KEY (`layer_id`) REFERENCES `layers` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `scan_types`
--

DROP TABLE IF EXISTS `scan_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `scan_types` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sessions`
--

DROP TABLE IF EXISTS `sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sessions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `map_id` int(11) NOT NULL,
  PRIMARY KEY (`id`,`map_id`),
  KEY `fk_sessions_maps1_idx` (`map_id`),
  CONSTRAINT `fk_sessions_maps1` FOREIGN KEY (`map_id`) REFERENCES `maps` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `storage`
--

DROP TABLE IF EXISTS `storage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `storage` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `kern_id` int(11) DEFAULT NULL,
  `cell_x` int(11) DEFAULT NULL,
  `cell_y` int(11) DEFAULT NULL,
  `rock_key` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-11-22 18:54:31
