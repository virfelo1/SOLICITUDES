# Módulo de Capacidad de Endeudamiento - Microservicio SOLICITUDES

## Descripción General

Este módulo implementa la funcionalidad de cálculo de capacidad de endeudamiento para el microservicio de solicitudes, siguiendo los principios de arquitectura hexagonal y los requerimientos técnicos especificados.

## Arquitectura Implementada

### 1. Domain (Dominio)
- **Modelo**: `CreditApplication` (existente)
- **Gateways**: 
  - `CapacityCalculationService` - Para encolar solicitudes
  - `AutomaticValidationService` - Para procesar validación automática
- **Casos de Uso**: `CapacityCalculationUseCase`

### 2. Infrastructure (Infraestructura)
- **SQS Sender**: `CapacityCalculationSQSSender` - Envío de solicitudes a Lambda externa
- **SQS Listener**: `CapacityResultListener` - Recepción de resultados de Lambda
- **Transaction Service**: `TransactionServiceAdapter` - Operaciones atómicas
- **Automatic Validation**: `AutomaticValidationAdapter` - Lógica de validación automática
- **Loan Type Repository**: `LoanTypeRepositoryAdapter` - Consulta de tipos de préstamo

### 3. Entry Points (Puntos de Entrada)
- **REST API**: Endpoint `POST /api/v1/calcular-capacidad`
- **SQS Listener**: Recibe resultados de Lambda de capacidad de endeudamiento
- **DTO**: `CreditApplicationDTO` (existente)
- **Mapper**: `CreditApplicationDTOMapper` (existente)

## Flujo de Funcionamiento

### 1. Validación Automática
```
Solicitud Creada → AutomaticValidationService → Consultar configuración → ¿Requiere validación? → Sí → Encolar en SQS → Lambda Externa Procesa → Resultado por SQS → Actualizar Estado
```

### 2. Flujo Asíncrono con Lambda Externa
```
Microservicio → SQS (capacity-calculation) → Lambda AWS → Cálculo → SQS (capacity-results) → Microservicio → Actualizar Estado → Notificar por Email
```

### 3. Cálculo de Capacidad de Endeudamiento (Lambda Externa)
```
Ingresos Totales × 35% = Capacidad Máxima
Capacidad Máxima - Deuda Actual = Capacidad Disponible
Cuota Nuevo Préstamo ≤ Capacidad Disponible → APROBADO
Cuota Nuevo Préstamo > Capacidad Disponible → RECHAZADO
```

### 4. Lógica de Decisión
- **APROBADO**: Cuota ≤ Capacidad Disponible
- **REVISIÓN MANUAL**: Aprobado + Monto > 5 salarios mínimos
- **RECHAZADO**: Cuota > Capacidad Disponible

## Endpoints Disponibles

### POST /api/v1/calcular-capacidad
**Descripción**: Encola una solicitud para cálculo de capacidad de endeudamiento

**Request Body** (usando CreditApplicationDTO existente):
```json
{
  "documentType": "CC",
  "documentNumber": "111444",
  "creditAmount": 8000000.00,
  "creditTime": 12,
  "email": "prueba3@email.com",
  "idLoanType": 1
}
```

**Response** (Inmediata - Confirmación de Encolado):
```json
{
  "message": "Solicitud de cálculo de capacidad encolada exitosamente. Recibirá el resultado por correo electrónico.",
  "idRequest": "123"
}
```

**Resultado Final** (Recibido por SQS de Lambda - CreditApplication actualizado):
```json
{
  "idRequest": 123,
  "documentType": "CC",
  "documentNumber": "111444",
  "creditAmount": 8000000.00,
  "creditTime": 12,
  "email": "prueba3@email.com",
  "idState": 2,
  "idLoanType": 1
}
```

## Configuración

### Variables de Entorno
```yaml
AWS_REGION=us-east-1
CAPACITY_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/123456789012/capacity-calculation
CAPACITY_RESULT_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/123456789012/capacity-results
NOTIFICATION_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/123456789012/notifications
```

### Logging
```yaml
logging:
  level:
    co.com.projectve.usecase.capacity: DEBUG
    co.com.projectve.sqs.sender: INFO
    co.com.projectve.sqs.listener: INFO
```

## Características Implementadas

### ✅ Arquitectura Hexagonal
- Separación clara entre dominio e infraestructura
- Gateways para servicios externos
- Casos de uso independientes de frameworks

### ✅ Operaciones Atómicas
- Transacciones para actualización de estados
- Rollback automático en caso de errores
- Historial de cambios de estado

### ✅ Logs de Traza
- Trazabilidad completa del flujo
- IDs únicos por request
- Logs estructurados por niveles

### ✅ Manejo de Excepciones
- Handler global de excepciones
- Mensajes de error user-friendly
- Logs detallados para debugging

### ✅ Integración con Modelo Existente
- Uso exclusivo del modelo `CreditApplication`
- Compatibilidad con estructura actual del GET
- Reutilización de DTOs y mappers existentes

## Monitoreo y Métricas

El módulo incluye métricas de Prometheus para monitoreo:
- Tiempo de respuesta de Lambda
- Tasa de éxito/error en cálculos
- Volumen de solicitudes procesadas
- Tiempo de envío de emails

## Testing

Para ejecutar las pruebas:
```bash
./gradlew test --tests "*Capacity*"
```

## Despliegue

1. **Configurar Base de Datos**:
   - Ejecutar script `database/loan_types_data.sql` para poblar tipos de préstamo
   - Verificar tabla `loan_type` con campo `auto_validation`

2. **Configurar Variables de Entorno**:
   - Configurar URLs de colas SQS
   - Configurar región AWS

3. **Crear Colas SQS**:
   - `capacity-calculation` (para enviar solicitudes a Lambda)
   - `capacity-results` (para recibir resultados de Lambda)
   - `notifications` (para notificaciones por email)

4. **Desplegar Lambda Externa**:
   - Lambda que procese `CreditApplication`
   - Envíe resultado actualizado por SQS

5. **Configurar Permisos**:
   - Permisos SQS para Lambda y microservicio
   - Permisos de base de datos para consultar `loan_type`

6. **Desplegar Microservicio**:
   - Con nuevos módulos SQS
   - Iniciar SQS Listener automáticamente

## Arquitectura de Colas SQS

```
┌─────────────────┐    ┌──────────────────────┐    ┌─────────────────────┐
│   Microservicio │───▶│ capacity-calculation │───▶│   Lambda Externa    │
│   SOLICITUDES   │    │        (SQS)         │    │ Capacidad Endeud.   │
└─────────────────┘    └──────────────────────┘    └─────────────────────┘
         ▲                                                        │
         │                                                        │
         │              ┌──────────────────────┐                  │
         └──────────────│  capacity-results    │◀─────────────────┘
                        │        (SQS)         │
                        └──────────────────────┘
```

## Configuración de Tipos de Préstamo

### Tipos con Validación Automática (auto_validation = true):
- **Libre inversión** (ID: 1) - Se procesará automáticamente
- **Hipotecario** (ID: 2) - Se procesará automáticamente  
- **Educativo** (ID: 4) - Se procesará automáticamente

### Tipos sin Validación Automática (auto_validation = false):
- **Vehículo** (ID: 3) - Requiere revisión manual
- **Consumo** (ID: 5) - Requiere revisión manual

## Próximos Pasos

1. Implementar cache para tipos de préstamo
2. Agregar validaciones adicionales de negocio
3. Implementar retry automático para fallos de SQS
4. Agregar métricas de negocio específicas
5. Implementar Dead Letter Queue (DLQ) para mensajes fallidos
6. Agregar monitoring y alertas para el flujo asíncrono
7. Agregar configuración dinámica de tipos de préstamo
