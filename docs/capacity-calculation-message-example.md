# Ejemplo de Mensaje Enriquecido para Cálculo de Capacidad de Endeudamiento

## Descripción
Este documento muestra el formato del mensaje enriquecido que se envía a la cola SQS para el cálculo de capacidad de endeudamiento por parte de la Lambda externa.

## Estructura del Mensaje

### Mensaje Original (antes del enriquecimiento)
```json
{
  "idRequest": 33,
  "documentType": "CC",
  "documentNumber": "123456789",
  "creditAmount": 1000000,
  "creditTime": 24,
  "email": "librecarbon@gmail.com",
  "idState": 1,
  "idLoanType": 3
}
```

### Mensaje Enriquecido (después del enriquecimiento)
```json
{
  "idRequest": 33,
  "documentType": "CC",
  "documentNumber": "123456789",
  "creditAmount": 1000000,
  "creditTime": 24,
  "email": "librecarbon@gmail.com",
  "idState": 1,
  "idLoanType": 3,
  "interestRate": 12.5,
  "baseSalary": 3000000,
  "currentMonthlyDebt": 150000,
  "maxBorrowingCapacity": 1050000,
  "availableCapacity": 900000
}
```

## Campos Adicionales Explicados

### `interestRate` (Double)
- **Descripción**: Tasa de interés del tipo de préstamo seleccionado
- **Fuente**: Tabla `loan_type.interest_rate`
- **Ejemplo**: `12.5` (representa 12.5% anual)

### `baseSalary` (BigDecimal)
- **Descripción**: Salario base del solicitante
- **Fuente**: Microservicio de autenticación via `AuthClient`
- **Ejemplo**: `3000000` (3,000,000 COP)

### `currentMonthlyDebt` (BigDecimal)
- **Descripción**: Suma de las cuotas mensuales de todos los créditos aprobados del solicitante
- **Fuente**: Cálculo basado en préstamos con `id_state = 2` (Aprobado)
- **Ejemplo**: `150000` (150,000 COP)

### `maxBorrowingCapacity` (BigDecimal)
- **Descripción**: Capacidad máxima de endeudamiento (35% del salario base)
- **Cálculo**: `baseSalary * 0.35`
- **Ejemplo**: `1050000` (1,050,000 COP)

### `availableCapacity` (BigDecimal)
- **Descripción**: Capacidad disponible para nuevo crédito
- **Cálculo**: `maxBorrowingCapacity - currentMonthlyDebt`
- **Ejemplo**: `900000` (900,000 COP)

## Flujo de Enriquecimiento

1. **Solicitud de crédito** → Se crea `CreditApplication`
2. **Validación automática** → Se verifica si `auto_validation = true`
3. **Enriquecimiento de datos**:
   - Se obtiene información del tipo de préstamo (`LoanTypeInfo`)
   - Se consulta el salario base del usuario (`AuthClient`)
   - Se calcula la deuda mensual actual (`ActiveLoanRepository`)
   - Se calcula la capacidad de endeudamiento (`CapacityCalculationGateway`)
4. **Envío a SQS** → Se envía el mensaje enriquecido a la Lambda externa

## Ventajas del Mensaje Enriquecido

- **Información completa**: La Lambda externa tiene todos los datos necesarios
- **Eficiencia**: No necesita consultas adicionales a la base de datos
- **Consistencia**: Los datos se calculan en el momento del envío
- **Trazabilidad**: Se mantiene el ID de la solicitud para seguimiento

## Uso por la Lambda Externa

La Lambda externa puede usar estos datos para:
- Validar la capacidad de endeudamiento
- Aplicar reglas de negocio adicionales
- Generar el plan de pagos
- Tomar la decisión final (APROBADO/RECHAZADO/REVISION MANUAL)
