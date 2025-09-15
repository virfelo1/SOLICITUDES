-- Script de inicialización de la base de datos
-- Este script crea todas las tablas necesarias para el microservicio de SOLICITUDES

-- Tabla de estados de solicitudes
CREATE TABLE IF NOT EXISTS states (
    id_state SMALLINT PRIMARY KEY,
    name_state VARCHAR(50) NOT NULL,
    description VARCHAR(255)
);

-- Tabla de tipos de préstamo
CREATE TABLE IF NOT EXISTS loan_type (
    id_loan_type SMALLINT PRIMARY KEY,
    name_loan VARCHAR(50) NOT NULL,
    max_amount DECIMAL(15,2) NOT NULL,
    min_amount DECIMAL(15,2) NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL,
    auto_validation BOOLEAN NOT NULL DEFAULT FALSE
);

-- Tabla de solicitudes de crédito
CREATE TABLE IF NOT EXISTS credit_applications (
    id_request SERIAL PRIMARY KEY,
    document_type VARCHAR(10) NOT NULL,
    document_number VARCHAR(20) NOT NULL,
    credit_amount DECIMAL(15,2) NOT NULL,
    credit_time INTEGER NOT NULL,
    email VARCHAR(100) NOT NULL,
    id_state SMALLINT NOT NULL,
    id_loan_type SMALLINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    state_history TEXT,
    FOREIGN KEY (id_state) REFERENCES states(id_state),
    FOREIGN KEY (id_loan_type) REFERENCES loan_type(id_loan_type)
);

-- Índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_credit_applications_email ON credit_applications(email);
CREATE INDEX IF NOT EXISTS idx_credit_applications_state ON credit_applications(id_state);
CREATE INDEX IF NOT EXISTS idx_credit_applications_loan_type ON credit_applications(id_loan_type);
CREATE INDEX IF NOT EXISTS idx_credit_applications_document ON credit_applications(document_type, document_number);
