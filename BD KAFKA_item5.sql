-- Item 5: Tablas para transferencia-donaciones y adhesion-evento
USE `empujecomunitario`;

-- Registro de transferencias de solicitudes entre organizaciones
CREATE TABLE IF NOT EXISTS `transferencias_externas` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `org_id_origen` INT NOT NULL,
  `org_id_destino` INT NOT NULL,
  `solicitud_id` VARCHAR(100) NOT NULL,
  `fecha_hora` DATETIME NOT NULL,
  `idempotency_key` VARCHAR(200) NOT NULL,
  `payload_json` JSON NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_transferencia_idem` (`idempotency_key`),
  KEY `idx_trf_solicitud` (`solicitud_id`),
  KEY `idx_trf_destino` (`org_id_destino`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Registro de adhesiones de organizaciones a un evento
CREATE TABLE IF NOT EXISTS `adhesiones_evento` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `org_id_organizador` INT NOT NULL,
  `evento_id` VARCHAR(100) NOT NULL,
  `org_id_adherente` INT NOT NULL,
  `fecha_hora` DATETIME NOT NULL,
  `idempotency_key` VARCHAR(200) NOT NULL,
  `payload_json` JSON NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_adhesion_idem` (`idempotency_key`),
  KEY `idx_adh_evento` (`evento_id`),
  KEY `idx_adh_org` (`org_id_adherente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
