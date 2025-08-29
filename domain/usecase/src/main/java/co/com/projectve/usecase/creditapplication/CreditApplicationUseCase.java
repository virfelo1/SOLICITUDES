package co.com.projectve.usecase.creditapplication;


import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.projectve.usecase.creditapplication.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class CreditApplicationUseCase {
    private final CreditApplicationRepository creditApplicationRepository;
    private static final Logger logger = LoggerFactory.getLogger(CreditApplicationUseCase.class);

    public Mono<CreditApplication> execute(CreditApplication creditApplication) {
        logger.trace("Iniciando ejecución del caso de uso para solicitud de crédito. ID: {}", creditApplication.getId());
        
        return Mono.just(creditApplication)
                .doOnNext(app -> {
                    logger.trace("Aplicando regla de negocio: estableciendo estado 'Pendiente de revision'");
                    logger.debug("Datos de la solicitud antes de procesar: tipoDocumento={}, numeroDocumento={}, montoCredito={}, plazoCredito={}, tipoCredito={}",
                            app.getDocumentType(), app.getDocumentNumber(), app.getCreditAmount(), app.getCreditTime(), app.getTypeCredit());
                })
                .flatMap(validatedApp -> {
                    validatedApp.setCreditStatus("Pendiente de revision");
                    logger.trace("Estado de crédito actualizado a: {}", validatedApp.getCreditStatus());
                    
                    logger.trace("Invocando repositorio para persistir la solicitud");
                    return creditApplicationRepository.saveRequest(validatedApp);
                })
                .doOnNext(savedApp -> {
                    logger.info("Solicitud de crédito procesada exitosamente. ID: {}, Estado: {}", 
                            savedApp.getId(), savedApp.getCreditStatus());
                })
                .doOnError(error -> {
                    logger.error("Error durante la ejecución del caso de uso: {}", error.getMessage(), error);
                })
                .doFinally(signalType -> {
                    logger.trace("Caso de uso finalizado. Señal: {}", signalType);
                });
    }
}