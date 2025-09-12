package co.com.projectve.model.creditapplication.gateways;

import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<Void> sendNotification(String message);
}
