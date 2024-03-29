SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `bulas` ;
CREATE SCHEMA IF NOT EXISTS `bulas` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci ;
USE `bulas` ;

-- -----------------------------------------------------
-- Table `bulas`.`usuario`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bulas`.`usuario` ;

CREATE  TABLE IF NOT EXISTS `bulas`.`usuario` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `nome` VARCHAR(60) NOT NULL ,
  `dtnascimento` DATE NOT NULL ,
  `login` VARCHAR(16) NOT NULL ,
  `password` VARCHAR(200) NOT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `nome_UNIQUE` (`nome` ASC) ,
  UNIQUE INDEX `login_UNIQUE` (`login` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `bulas`.`laboratorio`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bulas`.`laboratorio` ;

CREATE  TABLE IF NOT EXISTS `bulas`.`laboratorio` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `nome` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `nome_UNIQUE` (`nome` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `bulas`.`medicamento`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bulas`.`medicamento` ;

CREATE  TABLE IF NOT EXISTS `bulas`.`medicamento` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `nome` VARCHAR(100) NOT NULL ,
  `associacao` VARCHAR(160) NOT NULL ,
  `lab_detentor_id` INT NOT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `nome_assoc_UNIQUE` (`nome` ASC, `associacao` ASC) ,
  INDEX `fk_medicamento_laboratorio1` (`lab_detentor_id` ASC) ,
  CONSTRAINT `fk_medicamento_laboratorio1`
    FOREIGN KEY (`lab_detentor_id` )
    REFERENCES `bulas`.`laboratorio` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
COMMENT = 'Representa o medicamento de marca.';


-- -----------------------------------------------------
-- Table `bulas`.`bula`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bulas`.`bula` ;

CREATE  TABLE IF NOT EXISTS `bulas`.`bula` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `medicamento_fk` INT NULL DEFAULT NULL ,
  `texto` MEDIUMTEXT NOT NULL ,
  `codigo` VARCHAR(10) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_bula_medicamento1` (`medicamento_fk` ASC) ,
  UNIQUE INDEX `codigo_UNIQUE` (`codigo` ASC) ,
  CONSTRAINT `fk_bula_medicamento1`
    FOREIGN KEY (`medicamento_fk` )
    REFERENCES `bulas`.`medicamento` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `bulas`.`farmaco`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bulas`.`farmaco` ;

CREATE  TABLE IF NOT EXISTS `bulas`.`farmaco` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `nome` VARCHAR(100) NOT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `bulas`.`medicamentos_do_usuario`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bulas`.`medicamentos_do_usuario` ;

CREATE  TABLE IF NOT EXISTS `bulas`.`medicamentos_do_usuario` (
  `usuario_fk` INT NOT NULL ,
  `medicamento_fk` INT NOT NULL ,
  PRIMARY KEY (`usuario_fk`, `medicamento_fk`) ,
  INDEX `fk_usuario_has_medicamento_usuario` (`usuario_fk` ASC) ,
  INDEX `fk_usuario_has_medicamento_medicamento1` (`medicamento_fk` ASC) ,
  CONSTRAINT `fk_usuario_has_medicamento_usuario`
    FOREIGN KEY (`usuario_fk` )
    REFERENCES `bulas`.`usuario` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_usuario_has_medicamento_medicamento1`
    FOREIGN KEY (`medicamento_fk` )
    REFERENCES `bulas`.`medicamento` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `bulas`.`secao_bula`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bulas`.`secao_bula` ;

CREATE  TABLE IF NOT EXISTS `bulas`.`secao_bula` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `nome_curto` VARCHAR(30) NOT NULL ,
  `nome` VARCHAR(60) NULL ,
  `grupo` INT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `nome_curto_UNIQUE` (`nome_curto` ASC) )
ENGINE = InnoDB
COMMENT = 'Seções capturáveis pelo parser (sections.properties)';


-- -----------------------------------------------------
-- Table `bulas`.`conteudo_secao_bula`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bulas`.`conteudo_secao_bula` ;

CREATE  TABLE IF NOT EXISTS `bulas`.`conteudo_secao_bula` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `bula_id` INT NOT NULL ,
  `secao_id` INT NOT NULL ,
  `texto` MEDIUMTEXT NOT NULL ,
  INDEX `fk_bula_has_campo_bula_bula1` (`bula_id` ASC) ,
  INDEX `fk_bula_has_campo_bula_campo_bula1` (`secao_id` ASC) ,
  fulltext INDEX `texto_secao_fulltext`  (`texto` asc),
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `bula_secao_UNIQUE` (`bula_id` ASC, `secao_id` ASC) ,
  CONSTRAINT `fk_bula_has_campo_bula_bula1`
    FOREIGN KEY (`bula_id` )
    REFERENCES `bulas`.`bula` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_bula_has_campo_bula_campo_bula1`
    FOREIGN KEY (`secao_id` )
    REFERENCES `bulas`.`secao_bula` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `bulas`.`associacao`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bulas`.`associacao` ;

CREATE  TABLE IF NOT EXISTS `bulas`.`associacao` (
  `medicamento_id` INT NOT NULL ,
  `farmaco_id` INT NOT NULL ,
  PRIMARY KEY (`medicamento_id`, `farmaco_id`) ,
  INDEX `fk_medicamento_has_farmaco_medicamento1` (`medicamento_id` ASC) ,
  INDEX `fk_medicamento_has_farmaco_farmaco1` (`farmaco_id` ASC) ,
  CONSTRAINT `fk_medicamento_has_farmaco_medicamento1`
    FOREIGN KEY (`medicamento_id` )
    REFERENCES `bulas`.`medicamento` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_medicamento_has_farmaco_farmaco1`
    FOREIGN KEY (`farmaco_id` )
    REFERENCES `bulas`.`farmaco` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `bulas`.`generico`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bulas`.`generico` ;

CREATE  TABLE IF NOT EXISTS `bulas`.`generico` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `medicamento_id` INT NOT NULL ,
  `laboratorio_id` INT NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_generico_medicamento1` (`medicamento_id` ASC) ,
  INDEX `fk_generico_laboratorio1` (`laboratorio_id` ASC) ,
  CONSTRAINT `fk_generico_medicamento1`
    FOREIGN KEY (`medicamento_id` )
    REFERENCES `bulas`.`medicamento` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_generico_laboratorio1`
    FOREIGN KEY (`laboratorio_id` )
    REFERENCES `bulas`.`laboratorio` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `bulas`.`forma_farmaceutica`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bulas`.`forma_farmaceutica` ;

CREATE  TABLE IF NOT EXISTS `bulas`.`forma_farmaceutica` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `nome` VARCHAR(120) NULL ,
  `simbolo` VARCHAR(40) NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
COMMENT = 'Comprimidos, creme, solução oral, etc.';


-- -----------------------------------------------------
-- Table `bulas`.`apresentacao`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bulas`.`apresentacao` ;

CREATE  TABLE IF NOT EXISTS `bulas`.`apresentacao` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `forma_id` INT NOT NULL ,
  `medicamento_id` INT NOT NULL ,
  `concentracao` VARCHAR(45) NULL ,
  INDEX `fk_forma_farmaceutica_has_medicamento_forma_farmaceutica1` (`forma_id` ASC) ,
  INDEX `fk_forma_farmaceutica_has_medicamento_medicamento1` (`medicamento_id` ASC) ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_forma_farmaceutica_has_medicamento_forma_farmaceutica1`
    FOREIGN KEY (`forma_id` )
    REFERENCES `bulas`.`forma_farmaceutica` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_forma_farmaceutica_has_medicamento_medicamento1`
    FOREIGN KEY (`medicamento_id` )
    REFERENCES `bulas`.`medicamento` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `bulas`.`alergia`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bulas`.`alergia` ;

CREATE  TABLE IF NOT EXISTS `bulas`.`alergia` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `usuario_id` INT NOT NULL ,
  `farmaco_id` INT NULL ,
  `medicamento_id` INT NULL ,
  INDEX `fk_usuario_has_farmaco_usuario1` (`usuario_id` ASC) ,
  INDEX `fk_usuario_has_farmaco_farmaco1` (`farmaco_id` ASC) ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_alergia_medicamento1` (`medicamento_id` ASC) ,
  CONSTRAINT `fk_usuario_has_farmaco_usuario1`
    FOREIGN KEY (`usuario_id` )
    REFERENCES `bulas`.`usuario` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_usuario_has_farmaco_farmaco1`
    FOREIGN KEY (`farmaco_id` )
    REFERENCES `bulas`.`farmaco` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_alergia_medicamento1`
    FOREIGN KEY (`medicamento_id` )
    REFERENCES `bulas`.`medicamento` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;



-- Manual
DROP USER `bulas`@`localhost`;
CREATE USER `bulas`@`localhost` IDENTIFIED BY '1234';
GRANT ALL PRIVILEGES ON `bulas`.* TO `bulas`@`localhost` WITH GRANT OPTION;
