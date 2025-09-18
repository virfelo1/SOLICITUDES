package co.com.projectve.r2dbc.adapters;

import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.gateways.AutomaticValidationService;
import co.com.projectve.model.creditapplication.gateways.EnrichedCapacityCalculationService;
import co.com.projectve.model.creditapplication.gateways.LoanTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class AutomaticValidationAdapter implements AutomaticValidationService {
    
    private final LoanTypeRepository loanTypeRepository;
    private final EnrichedCapacityCalculationService enrichedCapacityCalculationService;
    
    @Override
    public Mono<CreditApplication> processAutomaticValidation(CreditApplication creditApplication) {
        log.trace("[processAutomaticValidation] Verificando validacion automatica para solicitud ID: {}",
                 creditApplication.getIdRequest());
        
        return loanTypeRepository.findLoanTypeById(creditApplication.getIdLoanType())
                .flatMap(loanType -> {
                    if (loanType.autoValidation() != null && loanType.autoValidation()) {
                        log.info("Tipo de prestamo '{}' requiere validacion automatica. Iniciando proceso.",
                                loanType.nameLoanType());
                        return triggerCapacityCalculation(creditApplication);
                    } else {
                        log.debug("Tipo de prestamo '{}' no requiere validacion automatica. Continuando con estado pendiente.",
                                loanType.nameLoanType());
                        return Mono.just(creditApplication);
                    }
                })
                .switchIfEmpty(Mono.fromCallable(() -> {
                    log.warn("No se encontro informacion del tipo de prestamo ID: {}. Continuando sin validacion automatica.",
                            creditApplication.getIdLoanType());
                    return creditApplication;
                }))
                .doOnError(error -> log.error("Error verificando validación automatica: {}", error.getMessage(), error));
    }
    
    private Mono<CreditApplication> triggerCapacityCalculation(CreditApplication creditApplication) {
        log.trace("[triggerCapacityCalculation] Iniciando calculo de capacidad para solicitud ID: {}",
                 creditApplication.getIdRequest());
        
        return enrichedCapacityCalculationService.enqueueEnrichedCapacityCalculation(creditApplication)
                .thenReturn(creditApplication) // La solicitud se mantiene en estado "Pendiente" hasta recibir resultado
                .doOnSuccess(result -> log.info("Solicitud ID: {} encolada para validacion automatica. Estado: Pendiente de calculo",
                           result.getIdRequest()))
                .doOnError(error -> log.error("Error encolando validacion automatica para solicitud ID: {}: {}",
                           creditApplication.getIdRequest(), error.getMessage(), error))
                .onErrorReturn(creditApplication); // En caso de error, mantener el estado original
    }
}
