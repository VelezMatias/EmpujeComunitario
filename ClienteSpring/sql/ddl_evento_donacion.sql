
USE `empujecomunitario`;

CREATE TABLE IF NOT EXISTS `evento_donacion` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `evento_id` INT NOT NULL,
  `donacion_id` INT NOT NULL,
  `cantidad` INT DEFAULT 0,
  `fecha_alta` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_evento` (`evento_id`),
  KEY `idx_donacion` (`donacion_id`),
  CONSTRAINT `fk_evento_donacion_evento` FOREIGN KEY (`evento_id`) REFERENCES `eventos`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_evento_donacion_donacion` FOREIGN KEY (`donacion_id`) REFERENCES `donaciones`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO evento_donacion (evento_id, donacion_id, cantidad) VALUES
  (1, 1, 5),
  (1, 2, 3),
  (2, 3, 2);

INSERT INTO eventos (nombre, descripcion, fecha_hora, creador_id) VALUES
  ('Campaña Agosto - Ropa', 'Reparto de ropa a la comunidad', '2025-08-10 10:00:00', 1),
  ('Campaña Agosto - Alimentos', 'Reparto de alimentos no perecederos', '2025-08-20 15:00:00', 1);

INSERT INTO evento_participantes (evento_id, usuario_id)
SELECT e.id, 4 FROM eventos e WHERE e.nombre IN ('Campaña Agosto - Ropa','Campaña Agosto - Alimentos');

INSERT INTO evento_participantes (evento_id, usuario_id)
SELECT e.id, 2 FROM eventos e WHERE e.nombre IN ('Campaña Agosto - Ropa','Campaña Agosto - Alimentos');

INSERT INTO evento_donacion (evento_id, donacion_id, cantidad)
SELECT e.id, 1, 10 FROM eventos e WHERE e.nombre = 'Campaña Agosto - Ropa';

INSERT INTO evento_donacion (evento_id, donacion_id, cantidad)
SELECT e.id, 2, 20 FROM eventos e WHERE e.nombre = 'Campaña Agosto - Alimentos';
