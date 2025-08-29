# Guía de Logging y Trazabilidad - Proyecto SOLICITUDES

## Descripción General

Este proyecto implementa un sistema completo de logging y trazabilidad que permite rastrear el flujo completo de las solicitudes de crédito desde la recepción HTTP hasta la persistencia en base de datos.

## Arquitectura de Logging

### 1. Niveles de Log Implementados

- **TRACE**: Logs más detallados para debugging profundo
- **DEBUG**: Información detallada para desarrollo
- **INFO**: Información general del flujo de la aplicación
- **WARN**: Advertencias que no impiden la ejecución
- **ERROR**: Errores que requieren atención

### 2. Componentes con Logging

#### Entry Point (API)
- **Handler**: Logs de traza para cada petición HTTP
- **LoggingFilter**: Filtro global para todas las peticiones HTTP
- **RouterRest**: Configuración de endpoints

#### Use Case
- **CreditApplicationUseCase**: Logs de la lógica de negocio

#### Infrastructure
- **MyReactiveRepositoryAdapter**: Logs de persistencia y base de datos

#### Application
- **MainApplication**: Logs de inicio y configuración

## Configuración de Logs

### 1. Archivos de Configuración

- `log4j2.properties`: Configuración básica de Log4j2
- `logback-spring.xml`: Configuración avanzada de Logback
- `application-logging.yaml`: Configuración específica de logging

### 2. Estructura de Archivos de Log

```
logs/
├── application.log          # Logs generales de la aplicación
├── trace.log               # Logs de nivel TRACE
├── error.log               # Solo logs de ERROR
└── archive/                # Archivos rotados
    ├── application.2024-01-15.0.log
    ├── trace.2024-01-15.0.log
    └── error.2024-01-15.0.log
```

## Características de Trazabilidad

### 1. Request ID Único
Cada petición HTTP recibe un ID único de 8 caracteres para rastreo:
```
[abc12345] Iniciando procesamiento de solicitud de crédito
[abc12345] DTO recibido: tipoDocumento=CC, numeroDocumento=123456789...
[abc12345] Solicitud procesada exitosamente. ID de respuesta: 1
```

### 2. Headers de Trazabilidad
- `X-Request-ID`: ID único de la petición
- Timestamp en cada log
- Duración de procesamiento

### 3. Flujo de Trazabilidad
```
HTTP Request → LoggingFilter → Handler → UseCase → Repository → Database
     ↓              ↓           ↓        ↓         ↓          ↓
  Request ID    Timestamp   Trace    Business   Persistence  SQL Logs
```

## Uso de los Logs

### 1. Para Desarrollo
```bash
# Ver logs en consola
./gradlew bootRun

# Ver logs de un paquete específico
tail -f logs/trace.log | grep "co.com.projectve.api"
```

### 2. Para Debugging
```bash
# Ver logs de errores
tail -f logs/error.log

# Ver logs de traza completos
tail -f logs/trace.log
```

### 3. Para Monitoreo
```bash
# Ver métricas de la aplicación
curl http://localhost:8080/actuator/metrics

# Ver health check
curl http://localhost:8080/actuator/health
```

## Configuración de Niveles

### 1. Por Paquete
```yaml
logging:
  level:
    co.com.projectve.api: TRACE      # Logs más detallados para API
    co.com.projectve.usecase: TRACE  # Logs de casos de uso
    co.com.projectve.r2dbc: TRACE    # Logs de base de datos
    co.com.projectve.model: DEBUG    # Logs de modelos
```

### 2. Por Entorno
```yaml
# Development
logging.level.root: DEBUG

# Production
logging.level.root: INFO
logging.level.co.com.projectve: WARN
```

## Ejemplos de Logs

### 1. Log de Petición HTTP
```
[TRACE] [abc12345] [2024-01-15 10:30:45.123] [http-nio-8080-exec-1] co.com.projectve.api.LoggingFilter - Petición HTTP recibida: POST /api/v1/solicitud - Headers: {Content-Type=[application/json]}
```

### 2. Log de Validación
```
[WARN] [abc12345] Validación fallida. Violaciones encontradas: 2
[WARN] [abc12345] Violación: documentType - El tipo de documento es obligatorio
[WARN] [abc12345] Violación: creditAmount - El monto del crédito debe ser mayor a 0
```

### 3. Log de Persistencia
```
[TRACE] [abc12345] Iniciando solicitud de guardado para CreditApplication. ID: null, TipoDocumento: CC, NumeroDocumento: 123456789
[INFO] [abc12345] CreditApplication guardado exitosamente con ID: 1
```

## Mejores Prácticas

### 1. Uso de Request ID
- Siempre incluir el Request ID en logs relacionados
- Usar formato consistente: `[RequestID] Mensaje`

### 2. Niveles de Log
- **TRACE**: Para debugging profundo y flujo detallado
- **DEBUG**: Para información de desarrollo
- **INFO**: Para eventos importantes del negocio
- **WARN**: Para situaciones que requieren atención
- **ERROR**: Solo para errores reales

### 3. Performance
- Los logs TRACE pueden impactar el rendimiento
- En producción, considerar nivel INFO o WARN
- Usar logback para mejor rendimiento

## Troubleshooting

### 1. Logs No Aparecen
- Verificar configuración de `logback-spring.xml`
- Confirmar que el directorio `logs/` existe
- Verificar permisos de escritura

### 2. Logs Muy Verbosos
- Ajustar niveles en `application-logging.yaml`
- Usar filtros específicos por paquete
- Configurar rotación de archivos

### 3. Performance Issues
- Reducir nivel de logs en producción
- Usar logback en lugar de log4j2
- Configurar rotación automática de archivos

## Monitoreo y Alertas

### 1. Métricas Disponibles
- Duración de peticiones HTTP
- Tasa de errores
- Uso de memoria y CPU
- Conexiones de base de datos

### 2. Endpoints de Actuator
- `/actuator/health`: Estado de la aplicación
- `/actuator/metrics`: Métricas del sistema
- `/actuator/prometheus`: Métricas en formato Prometheus

### 3. Integración con Herramientas
- Prometheus para métricas
- Grafana para visualización
- ELK Stack para análisis de logs
- Zipkin para trazabilidad distribuida
