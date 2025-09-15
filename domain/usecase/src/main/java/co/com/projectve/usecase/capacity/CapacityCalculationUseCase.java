package co.com.projectve.usecase.capacity;

import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.gateways.CapacityCalculationService;
import co.com.projectve.model.creditapplication.gateways.LoanTypeRepository;
import co.com.projectve.usecase.capacity.exception.CapacityCalculationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CapacityCalculationUseCase {
    
    private final CapacityCalculationService capacityCalculationService;
    private final LoanTypeRepository loanTypeRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(CapacityCalculationUseCase.class);
    
    public Mono<Void> enqueueCapacityCalculation(CreditApplication creditApplication) {
        String requestId = generateRequestId();
        logger.trace("[{}] Iniciando cálculo de capacidad para email: {}",
                    requestId, creditApplication.getEmail());
        
        logger.info("[{}] Datos de entrada - Monto: {}, Plazo: {} meses, ID Solicitud: {}",
                   requestId, creditApplication.getCreditAmount(),
                   creditApplication.getCreditTime(), creditApplication.getIdRequest());
        
        // 1. Obtener información del tipo de préstamo
        return loanTypeRepository.findLoanTypeById(creditApplication.getIdLoanType())
                .flatMap(loanType -> {
                    logger.debug("[{}] Tipo de préstamo: {}, Tasa: {}%", 
                               requestId, loanType.nameLoanType(), loanType.interestRate());
                    
                    // 2. Encolar para procesamiento por Lambda externa
                    return capacityCalculationService.enqueueCapacityCalculation(creditApplication);
                })
                .doOnSuccess(result -> logger.info("[{}] Solicitud encolada exitosamente para Lambda externa. ID: {}",
                           requestId, creditApplication.getIdRequest()))
                .doOnError(error -> logger.error("[{}] Error encolando solicitud: {}", requestId, error.getMessage(), error))
                .onErrorMap(throwable -> new CapacityCalculationException("Error encolando cálculo de capacidad", throwable));
    }
    
    private String generateRequestId() {
        return String.format("CAP-%d", System.currentTimeMillis() % 100000);
    }
}