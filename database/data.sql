-- Datos iniciales para las tablas del microservicio de SOLICITUDES

-- Insertar estados de solicitudes
INSERT INTO states (id_state, name_state, description) VALUES
(1, 'Pendiente', 'Solicitud creada, pendiente de procesamiento'),
(2, 'Aprobado', 'Solicitud aprobada por el sistema'),
(3, 'Rechazado', 'Solicitud rechazada por el sistema'),
(4, 'Revision Manual', 'Solicitud requiere revision manual'),
(5, 'Cancelado', 'Solicitud cancelada por el usuario')
ON CONFLICT (id_state) DO NOTHING;

-- Insertar tipos de préstamo
INSERT INTO loan_type (id_loan_type, name_loan, max_amount, min_amount, interest_rate, auto_validation) VALUES
(1, 'Libre inversion', 50000000.00, 1000000.00, 18.0, true),
(2, 'Hipotecario', 200000000.00, 10000000.00, 12.5, true),
(3, 'Vehiculo', 80000000.00, 5000000.00, 15.8, false),
(4, 'Educativo', 30000000.00, 2000000.00, 10.5, true),
(5, 'Consumo', 20000000.00, 500000.00, 22.0, false)
ON CONFLICT (id_loan_type) DO NOTHING;
