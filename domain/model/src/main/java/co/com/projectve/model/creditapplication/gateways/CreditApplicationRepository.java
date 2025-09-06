package co.com.projectve.model.creditapplication.gateways;

import co.com.projectve.model.creditapplication.CreditApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface CreditApplicationRepository {
    Mono<CreditApplication>saveRequest(CreditApplication creditApplication);
    Flux<CreditApplication> listRequest(); // contrato para el listado completo de solicitudes
    /**
     * Calcula la suma de todos los montos de crédito para un email dado.
     * @param email El email del usuario.
     * @return Mono que emite el total de créditos.
     */
    Mono<BigDecimal> sumAllCreditsByEmail(String email);
}
