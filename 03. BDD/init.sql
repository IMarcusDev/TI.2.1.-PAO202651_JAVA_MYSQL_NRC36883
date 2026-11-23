-- =============================================================
-- Sistema CountryAPI · TI.2.1 Consumo de APIs Públicas en Java con MySQL (MVC)
-- Script de creación de la base de datos api_paises (MySQL 8)
-- Autor: Escobar Marcos · 21/11/2026
-- =============================================================

DROP DATABASE IF EXISTS api_paises;
CREATE DATABASE api_paises
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE api_paises;

-- -------------------------------------------------------------
-- usuario: personas que inician sesión en el sistema.
-- La clave se guarda como hash SHA-256 (64 caracteres hexadecimales).
-- -------------------------------------------------------------
CREATE TABLE usuario (
  id_usuario     INT          NOT NULL AUTO_INCREMENT,
  username       VARCHAR(50)  NOT NULL,
  password_hash  CHAR(64)     NOT NULL,
  nombre         VARCHAR(100) NOT NULL,
  estado         VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO',
  CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
  CONSTRAINT uq_usuario_username UNIQUE (username),
  CONSTRAINT ck_usuario_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
) ENGINE = InnoDB;

-- -------------------------------------------------------------
-- region: catálogo de regiones de la API REST Countries, en español.
-- -------------------------------------------------------------
CREATE TABLE region (
  id_region  INT         NOT NULL AUTO_INCREMENT,
  nombre     VARCHAR(50) NOT NULL,
  CONSTRAINT pk_region PRIMARY KEY (id_region),
  CONSTRAINT uq_region_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

-- -------------------------------------------------------------
-- pais: tabla del taller, normalizada.
--   * nombre único: importar varias veces no duplica países.
--   * la región pasa al catálogo region (id_region).
--   * subregión y área (km²) tomadas de la API; área 0 si no viene.
--   * población y área no negativas y fecha de la última actualización.
-- -------------------------------------------------------------
CREATE TABLE pais (
  id                   INT          NOT NULL AUTO_INCREMENT,
  nombre               VARCHAR(100) NOT NULL,
  capital              VARCHAR(100) NULL,
  id_region            INT          NOT NULL,
  subregion            VARCHAR(100) NULL,
  poblacion            BIGINT       NOT NULL DEFAULT 0,
  area                 DOUBLE       NOT NULL DEFAULT 0,
  moneda               VARCHAR(100) NULL,
  fecha_actualizacion  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT pk_pais PRIMARY KEY (id),
  CONSTRAINT uq_pais_nombre UNIQUE (nombre),
  CONSTRAINT ck_pais_poblacion CHECK (poblacion >= 0),
  CONSTRAINT ck_pais_area CHECK (area >= 0),
  CONSTRAINT fk_pais_region FOREIGN KEY (id_region)
    REFERENCES region (id_region)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

-- -------------------------------------------------------------
-- universidad: universidades importadas desde la API de universidades
-- (universities.hipolabs.com). La pareja nombre y país es única, así que
-- importar el mismo país varias veces actualiza en lugar de duplicar.
-- -------------------------------------------------------------
CREATE TABLE universidad (
  id                   INT          NOT NULL AUTO_INCREMENT,
  nombre               VARCHAR(200) NOT NULL,
  dominio              VARCHAR(150) NULL,
  sitio_web            VARCHAR(250) NULL,
  pais                 VARCHAR(100) NOT NULL,
  fecha_actualizacion  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT pk_universidad PRIMARY KEY (id),
  CONSTRAINT uq_universidad_nombre_pais UNIQUE (nombre, pais)
) ENGINE = InnoDB;
