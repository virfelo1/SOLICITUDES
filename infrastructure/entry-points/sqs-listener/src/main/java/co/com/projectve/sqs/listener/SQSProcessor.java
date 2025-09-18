package co.com.projectve.sqs.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {
    
    private final CapacityResultProcessor capacityResultProcessor;

    @Override
    public Mono<Void> apply(Message message) {
        // ✅ DELEGAMOS al CapacityResultProcessor que tiene todos los logs
        return capacityResultProcessor.processMessage(message);
    }
}
