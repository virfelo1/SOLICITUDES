package co.com.projectve.sqs.sender;

import co.com.projectve.model.creditapplication.gateways.NotificationService;
import co.com.projectve.sqs.sender.config.SQSSenderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements NotificationService {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;

    @Override
    public Mono<Void> sendNotification(String message) {
        return Mono.fromCallable(() -> buildRequest(message))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .doOnError(error -> log.error("Error al enviar mensaje a SQS: {}", error.getMessage()))
                .then(); // Retorna un Mono<Void> ya que el contrato no espera un messageId
    }

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(message)
                .build();
    }
}
