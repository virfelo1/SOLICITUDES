package co.com.projectve.api.globalException;

import co.com.projectve.usecase.creditapplication.exception.BusinessException;
import co.com.projectve.usecase.creditapplication.exception.TechnicalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import jakarta.validation.ConstraintViolationException;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Order(-1) // Set a higher precedence to ensure it runs before the default handler
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, Object> errorBody = Map.of("error", "Ocurrió un error inesperado.");

        if (throwable instanceof ConstraintViolationException) {
            ConstraintViolationException ex = (ConstraintViolationException) throwable;
            status = HttpStatus.BAD_REQUEST;
            errorBody = ex.getConstraintViolations().stream()
                    .collect(Collectors.toMap(
                            violation -> violation.getPropertyPath().toString(),
                            ConstraintViolation::getMessage
                    ));
        } else if (throwable instanceof BusinessException) {
            status = HttpStatus.BAD_REQUEST;
            errorBody = Map.of("error", throwable.getMessage());
        } else if (throwable instanceof TechnicalException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorBody = Map.of("error", throwable.getMessage());
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] responseBytes = objectMapper.writeValueAsBytes(errorBody);
            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(responseBytes))
            );
        } catch (Exception e) {
            byte[] fallback = "{\"error\":\"No se pudo procesar el error.\"}"
                    .getBytes(StandardCharsets.UTF_8);
            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(fallback))
            );
        }
    }
}