package co.com.projectve.model.creditapplication.gateways;

import reactor.core.publisher.Mono;

public interface LoanTypeRepository {
    Mono<LoanTypeInfo> findLoanTypeById(Short loanTypeId);
}

