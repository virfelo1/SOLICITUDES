package co.com.projectve.model.creditapplication.gateways;

import co.com.projectve.model.creditapplication.CreditApplication;
import reactor.core.publisher.Mono;

public interface AutomaticValidationService {
    Mono<CreditApplication> processAutomaticValidation(CreditApplication creditApplication);
}
