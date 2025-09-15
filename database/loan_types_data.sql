-- Datos de prueba para la tabla loan_type
-- Estos datos permiten probar la funcionalidad de validación automática

INSERT INTO loan_type (id_loan_type, name_loan, max_amount, min_amount, interest_rate, auto_validation) VALUES
(1, 'Libre inversion', 50000000.00, 1000000.00, 18.0, true),
(2, 'Hipotecario', 200000000.00, 10000000.00, 12.5, true),
(3, 'Vehiculo', 80000000.00, 5000000.00, 15.8, false),
(4, 'Educativo', 30000000.00, 2000000.00, 10.5, true),
(5, 'Consumo', 20000000.00, 500000.00, 22.0, false);

-- Tipos de préstamo con validación automática habilitada (auto_validation = true):
-- - Libre inversión (ID: 1)
-- - Hipotecario (ID: 2) 
-- - Educativo (ID: 4)

-- Tipos de préstamo sin validación automática (auto_validation = false):
-- - Vehículo (ID: 3)
-- - Consumo (ID: 5)
