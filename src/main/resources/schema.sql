-- Esquema de la base de datos de candidaturas (MySQL 8)

CREATE DATABASE IF NOT EXISTS integrador;
use integrador;
CREATE TABLE IF NOT EXISTS empresa (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    sector         VARCHAR(100) NULL,
    web            VARCHAR(200) NULL,
    fechaRegistro  DATE NOT NULL
) DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS oferta (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    id_empresa        INT NOT NULL,
    titulo            VARCHAR(150) NOT NULL,
    descripcion       TEXT NULL,
    estado            VARCHAR(20) NOT NULL,
    fechaPostulacion  DATE NOT NULL,
    enlace            VARCHAR(300) NULL,
    salario           DECIMAL(10,2) NULL,
    notas             TEXT NULL,
    CONSTRAINT fk_oferta_empresa_v2 FOREIGN KEY (id_empresa) REFERENCES empresa (id),
    CONSTRAINT ck_oferta_estado CHECK (estado IN ('pendiente','entrevista','rechazado','aceptado'))
) DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS entrevista (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    id_oferta  INT NOT NULL,
    fecha      DATETIME NOT NULL,
    tipo       VARCHAR(50) NULL,
    notas      TEXT NULL,
    resultado  VARCHAR(50) NULL,
    CONSTRAINT fk_entrevista_oferta_v2 FOREIGN KEY (id_oferta) REFERENCES oferta (id) ON DELETE CASCADE
) DEFAULT CHARSET = utf8mb4;
