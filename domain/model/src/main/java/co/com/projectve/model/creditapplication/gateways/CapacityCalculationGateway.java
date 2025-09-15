package co.com.projectve.model.creditapplication.gateways;

import co.com.projectve.model.creditapplication.CapacityCalculationResult;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

public interface CapacityCalculationGateway {
    Mono<CapacityCalculationResult> calculateBorrowingCapacity(String email, BigDecimal baseSalary);
}
