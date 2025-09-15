package co.com.projectve.model.creditapplication.gateways;

import co.com.projectve.model.creditapplication.ActiveLoan;
import reactor.core.publisher.Flux;

public interface ActiveLoanRepository {
    Flux<ActiveLoan> findActiveLoansByEmail(String email);
}
