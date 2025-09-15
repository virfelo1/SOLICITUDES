package co.com.projectve.model.creditapplication.gateways;

import co.com.projectve.model.creditapplication.CreditApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface CreditApplicationRepository {
    Mono<CreditApplication>saveRequest(CreditApplication creditApplication);
    Flux<CreditApplication>listAllEnriched();// contrato para la lista de solicitudes
    Mono<CreditApplication>updateState(CreditApplication creditApplication); // contrato para actualizar el estado del credito con el PUT
    Mono<CreditApplication> findByEmailAndIdRequest(String email, Integer idRequest);//contrato para validar si existe el email y el ID en la a base de datos de solicitudes
    Mono<CreditApplication>borrowingCapacity(BigDecimal baseSalary); //contrato para calcular la capacidad de endeudamiento maxima
    Mono<CreditApplication> findById(Integer idRequest); //contrato para buscar solicitud por ID
}
