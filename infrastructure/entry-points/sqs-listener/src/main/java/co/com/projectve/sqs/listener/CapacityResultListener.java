package co.com.projectve.sqs.listener;

import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.projectve.model.creditapplication.gateways.TransactionService;
import co.com.projectve.sqs.listener.config.SQSListenerProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CapacityResultListener {
    
    private final SqsAsyncClient sqsClient;
    private final SQSListenerProperties properties;
    private final ObjectMapper objectMapper;
    private final CreditApplicationRepository creditApplicationRepository;
    private final TransactionService transactionService;
    
    public void startListening() {
        log.info("[startListening] Iniciando listener SQS para resultados de capacidad de endeudamiento");
        
        Mono.fromRunnable(() -> {
            while (true) {
                try {
                    receiveAndProcessMessages();
                    Thread.sleep(Duration.ofSeconds(5).toMillis()); // Poll cada 5 segundos
                } catch (InterruptedException e) {
                    log.warn("Listener interrumpido");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Error en el listener SQS: {}", e.getMessage(), e);
                }
            }
        }).subscribe();
    }
    
    private void receiveAndProcessMessages() {
        log.trace("[receiveAndProcessMessages] Recibiendo mensajes de SQS");
        
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .maxNumberOfMessages(10)
                .waitTimeSeconds(20)
                .visibilityTimeout(30)
                .build();
        
        sqsClient.receiveMessage(receiveRequest)
                .thenAccept(response -> {
                    List<Message> messages = response.messages();
                    log.debug("Recibidos {} mensajes de SQS", messages.size());
                    
                    for (Message message : messages) {
                        processMessage(message)
                                .doOnSuccess(result -> deleteMessage(message))
                                .doOnError(error -> log.error("Error procesando mensaje: {}", error.getMessage(), error))
                                .subscribe();
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Error recibiendo mensajes de SQS: {}", throwable.getMessage(), throwable);
                    return null;
                });
    }
    
    private Mono<Void> processMessage(Message message) {
        String messageId = message.messageId();
        log.trace("[processMessage] Procesando mensaje ID: {}", messageId);
        
        try {
            CreditApplication capacityResult = objectMapper.readValue(message.body(), CreditApplication.class);
            log.info("[processMessage] Resultado de capacidad recibido - ID Solicitud: {}, Estado: {}", 
                    capacityResult.getIdRequest(), capacityResult.getIdState());
            
            return updateApplicationWithCapacityResult(capacityResult)
                    .doOnSuccess(result -> log.info("[processMessage] Solicitud {} actualizada exitosamente con estado: {}", 
                               result.getIdRequest(), result.getIdState()))
                    .then();
                    
        } catch (Exception e) {
            log.error("[processMessage] Error parseando mensaje SQS: {}", e.getMessage(), e);
            return Mono.error(new RuntimeException("Error procesando mensaje de capacidad", e));
        }
    }
    
    private Mono<CreditApplication> updateApplicationWithCapacityResult(CreditApplication capacityResult) {
        log.trace("[updateApplicationWithCapacityResult] Actualizando solicitud ID: {} con estado: {}", 
                 capacityResult.getIdRequest(), capacityResult.getIdState());
        
        return creditApplicationRepository.findById(capacityResult.getIdRequest())
                .switchIfEmpty(Mono.error(new RuntimeException("Solicitud no encontrada: " + capacityResult.getIdRequest())))
                .flatMap(creditApplication -> {
                    log.debug("Solicitud encontrada. Actualizando estado con resultado de capacidad");
                    creditApplication.setIdState(capacityResult.getIdState());
                    return transactionService.updateStateWithCapacityResult(creditApplication, mapStateToString(capacityResult.getIdState()));
                })
                .doOnError(error -> log.error("Error actualizando solicitud con resultado de capacidad: {}", error.getMessage(), error));
    }
    
    private String mapStateToString(Short stateId) {
        return switch (stateId) {
            case 2 -> "Aprobado";
            case 3 -> "Rechazado";
            case 4 -> "Revisión Manual";
            default -> "Pendiente";
        };
    }
    
    private void deleteMessage(Message message) {
        log.trace("[deleteMessage] Eliminando mensaje procesado ID: {}", message.messageId());
        
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .receiptHandle(message.receiptHandle())
                .build();
        
        sqsClient.deleteMessage(deleteRequest)
                .thenAccept(response -> log.debug("Mensaje eliminado exitosamente"))
                .exceptionally(throwable -> {
                    log.error("Error eliminando mensaje: {}", throwable.getMessage(), throwable);
                    return null;
                });
    }
}
