USE `empujecomunitario`;

CREATE TABLE IF NOT EXISTS filtros_eventos (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  owner_user_id      BIGINT      NOT NULL,       -- usuario logueado que guarda el filtro
  nombre             VARCHAR(120) NOT NULL,      -- nombre elegido por el usuario
  usuario_objetivo_id BIGINT     NOT NULL,       -- el “usuario” obligatorio del informe
  fecha_desde        DATE        NULL,
  fecha_hasta        DATE        NULL,
  reparto            ENUM('SI','NO','AMBOS') NULL,
  created_at         TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
  updated_at         TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_owner_nombre (owner_user_id, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
