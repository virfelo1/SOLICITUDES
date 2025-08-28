package co.com.projectve.model.creditapplication.gateways;

import co.com.projectve.model.creditapplication.CreditApplication;
import reactor.core.publisher.Mono;

public interface CreditApplicationRepository {
    Mono<CreditApplication>saveRequest(CreditApplication creditApplication);

}
