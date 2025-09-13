package co.com.projectve.usecase.creditapplication;


import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.projectve.model.creditapplication.gateways.NotificationService;
import co.com.projectve.usecase.creditapplication.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RequiredArgsConstructor
public class CreditApplicationUseCase {
    private final CreditApplicationRepository creditApplicationRepository;
    private final NotificationService notificationService;
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

    public Flux<CreditApplication> listAllEnriched() {
        // Caso de uso para obtener el listado completo de solicitudes desde el repositorio
        return creditApplicationRepository.listAllEnriched();
    }

    // Contrato para buscar una solicitud por correo electrónico
    public Mono<CreditApplication> findByEmail(String email) {
        logger.trace("Buscando solicitud por email en el repositorio: {}", email);
        return creditApplicationRepository.findByEmail(email);
    }

    // Contrato para actualizar el estado de una solicitud
    public Mono<CreditApplication> updateStateAndNotify(String email, String state) {
        logger.trace("[updateStateAndNotify] Iniciando proceso de actualización de estado para email: {} y estado: {}", email, state);

        return creditApplicationRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new BusinessException("Solicitud con email '" + email + "' el correo no tiene asociado una solicitud de credito.")))
                .flatMap(creditApplication -> {
                    logger.trace("Solicitud encontrada. Actualizando estado.");
                    short newStateId = mapStateToId(state);
                    creditApplication.setIdState(newStateId);
                    logger.debug("Estado de la solicitud para el email {} cambiado a ID: {}", email, newStateId);
                    return creditApplicationRepository.updateState(creditApplication);
                })
                .flatMap(updatedApp -> {
                    if (updatedApp.getIdState() == 2 || updatedApp.getIdState() == 3) {
                        logger.trace("El estado es Aprobado o Rechazado. Enviando mensaje de notificación.");

                        // Construye el JSON dinámicamente en el caso de uso.
                        // Esto asegura que el mensaje contenga los datos correctos para cualquier solicitud.
                        String message = String.format("{\"email\":\"%s\", \"estadoFinal\":\"%s\"}",
                                updatedApp.getEmail(), state);

                        logger.debug("Mensaje a enviar: {}", message);

                        return notificationService.sendNotification(message)
                                .thenReturn(updatedApp);
                    } else {
                        logger.trace("El estado no requiere notificación. Proceso finalizado.");
                        return Mono.just(updatedApp);
                    }
                })
                .doOnSuccess(finalApp -> logger.info("[updateStateAndNotify] Proceso completado exitosamente para solicitud ID: {}", finalApp.getIdRequest()))
                .doOnError(error -> logger.error("[updateStateAndNotify] Error en el proceso: {}", error.getMessage()));
    }

    private short mapStateToId(String state) {
        return switch (state) {
            case "Aprobado" -> 2;
            case "Rechazado" -> 3;
            default -> 1;
        };
    }

}