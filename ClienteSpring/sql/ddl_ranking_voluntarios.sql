-- Tabla de participantes de eventos externos (Necesario para filtro Externos)
CREATE TABLE IF NOT EXISTS evento_externo_participantes (
  evento_externo_id BIGINT NOT NULL,
  usuario_id        INT    NOT NULL,
  PRIMARY KEY (evento_externo_id, usuario_id),
  CONSTRAINT fk_eep_externo
    FOREIGN KEY (evento_externo_id) REFERENCES eventos_externos(id),
  CONSTRAINT fk_eep_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Tabla de filtros persistentes para ranking voluntarios
CREATE TABLE IF NOT EXISTS filtros_ranking_voluntarios (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  owner_user_id  BIGINT       NOT NULL,
  nombre         VARCHAR(120) NOT NULL,
  fecha_desde    DATE         NOT NULL,
  fecha_hasta    DATE         NOT NULL,
  tipo_evento    ENUM('INTERNOS','EXTERNOS','AMBOS') NOT NULL,
  top_n          INT          NOT NULL,
  created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_owner_nombre (owner_user_id, nombre)
);
