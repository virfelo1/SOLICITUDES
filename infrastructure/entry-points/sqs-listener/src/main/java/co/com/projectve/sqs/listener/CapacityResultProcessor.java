package co.com.projectve.sqs.listener;

import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.projectve.model.creditapplication.gateways.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;
import co.com.projectve.sqs.listener.dto.CapacityResultDTO;

@Component
@Slf4j
@RequiredArgsConstructor
public class CapacityResultProcessor {

    private final ObjectMapper objectMapper;
    private final CreditApplicationRepository creditApplicationRepository;
    private final TransactionService transactionService;

    // Este método ahora es el punto de entrada para procesar un mensaje de SQS
    public Mono<Void> processMessage(Message message) {
        String messageId = message.messageId();
        log.trace("[processMessage] Iniciando procesamiento del mensaje ID: {}", messageId);

        // ✅ LOG 1: JSON RAW que llega de SQS
        log.info("🔵 [SQS-RAW] JSON recibido de cola externa: {}", message.body());
        log.info("🔵 [SQS-RAW] Message ID: {}, Receipt Handle: {}",
                message.messageId(), message.receiptHandle());

        CapacityResultDTO capacityResultDTO;
        try {
            capacityResultDTO = objectMapper.readValue(message.body(), CapacityResultDTO.class);
            Short idState = mapStateStringToId(capacityResultDTO.getState());

            // ✅ LOG 2: Objeto deserializado
            log.info("🟢 [DESERIALIZADO] DTO creado - ID: {}, Email: {}, State: {}, IdState: {}",
                    capacityResultDTO.getIdRequest(),
                    capacityResultDTO.getEmail(),
                    capacityResultDTO.getState(),
                    idState);

            log.info("[processMessage] JSON deserializado correctamente. ID Solicitud: {}, Estado: {}",
                    capacityResultDTO.getIdRequest(), idState);

            // Corrected: Pass idState as a variable to the method
            return updateApplicationWithCapacityResult(capacityResultDTO.getIdRequest(), idState)
                    .doOnSuccess(result -> {
                        // ✅ LOG 8: Proceso completo exitoso
                        log.info("🎉 [PROCESO-COMPLETO] Mensaje SQS procesado exitosamente - ID: {}, Estado final: {}", 
                                result.getIdRequest(), result.getIdState());
                        log.info("🎉 [PROCESO-COMPLETO] JSON original: {}", message.body());
                        log.info("[processMessage] Solicitud {} actualizada exitosamente con estado: {}",
                                result.getIdRequest(), result.getIdState());
                    })
                    .doOnError(error -> {
                        // ✅ LOG 9: Error en el proceso completo
                        log.error("💥 [PROCESO-ERROR] Error procesando mensaje SQS - JSON: {}, Error: {}", 
                                message.body(), error.getMessage(), error);
                        log.error("💥 [PROCESO-ERROR] ID Request: {}", capacityResultDTO.getIdRequest(), error);
                        log.error("[processMessage] Fallo en la actualización de la base de datos para ID {}: {}",
                                capacityResultDTO.getIdRequest(), error.getMessage(), error);
                    })
                    .then();

        } catch (Exception e) {
            // ✅ LOG 10: Error en deserialización
            log.error("🔴 [DESERIALIZAR-ERROR] Error deserializando JSON de SQS: {}", message.body());
            log.error("🔴 [DESERIALIZAR-ERROR] Excepción: {}", e.getMessage(), e);
            log.error("[processMessage] ERROR: Fallo al deserializar el mensaje SQS: {}", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    private Mono<CreditApplication> updateApplicationWithCapacityResult(Integer idRequest, Short idState) {
        // ✅ LOG 3: Parámetros que llegan al método de actualización
        log.info("🟡 [UPDATE-ENTRADA] Iniciando actualización BD - ID: {}, IdState: {}", idRequest, idState);
        
        log.trace("[updateApplicationWithCapacityResult] Actualizando solicitud ID: {} con estado: {}",
                idRequest, idState);

        return creditApplicationRepository.findById(idRequest)
                .switchIfEmpty(Mono.error(new RuntimeException("Solicitud no encontrada: " + idRequest)))
                .doOnNext(found -> {
                    // ✅ LOG 4: Solicitud encontrada en BD
                    log.info("[BD-FOUND] Solicitud encontrada - ID: {}, Estado actual: {}, Email: {}", 
                             found.getIdRequest(), found.getIdState(), found.getEmail());
                })
                .flatMap(creditApplication -> {
                    log.debug("Solicitud encontrada. Actualizando estado con resultado de capacidad");
                    
                    // ✅ LOG 5: Antes de actualizar el estado
                    log.info("🔄 [BD-UPDATE] Cambiando estado de {} a {} para solicitud {}", 
                             creditApplication.getIdState(), idState, creditApplication.getIdRequest());
                    
                    creditApplication.setIdState(idState);
                    
                    return transactionService.updateStateWithCapacityResult(creditApplication, mapStateToString(idState));
                })
                .doOnSuccess(result -> {
                    // ✅ LOG 6: Actualización exitosa
                    log.info("✅ [BD-SUCCESS] Solicitud {} actualizada exitosamente - Nuevo estado: {} ({})", 
                             result.getIdRequest(), result.getIdState(), mapStateToString(result.getIdState()));
                })
                .doOnError(error -> {
                    // ✅ LOG 7: Error en actualización
                    log.error("[BD-ERROR] Error actualizando solicitud {}: {}", idRequest, error.getMessage(), error);
                    log.error("Error actualizando solicitud con resultado de capacidad: {}", error.getMessage(), error);
                });
    }

    private String mapStateToString(Short stateId) {
        return switch (stateId) {
            case 2 -> "Aprobado";
            case 3 -> "Rechazado";
            case 4 -> "Revision Manual";
            default -> "Pendiente";
        };
    }

    private Short mapStateStringToId(String state) {
        Short mappedId = switch (state) {
            case "Aprobado" -> 2;
            case "Rechazado" -> 3;
            case "Revision Manual" -> 4;
            default -> 1; // Pendiente
        };
        
        // ✅ LOG 11: Mapeo de estado
        log.info("🔄 [MAPEO-ESTADO] String '{}' mapeado a ID: {}", state, mappedId);
        
        return mappedId;
    }
}
