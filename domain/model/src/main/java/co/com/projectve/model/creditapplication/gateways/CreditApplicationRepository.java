package co.com.projectve.model.creditapplication.gateways;

import co.com.projectve.model.creditapplication.CreditApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditApplicationRepository {
    Mono<CreditApplication>saveRequest(CreditApplication creditApplication);
    Flux<CreditApplication> listRequest();
    Flux<CreditApplication> listAllEnriched();// contrato creado para arreglasr el Usecase
}
