package co.com.projectve.runner;

import co.com.projectve.sqs.listener.CapacityResultProcessor;
import co.com.projectve.sqs.listener.config.SQSProperties;
import co.com.projectve.sqs.listener.helper.SQSListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Component
@Log4j2
@RequiredArgsConstructor
public class SQSListenerRunner {

    private final SqsAsyncClient sqsClient;
    private final SQSProperties properties;
    private final CapacityResultProcessor processor;

    // ❌ DESACTIVADO: Evitar duplicación con SQSConfig.sqsListener que usa SQSProcessor
    // @EventListener(ApplicationReadyEvent.class)
    // public void startSQSListener() {
    //     log.info("Iniciando SQS Listener para resultados de capacidad de endeudamiento...");
    //
    //     SQSListener.builder()
    //             .client(sqsClient)
    //             .properties(properties)
    //             .processor(processor::processMessage)
    //             .build()
    //             .start();
    // }
}