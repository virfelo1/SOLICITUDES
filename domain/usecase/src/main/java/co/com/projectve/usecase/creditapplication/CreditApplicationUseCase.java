package co.com.projectve.usecase.creditapplication;


import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.gateways.CreditApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RequiredArgsConstructor
public class CreditApplicationUseCase {
    private final CreditApplicationRepository creditApplicationRepository;
    private static final Logger logger = LoggerFactory.getLogger(CreditApplicationUseCase.class);

    public Mono<CreditApplication> execute(CreditApplication creditApplication) {
        logger.trace("Iniciando ejecución del caso de uso para solicitud de crédito. ID: {}", creditApplication.getIdRequest());
        
        return Mono.just(creditApplication)
                .doOnNext(app -> {
                    logger.trace("Aplicando regla de negocio: estableciendo estado 'Pendiente de revision'");
                    logger.debug("Datos de la solicitud antes de procesar: tipoDocumento={}, numeroDocumento={}, montoCredito={}, plazoCredito={}, tipoCredito={}",
                            app.getDocumentType(), app.getDocumentNumber(), app.getCreditAmount(), app.getCreditTime(), app.getIdLoanType());
                })
                .flatMap(validatedApp -> {
                    validatedApp.setIdState((short) 1);
                    logger.trace("Estado de crédito actualizado a: {}", validatedApp.getIdState());
                    
                    logger.trace("Invocando repositorio para persistir la solicitud");
                    return creditApplicationRepository.saveRequest(validatedApp); // luego de validar guarda el requerimiento
                })
                .doOnNext(savedApp -> {
                    logger.info("Solicitud de crédito procesada exitosamente. ID: {}, Estado: {}", 
                            savedApp.getIdRequest(), savedApp.getIdState());
                })
                .doOnError(error -> {
                    logger.error("Error durante la ejecución del caso de uso: {}", error.getMessage(), error);
                })
                .doFinally(signalType -> {
                    logger.trace("Caso de uso finalizado. Señal: {}", signalType);
                });
    }

    /*public Flux<CreditApplication> listAllEnriched() {
        // Caso de uso para obtener el listado completo de solicitudes desde el repositorio
        return creditApplicationRepository.listAllEnriched();
    }*/

}