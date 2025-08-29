package co.com.projectve.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class LoggingFilter implements WebFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        String timestamp = LocalDateTime.now().format(formatter);
        
        // Log de la petición entrante
        logger.trace("[{}] [{}] Petición HTTP recibida: {} {} - Headers: {}", 
                requestId, timestamp, 
                exchange.getRequest().getMethod(), 
                exchange.getRequest().getPath(),
                exchange.getRequest().getHeaders());
        
        // Agregar el requestId a los headers para rastreo
        exchange.getRequest().mutate()
                .header("X-Request-ID", requestId)
                .build();
        
        long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange)
                .doFinally(signalType -> {
                    long duration = System.currentTimeMillis() - startTime;
                    String responseStatus = exchange.getResponse().getStatusCode() != null ? 
                            exchange.getResponse().getStatusCode().toString() : "UNKNOWN";
                    
                    logger.trace("[{}] [{}] Respuesta HTTP enviada: {} - Duración: {}ms - Señal: {}", 
                            requestId, timestamp, responseStatus, duration, signalType);
                    
                    // Log de métricas básicas
                    if (duration > 1000) {
                        logger.warn("[{}] Petición lenta detectada: {} {} - Duración: {}ms", 
                                requestId, exchange.getRequest().getMethod(), exchange.getRequest().getPath(), duration);
                    } else {
                        logger.debug("[{}] Petición procesada: {} {} - Duración: {}ms", 
                                requestId, exchange.getRequest().getMethod(), exchange.getRequest().getPath(), duration);
                    }
                });
    }
}
