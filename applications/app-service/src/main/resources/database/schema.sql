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

-- Tabla de información de usuarios (para compatibilidad con TransactionServiceAdapter)
CREATE TABLE IF NOT EXISTS users_info (
    id_request INTEGER PRIMARY KEY,
    document_type VARCHAR(10) NOT NULL,
    document_number VARCHAR(20) NOT NULL,
    credit_amount DECIMAL(15,2) NOT NULL,
    credit_time INTEGER NOT NULL,
    email VARCHAR(100) NOT NULL,
    id_state SMALLINT NOT NULL,
    id_loan_type SMALLINT NOT NULL,
    FOREIGN KEY (id_state) REFERENCES states(id_state),
    FOREIGN KEY (id_loan_type) REFERENCES loan_type(id_loan_type)
);

-- Índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_credit_applications_email ON credit_applications(email);
CREATE INDEX IF NOT EXISTS idx_credit_applications_state ON credit_applications(id_state);
CREATE INDEX IF NOT EXISTS idx_credit_applications_loan_type ON credit_applications(id_loan_type);
CREATE INDEX IF NOT EXISTS idx_credit_applications_document ON credit_applications(document_type, document_number);

-- Índices para users_info
CREATE INDEX IF NOT EXISTS idx_users_info_email ON users_info(email);
CREATE INDEX IF NOT EXISTS idx_users_info_state ON users_info(id_state);
CREATE INDEX IF NOT EXISTS idx_users_info_loan_type ON users_info(id_loan_type);
CREATE INDEX IF NOT EXISTS idx_users_info_document ON users_info(document_type, document_number);

-- Sincronizar datos existentes de credit_applications a users_info
INSERT INTO users_info (id_request, document_type, document_number, credit_amount, credit_time, email, id_state, id_loan_type)
SELECT id_request, document_type, document_number, credit_amount, credit_time, email, id_state, id_loan_type
FROM credit_applications
ON CONFLICT (id_request) DO UPDATE SET
    document_type = EXCLUDED.document_type,
    document_number = EXCLUDED.document_number,
    credit_amount = EXCLUDED.credit_amount,
    credit_time = EXCLUDED.credit_time,
    email = EXCLUDED.email,
    id_state = EXCLUDED.id_state,
    id_loan_type = EXCLUDED.id_loan_type;
