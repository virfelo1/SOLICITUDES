package co.com.projectve.api.globalException;

import co.com.projectve.usecase.infouser.exception.BusinessException;
import co.com.projectve.usecase.infouser.exception.TechnicalException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Order(-1) // Set a higher precedence to ensure it runs before the default handler
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorMessage = "Ocurrió un error inesperado.";

        if (throwable instanceof BusinessException) {
            status = HttpStatus.BAD_REQUEST;
            errorMessage = throwable.getMessage();
        } else if (throwable instanceof TechnicalException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorMessage = throwable.getMessage();
        }

        // This is a simplified way to create a JSON response in an error handler
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(
                        ("{\"error\":\"" + errorMessage + "\"}").getBytes()
                ))
        );
    }
}