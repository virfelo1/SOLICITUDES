package co.com.projectve.sqs.sender;

import co.com.projectve.model.creditapplication.gateways.CapacityCalculationService;
import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@Log4j2
@RequiredArgsConstructor
public class CapacityCalculationSQSSender implements CapacityCalculationService {
    
    private final SqsAsyncClient client;
    private final SQSSenderProperties properties;
    private final ObjectMapper objectMapper;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    @Override
    public Mono<Void> enqueueCapacityCalculation(CreditApplication creditApplication) {
        log.info("Encolando solicitud de cálculo de capacidad para email: {}", 
                creditApplication.getEmail());
        log.debug("Datos de la solicitud: ID={}, Monto={}, Plazo={}, Email={}", 
                 creditApplication.getIdRequest(), creditApplication.getCreditAmount(), 
                 creditApplication.getCreditTime(), creditApplication.getEmail());
        log.info("Perfil activo: {}", activeProfile);
        log.info("URL de la cola SQS: {}", properties.capacityQueueUrl());
        log.info("Region: {}", properties.region());
        log.info("Queue URL: {}", properties.queueUrl());
        log.info("Endpoint: {}", properties.endpoint());

        return Mono.fromCallable(() -> {
                    try {
                        String messageBody = objectMapper.writeValueAsString(creditApplication);
                        log.trace("Mensaje JSON generado: {}", messageBody);
                        return buildRequest(messageBody);
                    } catch (Exception e) {
                        log.error("Error serializando creditApplication: {}", e.getMessage(), e);
                        throw new RuntimeException("Error serializando creditApplication", e);
                    }
                })
                .flatMap(requestMessage -> Mono.fromFuture(client.sendMessage(requestMessage)))
                .doOnNext(response -> log.info("Mensaje de capacidad enviado a SQS exitosamente. MessageId: {}", response.messageId()))
                .doOnError(error -> log.error("Error enviando mensaje de capacidad a SQS: {}", error.getMessage(), error))
                .then();
    }
    
    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.capacityQueueUrl()) // URL de la cola estándar
                .messageBody(message)
                .build();
    }
}
