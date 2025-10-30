USE `empujecomunitario`;

CREATE TABLE IF NOT EXISTS filtros_donaciones (
  id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
  owner_user_id       BIGINT       NOT NULL,          -- usuario logueado (dueño del filtro)
  nombre              VARCHAR(120) NOT NULL,          -- nombre elegido por el usuario
  categoria           VARCHAR(50)  NULL,              -- ALIMENTOS/ROPA/JUGUETES/UTILES_ESCOLARES o null (todas las categorias)
  fecha_desde         DATE         NULL,              -- "from"
  fecha_hasta         DATE         NULL,              -- "to"
  eliminado           ENUM('SI','NO','AMBOS') NULL,   -- "eliminado"
  created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_owner_nombre (owner_user_id, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
