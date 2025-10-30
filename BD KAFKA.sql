CREATE DATABASE  IF NOT EXISTS `empujecomunitario` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `empujecomunitario`;
-- MySQL dump 10.13  Distrib 8.0.28, for Win64 (x86_64)
--
-- Host: localhost    Database: empujecomunitario
-- ------------------------------------------------------
-- Server version	8.0.28

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `categorias`
--

DROP TABLE IF EXISTS `categorias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categorias` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categorias`
--

LOCK TABLES `categorias` WRITE;
/*!40000 ALTER TABLE `categorias` DISABLE KEYS */;
INSERT INTO `categorias` VALUES (2,'ALIMENTOS'),(3,'JUGUETES'),(1,'ROPA'),(4,'UTILES_ESCOLARES');
/*!40000 ALTER TABLE `categorias` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `donaciones`
--

DROP TABLE IF EXISTS `donaciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `donaciones` (
  `id` int NOT NULL AUTO_INCREMENT,
  `categoria_id` int NOT NULL,
  `descripcion` varchar(100) DEFAULT NULL,
  `cantidad` int NOT NULL,
  `eliminado` tinyint(1) DEFAULT '0',
  `fecha_alta` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_alta` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `categoria_id` (`categoria_id`),
  KEY `usuario_alta` (`usuario_alta`),
  CONSTRAINT `donaciones_ibfk_1` FOREIGN KEY (`categoria_id`) REFERENCES `categorias` (`id`),
  CONSTRAINT `donaciones_ibfk_2` FOREIGN KEY (`usuario_alta`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `donaciones_chk_1` CHECK ((`cantidad` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Se agregan columnas en tabla `donaciones` para Auditoria de Modificacion
--
ALTER TABLE `donaciones`
  ADD COLUMN `fecha_modificacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `usuario_modificacion` int DEFAULT NULL,
  ADD CONSTRAINT `donaciones_ibfk_3` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuarios`(`id`);
--
-- Dumping data for table `donaciones`
--

LOCK TABLES `donaciones` WRITE;
/*!40000 ALTER TABLE `donaciones` DISABLE KEYS */;
INSERT INTO `donaciones` VALUES (1,1,'Pantalones y remeras',50,0,'2025-08-31 18:47:26',1,NULL,NULL),(2,2,'Arroz y fideos',100,0,'2025-08-31 18:47:26',2,NULL,NULL),(3,3,'Ositos de peluche',20,0,'2025-08-31 18:47:26',4,NULL,NULL);
/*!40000 ALTER TABLE `donaciones` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `evento_participantes`
--

DROP TABLE IF EXISTS `evento_participantes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `evento_participantes` (
  `evento_id` int NOT NULL,
  `usuario_id` int NOT NULL,
  PRIMARY KEY (`evento_id`,`usuario_id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `evento_participantes_ibfk_1` FOREIGN KEY (`evento_id`) REFERENCES `eventos` (`id`),
  CONSTRAINT `evento_participantes_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `evento_participantes`
--

LOCK TABLES `evento_participantes` WRITE;
/*!40000 ALTER TABLE `evento_participantes` DISABLE KEYS */;
INSERT INTO `evento_participantes` VALUES (2,2),(1,4),(2,4),(5,8),(5,9);
/*!40000 ALTER TABLE `evento_participantes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eventos`
--

DROP TABLE IF EXISTS `eventos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eventos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `descripcion` text,
  `fecha_hora` datetime NOT NULL,
  `creador_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `creador_id` (`creador_id`),
  CONSTRAINT `eventos_ibfk_1` FOREIGN KEY (`creador_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eventos`
--

LOCK TABLES `eventos` WRITE;
/*!40000 ALTER TABLE `eventos` DISABLE KEYS */;
INSERT INTO `eventos` VALUES (1,'Visita a la escuela Nº 99','Se organizarán juegos y repartirán útiles.','2025-09-15 10:00:00',3),(2,'Visita al hogar de ancianos','Se repartirán alimentos y ropa.','2025-09-20 15:00:00',1),(5,'UTILES','UTILES','2025-10-01 01:47:00',5);
/*!40000 ALTER TABLE `eventos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eventos_externos`
--

DROP TABLE IF EXISTS `eventos_externos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eventos_externos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` int NOT NULL,
  `evento_id` varchar(100) NOT NULL,
  `fecha_hora` datetime NOT NULL,
  `estado` varchar(20) NOT NULL DEFAULT 'VIGENTE',
  `payload_json` json DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_evento_org` (`org_id`,`evento_id`),
  KEY `idx_eventos_estado` (`estado`),
  KEY `idx_eventos_fechahora` (`fecha_hora`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eventos_externos`
--

LOCK TABLES `eventos_externos` WRITE;
/*!40000 ALTER TABLE `eventos_externos` DISABLE KEYS */;
/*!40000 ALTER TABLE `eventos_externos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mensajes_procesados`
--

DROP TABLE IF EXISTS `mensajes_procesados`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mensajes_procesados` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `topic` varchar(200) NOT NULL,
  `partition_no` int NOT NULL,
  `offset_no` bigint NOT NULL,
  `message_key` varchar(200) DEFAULT NULL,
  `processed_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_topic_part_offset` (`topic`,`partition_no`,`offset_no`),
  KEY `idx_topic_key` (`topic`,`message_key`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mensajes_procesados`
--

LOCK TABLES `mensajes_procesados` WRITE;
/*!40000 ALTER TABLE `mensajes_procesados` DISABLE KEYS */;
INSERT INTO `mensajes_procesados` VALUES (1,'solicitud-donaciones',0,13,'SOL-2025-4AE6F632','2025-09-21 02:19:22'),(2,'solicitud-donaciones',0,14,'SOL-2025-D1395E4F','2025-09-21 02:19:38'),(3,'solicitud-donaciones',0,15,'SOL-2025-79D82A3D','2025-09-21 02:20:31');
/*!40000 ALTER TABLE `mensajes_procesados` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (3,'COORDINADOR'),(1,'PRESIDENTE'),(2,'VOCAL'),(4,'VOLUNTARIO');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `solicitud_items`
--

DROP TABLE IF EXISTS `solicitud_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `solicitud_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `solicitud_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `categoria` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `descripcion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cantidad` int DEFAULT NULL,
  `unidad` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_items_solicitud` (`solicitud_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `solicitud_items`
--

LOCK TABLES `solicitud_items` WRITE;
/*!40000 ALTER TABLE `solicitud_items` DISABLE KEYS */;
INSERT INTO `solicitud_items` VALUES (2,'SOL-2025-D1395E4F','ALIMENTOS','pollos',1,'u'),(3,'SOL-2025-79D82A3D','UTILES_ESCOLARES','cajas de lapices blancos',1,'u');
/*!40000 ALTER TABLE `solicitud_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `solicitudes_externas`
--

DROP TABLE IF EXISTS `solicitudes_externas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `solicitudes_externas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` int DEFAULT NULL,
  `solicitud_id` varchar(100) NOT NULL,
  `fecha_hora` datetime DEFAULT NULL,
  `estado` varchar(20) NOT NULL DEFAULT 'VIGENTE',
  `payload_json` json DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_solicitud_org` (`org_id`,`solicitud_id`),
  KEY `idx_solicitudes_estado` (`estado`),
  KEY `idx_solicitudes_fechahora` (`fecha_hora`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `solicitudes_externas`
--

LOCK TABLES `solicitudes_externas` WRITE;
/*!40000 ALTER TABLE `solicitudes_externas` DISABLE KEYS */;
INSERT INTO `solicitudes_externas` VALUES (2,NULL,'SOL-2025-D1395E4F','2025-09-21 02:19:39','VIGENTE','{\"items\": [{\"unidad\": \"u\", \"cantidad\": 1, \"categoria\": \"ALIMENTOS\", \"descripcion\": \"pollos\"}], \"org_id\": null, \"fecha_hora\": null, \"solicitud_id\": \"SOL-2025-D1395E4F\", \"idempotency_key\": \"SOL-2025-D1395E4F\"}','2025-09-21 02:19:38'),(3,NULL,'SOL-2025-79D82A3D','2025-09-21 02:20:31','VIGENTE','{\"items\": [{\"unidad\": \"u\", \"cantidad\": 1, \"categoria\": \"UTILES_ESCOLARES\", \"descripcion\": \"cajas de lapices blancos\"}], \"org_id\": null, \"fecha_hora\": null, \"solicitud_id\": \"SOL-2025-79D82A3D\", \"idempotency_key\": \"SOL-2025-79D82A3D\"}','2025-09-21 02:20:31');
/*!40000 ALTER TABLE `solicitudes_externas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `apellido` varchar(50) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `rol_id` int NOT NULL,
  `activo` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  KEY `rol_id` (`rol_id`),
  CONSTRAINT `usuarios_ibfk_1` FOREIGN KEY (`rol_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'admin1','Carlos','Pérez','12345678','admin123','carlos@example.com',1,0),(2,'vocal1','Laura','García','87654321','vocal123','laura@example.com',2,0),(3,'coord1','María','Fernández','99999999','coord123','nuevo_correo@example.com',3,0),(4,'vol1','José','Ramírez','55667788','vol123','jose@example.com',4,0),(5,'MATIAS','matias','velez','1123345678','$2b$12$qhL/Ww/QdTuQ.7EM.XhbEuTN8QfT24KHl2/RGRhAfrT2IeoPlAddu','MATIAS@MAIL.COM',1,1),(6,'jose_Vol','Jose','Perez','1234567890','$2b$12$0OBexgYQMBl92LptnJfq2e64dIkhF.ys/QnqBf0x0IxG2PaIwXTJW','jope@mail.com',4,1),(7,'maria_Voc','maria','lopez','1234567890','$2b$12$4MH0U.1ItAcMFeBO7/uvE.vze4F/d3SNZ7uhBgSnPVyfhILMDDoq.','malo@mail.com',2,1),(8,'Rami_Coor','ramiro','gutierrez','1234567890','$2b$12$/HchDKo1J1ZYIMe3gRTpZ.WfwRBjiejCG.vABdD3PVxNUmaVjBAp2','ragu@mail.com',3,1),(9,'Emi_coor','Emiliano','gomez','1133445566','$2b$12$Ww1zlL3SI1DFNQ8jWswmWOmaM5y1uRoJ2SRmunj.yyHUfm.75Vxf2','jogo@mail.com',3,1),(10,'pero_Voc','Pedro','rodriguez','1133445566','$2b$12$9PFsxSpJxlvgfXvdYXB8gehKArhDdG8HgMMyCNKjOpXMhfRztyA9W','pero@mail.com',2,0),(11,'MATIAS2','matias','velez','1234567890','$2b$12$47AHezSZxkwqdAmnfwFy9.k5mMZnntDumCqhfGjpjVKdR/A2KbGcK','velez.matias2020@gmail.com',1,1);
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'empujecomunitario'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


-- 1) Solicitudes PROPIAS (las que publica TU organización)
CREATE TABLE IF NOT EXISTS solicitudes (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  org_id        INT NOT NULL,                                  
  solicitud_id  VARCHAR(100) NOT NULL UNIQUE,                  
  fecha_hora    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,   
  payload_json  JSON NOT NULL,                                 
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_sol_org (org_id),
  KEY idx_sol_fechahora (fecha_hora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2) Ítems de solicitudes PROPIAS (separado de los ítems de externas)
CREATE TABLE IF NOT EXISTS solicitud_prop_items (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  solicitud_id  VARCHAR(100) NOT NULL,                          -- FK por código lógico
  categoria     VARCHAR(50),
  descripcion   VARCHAR(255),
  cantidad      INT,
  unidad        VARCHAR(20),
  CONSTRAINT fk_prop_items
    FOREIGN KEY (solicitud_id) REFERENCES solicitudes(solicitud_id)
    ON DELETE CASCADE,
  KEY idx_prop_items_sol (solicitud_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3) Vistas de conveniencia (propias, externas, unión)
DROP VIEW IF EXISTS vw_solicitudes_propias;
CREATE VIEW vw_solicitudes_propias AS
SELECT
  s.solicitud_id,
  s.org_id,
  s.fecha_hora,
  'PROPIA' AS origen,
  JSON_EXTRACT(s.payload_json, '$.items') AS items_json
FROM solicitudes s;

DROP VIEW IF EXISTS vw_solicitudes_externas;
CREATE VIEW vw_solicitudes_externas AS
SELECT
  se.solicitud_id,
  COALESCE(se.org_id, 0) AS org_id,
  se.fecha_hora,
  'EXTERNA' AS origen,
  JSON_EXTRACT(se.payload_json, '$.items') AS items_json
FROM solicitudes_externas se;

DROP VIEW IF EXISTS vw_solicitudes_todas;
CREATE VIEW vw_solicitudes_todas AS
SELECT * FROM vw_solicitudes_propias
UNION ALL
SELECT * FROM vw_solicitudes_externas;


CREATE TABLE IF NOT EXISTS solicitudes_cumplidas (
  solicitud_id VARCHAR(100) PRIMARY KEY,
  cumplida_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- Dump completed on 2025-10-08 12:43:57
