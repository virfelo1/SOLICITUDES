package co.com.projectve.model.creditapplication.gateways;

import co.com.projectve.model.creditapplication.CreditApplication;
import reactor.core.publisher.Mono;

public interface TransactionService {
    Mono<CreditApplication> updateStateAtomically(CreditApplication creditApplication, String newState);
    Mono<CreditApplication> updateStateWithCapacityResult(CreditApplication creditApplication, String capacityResult);
}
