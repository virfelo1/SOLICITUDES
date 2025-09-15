package co.com.projectve.api.globalException;

import co.com.projectve.usecase.capacity.exception.CapacityCalculationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class CapacityExceptionHandler {
    
    @ExceptionHandler(CapacityCalculationException.class)
    public Mono<ServerResponse> handleCapacityCalculationException(CapacityCalculationException ex) {
        log.error("[handleCapacityCalculationException] Error en cálculo de capacidad: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Error en cálculo de capacidad de endeudamiento");
        errorResponse.put("message", "No se pudo procesar la solicitud de cálculo de capacidad. Por favor, intente nuevamente.");
        errorResponse.put("details", ex.getMessage());
        errorResponse.put("path", "/api/v1/calcular-capacidad");
        
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyValue(errorResponse);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public Mono<ServerResponse> handleRuntimeException(RuntimeException ex) {
        // Solo manejar excepciones relacionadas con capacidad de endeudamiento
        if (ex.getMessage() != null && ex.getMessage().contains("capacidad")) {
            log.error("[handleRuntimeException] Error en runtime relacionado con capacidad: {}", ex.getMessage(), ex);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            errorResponse.put("error", "Servicio temporalmente no disponible");
            errorResponse.put("message", "El servicio de cálculo de capacidad no está disponible en este momento. Por favor, intente más tarde.");
            errorResponse.put("path", "/api/v1/calcular-capacidad");
            
            return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .bodyValue(errorResponse);
        }
        
        // Si no es relacionado con capacidad, dejar que se maneje por el handler global
        return Mono.error(ex);
    }
}
