-- MySQL dump 10.13  Distrib 8.4.3, for Win64 (x86_64)
--
-- Host: localhost    Database: hotel_db
-- ------------------------------------------------------
-- Server version	8.4.3

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `guests`
--

DROP TABLE IF EXISTS `guests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `guests` (
  `guest_id` int NOT NULL AUTO_INCREMENT,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `id_number` varchar(30) NOT NULL,
  `loyalty_points` int NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`guest_id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `guests`
--

LOCK TABLES `guests` WRITE;
/*!40000 ALTER TABLE `guests` DISABLE KEYS */;
INSERT INTO `guests` VALUES (1,'Nimal Perera','nimal.perera@example.com','+94771234501','902345678V',30,'2026-11-21 17:45:52'),(2,'Kasun Fernando','kasun.fernando@example.com','+94771234502','881234567V',60,'2026-11-21 17:45:52'),(3,'Ayesha Khan','ayesha.khan@example.com','+94779999999','199512345678',20,'2026-11-21 17:45:52'),(4,'Dilini Jayasuriya','dilini.j@example.com','+94771234504','925678901V',30,'2026-11-21 17:45:52'),(5,'Tharindu Silva','tharindu.silva@example.com','+94771234505','199823456789',30,'2026-11-21 17:45:52'),(6,'Naduni Wijesinghe','naduni.w@example.com','+94771234506','N1234567',20,'2026-11-21 17:45:52');
/*!40000 ALTER TABLE `guests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservations`
--

DROP TABLE IF EXISTS `reservations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservations` (
  `reservation_id` int NOT NULL AUTO_INCREMENT,
  `guest_id` int NOT NULL,
  `room_id` int NOT NULL,
  `check_in_date` date NOT NULL,
  `check_out_date` date NOT NULL,
  `num_guests` int NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'CONFIRMED',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cancelled_at` datetime DEFAULT NULL,
  PRIMARY KEY (`reservation_id`),
  KEY `guest_id` (`guest_id`),
  KEY `idx_res_room_dates` (`room_id`,`check_in_date`,`check_out_date`),
  CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`guest_id`) REFERENCES `guests` (`guest_id`),
  CONSTRAINT `reservations_ibfk_2` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`room_id`),
  CONSTRAINT `reservations_chk_1` CHECK ((`check_out_date` > `check_in_date`))
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservations`
--

LOCK TABLES `reservations` WRITE;
/*!40000 ALTER TABLE `reservations` DISABLE KEYS */;
INSERT INTO `reservations` VALUES (1,1,1,'2026-12-20','2026-12-23',1,25500.00,'CONFIRMED','2026-11-21 17:47:01',NULL),(2,2,2,'2026-12-24','2026-12-28',2,48000.00,'CONFIRMED','2026-11-21 17:47:01',NULL),(3,3,6,'2026-12-18','2026-12-20',2,20000.00,'CONFIRMED','2026-11-21 17:47:01',NULL),(4,4,8,'2026-11-01','2026-11-04',3,63000.00,'COMPLETED','2026-11-21 17:47:01',NULL),(5,5,9,'2026-12-30','2027-01-02',1,22500.00,'CONFIRMED','2026-11-21 17:47:01',NULL),(6,1,4,'2026-12-10','2026-12-12',2,50000.00,'CANCELLED','2026-11-21 17:47:01','2026-11-05 10:30:00'),(7,2,10,'2026-11-15','2026-11-17',2,22000.00,'CHECKED_IN','2026-11-21 17:47:01',NULL),(8,6,1,'2026-12-23','2026-12-25',1,17000.00,'CONFIRMED','2026-11-21 17:47:01',NULL),(9,3,7,'2027-01-10','2027-01-14',2,42000.00,'CANCELLED','2026-11-22 17:50:54','2026-11-22 17:52:22');
/*!40000 ALTER TABLE `reservations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rooms`
--

DROP TABLE IF EXISTS `rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rooms` (
  `room_id` int NOT NULL AUTO_INCREMENT,
  `branch_code` varchar(10) NOT NULL,
  `room_number` varchar(10) NOT NULL,
  `room_type` varchar(20) NOT NULL,
  `capacity` int NOT NULL,
  `price_per_night` decimal(10,2) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'active',
  PRIMARY KEY (`room_id`),
  UNIQUE KEY `branch_code` (`branch_code`,`room_number`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rooms`
--

LOCK TABLES `rooms` WRITE;
/*!40000 ALTER TABLE `rooms` DISABLE KEYS */;
INSERT INTO `rooms` VALUES (1,'COL','101','SINGLE',1,8500.00,'ACTIVE'),(2,'COL','102','DOUBLE',2,12000.00,'ACTIVE'),(3,'COL','201','DOUBLE',2,12500.00,'ACTIVE'),(4,'COL','301','SUITE',4,25000.00,'ACTIVE'),(5,'KDY','101','SINGLE',1,7500.00,'ACTIVE'),(6,'KDY','102','DOUBLE',2,10000.00,'ACTIVE'),(7,'KDY','201','DOUBLE',2,10500.00,'ACTIVE'),(8,'KDY','301','SUITE',4,21000.00,'ACTIVE'),(9,'GAL','101','SINGLE',1,7500.00,'ACTIVE'),(10,'GAL','102','DOUBLE',2,11000.00,'ACTIVE'),(11,'GAL','201','DOUBLE',2,11500.00,'MAINTENANCE'),(12,'GAL','301','SUITE',4,23000.00,'ACTIVE'),(16,'TST','D029676','SINGLE',1,5000.00,'ACTIVE'),(18,'KDY','401','DOUBLE',2,10500.00,'ACTIVE');
/*!40000 ALTER TABLE `rooms` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-26 12:42:23
