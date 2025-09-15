package co.com.projectve.sqs.sender;

import co.com.projectve.model.creditapplication.CapacityCalculationMessage;
import co.com.projectve.model.creditapplication.mapper.CapacityCalculationMessageMapper;
import co.com.projectve.model.creditapplication.CapacityCalculationResult;
import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.gateways.ActiveLoanRepository;
import co.com.projectve.model.creditapplication.gateways.CapacityCalculationGateway;
import co.com.projectve.model.creditapplication.gateways.EnrichedCapacityCalculationService;
import co.com.projectve.model.creditapplication.gateways.LoanTypeRepository;
import co.com.projectve.sqs.sender.config.SQSSenderProperties;
import co.com.projectve.shared.clients.AuthClient;
import co.com.projectve.shared.dto.UserInfoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.math.BigDecimal;

@Service
@Log4j2
@RequiredArgsConstructor
public class EnrichedCapacityCalculationSQSSender implements EnrichedCapacityCalculationService {
    
    private final SqsAsyncClient client;
    private final SQSSenderProperties properties;
    private final ObjectMapper objectMapper;
    private final CapacityCalculationMessageMapper messageMapper;
    private final LoanTypeRepository loanTypeRepository;
    private final CapacityCalculationGateway capacityCalculationGateway;
    private final ActiveLoanRepository activeLoanRepository;
    private final AuthClient authClient;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    @Override
    public Mono<Void> enqueueEnrichedCapacityCalculation(CreditApplication creditApplication) {
        log.info("Encolando solicitud enriquecida de cálculo de capacidad para email: {}", 
                creditApplication.getEmail());
        log.debug("Datos de la solicitud: ID={}, Monto={}, Plazo={}, Email={}", 
                 creditApplication.getIdRequest(), creditApplication.getCreditAmount(), 
                 creditApplication.getCreditTime(), creditApplication.getEmail());
        
        return Mono.zip(
                loanTypeRepository.findLoanTypeById(creditApplication.getIdLoanType()),
                authClient.findUserByEmail(creditApplication.getEmail()),
                activeLoanRepository.findActiveLoansByEmail(creditApplication.getEmail()).collectList()
        )
        .flatMap(tuple -> {
            var loanTypeInfo = tuple.getT1();
            var userInfo = tuple.getT2();
            var activeLoans = tuple.getT3();
            
            // Validar que el usuario existe y tiene salario base
            if (userInfo == null || userInfo.getBaseSalary() == null) {
                log.error("Usuario no encontrado o sin salario base para email: {}", creditApplication.getEmail());
                return Mono.error(new RuntimeException("Usuario no encontrado o sin salario base"));
            }
            
            // Calcular capacidad con el salario base real
            return capacityCalculationGateway.calculateBorrowingCapacity(creditApplication.getEmail(), userInfo.getBaseSalary())
                    .map(capacityResult -> {
                        log.info("Datos enriquecidos obtenidos - Salario: {}, Tasa: {}, Préstamos activos: {}", 
                                userInfo.getBaseSalary(), loanTypeInfo.interestRate(), activeLoans.size());
                        return messageMapper.toMessage(creditApplication, loanTypeInfo, userInfo.getBaseSalary(), capacityResult, activeLoans);
                    });
        })
        .flatMap(this::sendEnrichedMessage)
        .doOnSuccess(result -> log.info("Mensaje enriquecido enviado exitosamente para solicitud ID: {}", creditApplication.getIdRequest()))
        .doOnError(error -> log.error("Error enviando mensaje enriquecido: {}", error.getMessage(), error));
    }
    
    private Mono<Void> sendEnrichedMessage(CapacityCalculationMessage message) {
        return Mono.fromCallable(() -> {
                    try {
                        String messageBody = objectMapper.writeValueAsString(message);
                        log.trace("Mensaje enriquecido JSON generado: {}", messageBody);
                        return buildRequest(messageBody);
                    } catch (Exception e) {
                        log.error("Error serializando mensaje enriquecido: {}", e.getMessage(), e);
                        throw new RuntimeException("Error serializando mensaje enriquecido", e);
                    }
                })
                .flatMap(requestMessage -> Mono.fromFuture(client.sendMessage(requestMessage)))
                .doOnNext(response -> log.info("Mensaje enriquecido enviado a SQS exitosamente. MessageId: {}", response.messageId()))
                .doOnError(error -> log.error("Error enviando mensaje enriquecido a SQS: {}", error.getMessage(), error))
                .then();
    }
    
    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.capacityQueueUrl())
                .messageBody(message)
                .build();
    }
}
