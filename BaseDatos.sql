-- =====================================================================
-- BaseDatos.sql  |  Reto técnico - Arquitectura de microservicios
-- Cada microservicio tiene su propia base (database per service).
-- Motor: PostgreSQL 16. Con docker compose este script corre solo al
-- crear el volumen por primera vez.
-- =====================================================================

CREATE DATABASE customer_db;
CREATE DATABASE account_db;

-- ---------------------------------------------------------------------
-- customer_db  ->  Persona y Cliente (herencia JOINED)
-- ---------------------------------------------------------------------
\connect customer_db

CREATE TABLE persona (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    genero          VARCHAR(20)  NOT NULL,
    edad            INTEGER      NOT NULL CHECK (edad BETWEEN 0 AND 120),
    identificacion  VARCHAR(20)  NOT NULL UNIQUE,
    direccion       VARCHAR(255),
    telefono        VARCHAR(20)
);

CREATE TABLE cliente (
    persona_id  BIGINT       PRIMARY KEY REFERENCES persona (id) ON DELETE CASCADE,
    cliente_id  VARCHAR(50)  NOT NULL UNIQUE,
    contrasena  VARCHAR(100) NOT NULL,   -- hash BCrypt, nunca texto plano
    estado      BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ---------------------------------------------------------------------
-- account_db  ->  Cuenta y Movimiento (+ réplica local del cliente)
-- ---------------------------------------------------------------------
\connect account_db

-- Copia mínima del cliente, alimentada por eventos de RabbitMQ.
CREATE TABLE cliente_replica (
    cliente_id  VARCHAR(50)  PRIMARY KEY,
    nombre      VARCHAR(150) NOT NULL,
    estado      BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE cuenta (
    id             BIGSERIAL     PRIMARY KEY,
    numero_cuenta  VARCHAR(20)   NOT NULL UNIQUE,
    tipo_cuenta    VARCHAR(20)   NOT NULL CHECK (tipo_cuenta IN ('AHORRO', 'CORRIENTE')),
    saldo_inicial  NUMERIC(19,2) NOT NULL CHECK (saldo_inicial >= 0),
    saldo_actual   NUMERIC(19,2) NOT NULL CHECK (saldo_actual >= 0),
    estado         BOOLEAN       NOT NULL DEFAULT TRUE,
    cliente_id     VARCHAR(50)   NOT NULL REFERENCES cliente_replica (cliente_id)
);

CREATE TABLE movimiento (
    id               BIGSERIAL     PRIMARY KEY,
    fecha            TIMESTAMP     NOT NULL,
    tipo_movimiento  VARCHAR(20)   NOT NULL CHECK (tipo_movimiento IN ('DEPOSITO', 'RETIRO')),
    valor            NUMERIC(19,2) NOT NULL,
    saldo            NUMERIC(19,2) NOT NULL,   -- saldo disponible después del movimiento
    cuenta_id        BIGINT        NOT NULL REFERENCES cuenta (id)
);

CREATE INDEX idx_cuenta_cliente      ON cuenta (cliente_id);
CREATE INDEX idx_movimiento_cuenta_f ON movimiento (cuenta_id, fecha);
