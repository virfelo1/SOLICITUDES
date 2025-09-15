package co.com.projectve.r2dbc.adapters;

import co.com.projectve.model.creditapplication.gateways.ActiveLoanRepository;
import co.com.projectve.model.creditapplication.gateways.CapacityCalculationGateway;
import co.com.projectve.model.creditapplication.ActiveLoan;
import co.com.projectve.model.creditapplication.CapacityCalculationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
@RequiredArgsConstructor
public class CapacityCalculationAdapter implements CapacityCalculationGateway {
    
    private final ActiveLoanRepository activeLoanRepository;
    
    @Override
    public Mono<CapacityCalculationResult> calculateBorrowingCapacity(String email, BigDecimal baseSalary) {
        log.trace("[calculateBorrowingCapacity] Calculando capacidad básica para email: {}, salario: {}", email, baseSalary);
        
        // Solo calcular capacidad máxima de endeudamiento (35% del salario)
        // La Lambda externa se encargará de calcular deuda actual y capacidad disponible
        BigDecimal maxBorrowingCapacity = baseSalary.multiply(new BigDecimal("0.35"));
        log.debug("[calculateBorrowingCapacity] Capacidad máxima: {} (35% de {})", maxBorrowingCapacity, baseSalary);
        
        // Crear resultado básico - la Lambda externa hará los cálculos completos
        CapacityCalculationResult result = new CapacityCalculationResult(
                maxBorrowingCapacity,
                BigDecimal.ZERO, // Se calculará en la Lambda externa
                BigDecimal.ZERO, // Se calculará en la Lambda externa
                BigDecimal.ZERO, // Se calculará en la Lambda externa
                "PENDIENTE", // Se determinará en la Lambda externa
                "Datos básicos enviados a Lambda externa"
        );
        
        log.info("[calculateBorrowingCapacity] Datos básicos preparados - Capacidad máxima: {}", maxBorrowingCapacity);
        
        return Mono.just(result)
                .doOnError(error -> log.error("[calculateBorrowingCapacity] Error preparando datos: {}", error.getMessage(), error));
    }
}
