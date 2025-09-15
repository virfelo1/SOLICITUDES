package co.com.projectve.model.creditapplication.gateways;

import co.com.projectve.model.creditapplication.CreditApplication;
import reactor.core.publisher.Mono;

public interface EnrichedCapacityCalculationService {
    Mono<Void> enqueueEnrichedCapacityCalculation(CreditApplication creditApplication);
}
