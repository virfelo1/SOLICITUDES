package co.com.projectve.model.creditapplication.gateways;

import co.com.projectve.model.creditapplication.CreditApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditApplicationRepository {
    Mono<CreditApplication>saveRequest(CreditApplication creditApplication);
    Flux<CreditApplication> listAllEnriched();// contrato creado para arreglasr el Usecase
    Mono<CreditApplication>updateState(CreditApplication creditApplication); // contrato para actualizar el estado del credito con el PUT
    Mono<CreditApplication> findByEmail(String email);//contrato para validar si existe el email en la a base de datos de solicitudes
    //Mono<CreditApplication> sendNotification(String message); //esta es el contrato de enviar notificacion
}
