-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema empuje_comunitario
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema empuje_comunitario
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `empuje_comunitario` DEFAULT CHARACTER SET utf8 ;
USE `empuje_comunitario` ;

-- -----------------------------------------------------
-- Table `empuje_comunitario`.`rol`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `empuje_comunitario`.`rol` (
  `id_rol` INT NOT NULL,
  `nombre` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id_rol`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `empuje_comunitario`.`usuario`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `empuje_comunitario`.`usuario` (
  `id_usuario` INT NOT NULL AUTO_INCREMENT,
  `nombre_usuario` VARCHAR(45) NOT NULL,
  `nombre` VARCHAR(45) NOT NULL,
  `apellido` VARCHAR(45) NOT NULL,
  `telefono` VARCHAR(20) NULL,
  `password` VARCHAR(255) NOT NULL,
  `email` VARCHAR(45) NOT NULL,
  `activo` TINYINT NOT NULL,
  `rol_id` INT NOT NULL,
  PRIMARY KEY (`id_usuario`, `rol_id`),
  UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE,
  UNIQUE INDEX `id_usuario_UNIQUE` (`id_usuario` ASC) VISIBLE,
  INDEX `fk_usuario_rol1_idx` (`rol_id` ASC) VISIBLE,
  CONSTRAINT `fk_usuario_rol1`
    FOREIGN KEY (`rol_id`)
    REFERENCES `empuje_comunitario`.`rol` (`id_rol`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `empuje_comunitario`.`donacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `empuje_comunitario`.`donacion` (
  `id_donacion` INT NOT NULL AUTO_INCREMENT,
  `categoria` VARCHAR(45) NULL,
  `descripcion` VARCHAR(200) NULL,
  `cantidad` INT NULL,
  `eliminado` TINYINT NULL,
  `fecha_alta` DATETIME NULL,
  `fecha_modificacion` DATETIME NULL,
  `usuario_modificacion_id` INT NULL,
  `usuario_alta_id` INT NOT NULL,
  PRIMARY KEY (`id_donacion`, `usuario_alta_id`),
  INDEX `fk_donacion_usuario1_idx` (`usuario_alta_id` ASC) VISIBLE,
  CONSTRAINT `fk_donacion_usuario1`
    FOREIGN KEY (`usuario_alta_id`)
    REFERENCES `empuje_comunitario`.`usuario` (`id_usuario`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `empuje_comunitario`.`evento`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `empuje_comunitario`.`evento` (
  `id_evento` INT NOT NULL,
  `nombre` VARCHAR(45) NULL,
  `descripcion` VARCHAR(200) NULL,
  `fecha_hora` DATETIME NULL,
  `eliminado` TINYINT NULL,
  PRIMARY KEY (`id_evento`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `empuje_comunitario`.`usuario_evento`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `empuje_comunitario`.`usuario_evento` (
  `usuario_id` INT NOT NULL,
  `evento_id` INT NOT NULL,
  `fecha_inscripcion` DATETIME NULL,
  PRIMARY KEY (`usuario_id`, `evento_id`),
  INDEX `fk_usuario_has_evento_evento1_idx` (`evento_id` ASC) VISIBLE,
  INDEX `fk_usuario_has_evento_usuario_idx` (`usuario_id` ASC) VISIBLE,
  CONSTRAINT `fk_usuario_has_evento_usuario`
    FOREIGN KEY (`usuario_id`)
    REFERENCES `empuje_comunitario`.`usuario` (`id_usuario`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_usuario_has_evento_evento1`
    FOREIGN KEY (`evento_id`)
    REFERENCES `empuje_comunitario`.`evento` (`id_evento`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `empuje_comunitario`.`evento_reparticion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `empuje_comunitario`.`evento_reparticion` (
  `evento_id` INT NOT NULL,
  `donacion_id` INT NOT NULL,
  `cant_utilizada` INT NULL,
  PRIMARY KEY (`evento_id`, `donacion_id`),
  INDEX `fk_evento_has_donacion_donacion1_idx` (`donacion_id` ASC) VISIBLE,
  INDEX `fk_evento_has_donacion_evento1_idx` (`evento_id` ASC) VISIBLE,
  CONSTRAINT `fk_evento_has_donacion_evento1`
    FOREIGN KEY (`evento_id`)
    REFERENCES `empuje_comunitario`.`evento` (`id_evento`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_evento_has_donacion_donacion1`
    FOREIGN KEY (`donacion_id`)
    REFERENCES `empuje_comunitario`.`donacion` (`id_donacion`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
