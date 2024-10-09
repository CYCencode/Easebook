-- MySQL dump 10.13  Distrib 9.0.1, for macos14.4 (arm64)
--
-- Host: localhost    Database: socialapp
-- ------------------------------------------------------
-- Server version	9.0.1

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
-- Table structure for table `chat_room`
--

DROP TABLE IF EXISTS `chat_room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_room` (
  `chat_room_id` char(64) NOT NULL,
  `user_1` char(36) NOT NULL,
  `user_2` char(36) NOT NULL,
  `create_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`chat_room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_room`
--

LOCK TABLES `chat_room` WRITE;
/*!40000 ALTER TABLE `chat_room` DISABLE KEYS */;
/*!40000 ALTER TABLE `chat_room` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `friend_request`
--

DROP TABLE IF EXISTS `friend_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `friend_request` (
  `id` char(36) NOT NULL,
  `sender_id` char(36) NOT NULL,
  `sender_name` varchar(20) NOT NULL,
  `receiver_id` char(36) NOT NULL,
  `receiver_name` varchar(20) NOT NULL,
  `status` int NOT NULL,
  `create_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sender_receiver_createat` (`sender_id`,`receiver_id`,`create_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `friend_request`
--

LOCK TABLES `friend_request` WRITE;
/*!40000 ALTER TABLE `friend_request` DISABLE KEYS */;
/*!40000 ALTER TABLE `friend_request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `friendship`
--

DROP TABLE IF EXISTS `friendship`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `friendship` (
  `user_1` char(36) NOT NULL,
  `user_2` char(36) NOT NULL,
  `create_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_1`,`user_2`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `friendship`
--

LOCK TABLES `friendship` WRITE;
/*!40000 ALTER TABLE `friendship` DISABLE KEYS */;
/*!40000 ALTER TABLE `friendship` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_post`
--

DROP TABLE IF EXISTS `user_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_post` (
  `user_id` char(36) NOT NULL,
  `post_id` char(36) NOT NULL,
  `create_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_post`
--

LOCK TABLES `user_post` WRITE;
/*!40000 ALTER TABLE `user_post` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_story`
--

DROP TABLE IF EXISTS `user_story`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_story` (
  `user_id` char(36) NOT NULL,
  `story_id` char(36) NOT NULL,
  `create_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`story_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_story`
--

LOCK TABLES `user_story` WRITE;
/*!40000 ALTER TABLE `user_story` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_story` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` char(36) NOT NULL,
  `email` varchar(50) NOT NULL,
  `password` varchar(80) NOT NULL,
  `name` varchar(20) NOT NULL,
  `create_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('151c6cc2-32ac-41fc-a8cb-fce8d1026d44','eve@gmail.com','$2a$10$o0NwSAi1M.NMK.eLYQ5owuS1ePEw5odKL33mEUffucmUwwHvjecNC','張育菁','2024-10-07 05:32:17'),('35321fa1-b86a-4530-abf3-055bc8be226b','una@gmail.com','$2a$10$VuvmbYkF4VjxE8ej9ZO0/e947hYq1bp4MXVGsli6bkyVl/J2P0bzq','吳娜','2024-10-07 05:45:53'),('359444b6-c6a9-43ea-a51a-b69d162fd4e9','lion@gmail.com','$2a$10$H37yb56My76FvtjAAU4G8ub5OkDY0VGDqXkGmLEPGswn3nFwWOmAC','王賴恩','2024-10-07 05:46:45'),('3704b6d9-ec03-48d5-91a1-ddf148f89b13','wayne@gmail.com','$2a$10$iemWvNF613wbqfswUpmJpuwL6FhJ9L7SeY/Lr4ql1oavkODDLGOKW','李維恩','2024-10-07 05:48:33'),('8225f1a7-3ac5-4160-8103-7026c7213d78','reng@gmail.com','$2a$10$rJd9d7l8lpEDUFHQ2yYdo.jKh9x9fopLN0DINFbMV0KU8p21c90zC','黃崇任','2024-10-07 05:44:32'),('b6e4f859-9089-49b2-86c5-666d0be14f74','wuchane@gmail.com','$2a$10$wQl6EJBC4ktwO5AT6E4TjeUjRyfL3kHcG7SwOzzx/3gOt.d72qelq','吳成遠','2024-10-07 05:40:11'),('e58bae21-4e3a-4475-aaef-c9a39b385c5c','money@gmail.com','$2a$10$vwVABCaf1iXWDnlOVQ4EjuR6Zy22rTwMfRhvP/dQiRjixRFEDJkwW','林嘉欣','2024-10-07 05:39:17');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-10-07 14:00:57
